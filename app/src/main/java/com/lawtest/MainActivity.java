package com.lawtest;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.lawtest.model.PersonRepository;
import com.lawtest.model.Specialist;
import com.lawtest.model.UserRepository;
import com.lawtest.ui.login.LogInActivity;

public class MainActivity extends AppCompatActivity {

    private static MainActivity instance;
    private UserRepository userRepository;
    private MainViewModel viewModel;

    public static MainActivity getInstance() {
        return instance;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public MainViewModel getViewModel() {
        return viewModel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;

        //userRepository = new UserRepository(auth, database, storage);
        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        // запускает активити входа
        Intent intent = new Intent(this, LogInActivity.class);
        startActivity(intent);
    }
}
