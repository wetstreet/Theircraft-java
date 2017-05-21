package com.chenyirun.theircraft.activity.fragment;

import android.app.Fragment;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.chenyirun.theircraft.R;
import com.chenyirun.theircraft.audio.send.AudioRecorder;

public class MultiFragment extends Fragment {
    private Button button_connect;
    private Button button_record;
    private TextView textView_message;
    private EditText editText_ip;
    private AudioRecorder audioRecorder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.multi_ui, container, false);

        button_connect = (Button)view.findViewById(R.id.button_connect);
        button_connect.setOnClickListener(connectListener);
        button_record = (Button)view.findViewById(R.id.button_record);
        button_record.setOnClickListener(recordListener);
        textView_message = (TextView)view.findViewById(R.id.textView_message);
        editText_ip = (EditText)view.findViewById(R.id.editText_ip);

        // get Wifi service
        WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        // if Wifi is not enabled, enable it
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ip = intToIp(ipAddress);
        textView_message.setText(ip);

        return view;
    }

    private String intToIp(int i) {

        return (i & 0xFF ) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ( i >> 24 & 0xFF) ;
    }

    private View.OnClickListener connectListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String ip = editText_ip.getText().toString();
            audioRecorder = new AudioRecorder(ip);
        }
    };

    private View.OnClickListener recordListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            audioRecorder.startRecording();
        }
    };
}
