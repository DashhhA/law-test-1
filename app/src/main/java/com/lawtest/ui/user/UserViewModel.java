package com.lawtest.ui.user;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lawtest.MainActivity;
import com.lawtest.model.User;
import com.lawtest.model.UserRepository;

import java.util.Locale;

public class UserViewModel extends ViewModel {

    private MutableLiveData<String> userName;
    private MutableLiveData<String> userEmail;
    private MutableLiveData<Uri> avaUri;

    public UserViewModel() {
        userName = new MutableLiveData<>();
        userEmail = new MutableLiveData<>();
        avaUri = new MutableLiveData<>();

        UserRepository repository = MainActivity.getInstance().getUserRepository();
        repository.addOnChangeListener(new UserRepository.OnChangeListener() {
            @Override
            public void onChange(User user) {
                String nameSurName = String.format(
                        Locale.getDefault(),
                        "%s %s",
                        user.fName, user.surName
                );
                userName.postValue(nameSurName);
                userEmail.postValue(user.email);
                avaUri.postValue(user.getAvatarUri());
            }
        });

        initialPost();
    }

    LiveData<String> getUserName() { return userName; }
    LiveData<String> getUserEmail() { return userEmail; }
    LiveData<Uri> getAvaUri() { return avaUri; }

    private void initialPost() {
        User user = MainActivity.getInstance().getUserRepository().getLocalUser();
        String nameSurName = String.format(
                Locale.getDefault(),
                "%s %s",
                user.fName, user.surName
        );
        userName.postValue(nameSurName);
        userEmail.postValue(user.email);
        avaUri.postValue(user.getAvatarUri());
    }
}
