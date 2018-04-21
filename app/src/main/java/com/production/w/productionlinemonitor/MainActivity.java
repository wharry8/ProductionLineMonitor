package com.production.w.productionlinemonitor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // hide action bar.
        getSupportActionBar().hide();

        setContentView(R.layout.activity_main);
    }
}
