package com.chenyirun.theircraft.activity.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.chenyirun.theircraft.R;
import com.chenyirun.theircraft.model.SaveAndConfig;

public class SettingsFragment extends Fragment {
    private static final String TAG = "SettingsFragment";

    private SeekBar seekBar;
    private TextView textView;
    private ToggleButton toggleButton_autoJump;
    private ToggleButton toggleButton_sightVector;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.config_ui, container, false);

        textView = (TextView)view.findViewById(R.id.textView_chunkRadius);

        seekBar = (SeekBar)view.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
            public void onStartTrackingTouch(SeekBar seekBar){}
            public void onStopTrackingTouch(SeekBar seekBar){}
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                // chunk_radius no less than 1
                SaveAndConfig.chunk_radius = progress + 1;
                textView.setText(SaveAndConfig.chunk_radius + "");
                saveConfig();
            }
        });

        toggleButton_autoJump = (ToggleButton)view.findViewById(R.id.toggleButton_autoJump);
        toggleButton_autoJump.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleButton_autoJump.setChecked(isChecked);
                SaveAndConfig.auto_jump = isChecked;
                saveConfig();
            }
        });

        toggleButton_sightVector = (ToggleButton)view.findViewById(R.id.toggleButton_sightVector);
        toggleButton_sightVector.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleButton_sightVector.setChecked(isChecked);
                SaveAndConfig.sight_vector = isChecked;
                saveConfig();
            }
        });

        seekBar.setProgress(SaveAndConfig.chunk_radius - 1);
        toggleButton_sightVector.setChecked(SaveAndConfig.sight_vector);
        toggleButton_autoJump.setChecked(SaveAndConfig.auto_jump);
        return view;
    }

    private void saveConfig(){
        SharedPreferences sp = getContext().getSharedPreferences("config", Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putInt(SaveAndConfig.KEY_CHUNK_RADIUS, SaveAndConfig.chunk_radius);
        editor.putBoolean(SaveAndConfig.KEY_SIGHT_VECTOR, SaveAndConfig.sight_vector);
        editor.putBoolean(SaveAndConfig.KEY_AUTO_JUMP, SaveAndConfig.auto_jump);
        editor.apply();
    }
}
