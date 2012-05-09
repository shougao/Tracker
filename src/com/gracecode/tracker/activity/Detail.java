package com.gracecode.tracker.activity;

import android.app.LocalActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;
import com.gracecode.tracker.R;
import com.gracecode.tracker.activity.base.Activity;
import com.gracecode.tracker.activity.maps.BaiduMap;
import com.gracecode.tracker.dao.Archive;
import com.gracecode.tracker.dao.ArchiveMeta;
import com.gracecode.tracker.fragment.ArchiveMetaFragment;
import com.gracecode.tracker.fragment.ArchiveMetaTimeFragment;
import com.markupartist.android.widget.ActionBar;

public class Detail extends Activity implements View.OnTouchListener, View.OnClickListener {
    private String archiveFileName;

    private Archive archive;
    private ArchiveMeta archiveMeta;

    private ArchiveMetaFragment archiveMetaFragment;
    private ArchiveMetaTimeFragment archiveMetaTimeFragment;

    private TextView mDescription;
    private LocalActivityManager localActivityManager;
    private TabHost mTabHost;
    private View mMapMask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);

        localActivityManager = new LocalActivityManager(this, false);
        localActivityManager.dispatchCreate(savedInstanceState);

        archiveFileName = getIntent().getStringExtra(Records.INTENT_ARCHIVE_FILE_NAME);
        archive = new Archive(context, archiveFileName, Archive.MODE_READ_WRITE);
        archiveMeta = archive.getMeta();

        mMapMask = findViewById(R.id.map_mask);
        mDescription = (TextView) findViewById(R.id.item_description);
        mTabHost = (TabHost) findViewById(R.id.tabhost);

        archiveMetaFragment = new ArchiveMetaFragment(context, archiveMeta);
        archiveMetaTimeFragment = new ArchiveMetaTimeFragment(context, archiveMeta);
    }

    @Override
    public void onStart() {
        super.onStart();

        String description = archiveMeta.getDescription().trim();
        if (description.length() > 0) {
            mDescription.setText(description);
        } else {
            mDescription.setTextColor(getResources().getColor(R.color.gray));
            mDescription.setText(getString(R.string.no_description));
        }
        mDescription.setOnClickListener(this);

        addArchiveMetaTimeFragment();
        addArchiveMetaFragment();

        actionBar.setTitle(getString(R.string.title_detail));
        actionBar.removeAllActions();
        actionBar.addAction(new ActionBar.Action() {
            @Override
            public int getDrawable() {
                return R.drawable.ic_menu_delete;
            }

            @Override
            public void performAction(View view) {

                helper.showConfirmDialog(
                    getString(R.string.delete),
                    String.format(getString(R.string.sure_to_del), archiveMeta.getName()),
                    new Runnable() {
                        @Override
                        public void run() {
                            if (archive.delete()) {
                                finish();
                            }
                        }
                    },
                    new Runnable() {
                        @Override
                        public void run() {

                        }
                    }
                );

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        localActivityManager.dispatchResume();

        Intent mapIntent = new Intent(this, BaiduMap.class);
        String name = getIntent().getStringExtra(Records.INTENT_ARCHIVE_FILE_NAME);
        mapIntent.putExtra(Records.INTENT_ARCHIVE_FILE_NAME, name);

        TabHost.TabSpec tabSpec =
            mTabHost.newTabSpec("").setIndicator("").setContent(mapIntent);
        mTabHost.setup(localActivityManager);
        mTabHost.addTab(tabSpec);
        mMapMask.setOnTouchListener(this);

        if (!archive.exists()) {
            finish();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        mTabHost.clearAllTabs();
        localActivityManager.removeAllActivities();
        localActivityManager.dispatchPause(isFinishing());
    }


    private void addFragment(int layout, Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(layout, fragment);
        fragmentTransaction.commit();
    }

    private void addArchiveMetaTimeFragment() {
        addFragment(R.id.archive_meta_time_layout, archiveMetaTimeFragment);
    }

    private void addArchiveMetaFragment() {
        addFragment(R.id.archive_meta_layout, archiveMetaFragment);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
//        Intent intent = new Intent(this, BaiduMap.class);
//        intent.putExtra(Records.INTENT_ARCHIVE_FILE_NAME, archiveFileName);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
        return true;
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, Modify.class);
        intent.putExtra(Records.INTENT_ARCHIVE_FILE_NAME, archiveFileName);
        startActivity(intent);
    }
}
