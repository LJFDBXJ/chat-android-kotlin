package com.hyphenate.easeim.section.ui.chat.delegates

import android.view.View
import com.hyphenate.easeui.delegate.EaseMessageAdapterDelegate
import com.hyphenate.chat.EMMessage
import com.hyphenate.easeui.viewholder.EaseChatRowViewHolder
import com.hyphenate.easecallkit.utils.EaseMsgUtils
import com.hyphenate.easecallkit.base.EaseCallType
import android.view.ViewGroup
import com.hyphenate.easeui.widget.chatrow.EaseChatRow
import com.hyphenate.easeim.section.ui.chat.delegates.views.ChatRowVideoCallView
import com.hyphenate.easeui.interfaces.MessageListItemClickListener
import com.hyphenate.easeim.section.ui.chat.delegates.viewholder.ChatVideoCallVh

/**
 * 视频通话
 */
class ChatVideoCallAdapterDelegate :
    EaseMessageAdapterDelegate<EMMessage, EaseChatRowViewHolder>() {
    override fun isForViewType(item: EMMessage, position: Int): Boolean {
        val isRtcCall = item.getStringAttribute(
                EaseMsgUtils.CALL_MSG_TYPE,
                ""
            ) == EaseMsgUtils.CALL_MSG_INFO
        val isVideoCall = item.getIntAttribute(
                EaseMsgUtils.CALL_TYPE,
                0
            ) == EaseCallType.SINGLE_VIDEO_CALL.code
        return item.type == EMMessage.Type.TXT && isRtcCall && isVideoCall
    }

    override fun getEaseChatRow(parent: ViewGroup, isSender: Boolean): EaseChatRow {
        return ChatRowVideoCallView(parent.context, isSender)
    }

    override fun createViewHolder(
        view: View,
        itemClickListener: MessageListItemClickListener
    ): EaseChatRowViewHolder {
        return ChatVideoCallVh(view, itemClickListener)
    }
}