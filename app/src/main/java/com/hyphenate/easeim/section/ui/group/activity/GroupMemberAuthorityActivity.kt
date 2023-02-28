package com.hyphenate.easeim.section.ui.group.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.appcompat.widget.PopupMenu
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.chad.library.adapter.base.listener.OnItemLongClickListener
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMGroup
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.interfaceOrImplement.OnResourceParseCallback
import com.hyphenate.easeim.common.livedatas.LiveDataBus.Companion.get
import com.hyphenate.easeim.common.livedatas.obs
import com.hyphenate.easeim.common.utils.SpDbModel
import com.hyphenate.easeim.databinding.ActivityGroupMemberAuthorityBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeim.section.dialog_ktx.SimpleDialogFragment
import com.hyphenate.easeim.section.ui.group.GroupHelper
import com.hyphenate.easeim.section.ui.group.vm.GroupMemberAuthorityVm
import com.hyphenate.easeim.section.ui.contact.activity.ContactDetailActivity
import com.hyphenate.easeim.section.ui.contact.adapter.ContactListAdapter
import com.hyphenate.easeim.section.ui.me.activity.UserDetailActivity
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.easeui.manager.SidebarPresenter
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.easeui.widget.EaseTitleBar.OnBackPressListener
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshListener

open class GroupMemberAuthorityActivity(override val layoutId: Int = R.layout.activity_group_member_authority) :
    BaseInitActivityKtx<ActivityGroupMemberAuthorityBinding>(), OnBackPressListener,
    OnRefreshListener, OnItemClickListener, OnItemLongClickListener {
    private var presenter: SidebarPresenter? = null
    protected val adapter by lazy { ContactListAdapter() }
    protected val viewModel by viewModels<GroupMemberAuthorityVm>()
    protected var groupId: String = ""
    protected var muteMembers = ArrayList<String>()
    protected var blackMembers = ArrayList<String>()
    protected var flag = 0 //作为切换的flag
    var group: EMGroup? = null
    private var launch: ActivityResultLauncher<Intent>? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        groupId = intent.getStringExtra("groupId") ?: ""
        flag = intent.getIntExtra("type", 0)
        binding.titleBar.setTitle(getString(R.string.em_group_member_type_member))
        binding.rvList.adapter = adapter
        presenter = SidebarPresenter()
        presenter?.setupWithRecyclerView(binding.rvList, adapter, binding.floatingHeader)
        launch = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
//                    REQUEST_CODE_ADD_USER ->
                refreshData()
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        when (flag) {
            TYPE_MEMBER -> {
                menu.findItem(R.id.action_group_black).isVisible = true
                menu.findItem(R.id.action_group_mute).isVisible = true
                menu.findItem(R.id.action_group_add).isVisible = true
            }
            TYPE_BLACK -> {
                menu.findItem(R.id.action_group_member).isVisible = true
                menu.findItem(R.id.action_group_mute).isVisible = true
            }
            TYPE_MUTE -> {
                menu.findItem(R.id.action_group_member).isVisible = true
                menu.findItem(R.id.action_group_black).isVisible = true
            }
        }
        onSubPrepareOptionsMenu(menu)
        return false /*super.onPrepareOptionsMenu(menu)*/
    }

    protected open fun onSubPrepareOptionsMenu(menu: Menu) {
        //对角色进行判断
        if (!isOwner && !isInAdminList(SdkHelper.instance.currentUser)) {
            menu.findItem(R.id.action_group_black).isVisible = false
            menu.findItem(R.id.action_group_mute).isVisible = false
        }
        if (!GroupHelper.isCanInvite(group)) {
            menu.findItem(R.id.action_group_add).isVisible = false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.demo_group_member_authority_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_group_member -> {
                flag = TYPE_MEMBER
                refreshData()
                invalidateOptionsMenu()
            }
            R.id.action_group_black -> {
                flag = TYPE_BLACK
                refreshData()
                invalidateOptionsMenu()
            }
            R.id.action_group_mute -> {
                flag = TYPE_MUTE
                refreshData()
                invalidateOptionsMenu()
            }
            R.id.action_group_add -> GroupPickContactsActivity.actionStartForResult(
                context = this,
                groupId = groupId,
                owner = false,
                launch
            )
        }
        return super.onOptionsItemSelected(item)
    }

    override fun initListener() {
        super.initListener()
        binding.titleBar.setOnBackPressListener(this)
        adapter.setOnItemClickListener(this)
        adapter.setOnItemLongClickListener(this)
        binding.sidebar.setOnTouchEventListener(presenter)
        binding.srlRefresh.setOnRefreshListener(this)
    }

    override fun initData() {
        super.initData()
        getGroup()
        data
    }

    private fun getGroup() {
        group = SdkHelper.instance.groupManager.getGroup(groupId)
    }

    protected open fun refreshData() {
        if (flag == TYPE_MEMBER) {
            viewModel.getMembers(groupId)
        }
        if (isOwner || isInAdminList(SdkHelper.instance.currentUser)) {
            viewModel.getBlackMembers(groupId)
            viewModel.getMuteMembers(groupId)
        }
        when (flag) {
            TYPE_MEMBER -> {
                binding.titleBar.setTitle(getString(R.string.em_authority_menu_member_list))
            }
            TYPE_BLACK -> {
                binding.titleBar.setTitle(getString(R.string.em_authority_menu_black_list))
            }
            else -> {
                binding.titleBar.setTitle(getString(R.string.em_authority_menu_mute_list))
            }
        }
    }

    //监听有关用户属性的事件
    open val data: Unit
        get() {
            viewModel.members.observe(this) {
                adapter.setList(it)
                finishRefresh()
            }
            viewModel.muteMembers.observe(this) { response ->
                parseResource(
                    response = response,
                    object : OnResourceParseCallback<Map<String, Long>?>() {
                        override fun onSuccess(data: Map<String, Long>?) {
                            muteMembers.clear()
                            muteMembers.addAll(data!!.keys)
                            if (flag == TYPE_MUTE) {
                                val muteUsers = EaseUser.parse(muteMembers)
                                adapter.setList(muteUsers)
                            }
                        }

                        override fun hideLoading() {
                            super.hideLoading()
                            if (flag == TYPE_MUTE) {
                                finishRefresh()
                            }
                        }
                    })
            }
            viewModel.blackMembers.observe(this) { response ->
                parseResource(
                    response = response,
                    callback = object : OnResourceParseCallback<List<String>?>() {
                        override fun onSuccess(data: List<String>?) {
                            blackMembers.clear()
                            blackMembers.addAll(data!!)
                            if (flag == TYPE_BLACK) {
                                val blackUsers = EaseUser.parse(blackMembers)
                                adapter.setList(blackUsers)
                            }
                        }

                        override fun hideLoading() {
                            super.hideLoading()
                            if (flag == TYPE_BLACK) {
                                finishRefresh()
                            }
                        }
                    })
            }
            viewModel.refresh.observe(this) { response ->
                parseResource<String>(
                    response!!, object : OnResourceParseCallback<String>() {
                        override fun onSuccess(message: String?) {
                            refreshData()
                            get().use(DemoConstant.GROUP_CHANGE).postValue(
                                EaseEvent.create(
                                    DemoConstant.GROUP_CHANGE,
                                    EaseEvent.TYPE.GROUP
                                )
                            )
                        }
                    })
            }
            viewModel.transferOwner.observe(this) { response ->
                parseResource<Boolean>(
                    response!!, object : OnResourceParseCallback<Boolean>() {
                        override fun onSuccess(data: Boolean?) {
                            get().use(DemoConstant.GROUP_CHANGE).postValue(
                                EaseEvent.create(
                                    DemoConstant.GROUP_OWNER_TRANSFER,
                                    EaseEvent.TYPE.GROUP
                                )
                            )
                            finish()
                        }
                    })
            }

            arrayOf(
                DemoConstant.GROUP_CHANGE,
                DemoConstant.CONTACT_CHANGE,
                DemoConstant.CONTACT_UPDATE,
                DemoConstant.CONTACT_ADD
            ).obs<EaseEvent>(this) { key, event ->
                event ?: return@obs
                when (key) {
                    DemoConstant.GROUP_CHANGE -> {
                        if (event.isGroupChange) {
                            refreshData()
                        } else if (event.isGroupLeave && TextUtils.equals(groupId, event.message)) {
                            finish()
                        }
                    }
                    DemoConstant.CONTACT_ADD,
                    DemoConstant.CONTACT_UPDATE,
                    DemoConstant.CONTACT_CHANGE -> {
                        //监听有关用户属性的事件
                        refreshData()
                    }
                }
            }
            refreshData()
        }

    fun finishRefresh() {
        runOnUiThread { binding.srlRefresh.finishRefresh() }
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        refreshData()
    }

    override fun onBackPress(view: View) {
        onBackPressed()
    }

    protected open fun addToAdmins(username: String) {
        viewModel.addGroupAdmin(groupId, username)
    }

    protected open fun removeFromAdmins(username: String) {
        viewModel.removeGroupAdmin(groupId, username)
    }

    protected open fun transferOwner(username: String) {
        viewModel.changeOwner(groupId, username)
    }

    protected open fun removeFromGroup(username: String) {
        viewModel.removeUserFromGroup(groupId, username)
    }

    protected open fun addToBlack(username: String) {
        viewModel.blockUser(groupId, username)
    }

    protected open fun removeFromBlacks(username: String) {
        viewModel.unblockUser(groupId, username)
    }

    protected open fun addToMuteMembers(username: String) {
        val mutes: MutableList<String> = ArrayList()
        mutes.add(username)
        viewModel.muteGroupMembers(groupId, mutes, (20 * 60 * 1000).toLong())
    }

    protected open fun removeFromMuteMembers(username: String) {
        val unMutes = ArrayList<String>()
        unMutes.add(username)
        viewModel.unMuteGroupMembers(groupId, unMutes)
    }

    /**
     * 修改菜单项
     * @param menu
     */
    protected fun setMenuInfo(menu: Menu?) {}
    open val isOwner: Boolean
        get() = GroupHelper.isOwner(group)

    open fun isInAdminList(username: String): Boolean {
        return GroupHelper.isInAdminList(username = username, adminList = group!!.adminList)
    }

    fun isInMuteList(username: String): Boolean {
        return GroupHelper.isInMuteList(username = username, muteMembers = muteMembers)
    }

    fun isInBlackList(username: String): Boolean {
        return GroupHelper.isInBlackList(username = username, blackMembers = blackMembers)
    }

    open val isMember: Boolean
        get() = !GroupHelper.isAdmin(group = group) && !isOwner

    /**
     * 设置菜单条目可见
     * @param menu
     * @param actionId
     */
    protected fun setMenuItemVisible(menu: Menu, @IdRes actionId: Int) {
        menu.findItem(actionId).isVisible = true
    }


    override fun onItemClick(m: BaseQuickAdapter<*, *>, view: View, position: Int) {
        val user = adapter.getItem(position)
        if (user.username == EMClient.getInstance().currentUser) {
            UserDetailActivity.actionStart(this, null, null)
        } else {
            ContactDetailActivity.actionStart(
                this,
                user,
                SpDbModel.instance.isContact(user.username)
            )
        }
    }

    override fun onItemLongClick(
        m: BaseQuickAdapter<*, *>,
        view: View,
        position: Int
    ): Boolean {
        if (isMember) {
            return true
        }
        val menu = PopupMenu(this, view)
        menu.gravity = Gravity.CENTER_HORIZONTAL
        menu.menuInflater.inflate(R.menu.demo_group_member_authority_item_menu, menu.menu)
//        val menuPopupHelper = MenuPopupHelper(this, (menu.menu as MenuBuilder), view)
//        menuPopupHelper.setForceShowIcon(true)
//        menuPopupHelper.gravity = Gravity.CENTER_HORIZONTAL
        val item = adapter.getItem(position)
        val username = item.username
        setMenuInfo(menu.menu)
        when {
            isInBlackList(username = username) -> {
                setMenuItemVisible(menu.menu, R.id.action_group_remove_black)
            }
            isInMuteList(username = username) -> {
                if (flag != TYPE_MUTE) {
                    menu.menu.findItem(R.id.action_group_add_admin).isVisible = isOwner
                    setMenuItemVisible(menu.menu, R.id.action_group_remove_member)
                    setMenuItemVisible(menu.menu, R.id.action_group_add_black)
                }
                setMenuItemVisible(menu.menu, R.id.action_group_unmute)
            }
            isInAdminList(username) && isOwner -> {
                setMenuItemVisible(menu.menu, R.id.action_group_remove_admin)
                setMenuItemVisible(menu.menu, R.id.action_group_transfer_owner)
            }
            else -> {
                menu.menu.findItem(R.id.action_group_add_admin).isVisible = isOwner
                menu.menu.findItem(R.id.action_group_transfer_owner).isVisible = isOwner
                setMenuItemVisible(menu.menu, R.id.action_group_remove_member)
                setMenuItemVisible(menu.menu, R.id.action_group_add_black)
                setMenuItemVisible(menu.menu, R.id.action_group_mute)
            }
        }

        menu.show()
//        menuPopupHelper.show()
        menu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_group_add_admin -> addToAdmins(username = username)
                R.id.action_group_remove_admin -> removeFromAdmins(username = username)
                R.id.action_group_transfer_owner -> transferOwner(username = username)
                R.id.action_group_remove_member ->
                    SimpleDialogFragment.Builder(this)
                        .setTitle(R.string.em_authority_remove_group)
                        .setOnConfirmClickListener {
                            removeFromGroup(username = username)
                        }
                        .showCancelButton(true)
                        .show()
                R.id.action_group_add_black -> addToBlack(username = username)
                R.id.action_group_remove_black -> removeFromBlacks(username = username)
                R.id.action_group_mute -> addToMuteMembers(username = username)
                R.id.action_group_unmute -> removeFromMuteMembers(username = username)
            }
            false
        }
        return true
    }


    companion object {
        const val REQUEST_CODE_ADD_USER = 0
        const val TYPE_MEMBER = 0
        const val TYPE_BLACK = 1
        const val TYPE_MUTE = 2
        protected const val TYPE_TRANSFER = 3
        fun actionStart(context: Context, groupId: String?) {
            val starter = Intent(context, GroupMemberAuthorityActivity::class.java)
            starter.putExtra("groupId", groupId)
            context.startActivity(starter)
        }

        fun actionStart(context: Context, groupId: String?, type: Int) {
            val starter = Intent(context, GroupMemberAuthorityActivity::class.java)
            starter.putExtra("groupId", groupId)
            starter.putExtra("type", type)
            context.startActivity(starter)
        }
    }
}