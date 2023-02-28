package com.hyphenate.easeim.section.ui.me.headImage

import android.view.View
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.widget.GlideApp
import com.hyphenate.easeim.databinding.ContentHeadImageItemBinding
import com.hyphenate.easeim.section.base_ktx.BaseBindAdapter

/**
 * @author LXJDBXJ
 * @date: 2022/10/11
 */
class HeadImageAdapter :
    BaseBindAdapter<HeadImageInfo, ContentHeadImageItemBinding>(layoutResId = R.layout.content_head_image_item) {
    private var chooseIndex = -1
     var selectHeadUrl = ""

    init {
        setOnItemClickListener { _, _, position ->
            selectHeadUrl = getItem(position).url
            chooseIndex = position
            notifyItemRangeChanged(0, data.size)
        }
    }

    override fun convert(
        holder: BaseDataBindingHolder<ContentHeadImageItemBinding>,
        item: HeadImageInfo
    ) {
        super.convert(holder, item)
        holder.dataBinding?.let { bind ->
            bind.tvHeadImage.text = item.describe
            GlideApp.with(bind.ivHeadImage).load(item.url).into(bind.ivHeadImage)
            if (chooseIndex == holder.absoluteAdapterPosition) {
                bind.ivShowSelect.visibility = View.VISIBLE
                bind.ivShowSelect.setBackgroundResource(R.drawable.headimage_checked)
            } else {
                bind.ivShowSelect.visibility = View.GONE
            }

        }
    }

}