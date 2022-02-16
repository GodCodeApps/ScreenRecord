package com.zchd.screen.record.interfaces

import android.app.Activity
import android.content.Intent


/**
 * Copyright (C), 2020-2021, 中传互动（湖北）信息技术有限公司
 * @Author: pym
 * @Date: 2022/2/16:15:57
 * @Description:
 */
interface IRecorderBinder {
    fun create(resultCode: Int, data: Intent?) {}
    fun start(activity: Activity, recorderCallback: RecorderCallback) {}
    fun pause() {}
    fun resume() {}
    fun stop() {}
}