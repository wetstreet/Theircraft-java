package com.chenyirun.theircraft.activity;

import android.content.Intent;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.chenyirun.theircraft.R;
import com.chenyirun.theircraft.model.SaveAndConfig;

public class LoadingActivity extends AppCompatActivity {
    private TextView textView_loading;

    private Thread loadingThread;
    private int id;
    private int seed;
    private int x;
    private int y;
    private int z;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        textView_loading = (TextView)findViewById(R.id.textView_loading);

        Intent fromIntent = getIntent();
        id = fromIntent.getIntExtra(SaveAndConfig.ID, 1);
        seed = fromIntent.getIntExtra(SaveAndConfig.SEED, -1451589742);
        x = fromIntent.getIntExtra(SaveAndConfig.STEVE_X, 0);
        y = fromIntent.getIntExtra(SaveAndConfig.STEVE_Y, 100);
        z = fromIntent.getIntExtra(SaveAndConfig.STEVE_Z, -0);

        loadingThread = createGameLoader();
        loadingThread.start();
    }

    private Thread createGameLoader() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Intent toIntent = new Intent(LoadingActivity.this, MainActivity.class);
                toIntent.putExtra(SaveAndConfig.ID, id);
                toIntent.putExtra(SaveAndConfig.SEED, seed);
                toIntent.putExtra(SaveAndConfig.STEVE_X, x);
                toIntent.putExtra(SaveAndConfig.STEVE_Y, y);
                toIntent.putExtra(SaveAndConfig.STEVE_Z, z);
                SystemClock.sleep(400);
                startActivityForResult(toIntent, 0);
            }
        };
        return new Thread(runnable);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        if (resultCode == RESULT_CANCELED){
            return;
        }
        textView_loading.setText("Closing...");
    }
}
