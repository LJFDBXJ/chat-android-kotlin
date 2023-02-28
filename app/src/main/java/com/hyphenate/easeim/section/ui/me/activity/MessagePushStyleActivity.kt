package com.hyphenate.easeim.section.ui.me.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.hyphenate.chat.EMPushManager.DisplayStyle
import com.hyphenate.easeim.R
import com.hyphenate.easeim.databinding.ActivityMessagePushStyleBinding
import com.hyphenate.easeim.databinding.ItemMessageStyleBinding
import com.hyphenate.easeim.section.base_ktx.BaseActivityKtx
import com.hyphenate.easeim.section.base_ktx.BaseBindAdapter
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeim.section.ui.me.vm.PushStyleVm
import com.hyphenate.easeui.widget.EaseTitleBar.OnBackPressListener
import java.util.*

class MessagePushStyleActivity(override val layoutId: Int = R.layout.activity_message_push_style) :
    BaseInitActivityKtx<ActivityMessagePushStyleBinding>(),
    OnBackPressListener {
    private var selectedPosition = 0
    private val viewModel by viewModels<PushStyleVm>()


    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        selectedPosition = intent.getIntExtra("position", 0)
    }

    override fun initListener() {
        super.initListener()
        binding.titleBar.setOnBackPressListener(this)
    }

    override fun initData() {
        super.initData()
        val adapter = MessageStyleAdapter()
        binding.rvList.adapter = adapter
        binding.rvList.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        val values = DisplayStyle.values()
        val styles = ArrayList<DisplayStyle>()
        Collections.addAll(styles, *values)
        adapter.setList(styles)
        adapter.setOnItemClickListener { _, _, position ->
            selectedPosition = position
            adapter.notifyItemRangeChanged(0, adapter.data.size)
            viewModel.updateStyle(values[selectedPosition])
        }
        viewModel.pushStyleObservable.observe(this) {
            if (it){
                val intent = intent.putExtra("position", selectedPosition)
                setResult(RESULT_OK, intent)
                finish()
//            showLoading()
//            dismissLoading()
            }
        }
    }

    override fun onBackPress(view: View) {
        onBackPressed()
    }

    private inner class MessageStyleAdapter :
        BaseBindAdapter<DisplayStyle, ItemMessageStyleBinding>(layoutResId = R.layout.item_message_style) {
        override fun convert(
            holder: BaseDataBindingHolder<ItemMessageStyleBinding>,
            item: DisplayStyle
        ) {
            super.convert(holder, item)
            holder.dataBinding?.let { bind ->
                bind.tvName.setText(names[item.ordinal])
                bind.cbStyle.isChecked = selectedPosition == holder.absoluteAdapterPosition
            }
        }
    }

    companion object {
        private val names =
            intArrayOf(R.string.push_message_style_simple, R.string.push_message_show_detail)

        fun actionStartForResult(context: BaseActivityKtx, position: Int, requestCode: Int) {
            val intent = Intent(context, MessagePushStyleActivity::class.java)
            intent.putExtra("position", position)
            context.startActivityForResult(intent, requestCode)
        }
    }
}