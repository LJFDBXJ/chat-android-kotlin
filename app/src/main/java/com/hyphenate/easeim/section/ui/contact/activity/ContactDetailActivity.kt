package com.hyphenate.easeim.section.ui.contact.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import com.hyphenate.easecallkit.EaseCallKit
import com.hyphenate.easecallkit.base.EaseCallType
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.db.DbHelper
import com.hyphenate.easeim.common.ktx.toast
import com.hyphenate.easeim.common.livedatas.LiveDataBus
import com.hyphenate.easeim.common.utils.SpDbModel
import com.hyphenate.easeim.databinding.ActivityFriendsContactDetailBinding
import com.hyphenate.easeim.section.av.VideoCallActivity
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeim.section.ui.chat.ChatActivity
import com.hyphenate.easeim.section.dialog_ktx.SimpleDialogFragment
import com.hyphenate.easeim.section.ui.contact.vm.AddContactVm
import com.hyphenate.easeim.section.ui.contact.vm.ContactBlackVm
import com.hyphenate.easeim.section.ui.contact.vm.ContactDetailVm
import com.hyphenate.easeui.constants.EaseConstant
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.easeui.widget.EaseTitleBar.OnBackPressListener

/**
 * 联系人详情
 */
class ContactDetailActivity(override val layoutId: Int = R.layout.activity_friends_contact_detail) :
    BaseInitActivityKtx<ActivityFriendsContactDetailBinding>(),
    OnBackPressListener, View.OnClickListener {
    private var mUser: EaseUser? = null
    private var mIsFriend = false
    private var mIsBlack = false
    private val viewModel by viewModels<ContactDetailVm>()
    private val addContactViewModel by viewModels<AddContactVm>()
    private val blackViewModel by viewModels<ContactBlackVm>()
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        return mIsFriend && !mIsBlack
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.demo_friends_contact_detail_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        mUser ?: return true
        when (item.itemId) {
            R.id.action_detail_delete ->
                showDeleteDialog(user = mUser)
            R.id.action_add_black ->
                viewModel.addUserToBlackList(username = mUser!!.username, both = false)
        }
        return super.onOptionsItemSelected(item)
    }


    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mUser = intent.getSerializableExtra("user") as EaseUser?
        mIsFriend = intent.getBooleanExtra("isFriend", true)
        if (!mIsFriend) {
            val users = DbHelper.dbHelper().userDao?.loadContactUsers()
            mIsFriend = users?.contains(mUser?.username) == true
        }
        if (mIsFriend) {
            binding.groupFriend.visibility = View.VISIBLE
            binding.btnAddContact.visibility = View.GONE
            val user = SpDbModel.instance.contactList[mUser!!.username]
            if (user != null && user.contact == 1) {
                mIsBlack = true
                //如果在黑名单中
                binding.groupFriend.visibility = View.GONE
                binding.removeBlack.visibility = View.VISIBLE
                invalidateOptionsMenu()
            }
        } else {
            binding.groupFriend.visibility = View.GONE
            binding.btnAddContact.visibility = View.VISIBLE
        }
        binding.entity = mUser
    }

    override fun initListener() {
        super.initListener()
        binding.titleBarContactDetail.setOnBackPressListener(this)
        binding.beginChat.setOnClickListener(this)
        binding.beginVoice.setOnClickListener(this)
        binding.beginVideo.setOnClickListener(this)
        binding.btnAddContact.setOnClickListener(this)
        binding.removeBlack.setOnClickListener(this)
    }

    override fun initData() {
        super.initData()
        viewModel.black.observe(this) { response ->
            if (response) {
                LiveDataBus.get().use(DemoConstant.CONTACT_CHANGE).postValue(
                    EaseEvent.create(
                        DemoConstant.CONTACT_CHANGE,
                        EaseEvent.TYPE.CONTACT
                    )
                )
                finish()
            }
        }
        viewModel.delete.observe(this) { response ->
            if (response) {
                LiveDataBus.get().use(DemoConstant.CONTACT_CHANGE).postValue(
                    EaseEvent.create(
                        DemoConstant.CONTACT_CHANGE,
                        EaseEvent.TYPE.CONTACT
                    )
                )
                finish()
            }
        }
        viewModel.userInfo.observe(this) {
            binding.entity = it
            mUser = it
            sendEvent()
        }
        addContactViewModel.addContact.observe(this) {
            if (it) {
                toast(R.string.em_add_contact_send_successful)
                binding.btnAddContact.isEnabled = false
            }
        }
        blackViewModel.result.observe(this) {
            if (it) {
                LiveDataBus.get().use(DemoConstant.CONTACT_CHANGE).postValue(
                    EaseEvent.create(
                        DemoConstant.CONTACT_CHANGE,
                        EaseEvent.TYPE.CONTACT
                    )
                )
                finish()
            }
        }
        viewModel.getUserInfoById(userName = mUser!!.username, mIsFriend = mIsFriend)
    }

    private fun sendEvent() {
        //更新本地联系人列表
        SdkHelper.instance.updateContactList()
        val event = EaseEvent.create(DemoConstant.CONTACT_UPDATE, EaseEvent.TYPE.CONTACT)
        event.message = mUser!!.username
        //发送联系人更新事件
        LiveDataBus.get().use(DemoConstant.CONTACT_UPDATE).postValue(event)
    }

    private fun showDeleteDialog(user: EaseUser?) {
        user ?: return
        SimpleDialogFragment.Builder(this)
            .setTitle(R.string.ease_friends_delete_contact_hint)
            .setOnConfirmClickListener {
                viewModel.deleteContact(username = user.username)
            }
            .showCancelButton(true)
            .show()
    }

    override fun onBackPress(view: View) {
        onBackPressed()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.beginChat ->
                ChatActivity.actionStart(
                    context = this,
                    conversationId = mUser!!.username,
                    chatType = EaseConstant.CHATTYPE_SINGLE
                )
            R.id.beginVoice ->
                EaseCallKit.getInstance().startSingleCall(
                    EaseCallType.SINGLE_VOICE_CALL,
                    mUser?.username,
                    null,
                    VideoCallActivity::class.java
                )
            R.id.beginVideo ->
                EaseCallKit.getInstance().startSingleCall(
                    EaseCallType.SINGLE_VIDEO_CALL,
                    mUser?.username,
                    null,
                    VideoCallActivity::class.java
                )
            R.id.btn_add_contact ->
                addContactViewModel.addContact(
                    username = mUser?.username ?: "",
                    reason = resources.getString(R.string.em_add_contact_add_a_friend)
                )
            R.id.removeBlack -> removeBlack()
        }
    }

    private fun removeBlack() {
        SimpleDialogFragment.Builder(this)
            .setTitle(R.string.em_friends_move_out_the_blacklist_hint)
            .setOnConfirmClickListener {
                blackViewModel.removeUserFromBlackList(
                    mUser?.username
                )
            }
            .showCancelButton(true)
            .show()
    }

    companion object {
        fun actionStart(context: Context, user: EaseUser?) {
            val intent = Intent(context, ContactDetailActivity::class.java)
            intent.putExtra("user", user)
            intent.putExtra("isFriend", user?.contact == 0)
            context.startActivity(intent)
        }

        fun actionStart(context: Context, user: EaseUser?, isFriend: Boolean) {
            val intent = Intent(context, ContactDetailActivity::class.java)
            intent.putExtra("user", user)
            intent.putExtra("isFriend", isFriend)
            context.startActivity(intent)
        }


    }
}