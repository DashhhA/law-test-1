package com.lawtest.model;

import android.content.Context;
import android.content.ContextWrapper;

import androidx.lifecycle.MutableLiveData;

import com.lawtest.MainActivity;
import com.lawtest.util.crypto;
import com.lawtest.util.utils;

import java.io.File;
import java.util.regex.Pattern;

public class UserRepository {

    private MutableLiveData<User> data;
    private UserLocalService localService = new UserLocalService() {
        @Override
        public User getUser(String email, String pass) {
            return checkUser(email, pass);
        }

        @Override
        public void saveUser(User user) {
            saveUserAsync(user);
        }

        private User checkUser(String email, String pass){
            User user = utils.getUserFromPrefs(User.TAG);
            if (user != null && user.email.equals(email) && crypto.checkPass(pass, user.passSalt))
                return user;

            return null;
        }

        private void saveUserAsync(final User user) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // сохранение объекта
                    utils.saveToPrefs(user, User.TAG);

                    // удаление аватарки предидущего пользователя
                    ContextWrapper contextWrapper = new ContextWrapper(MainActivity.getInstance());
                    File directory = contextWrapper.getDir(utils.IMG_DIR, Context.MODE_PRIVATE);
                    File[] files = directory.listFiles();
                    if (user.getAvatarUri() != null) {
                        File avatar = new File(user.getAvatarUri().getPath());
                        for (File f : files) {
                            if ( Pattern.matches("^.+\\"+utils.AVATAR_FORMAT+"$", f.getName())
                                    && !f.equals(avatar) ) {
                                f.delete();
                            }
                        }
                    } else {
                        for (File f : files) {
                            if (Pattern.matches("^.+\\"+utils.AVATAR_FORMAT+"$", f.getName()))
                                f.delete();
                        }
                    }
                }
            }).start();
        }
    };

    public MutableLiveData<User> getUser(String email, String pass){
        if (data == null) {
            data = new MutableLiveData<>();
            receiveData(email, pass);
        }
        return data;
    }

    public void saveUser(User user){
        // TODO: push on server
        localService.saveUser(user);
    }

    private void receiveData(final String email, final String pass) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // проверка в локальном репозитории
                User user = localService.getUser(email, pass);
                if (user != null) data.postValue(user);
            }
        }).start();
    }
}
