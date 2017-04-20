package com.chenyirun.theircraft;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.chenyirun.theircraft.inputmanagercompat.InputManagerCompat;
import com.chenyirun.theircraft.inputmanagercompat.InputManagerCompat.InputDeviceListener;
import com.google.vr.sdk.base.AndroidCompat;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;

public class MainActivity extends GvrActivity implements InputDeviceListener {
    public MainActivity() {
        super();
    }

    private Vibrator vibrator;
    private Renderer mRenderer;
    private InputManagerCompat mInputManager;
    private InputDevice mInputDevice;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeGvrView();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        mInputManager = InputManagerCompat.Factory.getInputManager(getApplicationContext());
        mInputManager.registerInputDeviceListener(this, null);
    }

    public void initializeGvrView() {
        setContentView(R.layout.common_ui);

        GvrView gvrView = (GvrView) findViewById(R.id.gvr_view);
        //Install a config chooser which will choose a config with at least the specified depthSize
        //and stencilSize, and exactly the specified redSize, greenSize, blueSize and alphaSize.
        gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);

        mRenderer = new Renderer(getApplicationContext(), this.getResources());
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
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy(){
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
