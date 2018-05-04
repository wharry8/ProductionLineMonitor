package com.production.w.productionlinemonitor;

import android.content.Intent;
import android.icu.text.TimeZoneFormat;
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
import com.zgkxzx.modbus4And.requset.ModbusParam;
import com.zgkxzx.modbus4And.requset.ModbusReq;
import com.zgkxzx.modbus4And.requset.OnRequestBack;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private DrawerLayout mDrawerLayout;
    private TextView tv_status;
    private TextView tv_speed;
    private TextView tv_system_running_time;
    private TextView tv_machine_running_time;
    private TextView tv_target;
    private TextView tv_current;
    private TextView tv_up;
    private TextView tv_percent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initNavigationDrawer();
        initModbusInstance();
        bind();
//        updateView();
        updateView2();
    }

    public void initModbusInstance() {
        // todo
        // 1. enable change connection settings at runtime.
        ModbusReq.getInstance().setParam(new ModbusParam()
                .setHost("192.168.0.105")
                .setPort(502)
                .setEncapsulated(false)
                .setKeepAlive(true)
                .setTimeout(2000)
                .setRetries(0))
                .init(new OnRequestBack<String>() {
                    @Override
                    public void onSuccess(String s) {
                        Log.d(TAG, "onSuccess " + s);
                    }

                    @Override
                    public void onFailed(String msg) {
                        Log.d(TAG, "onFailed " + msg);
                    }
                });
    }
    public void initNavigationDrawer() {
        mDrawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();
                        String selectedTitle = (String) menuItem.getTitle();
                        Intent intent;

                        if (selectedTitle.equals(getString(R.string.main))) {

                        } else if (selectedTitle.equals(getString(R.string.productionLine))) {
                            intent = new Intent(getApplicationContext(), ProductionLineActivity.class);
                            startActivity(intent);

                        } else if (selectedTitle.equals(getString(R.string.workers))) {
                            intent = new Intent(getApplicationContext(), WorkStationListActivity.class);
                            startActivity(intent);

                        } else if (selectedTitle.equals(getString(R.string.logout))) {
                            intent = new Intent(getApplicationContext(), LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here
                        return true;
                    }
                });
    }

    public void bind() {
        tv_status = findViewById(R.id.pl_tv_status);
        tv_speed = findViewById(R.id.pl_tv_speed);
        tv_system_running_time = findViewById(R.id.tv_system_running_time);
        tv_machine_running_time = findViewById(R.id.tv_machine_running_time);
        tv_target = findViewById(R.id.ws_tv_target);
        tv_current = findViewById(R.id.ws_tv_current);
        tv_up = findViewById(R.id.tv_up);
        tv_percent = findViewById(R.id.ws_tv_percent);
    }
    public void updateView() {

        boolean running = isRunning();
        boolean stopped = isRunning();
        boolean error = isError();

        if (running) {
            tv_status.setText(R.string.normal);
        } else if (stopped) {
            tv_status.setText(R.string.stopped);
        } else if (error) {
            tv_status.setText(R.string.error);
        } else {
            // unknown status.
            tv_status.setText(R.string.error);
        }
        if (running) {
            int speed = getSpeed();
            tv_speed.setText(getSpeedString(speed));
        } else {
            tv_speed.setText(R.string.defaultSpeed);
        }

        if (running) {
            long system_running_time = getSystemRunningTime();
            tv_system_running_time.setText(getTimeString(system_running_time));
        } else {
            tv_system_running_time.setText(R.string.defaultTime);
        }

        if (running) {
            long machine_running_time = getMachineRunningTime();
            tv_machine_running_time.setText(getTimeString(machine_running_time));
        } else {
            tv_machine_running_time.setText(R.string.defaultTime);
        }

        if (running) {
            int target = getTarget();
            tv_target.setText(Integer.toString(target));
        } else {
            tv_target.setText(R.string.defaultTarget);
        }

        if (running) {
            int current = getCurrent();
            tv_current.setText(Integer.toString(current));
        } else {
            tv_current.setText(R.string.defaultCurrent);
        }

        if (running) {
            int up = getUp();
            tv_up.setText(Integer.toString(up));
        } else {
            tv_up.setText(R.string.defaultUp);
        }

        if (running) {
            double percent = getPercent();
            tv_percent.setText(getPercentString(percent));
        } else {
            tv_percent.setText(R.string.defaultPercent);
        }
    }
    public void updateView2 () {
        ModbusReq.getInstance().readCoil(new OnRequestBack<boolean[]>() {
            @Override
            public void onSuccess(boolean[] booleen) {
                updateStatus(booleen);
            }

            @Override
            public void onFailed(String msg) {
                Log.e(TAG, "readCoil onFailed " + msg);
            }
        }, 1, Constants.CoilStart, Constants.CoilLen);

        ModbusReq.getInstance().readHoldingRegisters(new OnRequestBack<short[]>() {
            @Override
            public void onSuccess(short[] data) {
                updateTarget(data);
                updateCurrent(data);
            }

            @Override
            public void onFailed(String msg) {
                Log.e(TAG, "readHoldingRegisters onFailed " + msg);
            }
        }, 1, Constants.RegisterStart, Constants.RegisterLen);
    }

    public void updateStatus (boolean[] booleen) {
        boolean running = booleen[Coil.systemRunning];
        boolean stopped = booleen[Coil.systemError];
        boolean error = booleen[Coil.systemError];

        if (running) {
            tv_status.setText(R.string.normal);
        } else if (stopped) {
            tv_status.setText(R.string.stopped);
        } else if (error) {
            tv_status.setText(R.string.error);
        } else {
            tv_status.setText(R.string.unknown);
        }
    }
    public void updateTarget (short[] data) {
        short target = data[Register.systemTargetOutput];
        tv_target.setText(Integer.toString(target));
    }
    public void updateCurrent (short[] data) {
        short current = data[Register.systemActualOutput];
        tv_current.setText(Integer.toString(current));
    }
    public void updateUp () {
    }
    public void updatePercent () {
    }
    public void updateSpeed () {
    }
    public void updateSystemRunningTime () {
    }
    public void updateMachineRunningTime () {
    }

    public String getSpeedString(int speed) {
        return speed + ".0/min";
    }
    public String getTimeString(long time) {
        return  "";
    }
    public String getPercentString (double d) {
        return d + "%";
    }
    public boolean isRunning () {
        return false;
    }
    public boolean isError () {
        return false;
    }
    public boolean isStopped() {
        return false;
    }
    public int getSpeed() {
        return 0;
    }
    public int getSystemRunningTime() {
        return 0;
    }
    public int getMachineRunningTime() {
        return 0;
    }
    public int getTarget() {
        return 0;
    }
    public int getCurrent() {
        return 0;
    }
    public int getUp() {
        return 0;
    }
    public double getPercent() {
        return 0;
    }
}
