package com.hyphenate.easeim.section.ui.me.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.View
import androidx.core.view.isVisible
import com.hyphenate.chat.EMClient
import com.hyphenate.easeim.AppClient
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.ktx.toast
import com.hyphenate.easeim.common.manager.OptionsHelper.Companion.get
import com.hyphenate.easeim.common.utils.SpDbModel
import com.hyphenate.easeim.common.widget.SwitchItemView
import com.hyphenate.easeim.databinding.ActivityDeveloperSetBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeim.section.ui.me.activity.AppKeyManageActivity.Companion.actionStartForResult
import com.hyphenate.easeim.section.ui.me.test.TestFunctionsIndexActivity
import com.hyphenate.easeui.widget.EaseTitleBar.OnBackPressListener

class DeveloperSetActivity(override val layoutId: Int = R.layout.activity_developer_set) :
    BaseInitActivityKtx<ActivityDeveloperSetBinding>(),
    OnBackPressListener, View.OnClickListener, SwitchItemView.OnCheckedChangeListener {
    private val settingsModel= SpDbModel.instance
    private val options by lazy { EMClient.getInstance().options }
    private val sortType = arrayOf(
        AppClient.instance.applicationContext.getString(R.string.in_order_of_reception),
        AppClient.instance.applicationContext.getString(R.string.by_server_time)
    )
    private var preTimestamp: Long = 0
    private var clickTimes = 0


    override fun initListener() {
        super.initListener()
        binding.titleBar.setOnBackPressListener(this)
        binding.itemAppkey.setOnClickListener(this)
        binding.itemSwitchTokenLogin.setOnCheckedChangeListener(this)
        binding.itemSwitchMsgFromServer.setOnCheckedChangeListener(this)
        binding.itemSwitchUploadToHx.setOnCheckedChangeListener(this)
        binding.itemSwitchAutoDownloadThumbnail.setOnCheckedChangeListener(this)
        binding.itemMsgSort.setOnClickListener(this)
        binding.itemPushNick.setOnClickListener(this)
        binding.itemMsgServiceDiagnose.setOnClickListener(this)
        binding.itemTest.setOnClickListener(this)
        binding.itemVersion.setOnClickListener(this)
    }

    override fun initData() {
        super.initData()
        binding.itemVersion.tvContent.text = "V" + EMClient.VERSION
        binding.itemSwitchTokenLogin.switch.isChecked = settingsModel.isEnableTokenLogin()
        binding.itemSwitchMsgFromServer.switch.isChecked = settingsModel.isMsgRoaming()
        binding.itemSwitchUploadToHx.switch.isChecked = settingsModel.isSetTransferFileByUser()
        binding.itemSwitchAutoDownloadThumbnail.switch.isChecked =
            settingsModel.isSetAutodownloadThumbnail()
        binding.itemMsgSort.tvContent.text =
            if (settingsModel.isSortMessageByServerTime()) sortType[1] else sortType[0]
        setAppKey(options.appKey)
    }

    override fun onBackPress(view: View) {
        onBackPressed()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.item_version -> showTestItem()
            R.id.item_appkey -> actionStartForResult(this, APP_KEY_REQUEST_CODE)
            R.id.item_msg_sort -> showSelectDialog()
            R.id.item_push_nick -> OfflinePushNickActivity.actionStart(this)
            R.id.item_msg_service_diagnose -> DiagnoseActivity.actionStart(this)
            R.id.item_test -> TestFunctionsIndexActivity.actionStart(this)
        }
    }

    private fun showTestItem() {
        if (binding.itemTest.isVisible) {
            return
        }
        clickTimes++
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - preTimestamp > 1000 && clickTimes > 0) {
            clickTimes = 0
        }
        if (clickTimes >= 7) {
            binding.itemTest.visibility = View.VISIBLE
            clickTimes = 0
            toast("Show test item")
        }
        preTimestamp = currentTimestamp
    }

    private fun showSelectDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.choose)
            .setItems(sortType) { dialog, which ->
                binding.itemMsgSort.tvContent.text = sortType[which]
                settingsModel.setSortMessageByServerTime(which == 1)
                options!!.isSortMessageByServerTime = which == 1
            }
            .show()
    }

    override fun onCheckedChanged(buttonView: SwitchItemView, isChecked: Boolean) {
        when (buttonView.id) {
            R.id.item_switch_token_login -> settingsModel.setEnableTokenLogin(isChecked)
            R.id.item_switch_msg_from_server -> settingsModel.setMsgRoaming(isChecked)
            R.id.item_switch_upload_to_hx -> {
                settingsModel.setTransfeFileByUser(isChecked)
                options!!.autoTransferMessageAttachments = isChecked
            }
            R.id.item_switch_auto_download_thumbnail -> {
                settingsModel.setAutoDownloadThumbnail(isChecked)
                options?.setAutoDownloadThumbnail(isChecked)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == APP_KEY_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                val appkey = data.getStringExtra("appkey")
                setAppKey(appkey)
                killApp()
            }
        }
    }

    private fun killApp() {
        SdkHelper.instance.killApp()
    }

    private fun setAppKey(appKey: String?) {
        if (TextUtils.equals(appKey, get().defAppKey)) {
            binding.itemAppkey.tvContent.text = getString(R.string.default_appkey)
        } else {
            binding.itemAppkey.tvContent.text = appKey
        }
    }

    companion object {
        private const val APP_KEY_REQUEST_CODE = 110
        fun actionStart(context: Context) {
            val starter = Intent(context, DeveloperSetActivity::class.java)
            context.startActivity(starter)
        }
    }
}