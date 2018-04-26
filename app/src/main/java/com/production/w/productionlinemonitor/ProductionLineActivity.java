package com.production.w.productionlinemonitor;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.production.w.productionlinemonitor.Helper.Coil;
import com.production.w.productionlinemonitor.Model.Area;
import com.production.w.productionlinemonitor.Model.AssemblyLine;
import com.production.w.productionlinemonitor.Model.Body;
import com.production.w.productionlinemonitor.Model.Box;
import com.production.w.productionlinemonitor.Model.Car;
import com.production.w.productionlinemonitor.Model.Hand;
import com.production.w.productionlinemonitor.Model.Platform;
import com.zgkxzx.modbus4And.requset.ModbusReq;
import com.zgkxzx.modbus4And.requset.OnRequestBack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    private float glHeight;
    private float glWidth;
    private float unitHeight;
    private float unitWidth;

    Area preparationArea;
    Area station1PreparationArea;
    Area station1WorkingArea;
    Area station1CompletionArea;

    Car car1;
    Car car2;
    int previousReachIndex;
    float blockX;

    int index = 0;
    int currentStatus[];
    int previousStatus[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // landscape mode.
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_production_line);

        initSmartGL();
        bind();
        initNavigationDrawer();
        updateView();

        run();
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

                        if (selectedTitle == getString(R.string.main)) {
                            intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                        } else if (selectedTitle == getString(R.string.productionLine)) {

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
        }, 1, 0, 10000);
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

    public void updateAnimation () {
        float deltaTime = renderer.getFrameDuration();

//        Log.e(TAG, "updateAnimation: car1: " + car1.getX() + "," + car1.getSpeed() + "," + car1.getDirection());

        // animation of cars.
        car1.move(deltaTime, blockX);

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
                Log.e(TAG, "run: speed: " + car1.getSpeed());
                Log.e(TAG, "run: car1x: " + car1.getX());
                Log.e(TAG, "run: blockx: " + blockX);
                boolean hasBox = car1.getBox() != null;
                if (hasBox) {
                    Log.e(TAG, "run: has box");
                } else {
                    Log.e(TAG, "run: no box");
                }
                if (index < 24) {
                    ++index;
                }
                int n = currentStatus.length;
                for (int i = 0; i < n; ++i) {
                    Log.e(TAG, "run: " + i + "," + car1.getSpeed());
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
                                }
                                // 到达站1储备位
                                break;
                            case 2:
                                if (currentStatus[i] == 1) {
                                    changeDirection(i);
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
                                    blockX = station1WorkingArea.x;
                                }
                                // 站1加工位挡停到位
                                break;
                            case 5:
                                // 站1加工位挡停回到原位
                                break;
                            case 6:
                                if (currentStatus[i] == 1) {
                                    blockX = station1PreparationArea.x;
                                }
                                // 站1储备位挡停到位
                                break;
                            case 7:
                                // 站1储备位回到原位
                                break;
                            case 8:
                                // 站1加工位上料盒到位
                                break;
                            case 9:
                                // 站1加工位下料盒到位
                                break;
                            case 10:
                                // 站1储备位上料盒到位
                                break;
                            case 11:
                                // 站1储备位下料盒到位
                                break;
                            case 12:
                                // 小车1出钩
                                if (currentStatus[i] == 1) {
                                    float boxX=  car1.getX();
                                    float boxY = car1.getY();
                                    float boxWidth = car1.getWidth();
                                    float boxHeight = car1.getHeight();
                                    Texture texture = new Texture(getApplicationContext(), R.drawable.box);
                                    Sprite sprite = new Sprite((int)boxWidth, (int)boxHeight);
                                    sprite.setPos(boxX, boxY);
                                    sprite.setTexture(texture);
                                    Box b = new Box(boxX, boxY, boxWidth, boxHeight, texture, sprite);
                                    b.render(renderPassSprite);
                                    car1.setBox(b);
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
    public void changeDirection (int reachIndex) {
        if (reachIndex > previousReachIndex) {
            car1.setDirection(Constants.RIGHT);
            car1.setSpeed(600);
            Log.e(TAG, "changeDirection: " + reachIndex);
        } else if (reachIndex < previousReachIndex) {
            car1.setDirection(Constants.LEFT);
            car1.setSpeed(600);
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
        // load resources.
        glHeight  = mSmartGLView.getHeight();
        glWidth = mSmartGLView.getWidth();
        unitHeight = glHeight / 2 / 18;
        unitWidth = glWidth / 11 / 4;

        preparationArea = new Area();
        station1PreparationArea = new Area();
        station1WorkingArea = new Area();
        station1CompletionArea = new Area();

        preparationArea.x = 0;
        preparationArea.width = unitWidth;


        // prepare car.
        car1 = new Car(0, glHeight / 2, unitWidth, unitHeight * 2);
        car1.setSpeed(0);

        Log.e(TAG, "initSmartGL: " + glHeight + "," + glWidth);
        Log.e(TAG, "initSmartGL: " + unitWidth + "," + unitHeight);

        float assemblyLineX = glWidth / 2;
        float assemblyLineY = glHeight / 2;
        float assemblyLineWidth = glWidth;
        float assemblyLineHeight = unitHeight * 2;

        float bodyX = glWidth / 2;
        float bodyY = glHeight / 2;
        float bodyWidth = unitWidth;
        float bodyHeight = unitHeight * 12;

        float platformTopWidth = unitWidth * 2;
        float platformTopHeight = unitHeight * 3;
        float platformTopX = bodyX ;
        float platformTopY = bodyY - bodyHeight / 2 - platformTopHeight / 2 + unitHeight / 2;

        float platformBottomWidth = platformTopWidth;
        float platformBottomHeight = platformTopHeight;
        float platformBottomX = bodyX;
        float platformBottomY = bodyY + bodyHeight / 2 + platformBottomHeight / 2 - unitHeight / 2;

        float handWidth = unitWidth * 1;
        float handHeight = unitHeight * 2;
        float handX = bodyX - bodyWidth / 2 - handWidth / 2;
        float handY = bodyY;

        int assemblyLinePriority = 10;
        int platformPriority = 5;
        int bodyPriority = 0;
        int handPriority = 0;

        station1PreparationArea.x = bodyX - unitWidth - unitWidth;
        station1PreparationArea.width = unitWidth;
        Log.e(TAG, "onPrepareView: station1 preparation area: " + station1PreparationArea.x);

        station1WorkingArea.x = bodyX - unitWidth;
        station1WorkingArea.width = unitWidth;
        Log.e(TAG, "onPrepareView: station1 working area: " + station1WorkingArea.x);

        station1CompletionArea.x = bodyX + unitWidth;
        station1CompletionArea.width = unitWidth;
        Log.e(TAG, "onPrepareView: station1 completion area: " + station1CompletionArea.x);

        renderer = smartGLView.getSmartGLRenderer();
        renderPassSprite = new RenderPassSprite();
        renderer.addRenderPass(renderPassSprite);  // add it only once for all Sprites

        Texture texture = new Texture(getApplicationContext(), R.drawable.assembly_line);
        Sprite sprite = new Sprite((int)assemblyLineWidth, (int)assemblyLineHeight);
        sprite.setPivot(0.5f, 0.5f);
        sprite.setPos(assemblyLineX, assemblyLineY);
        sprite.setDisplayPriority(assemblyLinePriority);
        sprite.setTexture(texture);
        AssemblyLine assemblyLine = new AssemblyLine(assemblyLineX, assemblyLineY, assemblyLineWidth, assemblyLineHeight, texture, sprite);
        assemblyLine.render(renderPassSprite);

        texture = new Texture(getApplicationContext(), R.drawable.body);
        sprite = new Sprite((int)bodyWidth, (int)bodyHeight);
        sprite.setPivot(0.5f, 0.5f);
        sprite.setPos(bodyX, bodyY);
        sprite.setDisplayPriority(bodyPriority);
        sprite.setTexture(texture);
        Body body = new Body(bodyX, bodyY, bodyWidth, bodyHeight, texture, sprite);
        body.render(renderPassSprite);

        texture = new Texture(getApplicationContext(), R.drawable.platform);
        sprite = new Sprite((int)platformTopWidth, (int) platformTopHeight);
        sprite.setPivot(0.5f, 0.5f);
        sprite.setPos(platformTopX, platformTopY);
        sprite.setDisplayPriority(platformPriority);
        sprite.setTexture(texture);
        Platform platformTop = new Platform(platformTopX, platformTopY, platformTopWidth, platformTopHeight, texture, sprite);
        platformTop.render(renderPassSprite);

        texture = new Texture(getApplicationContext(), R.drawable.platform_inverse);
        sprite = new Sprite((int)platformBottomWidth, (int)platformBottomHeight);
        sprite.setPivot(0.5f, 0.5f);
        sprite.setPos(platformBottomX, platformBottomY);
        sprite.setDisplayPriority(platformPriority);
        sprite.setTexture(texture);
        Platform paltformBottom = new Platform(platformBottomX, platformBottomY, platformBottomWidth, platformBottomHeight, texture, sprite);
        paltformBottom.render(renderPassSprite);

        texture = new Texture(getApplicationContext(), R.drawable.hand);
        sprite = new Sprite((int)handWidth, (int)handHeight);
        sprite.setPivot(0.5f, 0.5f);
        sprite.setPos(handX, handY);
        sprite.setDisplayPriority(handPriority);
        sprite.setTexture(texture);
        Hand hand = new Hand(handX, handY, handWidth, handHeight, texture, sprite);
        hand.render(renderPassSprite);
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

