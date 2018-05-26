package com.production.w.productionlinemonitor;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.provider.SyncStateContract;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.production.w.productionlinemonitor.Helper.Coil;
import com.production.w.productionlinemonitor.Helper.Constants;
import com.production.w.productionlinemonitor.Helper.Destination;
import com.production.w.productionlinemonitor.Helper.HandPosition;
import com.production.w.productionlinemonitor.Model.Area;
import com.production.w.productionlinemonitor.Model.AssemblyLine;
import com.production.w.productionlinemonitor.Model.Box;
import com.production.w.productionlinemonitor.Model.Car;
import com.production.w.productionlinemonitor.Model.Hand;
import com.production.w.productionlinemonitor.Model.WorkStation;
import com.zgkxzx.modbus4And.requset.ModbusParam;
import com.zgkxzx.modbus4And.requset.ModbusReq;
import com.zgkxzx.modbus4And.requset.OnRequestBack;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.file.attribute.PosixFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReferenceArray;

import fr.arnaudguyon.smartgl.opengl.RenderPassSprite;
import fr.arnaudguyon.smartgl.opengl.SmartGLRenderer;
import fr.arnaudguyon.smartgl.opengl.SmartGLView;
import fr.arnaudguyon.smartgl.opengl.SmartGLViewController;
import fr.arnaudguyon.smartgl.opengl.Sprite;
import fr.arnaudguyon.smartgl.opengl.Texture;
import fr.arnaudguyon.smartgl.touch.TouchHelperEvent;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.DeflaterSink;

public class ProductionLineActivity extends AppCompatActivity implements SmartGLViewController {

    String TAG = "ProductionLineActivity";
    private TextView tv_name;
    private TextView tv_status;
    private TextView tv_time;
    private TextView tv_speed;
    private List<TextView> leftCncList;
    private List<TextView> rightCncList;
    private DrawerLayout mDrawerLayout;

    // SmartGL
    private SmartGLView mSmartGLView;
    private RenderPassSprite renderPassSprite;
    private SmartGLRenderer renderer;

    public static float glHeight;
    public static float glWidth;
    public static float unitHeight;
    public static float unitWidth;

    Area preparationArea;

    Car car1;
    Car car2;
    WorkStation ws;
    List<WorkStation> workStationList;

    // used by run()
    int previousReachIndex;
    float blockX;

    int index = 0;
    int currentStatus[];
    int previousStatus[];

    // used by updateCar1()
    int previousPosition;
    int car1PreviousPosition;
    int car2PreviousPosition;
    boolean currentState[];
    boolean previousState[];

    // used by updateCar1_v2()

    OkHttpClient client;

    // used by fake animation
    private int fake_index;
    private int fake_need[];
    private int fake_current[];

    private long groupId;

    int period = 1000;
    Handler udpateViewHandler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            updateView();
            udpateViewHandler.postDelayed(this, period);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // landscape mode.
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_production_line);

        initModbus();
        initNavigationDrawer();
        bind();

        initSmartGL();
//        run2();
//        fake_animation();
//        fake_animation2();
//        run2();

        // works
        groupId = new Date().getTime();
        udpateViewHandler.postDelayed(runnable, period);
        run4();
//        run3();
//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                final boolean[] booleans = new boolean[3];
//                Log.d(TAG, "run: " + Arrays.toString(booleans));
//                writeToDB(booleans);
//            }
//        }, 1000);
    }
    private void initModbus() {
        ModbusReq.getInstance().setParam(new ModbusParam()
                .setHost("192.168.1.100")
                .setPort(8010)
                .setEncapsulated(false)
                .setKeepAlive(true)
                .setTimeout(2000)
                .setRetries(0))
                .init(new OnRequestBack<String>() {
                    @Override
                    public void onSuccess(String s) {
                        Log.e(TAG, "onSuccess 连接服务器成功" + s);
                    }
                    @Override
                    public void onFailed(String msg) {
                        Log.e(TAG, "onFailed 连接服务器失败" + msg);
                    }
                });
    }

    // 初始化菜单栏
    public void initNavigationDrawer() {

        mDrawerLayout = findViewById(R.id.pl_drawer_layout);

        NavigationView navigationView = findViewById(R.id.pl_nav_view);

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

                        } else if (selectedTitle.equals(getString(R.string.workers))) {
                            intent = new Intent(getApplicationContext(), WorkStationListActivity.class);
                            startActivity(intent);

                        } else if (selectedTitle.equals(getString(R.string.logout))) {
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
    // 初始化 SmartGL
    public void initSmartGL() {
        mSmartGLView = (SmartGLView) findViewById(R.id.smartGLView);
        mSmartGLView.setDefaultRenderer(this);
        mSmartGLView.setController(this);
    }
    // 更新界面
    public void updateView() {
        ModbusReq.getInstance().readCoil(new OnRequestBack<boolean[]>() {
            @Override
            public void onSuccess(boolean[] booleen) {
                Log.d(TAG, "readCoil onSuccess " + Arrays.toString(booleen));
                updateStatus(booleen);
                updateCncStatus(booleen);
            }

            @Override
            public void onFailed(String msg) {
                Log.e(TAG, "readCoil onFailed " + msg);
            }
        }, 1, Constants.CoilStart, Constants.CoilLen);
        // todo
        // 1. implement updateTime()
        // 2. implement updateSpeed()
        updateTime();
        updateSpeed();
    }
    // 更新状态信息
    public void updateStatus(final boolean[] booleen) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean running = booleen[Coil.systemRunning];
                boolean stopped = booleen[Coil.systemStopped];
                boolean error = booleen[Coil.systemError];

                Log.e(TAG, "updateStatus: running: " + running  + ", stopped: " + stopped + ", error:" + error );
                if (running) {
                    Log.e(TAG, "updateStatus: not here.");
                    tv_status.setText(R.string.normal);
                } else if (stopped) {
                    Log.e(TAG, "updateStatus: set");
                    tv_status.setText(R.string.stopped);
                    Log.e(TAG, "updateStatus: " + tv_status.getText());
                } else if (error) {
                    tv_status.setText(R.string.error);
                } else {
                    tv_status.setText(R.string.unknown);
                }
            }
        });
    }
    // 更新CNC状态信息
    // 是否屏蔽
    public void updateCncStatus(boolean[] booleans) {
        _updateCncStatus(leftCncList.get(0), booleans[Coil.station1LeftCNCWorking]);
        _updateCncStatus(rightCncList.get(0), booleans[Coil.station1RightCNCWorking]);

        _updateCncStatus(leftCncList.get(1), booleans[Coil.station2LeftCNCWorking]);
        _updateCncStatus(rightCncList.get(1), booleans[Coil.station2RightCNCWorking]);

        _updateCncStatus(leftCncList.get(2), booleans[Coil.station3LeftCNCWorking]);
        _updateCncStatus(rightCncList.get(2), booleans[Coil.station3RightCNCWorking]);

        _updateCncStatus(leftCncList.get(3), booleans[Coil.station4LeftCNCWorking]);
        _updateCncStatus(rightCncList.get(3), booleans[Coil.station4RightCNCWorking]);

        _updateCncStatus(leftCncList.get(4), booleans[Coil.station5LeftCNCWorking]);
        _updateCncStatus(rightCncList.get(4), booleans[Coil.station5RightCNCWorking]);
    }
    public void _updateCncStatus(final TextView tv, final boolean working) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (working) {
                    tv.setText(R.string.cncStopped);
                } else {
                    tv.setText(R.string.cncNormal);
                }
            }
        });
    }

    // 更新运行时间
    public void updateTime() {
    }
    // 更新速度
    public void updateSpeed() {
    }

    // 绑定 textview
    public void bind() {
        tv_name = findViewById(R.id.pl_tv_name);
        tv_status = findViewById(R.id.pl_tv_status_special);
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

    // 虚拟动画
    // 调用这个函数之前, 先打开本地API服务器
    private void fake_animation2() {
        //  控制每个状态需要持续多少次读取
        fake_need = new int[200];
        fake_need[0] = 0;
        fake_need[1] = 1;
        fake_need[2] = 1;
        fake_need[3] = 2;
        fake_need[4] = 1;
        fake_need[5] = 2;
        fake_need[6] = 2;
        fake_need[7] = 2;
        fake_need[8] = 2;
        fake_need[9] = 2;
        fake_need[10] = 2;
        fake_need[11] = 2;
        fake_need[12] = 2;
        fake_need[13] = 2;
        fake_need[14] = 1;
        fake_need[15] = 6;
        fake_need[16] = 1;
        fake_need[17] = 1;
        fake_need[18] = 6;
        fake_need[19] = 6;

        fake_current = new int[2000];
        for (int i = 0; i < 20; ++i) {
            fake_current[i] = 0;
        }

        currentState = new boolean[10000];
        previousState = new boolean[10000];
        fake_index = 0;
        // 1秒的延迟用来确保在动画运行之前, 资源已经加载完毕
        int delay = 1000;
        // 每2秒读取一次状态, 实际情况可能更快
        int period = 2000;
        client = new OkHttpClient();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    // 如果当前状态已经持续足够久了, 开始读取下一个状态
                    if (fake_current[fake_index] >= fake_need[fake_index]) {
                        ++fake_index;
                        if (fake_index > 19) {
                            return;
                        }
                    }
                    ++fake_current[fake_index];
                    Request request = new Request.Builder()
                            // 使用虚拟机运行时, 虚拟机的地址与电脑的地址是不一样的
//                             .url("http://127.0.0.1:3000/" + fake_index)

//                             10.0.2.2 是虚拟机转发到电脑 127.0.0.1 的地址
//                            .url("http://10.0.2.2:3000/" + fake_index)
                                .url("http://192.168.0.101:3000/" + fake_index)
//                            .url("http://192.168.42.145:3000/" + fake_index)
                            .build();
                    Response response = client.newCall(request).execute();

                    // 每调用1次 .string(), 需要重新执行一起请求
                    String jsonData = response.body().string();

                    JSONObject jsonObject = new JSONObject(jsonData);
                    JSONArray jsonArray = jsonObject.getJSONArray("status");

                    for (int i = 0; i < jsonArray.length(); ++i) {
//                        currentState[i] = jsonArray.getInt(i) == 0? false: true;
                        currentState[i] = jsonArray.getBoolean(i);
                    }
                    // 调试信息, 判断次读取到的状态是否和预期吻合
//                    Log.e(TAG, "toStart: " + fake_index + "," + currentState[Coil.car1AtStartPosition]);
//                    Log.e(TAG, "toStart: " + fake_index + "," + currentState[Coil.car1AtStartBlockPosition]);
//                    Log.e(TAG, "toS3: " + fake_index + "," + currentState[Coil.car2AtStation3ProcessingPosition]);
//                    Log.e(TAG, "hook in: " + fake_index + "," + currentState[Coil.car1HookOut]);

                    if (previousState == null) {
                        System.arraycopy(currentState, 0, previousState, 0, 10000);
                    }

                    // 由于虚拟动画只设计到工站1, 所以注释掉其他工站的同步和更新
                    Hand hand1 = workStationList.get(0).getHand();

//                            Hand hand2 =  workStationList.get(1).getHand();
//                            Hand hand3 =  workStationList.get(2).getHand();
//                            Hand hand4 =  workStationList.get(3).getHand();
//                            Hand hand5 =  workStationList.get(4).getHand();
                    if (hand1.isMatch()) {
                        updateHand1();
                    } else {
                        syncHand1();
                    }

//                            if (hand2.isMatch()) {
//                                updateHand2();
//                            } else {
//                                syncHand2();
//                            }
//
//                            if (hand3.isMatch()) {
//                                updateHand3();
//                            } else {
//                                syncHand3();
//                            }
//
//                            if (hand4.isMatch()) {
//                                updateHand4();
//                            } else {
//                                syncHand4();
//                            }
//
//                            if (hand5.isMatch()) {
//                                updateHand5();
//                            } else {
//                                syncHand5();
//                            }

                    if (car1.isMatch()) {
                        updateCar1_v2();
                        updateStation1_v2();
                        updateStation2_v2();
                        updateStation3_v2();
                    } else {
                        syncCar1();
                    }
                    if (car2.isMatch()) {
                        updateCar2_v2();
                        updateStation4_v2();
                        updateStation5_v2();
                    } else {
                        syncCar2();
                    }
                    // 下面的赋值方式是错误的, 会令 ps 和 cs 指向相同的地址
                    // previousState = currentState;
                    System.arraycopy(currentState, 0, previousState, 0, 10000);
                } catch (Exception e) {
                    Log.e(TAG, "Exceptioin at fake_animation2.doInBackground: " + e + ", signal index: " + fake_index);
                }
            }
        }, delay, period);
    }
    // 失败的动画
    private void fake_animation() {
        fake_index = 1;
        Thread thread = new Thread() {
            public void run() {
                Looper.prepare();
                int delay = 2000;
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (fake_index > 9) {
                            return;
                        }
                        try {
                            client = new OkHttpClient();
                            Request request = new Request.Builder()
                                    //                .url("http://127.0.0.1:3000/1")
                                    //                        .url("http://baidu.com")
                                    .url("http://10.0.2.2:3000/" + fake_index)
                                    .build();
                            Response response = client.newCall(request).execute();
                            ++fake_index;
                            Log.e(TAG, "response: " + response.body().string());
                        } catch (Exception e) {
                            Log.e(TAG, "doInBackground: " + e);
                        }
                    }
                }, delay);
                Looper.loop();
            }
        };
        thread.start();
    }
    class G extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                client = new OkHttpClient();
                Request request = new Request.Builder()
//                .url("http://127.0.0.1:3000/1")
//                        .url("http://baidu.com")
                        .url("http://10.0.2.2:3000/" + fake_index)
                        .build();
                Response response = client.newCall(request).execute();
                ++fake_index;
                Log.e(TAG, "response: " + response.body().string());
            } catch (Exception e) {
                Log.e(TAG, "doInBackground: " + e);
            }
            return null;
        }
    }

    // 线程1
    // 按帧显示动画, 大概1秒30帧
    @Override
    public void onTick(SmartGLView smartGLView) {
        updateAnimation();
    }
    public void updateAnimation() {

        float deltaTime = renderer.getFrameDuration();

//        Log.e(TAG, "updateAnimation: car1: " + car1.getX() + "," + car1.getSpeed() + "," + car1.getDirection());
//        car1.move(deltaTime, blockX);
//        Log.e(TAG, "updateAnimation: car1 move, index: " + (fake_index) + ", destionation: " + car1.getDestination());
        car1.move_v2(deltaTime);
//        Log.e(TAG, "updateAnimation: car2 move");
//        Log.e(TAG, "updateAnimation: car2 move, index: " + (fake_index) + ", destionation: " + car2.getDestination());
        car2.move_v2(deltaTime);

        // 工作站1
        if (workStationList.size() > 0 && workStationList.get(0).getProcessingArea().getBox() != null) {
            workStationList.get(0).getProcessingArea().getBox().update(deltaTime);
        }
        if (workStationList.size() > 0 && workStationList.get(0).getStorageArea().getBox() != null) {
            workStationList.get(0).getStorageArea().getBox().update(deltaTime);
        }
        workStationList.get(0).getHand().update(deltaTime);

        // 工作站2
        if (workStationList.size() > 1 && workStationList.get(1).getProcessingArea().getBox() != null) {
            workStationList.get(1).getProcessingArea().getBox().update(deltaTime);
        }
        if (workStationList.size() > 1 && workStationList.get(1).getStorageArea().getBox() != null) {
            workStationList.get(1).getStorageArea().getBox().update(deltaTime);
        }
        workStationList.get(1).getHand().update(deltaTime);

        // 工作站3
        if (workStationList.size() > 2 && workStationList.get(2).getProcessingArea().getBox() != null) {
            workStationList.get(2).getProcessingArea().getBox().update(deltaTime);
        }
        if (workStationList.size() > 2 && workStationList.get(2).getStorageArea().getBox() != null) {
            workStationList.get(2).getStorageArea().getBox().update(deltaTime);
        }
        workStationList.get(2).getHand().update(deltaTime);

        // 工作站4
        if (workStationList.size() > 3 && workStationList.get(3).getProcessingArea().getBox() != null) {
            workStationList.get(3).getProcessingArea().getBox().update(deltaTime);
        }
        if (workStationList.size() > 3 && workStationList.get(3).getStorageArea().getBox() != null) {
            workStationList.get(3).getStorageArea().getBox().update(deltaTime);
        }
        workStationList.get(3).getHand().update(deltaTime);

        // 工作站5
        if (workStationList.size() > 4 && workStationList.get(4).getProcessingArea().getBox() != null) {
            workStationList.get(4).getProcessingArea().getBox().update(deltaTime);
        }
        if (workStationList.size() > 4 && workStationList.get(4).getStorageArea().getBox() != null) {
            workStationList.get(4).getStorageArea().getBox().update(deltaTime);
        }
        workStationList.get(4).getHand().update(deltaTime);

    }

    private void writeToDB(boolean[] booleans) {
        Log.e(TAG, "writeToDB: hi");
        if (client == null) {
            client = new OkHttpClient();
        }
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        try {
            JSONObject signal = new JSONObject();
            try {
                String status = Arrays.toString(booleans);
                signal.put("status", status);
                signal.put("timestamp", new Date().toString());
                signal.put("groupId", Long.toString(groupId));
                Log.e(TAG, "writeToDB: data to post: " + signal.toString());
                RequestBody body = RequestBody.create(JSON, signal.toString());
                Request request = new Request.Builder()
//                        .url("http://192.168.0.101:3000/")
                        .url("http://10.0.2.2:3000/")
                        .post(body)
                        .build();
                Response response = client.newCall(request).execute();
                String resBody = response.body().string();
                Log.e(TAG, "writeToDB: response body: " + resBody);
            } catch (Exception e) {
                Log.e(TAG, "writeToDB: fail inner: " + e);
            }
        } catch (Exception e) {
            Log.e(TAG, "writeToDB: fail outer");
        }
    }

    // v3
    private void run4 () {
        Timer timer = new Timer();
        int delay = 1000;
        int period = 100;
        currentState = new boolean[600];
        previousState = new boolean[600];
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ModbusReq.getInstance().readCoil(new OnRequestBack<boolean[]>() {
                    @Override
                    public void onSuccess(boolean[] booleans) {
                        Log.e(TAG, "readCoil onSuccess " + Arrays.toString(booleans));
                         currentState = booleans;
                        if (previousState == null) {
                            System.arraycopy(currentState, 0, previousState, 0, 500);
                        }
                        if (car1.isMatch()) {
                            updateCar1_v3();
                            updateS1();
                            updateS2();
                            updateS3();
                        } else {
                            syncCar1_v3();
                            syncS1();
                            syncS2();
                            syncS3();
                        }
                        if (car2.isMatch()) {
                            updateCar2_v3();
                            updateS4();
                            updateS5();
                        } else {
                            syncCar2_v3();
                            syncS4();
                            syncS5();
                        }
                        Hand hand1 = workStationList.get(0).getHand();
                        Hand hand2 = workStationList.get(1).getHand();
                        Hand hand3 = workStationList.get(2).getHand();
                        Hand hand4 = workStationList.get(3).getHand();
                        Hand hand5 = workStationList.get(4).getHand();

//                        if (hand1.isMatch()) {
//                            syncHand1_v3();
//                        } else {
//                            updateHand1_v3();
//                        }
//                        if (hand2.isMatch()) {
//                            syncHand2_v3();
//                        } else {
//                            updateHand2_v3();
//                        }
//                        if (hand3.isMatch()) {
//                            syncHand3_v3();
//                        } else {
//                            updateHand3_v3();
//                        }
                        if (hand2.isMatch()) {
                            Log.e(TAG, "onSuccess: syncHand4");
//                            syncHand4_v3();
                            syncHand2_v4();
                        } else {
//                            Log.e(TAG, "onSuccess: updateHand4  start");
//                            updateHand4_v3();
                            updateHand2_v4();
//                            Log.e(TAG, "onSuccess: updateHand4 leave");
                        }
//                        if (hand5.isMatch()) {
//                            syncHand5_v3();
//                        } else {
//                            updateHand5_v3();
//                        }
                        System.arraycopy(currentState, 0, previousState, 0, 500);
                    }
                    @Override
                    public void onFailed(String msg) {
                        Log.e(TAG, "readCoil onFailed " + msg);
                    }
                }, 1, Constants.CoilStart, Constants.CoilLen);
            }
        },delay, period);
    }
    private void syncCar1_v3 () {
        // 当遇到某个到位信号时,将该位置作为小车的初始位置,完成同步
        // 上料位
        if (currentState[Coil.car1AtStartBlockPosition] || currentState[Coil.car1AtStartPosition]) {
            car1.setMatch(true);
            car1.setPos(Destination.initialPosition);
        }
        // 站1储备位
        if (currentState[Coil.car1AtStation1StoragePosition]) {
            car1.setMatch(true);
            car1.setPos(Destination.station1StoragePosition);
        }
        // 站1加工位
        if (currentState[Coil.car1AtStation1ProcessingPosition]) {
            car1.setMatch(true);
            car1.setPos(Destination.station1ProcessingPosition);
        }
        // 站1完成位
        if (currentState[Coil.car1AtStation1CompletionPosition]) {
            car1.setMatch(true);
            car1.setPos(Destination.station1CompletionPosition);
        }
        // 站2储备位
        if (currentState[Coil.car1AtStation2StoragePosition]) {
            car1.setMatch(true);
            car1.setPos(Destination.station2StoragePosition);
        }
        // 站2加工位
        if (currentState[Coil.car1AtStation2ProcessingPosition]) {
            car1.setMatch(true);
            car1.setPos(Destination.station2ProcessingPosition);
        }
        // 站2完成位
        if (currentState[Coil.car1AtStation2CompletionPosition]) {
            car1.setMatch(true);
            car1.setPos(Destination.station2CompletionPosition);
        }
         // 站3储备位
        if (currentState[Coil.car1AtStation3StoragePosition]) {
            car1.setMatch(true);
            car1.setPos(Destination.station3StoragePosition);
        }

        // 站3加工位
        if (currentState[Coil.car1AtStation3ProcessingPosition]) {
            car1.setMatch(true);
            car1.setPos(Destination.station3ProcessingPosition);
        }
        sync_car1_hook_in();
        sync_car1_hook_out();
    }
    private void updateCar1_v3 () {

        boolean ok = false;

        update_car1_hook_in();
        update_car1_hook_out();

        // 根据挡块的位置判断

        // 上料挡停位挡块升起
        if (currentState[Coil.startPositionBlocked]) {
            ok = true;
            _updateSpeedAndDirection(1, Destination.initialPosition);
        }

        // 站1储料位挡块升起
        if (currentState[Coil.station1StoragePositionBlocked]) {
            ok = true;
            _updateSpeedAndDirection(1, Destination.station1StoragePosition);
        }
        // 站1加工位挡块升起
        if (currentState[Coil.station1ProcessingPositionBlocked]) {
            ok = true;
            _updateSpeedAndDirection(1, Destination.station1ProcessingPosition);
        }
        // 站2储料位挡块升起
        if (currentState[Coil.station2StoragePositionBlocked]) {
            ok = true;
            _updateSpeedAndDirection(1, Destination.station2StoragePosition);
        }
        // 站2加工位挡块升起
        if (currentState[Coil.station2ProcessingPositionBlocked]) {
            ok = true;
            _updateSpeedAndDirection(1, Destination.station2ProcessingPosition);
        }
        // 站3储料位挡块升起
        if (currentState[Coil.station3StoragePositionBlocked]) {
            ok = true;
            _updateSpeedAndDirection(1, Destination.station3StoragePosition);
        }
        // 站3加工位挡块升起
        if (currentState[Coil.station3ProcessingPositionBlocked]) {
            ok = true;
            _updateSpeedAndDirection(1, Destination.station3ProcessingPosition);
        }

        // 如果没有挡块升起, 根据到位信号判断
        if (!ok) {
            if (currentState[Coil.car1AtStartPosition] || currentState[Coil.car1AtStartBlockPosition]) {
                _updateSpeedAndDirection(1, Destination.initialPosition);
            }
            if (currentState[Coil.car1AtStation1StoragePosition]) {
                _updateSpeedAndDirection(1, Destination.station1StoragePosition);
            }
            if (currentState[Coil.car1AtStation1ProcessingPosition]) {
                _updateSpeedAndDirection(1, Destination.station1ProcessingPosition);
            }
            if (currentState[Coil.car1AtStation1CompletionPosition]) {
                _updateSpeedAndDirection(1, Destination.station1CompletionPosition);
            }
            if (currentState[Coil.car1AtStation2StoragePosition]) {
                _updateSpeedAndDirection(1, Destination.station2StoragePosition);
            }
            if (currentState[Coil.car1AtStation2ProcessingPosition]) {
                _updateSpeedAndDirection(1, Destination.station2ProcessingPosition);
            }
            if (currentState[Coil.car1AtStation2CompletionPosition]) {
                _updateSpeedAndDirection(1, Destination.station2CompletionPosition);
            }
            if (currentState[Coil.car1AtStation3StoragePosition]) {
                _updateSpeedAndDirection(1, Destination.station3StoragePosition);
            }
            if (currentState[Coil.car1AtStation3ProcessingPosition]) {
                _updateSpeedAndDirection(1, Destination.station3ProcessingPosition);
            }
        }
    }
    // helper function for v3 animation
    private void _updateSpeedAndDirection (int id, float destination) {
        int speed = 100;
        float precision = 5;
        if (id == 1) {
            // car1
            car1.setDestination(destination);
            if (Math.abs(car1.getX() - destination) > precision) {
                car1.setSpeed(speed);
                if (car1.getX() < destination) {
                    car1.setDirection(Constants.RIGHT);
                }
                if (car1.getX() > destination)
                    car1.setDirection(Constants.LEFT);
            }
        } else {
            // car2
            car2.setDestination(destination);
            if (Math.abs(car2.getX() - destination) > precision) {
                car2.setSpeed(speed);
                if (car2.getX() < destination) {
                    car2.setDirection(Constants.RIGHT);
                }
                if (car2.getX() > destination) {
                    car2.setDirection(Constants.LEFT);
                }
            }

        }
    }

    private void sync_car2_hook_out () {
        if (currentState[Coil.car2HookOut]) {
            if (car2.getBox() == null) {
                Box box = generateBox(car2.getX(), car2.getY(), Constants.BOX_DECLINED);
                car2.setBox(box);
            }
        }
    }
    private void sync_car2_hook_in () {
        // 小车2处于回钩, 应该没有箱子, 如果有, 将该箱子销毁
        if (currentState[Coil.car2HookIn]) {
            if (car2.getBox() != null) {
                car2.getBox().getSprite().releaseResources();
                car2.setBox(null);
            }
        }
    }
    private void update_car2_hook_out () {
        Log.e(TAG, "update_car2_hook_out: enter, x: " + car2.getX() + ", destination: " + Destination.station3ProcessingPosition);
        // 小车2获取箱子
        if (previousState[Coil.car2HookIn] && currentState[Coil.car2HookOut]) {
            if (car2.getBox() != null) {
                Log.e(TAG, "updateCar1_v2: 小车2由回钩到出钩, 此时应该没有箱子, 但有");
            }

            // 从小车1获取箱子
            // 此时在站3加工位，发生在交接的时候
            if (Float.compare(car1.getX(), car2.getX()) == 0 && Float.compare(car1.getX(), Destination.station3ProcessingPosition) == 0) {
                if (car1.getBox() != null && workStationList.get(2).getProcessingArea().getBox() == null) {
                    Log.e(TAG, "update_car2_hook_out: exchange at: " + car1.getX());
                    car2.setBox(car1.getBox());
                    car1.setBox(null);
                }
            }
            // 小车2获取箱子
            // 小车2在站3加工位获取箱子
            if (Float.compare(car2.getX(), Destination.station3ProcessingPosition) == 0) {
                Log.e(TAG, "update_car2_hook_out: should be here");
                Area area = workStationList.get(2).getProcessingArea();
                if (area.getBox() == null) {
                    Log.e(TAG, "updateCar1_v2: 小车2在站3加工位由回钩到出钩, 此处应该有箱子, 但没有");
                } else {
                    car2.setBox(area.getBox());
                    area.setBox(null);
                }
            }
            // 小车2在站4储备位获取箱子
            if (Float.compare(car2.getX(), Destination.station4StoragePosition) == 0) {
                Area area = workStationList.get(3).getStorageArea();
                if (area.getBox() == null) {
                    Log.e(TAG, "updateCar1_v2: 小车2在站3储备位由回钩到出钩, 此处应该有箱子, 但没有");
                } else {
                    car2.setBox(area.getBox());
                    area.setBox(null);
                }
            }
            // 小车2在站4加工位获取箱子
            if (Float.compare(car2.getX(), Destination.station4ProcessingPosition) == 0) {
                Area area = workStationList.get(3).getProcessingArea();
                if (area.getBox() == null) {
                    Log.e(TAG, "updateCar1_v2: 小车2在站3加工位由回钩到出钩, 此处应该有箱子, 但没有");
                } else {
                    car2.setBox(area.getBox());
                    area.setBox(null);
                }
            }
            // 小车2在站5储备位获取箱子
            if (Float.compare(car2.getX(), Destination.station5StoragePosition) == 0) {
                Area area = workStationList.get(4).getStorageArea();
                if (area.getBox() == null) {
                    Log.e(TAG, "updateCar1_v2: 小车2在站5储备位由回钩到出钩, 此处应该有箱子, 但没有");
                } else {
                    car2.setBox(area.getBox());
                    area.setBox(null);
                }
            }
            // 小车2在站5加工位获取箱子
            if (Float.compare(car2.getX(), Destination.station5ProcessingPosition) == 0) {
                Area area = workStationList.get(4).getProcessingArea();
                if (area.getBox() == null) {
                    Log.e(TAG, "updateCar1_v2: 小车2在站5加工位由回钩到出钩, 此处应该有箱子, 但没有");
                } else {
                    car2.setBox(area.getBox());
                    area.setBox(null);
                }
            }
        }
    }
    private void update_car2_hook_in () {
        // 小车2放下箱子
        Log.e(TAG, "update_car2_hook_in: car2 x: " + car2.getX()  + ", s4 pos: " + Destination.station4ProcessingPosition);
        if (previousState[Coil.car2HookOut] && currentState[Coil.car2HookIn]) {
            if (car2.getBox() == null) {
                Log.e(TAG, "updateCar1_v2: 小车2由出钩到回钩, 此时应该有箱子, 但没有");

            }
            // 小车2在下料位放下箱子, 与其他工站不同的是，此时需要释放小车
            if (Float.compare(car2.getX(), Destination.finalPosition) == 0) {
                car2.getBox().getSprite().releaseResources();
                car2.setBox(null);
            }
            // 小车2在站3加工位放下箱子
            if (Float.compare(car2.getX(), Destination.station3ProcessingPosition) == 0) {
                Area area = workStationList.get(2).getProcessingArea();
                if (area.getBox() != null) {
                    Log.e(TAG, "updateCar1_v2: 小车2在站3加工位放下箱子, 此处原来应该没有箱子, 但有.");
                }
                area.setBox(car2.getBox());
                car2.setBox(null);
                if (area.getBox() != null) {
                    area.getBox().setStatus(Constants.BOX_RISING);
                }
            }
            // 小车2在站4储备位放下箱子
            if (Float.compare(car2.getX(), Destination.station4StoragePosition) == 0) {
                Area area = workStationList.get(3).getStorageArea();
                Log.e(TAG, "update_car2_hook_in: should be here. x :" + car2.getX() + "," + Destination.station4StoragePosition);
                if (area.getBox() != null) {
                    Log.e(TAG, "updateCar1_v2: 小车2在站4储备位放下箱子, 此处原来应该没有箱子, 但有.");
                }
                area.setBox(car2.getBox());
                car2.setBox(null);
                if (area.getBox() != null) {
                    area.getBox().setStatus(Constants.BOX_RISING);
                }
            }
            // 小车2在站4加工位放下箱子
            if (Float.compare(car2.getX(), Destination.station4ProcessingPosition) == 0) {
                Area area = workStationList.get(3).getProcessingArea();
                if (area.getBox() != null) {
                    Log.e(TAG, "updateCar1_v2: 小车2在站4加工位放下箱子, 此处原来应该没有箱子, 但有.");
                }
                area.setBox(car2.getBox());
                car2.setBox(null);
                if (area.getBox() != null) {
                    area.getBox().setStatus(Constants.BOX_RISING);
                }
            }
            // 小车2在站5储备位放下箱子
            if (Float.compare(car2.getX(), Destination.station5StoragePosition) == 0) {
                Area area = workStationList.get(4).getStorageArea();
                if (area.getBox() != null) {
                    Log.e(TAG, "updateCar1_v2: 小车2在站5储备位放下箱子, 此处原来应该没有箱子, 但有.");
                }
                area.setBox(car2.getBox());
                car2.setBox(null);
                if (area.getBox() != null) {
                    area.getBox().setStatus(Constants.BOX_RISING);
                }
            }
            // 小车2在站5加工位放下箱子
            if (Float.compare(car2.getX(), Destination.station5ProcessingPosition) == 0) {
                Area area = workStationList.get(4).getProcessingArea();
                if (area.getBox() != null) {
                    Log.e(TAG, "updateCar1_v2: 小车2在站5加工位放下箱子, 此处原来应该没有箱子, 但有.");
                }
                area.setBox(car2.getBox());
                car2.setBox(null);
                if (area.getBox() != null) {
                    area.getBox().setStatus(Constants.BOX_RISING);
                }
            }
        }
    }

    private void sync_car1_hook_out () {
        if (currentState[Coil.car1HookOut]) {
            if (car1.getBox() == null) {
                Box box = generateBox(car1.getX(), car1.getY(), Constants.BOX_DECLINED);
                car1.setBox(box);
            }
        }
    }
    private void sync_car1_hook_in () {
        // 小车1处于回钩状态, 如果此时小车1有箱子, 放下该箱子
        if (currentState[Coil.car1HookIn]) {
            if (car1.getBox() != null) {
                // 释放资源
                car1.getBox().getSprite().releaseResources();
                // 放下箱子
                car1.setBox(null);
            }
        }
    }
    private void update_car1_hook_out () {
        // 小车1获取箱子
        if (previousState[Coil.car1HookIn] && currentState[Coil.car1HookOut]) {
            // 小车1此时应该没有箱子
            if (car1.getBox() != null) {
                Log.e(TAG, "updateCar1: 小车1此时应该没有箱子，但有");
            }
            // 当前位置应该有一个箱子
            // 当前在起始位
            if (Float.compare(car1.getX(), Destination.initialPosition) == 0) {
                // 生成新的箱子
                // 将新生成的箱子放在小车上
                float boxX = car1.getX();
                float boxY = car1.getY();
                float boxWidth = car1.getWidth();
                float boxHeight = car1.getHeight() - 20;
                Texture texture = new Texture(getApplicationContext(), R.drawable.box);
                Sprite sprite = new Sprite((int) boxWidth, (int) boxHeight);
                sprite.setPivot(0.5f, 0.5f);
                sprite.setPos(boxX, boxY);
                sprite.setTexture(texture);
                Box b = new Box(boxX, boxY, boxWidth, boxHeight, texture, sprite);
                b.render(renderPassSprite);
                car1.setBox(b);
            }
            // 当前在站1储备位
            if (Float.compare(car1.getX(), Destination.station1StoragePosition) == 0) {
                Area area = workStationList.get(0).getStorageArea();
                if (area.getBox() == null) {
                    Log.e(TAG, "updateCar1_v2: 小车1在站1储备位由回钩到出钩, 此处应该有箱子, 但没有");
                }
                car1.setBox(area.getBox());
                area.setBox(null);
            }
            // 当前在站1加工位
            if (Float.compare(car1.getX(), Destination.station1ProcessingPosition) == 0) {
                Area area = workStationList.get(0).getProcessingArea();
                if (area.getBox() == null) {
                    Log.e(TAG, "updateCar1_v2: 小车1在站1加工位由回钩到出钩, 此处应该有箱子, 但没有");
                }
                car1.setBox(area.getBox());
                area.setBox(null);
            }
            // 当前在站2储备位
            if (Float.compare(car1.getX(), Destination.station2StoragePosition) == 0) {
                Area area = workStationList.get(1).getStorageArea();
                if (area.getBox() == null) {
                    Log.e(TAG, "updateCar1_v2: 小车1在站2储备位由回钩到出钩, 此处应该有箱子, 但没有");
                }
                car1.setBox(area.getBox());
                area.setBox(null);
            }
            // 当前在站2加工位
            if (Float.compare(car1.getX(), Destination.station2ProcessingPosition) == 0) {
                Area area = workStationList.get(1).getProcessingArea();
                if (area.getBox() == null) {
                    Log.e(TAG, "updateCar1_v2: 小车1在站2加工位由回钩到出钩, 此处应该有箱子, 但没有");
                }
                car1.setBox(area.getBox());
                area.setBox(null);
            }
            // 当前在站3储备位
            if (Float.compare(car1.getX(), Destination.station3StoragePosition) == 0) {
                Area area = workStationList.get(2).getStorageArea();
                if (area.getBox() == null) {
                    Log.e(TAG, "updateCar1_v2: 小车1在站3储备位由回钩到出钩, 此处应该有箱子, 但没有");
                }
                car1.setBox(area.getBox());
                area.setBox(null);
            }
            // 当前在站3加工位
            if (Float.compare(car1.getX(), Destination.station3ProcessingPosition) == 0) {
                Area area = workStationList.get(2).getProcessingArea();
                if (area.getBox() == null) {
                    Log.e(TAG, "updateCar1_v2: 小车1在站3加工位由回钩到出钩, 此处应该有箱子, 但没有");
                }
                car1.setBox(area.getBox());
                area.setBox(null);
            }
        }

    }
    private void update_car1_hook_in () {
        // 小车1放下箱子
        if (previousState[Coil.car1HookOut] && currentState[Coil.car1HookIn]) {
            if (car1.getBox() == null) {
                Log.e(TAG, "updateCar1_v2: 小车1由出钩到回钩, 应该有箱子, 但没有");
            }
            // 小车1在站1储备位放下箱子
            if (Float.compare(car1.getX(), Destination.station1StoragePosition) == 0) {
                Area area = workStationList.get(0).getStorageArea();
                if (area.getBox() != null) {
                    Log.e(TAG, "updateCar1_v2: 小车1在站1储备位放下箱子,该位置目前应该没有箱子, 但有");
                }
                area.setBox(car1.getBox());
                car1.setBox(null);
                area.getBox().setStatus(Constants.BOX_RISING);
            }
            // 小车1在站1加工位放下箱子
            if (Float.compare(car1.getX(), Destination.station1ProcessingPosition) == 0) {
                Area area = workStationList.get(0).getProcessingArea();
                if (area.getBox() != null) {
                    Log.e(TAG, "updateCar1_v2: 小车1在站1加工位放下箱子,该位置目前应该没有箱子, 但有");
                }
                area.setBox(car1.getBox());
                car1.setBox(null);
                area.getBox().setStatus(Constants.BOX_RISING);
            }
            // 小车1在站2储备位放下箱子
            if (Float.compare(car1.getX(), Destination.station2StoragePosition) == 0) {
                Area area = workStationList.get(1).getStorageArea();
                if (area.getBox() != null) {
                    Log.e(TAG, "updateCar1_v2: 小车1在站2储备位放下箱子,该位置目前应该没有箱子, 但有");
                }
                area.setBox(car1.getBox());
                car1.setBox(null);
            }
            // 小车1在站2加工位放下箱子
            if (Float.compare(car1.getX(), Destination.station2ProcessingPosition) == 0) {
                Area area = workStationList.get(1).getProcessingArea();
                if (area.getBox() != null) {
                    Log.e(TAG, "updateCar1_v2: 小车1在站2加工位放下箱子,该位置目前应该没有箱子, 但有");
                }
                area.setBox(car1.getBox());
                car1.setBox(null);
            }
            // 小车1在站3储备位放下箱子
            if (Float.compare(car1.getX(), Destination.station3StoragePosition) == 0) {
                Area area = workStationList.get(2).getStorageArea();
                if (area.getBox() != null) {
                    Log.e(TAG, "updateCar1_v2: 小车1在站3储备位放下箱子,该位置目前应该没有箱子, 但有");
                }
                area.setBox(car1.getBox());
                car1.setBox(null);
            }
            // 小车1在站3加工位放下箱子
            if (Float.compare(car1.getX(), Destination.station3ProcessingPosition) == 0) {
                Area area = workStationList.get(2).getProcessingArea();
                if (area.getBox() != null) {
                    Log.e(TAG, "updateCar1_v2: 小车1在站3加工位放下箱子,该位置目前应该没有箱子, 但有");
                }
                area.setBox(car1.getBox());
                car1.setBox(null);
            }
        }
    }

    // 同步小车2
    // 捕捉到小车2到达某个位置时, 完成同步
    private void syncCar2_v3 () {
        sync_car2_hook_in();
        sync_car2_hook_out();

        // 小车2在站3加工位
        if (currentState[Coil.car2AtStation3ProcessingPosition]) {
            car2.setMatch(true);
            car2.setPos(Destination.station3ProcessingPosition);
        }
        // 小车2在站3完成位
        if (currentState[Coil.car2AtStation3CompletionPosition]) {
            car2.setMatch(true);
            car2.setPos(Destination.station3CompletionPosition);
        }
        // 小车2在站4储备位
        if (currentState[Coil.car2AtStation4StoragePosition]) {
            car2.setMatch(true);
            car2.setPos(Destination.station4StoragePosition);
        }
        // 小车2在站4加工位
        if (currentState[Coil.car2AtStation4ProcessingPosition]) {
            car2.setMatch(true);
            car2.setPos(Destination.station4ProcessingPosition);
        }
        // 小车2在站4完成位
        if (currentState[Coil.car2AtStation4CompletionPosition]) {
            car2.setMatch(true);
            car2.setPos(Destination.station4CompletionPosition);
        }
        // 小车2在站5储备位
        if (currentState[Coil.car2AtStation5StoragePosition]) {
            car2.setMatch(true);
            car2.setPos(Destination.station5StoragePosition);
        }
        // 小车2在站5加工位
        if (currentState[Coil.car2AtStation5ProcessingPosition]) {
            car2.setMatch(true);
            car2.setPos(Destination.station5ProcessingPosition);
        }
        // 小车2在下料位
        if (currentState[Coil.car2AtEndPosition]) {
            car2.setMatch(true);
            car2.setPos(Destination.finalPosition);
        }
    }
    // 根据获取到的状态更新小车2的速度大小,方向和目的地
    // 先根据挡板的位置判断, 如果没有挡板升起, 根据到位信号判断
    private void updateCar2_v3 () {
        boolean ok = false;

        update_car2_hook_out();
        update_car2_hook_in();

        // 站3加工位挡板升起时, 小车2要不要朝这里移动呢?
        // 不需要
        // 前三个站于小车2无关
        if (currentState[Coil.station3ProcessingPositionBlocked]) {
        }
        // 站4储备位
        if (currentState[Coil.station4StoragePositionBlocked]) {
            ok = true;
            _updateSpeedAndDirection(2, Destination.station4StoragePosition);
        }
        // 站4加工位
        if (currentState[Coil.station4ProcessingPositionBlocked]) {
            ok = true;
            _updateSpeedAndDirection(2, Destination.station4ProcessingPosition);
        }
        // 站5储备位
        if (currentState[Coil.station5StoragePositionBlocked]) {
            ok = true;
            _updateSpeedAndDirection(2, Destination.station5StoragePosition);
        }
        // 站5加工位
        if (currentState[Coil.station5ProcessingPositionBlocked]) {
            ok = true;
            _updateSpeedAndDirection(2, Destination.station5ProcessingPosition);
        }

        // 没有挡板升起, 用到位信号
        // 可能出现的问题是: 小车使用到位信号移动的过程中, 某处有挡板升起, 此时小车可能不会到达
        // 到位信号指定的位置, 直接去挡板升起的位置. 如果挡板升起位置跟到位信号都位于小车的左边或者右边,
        // 那么就没有问题, 否则的话, 动画效果会不理想.
        if (!ok) {
            if (currentState[Coil.car2AtStation3ProcessingPosition]) {
                _updateSpeedAndDirection(2, Destination.station3ProcessingPosition);
            }
            if (currentState[Coil.car2AtStation3CompletionPosition]) {
                _updateSpeedAndDirection(2, Destination.station3CompletionPosition);
            }
            if (currentState[Coil.car2AtStation4StoragePosition]) {
                _updateSpeedAndDirection(2, Destination.station4StoragePosition);
            }
            if (currentState[Coil.car2AtStation4ProcessingPosition]) {
                _updateSpeedAndDirection(2, Destination.station4ProcessingPosition);
            }
            if (currentState[Coil.car2AtStation4CompletionPosition]) {
                _updateSpeedAndDirection(2, Destination.station4CompletionPosition);
            }
            if (currentState[Coil.car2AtStation5StoragePosition]) {
                _updateSpeedAndDirection(2, Destination.station5StoragePosition);
            }
            if (currentState[Coil.car2AtStation5ProcessingPosition]) {
                _updateSpeedAndDirection(2, Destination.station5ProcessingPosition);
            }
            if (currentState[Coil.car2AtEndPosition]) {
                _updateSpeedAndDirection(2, Destination.finalPosition);
            }
        }
    }

    private void syncS1 () {
        // 站1储备位
        // 站1储备位有升起的箱子
        if (currentState[Coil.station1StoragePositionUp]) {
            Area area = workStationList.get(0).getStorageArea();
            updateArea(area, Constants.BOX_RISED);
        }
        // 站1储备位有降下的箱子
        if (currentState[Coil.station1StoragePositionDown]) {
            if (currentState[Coil.station1StoragePositionHasBox]) {
                Area area = workStationList.get(0).getStorageArea();
                updateArea(area, Constants.BOX_DECLINED);
            }
        }
        // 站1加工位
        // 站1加工位有升起的箱子
        if (currentState[Coil.station1ProcessingPositionUp]) {
            Area area = workStationList.get(0).getProcessingArea();
            updateArea(area, Constants.BOX_RISED);
        }
        // 站1加工位有降下的箱子
        if (currentState[Coil.station1ProcessingPositionDown]) {
            if (currentState[Coil.station1ProcessingPositionHasBox]) {
                Area area = workStationList.get(0).getProcessingArea();
                updateArea(area, Constants.BOX_DECLINED);
            }
        }
    }
    private void updateS1 () {
        updateStation1_v2();
    }
    private void syncS2 () {
        // 站2储备位
        // 站2储备位有升起的箱子
        if (currentState[Coil.station2StoragePositionUp]) {
            Area area = workStationList.get(1).getStorageArea();
            updateArea(area, Constants.BOX_RISED);
        }
        // 站2储备位有降下的箱子
        if (currentState[Coil.station2StoragePositionDown]) {
            if (currentState[Coil.station2StoragePositionHasBox]) {
                Area area = workStationList.get(1).getStorageArea();
                updateArea(area, Constants.BOX_DECLINED);
            }
        }
        // 站2加工位
        // 站2加工位有升起的箱子
        if (currentState[Coil.station2ProcessingPositionUp]) {
            Area area = workStationList.get(1).getProcessingArea();
            updateArea(area, Constants.BOX_RISED);
        }
        // 站2加工位有降下的箱子
        if (currentState[Coil.station2ProcessingPositionDown]) {
            if (currentState[Coil.station2ProcessingPositionHasBox]) {
                Area area = workStationList.get(1).getProcessingArea();
                updateArea(area, Constants.BOX_DECLINED);
            }
        }
    }
    private void updateS2 () {
        updateStation2_v2();
    }
    private void syncS3 () {
                // 站3储备位
        // 站3储备位有升起的箱子
        if (currentState[Coil.station3StoragePositionUp]) {
            Area area = workStationList.get(2).getStorageArea();
            updateArea(area, Constants.BOX_RISED);
        }
        // 站3储备位有降下的箱子
        if (currentState[Coil.station3StoragePositionDown]) {
            if (currentState[Coil.station3StoragePositionHasBox]) {
                Area area = workStationList.get(2).getStorageArea();
                updateArea(area, Constants.BOX_DECLINED);
            }
        }
        // 站3加工位
        // 站3加工位有升起的箱子
        if (currentState[Coil.station3ProcessingPositionUp]) {
            Area area = workStationList.get(2).getProcessingArea();
            updateArea(area, Constants.BOX_RISED);
        }
        // 站3加工位有降下的箱子
        if (currentState[Coil.station3ProcessingPositionDown]) {
            if (currentState[Coil.station3ProcessingPositionHasBox]) {
                Area area = workStationList.get(2).getProcessingArea();
                updateArea(area, Constants.BOX_DECLINED);
            }
        }
    }
    private void updateS3 () {
        updateStation3_v2();
    }
    private void syncS4 () {
        // 站4储备位
        // 站4储备位有升起的箱子
        if (currentState[Coil.station4StoragePositionUp]) {
            Area area = workStationList.get(3).getStorageArea();
            updateArea(area, Constants.BOX_RISED);
        }
        // 站4储备位有降下的箱子
        if (currentState[Coil.station4StoragePositionDown]) {
            if (currentState[Coil.station4StoragePositionHasBox]) {
                Area area = workStationList.get(3).getStorageArea();
                updateArea(area, Constants.BOX_DECLINED);
            }
        }
        // 站4加工位
        // 站4加工位有升起的箱子
        if (currentState[Coil.station4ProcessingPositionUp]) {
            Area area = workStationList.get(3).getProcessingArea();
            updateArea(area, Constants.BOX_RISED);
        }
        // 站4加工位有降下的箱子
        if (currentState[Coil.station4ProcessingPositionDown]) {
            if (currentState[Coil.station4ProcessingPositionHasBox]) {
                Area area = workStationList.get(3).getProcessingArea();
                updateArea(area, Constants.BOX_DECLINED);
            }
        }
    }
    private void updateS4 () {
        updateStation4_v2();
    }
    private void syncS5 () {
        // 站5储备位
        // 站5储备位有升起的箱子
        if (currentState[Coil.station5StoragePositionUp]) {
            Area area = workStationList.get(4).getStorageArea();
            updateArea(area, Constants.BOX_RISED);
        }
        // 站5储备位有降下的箱子
        if (currentState[Coil.station5StoragePositionDown]) {
            if (currentState[Coil.station5StoragePositionHasBox]) {
                Area area = workStationList.get(4).getProcessingArea();
                updateArea(area, Constants.BOX_DECLINED);
            }
        }

        // 站5加工位
        // 站5加工位有升起的箱子
        if (currentState[Coil.station5ProcessingPositionUp]) {
            Area area = workStationList.get(4).getProcessingArea();
            updateArea(area, Constants.BOX_RISED);
        }
        // 站5加工位有降下的箱子
        if (currentState[Coil.station5ProcessingPositionDown]) {
            if (currentState[Coil.station5ProcessingPositionHasBox]) {
                Area area = workStationList.get(4).getProcessingArea();
                updateArea(area, Constants.BOX_DECLINED);
            }
        }
    }
    private void updateS5 () {
        updateStation5_v2();
    }

    private void syncHand1_v3 () {
    }
    private void updateHand1_v3 () {
    }

    private void syncHand2_v3 () {
    }
    private void updateHand2_v3 () {
    }

    private void syncHand3_v3 () {
    }
    private void updateHand3_v3 () {
    }

    private void syncHand4_v3 () {
        Log.e(TAG, "syncHand4_v3: syncHand4");
        float precision = 5;
        // 左移
        // 第二次以及之后
        Hand hand = workStationList.get(3).getHand();
        if (currentState[Coil.hand4ToMiddle1] &&
                currentState[Coil.hand4ToMiddle2] &&
                !currentState[Coil.hand4ToMiddle3]) {
            hand.updatePosition(Constants.HAND_MIDDLE_TOP);
            hand.setMatch(true);
        }
        // 左移
        // 第一次
        if (currentState[Coil.hand4FirstTimeToMiddle1] &&
                currentState[Coil.hand4FirstTimeToMiddle2] &&
                !currentState[Coil.hand4FirstTimeToMiddle3]) {
            hand.updatePosition(Constants.HAND_MIDDLE_TOP);
            hand.setMatch(true);
        }
        // 右移
        // 第二次和之后
        if (currentState[Coil.hand4ToRight1] &&
                currentState[Coil.hand4ToRight2]) {
            hand.updatePosition(Constants.HAND_RIGHT_TOP);
            hand.setMatch(true);
        }
        // 第一次
        if (currentState[Coil.hand4FirstTimeToRight1] &&
                currentState[Coil.hand4FirstTimeToRight2]) {
            hand.updatePosition(Constants.HAND_RIGHT_TOP);
            hand.setMatch(true);
        }
    }
    private void syncHand4_v4 () {
        Log.e(TAG, "syncHand4_v4: enter");
        Hand hand = workStationList.get(3).getHand();
        if (currentState[Coil.newHand4ToRight]) {
            hand.updatePosition(Constants.HAND_MIDDLE_TOP);
            Constants.previousHand4Position = HandPosition.Right;
            hand.setMatch(true);
        }
        if (currentState[Coil.newHand4ToMiddle]) {
            hand.setMatch(true);
            Constants.previousHand4Position = HandPosition.Middle;
            hand.updatePosition(Constants.HAND_RIGHT_TOP);
        }
    }
    private void syncHand2_v4 () {
        Log.e(TAG, "syncHand2_v4: enter");
        Hand hand = workStationList.get(1).getHand();
        if (currentState[Coil.newHand2ToRight]) {
            hand.updatePosition(Constants.HAND_MIDDLE_TOP);
            hand.previousPosition = HandPosition.Right;
            hand.setMatch(true);
        }
        if (currentState[Coil.newHand2ToMiddle]) {
            hand.setMatch(true);
            hand.previousPosition = HandPosition.Middle;
            hand.updatePosition(Constants.HAND_RIGHT_TOP);
        }
    }

    private void updateHand2_v4 () {

        Hand hand = workStationList.get(1).getHand();
        int precision = 10;
//        Log.e(TAG, "updateHand4_v4: middle: " + currentState[Coil.newHand4ToMiddle] + ", initY: " + hand.getInitY() + ", middleY: " + hand.getMiddleY());
//        Log.e(TAG, "updateHand4_v4: right: " + currentState[Coil.newHand4ToRight] + ", initY: " + hand.getInitY() + ", middleY: " + hand.getRightEndY());
//        Log.e(TAG, "updateHand4_v4: pos: " + Constants.previousHand4Position + ", waiting: " + currentState[Coil.waiting] + ", middleY: " + hand.getRightEndY() + ", initY:" + hand.getInitY());
//        Log.e(TAG, "updateHand2_v4: toright waiting: " + currentState[Coil.hand2Waiting] + ",pos:" + hand.getPreviousPosition());
        Log.e(TAG, "updateHand2_v4: waiting: " + currentState[Coil.hand2Waiting] + ",middle:" + currentState[Coil.newHand2ToMiddle] + ", right:" + currentState[Coil.newHand2ToRight] + ",pos:" + hand.getPreviousPosition());
        if (currentState[Coil.hand2Waiting] &&
                hand.getPreviousPosition() == HandPosition.Middle &&
                !currentState[Coil.newHand2ToMiddle]
//                Float.compare(hand.getInitY(), hand.getMiddleY()) == 0 &&
//                !currentState[Coil.newHand4ToMiddle]
                ) {
            hand.setStatus(Constants.handRightShifting);
        }
        if (currentState[Coil.hand2Waiting] &&
                hand.getPreviousPosition() == HandPosition.Right &&
                !currentState[Coil.newHand2ToRight]
//                Float.compare(hand.getInitY(), hand.getRightEndY()) == 0
//                !currentState[Coil.newHand4ToMiddle]
                ) {
            hand.setStatus(Constants.handLeftShifting);
        }
        if (currentState[Coil.newHand2ToMiddle]) {
//            if (Float.compare(hand.getY(), hand.getMiddleY()) != 0) {
                hand.updatePosition(Constants.HAND_MIDDLE_TOP);
//            }
        }

        if (currentState[Coil.newHand2ToRight]) {
//            if (Float.compare(hand.getY(), hand.getRightEndY()) != 0) {
                hand.updatePosition(Constants.HAND_RIGHT_TOP);
//            }
        }
//        if (!currentState[Coil.newHand4ToRight] && currentState[Coil.waiting] && Float.compare(hand.getInitY(), hand.getRightEndY()) == 0) {
//            if (Float.compare(hand.getInitY(), hand.getMiddleY()) != 0) {
//                hand.setStatus(Constants.handLeftShifting);
//            }
//        } else if (!currentState[Coil.newHand4ToMiddle] && currentState[Coil.waiting] && Float.compare(hand.getInitY(), hand.getMiddleY()) == 0) {
//            if (Float.compare(hand.getInitY(), hand.getRightEndY()) != 0) {
//                hand.setStatus(Constants.handRightShifting);
//            }
//        }
    }
    private void updateHand4_v4 () {

        Hand hand = workStationList.get(3).getHand();
        int precision = 10;
//        Log.e(TAG, "updateHand4_v4: middle: " + currentState[Coil.newHand4ToMiddle] + ", initY: " + hand.getInitY() + ", middleY: " + hand.getMiddleY());
//        Log.e(TAG, "updateHand4_v4: right: " + currentState[Coil.newHand4ToRight] + ", initY: " + hand.getInitY() + ", middleY: " + hand.getRightEndY());
        Log.e(TAG, "updateHand4_v4: pos: " + Constants.previousHand4Position + ", waiting: " + currentState[Coil.waiting] + ", middleY: " + hand.getRightEndY() + ", initY:" + hand.getInitY());
        if (currentState[Coil.waiting] &&
                Constants.previousHand4Position == HandPosition.Middle
//                Float.compare(hand.getInitY(), hand.getMiddleY()) == 0 &&
//                !currentState[Coil.newHand4ToMiddle]
                ) {
            hand.setStatus(Constants.handRightShifting);
        }
        if (currentState[Coil.waiting] &&
                Constants.previousHand4Position == HandPosition.Right
                //Float.compare(hand.getInitY(), hand.getRightEndY()) == 0 &&
//                !currentState[Coil.newHand4ToMiddle]
                ) {
            hand.setStatus(Constants.handLeftShifting);
        }
        if (currentState[Coil.newHand4ToMiddle]) {
            if (Float.compare(hand.getY(), hand.getMiddleY()) != 0) {
                hand.updatePosition(Constants.HAND_MIDDLE_TOP);
            }
        }

        if (currentState[Coil.newHand4ToRight]) {
            if (Float.compare(hand.getY(), hand.getRightEndY()) != 0) {
                hand.updatePosition(Constants.HAND_RIGHT_TOP);
            }
        }
//        if (!currentState[Coil.newHand4ToRight] && currentState[Coil.waiting] && Float.compare(hand.getInitY(), hand.getRightEndY()) == 0) {
//            if (Float.compare(hand.getInitY(), hand.getMiddleY()) != 0) {
//                hand.setStatus(Constants.handLeftShifting);
//            }
//        } else if (!currentState[Coil.newHand4ToMiddle] && currentState[Coil.waiting] && Float.compare(hand.getInitY(), hand.getMiddleY()) == 0) {
//            if (Float.compare(hand.getInitY(), hand.getRightEndY()) != 0) {
//                hand.setStatus(Constants.handRightShifting);
//            }
//        }
    }
    private void updateHand4_v3 () {
        float precision = 5;
        // 左移
        // 第二次以及之后
        Log.e(TAG, "updateHand4_v3:enter, middle: " + currentState[Coil.hand4ToMiddle1] + "," + currentState[Coil.hand4ToMiddle2] + "," + currentState[Coil.hand4ToMiddle3]);
        Log.e(TAG, "updateHand4_v3:enter, right: " + currentState[Coil.hand4ToRight1] + "," + currentState[Coil.hand4ToRight2]);
        Hand hand = workStationList.get(3).getHand();
        if (currentState[Coil.hand4ToMiddle1] &&
                currentState[Coil.hand4ToMiddle2] &&
                !currentState[Coil.hand4ToMiddle3]) {
            Log.e(TAG, "updateHand4_v3: to middle");
            // 首先应该判断当前机械手是在左边还是在右边
            // 然后才决定机械手是左移还是右移
            // 目前默认机械手不会在左边
            // todo
            // 加入机械手左移右移的判断
            if (Math.abs(hand.getY() - hand.getMiddleY()) > precision) {
                hand.setStatus(Constants.handLeftShifting);
            }
        }
        // 左移
        // 第一次
        if (currentState[Coil.hand4FirstTimeToMiddle1] &&
                currentState[Coil.hand4FirstTimeToMiddle2] &&
                !currentState[Coil.hand4FirstTimeToMiddle3]) {
            if (Math.abs(hand.getY() - hand.getMiddleY()) > precision) {
                Log.e(TAG, "updateHand4_v3: to middle");
                hand.setStatus(Constants.handLeftShifting);
            }
        }
        // 右移
        // 第二次和之后
        if (currentState[Coil.hand4ToRight1] &&
                currentState[Coil.hand4ToRight2]) {
            if (hand.getStatus() != Constants.handRightShifted && Math.abs(hand.getY() - hand.getRightEndY()) > precision) {
                hand.setStatus(Constants.handRightShifting);
                Log.e(TAG, "updateHand4_v3: to right");
            }
        }
        // 第一次
        if (currentState[Coil.hand4FirstTimeToRight1] &&
                currentState[Coil.hand4FirstTimeToRight2]) {
            if (hand.getStatus() != Constants.handRightShifted && Math.abs(hand.getY() - hand.getRightEndY()) > precision) {
                hand.setStatus(Constants.handRightShifting);
                Log.e(TAG, "updateHand4_v3: to right");
            }
        }
    }

    private void syncHand5_v3 () {
    }
    private void updateHand5_v3 () {
    }

    // v2
    private void run3() {
        Timer timer = new Timer();
        int delay = 1000;
        int period = 100;
        currentState = new boolean[500];
        previousState = new boolean[500];
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ModbusReq.getInstance().readCoil(new OnRequestBack<boolean[]>() {
                    @Override
                    public void onSuccess(boolean[] booleans) {
                        Log.e(TAG, "readCoil onSuccess " + Arrays.toString(booleans));
//                        writeToDB(booleans);
                         currentState = booleans;
                        if (previousState == null) {
                            System.arraycopy(currentState, 0, previousState, 0, 500);
                        }
                        Log.e(TAG, "onSuccess: ca1 to start Position: " + currentState[Coil.car1AtStartPosition] + "," + currentState[Coil.car1AtStartBlockPosition]);
                        Log.e(TAG, "onSuccess: ca1 to s2 processiong position: " + currentState[Coil.car1AtStation2ProcessingPosition]);
                        Log.e(TAG, "onSuccess: ca1 to s2 processiong position: " + currentState[Coil.car1AtStation2ProcessingPosition]);
                        Log.e(TAG, "onSuccess: hand2 rightShifting:" + currentState[Coil.station2HorizontallyToRightPutPosition]);
                        Log.e(TAG, "onSuccess: hand4 rightShifting:" + currentState[Coil.station4HorizontallyToRightPutPosition]);

//                            Hand hand1 = workStationList.get(0).getHand();
                            Hand hand2 = workStationList.get(1).getHand();
//                            Hand hand3 = workStationList.get(2).getHand();
                            Hand hand4 = workStationList.get(3).getHand();
//                            Hand hand5 = workStationList.get(4).getHand();
//                            if (hand1.isMatch()) {
//                                updateHand1();
//                            } else {
//                                syncHand1();
//                            }

                            if (hand2.isMatch()) {
                                updateHand2();
                            } else {
                                syncHand2();
                            }

//                            if (hand3.isMatch()) {
//                                updateHand3();
//                            } else {
//                                syncHand3();
//                            }

                            if (hand4.isMatch()) {
                                updateHand4();
                            } else {
                                syncHand4();
                            }
//
//                            if (hand5.isMatch()) {
//                                updateHand5();
//                            } else {
//                                syncHand5();
//                            }

                            if (car1.isMatch()) {
                                updateCar1_v2();
                                updateStation1_v2();
                                updateStation2_v2();
                                updateStation3_v2();
                            } else {
                                syncCar1();
                            }
                            if (car2.isMatch()) {
                                updateCar2_v2();
                                updateStation4_v2();
                                updateStation5_v2();
                            } else {
                                syncCar2();
                            }
                        System.arraycopy(currentState, 0, previousState, 0, 500);
                    }
                    @Override
                    public void onFailed(String msg) {
                        Log.e(TAG, "readCoil onFailed " + msg);
                    }
                }, 1, Constants.CoilStart, Constants.CoilLen);
            }
        },delay, period);

    }
    // 用于实际场景的动画效果
    // 在一定的时间间隔内读取一次生产线的状态, 根据状态的变化做出相应的动画
    // 目前的时间间隔是 0.2 秒
    public void run2() {
        final Handler handler = new Handler();
        final int delay = 200;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ModbusReq.getInstance().readCoil(new OnRequestBack<boolean[]>() {
                    @Override
                    public void onSuccess(boolean[] booleans) {
                        Log.d(TAG, "readCoil onSuccess " + Arrays.toString(booleans));
//                            updateLight(booleans);
//                            updateHand(booleans);
                        currentState = booleans;
                        if (previousState == null) {
                             System.arraycopy(currentState, 0, previousState, 0, 10000);
                        } else {
                            Hand hand1 = workStationList.get(0).getHand();
                            Hand hand2 = workStationList.get(1).getHand();
                            Hand hand3 = workStationList.get(2).getHand();
                            Hand hand4 = workStationList.get(3).getHand();
                            Hand hand5 = workStationList.get(4).getHand();
                            if (hand1.isMatch()) {
                                updateHand1();
                            } else {
                                syncHand1();
                            }

                            if (hand2.isMatch()) {
                                updateHand2();
                            } else {
                                syncHand2();
                            }

                            if (hand3.isMatch()) {
                                updateHand3();
                            } else {
                                syncHand3();
                            }

                            if (hand4.isMatch()) {
                                updateHand4();
                            } else {
                                syncHand4();
                            }

                            if (hand5.isMatch()) {
                                updateHand5();
                            } else {
                                syncHand5();
                            }

                            if (car1.isMatch()) {
                                updateCar1_v2();
                                updateStation1_v2();
                                updateStation2_v2();
                                updateStation3_v2();
                            } else {
                                syncCar1();
                            }
                            if (car2.isMatch()) {
                                updateCar2_v2();
                                updateStation4_v2();
                                updateStation5_v2();
                            } else {
                                syncCar2();
                            }
                        }
                        System.arraycopy(currentState, 0, previousState, 0, 10000);
                    }
                    @Override
                    public void onFailed(String msg) {
                        Log.e(TAG, "readCoil onFailed " + msg);
                    }
                }, 1, 1, 100);
            }
        }, delay);
    }
    // 辅助函数
    private void updateArea(Area area, int status) {
        if (area.getBox() == null) {
            // 生成与指定信号一致的箱子
            Box box = generateBox(area.x, car1.getY(), status);
            area.setBox(box);
        } else {
            if (area.getBox().getStatus() != status) {
                area.getBox().setStatus(status);
            }
        }
    }
    // 辅助函数
    // 生成指定坐标和状态的箱子
    private Box generateBox(float x, float y, int status) {
        float boxWidth = car1.getWidth();
        float boxHeight = car1.getHeight() - 20;
        Texture texture = new Texture(getApplicationContext(), R.drawable.box);
        Sprite sprite = new Sprite((int) boxWidth, (int) boxHeight);
        sprite.setPivot(0.5f, 0.5f);
        sprite.setPos(x, y);
        sprite.setTexture(texture);
        Box b = new Box(x, y, boxWidth, boxHeight, texture, sprite);
        b.changeSize(status);
        b.render(renderPassSprite);
        return b;
    }
    // 小车1同步中
    private void syncCar1() {
        Log.e(TAG, "syncCar1: syncing");
        // 小车1是否有箱子
        // 小车1处于出钩状态, 如果此时小车1没有箱子, 生成一个箱子, 放上去
        if (currentState[Coil.car1HookOut]) {
            if (car1.getBox() == null) {
                Box box = generateBox(car1.getX(), car1.getY(), Constants.BOX_DECLINED);
                car1.setBox(box);
            }
        }
        // 小车1处于回钩状态, 如果此时小车1有箱子, 放下该箱子
        if (currentState[Coil.car1HookIn]) {
            if (car1.getBox() != null) {
                car1.setBox(null);
            }
        }
        // 获取到小车1回到起始位的信号, 可以开始同步
        if (currentState[Coil.car1AtStartBlockPosition] || currentState[Coil.car1AtStartPosition]) {
            car1.setMatch(true);
        }

        // 获取到小车1开始从驱动到站1加工位/站1储备位/站1完成位, 可以开始同步
        if (
                currentState[Coil.car1AtStation2StoragePosition]
                        || currentState[Coil.car1AtStation2ProcessingPosition]
                        || currentState[Coil.car1AtStation2CompletionPosition]
                ) {
            car1.setMatch(true);
        }

        // 与小车1动作有关的工站状态
        // 站1储料位有料
        // 站1储料位料盒已经升起
        if (currentState[Coil.station1StoragePositionUp]) {
            Area area = workStationList.get(0).getStorageArea();
            if (area.getBox() == null) {
                // 如果当前位置没有箱子, 生成一个升起状态的箱子, 放在此处
                Box box = generateBox(area.x, car1.getY(), Constants.BOX_RISED);
                area.setBox(box);
            } else {
                // 如果当前位置已经有箱子
                // 如果箱子的状态是降下的
                // 修改箱子的状态为升起
                if (area.getBox().getStatus() != Constants.BOX_RISED) {
                    area.getBox().setStatus(Constants.BOX_RISED);
                }
            }
        }
        // 站1储料位料盒已经降下
        if (currentState[Coil.station1StoragePositionDown]) {
            Area area = workStationList.get(0).getStorageArea();
            // 判断是否有箱子
            if (currentState[Coil.station1StoragePositionHasBox]) {
                if (area.getBox() == null) {
                    // 如果当前位置没有箱子, 生成一个下降状态的箱子, 放在此处
                    Box box = generateBox(area.x, car1.getY(), Constants.BOX_DECLINED);
                    area.setBox(box);
                } else {
                    // 如果当前位置已经有箱子
                    // 如果箱子的状态是升起的
                    // 修改箱子的状态为降下的
                    if (area.getBox().getStatus() != Constants.BOX_DECLINED) {
                        area.getBox().setStatus(Constants.BOX_DECLINED);
                    }
                }
            }
        }

        // 站1加工位有料
        // 站1加工位料盒已经升起
        if (currentState[Coil.station1ProcessingPositionUp]) {
            Area area = workStationList.get(0).getProcessingArea();
            if (area.getBox() == null) {
                // 如果当前位置没有箱子, 生成一个升起状态的箱子, 放在此处
                Box box = generateBox(area.x, car1.getY(), Constants.BOX_RISED);
                area.setBox(box);
            } else {
                // 如果当前位置已经有箱子
                // 如果箱子的状态是升起的
                // 修改箱子的状态为降下的
                if (area.getBox().getStatus() != Constants.BOX_RISED) {
                    area.getBox().setStatus(Constants.BOX_RISED);
                }
            }
        }
        // 站1加工位料盒已经降下
        if (currentState[Coil.station1ProcessingPositionDown]) {
            if (currentState[Coil.station1ProcessingPositionHasBox]) {
                Area area = workStationList.get(0).getProcessingArea();
                if (area.getBox() == null) {
                    // 如果当前位置没有箱子, 生成一个下降状态的箱子, 放在此处
                    Box box = generateBox(area.x, car1.getY(), Constants.BOX_DECLINED);
                    area.setBox(box);
                } else {
                    // 如果当前位置已经有箱子
                    // 如果箱子的状态是升起的
                    // 修改箱子的状态为降下的
                    if (area.getBox().getStatus() != Constants.BOX_DECLINED) {
                        area.getBox().setStatus(Constants.BOX_DECLINED);
                    }
                }
            }
        }

        // 站2储备位有料
        // 站2储备位料盒已经升起

        if (currentState[Coil.station2StoragePositionUp]) {
            Area area = workStationList.get(1).getStorageArea();
            if (area.getBox() == null) {
                // 如果当前位置没有箱子, 生成一个升起状态的箱子, 放在此处
                Box box = generateBox(area.x, car1.getY(), Constants.BOX_RISED);
                area.setBox(box);
            } else {
                // 如果当前位置已经有箱子
                // 如果箱子的状态是降下的
                // 修改箱子的状态为升起
                if (area.getBox().getStatus() != Constants.BOX_RISED) {
                    area.getBox().setStatus(Constants.BOX_RISED);
                }
            }
        }
        // 站2储备位料盒已经降下
        if (currentState[Coil.station2StoragePositionDown]) {
            if (currentState[Coil.station2StoragePositionHasBox]) {
                Area area = workStationList.get(1).getStorageArea();
                if (area.getBox() == null) {
                    // 如果当前位置没有箱子, 生成一个下降状态的箱子, 放在此处
                    Box box = generateBox(area.x, car1.getY(), Constants.BOX_DECLINED);
                    area.setBox(box);
                } else {
                    // 如果当前位置已经有箱子
                        // 如果箱子的状态是升起的
                        // 修改箱子的状态为降下的
                    if (area.getBox().getStatus() != Constants.BOX_DECLINED) {
                        area.getBox().setStatus(Constants.BOX_DECLINED);
                    }
                }
            }
        }

        // 站2加工位有料
        // 站2加工位料盒已经升起
        if (currentState[Coil.station2ProcessingPositionUp]) {
            Area area = workStationList.get(1).getProcessingArea();
            if (area.getBox() == null) {
                // 如果当前位置没有箱子, 生成一个下降状态的箱子, 放在此处
                Box box = generateBox(area.x, car1.getY(), Constants.BOX_RISED);
                area.setBox(box);
            } else {
                // 如果当前位置已经有箱子
                // 如果箱子的状态是升起的
                // 修改箱子的状态为降下的
                if (area.getBox().getStatus() != Constants.BOX_RISED) {
                    area.getBox().setStatus(Constants.BOX_RISED);
                }
            }
        }
        // 站2加工位料盒已经降下
        if (currentState[Coil.station2ProcessingPositionDown]) {
            if (currentState[Coil.station2ProcessingPositionHasBox]) {
                Area area = workStationList.get(1).getProcessingArea();
                if (area.getBox() == null) {
                    // 如果当前位置没有箱子, 生成一个下降状态的箱子, 放在此处
                    Box box = generateBox(area.x, car1.getY(), Constants.BOX_DECLINED);
                    area.setBox(box);
                } else {
                    // 如果当前位置已经有箱子
                        // 如果箱子的状态是升起的
                        // 修改箱子的状态为降下的
                    if (area.getBox().getStatus() != Constants.BOX_DECLINED) {
                        area.getBox().setStatus(Constants.BOX_DECLINED);
                    }
                }
            }
        }
        // 站3储备位有料
        // 站3储备位料盒已经升起
        if (currentState[Coil.station3StoragePositionUp]) {
            Area area = workStationList.get(2).getStorageArea();
            updateArea(area, Constants.BOX_RISED);
        }
        // 站3储备位料盒已经降下
        if (currentState[Coil.station3StoragePositionDown]) {
            if (currentState[Coil.station3StoragePositionHasBox]) {
                Area area = workStationList.get(2).getStorageArea();
                updateArea(area, Constants.BOX_DECLINED);
            }
        }
        // 站3加工位有料
        // 站3加工位料盒已经升起
        if (currentState[Coil.station3ProcessingPositionUp]) {
            Area area = workStationList.get(2).getProcessingArea();
            updateArea(area, Constants.BOX_RISED);
        }
        // 站3加工位料盒已经降下
        if (currentState[Coil.station3ProcessingPositionDown]) {
            if (currentState[Coil.station3ProcessingPositionHasBox]) {
                Area area = workStationList.get(2).getProcessingArea();
                updateArea(area, Constants.BOX_DECLINED);
            }
        }
    }
    // 小车2同步中
    private void syncCar2() {
        Log.e(TAG, "syncCar2: syncing car2, index: " + fake_index);

        // 小车2回到起始位(站3加工位), 或者前往站4时, 开始同步
        if (currentState[Coil.car2AtStation3ProcessingPosition] ||
                currentState[Coil.car2AtStation4StoragePosition] ||
                currentState[Coil.car2AtStation4ProcessingPosition] ||
                currentState[Coil.car2AtStation4CompletionPosition]
                ) {
            car2.setMatch(true);
        }

        // 小车2是否有箱子
        // 小车2处于出钩状态, 应该有箱子, 如果没有, 生成一个箱子放上去
        if (currentState[Coil.car2HookOut]) {
            if (car2.getBox() == null) {
                Box box = generateBox(car2.getX(), car2.getY(), Constants.BOX_DECLINED);
                car2.setBox(box);
            }
        }

        // 小车2处于回钩状体, 应该没有箱子, 如果有, 将改箱子销毁
        if (currentState[Coil.car2HookIn]) {
            if (car2.getBox() != null) {
                car2.getBox().getSprite().releaseResources();
                car2.setBox(null);
            }
        }

        // 站4储备位
        // 站4储备位有升起的箱子
        if (currentState[Coil.station4StoragePositionUp]) {
            Area area = workStationList.get(3).getStorageArea();
            updateArea(area, Constants.BOX_RISED);
        }
        // 站4储备位有降下的箱子
        if (currentState[Coil.station4StoragePositionDown]) {
            if (currentState[Coil.station4StoragePositionHasBox]) {
                Area area = workStationList.get(3).getStorageArea();
                updateArea(area, Constants.BOX_DECLINED);
            }
        }

        // 站4加工位
        // 站4加工位有升起的箱子
        if (currentState[Coil.station4ProcessingPositionUp]) {
            Area area = workStationList.get(3).getProcessingArea();
            updateArea(area, Constants.BOX_RISED);
        }
        // 站4加工位有降下的箱子
        if (currentState[Coil.station4ProcessingPositionDown]) {
            if (currentState[Coil.station4ProcessingPositionHasBox]) {
                Area area = workStationList.get(3).getProcessingArea();
                updateArea(area, Constants.BOX_DECLINED);
            }
        }

        // 站5储备位
        // 站5储备位有升起的箱子
        if (currentState[Coil.station5StoragePositionUp]) {
            Area area = workStationList.get(4).getStorageArea();
            updateArea(area, Constants.BOX_RISED);
        }
        // 站5储备位有降下的箱子
        if (currentState[Coil.station5StoragePositionDown]) {
            if (currentState[Coil.station5StoragePositionHasBox]) {
                Area area = workStationList.get(4).getProcessingArea();
                updateArea(area, Constants.BOX_DECLINED);
            }
        }

        // 站5加工位
        // 站5加工位有升起的箱子
        if (currentState[Coil.station5ProcessingPositionUp]) {
            Area area = workStationList.get(4).getProcessingArea();
            updateArea(area, Constants.BOX_RISED);
        }
        // 站5加工位有降下的箱子
        if (currentState[Coil.station5ProcessingPositionDown]) {
            if (currentState[Coil.station5ProcessingPositionHasBox]) {
                Area area = workStationList.get(4).getProcessingArea();
                updateArea(area, Constants.BOX_DECLINED);
            }
        }
    }
    // 以下是与执行v2动画相关的函数
    // 根据读取到的状态, 修改物体的速度大小,方向和目的地, 物体的移动在另一个线程执行
    // 小车1同步中
    private void updateCar1_v2() {
        Log.e(TAG, "updateCar1_v2: running, index: " + fake_index);

        float precision = 1e-6f;
        int speed = 100;

        // 小车1获取箱子
        if (previousState[Coil.car1HookIn] && currentState[Coil.car1HookOut]) {
            // 小车1此时应该没有箱子
            if (car1.getBox() != null) {
                Log.e(TAG, "updateCar1: 小车1此时应该没有箱子，但有");
            }
            // 当前位置应该有一个箱子
            // 当前在起始位
            if (Float.compare(car1.getX(), Destination.initialPosition) == 0) {
                // 生成新的箱子
                // 将新生成的箱子放在小车上
                float boxX = car1.getX();
                float boxY = car1.getY();
                float boxWidth = car1.getWidth();
                float boxHeight = car1.getHeight() - 20;
                Texture texture = new Texture(getApplicationContext(), R.drawable.box);
                Sprite sprite = new Sprite((int) boxWidth, (int) boxHeight);
                sprite.setPivot(0.5f, 0.5f);
                sprite.setPos(boxX, boxY);
                sprite.setTexture(texture);
                Box b = new Box(boxX, boxY, boxWidth, boxHeight, texture, sprite);
                b.render(renderPassSprite);
                car1.setBox(b);
            }
            // 当前在站1储备位
            if (Float.compare(car1.getX(), Destination.station1StoragePosition) == 0) {
                Area area = workStationList.get(0).getStorageArea();
                if (area.getBox() == null) {
                    Log.e(TAG, "updateCar1_v2: 小车1在站1储备位由回钩到出钩, 此处应该有箱子, 但没有");
                }
                car1.setBox(area.getBox());
                area.setBox(null);
            }
            // 当前在站1加工位
            if (Float.compare(car1.getX(), Destination.station1ProcessingPosition) == 0) {
                Area area = workStationList.get(0).getProcessingArea();
                if (area.getBox() == null) {
                    Log.e(TAG, "updateCar1_v2: 小车1在站1加工位由回钩到出钩, 此处应该有箱子, 但没有");
                }
                car1.setBox(area.getBox());
                area.setBox(null);
            }
            // 当前在站2储备位
            if (Float.compare(car1.getX(), Destination.station2StoragePosition) == 0) {
                Area area = workStationList.get(1).getStorageArea();
                if (area.getBox() == null) {
                    Log.e(TAG, "updateCar1_v2: 小车1在站2储备位由回钩到出钩, 此处应该有箱子, 但没有");
                }
                car1.setBox(area.getBox());
                area.setBox(null);
            }
            // 当前在站2加工位
            if (Float.compare(car1.getX(), Destination.station2ProcessingPosition) == 0) {
                Area area = workStationList.get(1).getProcessingArea();
                if (area.getBox() == null) {
                    Log.e(TAG, "updateCar1_v2: 小车1在站2加工位由回钩到出钩, 此处应该有箱子, 但没有");
                }
                car1.setBox(area.getBox());
                area.setBox(null);
            }
            // 当前在站3储备位
            if (Float.compare(car1.getX(), Destination.station3StoragePosition) == 0) {
                Area area = workStationList.get(2).getStorageArea();
                if (area.getBox() == null) {
                    Log.e(TAG, "updateCar1_v2: 小车1在站3储备位由回钩到出钩, 此处应该有箱子, 但没有");
                }
                car1.setBox(area.getBox());
                area.setBox(null);
            }
            // 当前在站3加工位
            if (Float.compare(car1.getX(), Destination.station3ProcessingPosition) == 0) {
                Area area = workStationList.get(2).getProcessingArea();
                if (area.getBox() == null) {
                    Log.e(TAG, "updateCar1_v2: 小车1在站3加工位由回钩到出钩, 此处应该有箱子, 但没有");
                }
                car1.setBox(area.getBox());
                area.setBox(null);
            }
        }

        // 小车1放下箱子
        if (previousState[Coil.car1HookOut] && currentState[Coil.car1HookIn]) {
            if (car1.getBox() == null) {
                Log.e(TAG, "updateCar1_v2: 小车1由出钩到回钩, 应该有箱子, 但没有");
            }
            // 小车1在站1储备位放下箱子
            if (Float.compare(car1.getX(), Destination.station1StoragePosition) == 0) {
                Area area = workStationList.get(0).getStorageArea();
                if (area.getBox() != null) {
                    Log.e(TAG, "updateCar1_v2: 小车1在站1储备位放下箱子,该位置目前应该没有箱子, 但有");
                }
                area.setBox(car1.getBox());
                car1.setBox(null);
                area.getBox().setStatus(Constants.BOX_RISING);
            }
            // 小车1在站1加工位放下箱子
            if (Float.compare(car1.getX(), Destination.station1ProcessingPosition) == 0) {
                Area area = workStationList.get(0).getProcessingArea();
                if (area.getBox() != null) {
                    Log.e(TAG, "updateCar1_v2: 小车1在站1加工位放下箱子,该位置目前应该没有箱子, 但有");
                }
                area.setBox(car1.getBox());
                car1.setBox(null);
                area.getBox().setStatus(Constants.BOX_RISING);
            }
            // 小车1在站2储备位放下箱子
            if (Float.compare(car1.getX(), Destination.station2StoragePosition) == 0) {
                Area area = workStationList.get(1).getStorageArea();
                if (area.getBox() != null) {
                    Log.e(TAG, "updateCar1_v2: 小车1在站2储备位放下箱子,该位置目前应该没有箱子, 但有");
                }
                area.setBox(car1.getBox());
                car1.setBox(null);
            }
            // 小车1在站2加工位放下箱子
            if (Float.compare(car1.getX(), Destination.station2ProcessingPosition) == 0) {
                Area area = workStationList.get(1).getProcessingArea();
                if (area.getBox() != null) {
                    Log.e(TAG, "updateCar1_v2: 小车1在站2加工位放下箱子,该位置目前应该没有箱子, 但有");
                }
                area.setBox(car1.getBox());
                car1.setBox(null);
            }
            // 小车1在站3储备位放下箱子
            if (Float.compare(car1.getX(), Destination.station3StoragePosition) == 0) {
                Area area = workStationList.get(2).getStorageArea();
                if (area.getBox() != null) {
                    Log.e(TAG, "updateCar1_v2: 小车1在站3储备位放下箱子,该位置目前应该没有箱子, 但有");
                }
                area.setBox(car1.getBox());
                car1.setBox(null);
            }
            // 小车1在站3加工位放下箱子
            if (Float.compare(car1.getX(), Destination.station3ProcessingPosition) == 0) {
                Area area = workStationList.get(2).getProcessingArea();
                if (area.getBox() != null) {
                    Log.e(TAG, "updateCar1_v2: 小车1在站3加工位放下箱子,该位置目前应该没有箱子, 但有");
                }
                area.setBox(car1.getBox());
                car1.setBox(null);
            }
        }
        /*
         // 小车1驱动到起始位
        if (currentState[Coil.car1AtStartBlockPosition] || currentState[Coil.car1AtStartPosition]) {
            if (Math.abs(car1.getX() - Destination.initialPosition) < precision) {
                car1.setSpeed(speed);
                if (car1.getX() < Destination.initialPosition) {
                    car1.setDirection(Constants.RIGHT);
                }
                if (car1.getX() > Destination.initialPosition) {
                    car1.setDirection(Constants.LEFT);
                }
            }
            car1.setDestination(Destination.initialPosition);
            // 小车2在站5储备位获取箱子
            // 小车2在站5加工位获取箱子

        }
        */
        // 小车2放下箱子


        // 小车1驱动到起始位
        if (currentState[Coil.car1AtStartBlockPosition] || currentState[Coil.car1AtStartPosition]) {
            Log.e(TAG, "updateCar1_v2: hello, car1x, " + car1.getX() + ", destination: " + Destination.initialPosition);
            if (Math.abs(car1.getX() - Destination.initialPosition) > precision) {
                car1.setSpeed(speed);
                Log.e(TAG, "updateCar1_v2: car1speed: " + car1.getSpeed());
                if (car1.getX() < Destination.initialPosition) {
                    car1.setDirection(Constants.RIGHT);
                }
                if (car1.getX() > Destination.initialPosition) {
                    car1.setDirection(Constants.LEFT);
                }
            }
            car1.setDestination(Destination.initialPosition);
        }
        // 小车2驱动到下料位
        if (currentState[Coil.car2AtEndPosition]) {
            if (Math.abs(car2.getX() - Destination.finalPosition) > precision) {
                car2.setSpeed(speed);
                if (car2.getX() < Destination.finalPosition) {
                    car2.setDirection(Constants.RIGHT);
                }
                if (car2.getX() > Destination.finalPosition) {
                    car2.setDirection(Constants.LEFT);
                }
            }
            car2.setDestination(Destination.finalPosition);
        }
        // 小车1驱动到站1储料位
        if (currentState[Coil.car1AtStation1StoragePosition]) {
            if (Math.abs(car1.getX() - Destination.station1StoragePosition) > precision) {
                car1.setSpeed(speed);
                if (car1.getX() < Destination.station1StoragePosition) {
                    car1.setDirection(Constants.RIGHT);
                }
                if (car1.getX() > Destination.station1StoragePosition) {
                    car1.setDirection(Constants.LEFT);
                }
            }
            car1.setDestination(Destination.station1StoragePosition);
        }
        // 小车1驱动到站1加工位
        if (currentState[Coil.car1AtStation1ProcessingPosition]) {
            if (Math.abs(car1.getX() - Destination.station1ProcessingPosition) > precision) {
                car1.setSpeed(speed);
                if (car1.getX() < Destination.station1ProcessingPosition) {
                    car1.setDirection(Constants.RIGHT);
                }
                if (car1.getX() > Destination.station1ProcessingPosition) {
                    car1.setDirection(Constants.LEFT);
                }
            }
            car1.setDestination(Destination.station1ProcessingPosition);
        }
        // 小车1驱动到站1完成位
        if (currentState[Coil.car1AtStation1CompletionPosition]) {
            if (Math.abs(car1.getX() - Destination.station1CompletionPosition) > precision) {
                car1.setSpeed(speed);
                if (car1.getX() < Destination.station1CompletionPosition) {
                    car1.setDirection(Constants.RIGHT);
                }
                if (car1.getX() > Destination.station1CompletionPosition) {
                    car1.setDirection(Constants.LEFT);
                }
            }
            car1.setDestination(Destination.station1CompletionPosition);
        }
        // 小车1驱动到站2储备位
        if (currentState[Coil.car1AtStation2StoragePosition]) {
            if (Math.abs(car1.getX() - Destination.station2StoragePosition) > precision) {
                car1.setSpeed(speed);
                if (car1.getX() < Destination.station2StoragePosition) {
                    car1.setDirection(Constants.RIGHT);
                }
                if (car1.getX() > Destination.station2StoragePosition) {
                    car1.setDirection(Constants.LEFT);
                }
            }
            car1.setDestination(Destination.station2StoragePosition);
        }
        // 小车1驱动到站2加工位
        if (currentState[Coil.car1AtStation2ProcessingPosition]) {
            if (Math.abs(car1.getX() - Destination.station2ProcessingPosition) > precision) {
                car1.setSpeed(speed);
                if (car1.getX() < Destination.station2ProcessingPosition) {
                    car1.setDirection(Constants.RIGHT);
                }
                if (car1.getX() > Destination.station2ProcessingPosition) {
                    car1.setDirection(Constants.LEFT);
                }
            }
            car1.setDestination(Destination.station2ProcessingPosition);
        }
        // 小车1驱动到站2完成位
        if (currentState[Coil.car1AtStation2CompletionPosition]) {
            if (Math.abs(car1.getX() - Destination.station2CompletionPosition) > precision) {
                car1.setSpeed(speed);
                if (car1.getX() < Destination.station2CompletionPosition) {
                    car1.setDirection(Constants.RIGHT);
                }
                if (car1.getX() > Destination.station2CompletionPosition) {
                    car1.setDirection(Constants.LEFT);
                }
            }
            car1.setDestination(Destination.station2CompletionPosition);
        }
        // 小车1驱动到站3储备位
        if (currentState[Coil.car1AtStation3StoragePosition]) {
            if (Math.abs(car1.getX() - Destination.station3StoragePosition) > precision) {
                car1.setSpeed(speed);
                if (car1.getX() < Destination.station3StoragePosition) {
                    car1.setDirection(Constants.RIGHT);
                }
                if (car1.getX() > Destination.station3StoragePosition) {
                    car1.setDirection(Constants.LEFT);
                }
            }
            car1.setDestination(Destination.station3StoragePosition);
        }
        // 小车1驱动到站3加工位
        if (currentState[Coil.car1AtStation3ProcessingPosition]) {
            if (Math.abs(car1.getX() - Destination.station3ProcessingPosition) > precision) {
                car1.setSpeed(speed);
                if (car1.getX() < Destination.station3ProcessingPosition) {
                    car1.setDirection(Constants.RIGHT);
                }
                if (car1.getX() > Destination.station3ProcessingPosition) {
                    car1.setDirection(Constants.LEFT);
                }
            }
            car1.setDestination(Destination.station3ProcessingPosition);
        }
    }
    // 小车2同步中
    private void updateCar2_v2() {
        Log.e(TAG, "updateCar2_v2: running car2, index: " + fake_index);
        float precision = 1e-6f;
        int speed = 100;


        // 小车2获取箱子
        if (previousState[Coil.car2HookIn] && currentState[Coil.car2HookOut]) {
            if (car2.getBox() != null) {
                Log.e(TAG, "updateCar1_v2: 小车2由回钩到出钩, 此时应该没有箱子, 但有");
            }
            // 小车2获取箱子


            // 小车2在站3加工位获取箱子
            if (Float.compare(car2.getX(), Destination.station3ProcessingPosition) == 0) {
                Area area = workStationList.get(2).getProcessingArea();
                if (area.getBox() == null) {
                    Log.e(TAG, "updateCar1_v2: 小车2在站3加工位由回钩到出钩, 此处应该有箱子, 但没有");
                }
                car2.setBox(area.getBox());
                area.setBox(null);
            }
            // 小车2在站4储备位获取箱子
            if (Float.compare(car2.getX(), Destination.station4StoragePosition) == 0) {
                Area area = workStationList.get(3).getStorageArea();
                if (area.getBox() == null) {
                    Log.e(TAG, "updateCar1_v2: 小车2在站3储备位由回钩到出钩, 此处应该有箱子, 但没有");
                }
                car2.setBox(area.getBox());
                area.setBox(null);
            }
            // 小车2在站4加工位获取箱子
            if (Float.compare(car2.getX(), Destination.station4ProcessingPosition) == 0) {
                Area area = workStationList.get(3).getProcessingArea();
                if (area.getBox() == null) {
                    Log.e(TAG, "updateCar1_v2: 小车2在站3加工位由回钩到出钩, 此处应该有箱子, 但没有");
                }
                car2.setBox(area.getBox());
                area.setBox(null);
            }
            // 小车2在站5储备位获取箱子
            if (Float.compare(car2.getX(), Destination.station5StoragePosition) == 0) {
                Area area = workStationList.get(4).getStorageArea();
                if (area.getBox() == null) {
                    Log.e(TAG, "updateCar1_v2: 小车2在站5储备位由回钩到出钩, 此处应该有箱子, 但没有");
                }
                car2.setBox(area.getBox());
                area.setBox(null);
            }
            // 小车2在站5加工位获取箱子
            if (Float.compare(car2.getX(), Destination.station5ProcessingPosition) == 0) {
                Area area = workStationList.get(4).getProcessingArea();
                if (area.getBox() == null) {
                    Log.e(TAG, "updateCar1_v2: 小车2在站5加工位由回钩到出钩, 此处应该有箱子, 但没有");
                }
                car2.setBox(area.getBox());
                area.setBox(null);
            }
        }
        // 小车2放下箱子
        if (previousState[Coil.car2HookOut] && currentState[Coil.car2HookIn]) {
            if (car2.getBox() == null) {
                Log.e(TAG, "updateCar1_v2: 小车2由出钩到回钩, 此时应该有箱子, 但没有");

            }
            // 小车2在下料位放下箱子
            if (Float.compare(car2.getX(), Destination.finalPosition) == 0) {
                car2.getBox().getSprite().releaseResources();
                car2.setBox(null);
            }
            // 小车2在站3加工位放下箱子
            if (Float.compare(car2.getX(), Destination.station3ProcessingPosition) == 0) {
                Area area = workStationList.get(2).getProcessingArea();
                if (area.getBox() != null) {
                    Log.e(TAG, "updateCar1_v2: 小车2在站3加工位放下箱子, 此处原来应该没有箱子, 但有.");
                }
                area.setBox(car2.getBox());
                car2.setBox(null);
            }
            // 小车2在站4储备位放下箱子
            if (Float.compare(car2.getX(), Destination.station4StoragePosition) == 0) {
                Area area = workStationList.get(3).getStorageArea();
                if (area.getBox() != null) {
                    Log.e(TAG, "updateCar1_v2: 小车2在站4储备位放下箱子, 此处原来应该没有箱子, 但有.");
                }
                area.setBox(car2.getBox());
                car2.setBox(null);
            }
            // 小车2在站4加工位放下箱子
            if (Float.compare(car2.getX(), Destination.station4ProcessingPosition) == 0) {
                Area area = workStationList.get(3).getProcessingArea();
                if (area.getBox() != null) {
                    Log.e(TAG, "updateCar1_v2: 小车2在站4加工位放下箱子, 此处原来应该没有箱子, 但有.");
                }
                area.setBox(car2.getBox());
                car2.setBox(null);
            }
            // 小车2在站5储备位放下箱子
            if (Float.compare(car2.getX(), Destination.station5StoragePosition) == 0) {
                Area area = workStationList.get(4).getStorageArea();
                if (area.getBox() != null) {
                    Log.e(TAG, "updateCar1_v2: 小车2在站5储备位放下箱子, 此处原来应该没有箱子, 但有.");
                }
                area.setBox(car2.getBox());
                car2.setBox(null);
            }
            // 小车2在站5加工位放下箱子
            if (Float.compare(car2.getX(), Destination.station5ProcessingPosition) == 0) {
                Area area = workStationList.get(4).getProcessingArea();
                if (area.getBox() != null) {
                    Log.e(TAG, "updateCar1_v2: 小车2在站5加工位放下箱子, 此处原来应该没有箱子, 但有.");
                }
                area.setBox(car2.getBox());
                car2.setBox(null);
            }
        }


        // 小车2驱动到站3加工位
        if (currentState[Coil.car2AtStation3ProcessingPosition]) {
            Log.e(TAG, "updateCar2_v2: toS3");
            if (Math.abs(car2.getX() - Destination.station3ProcessingPosition) > precision) {
                car2.setSpeed(speed);
                if (car2.getX() < Destination.station3ProcessingPosition) {
                    car2.setDirection(Constants.RIGHT);
                }
                if (car2.getX() > Destination.station3ProcessingPosition) {
                    car2.setDirection(Constants.LEFT);
                }
            }
            car2.setDestination(Destination.station3ProcessingPosition);
        }
        // 小车2驱动到站3完成位
        if (currentState[Coil.car2AtStation3CompletionPosition]) {
            if (Math.abs(car2.getX() - Destination.station3CompletionPosition) > precision) {
                car2.setSpeed(speed);
                if (car2.getX() < Destination.station3CompletionPosition) {
                    car2.setDirection(Constants.RIGHT);
                }
                if (car2.getX() > Destination.station3CompletionPosition) {
                    car2.setDirection(Constants.LEFT);
                }
            }
            car2.setDestination(Destination.station3CompletionPosition);
        }
        // 小车2驱动到站4储备位
        if (currentState[Coil.car2AtStation4StoragePosition]) {
            if (Math.abs(car2.getX() - Destination.station4StoragePosition) > precision) {
                car2.setSpeed(speed);
                if (car2.getX() < Destination.station4StoragePosition) {
                    car2.setDirection(Constants.RIGHT);
                }
                if (car2.getX() > Destination.station4StoragePosition) {
                    car2.setDirection(Constants.LEFT);
                }
            }
            car2.setDestination(Destination.station4StoragePosition);
        }
        // 小车2驱动到站4加工位
        if (currentState[Coil.car2AtStation4ProcessingPosition]) {
            if (Math.abs(car2.getX() - Destination.station4ProcessingPosition) > precision) {
                car2.setSpeed(speed);
                if (car2.getX() < Destination.station4ProcessingPosition) {
                    car2.setDirection(Constants.RIGHT);
                }
                if (car2.getX() > Destination.station4ProcessingPosition) {
                    car2.setDirection(Constants.LEFT);
                }
            }
            car2.setDestination(Destination.station4ProcessingPosition);
        }
        // 小车2驱动到站4完成位
        if (currentState[Coil.car2AtStation4CompletionPosition]) {
            if (Math.abs(car2.getX() - Destination.station4CompletionPosition) > precision) {
                car2.setSpeed(speed);
                if (car2.getX() < Destination.station4CompletionPosition) {
                    car2.setDirection(Constants.RIGHT);
                }
                if (car2.getX() > Destination.station4CompletionPosition) {
                    car2.setDirection(Constants.LEFT);
                }
            }
            car2.setDestination(Destination.station4CompletionPosition);
        }
        // 小车2驱动到站5储备位
        if (currentState[Coil.car2AtStation5StoragePosition]) {
            if (Math.abs(car2.getX() - Destination.station5StoragePosition) > precision) {
                car2.setSpeed(speed);
                if (car2.getX() < Destination.station5StoragePosition) {
                    car2.setDirection(Constants.RIGHT);
                }
                if (car2.getX() > Destination.station5StoragePosition) {
                    car2.setDirection(Constants.LEFT);
                }
            }
            car2.setDestination(Destination.station5StoragePosition);
        }
        // 小车2驱动到站5加工位
        if (currentState[Coil.car2AtStation5ProcessingPosition]) {
            if (Math.abs(car2.getX() - Destination.station5ProcessingPosition) > precision) {
                car2.setSpeed(speed);
                if (car2.getX() < Destination.station5ProcessingPosition) {
                    car2.setDirection(Constants.RIGHT);
                }
                if (car2.getX() > Destination.station5ProcessingPosition) {
                    car2.setDirection(Constants.LEFT);
                }
            }
            car2.setDestination(Destination.station5ProcessingPosition);
        }
    }
    // 站1同步中
    private void updateStation1_v2() {
        // 料盒上升和下降
        // 料盒上升
        // 料盒上升由小车动作控制
        // 在小车放下料盒时, 设置料盒的方向和速度

        // 料盒下降
        WorkStation ws = workStationList.get(0);
        if (currentState[Coil.station1StoragePositionDown]) {
            if (ws.getStorageArea().getBox() != null) {
                Box box = ws.getStorageArea().getBox();
                if (box.getStatus() != Constants.BOX_DECLINED) {
                    box.setStatus(Constants.BOX_DECLING);
                }
            }
        }
        if (currentState[Coil.station1ProcessingPositionDown]) {
            if (ws.getProcessingArea().getBox() != null) {
                Box box = ws.getProcessingArea().getBox();
                if (box.getStatus() != Constants.BOX_DECLINED) {
                    box.setStatus(Constants.BOX_DECLING);
                }
            }
        }
      if (currentState[Coil.station1Running]) {
            ws.updateLight(Constants.SUCCESS);
        } else if (currentState[Coil.station1Stopped]) {
            ws.updateLight(Constants.WARNING);
        } else if (currentState[Coil.station1Error]) {
            ws.updateLight(Constants.DANGER);
        } else {
            ws.updateLight(Constants.STOPPED);
        }
    }
     // 站2同步中
    private void updateStation2_v2() {

        // 料盒上升和下降
        // 料盒上升
        // 料盒上升由小车动作控制
        // 在小车放下料盒时, 设置料盒的方向和速度

        // 料盒下降
        WorkStation ws = workStationList.get(1);
        if (currentState[Coil.station2StoragePositionDown]) {
            if (ws.getStorageArea().getBox() != null) {
                Box box = ws.getStorageArea().getBox();
                if (box.getStatus() != Constants.BOX_DECLINED) {
                    box.setStatus(Constants.BOX_DECLING);
                }
            }
        }
        if (currentState[Coil.station2ProcessingPositionDown]) {
            if (ws.getProcessingArea().getBox() != null) {
                Box box = ws.getProcessingArea().getBox();
                if (box.getStatus() != Constants.BOX_DECLINED) {
                    box.setStatus(Constants.BOX_DECLING);
                }
            }
        }
        if (currentState[Coil.station2Running]) {
            ws.updateLight(Constants.SUCCESS);
        } else if (currentState[Coil.station2Stopped]) {
            ws.updateLight(Constants.WARNING);
        } else if (currentState[Coil.station2Error]) {
            ws.updateLight(Constants.DANGER);
        } else {
            ws.updateLight(Constants.STOPPED);
        }
        /*
        Hand hand = ws.getHand();
        // 上下到取料位
        if (currentState[Coil.station2VerticallyToFetchPosition]) {
            if (hand.getStatus() != Constants.handDeclined) {
                hand.setStatus(Constants.handDeclining);
            }
        }
        // 上下到左放料位
        if (currentState[Coil.station2VerticallyToLeftPutPosition]) {
            if (hand.getStatus() != Constants.handDeclined) {
                hand.setStatus(Constants.handDeclining);
            }
        }
        // 上下到右放料位
        if (currentState[Coil.station2VerticallyToRightPutPosition]) {
            if (hand.getStatus() != Constants.handDeclined) {
                hand.setStatus(Constants.handDeclining);
            }
        }
        // 上下到等待位
        if (currentState[Coil.station2VerticallyToWaitPosition]) {
            if (hand.getStatus() != Constants.handRised) {
                hand.setStatus(Constants.handRised);
            }
        }
        // 平移到右放料位
        if (currentState[Coil.station2HorizontallyToRightPutPosition]) {
            if (hand.getStatus() != Constants.handRightShifted) {
                hand.setStatus(Constants.handRightShifting);
            }
        }
        // 平移到左放料位
        if (currentState[Coil.station2HorizontallyToLeftPutPosition]) {
            if (hand.getStatus() != Constants.handLeftShifted) {
                hand.setStatus(Constants.handLeftShifting);
            }
        }
        // 平移到取料位
        if (currentState[Coil.station2HorizontallyToFetchPosition]) {
            if (hand.getInitY() == hand.getLeftEndY()) {
                if (hand.getStatus() != Constants.handRightShifted) {
                    hand.setStatus(Constants.handRightShifting);
                }
            }
            if (hand.getInitY() == hand.getRightEndY()) {
                if (hand.getStatus() != Constants.handLeftShifted) {
                    hand.setStatus(Constants.handLeftShifting);
                }
            }
        }
        */
    }
     // 站3同步中
    private void updateStation3_v2() {

        // 料盒上升和下降
        // 料盒上升
        // 料盒上升由小车动作控制
        // 在小车放下料盒时, 设置料盒的方向和速度

        // 料盒下降
        WorkStation ws = workStationList.get(2);
        if (currentState[Coil.station3StoragePositionDown]) {
            if (ws.getStorageArea().getBox() != null) {
                Box box = ws.getStorageArea().getBox();
                if (box.getStatus() != Constants.BOX_DECLINED) {
                    box.setStatus(Constants.BOX_DECLING);
                }
            }
        }
        if (currentState[Coil.station3ProcessingPositionDown]) {
            if (ws.getProcessingArea().getBox() != null) {
                Box box = ws.getProcessingArea().getBox();
                if (box.getStatus() != Constants.BOX_DECLINED) {
                    box.setStatus(Constants.BOX_DECLING);
                }
            }
        }

        if (currentState[Coil.station3Running]) {
            ws.updateLight(Constants.SUCCESS);
        } else if (currentState[Coil.station3Stopped]) {
            ws.updateLight(Constants.WARNING);
        } else if (currentState[Coil.station3Error]) {
            ws.updateLight(Constants.DANGER);
        } else {
            ws.updateLight(Constants.STOPPED);
        }
        /*
        Hand hand = ws.getHand();
        // 上下到取料位
        if (currentState[Coil.station3VerticallyToFetchPosition]) {
            if (hand.getStatus() != Constants.handDeclined) {
                hand.setStatus(Constants.handDeclining);
            }
        }
        // 上下到左放料位
        if (currentState[Coil.station3VerticallyToLeftPutPosition]) {
            if (hand.getStatus() != Constants.handDeclined) {
                hand.setStatus(Constants.handDeclining);
            }
        }
        // 上下到右放料位
        if (currentState[Coil.station3VerticallyToRightPutPosition]) {
            if (hand.getStatus() != Constants.handDeclined) {
                hand.setStatus(Constants.handDeclining);
            }
        }
        // 上下到等待位
        if (currentState[Coil.station3VerticallyToWaitPosition]) {
            if (hand.getStatus() != Constants.handRised) {
                hand.setStatus(Constants.handRised);
            }
        }
        // 平移到右放料位
        if (currentState[Coil.station3HorizontallyToRightPutPosition]) {
            if (hand.getStatus() != Constants.handRightShifted) {
                hand.setStatus(Constants.handRightShifting);
            }
        }
        // 平移到左放料位
        if (currentState[Coil.station3HorizontallyToLeftPutPosition]) {
            if (hand.getStatus() != Constants.handLeftShifted) {
                hand.setStatus(Constants.handLeftShifting);
            }
        }
        // 平移到取料位
        if (currentState[Coil.station3HorizontallyToFetchPosition]) {
            if (hand.getInitY() == hand.getLeftEndY()) {
                if (hand.getStatus() != Constants.handRightShifted) {
                    hand.setStatus(Constants.handRightShifting);
                }
            }
            if (hand.getInitY() == hand.getRightEndY()) {
                if (hand.getStatus() != Constants.handLeftShifted) {
                    hand.setStatus(Constants.handLeftShifting);
                }
            }
        }
        */
    }
     // 站4同步中
    private void updateStation4_v2() {
        Log.e(TAG, "updateStation4_v2: enter");
        // 料盒上升和下降
        // 料盒上升
        // 料盒上升由小车动作控制
        // 在小车放下料盒时, 设置料盒的方向和速度

        // 料盒下降
        WorkStation ws = workStationList.get(3);
        if (currentState[Coil.station4StoragePositionDown]) {
            if (ws.getStorageArea().getBox() != null) {
                Box box = ws.getStorageArea().getBox();
                if (box.getStatus() != Constants.BOX_DECLINED) {
                    box.setStatus(Constants.BOX_DECLING);
                }
            }
        }
        if (currentState[Coil.station4ProcessingPositionDown]) {
            if (ws.getProcessingArea().getBox() != null) {
                Box box = ws.getProcessingArea().getBox();
                if (box.getStatus() != Constants.BOX_DECLINED) {
                    box.setStatus(Constants.BOX_DECLING);
                }
            }
        }
        if (currentState[Coil.station4Running]) {
            ws.updateLight(Constants.SUCCESS);
        } else if (currentState[Coil.station4Stopped]) {
            ws.updateLight(Constants.WARNING);
        } else if (currentState[Coil.station4Error]) {
            ws.updateLight(Constants.DANGER);
        } else {
            ws.updateLight(Constants.STOPPED);
        }
        /*
        Hand hand = ws.getHand();
        // 上下到取料位
        if (currentState[Coil.station4VerticallyToFetchPosition]) {
            if (hand.getStatus() != Constants.handDeclined) {
                hand.setStatus(Constants.handDeclining);
            }
        }
        // 上下到左放料位
        if (currentState[Coil.station4VerticallyToLeftPutPosition]) {
            if (hand.getStatus() != Constants.handDeclined) {
                hand.setStatus(Constants.handDeclining);
            }
        }
        // 上下到右放料位
        if (currentState[Coil.station4VerticallyToRightPutPosition]) {
            if (hand.getStatus() != Constants.handDeclined) {
                hand.setStatus(Constants.handDeclining);
            }
        }
        // 上下到等待位
        if (currentState[Coil.station4VerticallyToWaitPosition]) {
            if (hand.getStatus() != Constants.handRised) {
                hand.setStatus(Constants.handRised);
            }
        }
        // 平移到右放料位
        if (currentState[Coil.station4HorizontallyToRightPutPosition]) {
            if (hand.getStatus() != Constants.handRightShifted) {
                hand.setStatus(Constants.handRightShifting);
            }
        }
        // 平移到左放料位
        if (currentState[Coil.station4HorizontallyToLeftPutPosition]) {
            if (hand.getStatus() != Constants.handLeftShifted) {
                hand.setStatus(Constants.handLeftShifting);
            }
        }
        // 平移到取料位
        if (currentState[Coil.station4HorizontallyToFetchPosition]) {
            if (hand.getInitY() == hand.getLeftEndY()) {
                if (hand.getStatus() != Constants.handRightShifted) {
                    hand.setStatus(Constants.handRightShifting);
                }
            }
            if (hand.getInitY() == hand.getRightEndY()) {
                if (hand.getStatus() != Constants.handLeftShifted) {
                    hand.setStatus(Constants.handLeftShifting);
                }
            }
        }
        */
    }
    // 站5同步中
    private void updateStation5_v2() {

        // 料盒上升和下降
        // 料盒上升
        // 料盒上升由小车动作控制
        // 在小车放下料盒时, 设置料盒的方向和速度

        // 料盒下降
        WorkStation ws = workStationList.get(4);
        if (currentState[Coil.station5StoragePositionDown]) {
            if (ws.getStorageArea().getBox() != null) {
                Box box = ws.getStorageArea().getBox();
                if (box.getStatus() != Constants.BOX_DECLINED) {
                    box.setStatus(Constants.BOX_DECLING);
                }
            }
        }
        if (currentState[Coil.station5ProcessingPositionDown]) {
            if (ws.getProcessingArea().getBox() != null) {
                Box box = ws.getProcessingArea().getBox();
                if (box.getStatus() != Constants.BOX_DECLINED) {
                    box.setStatus(Constants.BOX_DECLING);
                }
            }
        }

        if (currentState[Coil.station5Running]) {
            ws.updateLight(Constants.SUCCESS);
        } else if (currentState[Coil.station5Stopped]) {
            ws.updateLight(Constants.WARNING);
        } else if (currentState[Coil.station5Error]) {
            ws.updateLight(Constants.DANGER);
        } else {
            ws.updateLight(Constants.STOPPED);
        }
        /*
        Hand hand = ws.getHand();
        // 上下到取料位
        if (currentState[Coil.station5VerticallyToFetchPosition]) {
            if (hand.getStatus() != Constants.handDeclined) {
                hand.setStatus(Constants.handDeclining);
            }
        }
        // 上下到左放料位
        if (currentState[Coil.station5VerticallyToLeftPutPosition]) {
            if (hand.getStatus() != Constants.handDeclined) {
                hand.setStatus(Constants.handDeclining);
            }
        }
        // 上下到右放料位
        if (currentState[Coil.station5VerticallyToRightPutPosition]) {
            if (hand.getStatus() != Constants.handDeclined) {
                hand.setStatus(Constants.handDeclining);
            }
        }
        // 上下到等待位
        if (currentState[Coil.station5VerticallyToWaitPosition]) {
            if (hand.getStatus() != Constants.handRised) {
                hand.setStatus(Constants.handRised);
            }
        }
        // 平移到右放料位
        if (currentState[Coil.station5HorizontallyToRightPutPosition]) {
            if (hand.getStatus() != Constants.handRightShifted) {
                hand.setStatus(Constants.handRightShifting);
            }
        }
        // 平移到左放料位
        if (currentState[Coil.station5HorizontallyToLeftPutPosition]) {
            if (hand.getStatus() != Constants.handLeftShifted) {
                hand.setStatus(Constants.handLeftShifting);
            }
        }
        // 平移到取料位
        if (currentState[Coil.station5HorizontallyToFetchPosition]) {
            if (Float.compare(hand.getInitY(), hand.getLeftEndY()) == 0) {
                if (hand.getStatus() != Constants.handRightShifted) {
                    hand.setStatus(Constants.handRightShifting);
                }
            }
            if (Float.compare(hand.getInitY(), hand.getRightEndY()) == 0) {
                if (hand.getStatus() != Constants.handLeftShifted) {
                    hand.setStatus(Constants.handLeftShifting);
                }
            }
        }
        */
    }
    // 站5机械臂同步中
    private void syncHand5() {
        // 初始位置是中间等待位, 当捕捉到从这里开始或以此处为目的地的信号时, 开始同步:
        // 1. 横移到左放料位=1
        // 2. 横移到右放料位=1
        // 3. 上下到取料位=1
        Hand hand = workStationList.get(4).getHand();
        if (currentState[Coil.station5HorizontallyToLeftPutPosition]
                || currentState[Coil.station5HorizontallyToRightPutPosition]
                || currentState[Coil.station5VerticallyToFetchPosition]) {
            hand.setMatch(true);
        }
    }
    // 站4机械臂同步中
    private void syncHand4() {
        // 初始位置是中间等待位, 当捕捉到从这里开始或以此处为目的地的信号时, 开始同步:
        // 1. 横移到左放料位=1
        // 2. 横移到右放料位=1
        // 3. 上下到取料位=1
        Hand hand = workStationList.get(3).getHand();
        if (currentState[Coil.station4HorizontallyToLeftPutPosition]
                || currentState[Coil.station4HorizontallyToRightPutPosition]
                || currentState[Coil.station4VerticallyToFetchPosition]) {
            hand.setMatch(true);
        }
    }
    // 站3机械臂同步中
    private void syncHand3() {
        // 初始位置是中间等待位, 当捕捉到从这里开始或以此处为目的地的信号时, 开始同步:
        // 1. 横移到左放料位=1
        // 2. 横移到右放料位=1
        // 3. 上下到取料位=1
        Hand hand = workStationList.get(2).getHand();
        if (currentState[Coil.station3HorizontallyToLeftPutPosition]
                || currentState[Coil.station3HorizontallyToRightPutPosition]
                || currentState[Coil.station3VerticallyToFetchPosition]) {
            hand.setMatch(true);
        }
    }
    // 站2机械臂同步中
    private void syncHand2() {
        // 初始位置是中间等待位, 当捕捉到从这里开始或以此处为目的地的信号时, 开始同步:
        // 1. 横移到左放料位=1
        // 2. 横移到右放料位=1
        // 3. 上下到取料位=1
        Hand hand = workStationList.get(1).getHand();
        if (currentState[Coil.station2HorizontallyToLeftPutPosition]
                || currentState[Coil.station2HorizontallyToRightPutPosition]
                || currentState[Coil.station2VerticallyToFetchPosition]) {
            hand.setMatch(true);
        }
    }
    // 站1机械臂同步中
    private void syncHand1() {
        // 初始位置是中间等待位, 当捕捉到从这里开始或以此处为目的地的信号时, 开始同步:
        // 1. 横移到左放料位=1
        // 2. 横移到右放料位=1
        // 3. 上下到取料位=1
        Hand hand = workStationList.get(0).getHand();
        if (currentState[Coil.station1HorizontallyToLeftPutPosition]
                || currentState[Coil.station1HorizontallyToRightPutPosition]
                || currentState[Coil.station1VerticallyToFetchPosition]) {
            hand.setMatch(true);
        }

    }
    // 站1机械臂完成同步, 执行动画
    private void updateHand1() {
        Hand hand = workStationList.get(0).getHand();
        // 上下到取料位
        if (currentState[Coil.station1VerticallyToFetchPosition]) {
            if (hand.getStatus() != Constants.handDeclined) {
                hand.setStatus(Constants.handDeclining);
            }
        }
        // 上下到左放料位
        if (currentState[Coil.station1VerticallyToLeftPutPosition]) {
            if (hand.getStatus() != Constants.handDeclined) {
                hand.setStatus(Constants.handDeclining);
            }
        }
        // 上下到右放料位
        if (currentState[Coil.station1VerticallyToRightPutPosition]) {
            if (hand.getStatus() != Constants.handDeclined) {
                hand.setStatus(Constants.handDeclining);
            }
        }
        // 上下到等待位
        if (currentState[Coil.station1VerticallyToWaitPosition]) {
            if (hand.getStatus() != Constants.handRised) {
                hand.setStatus(Constants.handRising);
            }
        }
        // 平移到右放料位
        if (currentState[Coil.station1HorizontallyToRightPutPosition]) {
            if (hand.getStatus() != Constants.handRightShifted) {
                hand.setStatus(Constants.handRightShifting);
            }
        }
        // 平移到左放料位
        if (currentState[Coil.station1HorizontallyToLeftPutPosition]) {
            if (hand.getStatus() != Constants.handLeftShifted) {
                hand.setStatus(Constants.handLeftShifting);
            }
        }
        // 平移到取料位
        if (currentState[Coil.station1HorizontallyToFetchPosition]) {
            if (hand.getInitY() == hand.getLeftEndY()) {
                if (hand.getStatus() != Constants.handRightShifted) {
                    hand.setStatus(Constants.handRightShifting);
                }
            }
            if (hand.getInitY() == hand.getRightEndY()) {
                if (hand.getStatus() != Constants.handLeftShifted) {
                    hand.setStatus(Constants.handLeftShifting);
                }
            }
        }

    }
    //  站2机械臂完成同步, 执行动画
    private void updateHand2() {
        Hand hand = workStationList.get(1).getHand();
        if (currentState[Coil.station2VerticallyToFetchPosition]) {
            if (hand.getStatus() != Constants.handDeclined) {
                hand.setStatus(Constants.handDeclining);
            }
        }
        // 上下到左放料位
        if (currentState[Coil.station2VerticallyToLeftPutPosition]) {
            if (hand.getStatus() != Constants.handDeclined) {
                hand.setStatus(Constants.handDeclining);
            }
        }
        // 上下到右放料位
        if (currentState[Coil.station2VerticallyToRightPutPosition]) {
            if (hand.getStatus() != Constants.handDeclined) {
                hand.setStatus(Constants.handDeclining);
            }
        }
        // 上下到等待位
        if (currentState[Coil.station2VerticallyToWaitPosition]) {
            if (hand.getStatus() != Constants.handRised) {
                hand.setStatus(Constants.handRised);
            }
        }
        // 平移到右放料位
        if (currentState[Coil.station2HorizontallyToRightPutPosition]) {
            if (hand.getStatus() != Constants.handRightShifted) {
                hand.setStatus(Constants.handRightShifting);
            }
        }
        // 平移到左放料位
        if (currentState[Coil.station2HorizontallyToLeftPutPosition]) {
            if (hand.getStatus() != Constants.handLeftShifted) {
                hand.setStatus(Constants.handLeftShifting);
            }
        }
        // 平移到取料位
        if (currentState[Coil.station2HorizontallyToFetchPosition]) {
            if (hand.getInitY() == hand.getLeftEndY()) {
                if (hand.getStatus() != Constants.handRightShifted) {
                    hand.setStatus(Constants.handRightShifting);
                }
            }
            if (hand.getInitY() == hand.getRightEndY()) {
                if (hand.getStatus() != Constants.handLeftShifted) {
                    hand.setStatus(Constants.handLeftShifting);
                }
            }
        }

    }
    //  站3机械臂完成同步, 执行动画
    private void updateHand3() {
        Hand hand = workStationList.get(2).getHand();
        if (currentState[Coil.station3VerticallyToFetchPosition]) {
            if (hand.getStatus() != Constants.handDeclined) {
                hand.setStatus(Constants.handDeclining);
            }
        }
        // 上下到左放料位
        if (currentState[Coil.station3VerticallyToLeftPutPosition]) {
            if (hand.getStatus() != Constants.handDeclined) {
                hand.setStatus(Constants.handDeclining);
            }
        }
        // 上下到右放料位
        if (currentState[Coil.station3VerticallyToRightPutPosition]) {
            if (hand.getStatus() != Constants.handDeclined) {
                hand.setStatus(Constants.handDeclining);
            }
        }
        // 上下到等待位
        if (currentState[Coil.station3VerticallyToWaitPosition]) {
            if (hand.getStatus() != Constants.handRised) {
                hand.setStatus(Constants.handRised);
            }
        }
        // 平移到右放料位
        if (currentState[Coil.station3HorizontallyToRightPutPosition]) {
            if (hand.getStatus() != Constants.handRightShifted) {
                hand.setStatus(Constants.handRightShifting);
            }
        }
        // 平移到左放料位
        if (currentState[Coil.station3HorizontallyToLeftPutPosition]) {
            if (hand.getStatus() != Constants.handLeftShifted) {
                hand.setStatus(Constants.handLeftShifting);
            }
        }
        // 平移到取料位
        if (currentState[Coil.station3HorizontallyToFetchPosition]) {
            if (hand.getInitY() == hand.getLeftEndY()) {
                if (hand.getStatus() != Constants.handRightShifted) {
                    hand.setStatus(Constants.handRightShifting);
                }
            }
            if (hand.getInitY() == hand.getRightEndY()) {
                if (hand.getStatus() != Constants.handLeftShifted) {
                    hand.setStatus(Constants.handLeftShifting);
                }
            }
        }

    }
    //  站4机械臂完成同步, 执行动画
    private void updateHand4() {
        Hand hand = workStationList.get(3).getHand();
        if (currentState[Coil.station4VerticallyToFetchPosition]) {
            if (hand.getStatus() != Constants.handDeclined) {
                hand.setStatus(Constants.handDeclining);
            }
        }
        // 上下到左放料位
        if (currentState[Coil.station4VerticallyToLeftPutPosition]) {
            if (hand.getStatus() != Constants.handDeclined) {
                hand.setStatus(Constants.handDeclining);
            }
        }
        // 上下到右放料位
        if (currentState[Coil.station4VerticallyToRightPutPosition]) {
            if (hand.getStatus() != Constants.handDeclined) {
                hand.setStatus(Constants.handDeclining);
            }
        }
        // 上下到等待位
        if (currentState[Coil.station4VerticallyToWaitPosition]) {
            if (hand.getStatus() != Constants.handRised) {
                hand.setStatus(Constants.handRised);
            }
        }
        // 平移到右放料位
        if (currentState[Coil.station4HorizontallyToRightPutPosition]) {
            if (hand.getStatus() != Constants.handRightShifted) {
                hand.setStatus(Constants.handRightShifting);
            }
        }
        // 平移到左放料位
        if (currentState[Coil.station4HorizontallyToLeftPutPosition]) {
            if (hand.getStatus() != Constants.handLeftShifted) {
                hand.setStatus(Constants.handLeftShifting);
            }
        }
        // 平移到取料位
        if (currentState[Coil.station4HorizontallyToFetchPosition]) {
            if (hand.getInitY() == hand.getLeftEndY()) {
                if (hand.getStatus() != Constants.handRightShifted) {
                    hand.setStatus(Constants.handRightShifting);
                }
            }
            if (hand.getInitY() == hand.getRightEndY()) {
                if (hand.getStatus() != Constants.handLeftShifted) {
                    hand.setStatus(Constants.handLeftShifting);
                }
            }
        }

    }
    //  站4机械臂完成同步, 执行动画
    private void updateHand5() {
        Hand hand = workStationList.get(4).getHand();
        if (currentState[Coil.station5VerticallyToFetchPosition]) {
            if (hand.getStatus() != Constants.handDeclined) {
                hand.setStatus(Constants.handDeclining);
            }
        }
        // 上下到左放料位
        if (currentState[Coil.station5VerticallyToLeftPutPosition]) {
            if (hand.getStatus() != Constants.handDeclined) {
                hand.setStatus(Constants.handDeclining);
            }
        }
        // 上下到右放料位
        if (currentState[Coil.station5VerticallyToRightPutPosition]) {
            if (hand.getStatus() != Constants.handDeclined) {
                hand.setStatus(Constants.handDeclining);
            }
        }
        // 上下到等待位
        if (currentState[Coil.station5VerticallyToWaitPosition]) {
            if (hand.getStatus() != Constants.handRised) {
                hand.setStatus(Constants.handRised);
            }
        }
        // 平移到右放料位
        if (currentState[Coil.station5HorizontallyToRightPutPosition]) {
            if (hand.getStatus() != Constants.handRightShifted) {
                hand.setStatus(Constants.handRightShifting);
            }
        }
        // 平移到左放料位
        if (currentState[Coil.station5HorizontallyToLeftPutPosition]) {
            if (hand.getStatus() != Constants.handLeftShifted) {
                hand.setStatus(Constants.handLeftShifting);
            }
        }
        // 平移到取料位
        if (currentState[Coil.station5HorizontallyToFetchPosition]) {
            if (hand.getInitY() == hand.getLeftEndY()) {
                if (hand.getStatus() != Constants.handRightShifted) {
                    hand.setStatus(Constants.handRightShifting);
                }
            }
            if (hand.getInitY() == hand.getRightEndY()) {
                if (hand.getStatus() != Constants.handLeftShifted) {
                    hand.setStatus(Constants.handLeftShifting);
                }
            }
        }

    }

    // 用于 run2() 函数, 更新小车的方向和速度.
    private void updateDirection(int index, int flag) {
        // 小车1
        if (index == 1) {
            // 如果小车没有移动, 调整速度为 0.
            if (car1PreviousPosition == car1GetPosition(flag)) {
                // car1 didn't move
                if (car1.getSpeed() != 0) {
                    car1.setSpeed(0);
                }
                return;
            }
            // car1 move to right
            // 小车1上一个位置在当前位置的左边, 小车1向右移动
            if (car1PreviousPosition < car1GetPosition(flag)) {
                car1.setDirection(Constants.RIGHT);
            }
            // car1 move to left
            // 小车1上一个位置在当前位置的右边, 小车1向左移动
            if (car1PreviousPosition > car1GetPosition(flag)) {
                car1.setDirection(Constants.LEFT);
            }
            // should have speed
            if (car1.getSpeed() == 0) {
                car1.setSpeed(100);
            }
            car1PreviousPosition = car1GetPosition(flag);
        }
        // 小车2
        if (index == 2) {
            // 如果小车2的位置没有改变, 速度设为0.
            if (car2PreviousPosition == car2GetPosition(flag)) {
                if (car2.getSpeed() != 0) {
                    car2.setSpeed(0);
                }
                return;
            }
            // 小车2上一个位置在当前位置的左边, 小车2向右移动
            if (car2PreviousPosition < car2GetPosition(flag)) {
                car2.setDirection(Constants.RIGHT);
            }
            // 小车2上一个位置在当前位置的右边,小车2向左移动
            if (car2PreviousPosition > car2GetPosition(flag)) {
                car2.setDirection(Constants.LEFT);
            }
            car2.setSpeed(100);
        }
    }


    // v1
    public void run() {
        previousReachIndex = 0;
        final Handler handler = new Handler();
        blockX = 2000;
        final int delay = 2000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                currentStatus = Signal.status[index];
                Log.e(TAG, "run: " + (index - 1) + " -> " + index);
//                Log.e(TAG, "run: speed: " + car1.getSpeed());
//                Log.e(TAG, "run: car1x: " + car1.getX());
//                Log.e(TAG, "run: blockx: " + blockX);
//                boolean hasBox = car1.getBox() != null;
//                if (hasBox) {
//                    Log.e(TAG, "run: has box");
//                } else {
//                    Log.e(TAG, "run: no box");
//                }
                if (index < 26) {
                    ++index;
                }
                int n = currentStatus.length;
                for (int i = 0; i < n; ++i) {
//                    Log.e(TAG, "run: " + i + "," + car1.getSpeed());
                    if (previousStatus == null || previousStatus[i] != currentStatus[i]) {
                        switch (i) {
                            case 0:
                                if (currentStatus[i] == 1) {
                                    changeDirection(i);
                                }
                                // 到达上料挡停位
                                break;
                            case 1:
                                if (currentStatus[i] == 1) {
                                    changeDirection(i);
                                    if (currentStatus[6] == 1 && car1.getBox() != null) {
                                        workStationList.get(0).getStorageArea().setBox(car1.getBox());
                                    } else if (currentStatus[6] == 1 && car1.getBox() == null && workStationList.get(0).getStorageArea().getBox() != null) {
                                        if (currentStatus[12] == 1) {
                                            car1.setBox(workStationList.get(0).getStorageArea().getBox());
                                            workStationList.get(0).getStorageArea().setBox(null);
                                        }
                                    }
                                }
                                // 到达站1储备位
                                break;
                            case 2:
                                if (currentStatus[i] == 1) {
                                    changeDirection(i);
                                    if (currentStatus[4] == 1 && car1.getBox() != null) {
                                        workStationList.get(0).getProcessingArea().setBox(car1.getBox());
                                    } else if (currentStatus[4] == 1 && car1.getBox() == null && workStationList.get(0).getProcessingArea().getBox() != null) {
//                                        if (currentStatus[12] == 1) {
//                                            car1.setBox(station1WorkingArea.getBox());
//                                            station1WorkingArea.setBox(null);
//                                        }
                                    }
                                }
                                // 到达站1加工位
                                break;
                            case 3:
                                if (currentStatus[i] == 1) {
                                    changeDirection(i);
                                }
                                // 到达站1完成位
                                break;
                            case 4:
                                if (currentStatus[i] == 1) {
                                    blockX = workStationList.get(0).getProcessingArea().x;
                                }
                                // 站1加工位挡停到位
                                break;
                            case 5:
                                // 站1加工位挡停回到原位
                                break;
                            case 6:
                                if (currentStatus[i] == 1) {
                                    blockX = workStationList.get(0).getStorageArea().x;
                                }
                                // 站1储备位挡停到位
                                break;
                            case 7:
                                // 站1储备位回到原位
                                break;
                            case 8:
                                if (currentStatus[i] == 1) {
                                    if (workStationList.get(0).getProcessingArea().getBox() != null) {
                                        workStationList.get(0).getProcessingArea().getBox().setStatus(Constants.BOX_RISING);
                                    }
                                }
                                // 站1加工位上料盒到位
                                break;
                            case 9:
                                if (currentStatus[i] == 1) {
                                    if (workStationList.get(0).getProcessingArea().getBox() != null) {
                                        workStationList.get(0).getProcessingArea().getBox().setStatus(Constants.BOX_DECLING);
                                    }
                                }
                                // 站1加工位下料盒到位
                                break;
                            case 10:
                                if (currentStatus[i] == 1) {
                                    if (workStationList.get(0).getStorageArea().getBox() != null) {
                                        workStationList.get(0).getStorageArea().getBox().setStatus(Constants.BOX_RISING);
                                    }
                                }
                                // 站1储备位上料盒到位
                                break;
                            case 11:
                                if (currentStatus[i] == 1) {
                                    if (workStationList.get(0).getStorageArea().getBox() != null) {
                                        workStationList.get(0).getStorageArea().getBox().setStatus(Constants.BOX_DECLING);
                                    }
                                }
                                // 站1储备位下料盒到位
                                break;
                            case 12:
                                // 小车1出钩
                                if (currentStatus[i] == 1) {
                                    if (currentStatus[2] == 1 && currentStatus[4] == 1 && workStationList.get(0).getProcessingArea().getBox() != null && car1.getBox() == null) {
                                        car1.setBox(workStationList.get(0).getProcessingArea().getBox());
                                        workStationList.get(0).getProcessingArea().setBox(null);
                                    } else {
                                        float boxX = car1.getX();
                                        float boxY = car1.getY();
                                        float boxWidth = car1.getWidth();
                                        float boxHeight = car1.getHeight() - 20;
                                        Texture texture = new Texture(getApplicationContext(), R.drawable.box);
                                        Sprite sprite = new Sprite((int) boxWidth, (int) boxHeight);
                                        sprite.setPivot(0.5f, 0.5f);
                                        sprite.setPos(boxX, boxY);
                                        sprite.setTexture(texture);
                                        Box b = new Box(boxX, boxY, boxWidth, boxHeight, texture, sprite);
                                        b.render(renderPassSprite);
                                        car1.setBox(b);
                                        Log.e(TAG, "run: carno. mark");
                                    }
                                }
                                break;
                            case 13:
                                // 小车1回钩
                                if (currentStatus[i] == 1) {
                                    car1.setBox(null);
                                }
                                break;
                            case 14:
                                if (currentStatus[i] == 1) {
                                    changeDirection(i);
                                    blockX = glWidth;
                                }
                                break;
                            case 15:
                                if (currentStatus[i] == 1) {
                                    blockX = 0;
                                }
                                break;
                            case 16:
                                break;

                            case 17:
                                if (currentStatus[i] == 1) {
                                    workStationList.get(0).getHand().setStatus(Constants.handRising);
                                }
                                break;
                            case 18:
                                if (currentStatus[i] == 1) {
                                    workStationList.get(0).getHand().setStatus(Constants.handDeclining);
                                }
                                break;
                            case 19:
                                if (currentStatus[i] == 1) {
                                    workStationList.get(0).getHand().setStatus(Constants.handDeclining);
                                }
                                break;
                            case 20:
                                if (currentStatus[i] == 1) {
                                    workStationList.get(0).getHand().setStatus(Constants.handDeclining);
                                }
                                break;
                            case 21:
                                if (currentStatus[i] == 1) {
                                    if (workStationList.get(0).getHand().getInitY() == workStationList.get(0).getHand().getLeftEndY()) {
                                        workStationList.get(0).getHand().setStatus(Constants.handRightShifting);
                                    } else if (workStationList.get(0).getHand().getInitY() == workStationList.get(0).getHand().getRightEndY()) {
                                        workStationList.get(0).getHand().setStatus(Constants.handLeftShifting);
                                    } else {
                                    }
                                }
                                break;
                            case 22:
                                if (currentStatus[i] == 1) {
                                    workStationList.get(0).getHand().setStatus(Constants.handLeftShifting);
                                }
                                break;
                            case 23:
                                if (currentStatus[i] == 1) {
                                    workStationList.get(0).getHand().setStatus(Constants.handRightShifting);
                                    Log.e(TAG, "run: reached.");
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
                previousStatus = currentStatus;
                handler.postDelayed(this, delay);
            }
        }, delay);
    }
    private int car2GetPosition(int flag) {
        int pos = 0;
        switch (flag) {
            case Coil.car2AtStation3ProcessingPosition:
                pos = 10;
                break;
            case Coil.car2AtStation3CompletionPosition:
                pos = 11;
                break;
            case Coil.car2AtStation4StoragePosition:
                pos = 12;
                break;
            case Coil.car2AtStation4ProcessingPosition:
                pos = 13;
                break;
            case Coil.car2AtStation4CompletionPosition:
                pos = 15;
                break;
            case Coil.car2AtStation5StoragePosition:
                pos = 16;
                break;
            case Coil.car2AtStation5ProcessingPosition:
                pos = 17;
                break;
            default:
                break;
        }
        return pos;
    }

    // 判断当前信号所在的位置, 用来和上一个位置进行比较, 决定小车的方向和速度
    private int car1GetPosition(int flag) {
        int pos = 0;
        switch (flag) {
            case Coil.car1AtStation1StoragePosition:
                pos = 10;
                break;
            case Coil.car1AtStation1ProcessingPosition:
                pos = 11;
                break;
            case Coil.car1AtStation1CompletionPosition:
                pos = 12;
                break;
            case Coil.car1AtStation2StoragePosition:
                pos = 13;
                break;
            case Coil.car1AtStation2ProcessingPosition:
                pos = 14;
                break;
            case Coil.car1AtStation2CompletionPosition:
                pos = 15;
                break;
            case Coil.car1AtStation3StoragePosition:
                pos = 16;
                break;
            case Coil.car1AtStation3ProcessingPosition:
                pos = 17;
                break;
            // to be continue
            default:
                break;
        }
        return pos;
    }
    // 存在错误的函数, 目前使用 updateCar1_v2()
    // 小车的动画, 由3个部分组成
    // 1. 小车左右移动
    // 2. 小车由回钩到出钩, 获得箱子
    // 3. 小车由出钩到回钩, 放下箱子
    // 小车左右移动, 根据上一个状态挡板升起位置和当前状态挡板升起位置来判断.
    // 如果两次状态挡板升起位置一致, 那么小车保持静止,
    // 如果上一个挡板升起位置在当前挡板升起位置的左边, 小车向右移动,
    // 如果上一个挡板升起位置在当前挡板升起位置的右边, 小车向左移动.
    // 小车由回钩到出钩, 判断条件是:
    // 1. 上一个状态小车是回钩状态
    // 2. 当前状态小车是出钩状态
    // 此时还要判断小车的位置, 小车在当前的位置获得箱子
    // 小车由出钩到回钩, 判断条件是:
    // 1. 上一个状态小车是出钩状态
    // 2. 当前状态小车是回钩状态
    // 此时还要判断小车的位置, 小车在当前位置放下箱子
    private void updateCar1(boolean[] booleans) {
        // 更新方向和速度
        // 根据小车现在经过的位置和之前经过的位置来决定小车的运动方向和速度, 后动效果,
        // 如果可以根据挡板升起位置来判断小车的运动方向和速度的话, 可以实现前动.
        // 经过站1
        // 储料位
        if (currentState[Coil.car1AtStation1StoragePosition]) {
            updateDirection(1, Coil.car1AtStation1StoragePosition);
        }
        // 加工位
        if (currentState[Coil.car1AtStation1ProcessingPosition]) {
            updateDirection(1, Coil.car1AtStation1ProcessingPosition);
        }
        // 完成位
        if (currentState[Coil.car1AtStation1CompletionPosition]) {
            updateDirection(1, Coil.car1AtStation1CompletionPosition);
        }
        // 经过站2
        // 储料位
        if (currentState[Coil.car1AtStation2StoragePosition]) {
            updateDirection(1, Coil.car1AtStation2StoragePosition);
        }
        // 加工位
        if (currentState[Coil.car1AtStation2ProcessingPosition]) {
            updateDirection(1, Coil.car1AtStation2ProcessingPosition);
        }
        // 完成位
        if (currentState[Coil.car1AtStation2CompletionPosition]) {
            updateDirection(1, Coil.car1AtStation2CompletionPosition);
        }
        // 经过站3
        // 储料位
        if (currentState[Coil.car1AtStation3StoragePosition]) {
            updateDirection(1, Coil.car1AtStation3StoragePosition);
        }
        // 加工位
        if (currentState[Coil.car1AtStation3ProcessingPosition]) {
            updateDirection(1, Coil.car1AtStation3ProcessingPosition);
        }

        // 出钩
        // 上一次状态是回钩，当前状态是出钩
        if (currentState[Coil.car1HookOut] && previousState[Coil.car1HookIn]) {
            // 小车1此时应该没有箱子
            if (car1.getBox() != null) {
                Log.e(TAG, "updateCar1: 小车1此时应该没有箱子，但有");
            }
            // 取箱的位置应该有挡板升起，所以判断小车1的当前位置是否在挡板升起处。
            // 如果是，该区域应该有一个箱子，小车1获得该箱子。
            // 如果不是，小车1继续移动。

            // 站1储备位挡板升起,小车1到达站1储备位
            if (currentState[Coil.car1AtStation1StoragePosition] && currentState[Coil.station1StoragePositionBlocked]) {
                if (workStationList.get(0).getStorageArea().getBox() == null) {
                    Log.e(TAG, "updateCar1: 工站1储备位应该有箱子，但没有");
                }
                car1.setBox(workStationList.get(0).getStorageArea().getBox());
                workStationList.get(0).getStorageArea().setBox(null);
            }

            // 站1加工位挡板升起,小车1到达站1加工位
            if (currentState[Coil.car1AtStation1ProcessingPosition] && currentState[Coil.station1ProcessingPositionBlocked]) {
                if (workStationList.get(0).getProcessingArea().getBox() == null) {
                    Log.e(TAG, "updateCar1: 工站1加工位应该有箱子，但没有");
                }
                car1.setBox(workStationList.get(0).getProcessingArea().getBox());
                workStationList.get(0).getProcessingArea().setBox(null);
            }

            // 站2储备位挡板升起, 小车1到达站2储备位
            if (currentState[Coil.car1AtStation2StoragePosition] && currentState[Coil.station2StoragePositionBlocked]) {
                if (workStationList.get(1).getStorageArea().getBox() == null) {
                    Log.e(TAG, "updateCar1: 工站2储备位应该有箱子，但没有");
                }
                car1.setBox(workStationList.get(1).getStorageArea().getBox());
                workStationList.get(1).getStorageArea().setBox(null);
            }

            // 站2加工位挡板升起, 小车1到达站2加工位
            if (currentState[Coil.car1AtStation2ProcessingPosition] && currentState[Coil.station2ProcessingPositionBlocked]) {
                if (workStationList.get(1).getProcessingArea().getBox() == null) {
                    Log.e(TAG, "updateCar1: 工站2加工位应该有箱子，但没有");
                }
                car1.setBox(workStationList.get(1).getProcessingArea().getBox());
                workStationList.get(1).getProcessingArea().setBox(null);
            }

            // 站3储备位挡板升起, 小车1到达站3储备位
            if (currentState[Coil.car1AtStation3StoragePosition] && currentState[Coil.station3StoragePositionBlocked]) {
                if (workStationList.get(2).getStorageArea().getBox() == null) {
                    Log.e(TAG, "updateCar1: 工站3储备位应该有箱子，但没有");
                }
                car1.setBox(workStationList.get(2).getStorageArea().getBox());
                workStationList.get(2).getStorageArea().setBox(null);
            }
            // 站3加工位挡板升起, 小车1到达站3加工位
            if (currentState[Coil.car1AtStation3ProcessingPosition] && currentState[Coil.station3ProcessingPositionBlocked]) {
                if (workStationList.get(2).getProcessingArea().getBox() == null) {
                    Log.e(TAG, "updateCar1: 工站3加工位应该有箱子，但没有");
                }
                car1.setBox(workStationList.get(2).getProcessingArea().getBox());
                workStationList.get(2).getProcessingArea().setBox(null);
            }
        }

        // 上一次状态是出钩, 当前状态是回钩, 放下箱子
        if (currentState[Coil.car1HookIn] && previousState[Coil.car1HookOut]) {
            // 小车1此时应该有箱子
            if (car1.getBox() == null) {
                Log.e(TAG, "updateCar1: 小车1应该有箱子，但没有。");
            }
            // 小车1经过站1储备位, 站1储备位挡板升起
            if (currentState[Coil.car1AtStation1StoragePosition] && currentState[Coil.station1StoragePositionBlocked]) {
                workStationList.get(0).getStorageArea().setBox(car1.getBox());
            }
            // 小车1经过站1加工位, 站1加工位挡板升起, 站1加工位获得箱子
            if (currentState[Coil.car1AtStation1ProcessingPosition] && currentState[Coil.station1ProcessingPositionBlocked]) {
                workStationList.get(0).getProcessingArea().setBox(car1.getBox());
            }
            // 小车1经过站2储备位, 站2储备位挡板升起, 站2储备位获得箱子
            if (currentState[Coil.car1AtStation2StoragePosition] && currentState[Coil.station2StoragePositionBlocked]) {
                workStationList.get(1).getStorageArea().setBox(car1.getBox());
            }
            // 小车1经过站2加工位, 站2加工位挡板升起, 站2加工位获得箱子
            if (currentState[Coil.car1AtStation2ProcessingPosition] && currentState[Coil.station2ProcessingPositionBlocked]) {
                workStationList.get(1).getProcessingArea().setBox(car1.getBox());
            }
            // 小车1经过站3储备位, 站3储备位挡板升起, 站3储备位获得箱子
            if (currentState[Coil.car1AtStation3StoragePosition] && currentState[Coil.station3StoragePositionBlocked]) {
                workStationList.get(2).getStorageArea().setBox(car1.getBox());
            }
            // 小车1经过站3加工位, 站3加工位挡板升起, 站3加工位获得箱子
            if (currentState[Coil.car1AtStation3ProcessingPosition] && currentState[Coil.station3ProcessingPositionBlocked]) {
                workStationList.get(2).getProcessingArea().setBox(car1.getBox());
            }
            // 小车1放下箱子
            car1.setBox(null);
        }

    }
    private void updateCar2(boolean[] booleans) {
        // 经过某地
        if (currentState[Coil.car2AtStation3ProcessingPosition]) {
            updateDirection(2, Coil.car2AtStation3ProcessingPosition);
        }
        if (currentState[Coil.car2AtStation3CompletionPosition]) {
            updateDirection(2, Coil.car2AtStation3CompletionPosition);
        }
        if (currentState[Coil.car2AtStation4StoragePosition]) {
            updateDirection(2, Coil.car2AtStation4StoragePosition);
        }
        if (currentState[Coil.car2AtStation4ProcessingPosition]) {
            updateDirection(2, Coil.car2AtStation4ProcessingPosition);
        }
        if (currentState[Coil.car2AtStation4CompletionPosition]) {
            updateDirection(2, Coil.car2AtStation4CompletionPosition);
        }
        if (currentState[Coil.car2AtStation5StoragePosition]) {
            updateDirection(2, Coil.car2AtStation5StoragePosition);
        }
        if (currentState[Coil.car2AtStation5ProcessingPosition]) {
            updateDirection(2, Coil.car2AtStation5ProcessingPosition);
        }
        // 出钩
        // 上一个状态是回钩，当前状态是出钩，小车获得箱子
        if (currentState[Coil.car2HookOut] && previousState[Coil.car2HookIn]) {
            // 小车2此时应该没有箱子
            if (car2.getBox() != null) {
                Log.e(TAG, "updateCar2: 小车2此时应该没有箱子，但有");
            }
            // 判断小车2在哪里获得箱子
            // 站3加工位
            if (currentState[Coil.car2AtStation3ProcessingPosition] && currentState[Coil.station3ProcessingPositionBlocked]) {
                // 工站3加工位应该有箱子
                if (workStationList.get(2).getProcessingArea().getBox() == null) {
                    Log.e(TAG, "updateCar2: 工站3加工位此时应该有箱子，但没有");
                }
                car2.setBox(workStationList.get(2).getProcessingArea().getBox());
                workStationList.get(2).getProcessingArea().setBox(null);
            }
            // 站4储备位
            if (currentState[Coil.car2AtStation4StoragePosition] && currentState[Coil.station3StoragePositionBlocked]) {
                if (workStationList.get(3).getStorageArea().getBox() == null) {
                    Log.e(TAG, "updateCar2: 工站4储备位此时应该有箱子，但没有");
                }
                car2.setBox(workStationList.get(3).getStorageArea().getBox());
                workStationList.get(3).getStorageArea().setBox(null);
            }
            // 站4加工位
            if (currentState[Coil.car2AtStation4ProcessingPosition] && currentState[Coil.station4ProcessingPositionBlocked]) {
                if (workStationList.get(3).getProcessingArea().getBox() == null) {
                    Log.e(TAG, "updateCar2: 工站4储备位此时应该有箱子，但没有");
                }
                car2.setBox(workStationList.get(3).getProcessingArea().getBox());
                workStationList.get(3).getProcessingArea().setBox(null);
            }
            // 站5储备位
            if (currentState[Coil.car2AtStation5StoragePosition] && currentState[Coil.station5StoragePositionBlocked]) {
                if (workStationList.get(4).getStorageArea().getBox() == null) {
                    Log.e(TAG, "updateCar2: 工站5储备位此时应该有箱子，但没有");
                }
                car2.setBox(workStationList.get(4).getStorageArea().getBox());
                workStationList.get(4).getStorageArea().setBox(null);
            }
            // 站5加工位
            if (currentState[Coil.car2AtStation5ProcessingPosition] && currentState[Coil.station5ProcessingPositionBlocked]) {
                if (workStationList.get(4).getProcessingArea().getBox() == null) {
                    Log.e(TAG, "updateCar2: 工站5加工位此时应该有箱子，但没有");
                }
                car2.setBox(workStationList.get(4).getProcessingArea().getBox());
                workStationList.get(4).getProcessingArea().setBox(null);
            }

        }

        // 回钩
        // 上一个状态是出钩，当前状态是回钩，小车放下箱子
        // 可能的位置：
        // 1. 各工站的储备位、加工位
        // 2. 下料位
        if (currentState[Coil.car2HookIn] && previousState[Coil.car2HookOut]) {
            // 小车此时应该有箱子
            if (car2.getBox() == null) {
                Log.e(TAG, "updateCar2: 小车2此时应该有箱子，但没有, 判定条件:上一个状态出钩，当前状态回钩");
            }
            // 判断小车在哪里放下箱子
            // 站3加工位
            if (currentState[Coil.car2AtStation3ProcessingPosition] && currentState[Coil.station3ProcessingPositionBlocked]) {
                // 站3加工位挡停到位, 小车2经过站3加工位, 此时站3加工位应该没有箱子
                if (workStationList.get(2).getProcessingArea().getBox() != null) {
                    Log.e(TAG, "updateCar2: 站3加工位此时应该没有箱子，但有");
                }
                workStationList.get(2).getProcessingArea().setBox(car2.getBox());
            }
            // 站4储备位
            if (currentState[Coil.car2AtStation4StoragePosition] && currentState[Coil.station4StoragePositionBlocked]) {
                // 站4储备位挡停到位, 小车2经过站4储备位, 站4储备位此时应该没有箱子
                if (workStationList.get(3).getStorageArea().getBox() != null) {
                    Log.e(TAG, "updateCar2: 站4储备位此时应该没有箱子，但有");
                }
                workStationList.get(3).getStorageArea().setBox(car2.getBox());
            }
            // 站4加工位
            if (currentState[Coil.car2AtStation4ProcessingPosition] && currentState[Coil.station4ProcessingPositionBlocked]) {
                // 站4加工位挡停到位，小车2经过站4加工位，站4加工位此时应该没有箱子
                if (workStationList.get(3).getProcessingArea().getBox() != null) {
                    Log.e(TAG, "updateCar2: 站4加工位此时应该没有箱子，但有");
                }
                workStationList.get(3).getProcessingArea().setBox(car2.getBox());
            }
            // 站5储备位
            if (currentState[Coil.car2AtStation5StoragePosition] && currentState[Coil.station5StoragePositionBlocked]) {
                // 站5储备位挡停到位，小车2经过站5储备位，站5储备位此时应该没有箱子，小车2将箱子放在此处
                if (workStationList.get(4).getStorageArea().getBox() != null) {
                    Log.e(TAG, "updateCar2: 站5储备位此时应该没有箱子，但有");
                }
                workStationList.get(4).getStorageArea().setBox(car2.getBox());
            }
            // 站5加工位
            if (currentState[Coil.car2AtStation5ProcessingPosition] && currentState[Coil.station5ProcessingPositionBlocked]) {
                // 站5加工位挡停到位，小车2经过站5加工位，站5加工位此时应该没有箱子，小车2将箱子放在此处
                if (workStationList.get(5).getProcessingArea().getBox() != null) {
                    Log.e(TAG, "updateCar2: 站5加工位此时应该没有箱子，但有");
                }
                workStationList.get(4).getProcessingArea().setBox(car2.getBox());
            }
            car2.setBox(null);
        }
    }
    // 工作台的动画由6个部分组成:
    // 1. 箱子上升
    // 2. 箱子下降
    // 3. 获得箱子
    // 4. 失去箱子
    // 5. 机械手移动
    // 6. 指示灯动画
    // 箱子上升, (后动)判断的信号是:
    // 1. 该位置上料盒到位
    // 2. (可选) 该位置有料盒
    // 箱子下降: (后动)判断的信号是:
    // 1. 该位置的下料盒到位
    // 2. (可选) 该位置有料盒
    // 获得箱子:由小车动画负责执行, 小车在当前位置由出钩到回钩时放下箱子
    // 失去箱子:由小车动画负责执行, 小车在当前位置由回钩到出钩时失去箱子
    // 机械手移动
    // 1. 机械手左右移动
    // 2. 机械手上下移动
    // 由于捕捉到的这些信号都是驱动信号, 所以, 当这些信号由 0 变 1 时, 开始做动画, 做完后进入等待状态
    // 指示灯动画
    // 指示灯的切换并不需要动画过程, 直接根据状态的变化切换即可.
    private void updateStation1(boolean[] booleans) {
        WorkStation ws = workStationList.get(0);
        // 指示灯动画
        if (booleans[Coil.station1Error]) {
            ws.updateLight(Constants.DANGER);
        }
        if (booleans[Coil.station1Stopped]) {
            ws.updateLight(Constants.WARNING);
        }
        if (booleans[Coil.station1Running]) {
            ws.updateLight(Constants.SUCCESS);
        }
        // 机械手动画, 以下捕捉的是驱动信号, 要根据前后两次状态判断是否开始做动画
        // 上下到取料位
        if (!previousState[Coil.station1VerticallyToFetchPosition] && currentState[Coil.station1VerticallyToFetchPosition]) {
            ws.getHand().setStatus(Constants.handDeclining);
        }
        // 上下到等待位
        if (!previousState[Coil.station1VerticallyToWaitPosition] && currentState[Coil.station1VerticallyToWaitPosition]) {
            ws.getHand().setStatus(Constants.handRising);
        }
        // 上下到左取料位
        if (!previousState[Coil.station1VerticallyToLeftPutPosition] && currentState[Coil.station1VerticallyToLeftPutPosition]) {
            ws.getHand().setStatus(Constants.handDeclining);
        }
        // 上下到右取料位
        if (!previousState[Coil.station1VerticallyToRightPutPosition] && currentState[Coil.station1VerticallyToRightPutPosition]) {
            ws.getHand().setStatus(Constants.handDeclining);
        }
        // 横移到取料位
        if (!previousState[Coil.station1HorizontallyToFetchPosition] && currentState[Coil.station1HorizontallyToFetchPosition]) {
            // 判断是向左还是向右横移
            if (ws.getHand().getInitY() == ws.getHand().getRightEndY()) {
                // 当前机械手在右边，左移
                ws.getHand().setStatus(Constants.handLeftShifting);
            }
            if (ws.getHand().getInitY() == ws.getHand().getLeftEndY()) {
                // 当前机械手在左边，右移
                ws.getHand().setStatus(Constants.handRightShifting);
            }
        }
        // 横移到左放料位
        if (!previousState[Coil.station1HorizontallyToLeftPutPosition] && currentState[Coil.station1HorizontallyToLeftPutPosition]) {
            ws.getHand().setStatus(Constants.handLeftShifting);
        }
        // 横移到右放料位
        if (!previousState[Coil.station1HorizontallyToRightPutPosition] && currentState[Coil.station1HorizontallyToRightPutPosition]) {
            ws.getHand().setStatus(Constants.handRightShifting);
        }

        // 箱子的上升和下降, 到达信号, 只能采取后动
        // 后动： 加工位料盒上升
        if (currentState[Coil.station1ProcessingPositionUp]) {
            if (ws.getProcessingArea().getBox() != null) {
                ws.getProcessingArea().getBox().setStatus(Constants.BOX_RISING);
            }
        }
        // 后动：加工位料盒下降
        if (currentState[Coil.station1ProcessingPositionDown]) {
            if (ws.getProcessingArea().getBox() != null) {
                ws.getProcessingArea().getBox().setStatus(Constants.BOX_DECLING);
            }
        }
        // 后动：储备位料盒上升
        if (currentState[Coil.station1StoragePositionUp]) {
            if (ws.getStorageArea().getBox() != null) {
                ws.getStorageArea().getBox().setStatus(Constants.BOX_RISING);
            }
        }
        // 后动：储备位料盒下降
        if (currentState[Coil.station1StoragePositionDown]) {
            if (ws.getStorageArea().getBox() != null) {
                ws.getStorageArea().getBox().setStatus(Constants.BOX_DECLING);
            }
        }
    }

    private void updateStation2(boolean[] booleans) {
        WorkStation ws = workStationList.get(1);
        // update light
        if (currentState[Coil.station2Error]) {
            ws.updateLight(Constants.DANGER);
        }
        if (currentState[Coil.station2Stopped]) {
            ws.updateLight(Constants.WARNING);
        }
        if (currentState[Coil.station2Running]) {
            ws.updateLight(Constants.SUCCESS);
        }
        // update hand
        // 上下到取料位
        if (!previousState[Coil.station2VerticallyToFetchPosition] && currentState[Coil.station2VerticallyToFetchPosition]) {
            ws.getHand().setStatus(Constants.handDeclining);
        }
        // 上下到等待位
        if (!previousState[Coil.station2VerticallyToWaitPosition] && currentState[Coil.station2VerticallyToWaitPosition]) {
            ws.getHand().setStatus(Constants.handRising);
        }
        // 上下到左取料位
        if (!previousState[Coil.station2VerticallyToLeftPutPosition] && currentState[Coil.station2VerticallyToLeftPutPosition]) {
            ws.getHand().setStatus(Constants.handDeclining);
        }
        // 上下到右取料位
        if (!previousState[Coil.station2VerticallyToRightPutPosition] && currentState[Coil.station2VerticallyToRightPutPosition]) {
            ws.getHand().setStatus(Constants.handDeclining);
        }
        // 横移到取料位
        if (!previousState[Coil.station2HorizontallyToFetchPosition] && currentState[Coil.station2HorizontallyToFetchPosition]) {
            // 判断是向左还是向右横移
            if (ws.getHand().getInitY() == ws.getHand().getRightEndY()) {
                // 当前机械手在右边，左移
                ws.getHand().setStatus(Constants.handLeftShifting);
            }
            if (ws.getHand().getInitY() == ws.getHand().getLeftEndY()) {
                // 当前机械手在左边，右移
                ws.getHand().setStatus(Constants.handRightShifting);
            }
        }
        // 横移到左放料位
        if (!previousState[Coil.station2HorizontallyToLeftPutPosition] && currentState[Coil.station2HorizontallyToLeftPutPosition]) {
            ws.getHand().setStatus(Constants.handLeftShifting);
        }
        // 横移到右放料位
        if (!previousState[Coil.station2HorizontallyToRightPutPosition] && currentState[Coil.station2HorizontallyToRightPutPosition]) {
            ws.getHand().setStatus(Constants.handRightShifting);
        }
        // 箱子动作
        // 后动： 加工位料盒上升
        if (currentState[Coil.station2ProcessingPositionUp]) {
            if (ws.getProcessingArea().getBox() != null) {
                ws.getProcessingArea().getBox().setStatus(Constants.BOX_RISING);
            }
        }
        // 后动：加工位料盒下降
        if (currentState[Coil.station2ProcessingPositionDown]) {
            if (ws.getProcessingArea().getBox() != null) {
                ws.getProcessingArea().getBox().setStatus(Constants.BOX_DECLING);
            }
        }
        // 后动：储备位料盒上升
        if (currentState[Coil.station2StoragePositionUp]) {
            if (ws.getStorageArea().getBox() != null) {
                ws.getStorageArea().getBox().setStatus(Constants.BOX_RISING);
            }
        }
        // 后动：储备位料盒下降
        if (currentState[Coil.station2StoragePositionDown]) {
            if (ws.getStorageArea().getBox() != null) {
                ws.getStorageArea().getBox().setStatus(Constants.BOX_DECLING);
            }
        }
    }

    private void updateStation3(boolean[] booleans) {
        WorkStation ws = workStationList.get(2);
        // update light
        if (currentState[Coil.station3Error]) {
            ws.updateLight(Constants.DANGER);
        }
        if (currentState[Coil.station3Stopped]) {
            ws.updateLight(Constants.WARNING);
        }
        if (currentState[Coil.station3Running]) {
            ws.updateLight(Constants.SUCCESS);
        }
        // update hand
        // 上下到取料位
        if (!previousState[Coil.station3VerticallyToFetchPosition] && currentState[Coil.station3VerticallyToFetchPosition]) {
            ws.getHand().setStatus(Constants.handDeclining);
        }
        // 上下到等待位
        if (!previousState[Coil.station3VerticallyToWaitPosition] && currentState[Coil.station3VerticallyToWaitPosition]) {
            ws.getHand().setStatus(Constants.handRising);
        }
        // 上下到左取料位
        if (!previousState[Coil.station3VerticallyToLeftPutPosition] && currentState[Coil.station3VerticallyToLeftPutPosition]) {
            ws.getHand().setStatus(Constants.handDeclining);
        }
        // 上下到右取料位
        if (!previousState[Coil.station3VerticallyToRightPutPosition] && currentState[Coil.station3VerticallyToRightPutPosition]) {
            ws.getHand().setStatus(Constants.handDeclining);
        }
        // 横移到取料位
        if (!previousState[Coil.station3HorizontallyToFetchPosition] && currentState[Coil.station3HorizontallyToFetchPosition]) {
            // 判断是向左还是向右横移
            if (ws.getHand().getInitY() == ws.getHand().getRightEndY()) {
                // 当前机械手在右边，左移
                ws.getHand().setStatus(Constants.handLeftShifting);
            }
            if (ws.getHand().getInitY() == ws.getHand().getLeftEndY()) {
                // 当前机械手在左边，右移
                ws.getHand().setStatus(Constants.handRightShifting);
            }
        }
        // 横移到左放料位
        if (!previousState[Coil.station3HorizontallyToLeftPutPosition] && currentState[Coil.station3HorizontallyToLeftPutPosition]) {
            ws.getHand().setStatus(Constants.handLeftShifting);
        }
        // 横移到右放料位
        if (!previousState[Coil.station3HorizontallyToRightPutPosition] && currentState[Coil.station3HorizontallyToRightPutPosition]) {
            ws.getHand().setStatus(Constants.handRightShifting);
        }

        // 箱子动作
        // 后动： 加工位料盒上升
        if (currentState[Coil.station3ProcessingPositionUp]) {
            if (ws.getProcessingArea().getBox() != null) {
                ws.getProcessingArea().getBox().setStatus(Constants.BOX_RISING);
            }
        }
        // 后动：加工位料盒下降
        if (currentState[Coil.station3ProcessingPositionDown]) {
            if (ws.getProcessingArea().getBox() != null) {
                ws.getProcessingArea().getBox().setStatus(Constants.BOX_DECLING);
            }
        }
        // 后动：储备位料盒上升
        if (currentState[Coil.station3StoragePositionUp]) {
            if (ws.getStorageArea().getBox() != null) {
                ws.getStorageArea().getBox().setStatus(Constants.BOX_RISING);
            }
        }
        // 后动：储备位料盒下降
        if (currentState[Coil.station3StoragePositionDown]) {
            if (ws.getStorageArea().getBox() != null) {
                ws.getStorageArea().getBox().setStatus(Constants.BOX_DECLING);
            }
        }
    }

    private void updateStation4(boolean[] booleans) {
        WorkStation ws = workStationList.get(3);
        // update light
        if (currentState[Coil.station4Error]) {
            ws.updateLight(Constants.DANGER);
        }
        if (currentState[Coil.station4Stopped]) {
            ws.updateLight(Constants.WARNING);
        }
        if (currentState[Coil.station4Running]) {
            ws.updateLight(Constants.SUCCESS);
        }
        // update hand
        // 上下到取料位
        if (!previousState[Coil.station4VerticallyToFetchPosition] && currentState[Coil.station4VerticallyToFetchPosition]) {
            ws.getHand().setStatus(Constants.handDeclining);
        }
        // 上下到等待位
        if (!previousState[Coil.station4VerticallyToWaitPosition] && currentState[Coil.station4VerticallyToWaitPosition]) {
            ws.getHand().setStatus(Constants.handRising);
        }
        // 上下到左取料位
        if (!previousState[Coil.station4VerticallyToLeftPutPosition] && currentState[Coil.station4VerticallyToLeftPutPosition]) {
            ws.getHand().setStatus(Constants.handDeclining);
        }
        // 上下到右取料位
        if (!previousState[Coil.station4VerticallyToRightPutPosition] && currentState[Coil.station4VerticallyToRightPutPosition]) {
            ws.getHand().setStatus(Constants.handDeclining);
        }
        // 横移到取料位
        if (!previousState[Coil.station4HorizontallyToFetchPosition] && currentState[Coil.station4HorizontallyToFetchPosition]) {
            // 判断是向左还是向右横移
            if (ws.getHand().getInitY() == ws.getHand().getRightEndY()) {
                // 当前机械手在右边，左移
                ws.getHand().setStatus(Constants.handLeftShifting);
            }
            if (ws.getHand().getInitY() == ws.getHand().getLeftEndY()) {
                // 当前机械手在左边，右移
                ws.getHand().setStatus(Constants.handRightShifting);
            }
        }
        // 横移到左放料位
        if (!previousState[Coil.station4HorizontallyToLeftPutPosition] && currentState[Coil.station4HorizontallyToLeftPutPosition]) {
            ws.getHand().setStatus(Constants.handLeftShifting);
        }
        // 横移到右放料位
        if (!previousState[Coil.station4HorizontallyToRightPutPosition] && currentState[Coil.station4HorizontallyToRightPutPosition]) {
            ws.getHand().setStatus(Constants.handRightShifting);
        }

        // 箱子动作
        // 后动： 加工位料盒上升
        if (currentState[Coil.station4ProcessingPositionUp]) {
            if (ws.getProcessingArea().getBox() != null) {
                ws.getProcessingArea().getBox().setStatus(Constants.BOX_RISING);
            }
        }
        // 后动：加工位料盒下降
        if (currentState[Coil.station4ProcessingPositionDown]) {
            if (ws.getProcessingArea().getBox() != null) {
                ws.getProcessingArea().getBox().setStatus(Constants.BOX_DECLING);
            }
        }
        // 后动：储备位料盒上升
        if (currentState[Coil.station4StoragePositionUp]) {
            if (ws.getStorageArea().getBox() != null) {
                ws.getStorageArea().getBox().setStatus(Constants.BOX_RISING);
            }
        }
        // 后动：储备位料盒下降
        if (currentState[Coil.station4StoragePositionDown]) {
            if (ws.getStorageArea().getBox() != null) {
                ws.getStorageArea().getBox().setStatus(Constants.BOX_DECLING);
            }
        }
    }

    private void updateStation5(boolean[] booleans) {
        WorkStation ws = workStationList.get(4);
        // update light
        if (currentState[Coil.station3Error]) {
            ws.updateLight(Constants.DANGER);
        }
        if (currentState[Coil.station3Stopped]) {
            ws.updateLight(Constants.WARNING);
        }
        if (currentState[Coil.station3Running]) {
            ws.updateLight(Constants.SUCCESS);
        }
        // update hand
        // 上下到取料位
        if (!previousState[Coil.station5VerticallyToFetchPosition] && currentState[Coil.station5VerticallyToFetchPosition]) {
            ws.getHand().setStatus(Constants.handDeclining);
        }
        // 上下到等待位
        if (!previousState[Coil.station5VerticallyToWaitPosition] && currentState[Coil.station5VerticallyToWaitPosition]) {
            ws.getHand().setStatus(Constants.handRising);
        }
        // 上下到左取料位
        if (!previousState[Coil.station5VerticallyToLeftPutPosition] && currentState[Coil.station5VerticallyToLeftPutPosition]) {
            ws.getHand().setStatus(Constants.handDeclining);
        }
        // 上下到右取料位
        if (!previousState[Coil.station5VerticallyToRightPutPosition] && currentState[Coil.station5VerticallyToRightPutPosition]) {
            ws.getHand().setStatus(Constants.handDeclining);
        }
        // 横移到取料位
        if (!previousState[Coil.station5HorizontallyToFetchPosition] && currentState[Coil.station5HorizontallyToFetchPosition]) {
            // 判断是向左还是向右横移
            if (ws.getHand().getInitY() == ws.getHand().getRightEndY()) {
                // 当前机械手在右边，左移
                ws.getHand().setStatus(Constants.handLeftShifting);
            }
            if (ws.getHand().getInitY() == ws.getHand().getLeftEndY()) {
                // 当前机械手在左边，右移
                ws.getHand().setStatus(Constants.handRightShifting);
            }
        }
        // 横移到左放料位
        if (!previousState[Coil.station5HorizontallyToLeftPutPosition] && currentState[Coil.station5HorizontallyToLeftPutPosition]) {
            ws.getHand().setStatus(Constants.handLeftShifting);
        }
        // 横移到右放料位
        if (!previousState[Coil.station5HorizontallyToRightPutPosition] && currentState[Coil.station5HorizontallyToRightPutPosition]) {
            ws.getHand().setStatus(Constants.handRightShifting);
        }

        // 箱子动作
        // 后动： 加工位料盒上升
        if (currentState[Coil.station3ProcessingPositionUp]) {
            if (ws.getProcessingArea().getBox() != null) {
                ws.getProcessingArea().getBox().setStatus(Constants.BOX_RISING);
            }
        }
        // 后动：加工位料盒下降
        if (currentState[Coil.station3ProcessingPositionDown]) {
            if (ws.getProcessingArea().getBox() != null) {
                ws.getProcessingArea().getBox().setStatus(Constants.BOX_DECLING);
            }
        }
        // 后动：储备位料盒上升
        if (currentState[Coil.station3StoragePositionUp]) {
            if (ws.getStorageArea().getBox() != null) {
                ws.getStorageArea().getBox().setStatus(Constants.BOX_RISING);
            }
        }
        // 后动：储备位料盒下降
        if (currentState[Coil.station3StoragePositionDown]) {
            if (ws.getStorageArea().getBox() != null) {
                ws.getStorageArea().getBox().setStatus(Constants.BOX_DECLING);
            }
        }
    }

    private void updateHand(boolean[] booleans) {

    }

    private void updateLight(boolean[] booleans) {
        // 1st station
        // 2nd station
        if (booleans[Coil.station2Error]) {
            workStationList.get(1).updateLight(Constants.DANGER);
        }
        if (booleans[Coil.station2Stopped]) {
            workStationList.get(1).updateLight(Constants.WARNING);
        }
        if (booleans[Coil.station2Running]) {
            workStationList.get(1).updateLight(Constants.SUCCESS);
        }
        // 3rd station
        if (booleans[Coil.station3Error]) {
            workStationList.get(2).updateLight(Constants.DANGER);
        }
        if (booleans[Coil.station3Stopped]) {
            workStationList.get(2).updateLight(Constants.WARNING);
        }
        if (booleans[Coil.station3Running]) {
            workStationList.get(2).updateLight(Constants.SUCCESS);
        }
        // 4th station
        if (booleans[Coil.station4Error]) {
            workStationList.get(3).updateLight(Constants.DANGER);
        }
        if (booleans[Coil.station4Stopped]) {
            workStationList.get(3).updateLight(Constants.WARNING);
        }
        if (booleans[Coil.station4Running]) {
            workStationList.get(3).updateLight(Constants.SUCCESS);
        }
        // 5th station
        if (booleans[Coil.station5Error]) {
            workStationList.get(4).updateLight(Constants.DANGER);
        }
        if (booleans[Coil.station5Stopped]) {
            workStationList.get(4).updateLight(Constants.WARNING);
        }
        if (booleans[Coil.station5Running]) {
            workStationList.get(4).updateLight(Constants.SUCCESS);
        }
    }

    public void changeDirection(int reachIndex) {
        if (reachIndex > previousReachIndex) {
            car1.setDirection(Constants.RIGHT);
            car1.setSpeed(800);
            Log.e(TAG, "changeDirection: " + reachIndex);
        } else if (reachIndex < previousReachIndex) {
            car1.setDirection(Constants.LEFT);
            car1.setSpeed(800);
        } else {
            car1.setSpeed(0);
        }
        previousReachIndex = reachIndex;
    }

    // SmartGL callbacks.
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void onPrepareView(SmartGLView smartGLView) {

        renderer = smartGLView.getSmartGLRenderer();
        renderPassSprite = new RenderPassSprite();
        renderer.addRenderPass(renderPassSprite);  // add it only once for all Sprites

        // load resources.
        glHeight = mSmartGLView.getHeight();
        glWidth = mSmartGLView.getWidth();
        unitHeight = glHeight / 2 / 18;
        unitWidth = glWidth / 11 / 4;
        unitWidth += 20;
        unitHeight += 10;
        Constants.glWidth = glWidth;
        Constants.glHeight = glHeight;
        Constants.unitWidth = unitWidth;
        Constants.unitHeight = unitHeight;
//        unitWidth += 10;
//        unitHeight += 10;

        workStationList = new ArrayList<>();

        for (int i = 0; i < 5; ++i) {
            float x = glWidth / 11 * (i * 2 + 1) + unitWidth * 2;
            ws = new WorkStation(getApplicationContext(), x, glHeight / 2);
            ws.init(glWidth, glHeight, unitWidth, unitHeight);
            ws.render(renderPassSprite);
            workStationList.add(ws);
        }
        // set position
        Destination.initialPosition = 0;
        Destination.finalPosition = glWidth;

        Destination.station1StoragePosition = workStationList.get(0).getStorageArea().x;
        Destination.station1ProcessingPosition = workStationList.get(0).getProcessingArea().x;
        Destination.station1CompletionPosition = workStationList.get(0).getCompletionArea().x;

        Destination.station2StoragePosition = workStationList.get(1).getStorageArea().x;
        Destination.station2ProcessingPosition = workStationList.get(1).getProcessingArea().x;
        Destination.station2CompletionPosition = workStationList.get(1).getCompletionArea().x;

        Destination.station3StoragePosition = workStationList.get(2).getStorageArea().x;
        Destination.station3ProcessingPosition = workStationList.get(2).getProcessingArea().x;
        Destination.station3CompletionPosition = workStationList.get(2).getCompletionArea().x;

        Destination.station4StoragePosition = workStationList.get(3).getStorageArea().x;
        Destination.station4ProcessingPosition = workStationList.get(3).getProcessingArea().x;
        Destination.station4CompletionPosition = workStationList.get(3).getCompletionArea().x;

        Destination.station5StoragePosition = workStationList.get(4).getStorageArea().x;
        Destination.station5ProcessingPosition = workStationList.get(4).getProcessingArea().x;


        preparationArea = new Area();
        preparationArea.x = 0;
        preparationArea.width = unitWidth;

        // prepare car.
        float car1X = 0;
        float car1Y = glHeight / 2;
        float carWidth = unitWidth;
        float carHeight = unitHeight * 2 + 20;
        int carPriority = 100;

        Texture carTexture = new Texture(getApplicationContext(), R.drawable.car);
        Sprite car1Sprite = new Sprite((int) carWidth, (int) carHeight);
        car1Sprite.setPivot(0.5f, 0.5f);
        car1Sprite.setPos(car1X, car1Y);
        car1Sprite.setTexture(carTexture);
        car1Sprite.setDisplayPriority(carPriority);

        car1 = new Car(car1X, car1Y, carWidth, carHeight, carTexture, car1Sprite);
        car1.setSpeed(0);
        renderPassSprite.addSprite(car1Sprite);

        float car2X = Destination.station3ProcessingPosition;
        float car2Y = glHeight / 2;
        Sprite car2Sprite = new Sprite((int) carWidth, (int) carHeight);
        car2Sprite.setPivot(0.5f, 0.5f);
        car2Sprite.setPos(car2X, car2Y);
        car2Sprite.setTexture(carTexture);
        car2Sprite.setDisplayPriority(carPriority);
        car2 = new Car(car2X, car2Y, carWidth, carHeight, carTexture, car2Sprite);
        car2.setSpeed(0);
        renderPassSprite.addSprite(car2Sprite);

        Log.e(TAG, "initSmartGL: " + glHeight + "," + glWidth);
        Log.e(TAG, "initSmartGL: " + unitWidth + "," + unitHeight);

        float assemblyLineX = glWidth / 2;
        float assemblyLineY = glHeight / 2;
        float assemblyLineWidth = glWidth;
        float assemblyLineHeight = unitHeight * 2;

        int assemblyLinePriority = 10;
        int platformPriority = 5;
        int bodyPriority = 0;
        int handPriority = 0;

        Texture texture = new Texture(getApplicationContext(), R.drawable.assembly_line);
        Sprite sprite = new Sprite((int) assemblyLineWidth, (int) assemblyLineHeight);
        sprite.setPivot(0.5f, 0.5f);
        sprite.setPos(assemblyLineX, assemblyLineY);
        sprite.setDisplayPriority(assemblyLinePriority);
        sprite.setTexture(texture);
        AssemblyLine assemblyLine = new AssemblyLine(assemblyLineX, assemblyLineY, assemblyLineWidth, assemblyLineHeight, texture, sprite);
        assemblyLine.render(renderPassSprite);
    }

    @Override
    public void onReleaseView(SmartGLView smartGLView) {

    }

    @Override
    public void onResizeView(SmartGLView smartGLView) {

    }

    @Override
    public void onTouchEvent(SmartGLView smartGLView, TouchHelperEvent touchHelperEvent) {

    }
}

