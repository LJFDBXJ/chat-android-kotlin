package com.hyphenate.easeim.section.ui.chat.delegates

import android.view.View
import com.hyphenate.easeui.delegate.EaseMessageAdapterDelegate
import com.hyphenate.chat.EMMessage
import com.hyphenate.easeui.viewholder.EaseChatRowViewHolder
import com.hyphenate.chat.EMCustomMessageBody
import com.hyphenate.easeim.common.constant.DemoConstant
import android.view.ViewGroup
import com.hyphenate.easeui.widget.chatrow.EaseChatRow
import com.hyphenate.easeim.section.ui.chat.delegates.views.ChatRowUserCardView
import com.hyphenate.easeui.interfaces.MessageListItemClickListener
import com.hyphenate.easeim.section.ui.chat.delegates.viewholder.ChatUserCardVh

/**
 * 名片消息
 */
class ChatUserCardAdapterDelegate :
    EaseMessageAdapterDelegate<EMMessage, EaseChatRowViewHolder>() {
    override fun isForViewType(item: EMMessage, position: Int): Boolean {
        if (item.type == EMMessage.Type.CUSTOM) {
            val messageBody = item.body as EMCustomMessageBody
            val event = messageBody.event()
            return event == DemoConstant.USER_CARD_EVENT
        }
        return false
    }

    override fun getEaseChatRow(parent: ViewGroup, isSender: Boolean): EaseChatRow {
        return ChatRowUserCardView(parent.context, isSender)
    }

    override fun createViewHolder(
        view: View,
        itemClickListener: MessageListItemClickListener
    ): EaseChatRowViewHolder {
        return ChatUserCardVh(view, itemClickListener)
    }
}