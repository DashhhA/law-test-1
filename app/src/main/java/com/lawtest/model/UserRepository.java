package com.lawtest.model;

import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.lawtest.MainActivity;
import com.lawtest.util.crypto;
import com.lawtest.util.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;

public class UserRepository {

    private interface UserLocalService {
        User getUser(String email, String pass);
        void saveUser(User user, TaskState taskState);
    }

    interface UserWebService {
        void getUser(String email, String pass, OnChangeListener listener);
        void saveUser(User user, TaskState taskState);
        void newUser(User user, TaskState taskState);
    }

    public interface OnChangeListener {
        void onChange(User user);
    }

    private MutableLiveData<User> data;
    private User localUser;
    private ArrayList<OnChangeListener> changeListeners;

    private FirebaseAuth auth;
    private DatabaseReference database;
    private StorageReference storage;

    private final static String EMAIL_TO_SALT_TAG = "emailToSalt";

    private UserLocalService localService = new UserLocalService() {
        @Override
        public User getUser(String email, String pass) {
            localUser = checkUser(email, pass);
            return localUser;
        }

        @Override
        public void saveUser(User user, TaskState taskState) {
            saveUserAsync(user, taskState);
        }

        private User checkUser(String email, String pass){
            User user = utils.getUserFromPrefs(User.TAG);
            if (
                    user != null &&
                    user.email.equals(email) &&
                    crypto.checkPass(pass, user.pass, user.salt)
            ) {
                return user;
            }

            return null;
        }

        private void saveUserAsync(final User user, TaskState taskState) {
            // TODO apply state listener
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // сохранение объекта
                    utils.saveToPrefs(user, User.TAG);

                    // удаление аватарки предидущего пользователя
                    ContextWrapper contextWrapper = new ContextWrapper(MainActivity.getInstance());
                    File directory = contextWrapper.getDir(utils.IMG_DIR, Context.MODE_PRIVATE);
                    File[] files = directory.listFiles();
                    if (user.getAvatarUri() != null) {
                        File avatar = new File(user.getAvatarUri().getPath());
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

                    // достаточно добавить здесь, так как репозиторрии синхронизируются
                    notifyUserChanged(user);
                }
            }).start();
        }
    };
    private UserWebService webService = new UserWebService() {
        byte[] salt;
        @Override
        public void getUser(final String email, final String pass, final OnChangeListener listener) {
            ValueEventListener saltEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    GenericTypeIndicator<ArrayList<Integer>> typeIndicator = new GenericTypeIndicator<ArrayList<Integer>>() {};
                    ArrayList<Integer> list = dataSnapshot.getValue(typeIndicator);
                    salt = utils.arrayToBytes(list);

                    auth.signInWithEmailAndPassword(email, crypto.getPassBySalt(pass, salt))
                            .addOnCompleteListener( new GetSignedInUserImpl(listener));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    listener.onChange(null);
                }
            };
            database
                    .child(EMAIL_TO_SALT_TAG)
                    .child(utils.emailForDatabase(email))
                    .addListenerForSingleValueEvent(saltEventListener);
        }

        @Override
        public void saveUser(final User user, final TaskState taskState) {
            String pass = new String(user.pass);
            auth.signInWithEmailAndPassword(user.email, pass)
                    .addOnCompleteListener(
                            new PutUserOnLogInImpl(user, taskState)
                    );
        }

        @Override
        public void newUser(final User user, TaskState taskState) {
            String pass = new String(user.pass);
            auth.createUserWithEmailAndPassword(user.email, pass)
                .addOnCompleteListener(
                        new PutUserOnLogInImpl(user, taskState)
                );
        }

        class GetSignedInUserImpl implements OnCompleteListener<AuthResult>{
            private OnChangeListener listener;
            private User user;

            GetSignedInUserImpl(final OnChangeListener listener){
                this.listener = listener;
            }

            private OnCompleteListener<byte []> avatarListener = new OnCompleteListener<byte[]>() {
                @Override
                public void onComplete(@NonNull Task<byte[]> task) {
                    if (task.isSuccessful()) {
                        utils.saveBytesToFile(user.getAvatarUri(), task.getResult());
                        listener.onChange(user);
                    } else {
                        listener.onChange(null);
                    }
                }
            };

            private ValueEventListener userEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    GenericTypeIndicator<Map<String, Object> > typeIndicator =
                            new GenericTypeIndicator<Map<String, Object> >() {};
                    Map<String, Object> map = dataSnapshot.getValue(typeIndicator);
                    user = new User(map);
                    if (user != null && user.getAvatarUri() != null) {
                        storage.child(User.DATABASE_AVA_FOLDER)
                                .child(user.getAvatarUri().getLastPathSegment())
                                .getBytes(utils.MAX_DOWNLOAD_BYTES)
                                .addOnCompleteListener(avatarListener);
                    } else {
                        listener.onChange(user);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    listener.onChange(null);
                }
            };

            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    String user_uid = auth.getCurrentUser().getUid();
                    database
                            .child(User.DATABASE_TAG)
                            .child(user_uid)
                            .addListenerForSingleValueEvent(userEventListener);

                } else {
                    listener.onChange(null);
                }
            }
        }
    };

    private class PutUserOnLogInImpl implements OnCompleteListener<AuthResult> {
        private User user;
        private TaskState taskState;
        private MultiTaskCompleteWatcher taskCompleteWatcher = new MultiTaskCompleteWatcher() {
            @Override
            void allComplete() {
                taskState.onCompleteWeb();
            }
        };
        private final MultiTaskCompleteWatcher.Task databaseTask = taskCompleteWatcher.newTask();
        private final MultiTaskCompleteWatcher.Task storageTask = taskCompleteWatcher.newTask();
        private final MultiTaskCompleteWatcher.Task saltTask = taskCompleteWatcher.newTask();

        PutUserOnLogInImpl(User user, TaskState taskState) {
            this.user = user;
            this.taskState = taskState;
        }

        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            if (task.isSuccessful()){
                // создание таблицы с пользовательскими данными
                String user_uid = auth.getCurrentUser().getUid();
                database
                        .child(User.DATABASE_TAG)
                        .child(user_uid)
                        .setValue(user.toMap())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) databaseTask.complete();
                                else taskState.onFailure(task.getException());
                            }
                        });

                // добавление соли в таблицу соответствия соли и почты (для аутентификации)
                database.child(EMAIL_TO_SALT_TAG)
                        .child(utils.emailForDatabase(user.email))
                        .setValue(utils.bytesToArray(user.salt))
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) saltTask.complete();
                                else taskState.onFailure(task.getException());
                            }
                        });

                // загрузка авватарки на сервер
                if (user.getAvatarUri() != null) {
                    Uri ava_file = Uri.fromFile(new File(user.getAvatarUri().getPath()));
                    StorageReference ava_ref = storage.child(User.DATABASE_AVA_FOLDER)
                            .child(ava_file.getLastPathSegment());
                    UploadTask uploadTask = ava_ref.putFile(ava_file);

                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) storageTask.complete();
                            else taskState.onFailure(task.getException());
                        }
                    });
                }
            } else {
                taskState.onFailure(task.getException());
            }
        }
    }

    private class TaskState{
        private boolean localComplete;
        private boolean webComplete;
        private boolean failed;
        private StateListener stateListener;

        TaskState(@Nullable StateListener stateListener){
            this.stateListener = stateListener;
        }

        void onCompleteLocal(){
            localComplete = true;
            if (!failed && stateListener != null) stateListener.onCompleteLocal();
            if (!failed && webComplete) onComplete();
        }
        void onCompleteWeb(){
            webComplete = true;
            if (!failed && stateListener != null) stateListener.onCompleteWeb();
            if (!failed && localComplete) onComplete();
        }
        private void onComplete(){
            if (!failed && stateListener != null) stateListener.onComplete();
        }
        void onFailure(Exception exception){
            failed = true;
            if (stateListener != null) stateListener.onFailure(exception);
        }
    }

    private abstract class MultiTaskCompleteWatcher{
        private ArrayList<Task> tasks;
        MultiTaskCompleteWatcher() {
            tasks = new ArrayList<>();
        }
        Task newTask(){
            Task task = new Task();
            tasks.add(task);
            return task;
        }
        abstract void allComplete();

        class Task {
            private boolean complete;
            void complete() {
                complete = true;
                boolean allComplete = true;
                for (Task task: tasks) allComplete = allComplete && task.isComplete();
                if (allComplete) allComplete();
            }
            boolean isComplete() { return complete; }
        }
    }

    public UserRepository(){
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance().getReference();
        changeListeners = new ArrayList<>();
    }

    public MutableLiveData<User> getUser(String email, String pass){
        if (data == null) {
            data = new MutableLiveData<>();
            receiveData(email, pass);
        }
        return data;
    }

    public void newUser(User user, @Nullable StateListener stateListener){
        TaskState state = new TaskState(stateListener);
        webService.newUser(user, state);
        localService.saveUser(user, state);
    }

    public void saveUser(User user, @Nullable StateListener stateListener){
        TaskState state = new TaskState(stateListener);
        webService.saveUser(user, state);
        localService.saveUser(user, state);
    }

    public void addOnChangeListener(OnChangeListener listener){
        changeListeners.add(listener);
    }

    // синхронизация пользователей в локальной и удаленной бд
    public void fetchUser(@Nullable StateListener stateListener){
        TaskState state = new TaskState(stateListener);
        // скорее всего не нужно, так как синхронизация происходит по коллбакам от сервера
    }

    // синхронизация с удаленной бд
    public void addChangeListenersToRemoteElements(final User user) {
        OnCompleteListener<AuthResult> onAuthorised = new OnCompleteListener<AuthResult>() {

            private OnCompleteListener<byte []> avatarListener = new OnCompleteListener<byte[]>() {
                @Override
                public void onComplete(@NonNull Task<byte[]> task) {
                    if (task.isSuccessful()) {
                        utils.saveBytesToFile(user.getAvatarUri(), task.getResult());
                        localService.saveUser(user, null); // TODO ? task callback
                    } else {
                        // TODO
                    }
                }
            };

            private ValueEventListener userEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    GenericTypeIndicator<Map<String, Object> > typeIndicator =
                            new GenericTypeIndicator<Map<String, Object> >() {};
                    Map<String, Object> map = dataSnapshot.getValue(typeIndicator);
                    localUser = new User(map);
                    localService.saveUser(localUser, null); // TODO ? task callback
                    User user_tmp = new User(map);
                    if (user_tmp.getAvatarUri() != user.getAvatarUri()) {
                        storage.child(User.DATABASE_AVA_FOLDER)
                                .getBytes(utils.MAX_DOWNLOAD_BYTES)
                                .addOnCompleteListener(avatarListener);
                    } else {
                        saveUser(user_tmp, null); // TODO ? task callback
                    }
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
                            .child(User.DATABASE_TAG)
                            .child(user_uid)
                            .addValueEventListener(userEventListener);
                }
            }
        };
        String pass = new String(user.pass);
        auth.signInWithEmailAndPassword(user.email, pass)
                .addOnCompleteListener(onAuthorised);
    }

    public User getLocalUser() { return localUser; }

    private void receiveData(final String email, final String pass) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // проверка в локальном репозитории
                localUser = localService.getUser(email, pass);
                if (localUser != null) {
                    data.postValue(localUser);
                    fetchUser(null);
                } else {
                    webService.getUser(email, pass, new OnChangeListener() {
                        @Override
                        public void onChange(User user) {
                            localUser = user;
                            data.postValue(user);
                            if (user != null) localService.saveUser(user, null); // TODO ? task callback
                        }
                    });
                }

            }
        }).start();
    }

    // TODO: apply where needed
    private void notifyUserChanged(User user) {
        for (OnChangeListener listener : changeListeners) listener.onChange(user);
    }
}
