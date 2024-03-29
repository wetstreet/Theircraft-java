package com.chenyirun.theircraft;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Toast;

import com.chenyirun.theircraft.model.SaveAndConfig;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import javax.microedition.khronos.egl.EGLConfig;

public class Renderer implements GvrView.StereoRenderer {
    private static final String TAG = "Renderer";
    private Context context;
    private final World world;

    public Renderer(Context context, Resources resources, SaveAndConfig saveAndConfig) {
        this.context = context;
        world = new World(context, resources, saveAndConfig);
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        GLHelper.drawBackground();
        world.calculateCamera();
        world.setSteveAngles(headTransform);
    }

    @Override
    public void onSurfaceCreated(EGLConfig config) {
        world.onSurfaceCreated();
    }

    @Override
    public void onDrawEye(Eye eye) {
        world.onDrawEye(eye);
    }

    public void onDestroy(){
        world.onDestroy();
    }

    @Override
    public void onRendererShutdown() {}

    @Override
    public void onSurfaceChanged(int width, int height) {}

    @Override
    public void onFinishFrame(Viewport viewport) {}

    public boolean dispatchGenericMotionEvent(MotionEvent ev, InputDevice inputDevice) {
        int eventSource = ev.getSource();
        //Toast.makeText(context, "keycode=" + ev.getActionButton(), Toast.LENGTH_SHORT).show();
        //Toast.makeText(context, "action=" + ev.getAction(), Toast.LENGTH_SHORT).show();
        if ((((eventSource & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) ||
                ((eventSource & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK))
                && ev.getAction() == MotionEvent.ACTION_MOVE) {
            int id = ev.getDeviceId();
            if (-1 != id) {
                if (world.onGenericMotionEvent(ev, inputDevice)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        switch (keyCode){
            case KeyEvent.KEYCODE_VOLUME_UP:
                processVolumeUp(event.getAction(), event.getRepeatCount());
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                processVolumeDown(event.getAction(), event.getRepeatCount());
                return true;
        }
        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) {
            if (event.getRepeatCount() == 0) {
                if(event.getAction() == KeyEvent.ACTION_DOWN){
                    //Toast.makeText(context, "keycode=" + keyCode, Toast.LENGTH_SHORT).show();
                    switch (keyCode){
                        case KeyEvent.KEYCODE_BUTTON_A:
                        case KeyEvent.KEYCODE_BUTTON_THUMBL:
                            world.pressA();
                            return true;
                        case KeyEvent.KEYCODE_BUTTON_X:
                            world.pressX();
                            return true;
                        case KeyEvent.KEYCODE_BUTTON_B:
                            world.pressB();
                            return true;
                        case KeyEvent.KEYCODE_BUTTON_L1:
                            world.pressLB();
                            return true;
                        case KeyEvent.KEYCODE_BUTTON_R1:
                            world.pressRB();
                            return true;
                    }
                }
            }
        }
        return false;
    }

    private void processVolumeUp(int action, int repeatCount) {
        // On long press, we receive a sequence of ACTION_DOWN, ignore all after the first one.
        if (repeatCount > 0) {
            return;
        }

        switch (action) {
            case KeyEvent.ACTION_DOWN:
                world.walk(Steve.WALKING_FORWARD);
                break;
            case KeyEvent.ACTION_UP:
                world.walk(Steve.NOT_WALKING);
                break;
        }
    }

    private void processVolumeDown(int action, int repeatCount) {
        // On long press, we receive a sequence of ACTION_DOWN, ignore all after the first one.
        if (repeatCount > 0) {
            return;
        }

        switch (action) {
            case KeyEvent.ACTION_DOWN:
                world.walk(Steve.WALKING_BACKWARD);
                break;
            case KeyEvent.ACTION_UP:
                world.walk(Steve.NOT_WALKING);
                break;
        }
    }

    public void onCardboardTrigger() {
        world.onCardboardTrigger();
    }
}
