package com.chenyirun.theircraft.activity;

import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.chenyirun.theircraft.R;
import com.chenyirun.theircraft.SaveAndConfig;

public class LoadingActivity extends AppCompatActivity {
    private Thread loadingThread;
    private int chunk_radius;
    int id;
    int seed;
    int x;
    int y;
    int z;
    
    private Thread createGameLoader() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Intent toIntent = new Intent(LoadingActivity.this, MainActivity.class);
                toIntent.setFlags(toIntent.FLAG_ACTIVITY_CLEAR_TOP);
                toIntent.putExtra(ConfigureActivity.CHUNK_RADIUS, chunk_radius);
                toIntent.putExtra(SaveAndConfig.ID, id);
                toIntent.putExtra(SaveAndConfig.SEED, seed);
                toIntent.putExtra(SaveAndConfig.STEVE_X, x);
                toIntent.putExtra(SaveAndConfig.STEVE_Y, y);
                toIntent.putExtra(SaveAndConfig.STEVE_Z, z);
                SystemClock.sleep(300);
                startActivity(toIntent);
            }
        };
        return new Thread(runnable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        Intent fromIntent = getIntent();
        chunk_radius = fromIntent.getIntExtra(ConfigureActivity.CHUNK_RADIUS, 3);
        id = fromIntent.getIntExtra(SaveAndConfig.ID, 1);
        seed = fromIntent.getIntExtra(SaveAndConfig.SEED, -1451589742);
        x = fromIntent.getIntExtra(SaveAndConfig.STEVE_X, 0);
        y = fromIntent.getIntExtra(SaveAndConfig.STEVE_Y, 100);
        z = fromIntent.getIntExtra(SaveAndConfig.STEVE_Z, -0);

        loadingThread = createGameLoader();
        loadingThread.start();
    }
}
