package com.lawtest;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.lawtest.model.UserRepository;
import com.lawtest.ui.login.LogInActivity;

public class MainActivity extends AppCompatActivity {

    private static MainActivity instance;
    private FirebaseAuth auth;
    private DatabaseReference database;
    private StorageReference storage;
    private UserRepository userRepository;

    public static MainActivity getInstance() {
        return instance;
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

    public UserRepository getUserRepository() {
        return userRepository;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance().getReference();

        userRepository = new UserRepository(auth, database, storage);

        // запускает активити входа
        Intent intent = new Intent(this, LogInActivity.class);
        startActivity(intent);
    }
}
