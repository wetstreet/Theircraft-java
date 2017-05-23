package com.chenyirun.theircraft.activity.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.chenyirun.theircraft.R;
import com.chenyirun.theircraft.model.SaveAndConfig;

public class SettingsFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {
    private static final String TAG = "DBService";

    private SeekBar seekBar;
    private TextView textView;
    private ToggleButton toggleButton_autoJump;
    private ToggleButton toggleButton_sightVector;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.config_ui, container, false);

        textView = (TextView)view.findViewById(R.id.textView_chunkRadius);
        seekBar = (SeekBar)view.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);
        toggleButton_autoJump = (ToggleButton)view.findViewById(R.id.toggleButton_autoJump);
        toggleButton_sightVector = (ToggleButton)view.findViewById(R.id.toggleButton_sightVector);

        readConfig();

        return view;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
        // chunk_radius no less than 1
        SaveAndConfig.chunk_radius = progress + 1;
        textView.setText(SaveAndConfig.chunk_radius + "");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar){}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar){}

    @Override
    public void onDestroy(){
        SaveAndConfig.auto_jump = toggleButton_autoJump.isChecked();
        SaveAndConfig.sight_vector = toggleButton_sightVector.isChecked();
        saveConfig();
        super.onDestroy();
    }

    private void saveConfig(){
        SharedPreferences sp = getContext().getSharedPreferences("config", Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putInt(SaveAndConfig.KEY_CHUNK_RADIUS, SaveAndConfig.chunk_radius);
        editor.putBoolean(SaveAndConfig.KEY_SIGHT_VECTOR, SaveAndConfig.sight_vector);
        editor.putBoolean(SaveAndConfig.KEY_AUTO_JUMP, SaveAndConfig.auto_jump);
        editor.apply();
    }

    private void readConfig(){
        SharedPreferences sp = getContext().getSharedPreferences("config", Context.MODE_PRIVATE);
        int chunk_radius = sp.getInt(SaveAndConfig.KEY_CHUNK_RADIUS, 3);
        Log.i(TAG, "readConfig: before="+SaveAndConfig.chunk_radius);
        seekBar.setProgress(chunk_radius - 1);
        Log.i(TAG, "readConfig: after="+SaveAndConfig.chunk_radius);
        SaveAndConfig.sight_vector = sp.getBoolean(SaveAndConfig.KEY_SIGHT_VECTOR, false);
        toggleButton_sightVector.setChecked(SaveAndConfig.sight_vector);
        SaveAndConfig.auto_jump = sp.getBoolean(SaveAndConfig.KEY_AUTO_JUMP, false);
        toggleButton_autoJump.setChecked(SaveAndConfig.auto_jump);
    }
}
