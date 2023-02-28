package com.hyphenate.easeim.section.ui.chat.adapter

import android.util.Log
import android.view.View
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.hyphenate.easeim.R
import com.hyphenate.easeim.databinding.ItemWidgetContactBinding
import com.hyphenate.easeim.section.base_ktx.BaseBindAdapter
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.easeui.utils.EaseCommonUtils
import com.hyphenate.easeui.utils.EaseUserUtils

class PickAllUserAdapter :
    BaseBindAdapter<EaseUser, ItemWidgetContactBinding>(R.layout.item_widget_contact) {

    override fun convert(holder: BaseDataBindingHolder<ItemWidgetContactBinding>, item: EaseUser) {
        super.convert(holder, item)
        holder.dataBinding?.let {bind->
            val header = EaseCommonUtils.getLetter(item.nickname)
            Log.e("TAG", "GroupContactAdapter header = $header")
            bind.header.visibility = View.GONE
            // 是否显示字母

//            if(position == 0 || (header != null && !header.equals(EaseCommonUtils.getLetter(getItem(position - 1).getNickname())))) {
//                if(header.isNotEmpty()) {
//                    mHeader.setVisibility(View.VISIBLE);
//                    mHeader.setText(header);
//                }
//            }
            bind.name.text = item.nickname
            EaseUserUtils.showUserAvatar(context, item.avatar, bind.avatar)
        }
    }
}