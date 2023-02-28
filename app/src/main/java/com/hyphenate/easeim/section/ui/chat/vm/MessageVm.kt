package com.hyphenate.easeim.section.ui.chat.vm

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hyphenate.EMCallBack
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMMessage
import com.hyphenate.easecallkit.EaseCallKit
import com.hyphenate.easecallkit.base.EaseCallType
import com.hyphenate.easeim.AppClient
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.ktx.toast
import com.hyphenate.easeim.common.livedatas.LiveDataBus
import com.hyphenate.easeim.section.av.VideoCallActivity
import com.hyphenate.easeim.section.base_ktx.BaseActivityKtx
import com.hyphenate.easeim.section.ui.chat.entity.ExpandDataEntity
import com.hyphenate.easeim.section.dialog_ktx.FullEditDialogFragment
import com.hyphenate.easeim.section.dialog_ktx.LabelEditDialogFragment
import com.hyphenate.easeim.section.dialog_ktx.ListDialogFragment
import com.hyphenate.easeim.section.dialog_ktx.SimpleDialogFragment
import com.hyphenate.easeim.section.ui.group.GroupHelper
import com.hyphenate.easeui.constants.EaseConstant
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.easeui.modules.chat.EaseChatLayout
import com.hyphenate.easeui.modules.chat.interfaces.IChatExtendMenu
import com.hyphenate.util.EMLog

class MessageVm(application: Application) : AndroidViewModel(application) {
    private val labels = arrayOf(
        AppClient.instance.applicationContext.getString(R.string.tab_politics),
        AppClient.instance.applicationContext.getString(R.string.tab_yellow_related),
        AppClient.instance.applicationContext.getString(R.string.tab_advertisement),
        AppClient.instance.applicationContext.getString(R.string.tab_abuse),
        AppClient.instance.applicationContext.getString(R.string.tab_violent),
        AppClient.instance.applicationContext.getString(R.string.tab_contraband),
        AppClient.instance.applicationContext.getString(R.string.tab_other)
    )
    private val calls = arrayOf(
        AppClient.instance.applicationContext.getString(R.string.video_call),
        AppClient.instance.applicationContext.getString(R.string.voice_call)
    )

    fun setMessageChange(change: EaseEvent) {
        LiveDataBus.get().use(change.event).postValue(change)
    }

    val action: LiveData<String> get() = _action
    private val _action = MutableLiveData<String>()

    fun updateAction(action: String) {
        _action.postValue(action)
    }

    /**
     * 初始化键盘菜单
     */
    fun initKeyBordData(chatType: Int, conversationId: String, menu: IChatExtendMenu) {
        val menuData = arrayOf(
            ExpandDataEntity(
                R.string.attach_picture,
                R.drawable.ease_chat_image_selector,
                R.id.extend_item_picture
            ),
            ExpandDataEntity(
                R.string.attach_take_pic,
                R.drawable.ease_chat_takepic_selector,
                R.id.extend_item_take_picture
            ),
            ExpandDataEntity(
                R.string.attach_video,
                R.drawable.em_chat_video_selector,
                R.id.extend_item_video
            ),
            ExpandDataEntity(
                R.string.attach_media_call,
                R.drawable.em_chat_video_call_selector,
                R.id.extend_item_video_call
            ),
            ExpandDataEntity(
                R.string.voice_and_video_conference,
                R.drawable.em_chat_video_call_selector,
                R.id.extend_item_conference_call
            ),
            ExpandDataEntity(
                R.string.attach_location,
                R.drawable.ease_chat_location_selector,
                R.id.extend_item_location
            ),
            ExpandDataEntity(
                R.string.attach_file,
                R.drawable.em_chat_file_selector,
                R.id.extend_item_file
            ),
            ExpandDataEntity(
                R.string.attach_user_card,
                R.drawable.em_chat_user_card_selector,
                R.id.extend_item_user_card
            ),
        )
        menuData.forEach {
            //添加扩展槽
            when {
                chatType == EaseConstant.CHATTYPE_SINGLE -> {
                    //inputMenu.registerExtendMenuItem(R.string.attach_voice_call, R.drawable.em_chat_voice_call_selector, EaseChatInputMenu.ITEM_VOICE_CALL, this);
                    menu.registerMenuItem(it.name, it.icon, it.id)
                }
                //目前普通模式也支持设置主播和观众人数，都建议使用普通模式
                //inputMenu.registerExtendMenuItem(R.string.title_live, R.drawable.em_chat_video_call_selector, EaseChatInputMenu.ITEM_LIVE, this);
                chatType == EaseConstant.CHATTYPE_GROUP && EMClient.getInstance().options.requireAck -> {
                    val group = SdkHelper.instance.groupManager.getGroup(conversationId)
                    if (GroupHelper.isOwner(group)) {
                        //名片扩展
                        menu.registerMenuItem(
                            it.name,
                            it.icon,
                            it.id
                        )
                    }
                }
                chatType == EaseConstant.CHATTYPE_GROUP -> {// 音视频会议
                    menu.registerMenuItem(
                        it.name,
                        it.icon,
                        it.id
                    )
                   }
                else -> {
                    //名片扩展
                    menu.registerMenuItem(
                        it.name,
                        it.icon,
                        it.id
                    )
                }
            }

        }

    }

    fun showDeliveryDialog(context: Context, chatLayout: EaseChatLayout) {
        FullEditDialogFragment.Builder(context as BaseActivityKtx)
            .setTitle(R.string.em_chat_group_read_ack)
            .setOnConfirmClickListener(R.string.em_chat_group_read_ack_send, object :
                FullEditDialogFragment.OnSaveClickListener {
                override fun onSaveClick(view: View, content: String) {
                    chatLayout.sendTextMessage(
                        content,
                        true
                    )
                }
            })
            .setConfirmColor(R.color.em_color_brand)
            .setHint(R.string.em_chat_group_read_ack_hint)
            .show()
    }

    fun showDeleteDialog(context: Context, message: EMMessage, chatLayout: EaseChatLayout) {
        SimpleDialogFragment.Builder(context as BaseActivityKtx)
            .setTitle(context.getString(R.string.em_chat_delete_title))
            .setConfirmColor(R.color.red)
            .setOnConfirmClickListener(
                context.getString(R.string.delete)
            ) {
                chatLayout.deleteMessage(message)
            }
            .showCancelButton(true)
            .show()
    }

    fun translateMessageFail(context: Context, message: EMMessage, code: Int, error: String) {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.unable_translate))
            .setMessage("$error.")
            .setPositiveButton(context.getString(R.string.confirm)) { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }


    fun showLabel(context: Context, message: EMMessage) {
        ListDialogFragment.Builder(context as BaseActivityKtx)
            .setData(labels)
            .setCancelColorRes(R.color.black)
            .setWindowAnimations(R.style.animate_dialog)
            .setOnItemClickListener { _, position ->
                showLabelDialog(context, message, labels[position])
            }
            .show()
    }

    private fun showLabelDialog(context: Context, message: EMMessage, label: String) {
        LabelEditDialogFragment.Builder(context as BaseActivityKtx)
            .setOnConfirmClickListener(object : LabelEditDialogFragment.OnConfirmClickListener {
                override fun onConfirm(view: View, content: String) {
                    SimpleDialogFragment.Builder(context)
                        .setTitle(context.getString(R.string.report_delete_title))
                        .setConfirmColor(R.color.em_color_brand)
                        .setOnConfirmClickListener(
                            context.getString(R.string.confirm)
                        ) {
                            EMClient.getInstance().chatManager().asyncReportMessage(
                                message.msgId,
                                label,
                                content,
                                object : EMCallBack {
                                    override fun onSuccess() {
                                        EMLog.e("ReportMessage：", "onSuccess 举报成功")
                                        context.toast("举报成功")
                                    }

                                    override fun onError(code: Int, error: String) {
                                        EMLog.e(
                                            "ReportMessage：",
                                            "onError 举报失败: code $code  : $error"
                                        )
                                        context.toast("举报失败： code: $code desc: $error")
                                    }

                                    override fun onProgress(progress: Int, status: String) {}
                                })
                        }
                        .showCancelButton(true)
                        .show()
                }
            }).show()
    }

    /**
     * 聊天界面 键盘弹出 音视频联系
     */
    fun showSelectDialog(context: Context, conversationId: String) {
        ListDialogFragment.Builder(context as BaseActivityKtx) //.setTitle(R.string.em_single_call_type)
            .setData(data = calls)
            .setCancelColorRes(R.color.black)
            .setWindowAnimations(R.style.animate_dialog)
            .setOnItemClickListener { _, position ->
                when (position) {
                    0 -> EaseCallKit.getInstance().startSingleCall(
                        EaseCallType.SINGLE_VIDEO_CALL,
                        conversationId,
                        null,
                        VideoCallActivity::class.java
                    )
                    1 -> EaseCallKit.getInstance().startSingleCall(
                        EaseCallType.SINGLE_VOICE_CALL,
                        conversationId,
                        null,
                        VideoCallActivity::class.java
                    )
                }
            }
            .show()
    }

    fun showTranslateDialog(context: Context, message: EMMessage, chatLayout: EaseChatLayout) {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.using_translate))
            .setMessage(context.getString(R.string.retranslate_prompt))
            .setPositiveButton(context.getString(R.string.confirm)) { _, _ ->
                chatLayout.translateMessage(
                    message,
                    false
                )
            }
            .show()
    }
}