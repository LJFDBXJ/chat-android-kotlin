package com.hyphenate.easeim.section.ui.chat.delegates.viewholder

import android.view.View
import com.hyphenate.easeim.section.ui.contact.activity.ContactDetailActivity.Companion.actionStart
import com.hyphenate.easeui.interfaces.MessageListItemClickListener
import com.hyphenate.easeui.viewholder.EaseChatRowViewHolder
import com.hyphenate.chat.EMMessage
import com.hyphenate.chat.EMCustomMessageBody
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.chat.EMClient
import com.hyphenate.easeim.section.ui.me.activity.UserDetailActivity
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.util.EMLog
import android.view.ViewGroup
import com.hyphenate.easeim.common.utils.SpDbModel
import com.hyphenate.easeim.section.ui.chat.delegates.views.ChatRowUserCardView

class ChatUserCardVh(itemView: View, itemClickListener: MessageListItemClickListener?) :
    EaseChatRowViewHolder(itemView, itemClickListener) {
    override fun onBubbleClick(message: EMMessage) {
        super.onBubbleClick(message)
        if (message.type == EMMessage.Type.CUSTOM) {
            val messageBody = message.body as EMCustomMessageBody
            val event = messageBody.event()
            if (event == DemoConstant.USER_CARD_EVENT) {
                val params = messageBody.params
                val uId = params[DemoConstant.USER_CARD_ID]
                val avatar = params[DemoConstant.USER_CARD_AVATAR]
                val nickname = params[DemoConstant.USER_CARD_NICK]
                if (!uId.isNullOrEmpty()) {
                    if (uId == EMClient.getInstance().currentUser) {
                        UserDetailActivity.actionStart(context, nickname, avatar)
                    } else {
                        var user = SdkHelper.instance.getUserInfo(uId)
                        if (user == null) {
                            user = EaseUser(uId)
                            user.avatar = avatar
                            user.nickname = nickname
                        }
                        val isFriend = SpDbModel.instance.isContact(uId)
                        if (isFriend) {
                            user.contact = 0
                        } else {
                            user.contact = 3
                        }
                        actionStart(context, user)
                    }
                } else {
                    EMLog.e("ChatUserCardViewHolder", "onBubbleClick uId is empty")
                }
            }
        }
    }

    companion object {
        fun create(
            parent: ViewGroup, isSender: Boolean,
            itemClickListener: MessageListItemClickListener?
        ): ChatUserCardVh {
            return ChatUserCardVh(
                ChatRowUserCardView(parent.context, isSender),
                itemClickListener
            )
        }
    }
}