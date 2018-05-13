package com.production.w.productionlinemonitor;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;

public class WorkStationListActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "extra_id";

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private DrawerLayout mDrawerLayout;

    private static final String TAG = "WorkStationListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_work_station_list);

        initNavigationDrawer();
        init();
    }
    // 更新菜单栏
    public void initNavigationDrawer () {

        mDrawerLayout = findViewById(R.id.wsl_drawer_layout);

        NavigationView navigationView = findViewById(R.id.wsl_nav_view);
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
    // 初始化列表
    public void init () {
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new MyAdapter(new String[]{"工站1", "工站2", "工站3", "工站4", "工站5"}, getApplicationContext());
        mRecyclerView.setAdapter(mAdapter);
    }
}
