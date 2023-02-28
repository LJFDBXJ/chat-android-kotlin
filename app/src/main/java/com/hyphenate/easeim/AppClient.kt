package com.hyphenate.easeim

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.hyphenate.easeim.common.interfaceOrImplement.UserActivityLifecycleCallbacks
import androidx.multidex.MultiDex
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.scwang.smartrefresh.layout.constant.SpinnerStyle
import com.scwang.smartrefresh.layout.footer.ClassicsFooter
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.hyphenate.easeim.common.utils.PreferenceManager
import java.lang.Exception
import com.hyphenate.util.EMLog

class AppClient : Application(), Thread.UncaughtExceptionHandler {
    val lifecycleCallbacks = UserActivityLifecycleCallbacks()
    override fun onCreate() {
        super.onCreate()
        instance = this
        initThrowableHandler()
        initHx()
        registerActivityLifecycleCallbacks()
        closeAndroidPDialog()
    }

    private fun initThrowableHandler() {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    private fun initHx() {
        // 初始化PreferenceManager
        PreferenceManager.init(this)
        // init hx sdk
        if (SdkHelper.instance.autoLogin) {
            SdkHelper.instance.init(this)
        }
    }

    private fun registerActivityLifecycleCallbacks() {
        this.registerActivityLifecycleCallbacks(lifecycleCallbacks)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    init {
        /**
         * 为了兼容5.0以下使用vector图标
         */
        //设置全局的Header构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, layout ->
            ClassicsHeader.REFRESH_HEADER_LASTTIME = context.getString(R.string.last_update)
            ClassicsHeader.REFRESH_HEADER_PULLDOWN = context.getString(R.string.pull_down)
            ClassicsHeader.REFRESH_HEADER_REFRESHING = context.getString(R.string.refreshing)
            ClassicsHeader.REFRESH_HEADER_RELEASE = context.getString(R.string.release_refresh)
            ClassicsHeader.REFRESH_HEADER_FINISH = context.getString(R.string.refresh_finish)
            ClassicsHeader.REFRESH_HEADER_FAILED = context.getString(R.string.refresh_failed)
            ClassicsHeader(context).setSpinnerStyle(SpinnerStyle.Translate) //指定为经典Header，默认是 贝塞尔雷达Header
        }
        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, _ ->
            ClassicsFooter.REFRESH_FOOTER_LOADING = context.getString(R.string.be_loading)
            ClassicsFooter.REFRESH_FOOTER_FINISH = context.getString(R.string.loaded)
            ClassicsFooter.REFRESH_FOOTER_FAILED = context.getString(R.string.load_failed)
            //指定为经典Footer，默认是 BallPulseFooter
            ClassicsFooter(context).setSpinnerStyle(SpinnerStyle.Translate)
        }
    }

    companion object {
        lateinit var instance: AppClient
            private set
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        EMLog.e("demoApp", e.message)
    }

    /**
     * 解决androidP 第一次打开程序出现莫名弹窗
     * 弹窗内容“detected problems with api ”
     */
    @SuppressLint("SoonBlockedPrivateApi")
    private fun closeAndroidPDialog() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
            try {
                val aClass = Class.forName("android.content.pm.PackageParser\$Package")
                val declaredConstructor = aClass.getDeclaredConstructor(
                    String::class.java
                )
                declaredConstructor.setAccessible(true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val cls = Class.forName("android.app.ActivityThread")
                val declaredMethod = cls.getDeclaredMethod("currentActivityThread")
                declaredMethod.isAccessible = true
                val activityThread = declaredMethod.invoke(null)
                val mHiddenApiWarningShown = cls.getDeclaredField("mHiddenApiWarningShown")
                mHiddenApiWarningShown.isAccessible = true
                mHiddenApiWarningShown.setBoolean(activityThread, true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}