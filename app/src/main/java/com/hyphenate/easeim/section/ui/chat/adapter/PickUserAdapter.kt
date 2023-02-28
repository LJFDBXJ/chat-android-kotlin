package com.hyphenate.easeim.section.ui.chat.adapter

import android.text.TextUtils
import android.util.Log
import com.hyphenate.easeui.domain.EaseUser
import android.view.View
import com.hyphenate.easeim.R
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.hyphenate.easeim.databinding.ItemWidgetContactBinding
import com.hyphenate.easeim.section.base_ktx.BaseBindAdapter
import com.hyphenate.easeui.utils.EaseCommonUtils
import com.hyphenate.easeui.utils.EaseUserUtils

class PickUserAdapter :
    BaseBindAdapter<EaseUser, ItemWidgetContactBinding>(R.layout.item_widget_contact) {


    override fun convert(holder: BaseDataBindingHolder<ItemWidgetContactBinding>, item: EaseUser) {
        super.convert(holder, item)
        holder.dataBinding?.let { bind ->
            val header = EaseCommonUtils.getLetter(item.nickname)
            Log.e("TAG", "GroupContactAdapter header = $header")
            bind.header.visibility = View.GONE
            // 是否显示字母
            if (holder.absoluteAdapterPosition == 0 || header != null &&
                header != EaseCommonUtils.getLetter(
                    getItem(holder.absoluteAdapterPosition - 1).nickname
                )
            ) {
                if (!header.isNullOrEmpty()) {
                    bind.header.visibility = View.VISIBLE
                    bind.header.text = header
                }
            }
            bind.name.text = item.nickname
            EaseUserUtils.setUserAvatar(context, item.username, bind.avatar)
        }
    }
}