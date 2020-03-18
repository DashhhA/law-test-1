package com.lawtest.model;

import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.lawtest.MainActivity;
import com.lawtest.util.MultiTaskCompleteWatcher;
import com.lawtest.util.crypto;
import com.lawtest.util.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;

public class PersonRepository<T extends BasePerson> {
    public final static String EMAIL_TO_SALT_TAG = "emailToSalt";

    private MutableLiveData<T> data;
    private T localPerson;
    private Class<T> tClass;

    private FirebaseAuth auth;
    private DatabaseReference database;
    private StorageReference storage;

    private PersonLocalService localService;
    private PersonWebService webService;

    public PersonRepository(FirebaseAuth auth, DatabaseReference database, StorageReference storage, Class<T> tClass) {
        this.auth = auth;
        this.database = database;
        this.storage = storage;
        this.tClass = tClass;

        localService = new PersonLocalService();
        webService = new PersonWebService();
        data = new MutableLiveData<>();
    }

    public LiveData<T> getPerson(String email, final String password) {
        MultiTaskCompleteWatcher watcher = new MultiTaskCompleteWatcher() {
            @Override
            public void allComplete() {
                data.postValue(localPerson);
            }

            @Override
            public void onTaskFailed(Task task, Exception exception) {
                // TODO
            }
        };
        final MultiTaskCompleteWatcher.Task fromLocalDB = watcher.newTask();
        final MultiTaskCompleteWatcher.Task fromRemoteDB = watcher.newTask();
        final MultiTaskCompleteWatcher.Task saveTask = watcher.newTask();

        localService.getPerson(email, password, new OnPersonResolvedListener() {
            @Override
            public void onResolved(Object person) {
                if (person != null) {
                    localPerson = (T) person;
                    data.postValue(localPerson);
                }
                fromLocalDB.complete();
            }

            @Override
            public void onFailed(Exception exception) {}
        });
        webService.getPerson(email, password, new OnPersonResolvedListener() {
            @Override
            public void onResolved(Object person) {
                localPerson = (T) person;
                data.postValue(localPerson);
                fromRemoteDB.complete();
                localService.savePerson(localPerson, saveTask);
            }

            @Override
            public void onFailed(Exception exception) {

            }
        });

        // слушаем изменения пользователя
        webService.authorise(email, password, onAuthorised, null);

        return data;
    }

    public void newPerson(final T person, final StateListener listener) {
        MultiTaskCompleteWatcher watcher = new MultiTaskCompleteWatcher() {
            @Override
            public void allComplete() {
                localPerson = person;
                data.postValue(localPerson);
                listener.onComplete();
            }

            @Override
            public void onTaskFailed(Task task, Exception exception) {
                listener.onFailure(exception);
            }
        };
        MultiTaskCompleteWatcher.Task localTask = watcher.newTask();
        MultiTaskCompleteWatcher.Task webTask = watcher.newTask();

        localService.savePerson(person, localTask);
        webService.newPerson(person, webTask);
    }

    public void savePerson(final StateListener listener) {
        MultiTaskCompleteWatcher watcher = new MultiTaskCompleteWatcher() {
            @Override
            public void allComplete() {
                data.postValue(localPerson);
                listener.onComplete();
            }

            @Override
            public void onTaskFailed(Task task, Exception exception) {
                listener.onFailure(exception);
            }
        };
        MultiTaskCompleteWatcher.Task localTask = watcher.newTask();
        MultiTaskCompleteWatcher.Task webTask = watcher.newTask();

        localService.savePerson(localPerson, localTask);
        webService.savePerson(webTask);
    }

    private interface OnPersonResolvedListener{
        void onResolved(Object person);
        void onFailed(Exception exception);
    }

    private class PersonLocalService {
        void getPerson(String email, String pass, OnPersonResolvedListener listener) {
            T person = utils.getPersonFromPrefs(getTag(tClass), tClass);
            if (
                    person != null &&
                    person.email.equals(email) &&
                    crypto.checkPass(pass, person.pass, person.salt)
            ) {
                listener.onResolved(person);
            } else {
                listener.onResolved(null);
            }
        }

        void savePerson(final T person, @Nullable final MultiTaskCompleteWatcher.Task task) {
            new Thread( new Runnable() {
                @Override
                public void run() {
                    // сохранение объекта
                    utils.saveToPrefs(person, getTag(tClass));

                    // удаление аватарки предидущего пользователя
                    ContextWrapper contextWrapper = new ContextWrapper(MainActivity.getInstance());
                    File directory = contextWrapper.getDir(utils.IMG_DIR, Context.MODE_PRIVATE);
                    File[] files = directory.listFiles();
                    if (person.getAvatarUri() != null) {
                        File avatar = new File(person.getAvatarUri().getPath());
                        for (File f : files) {
                            if ( Pattern.matches("^.+\\"+utils.AVATAR_FORMAT+"$", f.getName())
                                    && !f.equals(avatar) ) {
                                f.delete();
                            }
                        }
                    } else {
                        for (File f : files) {
                            if (Pattern.matches("^.+\\"+utils.AVATAR_FORMAT+"$", f.getName()))
                                f.delete();
                        }
                    }

                    if (task != null) task.complete();
                }
            }).start();
        }

        private String getTag(Class c) {
            if (c.equals(User.class)) return User.TAG;
            if (c.equals(Specialist.class)) return Specialist.TAG;
            return null;
        }
    }

    private class PersonWebService {
        private byte[] salt;
        void getPerson(final String email, final String pass, final OnPersonResolvedListener listener) {
            authorise(email, pass, new GetSignedInPersonImpl(listener), listener);
        }

        void savePerson(MultiTaskCompleteWatcher.Task task) {
            String pass = new String(localPerson.pass);
            auth.signInWithEmailAndPassword(localPerson.email, pass)
                    .addOnCompleteListener( new PutPersonImpl(localPerson, task));
        }

        void newPerson(T person, MultiTaskCompleteWatcher.Task task) {
            String pass = new String(person.pass);
            auth.createUserWithEmailAndPassword(person.email, pass)
                    .addOnCompleteListener( new PutPersonImpl(person, task));
        }

        void authorise(final String email, final String password,
                       final OnCompleteListener<AuthResult> completeListener,
                       @Nullable final OnPersonResolvedListener listener) {
            ValueEventListener saltEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    GenericTypeIndicator<ArrayList<Integer>> typeIndicator = new GenericTypeIndicator<ArrayList<Integer>>() {};
                    ArrayList<Integer> list = dataSnapshot.getValue(typeIndicator);
                    salt = utils.arrayToBytes(list);

                    auth.signInWithEmailAndPassword(email, crypto.getPassBySalt(password, salt))
                            .addOnCompleteListener( completeListener );
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    if (listener != null) listener.onFailed(databaseError.toException());
                }
            };

            database
                    .child(EMAIL_TO_SALT_TAG)
                    .child(utils.emailForDatabase(email))
                    .addListenerForSingleValueEvent(saltEventListener);
        }
    }

    class GetSignedInPersonImpl implements OnCompleteListener<AuthResult> {
        private OnPersonResolvedListener listener;
        private T person;

        GetSignedInPersonImpl(final OnPersonResolvedListener listener){
            this.listener = listener;
        }

        private OnCompleteListener<byte []> avatarListener = new OnCompleteListener<byte[]>() {
            @Override
            public void onComplete(@NonNull Task<byte[]> task) {
                if (task.isSuccessful()) {
                    utils.saveBytesToFile(person.getAvatarUri(), task.getResult());
                    listener.onResolved(person);
                } else {
                    listener.onFailed(task.getException());
                }
            }
        };

        private ValueEventListener personEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GenericTypeIndicator<Map<String, Object>> typeIndicator =
                        new GenericTypeIndicator<Map<String, Object> >() {};
                Map<String, Object> map = dataSnapshot.getValue(typeIndicator);
                try {
                    person = tClass.newInstance();
                    person.fromMap(map);
                } catch (Exception e) {
                    // TODO
                }
                if (person != null && person.getAvatarUri() != null) {
                    storage.child(getDatabaseAvaFolder(tClass))
                            .child(person.getAvatarUri().getLastPathSegment())
                            .getBytes(utils.MAX_DOWNLOAD_BYTES)
                            .addOnCompleteListener(avatarListener);
                } else {
                    listener.onResolved(person);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onFailed(databaseError.toException());
            }
        };

        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            if (task.isSuccessful()) {

                String user_uid = auth.getCurrentUser().getUid();
                database
                        .child(getDatabaseTag(tClass))
                        .child(user_uid)
                        .addListenerForSingleValueEvent(personEventListener);

            } else {
                listener.onFailed(task.getException());
            }
        }
    }

    private class PutPersonImpl implements OnCompleteListener<AuthResult> {
        private T person;
        private MultiTaskCompleteWatcher.Task task;
        private MultiTaskCompleteWatcher taskCompleteWatcher = new MultiTaskCompleteWatcher() {
            @Override
            public void allComplete() {
                task.complete();
            }

            @Override
            public void onTaskFailed(Task task, Exception exception) {
                PutPersonImpl.this.task.fail(exception);
            }
        };
        private final MultiTaskCompleteWatcher.Task databaseTask = taskCompleteWatcher.newTask();
        private final MultiTaskCompleteWatcher.Task storageTask = taskCompleteWatcher.newTask();
        private final MultiTaskCompleteWatcher.Task saltTask = taskCompleteWatcher.newTask();

        PutPersonImpl(T person, MultiTaskCompleteWatcher.Task task) {
            this.person = person;
            this.task = task;
        }

        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            if (task.isSuccessful()){
                // создание таблицы с пользовательскими данными
                String user_uid = auth.getCurrentUser().getUid();
                database
                        .child(getDatabaseTag(tClass))
                        .child(user_uid)
                        .setValue(person.toMap())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) databaseTask.complete();
                                else saltTask.fail(task.getException());
                            }
                        });

                // добавление соли в таблицу соответствия соли и почты (для аутентификации)
                database.child(EMAIL_TO_SALT_TAG)
                        .child(utils.emailForDatabase(person.email))
                        .setValue(utils.bytesToArray(person.salt))
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) saltTask.complete();
                                else saltTask.fail(task.getException());
                            }
                        });

                // загрузка авватарки на сервер
                if (person.getAvatarUri() != null) {
                    Uri ava_file = Uri.fromFile(new File(person.getAvatarUri().getPath()));
                    StorageReference ava_ref = storage.child(getDatabaseAvaFolder(tClass))
                            .child(ava_file.getLastPathSegment());
                    UploadTask uploadTask = ava_ref.putFile(ava_file);

                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) storageTask.complete();
                            else storageTask.fail(task.getException());
                        }
                    });
                }
            } else {
                storageTask.fail(task.getException());
            }
        }
    }

    private OnCompleteListener<AuthResult> onAuthorised = new OnCompleteListener<AuthResult>() {

        private OnCompleteListener<byte []> avatarListener = new OnCompleteListener<byte[]>() {
            @Override
            public void onComplete(@NonNull Task<byte[]> task) {
                if (task.isSuccessful()) {
                    utils.saveBytesToFile(localPerson.getAvatarUri(), task.getResult());
                    localService.savePerson(localPerson, null); // TODO ? task callback
                    data.postValue(localPerson);
                } else {
                    // TODO
                }
            }
        };

        private ValueEventListener personEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GenericTypeIndicator<Map<String, Object> > typeIndicator =
                        new GenericTypeIndicator<Map<String, Object> >() {};
                Map<String, Object> map = dataSnapshot.getValue(typeIndicator);
                boolean addAvaListener =
                        dataSnapshot.hasChild("avatarUri") &&
                        localPerson != null &&
                        localPerson.avatarUri == null;
                try {
                    localPerson = tClass.newInstance();
                    localPerson.fromMap(map);
                } catch (Exception e) {
                    // TODO
                }
                if (localPerson != null && localPerson.getAvatarUri() != null && addAvaListener){
                    dataSnapshot.child("avatarUri").getRef()
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    storage.child(getDatabaseAvaFolder(tClass))
                                            .child(localPerson.getAvatarUri().getLastPathSegment())
                                            .getBytes(utils.MAX_DOWNLOAD_BYTES)
                                            .addOnCompleteListener(avatarListener);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // TODO
                                }
                            });
                } else {
                    data.postValue(localPerson);
                }
                localService.savePerson(localPerson, null); // TODO ? task callback
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // TODO
            }
        };

        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            if (task.isSuccessful()) {
                String user_uid = auth.getCurrentUser().getUid();
                database
                        .child(getDatabaseTag(tClass))
                        .child(user_uid)
                        .addValueEventListener(personEventListener);
            }
        }
    };

    private String getDatabaseAvaFolder(Class c) {
        if (c.equals(User.class)) return User.DATABASE_AVA_FOLDER;
        if (c.equals(Specialist.class)) return Specialist.DATABASE_AVA_FOLDER;
        return null;
    }

    private String getDatabaseTag(Class c) {
        if (c.equals(User.class)) return User.DATABASE_TAG;
        if (c.equals(Specialist.class)) return Specialist.DATABASE_TAG;
        return null;
    }
}
