package com.lawtest.model;

interface UserLocalService {
    User getUser(String email, String pass);
    void saveUser(User user);
}
