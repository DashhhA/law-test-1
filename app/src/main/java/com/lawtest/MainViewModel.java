package com.lawtest;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.lawtest.model.PersonRepository;
import com.lawtest.model.Specialist;

public class MainViewModel extends ViewModel {

    private FirebaseAuth auth;
    private DatabaseReference database;
    private StorageReference storage;
    private PersonRepository<Specialist> specialistRepository;
    private LiveData<Specialist> specialist;

    public MainViewModel() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance().getReference();

        specialistRepository = new PersonRepository<>(auth, database, storage, Specialist.class);
    }

    public void authSpecialist(String email, String password) {
        specialist = specialistRepository.getPerson(email, password);
    }

    public LiveData<Specialist> getSpecialist() {
        return specialist;
    }

    public PersonRepository<Specialist> getSpecialistRepository() {
        return specialistRepository;
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
}
