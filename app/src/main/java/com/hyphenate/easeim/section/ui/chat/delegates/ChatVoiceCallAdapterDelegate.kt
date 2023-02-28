package com.hyphenate.easeim.section.ui.chat.delegates

import android.view.View
import com.hyphenate.easeui.delegate.EaseMessageAdapterDelegate
import com.hyphenate.chat.EMMessage
import com.hyphenate.easeui.viewholder.EaseChatRowViewHolder
import com.hyphenate.easecallkit.utils.EaseMsgUtils
import com.hyphenate.easecallkit.base.EaseCallType
import android.view.ViewGroup
import com.hyphenate.easeui.widget.chatrow.EaseChatRow
import com.hyphenate.easeim.section.ui.chat.delegates.views.ChatRowVoiceCallView
import com.hyphenate.easeui.interfaces.MessageListItemClickListener
import com.hyphenate.easeim.section.ui.chat.delegates.viewholder.ChatVoiceCallVh

/**
 * 语音通话
 */
class ChatVoiceCallAdapterDelegate :
    EaseMessageAdapterDelegate<EMMessage, EaseChatRowViewHolder>() {
    override fun isForViewType(item: EMMessage, position: Int): Boolean {
        val isRtcCall =
            item.getStringAttribute(EaseMsgUtils.CALL_MSG_TYPE, "") == EaseMsgUtils.CALL_MSG_INFO

        val isVoiceCall = item.getIntAttribute(
            EaseMsgUtils.CALL_TYPE,
            0
        ) == EaseCallType.SINGLE_VOICE_CALL.code
        return item.type == EMMessage.Type.TXT && isRtcCall && isVoiceCall
    }

    override fun getEaseChatRow(parent: ViewGroup, isSender: Boolean): EaseChatRow {
        return ChatRowVoiceCallView(parent.context, isSender)
    }

    override fun createViewHolder(
        view: View,
        itemClickListener: MessageListItemClickListener
    ): EaseChatRowViewHolder {
        return ChatVoiceCallVh(view, itemClickListener)
    }
}