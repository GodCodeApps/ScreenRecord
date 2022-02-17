package com.zchd.screen.record.utils

import android.content.Context
import android.os.Environment
import android.text.TextUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
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
         val recordingPath =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath + "/camera/RecordingSdk" + System.currentTimeMillis() + ".mp4"
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

        fun createFile(mimeType: String): File? {
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HH_mm_ss").format(Date())
            val fileName = "camera_" + timeStamp + "_"
            val storageDir: File =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            var file: File? = null
            try {
                file = File.createTempFile(fileName, mimeType, storageDir)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            file?.mkdirs()
            return file
        }

        fun fileCopy(oldFilePath: String?, newFilePath: String?): String? {
            val file = File(oldFilePath)
            if (file == null || !file.exists()) {
                return ""
            }
            val newFile = File(newFilePath)
            if (newFile.exists()) {
                newFile.delete()
            }
            try {
                newFile.createNewFile()
            } catch (e1: IOException) {
                return ""
            }
            var input: FileInputStream? = null
            var output: FileOutputStream? = null
            return try {
                input = FileInputStream(file)
                output = FileOutputStream(newFile)
                val buffer = ByteArray(4096)
                var n = 0
                while (-1 != input.read(buffer).also { n = it }) {
                    output.write(buffer, 0, n)
                }
                newFile.path
            } catch (ioe: Exception) {
                ""
            } finally {
                if (output != null) {
                    try {
                        output.close()
                    } catch (e: IOException) {
                    }
                }
                if (input != null) {
                    try {
                        input.close()
                    } catch (e: IOException) {
                    }
                }
            }
        }
    }
}

