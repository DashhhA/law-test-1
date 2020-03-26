package com.lawtest.ui.login;

import androidx.lifecycle.ViewModel;

// содержит и предоставляет доступ к вееденным пользователем данным
public class LogInViewModel extends ViewModel {
    private String email;

    String getEmail() {
        return email;
    }

    void setEmail(String email) {
        this.email = email;
    }

}
