package com.hyphenate.easeim.section.ui.chat.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.hyphenate.easeim.AppClient
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.manager.PushAndMessageHelper
import com.hyphenate.easeim.databinding.ActivityContactListBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeim.section.ui.chat.adapter.PickUserAdapter
import com.hyphenate.easeim.section.dialog_ktx.SimpleDialogFragment
import com.hyphenate.easeim.section.ui.chat.ChatActivity
import com.hyphenate.easeim.section.ui.contact.vm.ContactListVm
import com.hyphenate.easeui.utils.EaseCommonUtils
import com.hyphenate.easeui.widget.EaseSidebar.OnTouchEventListener
import com.hyphenate.easeui.widget.EaseTitleBar.OnBackPressListener
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshListener

class ForwardMessageActivity(override val layoutId: Int = R.layout.activity_contact_list) :
    BaseInitActivityKtx<ActivityContactListBinding>(), OnRefreshListener,
    OnTouchEventListener, OnBackPressListener {
    private val mAdapter by lazy { PickUserAdapter() }
    private val mViewModel by viewModels<ContactListVm>()
    private var mForwardMsgId: String? = null
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mForwardMsgId = intent.getStringExtra("forward_msg_id")
        binding.rvContactList.adapter = mAdapter
    }

    override fun initListener() {
        super.initListener()
        binding.srlRefresh.setOnRefreshListener(this)
        mAdapter.setOnItemClickListener { _, _, position ->
            val user = mAdapter.data[position]
            SimpleDialogFragment.Builder(this)
                .setTitle(getString(R.string.confirm_forward_to, user.nickname))
                .setOnConfirmClickListener {
                    PushAndMessageHelper.sendForwardMessage(user.username, mForwardMsgId)
                    finish()
                }
                .showCancelButton(true)
                .show()
        }
        binding.sideBarPickUser.setOnTouchEventListener(this)
        binding.titleBarContactList.setOnBackPressListener(this)
    }

    override fun initData() {
        super.initData()
        mViewModel.contactList.observe(this) {
            mAdapter.setList(it)
            finishRefresh()
        }
        mViewModel.contactList()
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        mViewModel.contactList()
    }

    private fun finishChatActivity() {
        AppClient.instance.lifecycleCallbacks.finishTarget(ChatActivity::class.java)
    }

    override fun onActionDown(event: MotionEvent, pointer: String) {
        showFloatingHeader(pointer)
        moveToRecyclerItem(pointer)
    }

    override fun onActionMove(event: MotionEvent, pointer: String) {
        showFloatingHeader(pointer)
        moveToRecyclerItem(pointer)
    }

    override fun onActionUp(event: MotionEvent) {
        hideFloatingHeader()
    }

    override fun onBackPress(view: View) {
        onBackPressed()
    }

    private fun moveToRecyclerItem(pointer: String) {
        val data = mAdapter.data
        if (data.isEmpty()) {
            return
        }
        data.forEachIndexed { index, easeUser ->
            if (EaseCommonUtils.getLetter(easeUser.nickname) == pointer) {
                val manager = binding.rvContactList.layoutManager as LinearLayoutManager?
                manager?.scrollToPositionWithOffset(index, 0)
            }
        }
    }

    /**
     * 展示滑动的字符
     *
     * @param pointer
     */
    private fun showFloatingHeader(pointer: String) {
        if (TextUtils.isEmpty(pointer)) {
            hideFloatingHeader()
            return
        }
        binding.floatingHeader.text = pointer
        binding.floatingHeader.visibility = View.VISIBLE
    }

    private fun hideFloatingHeader() {
        binding.floatingHeader.visibility = View.GONE
    }

    private fun finishRefresh() {
        runOnUiThread { binding.srlRefresh.finishRefresh() }
    }


    companion object {
        fun actionStart(context: Context, forward_msg_id: String?) {
            val starter = Intent(context, ForwardMessageActivity::class.java)
            starter.putExtra("forward_msg_id", forward_msg_id)
            context.startActivity(starter)
        }
    }
}