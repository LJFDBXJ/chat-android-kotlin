package com.hyphenate.easeim.section.search.adapter

import android.view.View
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.hyphenate.chat.EMMessage
import com.hyphenate.chat.EMMessage.ChatType
import com.hyphenate.easeim.R
import com.hyphenate.easeim.databinding.ItemRowChatHistoryBinding
import com.hyphenate.easeim.section.base_ktx.BaseBindAdapter
import com.hyphenate.easeui.utils.EaseCommonUtils
import com.hyphenate.easeui.utils.EaseDateUtils
import com.hyphenate.easeui.utils.EaseEditTextUtils
import com.hyphenate.easeui.utils.EaseSmileUtils
import java.util.*

class SearchMessageAdapter :
    BaseBindAdapter<EMMessage, ItemRowChatHistoryBinding>(layoutResId = R.layout.item_row_chat_history) {
    private var keyword: String? = null

    fun setKeyword(keyword: String) {
        this.keyword = keyword
    }

    override fun convert(
        holder: BaseDataBindingHolder<ItemRowChatHistoryBinding>,
        item: EMMessage
    ) {
        super.convert(holder, item)
        holder.dataBinding?.let { bind ->
            val chatType = item.chatType
            bind.time.text = EaseDateUtils.getTimestampString(context, Date(item.msgTime))
            if (chatType == ChatType.GroupChat || chatType == ChatType.ChatRoom) {
                bind.name.text = item.from
            } else {
                if (item.direct() == EMMessage.Direct.SEND) {
                    bind.name.text = item.from
                } else {
                    bind.name.text = item.to
                }
            }
            if (item.direct() == EMMessage.Direct.SEND && item.status() == EMMessage.Status.FAIL) {
                bind.msgState.visibility = View.VISIBLE
            } else {
                bind.msgState.visibility = View.GONE
            }
            val content = EaseSmileUtils.getSmiledText(
                context,
                EaseCommonUtils.getMessageDigest(item, context)
            ).toString()

            val subContent = EaseEditTextUtils.ellipsizeString(
                bind.message,
                content,
                keyword,
                bind.message.width
            )
            val builder = EaseEditTextUtils.highLightKeyword(context, subContent, keyword)
            if (builder != null) {
                bind.message.text = builder
            } else {
                bind.message.text = content
            }

        }
    }

}