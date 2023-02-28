package com.hyphenate.easeim.section.ui.chat.delegates

import android.view.View
import com.hyphenate.easeui.delegate.EaseMessageAdapterDelegate
import com.hyphenate.chat.EMMessage
import com.hyphenate.easeui.viewholder.EaseChatRowViewHolder
import com.hyphenate.easeim.common.constant.DemoConstant
import android.view.ViewGroup
import com.hyphenate.easeui.widget.chatrow.EaseChatRow
import com.hyphenate.easeim.section.ui.chat.delegates.views.ChatRowNotificationView
import com.hyphenate.easeui.interfaces.MessageListItemClickListener
import com.hyphenate.easeim.section.ui.chat.delegates.viewholder.ChatNotificationVh

/**
 * 入群等通知消息
 */
class ChatNotificationAdapterDelegate :
    EaseMessageAdapterDelegate<EMMessage, EaseChatRowViewHolder>() {
    override fun isForViewType(item: EMMessage, position: Int): Boolean {
        return item.type == EMMessage.Type.TXT && item.getBooleanAttribute(
            DemoConstant.EM_NOTIFICATION_TYPE,
            false
        )
    }

    override fun getEaseChatRow(parent: ViewGroup, isSender: Boolean): EaseChatRow {
        return ChatRowNotificationView(parent.context, isSender)
    }

    override fun createViewHolder(
        view: View,
        itemClickListener: MessageListItemClickListener
    ): EaseChatRowViewHolder {
        return ChatNotificationVh(view, itemClickListener)
    }
}