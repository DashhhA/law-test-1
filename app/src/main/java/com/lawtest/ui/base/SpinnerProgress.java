package com.lawtest.ui.base;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import com.lawtest.R;

// "крутящийся кружок" посреди экрана с прозрачным фоном
public class SpinnerProgress extends ProgressDialog {
    public SpinnerProgress(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setCancelable(false);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        setContentView(R.layout.progress_spinner);
        super.onStart();
    }
}
