package com.production.w.productionlinemonitor;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

public class WorkStationActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    String TAG = "WorkStationActiviy";
    private TextView tv_name;
    private TextView tv_status;
    private TextView tv_up_time;
    private TextView tv_run_time;
    private TextView tv_target;
    private TextView tv_current;
    private TextView tv_percent;
    private TextView tv_cnc_left_status;
    private TextView tv_cnc_right_status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_station);

        initNavigationDrawer();
        bind();
        updateView();
    }
    public void initNavigationDrawer () {

        mDrawerLayout = findViewById(R.id.ws_drawer_layout);

        NavigationView navigationView = findViewById(R.id.ws_nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();
                        String selectedTitle = (String) menuItem.getTitle();
                        Log.e(TAG, "onNavigationItemSelected: " + selectedTitle);
                        Intent intent;

                        if (selectedTitle == getString(R.string.main)) {
                            intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                        } else if (selectedTitle == getString(R.string.productionLine)) {
                            intent = new Intent(getApplicationContext(), ProductionLineActivity.class);
                            startActivity(intent);
                        } else if (selectedTitle == getString(R.string.workers)) {
                            intent = new Intent(getApplicationContext(), WorkStationListActivity.class);
                            startActivity(intent);
                        } else {

                        }

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here
                        return true;
                    }
                });
    }

    public void bind() {
        tv_name = findViewById(R.id.ws_tv_name);
        tv_status = findViewById(R.id.ws_tv_status);
        tv_up_time = findViewById(R.id.ws_tv_up_time);
        tv_run_time = findViewById(R.id.ws_tv_run_time);
        tv_target = findViewById(R.id.ws_tv_target);
        tv_current = findViewById(R.id.ws_tv_current);
        tv_percent = findViewById(R.id.ws_tv_percent);
        tv_cnc_left_status = findViewById(R.id.ws_tv_cnc_left_status);
        tv_cnc_right_status = findViewById(R.id.ws_tv_cnc_right_status);
    }

    public void updateView () {
        updateName();
        updateStatus();
        updateUpTime();
        updateRunTime();
        updateTarget();
        updateCurrent();
        updatePercent();
        updateLeftCncStatus();
        updateRightCncStatus();
    }

    public void updateName () {
    }
    public void updateStatus () {
    }
    public void updateUpTime () {
    }
    public void updateRunTime () {
    }
    public void updateTarget () {
    }
    public void updateCurrent () {
    }
    public void updatePercent () {
    }
    public void updateLeftCncStatus () {
    }
    public void updateRightCncStatus () {
    }
}
