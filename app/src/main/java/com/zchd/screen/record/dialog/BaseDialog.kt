package com.zchd.screen.record.dialog

import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.WindowManager

/**
 * Copyright (C), 2020-2022, 中传互动（湖北）信息技术有限公司
 * Author: HeChao
 * Date: 2022/2/15 11:09
 * Description:
 */
internal open class BaseDialog(context: Context) : Dialog(context) {

    protected val mContext = context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCanceledOnTouchOutside(false)
        setContentView(getLayoutId())
        if (getAnimations() > 0) {
            window?.setWindowAnimations(getAnimations())
        }
        if (isLarge()) {
            window?.attributes = window?.attributes?.apply {
                gravity = Gravity.CENTER
                height = (getScreenSize(context)[1] * 0.8).toInt()
            }
        }
        if (isShowAppBackground()) {
            if (Build.VERSION.SDK_INT >= 26) {
                window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
            } else {
                window?.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
            }
        }
        initViews()
    }

    open fun getLayoutId(): Int = 0

    open fun getAnimations(): Int = -1

    open fun initViews() {
    }

    protected open fun isLarge(): Boolean = false
    protected open fun isShowAppBackground(): Boolean = false

    fun getScreenSize(context: Context): IntArray {
        val size = IntArray(2)
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val metrics = DisplayMetrics()
        display.getMetrics(metrics)
        size[0] = metrics.widthPixels
        size[1] = metrics.heightPixels
        return size
    }

    /**
     * 获取是否为横屏
     */
    protected fun getOrientationLandscape(): Boolean {
        val mConfiguration = context.resources.configuration
        val orientation = mConfiguration.orientation
        return orientation == Configuration.ORIENTATION_LANDSCAPE
    }

}