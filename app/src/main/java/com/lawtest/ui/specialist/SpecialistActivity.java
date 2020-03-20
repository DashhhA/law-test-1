package com.lawtest.ui.specialist;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.lawtest.R;
import com.lawtest.model.Specialist;
import com.lawtest.ui.login.LogInActivity;

public class SpecialistActivity extends AppCompatActivity {
    private AppBarConfiguration appBarConfiguration;
    private SpecialistViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specialist);

        // инициализация элементов view
        Toolbar toolbar = findViewById(R.id.specialist_toolbar);
        setSupportActionBar(toolbar);

        // инициализация navigation drawer и navigation component
        DrawerLayout drawer = findViewById(R.id.specialist_drawer_layout);
        NavigationView navigationView = findViewById(R.id.specialist_nav_view);
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_spec_home,
                R.id.nav_spec_appointments,
                R.id.nav_spec_reviews,
                R.id.nav_spec_info)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_specialist_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // получение ссылок на элементы view
        View hView =  navigationView.getHeaderView(0);
        final TextView userNameText = hView.findViewById(R.id.specSidebarUsernameView);
        final TextView emailText = hView.findViewById(R.id.specSidebarEmailView);
        final ImageView avaView = hView.findViewById(R.id.specAvatarImageView);

        // уведомление о загрузке информации
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage(getString(R.string.specialist_loading));
        progress.show();

        // получение ViewModel и обновление элементов view по коллбакам
        viewModel = ViewModelProviders.of(this).get(SpecialistViewModel.class);
        viewModel.getPerson()
        .observe(this, new Observer<Specialist>() {
            @Override
            public void onChanged(Specialist specialist) {
                progress.dismiss();
                userNameText.setText(String.format("%s %s", specialist.fName, specialist.surName));
                emailText.setText(specialist.email);

                avaView.setImageURI(null);
                if ( specialist.getAvatarUri() != null ) avaView.setImageURI(specialist.getAvatarUri());
                else avaView.setImageResource(R.drawable.ic_user_default); // default image
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_specialist_host_fragment);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
