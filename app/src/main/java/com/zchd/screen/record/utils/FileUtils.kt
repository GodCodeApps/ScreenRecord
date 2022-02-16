package com.zchd.screen.record.utils

import android.content.Context
import android.os.Environment
import android.text.TextUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Copyright (C), 2020-2021, 中传互动（湖北）信息技术有限公司
 * @Author: pym
 * @Date: 2022/2/16:10:55
 * @Description:
 */
class FileUtils {
    companion object {
        private val date = Date()

        private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.getDefault())

        /**
         * 返回带日期的名称
         *
         * @return String
         */
        fun getDateName(): String? {
            return getDateName(null)
        }

        /**
         * 返回带日期的名称
         *
         * @param prefix 文件名前缀(会自动拼接 _ )
         * @return String
         */
        fun getDateName(prefix: String?): String? {
            date.time = System.currentTimeMillis()
            val dateStr = dateFormat.format(date)
            return if (!TextUtils.isEmpty(prefix)) {
                prefix + "_" + dateStr
            } else {
                dateStr
            }
        }

        /**
         * 获取Cache目录
         *
         * @param context context
         * @return File
         */
        fun getCacheDir(context: Context): File? {
            return context.externalCacheDir
        }

        /**
         * 获取Cache目录 Movie
         *
         * @param context context
         * @return File
         */
        fun getCacheMovieDir(context: Context): File? {
            val dir = Environment.DIRECTORY_MOVIES
            return File(getCacheDir(context), dir)
        }
    }
}