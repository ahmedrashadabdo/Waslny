package com.waslnyapp.waslny;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.waslnyapp.waslny.customer.CustomerMapActivity;
import com.waslnyapp.waslny.driver.EndTrip;

public class PaymentsActivity extends AppCompatActivity {

    private ImageView Back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payments);
        Back = findViewById(R.id.back);

        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PaymentsActivity.this, CustomerMapActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

}
