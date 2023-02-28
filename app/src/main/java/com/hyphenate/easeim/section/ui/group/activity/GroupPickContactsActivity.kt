package com.hyphenate.easeim.section.ui.group.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.hyphenate.easeim.R
import com.hyphenate.easeim.databinding.ActivityChatGroupPickContactsBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeim.section.ui.group.adapter.GroupPickContactsAdapter
import com.hyphenate.easeim.section.ui.group.vm.GroupPickContactsVm
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.easeui.manager.SidebarPresenter
import com.hyphenate.easeui.widget.EaseTitleBar.OnRightClickListener
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshListener

/**
 * @author LXJDBXJ
 * @date 2022/10/10
 * @desc 现有好友挑选成员
 */
open class GroupPickContactsActivity(override val layoutId: Int = R.layout.activity_chat_group_pick_contacts) :
    BaseInitActivityKtx<ActivityChatGroupPickContactsBinding>(), OnRightClickListener,
    OnRefreshListener, GroupPickContactsAdapter.OnSelectListener {

    protected val adapter by lazy { GroupPickContactsAdapter() }
    private val viewModel by viewModels<GroupPickContactsVm>()
    private var groupId: String? = null
    private var isOwner = false
    private var newmembers: Array<String>? = null
    private var keyword: String? = null
    private var contacts: List<EaseUser>? = null
    private var presenter: SidebarPresenter? = null


    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        groupId = intent.getStringExtra("groupId")
        isOwner = intent.getBooleanExtra("isOwner", false)
        newmembers = intent.getStringArrayExtra("newMembers")
        binding.titleBar.rightText
            .setTextColor(ContextCompat.getColor(this, R.color.em_color_brand))
        adapter.isCreateGroup = groupId.isNullOrEmpty()
        binding.rvList.adapter = adapter
        presenter = SidebarPresenter()
        presenter?.setupWithRecyclerView(binding.rvList, adapter, binding.floatingHeader)
    }

    override fun initListener() {
        super.initListener()
        binding.titleBar.setOnBackPressListener {
            onBackPressed()
        }
        binding.titleBar.setOnRightClickListener(this)
        binding.srlRefresh.setOnRefreshListener(this)
        binding.sidebar.setOnTouchEventListener(presenter)
        adapter.setOnSelectListener(this)
        binding.query.doAfterTextChanged {
            keyword = it.toString()
            binding.searchClear.visibility = if (!TextUtils.isEmpty(keyword)) {
                viewModel.getSearchContacts(keyword)
                View.VISIBLE

            } else {
                adapter.setList(contacts)
                View.INVISIBLE
            }
        }
        binding.searchClear.setOnClickListener {
            binding.query.text?.clear()
            adapter.setList(contacts)
        }
    }

    override fun initData() {
        super.initData()
        viewModel.contacts.observe(this) {
            contacts = it
            adapter.setList(it)
            if (!TextUtils.isEmpty(groupId)) {
                viewModel.getGroupMembers(groupId)
            } else {
                if (newmembers != null) {
                    adapter.setExistMember(listOf(*newmembers!!))
                }
            }
            finishRefresh()

        }
        viewModel.groupMembers.observe(this) {
            it ?: return@observe
            adapter.setExistMember(it)
        }
        viewModel.addMembers.observe(this) {
            if (it) {
                setResult(RESULT_OK)
                finish()
            }
        }
        viewModel.searchContacts.observe(this) {
            adapter.setList(it)
        }
        viewModel.allContacts
    }

    private fun finishRefresh() {
        runOnUiThread { binding.srlRefresh.finishRefresh() }
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        viewModel.allContacts
    }

    override fun onRightClick(view: View) {
        val selectedMembers = adapter.selectedMembers
        if (selectedMembers.isEmpty()) {
            setResult(RESULT_OK)
            finish()
            return
        }
        val newMembers = selectedMembers.toTypedArray()
        if (TextUtils.isEmpty(groupId)) {
            val intent = intent.putExtra("newMembers", newMembers)
            setResult(RESULT_OK, intent)
            finish()
            return
        }
        viewModel.addGroupMembers(isOwner, groupId, newMembers)
    }

    override fun onSelected(v: View?, selectedMembers: List<String>?) {
        binding.titleBar.rightText.text =
            getString(R.string.finish) + "(" + selectedMembers?.size + ")"
    }


    companion object {
        fun actionStartForResult(context: Activity, newmembers: Array<String>?, requestCode: Int) {
            val starter = Intent(context, GroupPickContactsActivity::class.java)
            starter.putExtra("newMembers", newmembers)
            context.startActivityForResult(starter, requestCode)
        }


        fun actionStartForResult(
            context: Activity,
            groupId: String?,
            owner: Boolean,
            launch: ActivityResultLauncher<Intent>?
        ) {
            val starter = Intent(context, GroupPickContactsActivity::class.java)
            starter.putExtra("groupId", groupId)
            starter.putExtra("isOwner", owner)
//            context.startActivityForResult(starter, requestCode)
            launch?.launch(starter)
        }
    }

}