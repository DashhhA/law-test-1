package com.lawtest.ui.user.home;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.lawtest.MainActivity;
import com.lawtest.R;
import com.lawtest.model.StateListener;
import com.lawtest.model.User;
import com.lawtest.ui.base.CropActivity;
import com.lawtest.ui.base.EditNameDialog;

import static android.app.Activity.RESULT_OK;

// "личный кабинет" пользователя
public class HomeFragment extends Fragment {
    public static final int PICK_IMAGE = 1;
    public static final int CROP_IMAGE = 2;
    private ImageView avaView;
    private User user;
    // обработчик нажатия на кнопку редактирования, стартующий активити для выбора изобрадения
    // из галереи
    private View.OnClickListener select_img_lstnr = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
            getIntent.setType("image/*");

            Intent pickIntent = new Intent(Intent.ACTION_PICK);
            pickIntent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

            Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

            startActivityForResult(pickIntent, PICK_IMAGE);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_user_home, container, false);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // получение ссылок на элементы интерфейса
        avaView = view.findViewById(R.id.userAvatarEditView);
        final TextView nameText = view.findViewById(R.id.userName);
        final TextView emailText = view.findViewById(R.id.userEmail);
        final ImageButton redactButton = view.findViewById(R.id.userHomeRedact);
        final ImageButton erase = view.findViewById(R.id.userHomeErase);
        final ImageButton redactName = view.findViewById(R.id.userEditName);
        redactButton.setOnClickListener(select_img_lstnr);
        erase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // замена аватарки на дефолтную и сохранение изменений
                user.setAvatarUri(null);
                MainActivity.getInstance().getViewModel().getUserRepository().savePerson(null);
            }
        });
        redactName .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // диалог с выбором новых ФИО
                new EditNameDialog(getContext()).setOnValuesSetListener(
                        new EditNameDialog.OnValuesSetListener() {
                    @Override
                    public void onValuesSet(final String fName, final String sName, final String surName) {
                        MainActivity.getInstance().getViewModel().getUser().observe(
                                getViewLifecycleOwner(), new Observer<User>() {
                            @Override
                            public void onChanged(User user) {
                                user.fName = fName;
                                user.sName = sName;
                                user.surName = surName;
                                MainActivity.getInstance().getViewModel().getUserRepository().savePerson(null);
                            }
                        });
                    }
                });
            }
        });

        // получение данных о текущем пользователе и обновление элементов интерфейса в соответствии
        // с полученной информацией
        LiveData<User> user = MainActivity.getInstance().getViewModel().getUser();
        user.observe(this.getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                HomeFragment.this.user = user;

                String name;
                if (user.sName != null) {
                    name = String.format("%s %s %s",
                            user.fName, user.sName, user.surName);
                } else {
                    name = String.format("%s %s",
                            user.fName, user.surName);
                }
                nameText.setText(name);
                emailText.setText(user.email);
                if ( user.getAvatarUri() != null ) avaView.setImageURI(user.getAvatarUri());
                else avaView.setImageResource(R.drawable.ic_user_default); // default image
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // если результат получен из активити, выбирающего изображение из галереи, вызывается
        // активити для обрезки изображений
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imgUri = data.getData();

            Intent intent = new Intent(this.getActivity(), CropActivity.class);
            intent.setData(imgUri);
            startActivityForResult(intent, CROP_IMAGE);
        }

        // обновляем изображение "аватарки" по выполнении активити "обрезающего" изображение
        if (requestCode == CROP_IMAGE && resultCode == RESULT_OK){
            Uri avatarUri = data.getData();
            user.setAvatarUri(avatarUri);
            final ProgressDialog progress = new ProgressDialog(this.getActivity());
            progress.setMessage(getString(R.string.specialist_fetching));
            progress.show();
            MainActivity.getInstance().getViewModel().getUserRepository().savePerson(new StateListener() {
                @Override
                public void onStartProcessing() {

                }

                @Override
                public void onCompleteLocal() {

                }

                @Override
                public void onCompleteWeb() {

                }

                @Override
                public void onComplete() {
                    progress.dismiss();
                }

                @Override
                public void onFailure(Exception exception) {

                }
            });
        }
    }
}
