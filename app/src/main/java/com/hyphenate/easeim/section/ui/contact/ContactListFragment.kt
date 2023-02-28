package com.hyphenate.easeim.section.ui.contact

import com.hyphenate.easeui.modules.contact.EaseContactListFragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.hyphenate.easeim.section.ui.contact.vm.ContactsVm
import android.os.Bundle
import com.hyphenate.easeim.R
import com.hyphenate.easeui.modules.menu.EasePopupMenuHelper
import com.hyphenate.easeui.domain.EaseUser
import android.view.MenuItem
import android.view.View
import com.hyphenate.easeim.section.ui.contact.activity.AddContactActivity
import com.hyphenate.easeim.common.enums.SearchType
import com.hyphenate.easeim.section.ui.contact.activity.GroupContactManageActivity
import com.hyphenate.easeim.section.ui.contact.activity.ChatRoomContactManageActivity
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.section.search.SearchFriendsActivity
import com.hyphenate.easeim.section.dialog_ktx.SimpleDialogFragment
import com.hyphenate.easeim.section.ui.contact.activity.ContactDetailActivity
import androidx.fragment.app.activityViewModels
import com.hyphenate.easeim.common.ktx.toast
import com.hyphenate.easeim.common.livedatas.obs
import com.hyphenate.easeim.databinding.LayoutSearchBinding
import com.hyphenate.easeim.section.base_ktx.BaseActivityKtx

class ContactListFragment : EaseContactListFragment(), View.OnClickListener,
    SwipeRefreshLayout.OnRefreshListener {
    private val mViewModel by activityViewModels<ContactsVm>()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        addSearchView()
        //设置无数据时空白页面
        //contactLayout.contactList.listAdapter.setEmptyLayoutResource(R.layout.demo_layout_friends_empty_list)

        //添加头布局
        mViewModel.initHeadItem(contactList = contactLayout.contactList)

        //设置为简洁模式
        //contactLayout.showSimple();

        //获取列表控件
        val contactList = contactLayout.contactList;

        //设置条目高度
//         contactList.setItemHeight( EaseCommonUtils.dip2px(mContext, 80f).toInt())

        //设置条目背景
        //contactList.setItemBackGround(ContextCompat.getDrawable(mContext, R.color.gray));

        //设置头像样式
        //contactList.setAvatarShapeType(EaseImageView.ShapeType.ROUND);

        //设置头像圆角
        //contactList.setAvatarRadius( EaseCommonUtils.dip2px(mContext, 5f).toInt())

        //设置header背景
        // contactList.setHeaderBackGround(ContextCompat.getDrawable(mContext, R.color.white));
    }

    private fun addSearchView() {
        //添加搜索会话布局
        val bind = LayoutSearchBinding.inflate(layoutInflater)
        llRoot.addView(bind.root, 0)
        bind.search.setOnClickListener(this)
    }

    override fun onMenuPreShow(menuHelper: EasePopupMenuHelper, position: Int) {
        super.onMenuPreShow(menuHelper, position)
        menuHelper.addItemMenu(
            1,
            R.id.action_friend_block,
            2,
            getString(R.string.em_friends_move_into_the_blacklist_new)
        )
        menuHelper.addItemMenu(
            1,
            R.id.action_friend_delete,
            1,
            getString(R.string.ease_friends_delete_the_contact)
        )
    }

    override fun onMenuItemClick(item: MenuItem, position: Int): Boolean {
        val user = contactLayout.contactList.getItem(position)
        when (item.itemId) {
            // 添加到黑名单
            R.id.action_friend_block -> {
                mViewModel.addUserToBlackList(user.username, false)
                return true
            }
            // 删除 好友
            R.id.action_friend_delete -> {
                showDeleteDialog(user = user)
                return true
            }
        }
        return super.onMenuItemClick(item, position)
    }

    override fun initListener() {
        super.initListener()

        contactLayout.swipeRefreshLayout.setOnRefreshListener(this)

        /**
         * 通讯录 顶部 新的好友 群聊 聊天室
         */
        contactLayout.contactList.setOnCustomItemClickListener { _, position ->
            val item = contactLayout.contactList.customAdapter.getItem(position)
            when (item.id) {
                R.id.contact_header_item_new_chat -> {
                    //添加联系人
                    AddContactActivity.startAction(context = mContext, type = SearchType.CHAT)
                }
                R.id.contact_header_item_group_list -> {
                    //群主列表
                    GroupContactManageActivity.actionStart(context = mContext)
                }
                R.id.contact_header_item_chat_room_list -> {
                    //联系人列表管理
                    ChatRoomContactManageActivity.actionStart(context = mContext)
                }

            }
        }
    }

    override fun initData() {
        // 好友列表结果
        mViewModel.contact.observe(this) {
            contactLayout.contactList.setData(it)
            contactLayout.swipeRefreshLayout.isRefreshing = false
        }

        // 加入黑名单结果
        mViewModel.blackResult.observe(this) {
            if (it) {
                context.toast(R.string.em_friends_move_into_blacklist_success)
                mViewModel.loadContactList(fetchServer = false)
            }
        }
        // 删除好友结果
        mViewModel.deleteUser.observe(this) {
            if (it) {
                mViewModel.loadContactList(fetchServer = false)
            }
        }

        arrayOf(
            DemoConstant.REMOVE_BLACK,
            DemoConstant.CONTACT_ADD,
            DemoConstant.CONTACT_DELETE,
            DemoConstant.CONTACT_UPDATE,
            DemoConstant.CONTACT_CHANGE,
        ).obs<EaseEvent>(this) { key, result ->
            result ?: return@obs
            when (key) {
                DemoConstant.REMOVE_BLACK,
                DemoConstant.CONTACT_DELETE,
                DemoConstant.CONTACT_UPDATE,
                DemoConstant.CONTACT_CHANGE,
                DemoConstant.NOTIFY_CHANGE,
                DemoConstant.CONTACT_ADD -> {
                    if (result.isContactChange) {
                        mViewModel.loadContactList(fetchServer = false)
                    }
                }

            }
        }
        DemoConstant.NOTIFY_CHANGE.obs<EaseEvent>(this) {
            mViewModel.loadContactList(fetchServer = true)
        }
        mViewModel.loadContactList(true)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.search ->
                SearchFriendsActivity.actionStart(context = mContext)
        }
    }

    /**
     * 显示删除好友 对话框
     */
    private fun showDeleteDialog(user: EaseUser) {
        SimpleDialogFragment.Builder(mContext as BaseActivityKtx)
            .setTitle(R.string.ease_friends_delete_contact_hint)
            .setOnConfirmClickListener {
                mViewModel.deleteContact(userName = user.username)
            }
            .showCancelButton(true)
            .show()
    }

    override fun onItemClick(view: View, position: Int) {
        super.onItemClick(view, position)
        val user = contactLayout.contactList.getItem(position)
        ContactDetailActivity.actionStart(context = mContext, user = user)
    }

    override fun onRefresh() {
        mViewModel.loadContactList(fetchServer = true)
    }
}