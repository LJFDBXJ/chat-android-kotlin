package com.hyphenate.easeim.section.ui.me.activity

import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeim.R
import android.app.ProgressDialog
import android.content.Context
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.EMCallBack
import com.hyphenate.easeim.section.ui.chat.ChatPresenter
import android.view.View
import com.hyphenate.easeim.common.ktx.jump
import com.hyphenate.easeim.login.activity.LoginActivity
import com.hyphenate.easeim.common.ktx.toast
import com.hyphenate.easeim.databinding.ActivitySetIndexBinding

class SettingActivity(override val layoutId: Int = R.layout.activity_set_index) :
    BaseInitActivityKtx<ActivitySetIndexBinding>(), View.OnClickListener {

    override fun initListener() {
        super.initListener()
        binding.titleBar.setOnBackPressListener {
            onBackPressed()
        }
        binding.itemSecurity.setOnClickListener(this)
        binding.itemNotification.setOnClickListener(this)
        binding.itemCommonSet.setOnClickListener(this)
        binding.itemPrivacy.setOnClickListener(this)
        binding.btnLogout.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.item_security -> AccountSecurityActivity.actionStart(this)
            R.id.item_notification -> MessageReceiveSetActivity.actionStart(this)
            R.id.itemCommonSet -> CommonSettingsActivity.actionStart(this)
            R.id.item_privacy -> PrivacyIndexActivity.actionStart(this)
            R.id.btnLogout -> logout()
        }
    }

    fun logout() {
        val pd = ProgressDialog(this)
        val st = resources.getString(R.string.Are_logged_out)
        pd.setMessage(st)
        pd.setCanceledOnTouchOutside(false)
        pd.show()
        SdkHelper.instance.logOut(true, object : EMCallBack {
            override fun onSuccess() {
                runOnUiThread {
                    pd.dismiss()
                    ChatPresenter.getInstance().clear()
                    // show login screen
                    finishOtherActivities()
                    jump<LoginActivity>()
                    finish()
                }
            }

            override fun onProgress(progress: Int, status: String) {}
            override fun onError(code: Int, message: String) {
                runOnUiThread {
                    pd.dismiss()
                    toast(R.string.unbind_failed)
                }
            }
        })
    }

    companion object {
        fun actionStart(context: Context) {
            context.jump<SettingActivity>()
        }
    }
}