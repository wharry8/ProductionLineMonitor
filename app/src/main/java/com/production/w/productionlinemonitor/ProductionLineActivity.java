package com.production.w.productionlinemonitor;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
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
import com.production.w.productionlinemonitor.Model.Area;
import com.production.w.productionlinemonitor.Model.AssemblyLine;
import com.production.w.productionlinemonitor.Model.Box;
import com.production.w.productionlinemonitor.Model.Car;
import com.production.w.productionlinemonitor.Model.Hand;
import com.production.w.productionlinemonitor.Model.WorkStation;
import com.zgkxzx.modbus4And.requset.ModbusReq;
import com.zgkxzx.modbus4And.requset.OnRequestBack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;

import fr.arnaudguyon.smartgl.opengl.RenderPassSprite;
import fr.arnaudguyon.smartgl.opengl.SmartGLRenderer;
import fr.arnaudguyon.smartgl.opengl.SmartGLView;
import fr.arnaudguyon.smartgl.opengl.SmartGLViewController;
import fr.arnaudguyon.smartgl.opengl.Sprite;
import fr.arnaudguyon.smartgl.opengl.Texture;
import fr.arnaudguyon.smartgl.touch.TouchHelperEvent;

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

    // used by run1();
    // used by updateCar1()
    int previousPosition;
    int car1PreviousPosition;
    int car2PreviousPosition;
    boolean currentState[];
    boolean previousState[];

    // used by updateCar1_v2()

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // landscape mode.
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_production_line);

        new ConncetToDB().execute();
        initNavigationDrawer();
        bind();
        updateView();

        initSmartGL();
        run2();
    }

    class ConncetToDB extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            return null;
        }
    }

    public void initNavigationDrawer () {

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
    public void initSmartGL () {
        mSmartGLView = (SmartGLView) findViewById(R.id.smartGLView);
        mSmartGLView.setDefaultRenderer(this);
        mSmartGLView.setController(this);
    }

    public void updateView () {
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
    public void updateCncStatus (boolean[] booleans) {
        // todo
        // 1. find mappings.
        // 2. read status and display.
    }
    public void updateTime () {

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

    @Override
    public void onTick(SmartGLView smartGLView) {
        updateAnimation();
    }

    // 线程1
    // 按帧显示动画, 大概1秒30帧
    public void updateAnimation () {
        float deltaTime = renderer.getFrameDuration();

//        Log.e(TAG, "updateAnimation: car1: " + car1.getX() + "," + car1.getSpeed() + "," + car1.getDirection());

        // animation of cars.
//        car1.move(deltaTime, blockX);
        car1.move_v2(deltaTime);

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

//        car2.move(deltaTime);

        // animation of box up and down.

        // animation of hand.

    }
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
                                    if (currentStatus[2] == 1 &&  currentStatus[4] == 1 && workStationList.get(0).getProcessingArea().getBox() != null && car1.getBox() == null) {
                                        car1.setBox(workStationList.get(0).getProcessingArea().getBox());
                                        workStationList.get(0).getProcessingArea().setBox(null);
                                    } else {
                                        float boxX=  car1.getX();
                                        float boxY = car1.getY();
                                        float boxWidth = car1.getWidth();
                                        float boxHeight = car1.getHeight() - 20;
                                        Texture texture = new Texture(getApplicationContext(), R.drawable.box);
                                        Sprite sprite = new Sprite((int)boxWidth, (int)boxHeight);
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

    // 用于实际场景的动画效果
    // 在一定的时间间隔内读取一次生产线的状态, 根据状态的变化做出相应的动画
    // 目前的时间间隔是 0.2 秒
    public void run2()
    {
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
                                // todo
                                // initial set up.
                            } else {
                                Hand hand1 =  workStationList.get(0).getHand();
                                Hand hand2 =  workStationList.get(1).getHand();
                                Hand hand3 =  workStationList.get(2).getHand();
                                Hand hand4 =  workStationList.get(3).getHand();
                                Hand hand5 =  workStationList.get(4).getHand();
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
                                    updateStation1_v2 ();
                                    updateStation2_v2 ();
                                    updateStation3_v2 ();
                                } else {
                                    syncCar1();
                                }
                                if (car2.isMatch()) {
                                    updateCar2_v2();
                                    updateStation4_v2 ();
                                    updateStation5_v2 ();
                                } else {
                                    syncCar2();
                                }
                            }
                            previousState = currentState;
                        }
                        @Override
                        public void onFailed(String msg) {
                            Log.e(TAG, "readCoil onFailed " + msg);
                        }
                    }, 1, Constants.CoilStart, Constants.CoilLen);
            }
        }, delay);
    }

    // 小车2同步中
    private void syncCar2 () {
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
            Area area = workStationList.get(3).getStorageArea();
            updateArea(area, Constants.BOX_DECLINED);
        }

        // 站4加工位
        // 站4加工位有升起的箱子
        if (currentState[Coil.station4ProcessingPositionUp]) {
            Area area = workStationList.get(3).getProcessingArea();
            updateArea(area, Constants.BOX_RISED);
        }
        // 站4加工位有降下的箱子
        if (currentState[Coil.station4ProcessingPositionDown]) {
            Area area = workStationList.get(3).getProcessingArea();
            updateArea(area, Constants.BOX_DECLINED);
        }

        // 站5储备位
        // 站5储备位有升起的箱子
        if (currentState[Coil.station5StoragePositionUp]) {
            Area area = workStationList.get(4).getStorageArea();
            updateArea(area, Constants.BOX_RISED);
        }
        // 站5储备位有降下的箱子
        if (currentState[Coil.station5StoragePositionDown]) {
            Area area = workStationList.get(4).getProcessingArea();
            updateArea(area, Constants.BOX_DECLINED);
        }

        // 站5加工位
        // 站5加工位有升起的箱子
        if (currentState[Coil.station5ProcessingPositionUp]) {
            Area area = workStationList.get(4).getProcessingArea();
            updateArea(area, Constants.BOX_RISED);
        }
        // 站5加工位有降下的箱子
        if (currentState[Coil.station5ProcessingPositionDown]) {
            Area area = workStationList.get(4).getProcessingArea();
            updateArea(area, Constants.BOX_DECLINED);
        }
    }

    // 小车1同步中
    private void syncCar1 () {
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
        if (currentState[Coil.car1AtStartBlockPosition]) {
            car1.setMatch(true);
        }
        // 获取到小车1开始从驱动到站1加工位/站1储备位/站1完成位, 可以开始同步
        if(
            currentState[Coil.car1AtStation1StoragePosition]
            ||currentState[Coil.car1AtStation1ProcessingPosition]
            ||currentState[Coil.car1AtStation1CompletionPosition]
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
                // todo
                // Box 类对应的更新函数需要修改
                if (area.getBox().getStatus() != Constants.BOX_RISED) {
                    area.getBox().setStatus(Constants.BOX_RISED);
                }
            }
        }
        // 站1储料位料盒已经降下
        // todo
        // 补充信号
        if (currentState[Coil.station1StoragePositionDown]) {
            Area area = workStationList.get(0).getStorageArea();
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

        // 站2储备位有料
        // 站2储备位料盒已经升起

        if (currentState[Coil.station2StoragePositionUp]) {
            Area area = workStationList.get(1).getStorageArea();
            if (area.getBox() == null) {
                // 如果当前位置没有箱子, 生成一个升起状态的箱子, 放在此处
                Box box = generateBox(area.x, car1.getY(), Constants.BOX_RISED);
                // todo
                // 这里 area 是引用还是 copy?
                area.setBox(box);
            } else {
                // 如果当前位置已经有箱子
                // 如果箱子的状态是降下的
                // 修改箱子的状态为升起
                // todo
                // Box 类对应的更新函数需要修改
                if (area.getBox().getStatus() != Constants.BOX_RISED) {
                    area.getBox().setStatus(Constants.BOX_RISED);
                }
            }
        }
        // 站2储备位料盒已经降下
        if (currentState[Coil.station2StoragePositionDown]) {
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
        // 站3储备位有料
        // 站3储备位料盒已经升起
        if (currentState[Coil.station3StoragePositionUp]) {
            Area area = workStationList.get(2).getStorageArea();
            updateArea(area, Constants.BOX_RISED);
        }
        // 站3储备位料盒已经降下
        if (currentState[Coil.station3StoragePositionDown]) {
            Area area = workStationList.get(2).getStorageArea();
            updateArea(area, Constants.BOX_DECLINED);
        }
        // 站3加工位有料
        // 站3加工位料盒已经升起
        if (currentState[Coil.station3ProcessingPositionUp]) {
            Area area = workStationList.get(2).getProcessingArea();
            updateArea(area, Constants.BOX_RISED);
        }
        // 站3加工位料盒已经降下
        if (currentState[Coil.station3ProcessingPositionDown]) {
            Area area = workStationList.get(2).getProcessingArea();
            updateArea(area, Constants.BOX_DECLINED);
        }
    }

    private void updateArea (Area area, int status) {
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


    private void fillStation () {
    }

    private Box generateBox (float x, float y, int status) {
        float boxWidth = car1.getWidth();
        float boxHeight = car1.getHeight() - 20;
        Texture texture = new Texture(getApplicationContext(), R.drawable.box);
        Sprite sprite = new Sprite((int)boxWidth, (int)boxHeight);
        sprite.setPivot(0.5f, 0.5f);
        sprite.setPos(x, y);
        sprite.setTexture(texture);
        Box b = new Box(x, y, boxWidth, boxHeight, texture, sprite);
        b.changeSize(status);
        b.render(renderPassSprite);
        return b;
    }
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
    private void updateCar1 (boolean[] booleans) {
        // 更新方向和速度
        // 根据小车现在经过的位置和之前经过的位置来决定小车的运动方向和速度, 后动效果,
        // 如果可以根据挡板升起位置来判断小车的运动方向和速度的话, 可以实现前动.
        // 经过站1
        // 储料位
        if (currentState[Coil.car1AtStation1StoragePosition]) {
            updateDirection (1, Coil.car1AtStation1StoragePosition);
        }
        // 加工位
        if (currentState[Coil.car1AtStation1ProcessingPosition]) {
            updateDirection (1, Coil.car1AtStation1ProcessingPosition);
        }
        // 完成位
        if (currentState[Coil.car1AtStation1CompletionPosition]) {
            updateDirection (1, Coil.car1AtStation1CompletionPosition);
        }
        // 经过站2
        // 储料位
        if (currentState[Coil.car1AtStation2StoragePosition]) {
            updateDirection (1, Coil.car1AtStation2StoragePosition);
        }
        // 加工位
        if (currentState[Coil.car1AtStation2ProcessingPosition]) {
            updateDirection (1, Coil.car1AtStation2ProcessingPosition);
        }
        // 完成位
        if (currentState[Coil.car1AtStation2CompletionPosition]) {
            updateDirection (1, Coil.car1AtStation2CompletionPosition);
        }
        // 经过站3
        // 储料位
        if (currentState[Coil.car1AtStation3StoragePosition]) {
            updateDirection (1, Coil.car1AtStation3StoragePosition);
        }
        // 加工位
        if (currentState[Coil.car1AtStation3ProcessingPosition]) {
            updateDirection (1, Coil.car1AtStation3ProcessingPosition);
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
        if (currentState[Coil.car1HookIn] &&  previousState[Coil.car1HookOut]) {
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
    // 小车左右移动逻辑
    // 如果读取到小车经过某个位置, 修改小车的目的地.
    // (小车的速度应该比实际生产线的小车略快, 因为在生产线上实际小车到达下一个位置时,
    // 小车应该已经到达前一个位置,等待新的目的地).
    // 如果小车已经到达了目的地, 并且当前目的地有挡块升起, 小车应该在这里停止.
    // 需要额外的信号判断小车是否停止.

    private void updateCar1_v2 () {

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
                float boxX=  car1.getX();
                float boxY = car1.getY();
                float boxWidth = car1.getWidth();
                float boxHeight = car1.getHeight() - 20;
                Texture texture = new Texture(getApplicationContext(), R.drawable.box);
                Sprite sprite = new Sprite((int)boxWidth, (int)boxHeight);
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
            }
            // 小车1在站1加工位放下箱子
            if (Float.compare(car1.getX(), Destination.station1ProcessingPosition) == 0) {
                Area area = workStationList.get(0).getProcessingArea();
                if (area.getBox() != null) {
                    Log.e(TAG, "updateCar1_v2: 小车1在站1加工位放下箱子,该位置目前应该没有箱子, 但有");
                }
                area.setBox(car1.getBox());
                car1.setBox(null);
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
         // 小车1驱动到起始位
        if (currentState[Coil.car1AtStartBlockPosition]) {
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
        // 小车2放下箱子


        // 小车1驱动到起始位
        if (currentState[Coil.car1AtStartBlockPosition]) {
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
        }
        // 小车2驱动到下料位
        if (currentState[Coil.car2AtEndPosition]) {
            if (Math.abs(car2.getX() - Destination.finalPosition) < precision) {
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
            if (Math.abs(car1.getX() - Destination.station1StoragePosition) < precision) {
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
            if (Math.abs(car1.getX() - Destination.station1ProcessingPosition) < precision) {
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
            if (Math.abs(car1.getX() - Destination.station1CompletionPosition) < precision) {
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
            if (Math.abs(car1.getX() - Destination.station2StoragePosition) < precision) {
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
            if (Math.abs(car1.getX() - Destination.station2ProcessingPosition) < precision) {
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
            if (Math.abs(car1.getX() - Destination.station2CompletionPosition) < precision) {
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
            if (Math.abs(car1.getX() - Destination.station3StoragePosition) < precision) {
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
            if (Math.abs(car1.getX() - Destination.station3ProcessingPosition) < precision) {
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
    private void updateCar2_v2 () {
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
            if (Math.abs(car2.getX() - Destination.station3ProcessingPosition) < precision) {
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
            if (Math.abs(car2.getX() - Destination.station3CompletionPosition) < precision) {
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
            if (Math.abs(car2.getX() - Destination.station4StoragePosition) < precision) {
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
            if (Math.abs(car2.getX() - Destination.station4ProcessingPosition) < precision) {
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
            if (Math.abs(car2.getX() - Destination.station4CompletionPosition) < precision) {
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
            if (Math.abs(car2.getX() - Destination.station5StoragePosition) < precision) {
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
            if (Math.abs(car2.getX() - Destination.station5ProcessingPosition) < precision) {
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
    private void updateCar_v2 () {
    }

    private void updateStation5_v2 () {

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
    }
    private void updateStation4_v2 () {

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
    }
    private void updateStation3_v2 () {

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
    }
    private void updateStation2_v2 () {

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
    }

    // 站5机械臂同步中
    private void syncHand5 () {
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
    private void syncHand4 () {
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
    private void syncHand3 () {
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
    private void syncHand2 () {
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
    private void syncHand1 () {
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
    private void updateHand1 () {
        Hand hand = workStationList.get(0).getHand();
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
                hand.setStatus(Constants.handRised);
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
    private void updateHand2 () {
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
    private void updateHand3 () {
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
    private void updateHand4 () {
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
    private void updateHand5 () {
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

    private void updateStation1_v2 () {
        // 料盒上升和下降
        // 料盒上升
        // 料盒上升由小车动作控制
        // 在小车放下料盒时, 设置料盒的方向和速度
        // todo
        // 小车放下箱子时, 同时让箱子升起

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
        /*
        Hand hand = ws.getHand();
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
                hand.setStatus(Constants.handRised);
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
        */
    }


    // 用于 run2() 函数, 更新小车的方向和速度.
    private void updateDirection (int index, int flag) {
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
    private int car2GetPosition (int flag) {
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
    private int car1GetPosition (int flag) {
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
    private void updateCar2 (boolean[] booleans) {
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
                if (workStationList.get(3).getStorageArea().getBox() != null ) {
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

    private void updateStation1 (boolean[] booleans) {
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
    private void updateStation2 (boolean[] booleans) {
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
    private void updateStation3 (boolean[] booleans) {
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
    private void updateStation4 (boolean[] booleans) {
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
    private void updateStation5 (boolean[] booleans) {
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

    private void updateHand (boolean[] booleans) {

    }
    private void updateLight (boolean[] booleans) {
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

    public void changeDirection (int reachIndex) {
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
        glHeight  = mSmartGLView.getHeight();
        glWidth = mSmartGLView.getWidth();
        unitHeight = glHeight / 2 / 18;
        unitWidth = glWidth / 11 / 4;
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
        Sprite car1Sprite = new Sprite((int)carWidth, (int)carHeight);
        car1Sprite.setPivot(0.5f, 0.5f);
        car1Sprite.setPos(car1X, car1Y);
        car1Sprite.setTexture(carTexture);
        car1Sprite.setDisplayPriority(carPriority);

        car1 = new Car(car1X, car1Y, carWidth, carHeight, carTexture, car1Sprite);
        car1.setSpeed(0);
        renderPassSprite.addSprite(car1Sprite);

        float car2X = 900;
        float car2Y = glHeight / 2;
        Sprite car2Sprite = new Sprite((int)carWidth, (int)carHeight);
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
        Sprite sprite = new Sprite((int)assemblyLineWidth, (int)assemblyLineHeight);
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

