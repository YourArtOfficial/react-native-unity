package com.azesmwayreactnativeunity;

import android.app.Activity;
import android.content.res.Configuration;
import android.util.Log;
import android.widget.FrameLayout;

import com.unity3d.player.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class UPlayer {
    private static final String TAG = "UPlayer";
    private static UnityPlayer unityPlayer;

    public UPlayer(final Activity activity, final ReactNativeUnity.UnityPlayerCallback callback) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, InstantiationException {
        super();
        Class<?> _player = null;

        try {
            _player = Class.forName("com.unity3d.player.UnityPlayerForActivityOrService");
        } catch (ClassNotFoundException e) {
            _player = Class.forName("com.unity3d.player.UnityPlayer");
        }

        Constructor<?> constructor = _player.getConstructors()[1];
        unityPlayer = (UnityPlayer) constructor.newInstance(activity, new IUnityPlayerLifecycleEvents() {
            @Override
            public void onUnityPlayerUnloaded() {
                callback.onUnload();
            }

            @Override
            public void onUnityPlayerQuitted() {
                callback.onQuit();
            }
        });
    }

    public static void UnitySendMessage(String gameObject, String methodName, String message) {
        UnityPlayer.UnitySendMessage(gameObject, methodName, message);
    }

    public void pause() {
        unityPlayer.pause();
    }

    public void windowFocusChanged(boolean b) {
        unityPlayer.windowFocusChanged(b);
    }

    public void resume() {
        unityPlayer.resume();
    }

    public void unload() {
        unityPlayer.unload();
    }

    public Object getParentPlayer() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        try {
            Method getFrameLayout = unityPlayer.getClass().getMethod("getFrameLayout");
            FrameLayout frame = (FrameLayout) this.requestFrame();

            return frame.getParent();
        } catch (NoSuchMethodException e) {
            Method getParent = unityPlayer.getClass().getMethod("getParent");

            return getParent.invoke(unityPlayer);
        }
    }

    public void configurationChanged(Configuration newConfig) {
        unityPlayer.configurationChanged(newConfig);
    }

    public void destroy() {
        unityPlayer.destroy();
    }

    public void requestFocusPlayer() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        try {
            Method getFrameLayout = unityPlayer.getClass().getMethod("getFrameLayout");

            FrameLayout frame = (FrameLayout) this.requestFrame();
            frame.requestFocus();
        } catch (NoSuchMethodException e) {
            Method requestFocus = unityPlayer.getClass().getMethod("requestFocus");

            requestFocus.invoke(unityPlayer);
        }
    }

    public FrameLayout requestFrame() {
        try {
            Method getFrameLayout = unityPlayer.getClass().getMethod("getFrameLayout");

            return (FrameLayout) getFrameLayout.invoke(unityPlayer);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // If it is old UnityPlayer, use isInstance() and cast() to bypass incompatible type checks when compiling using newer versions of UnityPlayer
            if (FrameLayout.class.isInstance(unityPlayer)) {
                return FrameLayout.class.cast(unityPlayer);
            } else {
                Log.w(TAG, "UnityPlayer is not a FrameLayout and has no getFrameLayout() — requestFrame() returning null");
                return null;
            }
        }
    }

    public void setZ(float v) {
        try {
            FrameLayout frame = this.requestFrame();
            if (frame != null) {
                frame.setZ(v);
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to set Z on Unity player", e);
        }
    }

    public Object getContextPlayer() {
        return unityPlayer.getContext();
    }
}
