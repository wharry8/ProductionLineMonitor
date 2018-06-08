package com.production.w.productionlinemonitor;

import java.util.Date;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.icu.text.TimeZoneFormat;
import android.os.AsyncTask;
import android.os.Handler;
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
import com.production.w.productionlinemonitor.Model.Hand;
import com.zgkxzx.modbus4And.requset.ModbusParam;
import com.zgkxzx.modbus4And.requset.ModbusReq;
import com.zgkxzx.modbus4And.requset.OnRequestBack;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

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

    private int target;
    private int current;
    private float percent;

    private int period = 1000;
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            updateView2();
            handler.postDelayed(this, period);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initNavigationDrawer();
        bind();
//        updateView();
//        updateView2();
        handler.postDelayed(runnable, period);
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

    // 绑定TextView
    public void bind() {
        tv_status = findViewById(R.id.pl_tv_status);
        tv_speed = findViewById(R.id.pl_tv_speed);
        tv_system_running_time = findViewById(R.id.tv_system_running_time);
        tv_machine_running_time = findViewById(R.id.tv_machine_running_time);
        tv_target = findViewById(R.id.ma_tv_target);
        tv_current = findViewById(R.id.ma_tv_current);
        tv_up = findViewById(R.id.tv_up);
        tv_percent = findViewById(R.id.ws_tv_percent);
    }
    // v1
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

    // v2
    // 更新ui
    public void updateView2 () {
           updateUp();
           updateSpeed();
           updatePercent();
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
                Log.e(TAG, "onSuccess:register: " + Arrays.toString(data));
                Log.e(TAG, "onSuccess: start update target");
                updateTarget(data);
                Log.e(TAG, "onSuccess: start update current");
                updateCurrent(data);
                Log.e(TAG, "onSuccess: finish update current");
            }

            @Override
            public void onFailed(String msg) {
                Log.e(TAG, "readHoldingRegisters onFailed " + msg);
            }
        }, 1, Constants.RegisterStart, Constants.RegisterLen);
          ModbusReq.getInstance().readHoldingRegisters(new OnRequestBack<short[]>() {
            @Override
            public void onSuccess(short[] data) {
                updateSystemRunningTime(data);
            }

            @Override
            public void onFailed(String msg) {
            }
        }, 1, Register.time1, 2);
    }
    // 更新状态
    public void updateStatus (boolean[] booleans) {
        boolean running = booleans[Coil.systemRunning];
        boolean stopped = booleans[Coil.systemError];
        boolean error = booleans[Coil.systemError];

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

    // 更新目标产量
    public void updateTarget (final short[] data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "updateTarget: inside updateTarget." );
                target = convertShortToInt(data[Register.systemTargetOutput]);
                Log.e(TAG, "updateTarget: inside updateTarget 2." );
                Log.e(TAG, "updateTarget: " + target );
                tv_target.setText(Integer.toString(target));
                Log.e(TAG, "updateTarget: inside updateTarget 3." );
            }
        });
    }

    // 更新当前产量
    public void updateCurrent (final short[] data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "updateCurrent: " );
//                current = data[Register.systemActualOutput];
                current = convertShortToInt(data[Register.systemActualOutput]);
                Log.e(TAG, "updateCurrent: " );
                tv_current.setText(Integer.toString(current));
            }
        });
    }
    // 更新已上料量
    public void updateUp () {
        tv_up.setText("未知");
    }
    // 更新完成率
    public void updatePercent () {
        if (target != 0) {
            percent = current / target * 100;
        } else {
            percent = 0f;
        }
        tv_percent.setText(Float.toString(percent));
    }
    // 更新速度
    public void updateSpeed () {
        // todo
        // 完成该函数
        // 知道启动时间之后, 根据当前时间获取已经运行的时间, 以分钟为单位
    }
    // 更新系统运行时间
    public void updateSystemRunningTime (short[] data) {
        // TODO
        // 这似乎是浮点数?
        int part1 = convertShortToInt(data[0]);
        int part2 = convertShortToInt(data[1]);
        int seconds = part1 + part2;
        int days = seconds % (3600 * 24);
        seconds /= (3600 * 24);
        int hours = seconds % (3600);
        seconds /= 3600;
        int mins = seconds % 60;
        seconds /= 60;
        tv_system_running_time.setText(days + "天" + hours + "时" + mins + "分" + seconds + "秒");
    }
    int convertShortToInt (short x) {
        if (x >= 0 && x < (1 << 15)) {
            return x;
        } else {
            return (1 << 15) + (x + (1 << 15));
        }
    }
    // 更新整机运行时间
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
