package com.hyphenate.easeim.section.ui.me.activity

import android.content.Context
import android.content.Intent
import android.view.View
import com.hyphenate.easeim.R
import com.hyphenate.easeim.databinding.ActivityAccountSecurityBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeui.widget.EaseTitleBar.OnBackPressListener

class AccountSecurityActivity(override val layoutId: Int = R.layout.activity_account_security) :
    BaseInitActivityKtx<ActivityAccountSecurityBinding>(),
    OnBackPressListener, View.OnClickListener {

    override fun initListener() {
        super.initListener()
        binding.titleBar.setOnBackPressListener(this)
        binding.itemEquipments.setOnClickListener(this)
    }

    override fun onBackPress(view: View) {
        onBackPressed()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.itemEquipments -> MultiDeviceActivity.actionStart(this)
        }
    }


    companion object {
        fun actionStart(context: Context) {
            val intent = Intent(context, AccountSecurityActivity::class.java)
            context.startActivity(intent)
        }
    }
}