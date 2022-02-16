package com.zchd.screen.record.interfaces

import java.io.File

/**
 * Copyright (C), 2020-2021, 中传互动（湖北）信息技术有限公司
 * @Author: pym
 * @Date: 2022/2/16:10:58
 * @Description:
 */
interface MediaRecorderCallback {
    /**
     * 成功
     *
     * @param file 录制后的File
     */
    fun onSuccess(file: File?) {}

    /**
     * 失败
     */
    fun onFail() {}
}
