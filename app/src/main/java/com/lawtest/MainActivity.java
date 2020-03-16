package com.lawtest;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.lawtest.ui.login.LogInActivity;

public class MainActivity extends AppCompatActivity {

    private static MainActivity instance;
    private MainViewModel viewModel;

    public static MainActivity getInstance() {
        return instance;
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
