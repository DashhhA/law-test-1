package com.lawtest.util;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;

import com.google.gson.Gson;
import com.lawtest.MainActivity;
import com.lawtest.model.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class utils {
    public static String IMG_DIR = "img_dir";
    public static String AVATAR_FORMAT = ".vtr";

    // сохранение bitmap в локальную директорию приложения
    public static Uri saveToInternalStorage(Context context, Bitmap bitmapImage, String name){
        ContextWrapper contextWrapper = new ContextWrapper(context);
        File directory = contextWrapper.getDir(IMG_DIR, Context.MODE_PRIVATE);
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

    // сохранение объекта в shared preferences с помощью gson
    public static <T> void saveToPrefs( T object, String TAG){
        SharedPreferences preferences = MainActivity.getInstance().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        editor.putString(TAG, gson.toJson(object));
        editor.apply();
    }

    // получение объекта из shared preferences с помощью gson
    public static User getUserFromPrefs(String TAG){
        SharedPreferences preferences = MainActivity.getInstance().getPreferences(Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString(TAG, null);
        if (json != null) return gson.fromJson(json, User.class);
        return null;
    }

    // сохранение объекта в локальную директорию приложения
    public static void saveObject(Object object, String fileName){
        MainActivity context = MainActivity.getInstance();
        FileOutputStream fos = null;
        ObjectOutputStream os = null;
        try {
            fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            os = new ObjectOutputStream(fos);
            os.writeObject(object);
        }catch (IOException e){
            //TODO: handle exception
        } finally {
            try {
                os.close();
                fos.close();
            } catch (Exception e) {
                //TODO: handle exception
            }
        }
    }

    // загрузка объекта из локальной директории приложения
    public static <T> T loadObject(String fileName){
        MainActivity context = MainActivity.getInstance();
        FileOutputStream fos = null;
        ObjectOutputStream os = null;
        T object = null;
        try {
            FileInputStream fis = context.openFileInput(fileName);
            ObjectInputStream is = new ObjectInputStream(fis);
            object = (T) is.readObject();
        }catch (IOException e){
            //TODO: handle exception
            e.getMessage();
        } catch (ClassNotFoundException e){

        } finally {
            try {
                os.close();
                fos.close();
            } catch (Exception e) {
                //TODO: handle exception
            }
        }
        return object;
    }
}
