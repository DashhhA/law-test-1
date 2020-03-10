package com.lawtest.ui.admin.new_specialist;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lawtest.R;
import com.lawtest.model.AgencyService;
import com.lawtest.model.Specialist;
import com.lawtest.model.UserRepository;
import com.lawtest.ui.base.MAlertDialog;
import com.lawtest.util.MultiTaskCompleteWatcher;
import com.lawtest.util.crypto;
import com.lawtest.util.utils;

import java.util.ArrayList;

public class NewSpecialistActivity extends AppCompatActivity {

    private NewSpecialistViewModel viewModel;
    private ProgressDialog dialog;
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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_specialist);

        dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.new_specialist_registering));

        // настройк toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_new_specialist);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowTitleEnabled(false);
        ab.setDisplayHomeAsUpEnabled(true);

        // получение ссылок на элементы пользовательского интерфейса
        Button submit = findViewById(R.id.newSpecSubmit);
        final EditText fName = findViewById(R.id.newSpecFstName);
        final EditText sName = findViewById(R.id.newSpecSndName);
        final EditText surName = findViewById(R.id.newSpecSurname);
        final EditText email = findViewById(R.id.newSpecEmail);
        final EditText password = findViewById(R.id.newSpecPassword);
        final Spinner spinner = findViewById(R.id.newSpecSpinner);
        sName.addTextChangedListener(sNameTextWatcher);

        // получение ViewModel и заполнение полей пользовательского интерфейса в соответствии
        // с сохраненными данными.
        viewModel = ViewModelProviders.of(this).get(NewSpecialistViewModel.class);
        String s = viewModel.getfName();
        if (s != null) fName.setText(s);
        s = viewModel.getsName();
        if (s != null) sName.setText(s);
        s = viewModel.getSurName();
        if (s != null) surName.setText(s);
        s = viewModel.getEmail();
        if (s != null) email.setText(s);

        final ServicesArrayAdapter arrayAdapter = new ServicesArrayAdapter(this, 0);
        spinner.setAdapter(arrayAdapter);
        spinner.setEnabled(!arrayAdapter.isEmpty());
        viewModel.getService().observe(this, new Observer<ArrayList<AgencyService>>() {
            @Override
            public void onChanged(ArrayList<AgencyService> agencyServices) {
                arrayAdapter.clear();
                for (AgencyService service: agencyServices) {
                    arrayAdapter.add(service);
                }
                arrayAdapter.notifyDataSetChanged();
                spinner.setEnabled(!arrayAdapter.isEmpty());
            }
        });

        new ContentModel(fName, surName, email, password, submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();

                ArrayList<String> services_ids = new ArrayList<>();
                for (AgencyService service: arrayAdapter.getSelected()) {
                    services_ids.add(service.id);
                }

                final Specialist specialist = new Specialist(
                        viewModel.getfName(),
                        viewModel.getsName(),
                        viewModel.getSurName(),
                        viewModel.getEmail(),
                        services_ids,
                        crypto.getPassSalt(password.getText().toString()),
                        null
                );

                final DatabaseReference database = viewModel.getDatabase();

                MultiTaskCompleteWatcher taskCompleteWatcher = new MultiTaskCompleteWatcher() {
                    @Override
                    public void allComplete() {
                        dialog.dismiss();
                        finish();
                    }

                    @Override
                    public void onTaskFailed(Task task, Exception exception) {
                        MAlertDialog dialog = new MAlertDialog(NewSpecialistActivity.this,
                                "Error:" + exception.getMessage());
                        dialog.dismiss();
                    }
                };

                final MultiTaskCompleteWatcher.Task databaseTask = taskCompleteWatcher.newTask();
                final MultiTaskCompleteWatcher.Task saltTask = taskCompleteWatcher.newTask();
                final MultiTaskCompleteWatcher.Task authTask = taskCompleteWatcher.newTask();

                OnCompleteListener<AuthResult> authListener = new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            database.child(Specialist.DATABASE_TAG)
                                    .child(task.getResult().getUser().getUid())
                                    .setValue(specialist.toMap())
                                    .addOnCompleteListener(
                                            new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful())databaseTask.complete();
                                                    else databaseTask.fail(task.getException());
                                                }
                                            });
                            authTask.complete();
                        }else {
                            authTask.fail(task.getException());
                        }
                    }
                };

                database.child(UserRepository.EMAIL_TO_SALT_TAG)
                        .child(utils.emailForDatabase(viewModel.getEmail()))
                        .setValue(utils.bytesToArray(specialist.salt))
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) saltTask.complete();
                                else saltTask.fail(task.getException());
                            }
                        });

                FirebaseAuth auth = viewModel.getAuth();
                auth.createUserWithEmailAndPassword(specialist.email, new String(specialist.pass))
                .addOnCompleteListener(authListener);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
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
