package com.lawtest.ui.admin.new_service;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.lawtest.MainActivity;
import com.lawtest.R;
import com.lawtest.model.AgencyService;

import java.util.UUID;

public class NewServiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_service);

        // настройк toolbar
        Toolbar toolbar = findViewById(R.id.toolbarNewService);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowTitleEnabled(false);
        ab.setDisplayHomeAsUpEnabled(true);

        final EditText name = findViewById(R.id.newServiceName);
        final EditText desc = findViewById(R.id.newServiceDescription);
        Button submit = findViewById(R.id.newServiceSubmit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progress = new ProgressDialog(NewServiceActivity.this);
                progress.setMessage(getString(R.string.login_checking));
                progress.show();

                AgencyService service = new AgencyService();
                service.id = UUID.randomUUID().toString();
                service.name = name.getText().toString();
                service.description = desc.getText().toString();

                DatabaseReference database = MainActivity.getInstance().getViewModel().getDatabase();
                database.child(AgencyService.DATABASE_ENTRY)
                        .child(service.id)
                        .setValue(service)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                progress.dismiss();
                                if (task.isSuccessful()) {
                                    finish();
                                } else {
                                    AlertDialog.Builder builder =
                                            new AlertDialog.Builder(NewServiceActivity.this);
                                    builder.setTitle("Error");
                                    builder.setMessage(task.getException().getMessage());
                                    builder.setPositiveButton("Ok",
                                            new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    });
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                }
                            }
                        });
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
}
