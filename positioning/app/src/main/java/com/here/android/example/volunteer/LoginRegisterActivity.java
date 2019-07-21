package com.here.android.example.volunteer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;



public class LoginRegisterActivity extends AppCompatActivity {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_login:
                    loadFragment(LoginFragment.newInstance());
                    return true;
                case R.id.navigation_register:
                    loadFragment(RegisterFragment.newInstance());
                    return true;
            }
            return false;
        }
    };

    private void loadFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fl_content, fragment);
        ft.commit();
    }
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("secure", MODE_PRIVATE);
        String token = prefs.getString("token", "");
        //if(!token.isEmpty()){
        //    startActivity(new Intent(LoginRegisterActivity.this, VolunteerActivity.class));
        //} else {
            setContentView(R.layout.activity_loginregister);
            BottomNavigationView navView = findViewById(R.id.navigation);
            navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
            loadFragment(LoginFragment.newInstance());
      //  }

    }
}
