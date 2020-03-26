package com.lawtest.ui.admin.services;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.lawtest.MainActivity;
import com.lawtest.R;
import com.lawtest.model.AgencyService;

import static android.content.DialogInterface.BUTTON_POSITIVE;

// активити, позволяющее просматривать и редактировать информацию о сервисе
public class ShowServiceActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_service);

        // получение ссылок на элементы интерфейса
        final TextView nameText = findViewById(R.id.showServiceName);
        final TextView descText = findViewById(R.id.showServiceDesc);
        final ImageButton editName = findViewById(R.id.showServiceEditName);
        final ImageButton editDesc = findViewById(R.id.showServiceEditDesc);

        // id выбранного сервиса
        final String serviceId = getIntent().getStringExtra("serviceId");
        // отслеживание данных о выбранном сервисе
        MainActivity.getInstance().getViewModel().getDatabase()
                .child(AgencyService.DATABASE_ENTRY)
                .child(serviceId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final AgencyService service = dataSnapshot.getValue(AgencyService.class);
                        nameText.setText(service.name);
                        descText.setText(service.description);

                        // создание диалога изменения названия сервиса по нажатию на
                        // соответствующую кнопку
                        editName.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(
                                        ShowServiceActivity.this
                                );
                                final EditText text = new EditText(getApplicationContext());
                                text.setHint(R.string.new_service_hint_name);
                                builder.setPositiveButton("Ok", null);
                                builder.setNegativeButton("Cancel", null);
                                builder.setView(text);
                                final AlertDialog dialog = builder.create();
                                dialog.show();
                                dialog.getButton(BUTTON_POSITIVE)
                                        .setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if ( text.getText().toString().trim().isEmpty() ) {
                                            // сообщение, что поле не может быть пустым
                                            AlertDialog.Builder builderInner =
                                                    new AlertDialog.Builder(
                                                            ShowServiceActivity.this
                                                    );
                                            builderInner.setMessage(R.string.show_service_name_empty);
                                            builderInner.setPositiveButton("Ok", null);
                                            builderInner.create().show();
                                        } else {
                                            // сохранение изменений в серверной дб
                                            service.name = text.getText().toString();
                                            MainActivity.getInstance().getViewModel().getDatabase()
                                                    .child(AgencyService.DATABASE_ENTRY)
                                                    .child(serviceId)
                                                    .setValue(service);
                                            dialog.dismiss();
                                        }
                                    }
                                });
                            }
                        });

                        // создание диалога изменения описания сервиса по нажатию на
                        // соответствующую кнопку
                        editDesc.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(
                                        ShowServiceActivity.this
                                );
                                final EditText text = new EditText(getApplicationContext());
                                text.setHint(R.string.new_service_hint_desc);
                                builder.setPositiveButton("Ok", null);
                                builder.setNegativeButton("Cancel", null);
                                builder.setView(text);
                                final AlertDialog dialog = builder.create();
                                dialog.show();
                                dialog.getButton(BUTTON_POSITIVE)
                                        .setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if ( text.getText().toString().trim().isEmpty() ) {
                                                    // сообщение, что поле не может быть пустым
                                                    AlertDialog.Builder builderInner =
                                                            new AlertDialog.Builder(
                                                                    ShowServiceActivity.this
                                                            );
                                                    builderInner.setMessage(R.string.show_service_desc_empty);
                                                    builderInner.setPositiveButton("Ok", null);
                                                    builderInner.create().show();
                                                } else {
                                                    // сохранение изменений в серверной дб
                                                    service.description = text.getText().toString();
                                                    MainActivity.getInstance().getViewModel().getDatabase()
                                                            .child(AgencyService.DATABASE_ENTRY)
                                                            .child(serviceId)
                                                            .setValue(service);
                                                    dialog.dismiss();
                                                }
                                            }
                                        });
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        //todo
                    }
                });
    }
}
