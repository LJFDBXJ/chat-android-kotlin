package com.hyphenate.easeim.section.base_ktx

import androidx.databinding.ViewDataBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder

/**
 *  Created by LJFDBXJ on 2022/09/20.
 *  Describe : BaseDataBindingHolder
 */
open class BaseBindAdapter<T, BD : ViewDataBinding>(layoutResId: Int, val br: Int? = null) :
    BaseQuickAdapter<T, BaseDataBindingHolder<BD>>(layoutResId) {

    override fun convert(holder: BaseDataBindingHolder<BD>, item: T) {
        holder.run {
            br?.let {
                dataBinding?.setVariable(br, item)
            }
            dataBinding?.executePendingBindings()
        }
    }

}