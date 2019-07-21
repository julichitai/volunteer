package com.here.android.example.volunteer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainwindow);

        Button btnOrder = findViewById(R.id.orderButton);
        View.OnClickListener oclBtnOrder = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                startActivity(new Intent(MainActivity.this, OrderActivity.class));

            }
        };
        btnOrder.setOnClickListener(oclBtnOrder);

        Button btnWork = findViewById(R.id.workButton);
        View.OnClickListener oclBtnWork = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                startActivity(new Intent(MainActivity.this, LoginRegisterActivity.class));

            }
        };
        btnWork.setOnClickListener(oclBtnWork);
    }
}
