package com.production.w.productionlinemonitor;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.production.w.productionlinemonitor.Model.AssemblyLine;
import com.production.w.productionlinemonitor.Model.Body;
import com.production.w.productionlinemonitor.Model.Box;
import com.production.w.productionlinemonitor.Model.Hand;
import com.production.w.productionlinemonitor.Model.Platform;

import java.util.ArrayList;
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
    private float glHeight;
    private float glWidth;
    private float unitHeight;
    private float unitWidth;


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
    }
    public void initSmartGL () {
        mSmartGLView = (SmartGLView) findViewById(R.id.smartGLView);
        mSmartGLView.setDefaultRenderer(this);
        mSmartGLView.setController(this);
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

        SmartGLRenderer renderer = smartGLView.getSmartGLRenderer();
        RenderPassSprite renderPassSprite = new RenderPassSprite();
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
    public void onTick(SmartGLView smartGLView) {

    }

    @Override
    public void onTouchEvent(SmartGLView smartGLView, TouchHelperEvent touchHelperEvent) {

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
    public void updateView () {
        updateStatus();
        updateTime();
        updateSpeed();
        updateCncStatus();

        // update animations.
    }
    public void updateStatus () {

    }
    public void updateTime () {

    }
    public void updateCncStatus () {

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
    public void init () {

    }
}

