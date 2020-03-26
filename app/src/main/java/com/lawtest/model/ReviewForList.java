package com.lawtest.model;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.lawtest.MainActivity;
import com.lawtest.util.utils;

import java.util.Map;

// класс, содержащий информацию, необходимую для визуализации отзыва
public class ReviewForList {
    public String fName;
    public String surName;
    public String body;
    private String avatarUri;
    private MutableLiveData<ReviewForList> data; // LiveData, в которую "публикуются" изменения отзыва
    private String key; // "ключ" под которым находится объект

    // загрузка и сохранение аватарки
    private OnCompleteListener<byte []> avatarListener = new OnCompleteListener<byte[]>() {
        @Override
        public void onComplete(@NonNull Task<byte[]> task) {
            if (task.isSuccessful()) {
                utils.saveBytesToFile(getAvatarUri(), task.getResult());
            }
            data.postValue(ReviewForList.this);
        }
    };

    public ReviewForList(Review review, String key) {
        data = new MutableLiveData<>();
        body = review.body;
        this.key = key;

        MainActivity.getInstance().getViewModel().getDatabase()
                .child(User.DATABASE_TAG)
                .child(review.userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // получение пользователя, оставившего отзыв
                        GenericTypeIndicator<Map<String, Object>> typeIndicator =
                                new GenericTypeIndicator<Map<String, Object>>() {};
                        Map<String, Object> map = dataSnapshot.getValue(typeIndicator);

                        fName = (String) map.get("fName");
                        surName = (String) map.get("surName");
                        avatarUri = (String) map.get("avatarUri");
                        if ( avatarUri != null ) {
                            MainActivity.getInstance().getViewModel().getStorage()
                                    .child(User.DATABASE_AVA_FOLDER)
                                    .child(Uri.parse(avatarUri).getLastPathSegment())
                                    .getBytes(utils.MAX_DOWNLOAD_BYTES)
                                    .addOnCompleteListener(avatarListener);
                        } else {
                            data.postValue(ReviewForList.this);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        //todo
                    }
                });
    }

    public String getKey() {
        return key;
    }

    public Uri getAvatarUri() {
        if (avatarUri != null) return Uri.parse(avatarUri);
        return null;
    }

    public LiveData<ReviewForList> getReview() {
        return data;
    }
}
