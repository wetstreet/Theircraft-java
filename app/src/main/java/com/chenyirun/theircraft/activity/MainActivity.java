package com.chenyirun.theircraft.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.chenyirun.theircraft.R;
import com.chenyirun.theircraft.Renderer;
import com.chenyirun.theircraft.inputmanagercompat.InputManagerCompat;
import com.chenyirun.theircraft.inputmanagercompat.InputManagerCompat.InputDeviceListener;
import com.google.vr.sdk.base.AndroidCompat;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;

public class MainActivity extends GvrActivity implements InputDeviceListener {
    private static final String TAG = "MainActivity";
    public MainActivity() {
        super();
    }

    private Vibrator vibrator;
    private Renderer mRenderer;
    private InputManagerCompat mInputManager;
    private InputDevice mInputDevice;

    private static int CHUNK_RADIUS;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        CHUNK_RADIUS = intent.getIntExtra(ConfigureActivity.CHUNK_RADIUS, 3);
        Log.i(TAG, "onCreate: chunk radius="+ CHUNK_RADIUS);

        initializeGvrView();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        mInputManager = InputManagerCompat.Factory.getInputManager(getApplicationContext());
        mInputManager.registerInputDeviceListener(this, null);
    }

    public void initializeGvrView() {
        setContentView(R.layout.game_ui);

        GvrView gvrView = (GvrView) findViewById(R.id.gvr_view);
        //Install a config chooser which will choose a config with at least the specified depthSize
        //and stencilSize, and exactly the specified redSize, greenSize, blueSize and alphaSize.
        gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);

        mRenderer = new Renderer(getApplicationContext(), this.getResources(), CHUNK_RADIUS);
        gvrView.setRenderer(mRenderer);

        gvrView.setTransitionViewEnabled(true);

        // Enable Cardboard-trigger feedback with Daydream headsets. This is a simple way of supporting
        // Daydream controller input for basic interactions using the existing Cardboard trigger API.
        gvrView.enableCardboardTriggerEmulation();

        if (gvrView.setAsyncReprojectionEnabled(true)) {
            // Async reprojection decouples the app framerate from the display framerate,
            // allowing immersive interaction even at the throttled clockrates set by
            // sustained performance mode.
            AndroidCompat.setSustainedPerformanceMode(this, true);
        }

        setGvrView(gvrView);
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause: ");
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy(){
        Log.i(TAG, "onDestroy: ");
        mRenderer.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onCardboardTrigger() {
        // always give user feedback
        vibrator.vibrate(50);
        mRenderer.onCardboardTrigger();
    }

    @Override
    public void onInputDeviceAdded(int deviceId) {
        vibrator.vibrate(50);
        mInputDevice = InputDevice.getDevice(deviceId);
    }

    @Override
    public void onInputDeviceRemoved(int deviceId) {
        vibrator.vibrate(50);
    }

    @Override
    public void onInputDeviceChanged(int deviceId) {
        vibrator.vibrate(50);
        mInputDevice = InputDevice.getDevice(deviceId);
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent ev) {
        if (mRenderer.dispatchGenericMotionEvent(ev, mInputDevice)){
            return true;
        } else {
            return super.dispatchGenericMotionEvent(ev);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mRenderer.dispatchKeyEvent(event)){
            return true;
        } else{
            return super.dispatchKeyEvent(event);
        }
    }
}
