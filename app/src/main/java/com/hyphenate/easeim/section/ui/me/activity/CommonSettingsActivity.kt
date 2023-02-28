package com.hyphenate.easeim.section.ui.me.activity

import android.content.Context
import android.content.Intent
import android.view.View
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMOptions
import com.hyphenate.easeim.SdkHelper.Companion.instance
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.utils.SpDbModel
import com.hyphenate.easeim.common.widget.SwitchItemView
import com.hyphenate.easeim.databinding.ActivityCommonSettingsBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeui.widget.EaseTitleBar.OnBackPressListener

class CommonSettingsActivity(override val layoutId: Int = R.layout.activity_common_settings) :
    BaseInitActivityKtx<ActivityCommonSettingsBinding>(),
    View.OnClickListener, SwitchItemView.OnCheckedChangeListener, OnBackPressListener {
    private var chatOptions: EMOptions? = null
    override fun initListener() {
        super.initListener()
        binding.titleBar.setOnBackPressListener(this)
        binding.itemNotification.setOnClickListener(this)
        binding.itemCallOption.setOnClickListener(this)
        binding.itemSwitchTyping.setOnCheckedChangeListener(this)
        binding.itemSwitchSpeaker.setOnCheckedChangeListener(this)
        binding.itemSwitchChatroom.setOnCheckedChangeListener(this)
        binding.itemSwitchDeleteMsg.setOnCheckedChangeListener(this)
        binding.itemSwitchAutoFile.setOnCheckedChangeListener(this)
        binding.itemSwitchAutoDownload.setOnCheckedChangeListener(this)
        binding.itemSwitchAutoAcceptGroup.setOnCheckedChangeListener(this)
        binding.itemSwitchChatroomDeleteMsg.setOnCheckedChangeListener(this)
        binding.itemLanguage.setOnClickListener(this)
    }

    override fun initData() {
        super.initData()

        chatOptions = EMClient.getInstance().options
        binding.itemSwitchTyping.switch.isChecked = SpDbModel.instance.isShowMsgTyping()
        binding.itemSwitchSpeaker.switch.isChecked = SpDbModel.instance.settingMsgSpeaker
        binding.itemSwitchChatroom.switch.isChecked =
            SpDbModel.instance.isChatroomOwnerLeaveAllowed()
        binding.itemSwitchDeleteMsg.switch.isChecked =
            SpDbModel.instance.isDeleteMessagesAsExitGroup()
        binding.itemSwitchAutoFile.switch.isChecked = SpDbModel.instance.isSetTransferFileByUser()
        binding.itemSwitchAutoDownload.switch.isChecked =
            SpDbModel.instance.isSetAutodownloadThumbnail()
        binding.itemSwitchAutoAcceptGroup.switch.isChecked =
            SpDbModel.instance.isAutoAcceptGroupInvitation()
        binding.itemSwitchChatroomDeleteMsg.switch.isChecked =
            SpDbModel.instance.isDeleteMessagesAsExitChatRoom()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.item_notification -> OfflinePushSettingsActivity.actionStart(this)
            R.id.item_call_option -> CallOptionActivity.actionStart(this)
            R.id.item_language -> LanguageActivity.actionStart(this)
        }
    }

    override fun onCheckedChanged(buttonView: SwitchItemView, isChecked: Boolean) {
        when (buttonView.id) {
            R.id.item_switch_typing -> SpDbModel.instance.showMsgTyping(isChecked)
            R.id.item_switch_speaker -> SpDbModel.instance.settingMsgSpeaker = isChecked
            R.id.item_switch_chatroom -> {
                SpDbModel.instance.allowChatroomOwnerLeave(isChecked)
                chatOptions!!.allowChatroomOwnerLeave(isChecked)
            }
            R.id.item_switch_delete_msg -> {
                SpDbModel.instance.setDeleteMessagesAsExitGroup(isChecked)
                chatOptions!!.isDeleteMessagesAsExitGroup = isChecked
            }
            R.id.item_switch_auto_file -> {
                SpDbModel.instance.setTransfeFileByUser(isChecked)
                chatOptions!!.autoTransferMessageAttachments = isChecked
            }
            R.id.item_switch_auto_download -> {
                SpDbModel.instance.setAutoDownloadThumbnail(isChecked)
                chatOptions!!.setAutoDownloadThumbnail(isChecked)
            }
            R.id.item_switch_auto_accept_group -> {
                SpDbModel.instance.setAutoAcceptGroupInvitation(isChecked)
                chatOptions!!.isAutoAcceptGroupInvitation = isChecked
            }
            R.id.item_switch_chatroom_delete_msg -> {
                SpDbModel.instance.setDeleteMessagesAsExitChatRoom(isChecked)
                chatOptions!!.isDeleteMessagesAsExitChatRoom = isChecked
            }
        }
    }

    override fun onBackPress(view: View) {
        onBackPressed()
    }

    companion object {
        fun actionStart(context: Context) {
            val intent = Intent(context, CommonSettingsActivity::class.java)
            context.startActivity(intent)
        }
    }
}