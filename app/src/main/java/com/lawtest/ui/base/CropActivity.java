package com.lawtest.ui.base;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.isseiaoki.simplecropview.CropImageView;
import com.isseiaoki.simplecropview.callback.CropCallback;
import com.isseiaoki.simplecropview.callback.LoadCallback;
import com.lawtest.R;
import com.lawtest.util.utils;

import java.util.UUID;

public class CropActivity extends AppCompatActivity {

    private SpinnerProgress progress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_crop);

        final CropImageView view = findViewById(R.id.cropImageView);
        final Uri selectedImage = getIntent().getData();
        ImageButton submit = findViewById(R.id.btn_submit_crop);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.show();
                view.crop(selectedImage).execute(new CropCallback() {
                    @Override
                    public void onSuccess(Bitmap cropped) {
                        Bitmap scaled = Bitmap.createScaledBitmap(cropped, 256, 256, true);
                        UUID imgId = UUID.randomUUID();
                        Uri saved = utils.saveToInternalStorage(
                                getApplicationContext(),
                                scaled,
                                imgId.toString() + utils.AVATAR_FORMAT);
                        Intent intent = new Intent();
                        intent.setData(saved);
                        setResult(RESULT_OK, intent);

                        progress.dismiss();
                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
            }
        });
        ImageButton cancel = findViewById(R.id.btn_cancel_crop);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        view.setCropMode(CropImageView.CropMode.CIRCLE);
        view.load(selectedImage)
                .useThumbnail(true)
                .execute(new LoadCallback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });

        progress = new SpinnerProgress(this);
    }
}