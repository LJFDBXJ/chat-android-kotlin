package com.hyphenate.easeim.section.ui.me.activity

import com.hyphenate.easeui.widget.EaseTitleBar.OnRightClickListener
import com.hyphenate.easeui.widget.EaseTitleBar.OnBackPressListener
import com.hyphenate.easeim.R
import com.hyphenate.easeim.SdkHelper
import android.app.Activity
import android.content.Intent
import android.view.View
import com.hyphenate.easeim.common.utils.SpDbModel
import com.hyphenate.easeim.databinding.ActivityAppkeyAddBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx

class AppKeyAddActivity(override val layoutId: Int = R.layout.activity_appkey_add) :
    BaseInitActivityKtx<ActivityAppkeyAddBinding>(), OnRightClickListener, OnBackPressListener {


    override fun initListener() {
        super.initListener()
        binding.titleBar.setOnBackPressListener(this)
        binding.titleBar.setOnRightClickListener(this)
    }

    override fun onRightClick(view: View) {
        val appKey = binding.editCustomAppkey.text.toString().trim { it <= ' ' }
        if (appKey.isNotEmpty()) {
            SpDbModel.instance.saveAppKey(appKey)
        }
        setResult(RESULT_OK)
        finish()
    }

    override fun onBackPress(view: View) {
        onBackPressed()
    }

    companion object {
        @JvmStatic
        fun actionStartForResult(activity: Activity, requestCode: Int) {
            val starter = Intent(activity, AppKeyAddActivity::class.java)
            activity.startActivityForResult(starter, requestCode)
        }
    }
}