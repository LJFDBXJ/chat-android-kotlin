package com.hyphenate.easeim.section.ui.contact.adapter

import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.hyphenate.chat.EMChatRoom
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.databinding.ItemWidgetContactBinding
import com.hyphenate.easeim.section.base_ktx.BaseBindAdapter
import com.hyphenate.easeim.section.ui.chat.ChatActivity

class ChatRoomContactAdapter :
    BaseBindAdapter<EMChatRoom, ItemWidgetContactBinding>(layoutResId = R.layout.item_widget_contact) {

    init {
        setOnItemClickListener { _, _, position ->
            val item = getItem(position)
            ChatActivity.actionStart(
                context = context,
                conversationId = item.id,
                chatType = DemoConstant.CHATTYPE_CHATROOM
            )
        }
    }

    override fun convert(
        holder: BaseDataBindingHolder<ItemWidgetContactBinding>,
        item: EMChatRoom
    ) {
        super.convert(holder, item)
        holder.dataBinding?.let { bind ->
            bind.name.text = item.name
            bind.avatar.setImageResource(R.drawable.ease_chat_room_icon)
            bind.signature.text = item.id.toString()
        }
    }
}