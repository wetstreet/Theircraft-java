package com.chenyirun.theircraft.activity.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.chenyirun.theircraft.R;
import com.chenyirun.theircraft.model.SaveAndConfig;

public class SettingsFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {
    private SeekBar seekBar;
    private TextView textView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.config_ui, container, false);

        textView = (TextView)view.findViewById(R.id.textView_chunkRadius);
        seekBar = (SeekBar)view.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);

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
}
