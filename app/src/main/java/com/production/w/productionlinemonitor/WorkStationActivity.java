package com.production.w.productionlinemonitor;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.support.annotation.VisibleForTesting;
import android.support.constraint.ConstraintLayout;
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
    private ConstraintLayout cl_left_cnc_container;
    private ConstraintLayout cl_right_cnc_container;

    private int stationId;

    private int current;
    private int target;
    private boolean leftCNCWorking;
    private boolean rightCNCWorking;
    private float percent;

    private int period;
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            updateView();
            handler.postDelayed(this, period);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_work_station);

        Intent intent = getIntent();
        stationId = intent.getIntExtra(WorkStationListActivity.EXTRA_ID, 1);
        Log.e(TAG, "onCreate: stationId: " + stationId);

        initNavigationDrawer();
        bind();
//        updateView();
        updateName();
        handler.postDelayed(runnable, period);
    }
    private void updateName () {
        String name = "";
        tv_name.setText(Integer.toString(stationId) + "号工作站");
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(runnable, period);
    }

    // 初始化菜单栏
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

                        if (selectedTitle.equals(getString(R.string.main))) {
                            intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                        } else if (selectedTitle.equals(getString(R.string.productionLine))) {
                            intent = new Intent(getApplicationContext(), ProductionLineActivity.class);
                            startActivity(intent);
                        } else if (selectedTitle.equals(getString(R.string.workers))) {
                            intent = new Intent(getApplicationContext(), WorkStationListActivity.class);
                            startActivity(intent);
                        } else if (selectedTitle.equals(R.string.logout)) {
                            intent = new Intent(getApplicationContext(), LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                        }

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here
                        return true;
                    }
                });
    }
    // 绑定textview
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
        cl_left_cnc_container = findViewById(R.id.ws_left_cnc_container);
        cl_right_cnc_container = findViewById(R.id.ws_right_cnc_container);
    }
    // 更新ui
    public void updateView () {
        ModbusReq.getInstance().readCoil(new OnRequestBack<boolean[]>() {
            @Override
            public void onSuccess(boolean[] booleen) {
                Log.d(TAG, "onSuccess: ok");
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
        updateUpTime();
        updateRunTime();
        updatePercent();
    }
    // 更新状态
    public void updateStatus (final boolean[] booleans) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
        });
    }
    // 更新开机时间(有必要?)
    public void updateUpTime () {
    }
    // 更新运行时间
    public void updateRunTime () {
    }
    // 更新目标产量
    public void updateTarget (final short[] data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
                if (target < 0) {
                    target = (1 << 15) + (1 << 15) + target;
                }
                tv_target.setText(Integer.toString(target));
            }
        });
    }
    // 更新当前状态
    public void updateCurrent (final short[] data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
                if (current < 0) {
                    current = (1 << 15) + (1 << 15) + current;
                }
                tv_current.setText(Integer.toString(current));
            }
        });
    }
    // 更新百分比
    public void updatePercent () {
        if (target != 0) {
            percent = current / target * 100.0f;
        } else {
            percent = 0f;
        }
        tv_percent.setText(Float.toString(percent));
    }
    // 更新左CNC的状态
    public void updateLeftCncStatus (final boolean[] booleans) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (stationId){
                    case 1:
                        leftCNCWorking = booleans[Coil.station1LeftCNCWorking];
                        break;
                    case 2:
                        leftCNCWorking = booleans[Coil.station2LeftCNCWorking];
                        break;
                    case 3:
                        leftCNCWorking = booleans[Coil.station3LeftCNCWorking];
                        break;
                    case 4:
                        leftCNCWorking = booleans[Coil.station4LeftCNCWorking];
                        break;
                    case 5:
                        leftCNCWorking = booleans[Coil.station5LeftCNCWorking];
                        break;
                    default:
                        break;
                }
                if (!leftCNCWorking) {
                    tv_cnc_left_status.setText(R.string.cncNormal);
                    cl_left_cnc_container.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                } else {
                    tv_cnc_left_status.setText(R.string.cncStopped);
                    cl_left_cnc_container.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                }
            }
        });
    }
    // 更新右CNC的状态
    public void updateRightCncStatus (final boolean[] booleans) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                switch (stationId){
                    case 1:
                        rightCNCWorking = booleans[Coil.station1RightCNCWorking];
                        break;
                    case 2:
                        rightCNCWorking = booleans[Coil.station2RightCNCWorking];
                        break;
                    case 3:
                        rightCNCWorking = booleans[Coil.station3RightCNCWorking];
                        break;
                    case 4:
                        rightCNCWorking = booleans[Coil.station4RightCNCWorking];
                        break;
                    case 5:
                        rightCNCWorking = booleans[Coil.station5RightCNCWorking];
                        break;
                    default:
                        break;
                }
                if (!rightCNCWorking) {
                    tv_cnc_right_status.setText(R.string.cncNormal);
                    cl_right_cnc_container.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                } else {
                    tv_cnc_right_status.setText(R.string.cncStopped);
                    cl_right_cnc_container.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                }
            }
        });
    }
}
