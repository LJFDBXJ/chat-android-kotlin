package com.hyphenate.easeim.common.utils

import android.content.pm.PackageManager
import android.os.Bundle
import com.hyphenate.easeim.AppClient

class AppMetaDataHelper private constructor() {
    private var metaBundle: Bundle? = null
    private fun getMetaBundle() {
        val context = AppClient.instance.applicationContext
        try {
            val info = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            metaBundle = info.metaData
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    /**
     * 从manifestPlaceholders中获取定义的值
     * @param key
     * @return
     */
    fun getPlaceholderValue(key: String): String {
        return metaBundle?.getString(key, "") ?: ""
    }

    companion object {
        private var mInstance: AppMetaDataHelper? = null
        fun instance(): AppMetaDataHelper {
            if (mInstance == null) {
                synchronized(AppMetaDataHelper::class.java) {
                    mInstance = AppMetaDataHelper()
                }
            }
            return mInstance!!
        }
    }

    init {
        getMetaBundle()
    }
}