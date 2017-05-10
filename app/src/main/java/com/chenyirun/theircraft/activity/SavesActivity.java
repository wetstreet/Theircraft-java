package com.chenyirun.theircraft.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.chenyirun.theircraft.DBService;
import com.chenyirun.theircraft.R;

public class SavesActivity extends AppCompatActivity {
    private static final String TAG = "SavesActivity";
    private Button button_config;
    private Button button_start;
    private Button button_new;
    private int chunk_radius = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.saves_ui);

        DBService.setContext(getApplicationContext());

        button_config = (Button)findViewById(R.id.button_config);
        button_start = (Button)findViewById(R.id.button_start);
        button_new = (Button)findViewById(R.id.button_new);
        button_config.setOnClickListener(configListener);
        button_start.setOnClickListener(startListener);
        button_new.setOnClickListener(newListener);
    }

    private View.OnClickListener configListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(SavesActivity.this, ConfigureActivity.class);
            intent.putExtra(ConfigureActivity.CHUNK_RADIUS, chunk_radius);
            startActivityForResult(intent, 0);
        }
    };

    private View.OnClickListener startListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(SavesActivity.this, MainActivity.class);
            intent.putExtra(ConfigureActivity.CHUNK_RADIUS, chunk_radius);
            startActivityForResult(intent, 0);
        }
    };

    private View.OnClickListener newListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(SavesActivity.this, NewActivity.class);
            startActivityForResult(intent, 0);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        Log.i(TAG, "onActivityResult: requestCode=" + requestCode+",resultCode="+resultCode+",intent="+intent);

        if (intent == null){
            return;
        }
        Bundle bundle = intent.getExtras();
        String activity_name = bundle.getString(ConfigureActivity.ACTIVITY_NAME_KEY);
        switch (activity_name){
            case NewActivity.ACTIVITY_NAME:
                String save_name = bundle.getString(ConfigureActivity.SAVE_NAME);
                int seed = bundle.getInt(ConfigureActivity.SEED);
                break;
            case ConfigureActivity.ACTIVITY_NAME:
                chunk_radius = bundle.getInt(ConfigureActivity.CHUNK_RADIUS);
                break;
        }
    }
}
