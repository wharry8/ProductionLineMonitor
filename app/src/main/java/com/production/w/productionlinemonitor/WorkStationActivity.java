package com.production.w.productionlinemonitor;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.production.w.productionlinemonitor.Helper.Coil;
import com.production.w.productionlinemonitor.Helper.Register;
import com.zgkxzx.modbus4And.requset.ModbusReq;
import com.zgkxzx.modbus4And.requset.OnRequestBack;

import java.util.Arrays;

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

        ModbusReq.getInstance().readCoil(new OnRequestBack<boolean[]>() {
            @Override
            public void onSuccess(boolean[] booleen) {
                Log.d(TAG, "readCoil onSuccess " + Arrays.toString(booleen));
                updateStatus(booleen);
                updateLeftCncStatus(booleen);
                updateRightCncStatus(booleen);
            }

            @Override
            public void onFailed(String msg) {
                Log.e(TAG, "readCoil onFailed " + msg);
            }
        }, 1, 0, 10000);

        ModbusReq.getInstance().readHoldingRegisters(new OnRequestBack<short[]>() {
            @Override
            public void onSuccess(short[] data) {
                Log.d(TAG, "readHoldingRegisters onSuccess " + Arrays.toString(data));
                updateTarget(data);
                updateCurrent(data);
            }

            @Override
            public void onFailed(String msg) {
                Log.e(TAG, "readHoldingRegisters onFailed " + msg);
            }
        }, 1, 0, 10000);
        updateName();
        updateUpTime();
        updateRunTime();
        updatePercent();
    }

    public void updateName () {
    }
    public void updateStatus (boolean[] booleen) {
        // todo
        // 根据传入的工站号选择对应的状态
        boolean running = booleen[Coil.station1Running];
        boolean stopped = booleen[Coil.station1Stopped];
        boolean error = booleen[Coil.station1Error];

        if (running) {
            tv_status.setText(R.string.workStationNormal);
        } else if (stopped) {
            tv_status.setText(R.string.workStationStopped);
        } else if (error) {
            tv_status.setText(R.string.workStationError);
        } else {
            tv_status.setText(R.string.unknown);
        }
    }
    public void updateUpTime () {
    }
    public void updateRunTime () {
    }
    public void updateTarget (short[] data) {
        // todo
        // 根据传入的工站号选择对应的数据
        short target = data[Register.station1TargetOutput];
        tv_target.setText(Integer.toString(target));
    }
    public void updateCurrent (short[] data) {
        // todo
        // 根据传入的工站号选择对应的数据
        short current = data[Register.station1ActualOutput];
        tv_current.setText(Integer.toString(current));
    }
    public void updatePercent () {
    }
    public void updateLeftCncStatus (boolean[] booleans) {
    }
    public void updateRightCncStatus (boolean[] booleans) {
    }
}
