package com.hyphenate.easeim.section.ui.contact.adapter

import android.view.View
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.hyphenate.chat.EMClient
import com.hyphenate.easeim.R
import com.hyphenate.easeim.databinding.ItemWidgetContactBinding
import com.hyphenate.easeim.section.base_ktx.BaseBindAdapter
import com.hyphenate.easeui.EaseIM
import com.hyphenate.easeui.domain.EaseUser

class ContactListAdapter :
    BaseBindAdapter<EaseUser, ItemWidgetContactBinding>(layoutResId = R.layout.item_widget_contact) {

    override fun convert(holder: BaseDataBindingHolder<ItemWidgetContactBinding>, item: EaseUser) {
        super.convert(holder, item)
        holder.dataBinding?.let { bind ->
            val header = item.initialLetter
            bind.header.visibility = View.GONE
            if (holder.absoluteAdapterPosition == 0 || header != null &&
                header != getItem(holder.absoluteAdapterPosition - 1).initialLetter
            ) {
                if (!header.isNullOrEmpty()) {
                    bind.header.visibility = View.VISIBLE
                    bind.header.text = header
                }
            }
            //判断是否为自己账号多端登录
            var username = item.username
            var nickname = item.nickname
            if (username.contains("/") &&
                username.contains(EMClient.getInstance().currentUser)
            ) {
                username = EMClient.getInstance().currentUser
            }
            val userProvider = EaseIM.getInstance().userProvider
            if (userProvider != null) {
                val user = userProvider.getUser(username)
                if (user != null) {
                    nickname = user.nickname
                    Glide.with(bind.avatar)
                        .load(user.avatar)
                        .placeholder(R.drawable.ease_default_avatar)
                        .error(R.drawable.ease_default_avatar)
                        .into(bind.avatar)
                }
            }
            var postfix = ""
            if (username.contains("/") && username.contains(EMClient.getInstance().currentUser)) {
                postfix = "/" + username.split("/")[1]
                nickname += postfix
            }
            bind.name.text = nickname
        }
    }


}