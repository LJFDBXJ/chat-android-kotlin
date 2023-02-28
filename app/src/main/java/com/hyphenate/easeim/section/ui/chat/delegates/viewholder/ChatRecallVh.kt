package com.hyphenate.easeim.section.ui.chat.delegates.viewholder

import android.view.View
import com.hyphenate.easeui.interfaces.MessageListItemClickListener
import com.hyphenate.easeui.viewholder.EaseChatRowViewHolder
import android.view.ViewGroup
import com.hyphenate.easeim.section.ui.chat.delegates.views.ChatRowRecallView

class ChatRecallVh(itemView: View, itemClickListener: MessageListItemClickListener?) :
    EaseChatRowViewHolder(itemView, itemClickListener) {
    companion object {
        fun create(
            parent: ViewGroup, isSender: Boolean,
            listener: MessageListItemClickListener?
        ): ChatRecallVh {
            return ChatRecallVh(ChatRowRecallView(parent.context, isSender), listener)
        }
    }
}