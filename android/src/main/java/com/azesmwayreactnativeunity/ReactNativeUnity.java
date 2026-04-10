package com.azesmwayreactnativeunity;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import java.lang.reflect.InvocationTargetException;

public class ReactNativeUnity {
    private static final String TAG = "ReactNativeUnity";

    private static UPlayer unityPlayer;
    public static boolean _isUnityReady;
    public static boolean _isUnityPaused;
    public static boolean _fullScreen;

    public static UPlayer getPlayer() {
        if (!_isUnityReady) {
            return null;
        }
        return unityPlayer;
    }

    public static boolean isUnityReady() {
        return _isUnityReady;
    }

    public static boolean isUnityPaused() {
        return _isUnityPaused;
    }

    public static void createPlayer(final Activity activity, final UnityPlayerCallback callback) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        if (unityPlayer != null) {
            callback.onReady();

            return;
        }

        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.getWindow().setFormat(PixelFormat.RGBA_8888);
                    int flag = activity.getWindow().getAttributes().flags;
                    boolean fullScreen = false;
                    if ((flag & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN) {
                        fullScreen = true;
                    }

                    try {
                        unityPlayer = new UPlayer(activity, callback);
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        Log.e(TAG, "Failed to create Unity player", e);
                    }

                    if (unityPlayer == null) {
                        Log.e(TAG, "Unity player was not created — aborting initialization");
                        return;
                    }

                    // Defer post-init work to avoid blocking the UI thread.
                    // The old Thread.sleep(1000) caused ANR risk and rendering issues.
                    final boolean wasFullScreen = fullScreen;
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                addUnityViewToBackground();
                            } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                                Log.e(TAG, "Failed to add Unity view to background", e);
                            }

                            unityPlayer.windowFocusChanged(true);

                            try {
                                unityPlayer.requestFocusPlayer();
                            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                                Log.w(TAG, "Failed to request focus on Unity player", e);
                            }

                            unityPlayer.resume();

                            if (!wasFullScreen) {
                                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                            }

                            _isUnityReady = true;

                            try {
                                callback.onReady();
                            } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                                Log.e(TAG, "Callback onReady failed", e);
                            }
                        }
                    }, 1000);
                }
            });
        }
    }

    public static void pause() {
        if (unityPlayer != null) {
            unityPlayer.pause();
            _isUnityPaused = true;
        }
    }

    public static void resume() {
        if (unityPlayer != null) {
            unityPlayer.resume();
            _isUnityPaused = false;
        }
    }

    public static void unload() {
        if (unityPlayer != null) {
            unityPlayer.unload();
            _isUnityPaused = false;
        }
    }

    public static void addUnityViewToBackground() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        if (unityPlayer == null) {
            return;
        }

        android.widget.FrameLayout frame = unityPlayer.requestFrame();
        if (frame == null) {
            Log.w(TAG, "requestFrame() returned null — cannot add Unity view to background");
            return;
        }

        if (unityPlayer.getParentPlayer() != null) {
            // NOTE: If we're being detached as part of the transition, make sure
            // to explicitly finish the transition first, as it might still keep
            // the view's parent around despite calling `removeView()` here. This
            // prevents a crash on an `addContentView()` later on.
            // Otherwise, if there's no transition, it's a no-op.
            // See https://stackoverflow.com/a/58247331
            ((ViewGroup) unityPlayer.getParentPlayer()).endViewTransition(frame);
            ((ViewGroup) unityPlayer.getParentPlayer()).removeView(frame);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            unityPlayer.setZ(-1f);
        }

        final Activity activity = ((Activity) unityPlayer.getContextPlayer());
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(1, 1);
        activity.addContentView(frame, layoutParams);
    }

    public static void addUnityViewToGroup(ViewGroup group) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (unityPlayer == null) {
            return;
        }

        android.widget.FrameLayout frame = unityPlayer.requestFrame();
        if (frame == null) {
            Log.w(TAG, "requestFrame() returned null — cannot add Unity view to group");
            return;
        }

        if (unityPlayer.getParentPlayer() != null) {
            ((ViewGroup) unityPlayer.getParentPlayer()).removeView(frame);
        }

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        group.addView(frame, 0, layoutParams);
        unityPlayer.windowFocusChanged(true);
        unityPlayer.requestFocusPlayer();
        unityPlayer.resume();
    }

    public interface UnityPlayerCallback {
        void onReady() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException;

        void onUnload();

        void onQuit();
    }
}
