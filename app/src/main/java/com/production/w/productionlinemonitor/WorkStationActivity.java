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
import com.production.w.productionlinemonitor.Helper.Constants;
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

    private int stationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_station);

        Intent intent = getIntent();
        stationId = intent.getIntExtra(WorkStationListActivity.EXTRA_ID, 1);

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
        }, 1, Constants.CoilStart, Constants.CoilLen);

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
        }, 1, Constants.RegisterStart, Constants.RegisterLen);
        updateName();
        updateUpTime();
        updateRunTime();
        updatePercent();
    }

    public void updateName () {
    }
    public void updateStatus (boolean[] booleans) {
        boolean running = false;
        boolean stopped = false;
        boolean error = false;

        switch (stationId) {
            case 1:
                running = booleans[Coil.station1Running];
                stopped = booleans[Coil.station1Stopped];
                error = booleans[Coil.station1Error];
                break;
            case 2:
                running = booleans[Coil.station2Running];
                stopped = booleans[Coil.station2Stopped];
                error = booleans[Coil.station2Error];
                break;
            case 3:
                running = booleans[Coil.station3Running];
                stopped = booleans[Coil.station3Stopped];
                error = booleans[Coil.station3Error];
                break;
            case 4:
                running = booleans[Coil.station4Running];
                stopped = booleans[Coil.station4Stopped];
                error = booleans[Coil.station4Error];
                break;
            case 5:
                running = booleans[Coil.station5Running];
                stopped = booleans[Coil.station5Stopped];
                error = booleans[Coil.station5Error];
                break;
        }
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
        int target = 0;
        switch (stationId) {
            case 1:
                target = data[Register.station1TargetOutput];
                break;
            case 2:
                target = data[Register.station2TargetOutput];
                break;
            case 3:
                target = data[Register.station3TargetOutput];
                break;
            case 4:
                target = data[Register.station4TargetOutput];
                break;
            case 5:
                target = data[Register.station5TargetOutput];
                break;
            default:
                break;
        }
        tv_target.setText(Integer.toString(target));
    }
    public void updateCurrent (short[] data) {
        short current = 0;

        switch (stationId) {
            case 1:
                current = data[Register.station1ActualOutput];
                break;
            case 2:
                current = data[Register.station2ActualOutput];
                break;
            case 3:
                current = data[Register.station3ActualOutput];
                break;
            case 4:
                current = data[Register.station4ActualOutput];
                break;
            case 5:
                current = data[Register.station5ActualOutput];
                break;
            default:
                break;
        }
        current = data[Register.station1ActualOutput];
        tv_current.setText(Integer.toString(current));
    }

    public void updatePercent () {
    }

    public void updateLeftCncStatus (boolean[] booleans) {
    }

    public void updateRightCncStatus (boolean[] booleans) {
    }
}
