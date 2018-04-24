package com.production.w.productionlinemonitor;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ProductionLineActivity extends AppCompatActivity {

    private TextView tv_name;
    private TextView tv_status;
    private TextView tv_time;
    private TextView tv_speed;
    private List<TextView> leftCncList;
    private List<TextView> rightCncList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_production_line);

        bind();

        updateView();
    }
    public void updateView () {
        updateStatus();
        updateTime();
        updateSpeed();
        updateCncStatus();

        // update animations.
    }
    public void updateStatus () {

    }
    public void updateTime () {

    }
    public void updateCncStatus () {

    }
    public void updateSpeed () {

    }

    public void bind() {
        tv_name = findViewById(R.id.pl_tv_name);
        tv_status = findViewById(R.id.pl_tv_status);
        tv_time = findViewById(R.id.pl_tv_time);
        tv_speed = findViewById(R.id.pl_tv_speed);

        leftCncList = new ArrayList<>();
        rightCncList = new ArrayList<>();

        TextView tv_cnc = findViewById(R.id.pl_tv_cnc1_left);
        leftCncList.add(tv_cnc);
        tv_cnc = findViewById(R.id.pl_tv_cnc2_left);
        leftCncList.add(tv_cnc);
        tv_cnc = findViewById(R.id.pl_tv_cnc3_left);
        leftCncList.add(tv_cnc);
        tv_cnc = findViewById(R.id.pl_tv_cnc4_left);
        leftCncList.add(tv_cnc);
        tv_cnc = findViewById(R.id.pl_tv_cnc5_left);
        leftCncList.add(tv_cnc);

        tv_cnc = findViewById(R.id.pl_tv_cnc1_right);
        rightCncList.add(tv_cnc);
        tv_cnc = findViewById(R.id.pl_tv_cnc2_right);
        rightCncList.add(tv_cnc);
        tv_cnc = findViewById(R.id.pl_tv_cnc3_right);
        rightCncList.add(tv_cnc);
        tv_cnc = findViewById(R.id.pl_tv_cnc4_right);
        rightCncList.add(tv_cnc);
        tv_cnc = findViewById(R.id.pl_tv_cnc5_right);
        rightCncList.add(tv_cnc);
    }

    public void init () {

    }
}

