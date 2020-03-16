package com.lawtest;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.lawtest.model.PersonRepository;
import com.lawtest.model.Specialist;
import com.lawtest.model.User;

public class MainViewModel extends ViewModel {

    private FirebaseAuth auth;
    private DatabaseReference database;
    private StorageReference storage;
    private PersonRepository<Specialist> specialistRepository;
    private LiveData<Specialist> specialist;
    private PersonRepository<User> userRepository;
    private LiveData<User> user;
    private User localUser;

    public MainViewModel() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance().getReference();
    }

    public void authSpecialist(String email, String password) {
        if (specialistRepository == null)
            specialistRepository = new PersonRepository<>(auth, database, storage, Specialist.class);
        specialist = specialistRepository.getPerson(email, password);
    }

    public void authUser(String email, String password) {
        if (userRepository == null)
            userRepository = new PersonRepository<>(auth, database, storage, User.class);
        user = userRepository.getPerson(email, password);
        user.observeForever(new Observer<User>() {
            @Override
            public void onChanged(User user) {
                localUser = user;
            }
        });
    }

    public LiveData<Specialist> getSpecialist() {
        return specialist;
    }

    public LiveData<User> getUser() {
        return user;
    }

    public PersonRepository<Specialist> getSpecialistRepository() {
        if (specialistRepository == null)
            specialistRepository = new PersonRepository<>(auth, database, storage, Specialist.class);
        return specialistRepository;
    }

    public PersonRepository<User> getUserRepository() {
        if (userRepository == null)
            userRepository = new PersonRepository<>(auth, database, storage, User.class);
        return userRepository;
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public DatabaseReference getDatabase() {
        return database;
    }

    public StorageReference getStorage() {
        return storage;
    }

    public User getUserOnce() {
        return localUser;
    }
}
