package com.chenyirun.theircraft.activity.fragment;

import android.app.Fragment;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.chenyirun.theircraft.R;
import com.chenyirun.theircraft.audio.receive.AudioReceiver;
import com.chenyirun.theircraft.audio.send.AudioRecorder;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class MultiFragment extends Fragment {
    public static final String TAG = "MultiFragment";
    private Button button_connect;
    private Button button_record;
    private Button button_receive;
    private Button button_record_stop;
    private Button button_receive_stop;
    private TextView textView_message;
    private EditText editText_ip;
    private AudioRecorder audioRecorder;
    private AudioReceiver audioReceiver = new AudioReceiver();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.multi_ui, container, false);

        button_connect = (Button)view.findViewById(R.id.button_connect);
        button_connect.setOnClickListener(connectListener);
        button_record = (Button)view.findViewById(R.id.button_record);
        button_record.setOnClickListener(recordListener);
        button_receive = (Button)view.findViewById(R.id.button_receive);
        button_receive.setOnClickListener(receiveListener);
        button_record_stop = (Button)view.findViewById(R.id.button_record_stop);
        button_record_stop.setOnClickListener(recordStopListener);
        button_receive_stop = (Button)view.findViewById(R.id.button_receive_stop);
        button_receive_stop.setOnClickListener(receiveStopListener);
        textView_message = (TextView)view.findViewById(R.id.textView_message);
        editText_ip = (EditText)view.findViewById(R.id.editText_ip);

        String ip;
        if (isWifiEnabled()){
            ip = getWifiIpAddress();
        } else {
            ip = getLocalIpAddress();
        }
        textView_message.setText(ip);
        editText_ip.setText(ip);

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
            textView_message.setText("connection done");
        }
    };

    private View.OnClickListener recordListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                audioRecorder.startRecording();
            } catch (Exception e){
                Toast.makeText(getContext(), "AudioRecord initialization failed", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private View.OnClickListener receiveListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            audioReceiver.startRecieving();
        }
    };

    private View.OnClickListener recordStopListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            audioRecorder.stopRecording();
        }
    };

    private View.OnClickListener receiveStopListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            audioReceiver.stopRecieving();
        }
    };

    private boolean isWifiEnabled(){
        WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    private String getWifiIpAddress(){
        WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return intToIp(ipAddress);
    }

    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        }
        catch (SocketException ex)
        {
            Log.e("ex:", ex.toString());
        }
        return null;
    }
}
