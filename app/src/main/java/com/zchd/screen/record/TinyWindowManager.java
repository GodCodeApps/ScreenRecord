package com.zchd.screen.record;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * @PackageName : com.zchd.haogames.sdk.utils
 * @Author : Waiting
 * @Date :   1/17/21 3:37 PM
 */
public class TinyWindowManager {

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mParams;
    private WeakReference<View> mWeakReference;
    private boolean hasPermission;/*判断是否有悬浮窗权限*/
    private List<Class> mSupportClass = new ArrayList<>();/*指定支持显示的Activity*/

    private TinyWindowManager() {
    }

    private static TinyWindowManager sInstance;

    public static TinyWindowManager getInstance() {
        if (sInstance == null) {
            synchronized (TinyWindowManager.class) {
                if (sInstance == null) {
                    sInstance = new TinyWindowManager();
                    sInstance.hasPermission = sInstance.checkPermission();
                }
            }
        }
        return sInstance;
    }

    public void initial(Context context) {
        hasPermission = checkPermission();
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int type;
        if (hasPermission) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
            }
        } else {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
        }
        mParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.RGBA_8888);
        mParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
    }

    private boolean checkPermission() {
        return true;
//        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(context);
    }

    public WindowManager getWindowManager() {
        return mWindowManager;
    }

    public WindowManager.LayoutParams getWindowManagerParameter() {
        return mParams;
    }

    public boolean hasPermission() {
        return hasPermission;
    }

    public void addActivity(Class className) {
        if (!mSupportClass.contains(className)) {
            mSupportClass.add(className);
        }
    }

    public List<Class> getSupportActivity() {
        return mSupportClass;
    }

    public synchronized void addView(View view) {
        addView(view, mParams);
    }

    public synchronized void addView(View view, WindowManager.LayoutParams params) {
        mWeakReference = new WeakReference<View>(view);
        if (mWindowManager != null) {
            this.mWindowManager.addView(view, params);
        }
    }

    public void updateViewLayout(View view, WindowManager.LayoutParams params) {
        if (mWindowManager != null) {
            mWindowManager.updateViewLayout(view, params);
        }
    }

    public synchronized View getTargetView() {
        if (mWeakReference != null) {
            return mWeakReference.get();
        }
        return null;
    }

    public void visible() {
        if (mWeakReference != null && mWeakReference.get() != null) {
            mWeakReference.get().setVisibility(View.VISIBLE);
        }
    }

    public void gone() {
        if (mWeakReference != null && mWeakReference.get() != null) {
            mWeakReference.get().setVisibility(View.GONE);
        }
    }

    public synchronized void removeView() {
        if (mWeakReference != null && mWeakReference.get() != null) {
            removeView(mWeakReference.get());
        }
    }

    public synchronized void removeView(View view) {
        if (mWindowManager != null && view.isAttachedToWindow()) {
            mWindowManager.removeViewImmediate(view);
            mWeakReference.clear();
            mWeakReference = null;
        }
    }
}
