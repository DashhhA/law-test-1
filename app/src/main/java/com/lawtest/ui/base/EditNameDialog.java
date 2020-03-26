package com.lawtest.ui.base;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.lawtest.R;

import static android.content.DialogInterface.BUTTON_POSITIVE;

// класс, создающий диалог для изменения ФИО
public class EditNameDialog{
    private OnValuesSetListener listener;

    // интерфейс для получения значений после того, как они были заданы
    public interface OnValuesSetListener{
        void onValuesSet(String fName, String sName, String surName);
    }

    public EditNameDialog(final Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // получение view и ссылок на элементы интерфейса
        View root = LayoutInflater.from(context).inflate(R.layout.dialog_edit_name,null);
        final EditText fName = root.findViewById(R.id.persDialogfName);
        final EditText sName = root.findViewById(R.id.persDialogsName);
        final EditText surName = root.findViewById(R.id.persDialogsurName);

        builder.setView(root);
        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton("Ok", null);

        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( isInputValid(fName, sName, surName) ) {
                    if (listener != null) {
                        listener.onValuesSet(
                                fName.getText().toString(),
                                sName.getText().toString(),
                                surName.getText().toString()
                        );
                        dialog.dismiss();
                    }
                } else {
                    // показывается если соответствующие поля не заполнены
                    AlertDialog.Builder builderInner = new AlertDialog.Builder(context);
                    builderInner.setMessage(R.string.edit_name_warning);
                    builderInner.setPositiveButton("Ok", null);
                    builderInner.create().show();
                }
            }
        });
    }

    public void setOnValuesSetListener(OnValuesSetListener listener) {
        this.listener = listener;
    }

    private boolean isInputValid(EditText fName, EditText sName, EditText surName) {
        return  !fName.getText().toString().trim().isEmpty() &&
                !surName.getText().toString().trim().isEmpty();
    }
}
