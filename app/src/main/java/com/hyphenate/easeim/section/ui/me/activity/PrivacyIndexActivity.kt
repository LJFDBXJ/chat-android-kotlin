package com.hyphenate.easeim.section.ui.me.activity

import android.content.Context
import android.content.Intent
import android.view.View
import com.hyphenate.easeim.R
import com.hyphenate.easeim.databinding.ActivityPrivacyIndexBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeim.section.ui.contact.activity.ContactBlackListActivity
import com.hyphenate.easeui.widget.EaseTitleBar.OnBackPressListener

class PrivacyIndexActivity(override val layoutId: Int = R.layout.activity_privacy_index) :
    BaseInitActivityKtx<ActivityPrivacyIndexBinding>(),
    View.OnClickListener, OnBackPressListener {

    override fun initListener() {
        super.initListener()
        binding.titleBar.setOnBackPressListener(this)
        binding.itemBlackManager.setOnClickListener(this)
        binding.itemEquipmentManager.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.item_black_manager -> ContactBlackListActivity.actionStart(this)
            R.id.item_equipment_manager -> {}
        }
    }

    override fun onBackPress(view: View) {
        onBackPressed()
    }

    companion object {
        fun actionStart(context: Context) {
            val starter = Intent(context, PrivacyIndexActivity::class.java)
            context.startActivity(starter)
        }
    }
}