package com.hyphenate.easeim.section.ui.contact.adapter

import com.hyphenate.chat.EMGroupInfo
import com.hyphenate.easeim.R
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.hyphenate.easeim.databinding.ItemWidgetContactBinding
import com.hyphenate.easeim.section.base_ktx.BaseBindAdapter
class PublicGroupContactAdapter :
    BaseBindAdapter<EMGroupInfo, ItemWidgetContactBinding>(layoutResId = R.layout.item_widget_contact) {

    override fun convert(
        holder: BaseDataBindingHolder<ItemWidgetContactBinding>,
        item: EMGroupInfo
    ) {
        super.convert(holder, item)
        holder.dataBinding?.let { bind ->
            bind.avatar.setImageResource(R.drawable.ease_group_icon)
            bind.name.text = item.groupName
            bind.signature.text = item.groupId.toString()
        }
    }
}