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
import com.production.w.productionlinemonitor.Helper.Constants;
import com.production.w.productionlinemonitor.Model.Area;
import com.production.w.productionlinemonitor.Model.AssemblyLine;
import com.production.w.productionlinemonitor.Model.Body;
import com.production.w.productionlinemonitor.Model.Box;
import com.production.w.productionlinemonitor.Model.Car;
import com.production.w.productionlinemonitor.Model.Hand;
import com.production.w.productionlinemonitor.Model.Platform;
import com.production.w.productionlinemonitor.Model.WorkStation;
import com.zgkxzx.modbus4And.requset.ModbusReq;
import com.zgkxzx.modbus4And.requset.OnRequestBack;

import org.w3c.dom.Text;

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

    public static float glHeight;
    public static float glWidth;
    public static float unitHeight;
    public static float unitWidth;
    public static float bodyHeight;


    Area preparationArea;
    Area station1PreparationArea;
    Area station1WorkingArea;
    Area station1CompletionArea;

    Car car1;
    Car car2;
    WorkStation ws;
    Hand hand;
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

        initNavigationDrawer();
        bind();
        updateView();

        initSmartGL();
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
        if (ws.getWorkingArea().getBox() != null) {
            ws.getWorkingArea().getBox().update(deltaTime);
        }
        if (ws.getPreparationArea().getBox() != null) {
            ws.getPreparationArea().getBox().update(deltaTime);
        }
        ws.getHand().update(deltaTime);

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
                                        ws.getPreparationArea().setBox(car1.getBox());
                                    } else if (currentStatus[6] == 1 && car1.getBox() == null && ws.getPreparationArea().getBox() != null) {
                                        if (currentStatus[12] == 1) {
                                            car1.setBox(ws.getPreparationArea().getBox());
                                            ws.getPreparationArea().setBox(null);
                                        }
                                    }
                                }
                                // 到达站1储备位
                                break;
                            case 2:
                                if (currentStatus[i] == 1) {
                                    changeDirection(i);
                                    if (currentStatus[4] == 1 && car1.getBox() != null) {
                                        ws.getWorkingArea().setBox(car1.getBox());
                                    } else if (currentStatus[4] == 1 && car1.getBox() == null && ws.getWorkingArea().getBox() != null) {
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
                                    blockX = ws.getWorkingArea().x;
                                }
                                // 站1加工位挡停到位
                                break;
                            case 5:
                                // 站1加工位挡停回到原位
                                break;
                            case 6:
                                if (currentStatus[i] == 1) {
                                    blockX = ws.getPreparationArea().x;
                                }
                                // 站1储备位挡停到位
                                break;
                            case 7:
                                // 站1储备位回到原位
                                break;
                            case 8:
                                if (currentStatus[i] == 1) {
                                    if (ws.getWorkingArea().getBox() != null) {
                                        ws.getWorkingArea().getBox().setStatus(Constants.BOX_RISING);
                                    }
                                }
                                // 站1加工位上料盒到位
                                break;
                            case 9:
                                if (currentStatus[i] == 1) {
                                    if (ws.getWorkingArea().getBox() != null) {
                                        ws.getWorkingArea().getBox().setStatus(Constants.BOX_DECLING);
                                    }
                                }
                                // 站1加工位下料盒到位
                                break;
                            case 10:
                                if (currentStatus[i] == 1) {
                                    if (ws.getPreparationArea().getBox() != null) {
                                        ws.getPreparationArea().getBox().setStatus(Constants.BOX_RISING);
                                    }
                                }
                                // 站1储备位上料盒到位
                                break;
                            case 11:
                                if (currentStatus[i] == 1) {
                                    if (ws.getPreparationArea().getBox() != null) {
                                        ws.getPreparationArea().getBox().setStatus(Constants.BOX_DECLING);
                                    }
                                }
                                // 站1储备位下料盒到位
                                break;
                            case 12:
                                // 小车1出钩
                                if (currentStatus[i] == 1) {
                                    if (currentStatus[2] == 1 &&  currentStatus[4] == 1 && ws.getWorkingArea().getBox() != null && car1.getBox() == null) {
                                        car1.setBox(ws.getWorkingArea().getBox());
                                        ws.getWorkingArea().setBox(null);
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
                                    ws.getHand().setStatus(Constants.handRising);
                                }
                                break;
                            case 18:
                                if (currentStatus[i] == 1) {
                                    ws.getHand().setStatus(Constants.handDeclining);
                                }
                                break;
                            case 19:
                                if (currentStatus[i] == 1) {
                                    ws.getHand().setStatus(Constants.handDeclining);
                                }
                                break;
                            case 20:
                                if (currentStatus[i] == 1) {
                                    ws.getHand().setStatus(Constants.handDeclining);
                                }
                                break;
                            case 21:
                                if (currentStatus[i] == 1) {
                                    if (ws.getHand().getInitY() == ws.getHand().getLeftEndY()) {
                                       ws.getHand().setStatus(Constants.handRightShifting);
                                    } else if (ws.getHand().getInitY() == ws.getHand().getRightEndY()) {
                                        ws.getHand().setStatus(Constants.handLeftShifting);
                                    } else {

                                    }
                                }
                                break;
                            case 22:
                                if (currentStatus[i] == 1) {
                                    ws.getHand().setStatus(Constants.handLeftShifting);
                                }
                                break;
                            case 23:
                                if (currentStatus[i] == 1) {
                                    ws.getHand().setStatus(Constants.handRightShifting);
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

        renderer = smartGLView.getSmartGLRenderer();
        renderPassSprite = new RenderPassSprite();
        renderer.addRenderPass(renderPassSprite);  // add it only once for all Sprites

        // load resources.
        glHeight  = mSmartGLView.getHeight();
        glWidth = mSmartGLView.getWidth();
        unitHeight = glHeight / 2 / 18;
        unitWidth = glWidth / 11 / 4;
        unitWidth += 10;
        unitHeight += 10;

        ws = new WorkStation(getApplicationContext() ,glWidth/ 4, glHeight / 2);
        ws.init(glWidth, glHeight, unitWidth, unitHeight);
        ws.render(renderPassSprite);

        preparationArea = new Area();
        preparationArea.x = 0;
        preparationArea.width = unitWidth;

        // prepare car.
        float carX = 0;
        float carY = glHeight / 2;
        float carWidth = unitWidth;
        float carHeight = unitHeight * 2 + 20;

        Texture carTexture = new Texture(getApplicationContext(), R.drawable.car);
        Sprite carSprite = new Sprite((int)carWidth, (int)carHeight);
        carSprite.setPivot(0.5f, 0.5f);
        carSprite.setPos(0, glHeight / 2);
        carSprite.setTexture(carTexture);
        carSprite.setDisplayPriority(100);
        car1 = new Car(carX, carY, carWidth, carHeight, carTexture, carSprite);
        car1.setSpeed(0);
        renderPassSprite.addSprite(carSprite);

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

