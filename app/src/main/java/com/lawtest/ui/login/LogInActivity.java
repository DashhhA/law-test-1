package com.lawtest.ui.login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.lawtest.MainActivity;
import com.lawtest.R;
import com.lawtest.ui.admin.AdminActivity;
import com.lawtest.ui.new_user.NewUserActivity;
import com.lawtest.ui.specialist.SpecialistActivity;
import com.lawtest.ui.user.UserActivity;

// активити входа в приложение
public class LogInActivity extends AppCompatActivity {

    private LogInViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // получение ссылок на элементы пользовательского интерфейса
        EditText email = findViewById(R.id.loginEmail);
        final EditText password = findViewById(R.id.loginPassword);
        Button submit = findViewById(R.id.loginSubmit);
        new ContentModel(email, password, submit);

        // получение ViewModel и заполнение полей пользовательского интерфейса в соответствии
        // с сохраненными данными. Пароль решил на всякий случай не сохранять в ViewModel.
        viewModel = ViewModelProviders.of(this).get(LogInViewModel.class);
        String email_s = viewModel.getEmail();
        if (email_s != null) email.setText(email_s);

        // кнопка, создающая новый аккаунт
        Button create = findViewById(R.id.loginCreate);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(LogInActivity.this);
                Intent intent = new Intent(MainActivity.getInstance(), NewUserActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // кнопка входа в аккаунт
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progress = new ProgressDialog(LogInActivity.this);
                progress.setMessage(getString(R.string.login_checking));
                progress.show();
                AuthIdentifier authIdentifier =
                        new AuthIdentifier(viewModel.getEmail(), password.getText().toString());
                authIdentifier.getLogInType(new AuthIdentifier.OnAnswer() {
                    @Override
                    public void onSuccess(int type) {
                        progress.dismiss();
                        Intent intent;
                        // запуск активити в зависимости от результатов авторизации
                        switch (type) {
                            case AuthIdentifier.USER:
                                MainActivity.getInstance().getViewModel()
                                        .authUser(viewModel.getEmail(), password.getText().toString());         // сответствующая авторизация в основной ViewModel
                                intent = new Intent(LogInActivity.this, UserActivity.class);
                                startActivity(intent);
                                break;
                            case AuthIdentifier.SPECIALIST:
                                MainActivity.getInstance().getViewModel()
                                        .authSpecialist(viewModel.getEmail(), password.getText().toString());   // сответствующая авторизация в основной ViewModel
                                intent = new Intent(LogInActivity.this, SpecialistActivity.class);
                                startActivity(intent);
                                break;
                            case AuthIdentifier.ADMIN:
                                intent = new Intent(LogInActivity.this, AdminActivity.class);
                                startActivity(intent);
                                break;
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        progress.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(LogInActivity.this);
                        builder.setTitle("Error");
                        builder.setMessage(e.getMessage());
                        builder.setPositiveButton("Ok", null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });
            }
        });
    }

    // класс, контролирующий введенные данные (длина пароля должна быть не меньше 5 символов и почта
    // соответствовать шаблону).
    private class ContentModel{
        private boolean enable = false;
        private boolean emailValid = false;
        private boolean passwordValid = false;
        private int emailError = R.string.new_user_email_error;
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

        private EditText email;
        private EditText password;
        private Button button;

        // ф-я, контролирующая доступность кнопки входа в аккаунт
        private void updateEnable(){
            enable = emailValid && passwordValid;
            button.setEnabled(enable);
        }

        // ф-я, контролирующая вывод ошибок ввода
        private void updateErrors(){
            if (!emailValid){
                email.setError(getString(emailError));
            }
            if (!passwordValid){
                password.setError(getString(R.string.new_user_password_error));
            }
        }

        ContentModel(EditText email, EditText password, Button button){
            // сохранение ссылок на элеиенты интерфейса и добавление обработчиков введенных данных
            // к полям ввода
            this.email = email;
            this.email.addTextChangedListener(emailWatcher);
            this.password = password;
            this.password.addTextChangedListener(passwordWatcher);
            this.button = button;
            updateErrors();
            updateEnable();
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

        private boolean isPasswordValid(String password){
            return password != null && password.trim().length() > 5;
        }

    }

    // функция, прячущая клавиатуру
    private void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
