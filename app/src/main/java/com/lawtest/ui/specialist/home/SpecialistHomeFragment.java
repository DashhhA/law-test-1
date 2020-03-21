package com.lawtest.ui.specialist.home;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.lawtest.MainActivity;
import com.lawtest.R;
import com.lawtest.model.Specialist;
import com.lawtest.model.StateListener;
import com.lawtest.ui.base.CropActivity;
import com.lawtest.ui.base.EditNameDialog;
import com.lawtest.ui.login.LogInActivity;

import static android.app.Activity.RESULT_OK;

public class SpecialistHomeFragment extends Fragment {
    public static final int PICK_IMAGE = 1;
    public static final int CROP_IMAGE = 2;
    private SpecialistHomeViewModel viewModel;
    private ImageView avaView;
    private Specialist specialist;
    // обработчик нажатия на изображение, стартующий активити для выбора изобрадения из галереи
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        viewModel = ViewModelProviders.of(this).get(SpecialistHomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_specialist_home, container, false);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        avaView = view.findViewById(R.id.specAvatarEditView);
        final TextView nameText = view.findViewById(R.id.specName);
        final TextView emailText = view.findViewById(R.id.specEmail);
        TextView aboutText = view.findViewById(R.id.specAbout);
        final ImageButton redact = view.findViewById(R.id.specHomeRedact);
        final ImageButton erase = view.findViewById(R.id.specHomeErase);
        final ImageButton nameEdit = view.findViewById(R.id.specNameEdit);
        redact.setOnClickListener(select_img_lstnr);
        erase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                specialist.setAvatarUri(null);
                MainActivity.getInstance().getViewModel().getSpecialistRepository().savePerson(null);
            }
        });
        nameEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new EditNameDialog(getContext()).setOnValuesSetListener(
                        new EditNameDialog.OnValuesSetListener() {
                            @Override
                            public void onValuesSet(final String fName, final String sName, final String surName) {
                                MainActivity.getInstance().getViewModel().getSpecialist().observe(
                                        getViewLifecycleOwner(), new Observer<Specialist>() {
                                            @Override
                                            public void onChanged(Specialist specialist) {
                                                specialist.fName = fName;
                                                specialist.sName = sName;
                                                specialist.surName = surName;
                                                MainActivity.getInstance().getViewModel().getUserRepository().savePerson(null);
                                            }
                                        });
                            }
                        });
            }
        });

        viewModel.getSpecialist().observe(this.getViewLifecycleOwner(), new Observer<Specialist>() {
            @Override
            public void onChanged(Specialist specialist) {
                SpecialistHomeFragment.this.specialist = specialist;
                String name;
                if (specialist.sName != null) {
                    name = String.format("%s %s %s",
                            specialist.fName, specialist.sName, specialist.surName);
                } else {
                    name = String.format("%s %s",
                            specialist.fName, specialist.surName);
                }
                nameText.setText(name);
                emailText.setText(specialist.email);
                if ( specialist.getAvatarUri() != null ) avaView.setImageURI(specialist.getAvatarUri());
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
            specialist.setAvatarUri(avatarUri);
            final ProgressDialog progress = new ProgressDialog(this.getActivity());
            progress.setMessage(getString(R.string.specialist_fetching));
            progress.show();
            MainActivity.getInstance().getViewModel().getSpecialistRepository().savePerson(new StateListener() {
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
