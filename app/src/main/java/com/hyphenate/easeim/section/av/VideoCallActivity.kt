package com.hyphenate.easeim.section.av

import android.graphics.Color
import com.hyphenate.easecallkit.ui.EaseVideoCallActivity
import com.hyphenate.easecallkit.base.EaseCallType
import com.hyphenate.easeui.utils.StatusBarCompat
import android.os.Build
import android.view.View
import android.view.WindowManager
import android.view.ViewGroup
import android.view.Window

class VideoCallActivity : EaseVideoCallActivity() {
    override fun initView() {
        setFitSystem(true)
        if (callType == EaseCallType.SINGLE_VIDEO_CALL) {
            StatusBarCompat.compat(this, Color.parseColor("#000000"))
        } else {
            StatusBarCompat.compat(this, Color.parseColor("#bbbbbb"))
        }
        setStatusBarTextColor(true)
        super.initView()
    }

    /**
     * 修改状态栏文字颜色
     * @param isLight 是否是浅色字体
     */
    fun setStatusBarTextColor(isLight: Boolean) {
        StatusBarCompat.setLightStatusBar(this, !isLight)
    }

    /**
     * 设置是否是沉浸式
     * @param fitSystemForTheme
     */
    fun setFitSystem(fitSystemForTheme: Boolean) {

        if (fitSystemForTheme) {
            val contentFrameLayout = findViewById<View>(Window.ID_ANDROID_CONTENT) as ViewGroup
            val parentView = contentFrameLayout.getChildAt(0)
            if (parentView != null) {
                parentView.fitsSystemWindows = true
            }
        }
        val window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    }
}