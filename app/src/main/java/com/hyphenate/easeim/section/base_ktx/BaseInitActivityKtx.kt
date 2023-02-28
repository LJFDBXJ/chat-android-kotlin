package com.hyphenate.easeim.section.base_ktx

import android.os.Bundle
import android.content.Intent
import android.view.View
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.hyphenate.easecallkit.EaseCallKit
import com.hyphenate.easecallkit.utils.EaseCallState
import com.hyphenate.easecallkit.base.EaseCallFloatWindow
import com.hyphenate.easecallkit.base.EaseCallType
import com.hyphenate.easeim.section.av.MultipleVideoActivity
import com.hyphenate.easeim.section.av.VideoCallActivity

abstract class BaseInitActivityKtx<D : ViewDataBinding> : BaseActivityKtx() {
    lateinit var binding: D

    /**
     * get layout id
     * @return
     */
    @get:LayoutRes
    abstract val layoutId: Int

    val contentView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, layoutId)
        initSystemFit()
        initView(savedInstanceState)
        initListener()
        initData()
    }

    open fun initSystemFit() {
        setFitSystemForTheme(true)
    }


    /**
     * init view
     * @param savedInstanceState
     */
    open fun initView(savedInstanceState: Bundle?) {}


    /**
     * init listener
     */
    open fun initListener() {}

    /**
     * init data
     */
    open fun initData() {}
    override fun onRestart() {
        super.onRestart()
        if (EaseCallKit.getInstance().callState != EaseCallState.CALL_IDLE
            && !EaseCallFloatWindow.getInstance(this).isShowing
        ) {
            val dist = if (EaseCallKit.getInstance().callType == EaseCallType.CONFERENCE_CALL) {
                MultipleVideoActivity::class.java
            } else {
                VideoCallActivity::class.java
            }
            startActivity(
                Intent(this, dist).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )

        }
    }
}