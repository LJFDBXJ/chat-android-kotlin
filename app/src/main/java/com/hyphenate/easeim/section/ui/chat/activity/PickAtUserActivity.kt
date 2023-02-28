package com.hyphenate.easeim.section.ui.chat.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.R
import com.hyphenate.easeim.databinding.ActivityChatPickAtUserBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeim.section.ui.chat.adapter.PickAllUserAdapter
import com.hyphenate.easeim.section.ui.chat.adapter.PickUserAdapter
import com.hyphenate.easeim.section.ui.contact.vm.GroupContactVm
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.easeui.manager.EaseThreadManager
import com.hyphenate.easeui.utils.EaseCommonUtils
import com.hyphenate.easeui.widget.EaseSidebar.OnTouchEventListener
import com.hyphenate.easeui.widget.EaseTitleBar.OnBackPressListener
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshListener

class PickAtUserActivity(override val layoutId: Int = R.layout.activity_chat_pick_at_user) :
    BaseInitActivityKtx<ActivityChatPickAtUserBinding>(),
    OnRefreshListener, OnTouchEventListener, OnBackPressListener {
    private var mGroupId: String? = null
    private val mViewModel by viewModels<GroupContactVm>()
    private val mAdapter: PickUserAdapter by lazy { PickUserAdapter() }
    private val baseAdapter by lazy { ConcatAdapter() }
    private var headerAdapter: PickAllUserAdapter? = null


    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mGroupId = intent.getStringExtra("groupId")
        baseAdapter.addAdapter(mAdapter)
        binding.rvPickUserList.adapter = baseAdapter
    }

    override fun initListener() {
        super.initListener()
        binding.srlRefresh.setOnRefreshListener(this)
        mAdapter.setOnItemClickListener { _, _, position ->
            val user = mAdapter.data[position]
            if (TextUtils.equals(user.username, SdkHelper.instance.currentUser)) {
                return@setOnItemClickListener
            }
            val intent = intent
            intent.putExtra("username", user.username)
            setResult(RESULT_OK, intent)
            finish()
        }
        binding.sideBarPickUser.setOnTouchEventListener(this)
        binding.titleBarPick.setOnBackPressListener(this)
    }

    override fun initData() {
        super.initData()
        mViewModel.groupMember.observe(this) {
            if (it != null) {
                checkIfAddHeader()
            }
            removeSelf(it as ArrayList<EaseUser>?)
            mAdapter.setList(it)
            finishRefresh()

        }
        mViewModel.getGroupMembers(mGroupId)
    }

    private fun removeSelf(data: ArrayList<EaseUser>?) {
        if (data.isNullOrEmpty()) {
            return
        }
        val iterator = data.iterator()
        while (iterator.hasNext()) {
            val user = iterator.next()
            if (user.username == SdkHelper.instance.currentUser) {
                iterator.remove()
            }
        }
    }

    private fun checkIfAddHeader() {
        val group = SdkHelper.instance.groupManager.getGroup(mGroupId)
        if (group != null) {
            if (group.owner == SdkHelper.instance.currentUser) {
                addHeader()
            }
        }
    }

    private fun addHeader() {
        if (headerAdapter == null) {
            headerAdapter = PickAllUserAdapter()
            val user = EaseUser(getString(R.string.all_members))
            user.avatar = R.drawable.ease_groups_icon.toString() + ""
            val users: MutableList<EaseUser> = ArrayList()
            users.add(user)
            headerAdapter?.data = users
        }
        if (!baseAdapter.adapters.contains(headerAdapter)) {
            baseAdapter.addAdapter(0, headerAdapter!!)
            headerAdapter?.setOnItemClickListener { _, _, position ->
                setResult(
                    RESULT_OK,
                    Intent().putExtra("username", headerAdapter!!.getItem(position).username)
                )
                finish()
            }
        }
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        mViewModel.getGroupMembers(mGroupId)
    }

    private fun finishRefresh() {
        EaseThreadManager.getInstance().runOnMainThread { binding.srlRefresh.finishRefresh() }
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

    private fun moveToRecyclerItem(pointer: String) {
        val data = mAdapter.data
        if (data.isEmpty()) {
            return
        }
        for (i in data.indices) {
            if (TextUtils.equals(EaseCommonUtils.getLetter(data[i].nickname), pointer)) {
                val manager = binding.rvPickUserList.layoutManager as LinearLayoutManager?
                manager?.scrollToPositionWithOffset(i, 0)
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

    override fun onBackPress(view: View) {
        onBackPressed()
    }

    companion object {
        fun actionStartForResult(fragment: Fragment, groupId: String?, requestCode: Int) {
            val starter = Intent(fragment.context, PickAtUserActivity::class.java)
            starter.putExtra("groupId", groupId)
            fragment.startActivityForResult(starter, requestCode)
        }
    }
}