package com.lawtest.util;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;

public class utils {

    // сохранение bitmap в локальную директорию приложения
    public static Uri saveToInternalStorage(Context context, Bitmap bitmapImage, String name){
        ContextWrapper contextWrapper = new ContextWrapper(context);
        File directory = contextWrapper.getDir("img_dir", Context.MODE_PRIVATE);
        File filePath=new File(directory, name);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath, false);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            //TODO: handle exception
        } finally {
            try {
                fos.flush();
                fos.close();
            } catch (Exception e) {

            }
        }

        return Uri.parse(filePath.toString());
    }
}
