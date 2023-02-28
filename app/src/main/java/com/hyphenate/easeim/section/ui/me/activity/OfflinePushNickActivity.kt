package com.hyphenate.easeim.section.ui.me.activity

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.hyphenate.chat.EMClient
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.ktx.jump
import com.hyphenate.easeim.common.ktx.toast
import com.hyphenate.easeim.databinding.ActivityOfflinePushBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeim.section.ui.me.vm.OfflinePushSetVm

class OfflinePushNickActivity(override val layoutId: Int = R.layout.activity_offline_push) :
    BaseInitActivityKtx<ActivityOfflinePushBinding>(),
    View.OnClickListener {
    private var nickName: String? = null
    private val viewModel by viewModels<OfflinePushSetVm>()


    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        nickName = intent.getStringExtra("nickName")
    }

    override fun initListener() {
        super.initListener()
        binding.titleBar.setOnBackPressListener { onBackPressed() }
        binding.btnSave.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (v.id == R.id.btnSave) {
            val nick = binding.etInputNickname.text.toString()
            if (nick.isNotEmpty()) {
                viewModel.update(this, nick)
                nickName = nick
            } else {
                toast(R.string.demo_offline_nickname_is_empty)
            }
        }
    }

    override fun initData() {
        super.initData()
        if (nickName.isNullOrEmpty()) {
            binding.etInputNickname.setText(nickName)
        } else {
            binding.etInputNickname.setText(EMClient.getInstance().currentUser)
        }

        viewModel.updatePushNickname.observe(this) {
            intent.putExtra("nickName", nickName)
            setResult(RESULT_OK, intent)
            finish()
        }
    }


    companion object {
        fun actionStart(context: Context) {
            context.jump<OfflinePushNickActivity>()
        }
    }
}