package com.zchd.screen.record.interfaces

import android.graphics.Bitmap

/**
 * Copyright (C), 2020-2021, 中传互动（湖北）信息技术有限公司
 * @Author: pym
 * @Date: 2022/2/16:10:58
 * @Description:
 */
abstract class ScreenCaptureCallback {
    /**
     * 成功
     *
     * @param bitmap 截图后的Bitmap
     */
    fun onSuccess(bitmap: Bitmap?) {}

    /**
     * 失败
     */
    fun onFail() {}
}
