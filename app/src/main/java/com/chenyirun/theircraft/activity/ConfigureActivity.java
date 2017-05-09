package com.chenyirun.theircraft.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.chenyirun.theircraft.R;

public class ConfigureActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {
    private static final String TAG = "ConfigureActivity";
    public static final String CHUNK_RADIUS = "chunk_radius";
    public static final String SAVE_NAME = "save_name";
    public static final String ACTIVITY_NAME_KEY = "activity_name";
    public static final String ACTIVITY_NAME = "ConfigureActivity";

    private int chunk_radius;
    Bundle bundle;
    Intent intent;

    private SeekBar seekBar;
    private Button button;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config_ui);

        intent = this.getIntent();
        bundle = intent.getExtras();
        chunk_radius = bundle.getInt(CHUNK_RADIUS);

        button = (Button)findViewById(R.id.button_save);
        button.setOnClickListener(clickListener);
        textView = (TextView)findViewById(R.id.textView_chunkRadius);
        seekBar = (SeekBar)findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);
        setChunkRadius(chunk_radius);
    }

    private void setChunkRadius(int chunk_radius){
        // progress starts at 0
        seekBar.setProgress(chunk_radius - 1);
        textView.setText(chunk_radius + "");
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ConfigureActivity.this, MainActivity.class);
            intent.putExtra(CHUNK_RADIUS, chunk_radius);
            intent.putExtra(ACTIVITY_NAME_KEY, ACTIVITY_NAME);
            setResult(RESULT_OK, intent);
            finish();
        }
    };

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
        // chunk_radius no less than 1
        chunk_radius = progress + 1;
        textView.setText(chunk_radius + "");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar){}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar){}
}
