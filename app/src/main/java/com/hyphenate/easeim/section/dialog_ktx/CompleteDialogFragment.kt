package com.hyphenate.easeim.section.dialog_ktx

import com.hyphenate.easeim.R
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.hyphenate.easeim.databinding.DialogFragmentMiddleLayoutBinding
import com.hyphenate.easeim.section.base_ktx.BaseActivityKtx

class CompleteDialogFragment : DialogKtxFragment() {
    override val middleLayoutId: Int
        get() = R.layout.dialog_fragment_middle_layout


    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        if (!mContent.isNullOrEmpty()) {
            DataBindingUtil.getBinding<DialogFragmentMiddleLayoutBinding>(bind.rlDialogMiddle)
                ?.let {
                    it.tvContent.text = mContent
                }
        }
    }

    class Builder(context: BaseActivityKtx) : DialogKtxFragment.Builder(
        context
    ) {
        override fun iniFragment(): DialogKtxFragment.Builder {
            super.iniFragment()
            currentFragment = CompleteDialogFragment()
            return this
        }
    }
}