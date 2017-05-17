package com.chenyirun.theircraft.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

import com.chenyirun.theircraft.R;

public class ConfigureActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {
    public static final String CHUNK_RADIUS = "chunk_radius";
    public static final String SIGHT_VECTOR = "sight_vector";

    public static int chunk_radius = 3;
    public static boolean sight_vector = false;

    private SeekBar seekBar;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config_ui);

        textView = (TextView)findViewById(R.id.textView_chunkRadius);
        seekBar = (SeekBar)findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);
        setChunkRadius(chunk_radius);
    }

    private void setChunkRadius(int chunk_radius){
        // progress starts at 0
        seekBar.setProgress(chunk_radius - 1);
    }

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
