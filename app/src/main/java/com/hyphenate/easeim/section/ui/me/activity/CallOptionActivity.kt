package com.hyphenate.easeim.section.ui.me.activity

import android.content.Context
import android.content.Intent
import android.view.View
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.ktx.jump
import com.hyphenate.easeim.common.utils.PreferenceManager
import com.hyphenate.easeim.common.widget.SwitchItemView
import com.hyphenate.easeim.databinding.ActivityCallOptionBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx

/**
 * @author LXJDBXJ
 * @date 2022/10/9
 */
class CallOptionActivity(override val layoutId: Int = R.layout.activity_call_option) :
    BaseInitActivityKtx<ActivityCallOptionBinding>() {

    override fun initListener() {
        super.initListener()
        binding.titleBar.setOnBackPressListener {
            onBackPressed()
        }
    }

    override fun initData() {
        super.initData()
        binding.rlSwitchOfflineCallPush.switch.isChecked =
            PreferenceManager.getInstance().isPushCall
    }


    companion object {
        fun actionStart(context: Context) {
            context.jump<CallOptionActivity>()
        }
    }
}