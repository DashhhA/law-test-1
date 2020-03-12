package com.lawtest.ui.login;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.lawtest.MainActivity;
import com.lawtest.model.BasePerson;
import com.lawtest.model.Specialist;
import com.lawtest.model.User;
import com.lawtest.util.crypto;
import com.lawtest.util.utils;

import java.util.ArrayList;

import static com.lawtest.model.PersonRepository.EMAIL_TO_SALT_TAG;

public class AuthIdentifier {
    public static final int USER = 0;
    public static final int SPECIALIST = 1;
    public static final int ADMIN = 2;

    private FirebaseAuth auth;
    private DatabaseReference database;
    private String email;
    private String password;

    private class ChildExistsListener implements ValueEventListener{

        private String key;
        private int person;
        private MultipleAuthCheckListener listener;

        ChildExistsListener(String key, int person, MultipleAuthCheckListener listener) {
            this.key = key;
            this.person = person;
            this.listener = listener;
        }

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.hasChild(key)) listener.onKeyFound(person);
            else listener.onKeyNotFound(person);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            // TODO
        }
    }

    private class AuthListener implements OnCompleteListener<AuthResult> {
        private MultipleAuthCheckListener multipleAuthCheckListener;
        private OnAnswer answer;

        AuthListener(OnAnswer answer) {
            multipleAuthCheckListener = new MultipleAuthCheckListener(answer);
            this.answer = answer;
        }

        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            if ( task.isSuccessful() ) {
                // проверка в списке пользователей
                database.child(User.DATABASE_TAG)
                        .addListenerForSingleValueEvent(
                                new ChildExistsListener(
                                        auth.getUid(),
                                        USER,
                                        multipleAuthCheckListener
                                )
                        );
                // проверка в списке специалитов
                database.child(Specialist.DATABASE_TAG)
                        .addListenerForSingleValueEvent(
                                new ChildExistsListener(
                                        auth.getUid(),
                                        SPECIALIST,
                                        multipleAuthCheckListener
                                )
                        );
            } else {
                answer.onFailure(task.getException());
            }
        }
    }

    private class MultipleAuthCheckListener {
        private OnAnswer answer;
        private ArrayList<Integer> failedKeys;

        MultipleAuthCheckListener(OnAnswer answer) {
            this.answer = answer;
            failedKeys = new ArrayList<>();
        }

        void onKeyFound(int key) {
            answer.onSuccess(key);
        }

        void onKeyNotFound(int key) {
            failedKeys.add(key);
            if (hasAllKeys()) {
                answer.onSuccess(ADMIN);
            }
        }

        private boolean hasAllKeys() {
            return  failedKeys.contains(USER) &&
                    failedKeys.contains(SPECIALIST);
        }
    }

    public interface OnAnswer {
        void onSuccess(int type);
        void onFailure(Exception e);
    }

    AuthIdentifier(String email, String password) {
        auth = MainActivity.getInstance().getAuth();
        database = MainActivity.getInstance().getDatabase();

        this.email = email;
        this.password = password;
    }

    public void getLogInType(final OnAnswer answer) {

        // проверка локального пользователя
        User user = utils.getPersonFromPrefs(User.TAG, User.class);
        if (checkPerson(user, email, password)) {
            answer.onSuccess(USER);
            return;
        }
        // проверка локального специалиста
        Specialist specialist = utils.getPersonFromPrefs(Specialist.TAG, Specialist.class);
        if (checkPerson(specialist, email, password)) {
            answer.onSuccess(SPECIALIST);
            return;
        }
        // проверка в удаленном репозитории
        database
                .child(EMAIL_TO_SALT_TAG)
                .child(utils.emailForDatabase(email))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        GenericTypeIndicator<ArrayList<Integer>> typeIndicator = new GenericTypeIndicator<ArrayList<Integer>>() {};
                        ArrayList<Integer> list = dataSnapshot.getValue(typeIndicator);
                        if (list != null) {
                            final byte[] salt = utils.arrayToBytes(list);

                            auth.signInWithEmailAndPassword(email, crypto.getPassBySalt(password, salt))
                                    .addOnCompleteListener(new AuthListener(answer));
                        } else {
                            answer.onFailure(new Exception("Cannot receive salt from server \n" +
                                    "maybe the account have been deleted"));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        answer.onFailure(databaseError.toException());
                    }
                });
    }

    private <T extends BasePerson> boolean checkPerson(T person, String email, String password) {
        return  person != null &&
                person.email.equals(email) &&
                crypto.checkPass(password, person.pass, person.salt);
        }

    }