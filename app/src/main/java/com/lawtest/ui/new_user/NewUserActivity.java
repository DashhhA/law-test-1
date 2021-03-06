package com.lawtest.ui.new_user;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.lawtest.MainActivity;
import com.lawtest.R;
import com.lawtest.model.StateListener;
import com.lawtest.model.User;
import com.lawtest.ui.base.CropActivity;
import com.lawtest.ui.login.LogInActivity;
import com.lawtest.ui.user.UserActivity;
import com.lawtest.util.crypto;

// активити создания нового пользователя
public class NewUserActivity extends AppCompatActivity {

    public static final int PICK_IMAGE = 1;
    public static final int CROP_IMAGE = 2;
    private ImageView userAva;
    private NewUserViewModel viewModel;
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

    private TextWatcher sNameTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            viewModel.setsName(s.toString());
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        // устанавливается обработчик нажатия для "аватарки"
        userAva = findViewById(R.id.usr_default_icon);
        userAva.setOnClickListener(select_img_lstnr);

        // получение ссылок на элементы пользовательского интерфейса
        Button submit = findViewById(R.id.createButton);
        final EditText fName = findViewById(R.id.newUsrFstName);
        final EditText sName = findViewById(R.id.newUsrSndName);
        final EditText surName = findViewById(R.id.newUsrSurname);
        final EditText email = findViewById(R.id.newUsrEmail);
        final EditText password = findViewById(R.id.newUsrPassword);
        final CheckBox autologin = findViewById(R.id.autologin);
        sName.addTextChangedListener(sNameTextWatcher);

        // получение ViewModel и заполнение полей пользовательского интерфейса в соответствии
        // с сохраненными данными.
        viewModel = ViewModelProviders.of(this).get(NewUserViewModel.class);
        String s = viewModel.getfName();
        if (s != null) fName.setText(s);
        s = viewModel.getsName();
        if (s != null) sName.setText(s);
        s = viewModel.getSurName();
        if (s != null) surName.setText(s);
        s = viewModel.getEmail();
        if (s != null) email.setText(s);
        autologin.setChecked(viewModel.isRemember());
        autologin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                viewModel.setRemember(isChecked);
            }
        });
        Uri ava_uri = viewModel.getAvatarUri();
        if (ava_uri != null) userAva.setImageURI(ava_uri);

        // set model to control if an input is valid
        new ContentModel(fName, surName, email, password, submit);

        // обработка нажатия на кнопку создания аккаунта
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = new User(
                        viewModel.getfName(),
                        viewModel.getsName(),
                        viewModel.getSurName(),
                        viewModel.getEmail(),
                        crypto.getPassSalt(password.getText().toString()),
                        viewModel.getImgUri(),
                        viewModel.getAvatarUri(),
                        viewModel.isRemember()
                );

                // уведомление о загрузке и коллбак на создание аккаунта
                final ProgressDialog progress = new ProgressDialog(NewUserActivity.this);
                progress.setMessage(getString(R.string.new_user_creating));
                progress.show();
                MainActivity.getInstance().getViewModel().getUserRepository()
                        .newPerson(user, new StateListener() {
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
                                // запуск соответствующего активити и авторизация
                                // в основном ViewModel
                                progress.dismiss();
                                MainActivity.getInstance().getViewModel()
                                        .authUser(viewModel.getEmail(), password.getText().toString());
                                Intent intent = new Intent(NewUserActivity.this, UserActivity.class);
                                startActivity(intent);
                            }

                            @Override
                            public void onFailure(Exception exception) {
                                // сообщение об ошибке
                                progress.dismiss();
                                AlertDialog.Builder builder = new AlertDialog.Builder(NewUserActivity.this);
                                builder.setTitle("Error");
                                builder.setMessage(exception.getMessage());
                                builder.setPositiveButton("Ok", null);
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        });
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
            viewModel.setImgUri(imgUri);

            Intent intent = new Intent(this, CropActivity.class);
            intent.setData(imgUri);
            startActivityForResult(intent, CROP_IMAGE);
        }

        // обновляем изображение "аватарки" по выполнении активити "обрезающего" изображение
        if (requestCode == CROP_IMAGE && resultCode == RESULT_OK){
            Uri avatarUri = data.getData();
            viewModel.setAvatarUri(avatarUri);
            userAva.setImageURI(null);
            userAva.setImageURI(avatarUri);
        }
    }

    // класс, следящий за корректностью введенных пользователем данных
    private class ContentModel{

        private boolean enable = false;
        private boolean fNameValid = false;
        private boolean surNameValid = false;
        private boolean emailValid = false;
        private boolean passwordValid = false;
        private int emailError = R.string.new_user_email_error;
        private TextWatcher fNameWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                fNameValid = isNameValid(s.toString());
                updateEnable();
                updateErrors();
                viewModel.setfName(s.toString());
            }
        };
        private TextWatcher surNameWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                surNameValid = isNameValid(s.toString());
                updateEnable();
                updateErrors();
                viewModel.setSurName(s.toString());
            }
        };
        private TextWatcher emailWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                emailValid = isEmailValid(s.toString());
                updateEnable();
                updateErrors();
                viewModel.setEmail(s.toString());
            }
        };
        private TextWatcher passwordWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                passwordValid = isPasswordValid(s.toString());
                updateEnable();
                updateErrors();
            }
        };
        private EditText fName;
        private EditText surName;
        private EditText email;
        private EditText password;
        private Button button;

        private void updateEnable(){
            enable = fNameValid && emailValid && passwordValid;
            button.setEnabled(enable);
        }

        private void updateErrors(){
            if (!fNameValid){
                fName.setError(getString(R.string.new_user_name_error));
            }
            if (!surNameValid) {
                surName.setError(getString(R.string.new_user_name_error));
            }
            if (!emailValid){
                email.setError(getString(emailError));
            }
            if (!passwordValid){
                password.setError(getString(R.string.new_user_password_error));
            }
        }

        ContentModel(EditText fName, EditText surName, EditText email, EditText password, Button button){
            this.fName = fName;
            this.fName.addTextChangedListener(fNameWatcher);
            this.surName = surName;
            this.surName.addTextChangedListener(surNameWatcher);
            this.email = email;
            this.email.addTextChangedListener(emailWatcher);
            this.password = password;
            this.password.addTextChangedListener(passwordWatcher);
            this.button = button;
            updateErrors();
            updateEnable();
        }

        boolean isPasswordValid(String password) {
            return password != null && password.trim().length() > 5;
        }

        private boolean isEmailValid(String email){
            if (email == null || email.equals("")) {
                emailError = R.string.new_user_empty_error;
                return false;
            }else {
                emailError = R.string.new_user_email_error;
                return Patterns.EMAIL_ADDRESS.matcher(email).matches();
            }
        }

        boolean isNameValid(String fName){
            return fName != null && !fName.trim().isEmpty();
        }
    }
}
