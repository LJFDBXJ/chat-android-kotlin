package com.hyphenate.easeim.section.ui.chat.delegates.viewholder

import android.view.View
import com.hyphenate.easeui.interfaces.MessageListItemClickListener
import com.hyphenate.easeui.viewholder.EaseChatRowViewHolder
import com.hyphenate.chat.EMMessage
import com.hyphenate.easecallkit.EaseCallKit
import com.hyphenate.easecallkit.base.EaseCallType
import com.hyphenate.easeim.section.av.VideoCallActivity
import android.view.ViewGroup
import com.hyphenate.easeim.section.ui.chat.delegates.views.ChatRowVideoCallView

class ChatVideoCallVh(itemView: View, itemClickListener: MessageListItemClickListener?) :
    EaseChatRowViewHolder(itemView, itemClickListener) {
    override fun onBubbleClick(message: EMMessage) {
        super.onBubbleClick(message)
        if (message.direct() == EMMessage.Direct.SEND) {
            EaseCallKit.getInstance().startSingleCall(
                EaseCallType.SINGLE_VIDEO_CALL,
                message.to,
                null,
                VideoCallActivity::class.java
            )
        } else {
            EaseCallKit.getInstance().startSingleCall(
                EaseCallType.SINGLE_VIDEO_CALL,
                message.from,
                null,
                VideoCallActivity::class.java
            )
        }
    }

    companion object {
        fun create(
            parent: ViewGroup, isSender: Boolean,
            itemClickListener: MessageListItemClickListener?
        ): ChatVideoCallVh {
            return ChatVideoCallVh(
                ChatRowVideoCallView(parent.context, isSender),
                itemClickListener
            )
        }
    }
}