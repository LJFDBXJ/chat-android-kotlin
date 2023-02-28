package com.hyphenate.easeim.section.ui.group.adapter

import com.hyphenate.chat.EMMucSharedFile
import com.hyphenate.easeim.R
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.hyphenate.easeim.databinding.ItemSharedFileRowLayoutBinding
import com.hyphenate.easeim.section.base_ktx.BaseBindAdapter
import com.hyphenate.util.TextFormater
import java.util.*

class SharedFilesAdapter :
    BaseBindAdapter<EMMucSharedFile, ItemSharedFileRowLayoutBinding>(layoutResId = R.layout.item_shared_file_row_layout) {

    override fun convert(
        holder: BaseDataBindingHolder<ItemSharedFileRowLayoutBinding>,
        item: EMMucSharedFile
    ) {
        super.convert(holder, item)
        holder.dataBinding?.let { bind ->
            bind.tvFileName.text = item.fileName
            bind.tvFileSize.text = TextFormater.getDataSize(item.fileSize)
            bind.tvUpdateTime.text = Date(item.fileUpdateTime).toString()
        }
    }

}