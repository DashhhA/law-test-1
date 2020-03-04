package com.lawtest;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.lawtest.model.UserRepository;
import com.lawtest.ui.login.LogInActivity;

public class MainActivity extends AppCompatActivity {

    private static MainActivity instance;
    private UserRepository userRepository;

    public static MainActivity getInstance() {
        return instance;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;

        userRepository = new UserRepository();

        // запускает активити входа
        Intent intent = new Intent(this, LogInActivity.class);
        startActivity(intent);
    }
}
