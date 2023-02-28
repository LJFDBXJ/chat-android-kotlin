package com.hyphenate.easeim.section.ui.chat.delegates.viewholder

import android.view.View
import com.hyphenate.easeui.interfaces.MessageListItemClickListener
import com.hyphenate.easeui.viewholder.EaseChatRowViewHolder
import com.hyphenate.chat.EMMessage
import android.view.ViewGroup
import com.hyphenate.easeim.section.ui.chat.delegates.views.ChatRowConferenceInviteView

class ChatConferenceInviteVh(
    itemView: View,
    itemClickListener: MessageListItemClickListener?
) : EaseChatRowViewHolder(itemView, itemClickListener) {
    override fun onBubbleClick(message: EMMessage) {
        super.onBubbleClick(message)
    }

    companion object {
        fun create(
            parent: ViewGroup, isSender: Boolean,
            itemClickListener: MessageListItemClickListener?
        ): ChatConferenceInviteVh {
            return ChatConferenceInviteVh(
                ChatRowConferenceInviteView(parent.context, isSender),
                itemClickListener
            )
        }
    }
}