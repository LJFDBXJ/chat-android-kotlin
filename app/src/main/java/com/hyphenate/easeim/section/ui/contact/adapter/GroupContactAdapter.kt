package com.hyphenate.easeim.section.ui.contact.adapter

import android.text.TextUtils
import com.hyphenate.chat.EMGroup
import android.view.View
import com.hyphenate.easeim.R
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.hyphenate.chat.EMClient
import com.hyphenate.easeim.databinding.ItemWidgetContactBinding
import com.hyphenate.easeim.section.base_ktx.BaseBindAdapter

class GroupContactAdapter :
    BaseBindAdapter<EMGroup, ItemWidgetContactBinding>(layoutResId = R.layout.item_widget_contact) {
    override fun convert(holder: BaseDataBindingHolder<ItemWidgetContactBinding>, item: EMGroup) {
        super.convert(holder, item)
        holder.dataBinding?.let { bind ->
            bind.avatar.setImageResource(R.drawable.ease_group_icon)
            bind.signature.visibility = View.VISIBLE
            bind.name.text = item.groupName
            bind.signature.text = item.groupId.toString()
            bind.label.visibility = View.GONE
            if (isOwner(owner = item.owner)) {
                bind.label.visibility = View.VISIBLE
                bind.label.setText(R.string.group_owner)
            }
            if (item.isDisabled) {
                val data = bind.name.text.toString()
                    .trim { it <= ' ' } + context.getString(R.string.group_disabled)
                bind.name.text = data
            }
        }
    }

    private fun isOwner(owner: String): Boolean {
        return EMClient.getInstance().currentUser == owner
    }
}