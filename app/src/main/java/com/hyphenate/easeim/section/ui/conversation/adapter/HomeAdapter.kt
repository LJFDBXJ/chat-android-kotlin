package com.hyphenate.easeim.section.ui.conversation.adapter

import android.text.TextUtils
import android.view.View
import com.hyphenate.easeim.R
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.chat.EMConversation
import com.hyphenate.easeui.manager.EaseAtMessageHelper
import com.hyphenate.chat.EMMessage
import com.hyphenate.easeui.utils.EaseSmileUtils
import com.hyphenate.easeui.utils.EaseCommonUtils
import com.hyphenate.easeui.utils.EaseDateUtils
import com.hyphenate.easeim.common.db.entity.MsgTypeManageEntity
import com.hyphenate.easeim.common.db.entity.InviteMessage
import com.hyphenate.easeim.common.db.entity.InviteMessageStatus
import com.hyphenate.easeim.common.manager.PushAndMessageHelper
import com.hyphenate.easeim.common.utils.SpDbModel
import com.hyphenate.easeim.databinding.ItemRowChatHistoryBinding
import com.hyphenate.easeim.section.base_ktx.BaseBindAdapter
import java.util.*

class HomeAdapter :
    BaseBindAdapter<Any, ItemRowChatHistoryBinding>(layoutResId = R.layout.item_row_chat_history) {

    override fun convert(holder: BaseDataBindingHolder<ItemRowChatHistoryBinding>, item: Any) {
        super.convert(holder, item)
        holder.dataBinding?.run {
            if (item is EMConversation) {
                val username = item.conversationId()
                listIteaseLayout.background =
                    if (!item.extField.isNullOrEmpty()) ContextCompat.getDrawable(
                        context,
                        R.drawable.ease_conversation_top_bg
                    ) else null
                mentioned.visibility = View.GONE
                when (item.type) {
                    EMConversation.EMConversationType.GroupChat -> {
                        if (EaseAtMessageHelper.get().hasAtMeMsg(username)) {
                            mentioned.setText(R.string.were_mentioned)
                            mentioned.visibility = View.VISIBLE
                        }
                        avatar.setImageResource(R.drawable.ease_group_icon)
                        val group = SdkHelper.instance.groupManager.getGroup(username)
                        name.text = if (group != null) group.groupName else username
                    }
                    EMConversation.EMConversationType.ChatRoom -> {
                        avatar.setImageResource(R.drawable.ease_chat_room_icon)
                        val chatRoom = SdkHelper.instance.chatroomManager.getChatRoom(username)
                        name.text =
                            if (chatRoom != null && !TextUtils.isEmpty(chatRoom.name)) chatRoom.name else username
                    }
                    else -> {
                        avatar.setImageResource(R.drawable.ease_default_avatar)
                        name.text = username
                    }
                }
                unreadMsgNumber.visibility = if (item.unreadMsgCount > 0) {
                    unreadMsgNumber.text = item.unreadMsgCount.toString()
                    View.VISIBLE
                } else {
                    View.GONE
                }
                if (item.allMsgCount != 0) {
                    val lastMessage = item.lastMessage
                    message.text = EaseSmileUtils.getSmiledText(
                        context,
                        EaseCommonUtils.getMessageDigest(lastMessage, context)
                    )
                    time.text =
                        EaseDateUtils.getTimestampString(context, Date(lastMessage.msgTime))
                    msgState.visibility =
                        if (lastMessage.direct() == EMMessage.Direct.SEND && lastMessage.status() == EMMessage.Status.FAIL) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                }
                if (mentioned.visibility != View.VISIBLE) {
                    val unSendMsg = SpDbModel.instance.getUnSendMsg(username)
                    if (!TextUtils.isEmpty(unSendMsg)) {
                        mentioned.setText(R.string.were_not_send_msg)
                        message.text = unSendMsg
                        mentioned.visibility = View.VISIBLE
                    }
                }
            } else if (item is MsgTypeManageEntity) {
                val type = item.type
                val lastMsg = item.lastMsg
                if (lastMsg == null || TextUtils.isEmpty(type)) {
                    return
                }
                listIteaseLayout.background = if (!item.extField.isNullOrEmpty())
                    ContextCompat.getDrawable(
                        context, R.drawable.ease_conversation_top_bg
                    )
                else
                    null
                if (type == MsgTypeManageEntity.msgType.NOTIFICATION.name) {
                    avatar.setImageResource(R.drawable.em_system_nofinication)
                    name.setText(R.string.em_conversation_system_notification)
                }
                val unReadCount = item.unReadCount
                unreadMsgNumber.visibility = if (unReadCount > 0) {
                    unreadMsgNumber.text = unReadCount.toString()
                    View.VISIBLE
                } else {
                    View.GONE
                }
                if (lastMsg is InviteMessage) {
                    time.text =
                        EaseDateUtils.getTimestampString(context, Date(lastMsg.time))
                    val status = lastMsg.statusEnum ?: return
                    val reason = lastMsg.reason
                    if (status == InviteMessageStatus.BEINVITEED ||
                        status == InviteMessageStatus.BEAPPLYED ||
                        status == InviteMessageStatus.GROUPINVITATION
                    ) {
                        message.text = if (reason.isNullOrEmpty())
                            PushAndMessageHelper.getSystemMessage(lastMsg)
                        else
                            reason
                    } else {
                        message.text = PushAndMessageHelper.getSystemMessage(lastMsg)
                    }
                }
            }
        }
    }
}