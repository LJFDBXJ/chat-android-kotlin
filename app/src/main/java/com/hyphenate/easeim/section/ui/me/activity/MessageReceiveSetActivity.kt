package com.hyphenate.easeim.section.ui.me.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.hyphenate.chat.EMPushManager.DisplayStyle
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.utils.SpDbModel
import com.hyphenate.easeim.common.widget.SwitchItemView
import com.hyphenate.easeim.databinding.ActivityMessageReceiveSetBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeim.section.ui.me.vm.OfflinePushSetVm
import com.hyphenate.easeui.widget.EaseTitleBar.OnBackPressListener

class MessageReceiveSetActivity(override val layoutId: Int = R.layout.activity_message_receive_set) :
    BaseInitActivityKtx<ActivityMessageReceiveSetBinding>(),
    SwitchItemView.OnCheckedChangeListener, OnBackPressListener, View.OnClickListener {
    private var displayStyle: DisplayStyle? = null
    private val viewModel by viewModels<OfflinePushSetVm>()


    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
    }

    override fun initListener() {
        super.initListener()
        binding.titleBar.setOnBackPressListener(this)
        binding.rlSwitchNotification.setOnCheckedChangeListener(this)
        binding.rlSwitchSound.setOnCheckedChangeListener(this)
        binding.rlSwitchVibrate.setOnCheckedChangeListener(this)
        binding.itemPushMessageStyle.setOnClickListener(this)
    }

    override fun initData() {
        super.initData()
        binding.rlSwitchNotification.switch.isChecked = SpDbModel.instance.settingMsgNotification
        setSwitchVisible(binding.rlSwitchNotification.switch.isChecked)
        binding.rlSwitchSound.switch.isChecked = SpDbModel.instance.settingMsgSound
        binding.rlSwitchVibrate.switch.isChecked = SpDbModel.instance.settingMsgVibrate
        viewModel.configs.observe(this) {
            if (it != null) {
                displayStyle = it.displayStyle
                if (displayStyle == DisplayStyle.SimpleBanner) {
                    binding.itemPushMessageStyle.tvContent.setText(R.string.push_message_style_simple)
                } else {
                    binding.itemPushMessageStyle.tvContent.setText(R.string.push_message_style_summary)
                }
            }
        }
        viewModel.pushConfigs()
    }

    override fun onCheckedChanged(buttonView: SwitchItemView, isChecked: Boolean) {
        when (buttonView.id) {
            R.id.rl_switch_notification -> {
                setSwitchVisible(isChecked)
                SpDbModel.instance.settingMsgNotification = isChecked
            }
            R.id.rl_switch_sound -> SpDbModel.instance.settingMsgSound = isChecked
            R.id.rl_switch_vibrate -> SpDbModel.instance.settingMsgVibrate = isChecked
        }
    }

    override fun onBackPress(view: View) {
        onBackPressed()
    }

    /**
     * 设置声音和震动的布局是否可见
     * @param isChecked
     */
    private fun setSwitchVisible(isChecked: Boolean) {
       val visibility= if (isChecked) {
           View.VISIBLE
        } else {
           View.GONE
        }
        binding. rlSwitchSound.visibility = visibility
        binding. rlSwitchVibrate.visibility =visibility
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.item_push_message_style -> if (displayStyle != null) {
                MessagePushStyleActivity.actionStartForResult(this, displayStyle!!.ordinal, 100)
            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 100) {
            viewModel.pushConfigs()
        }
    }

    companion object {
        fun actionStart(context: Context) {
            val intent = Intent(context, MessageReceiveSetActivity::class.java)
            context.startActivity(intent)
        }
    }
}