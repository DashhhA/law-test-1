package com.lawtest.ui.user;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.lawtest.MainActivity;
import com.lawtest.R;
import com.lawtest.model.User;

public class UserActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // инициализация элементов view
        Toolbar toolbar = findViewById(R.id.user_toolbar);
        setSupportActionBar(toolbar);

        // инициализация navigation drawer и navigation component
        DrawerLayout drawer = findViewById(R.id.user_drawer_layout);
        NavigationView navigationView = findViewById(R.id.user_nav_view);
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_specialists, R.id.nav_user_appointments, R.id.nav_info)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_user_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // получение ссылок на элементы view
        View hView =  navigationView.getHeaderView(0);
        final TextView userNameText = hView.findViewById(R.id.sidebarUsernameView);
        final TextView emailText = hView.findViewById(R.id.sidebarEmailView);
        final ImageView avaView = hView.findViewById(R.id.avatarImageView);

        // уведомление о загрузке информации
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage(getString(R.string.specialist_loading));
        progress.show();

        // обновление элементов view по коллбакам
        LiveData<User> user = MainActivity.getInstance().getViewModel().getUser();
        user.observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                progress.dismiss();
                userNameText.setText(
                        String.format("%s %s", user.fName, user.surName)
                );
                emailText.setText(user.email);
                if ( user.getAvatarUri() != null ) avaView.setImageURI(user.getAvatarUri());
                else avaView.setImageResource(R.drawable.ic_user_default); // default image
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_user_host_fragment);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
