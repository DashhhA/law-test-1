package com.lawtest.model;

import androidx.lifecycle.LiveData;

interface UserWebService {
    LiveData<User> getUser(String email, String pass);
    void saveUser(User user);
}
