package com.lawtest.ui.login;

import androidx.lifecycle.ViewModel;

class LogInViewModel extends ViewModel {
    private String email;

    String getEmail() {
        return email;
    }

    void setEmail(String email) {
        this.email = email;
    }

}
