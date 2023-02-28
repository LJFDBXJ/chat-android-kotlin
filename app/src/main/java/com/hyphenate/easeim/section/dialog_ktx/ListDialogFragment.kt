package com.hyphenate.easeim.section.dialog_ktx

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.hyphenate.easeim.R
import com.hyphenate.easeim.databinding.DialogDefaultListItemBinding
import com.hyphenate.easeim.databinding.FragmentDialogListBinding
import com.hyphenate.easeim.section.base_ktx.BaseActivityKtx
import com.hyphenate.easeim.section.base_ktx.BaseBindAdapter
import com.hyphenate.easeim.section.base_ktx.BaseDialogKtxFragment
import com.hyphenate.easeui.adapter.EaseBaseRecyclerViewAdapter

typealias  ClickListener = Function2<View?, Int, Unit>

class ListDialogFragment(override val layoutId: Int = R.layout.fragment_dialog_list) :
    BaseDialogKtxFragment<FragmentDialogListBinding>() {
    private var title: String? = null
    private var cancel: String? = null
    private var cancelColor = 0
    private var itemClickListener: ClickListener? = null
    private var data: List<String>? = null
    private var cancelClickListener: OnDialogCancelClickListener? = null
    private var animations //进出动画
            = 0

    override fun onStart() {
        super.onStart()
        setDialogParams()
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        if (animations != 0) {
            try {
                dialog?.window?.setWindowAnimations(animations)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (title.isNullOrEmpty()) {
            bind.tvTitle.visibility = View.GONE
            bind.viewDivider.visibility = View.GONE
        } else {
            bind.tvTitle.visibility = View.VISIBLE
            bind.viewDivider.visibility = View.VISIBLE
            bind.tvTitle.text = title
        }

        if (cancel.isNullOrEmpty()) {
            bind.btnCancel.text = getString(R.string.cancel)
        } else {
            bind.btnCancel.text = cancel
        }
        if (cancelColor != 0) {
            bind.btnCancel.setTextColor(cancelColor)
        }
    }

    override fun initListener() {
        super.initListener()
        bind.btnCancel.setOnClickListener { v: View? ->
            dismiss()
            cancelClickListener?.OnCancel(v)
        }
    }

    override fun initData() {
        super.initData()
        bind.rvDialogList.layoutManager = LinearLayoutManager(requireContext())
        val adapter = DefaultAdapter()
        bind.rvDialogList.adapter = adapter
        bind.rvDialogList.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
        adapter.setList(data)
        adapter.setOnItemClickListener { _, view, position ->
            dismiss()
            itemClickListener?.invoke(view, position)
        }
    }

    class Builder(private val context: BaseActivityKtx) {
        private var title: String? = null
        private var adapter: EaseBaseRecyclerViewAdapter<String>? = null
        private var data: List<String>? = null
        private var clickListener: ClickListener? = null
        private var cancel: String? = null
        private var cancelColor = 0
        private var cancelClickListener: OnDialogCancelClickListener? = null
        private var bundle: Bundle? = null
        private var animations = 0 //进出动画

        fun setTitle(@StringRes title: Int): Builder {
            this.title = context.getString(title)
            return this
        }

        fun setTitle(title: String?): Builder {
            this.title = title
            return this
        }

        fun setAdapter(adapter: EaseBaseRecyclerViewAdapter<String>): Builder {
            this.adapter = adapter
            return this
        }

        fun setData(data: List<String>?): Builder {
            this.data = data
            return this
        }

        fun setData(data: Array<String>): Builder {
            this.data = listOf(*data)
            return this
        }

        fun setOnItemClickListener(listener: ClickListener?): Builder {
            clickListener = listener
            return this
        }

        fun setOnCancelClickListener(
            @StringRes cancel: Int,
            listener: OnDialogCancelClickListener?
        ): Builder {
            this.cancel = context.getString(cancel)
            cancelClickListener = listener
            return this
        }

        fun setOnCancelClickListener(
            cancel: String?,
            listener: OnDialogCancelClickListener?
        ): Builder {
            this.cancel = cancel
            cancelClickListener = listener
            return this
        }

        fun setCancelColorRes(@ColorRes color: Int): Builder {
            cancelColor = ContextCompat.getColor(context, color)
            return this
        }

        fun setCancelColor(@ColorInt color: Int): Builder {
            cancelColor = color
            return this
        }

        fun setArgument(bundle: Bundle?): Builder {
            this.bundle = bundle
            return this
        }

        fun setWindowAnimations(@StyleRes animations: Int): Builder {
            this.animations = animations
            return this
        }

        fun build(): ListDialogFragment {
            val fragment = ListDialogFragment()
            fragment.setTitle(title)
            fragment.setData(data)
            fragment.setOnItemClickListener(clickListener)
            fragment.setCancel(cancel)
            fragment.setCancelColor(cancelColor)
            fragment.setOnCancelClickListener(cancelClickListener)
            fragment.arguments = bundle
            fragment.setWindowAnimations(animations)
            return fragment
        }

        fun show(): ListDialogFragment {
            val fragment = build()
            val transaction = context.supportFragmentManager.beginTransaction().setTransition(
                FragmentTransaction.TRANSIT_FRAGMENT_FADE
            )
            fragment.show(transaction, null)
            return fragment
        }
    }

    private fun setCancelColor(cancelColor: Int) {
        this.cancelColor = cancelColor
    }

    private fun setWindowAnimations(animations: Int) {
        this.animations = animations
    }

    private fun setData(data: List<String>?) {
        this.data = data
    }

    private fun setOnCancelClickListener(cancelClickListener: OnDialogCancelClickListener?) {
        this.cancelClickListener = cancelClickListener
    }

    private fun setCancel(cancel: String?) {
        this.cancel = cancel
    }

    private fun setOnItemClickListener(clickListener: ClickListener?) {
        itemClickListener = clickListener
    }

    private fun setTitle(title: String?) {
        this.title = title
    }


    interface OnDialogCancelClickListener {
        fun OnCancel(view: View?)
    }

    private class DefaultAdapter :
        BaseBindAdapter<String, DialogDefaultListItemBinding>(R.layout.dialog_default_list_item) {
        override fun convert(
            holder: BaseDataBindingHolder<DialogDefaultListItemBinding>,
            item: String
        ) {
            super.convert(holder, item)
            holder.dataBinding?.let {
                it.tvTitle.text = item
            }
        }
    }
}