package com.chenyirun.theircraft.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.chenyirun.theircraft.R;
import com.chenyirun.theircraft.activity.fragment.MultiFragment;
import com.chenyirun.theircraft.activity.fragment.SettingsFragment;
import com.chenyirun.theircraft.activity.fragment.SingleFragment;

public class NavigateActivity extends TitleActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigate);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_single);
    }

    private void loadSingleFragment(){
        setTitle("Your Saves");
        showForwardView(R.string.button_new, true);
        showBackwardView(R.string.button_new, false);

        SingleFragment fragment = new SingleFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager. beginTransaction();
        transaction.replace(R.id.content, fragment);
        transaction.commit();
    }

    private void loadMultiFragment(){
        setTitle("Multiplayer Mode(Test)");
        showForwardView(R.string.button_new, false);
        showBackwardView(R.string.button_new, false);

        MultiFragment fragment = new MultiFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager. beginTransaction();
        transaction.replace(R.id.content, fragment);
        transaction.commit();
    }

    private void loadSettingsFragment(){
        setTitle("Configurations");
        showForwardView(R.string.button_new, false);
        showBackwardView(R.string.button_new, false);

        SettingsFragment fragment = new SettingsFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager. beginTransaction();
        transaction.replace(R.id.content, fragment);
        transaction.commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_single:
                    loadSingleFragment();
                    return true;
                case R.id.navigation_multi:
                    loadMultiFragment();
                    return true;
                case R.id.navigation_settings:
                    loadSettingsFragment();
                    return true;
            }
            return false;
        }

    };
}
