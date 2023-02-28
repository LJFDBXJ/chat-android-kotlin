package com.hyphenate.easeim.section.ui.me.activity

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.activity.viewModels
import com.hyphenate.chat.EMPushConfigs
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.ktx.toast
import com.hyphenate.easeim.common.utils.SpDbModel
import com.hyphenate.easeim.common.widget.SwitchItemView
import com.hyphenate.easeim.databinding.ActivityOfflinePushSettingsBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeim.section.dialog_ktx.TimePickerDialogFragment
import com.hyphenate.easeim.section.dialog_ktx.TimePickerDialogFragment.OnTimePickCancelListener
import com.hyphenate.easeim.section.dialog_ktx.TimePickerDialogFragment.OnTimePickSubmitListener
import com.hyphenate.easeim.section.ui.me.vm.OfflinePushSetVm
import com.hyphenate.easeui.widget.EaseTitleBar.OnBackPressListener

/**
 * Created by wei on 2016/12/6.
 */
class OfflinePushSettingsActivity(override val layoutId: Int = R.layout.activity_offline_push_settings) :
    BaseInitActivityKtx<ActivityOfflinePushSettingsBinding>(),
    OnBackPressListener, SwitchItemView.OnCheckedChangeListener, View.OnClickListener {
    var mPushConfigs: EMPushConfigs? = null
    private val viewModel by viewModels<OfflinePushSetVm>()
    private var startTime = 0
    private var endTime = 0
    private var shouldUpdateToServer = false

    override fun initListener() {
        super.initListener()
        binding.titleBar.setOnBackPressListener(this)
        binding.switchPushNoDisturb.setOnClickListener(this)
        binding.rlCustomServer.setOnCheckedChangeListener(this)
        binding.itemPushTimeRange.setOnClickListener(this)
    }

    override fun initData() {
        super.initData()
        binding.rlCustomServer.switch.isChecked = SpDbModel.instance.isUseFCM()
        viewModel.configs.observe(this) {
            mPushConfigs = it
            processPushConfigs()
        }
        viewModel.disable.observe(this) {
            if (it) {
                binding.itemPushTimeRange.tvContent.text =
                    getTimeRange(startTime, endTime)
                shouldUpdateToServer = false
            }
        }
        viewModel.enable.observe(this) {

        }
        viewModel.pushConfigs()
    }

    private fun processPushConfigs() {
        if (mPushConfigs == null) return
        startTime = mPushConfigs!!.silentModeStart
        endTime = mPushConfigs!!.noDisturbEndHour
        if (startTime < 0) {
            startTime = 0
        }
        if (endTime < 0) {
            endTime = 0
        }
        binding.itemPushTimeRange.tvContent.text = getTimeRange(startTime, endTime)
        if (mPushConfigs!!.isNoDisturbOn) {
            binding.switchPushNoDisturb.switch.isChecked = mPushConfigs!!.isNoDisturbOn
            setOptionsVisible(true)
            if (shouldUpdateToServer) {
                viewModel.disableOfflinePush(startTime, endTime)
            }
        }
    }

    private fun getTimeRange(start: Int, end: Int): String {
        return getTimeToString(start) + "~" + getTimeToString(end)
    }

    private fun getTimeToString(hour: Int): String {
        return getDoubleDigit(hour) + ":00"
    }

    private fun getDoubleDigit(num: Int): String {
        return if (num > 10) num.toString() else "0$num"
    }

    override fun onBackPress(view: View) {
        onBackPressed()
    }

    override fun onCheckedChanged(buttonView: SwitchItemView, isChecked: Boolean) {
        when (buttonView.id) {
            R.id.rl_custom_server ->SpDbModel.instance.setUseFCM(isChecked)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.switch_push_no_disturb -> {
                val checked = binding.switchPushNoDisturb.switch.isChecked
                binding.switchPushNoDisturb.switch.isChecked = !checked
                if (binding.switchPushNoDisturb.switch.isChecked) {
                    viewModel.pushConfigs()
                    shouldUpdateToServer = true
                    setOptionsVisible(true)
                } else {
                    setOptionsVisible(false)
                    viewModel.enableOfflinePush()
                }
            }
            R.id.item_push_time_range -> showTimePicker()
        }
    }

    private fun setOptionsVisible(visible: Boolean) {
        val resultVisibility = if (visible) View.VISIBLE else View.GONE
        binding.itemPushTimeRange.visibility = resultVisibility
        binding.rlCustomServer.visibility = resultVisibility
    }

    private fun showTimePicker() {
        TimePickerDialogFragment.Builder(this)
            .setTitle(R.string.demo_no_disturb_time)
            .setConfirmColor(R.color.em_color_brand)
            .showCancelButton(true)
            .showMinute(false)
            .setStartTime(getTimeToString(startTime))
            .setEndTime(getTimeToString(endTime))
            .setOnTimePickCancelListener(R.string.cancel, object : OnTimePickCancelListener {
                override fun onClickCancel(view: View?) {}
            })
            .setOnTimePickSubmitListener(R.string.confirm, object : OnTimePickSubmitListener {
                override fun onClickSubmit(view: View?, start: String?, end: String?) {
                    try {
                        val startHour = getHour(start).toInt()
                        val endHour = getHour(end).toInt()
                        if (startHour != endHour) {
                            startTime = startHour
                            endTime = endHour
                            viewModel.disableOfflinePush(startTime, endTime)
                        } else {
                            toast(R.string.offline_time_rang_error)
                        }
                    } catch (e: NumberFormatException) {
                        e.printStackTrace()
                    }
                }
            })
            .show()
    }

    private fun getHour(time: String?): String {
        return if (time!!.contains(":")) time.substring(0, time.indexOf(":")) else time
    }

    companion object {
        @JvmStatic
        fun actionStart(context: Context) {
            val intent = Intent(context, OfflinePushSettingsActivity::class.java)
            context.startActivity(intent)
        }
    }
}