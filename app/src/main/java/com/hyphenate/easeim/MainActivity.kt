package com.hyphenate.easeim

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailabilityLight
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessaging
import com.hyphenate.EMValueCallBack
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMUserInfo
import com.hyphenate.chat.EMUserInfo.EMUserInfoType
import com.hyphenate.easecallkit.base.EaseCallType
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.enums.SearchType
import com.hyphenate.easeim.common.ktx.jump
import com.hyphenate.easeim.common.ktx.toast
import com.hyphenate.easeim.common.livedatas.LiveDataBus
import com.hyphenate.easeim.common.livedatas.obs
import com.hyphenate.easeim.common.utils.PreferenceManager
import com.hyphenate.easeim.common.utils.PushUtils
import com.hyphenate.easeim.common.utils.SpDbModel
import com.hyphenate.easeim.databinding.ActivityMainBinding
import com.hyphenate.easeim.section.MainVm
import com.hyphenate.easeim.section.av.MultipleVideoActivity
import com.hyphenate.easeim.section.av.VideoCallActivity
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeim.section.base_ktx.HomePageAdapter
import com.hyphenate.easeim.section.ui.chat.ChatPresenter
import com.hyphenate.easeim.section.ui.conversation.ConversationListFragment
import com.hyphenate.easeim.section.ui.group.activity.GroupPrePickActivity
import com.hyphenate.easeim.section.ui.contact.ContactListFragment
import com.hyphenate.easeim.section.ui.contact.activity.AddContactActivity
import com.hyphenate.easeim.section.ui.contact.activity.GroupContactManageActivity
import com.hyphenate.easeim.section.ui.contact.vm.ContactsVm
import com.hyphenate.easeim.section.ui.me.AboutMeFragment
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.util.EMLog

/**
 * 聊天 host activity
 */
class MainActivity(override val layoutId: Int = R.layout.activity_main) :
    BaseInitActivityKtx<ActivityMainBinding>() {
    private val viewModel by viewModels<MainVm>()

    //加载联系人
    private val contactsViewModel by viewModels<ContactsVm>()

    private var showMenu = true //是否显示菜单项

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val search: Boolean
        val create: Boolean
        if (binding.viewPage.currentItem == 1) {
            create = false
            search = true
        } else {
            create = true
            search = false
        }
        //创建群
        menu.findItem(R.id.action_group).isVisible = create
        //添加好友
        menu.findItem(R.id.action_friend).isVisible = create
        //搜索朋友
        menu.findItem(R.id.action_search_friend).isVisible = search
        //搜索群
        menu.findItem(R.id.action_search_group).isVisible = search
        return showMenu
    }


    override fun onResume() {
        super.onResume()
        SdkHelper.instance.showNotificationPermissionDialog(this)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.demo_conversation_menu, menu)
        return showMenu
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_video -> {}
            R.id.action_group -> {
                GroupPrePickActivity.actionStart(this)
            }
            R.id.action_friend,
            R.id.action_search_friend -> {
                AddContactActivity.startAction(
                    context = this,
                    type = SearchType.CHAT
                )
            }
            R.id.action_search_group -> {
                GroupContactManageActivity.actionStart(this, true)
            }
            R.id.action_scan -> toast(R.string.em_conversation_menu_scan)
        }
        return showMenu
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        val pageData = arrayListOf(
            // 会话列表
            ConversationListFragment(),

            //联系人列表
            ContactListFragment(),

            //关于我
            AboutMeFragment()
        )
        val pageAdapter = HomePageAdapter(supportFragmentManager, lifecycle)
        binding.viewPage.adapter = pageAdapter
        binding.viewPage.offscreenPageLimit = 3
        pageAdapter.addAll(pageData)
        binding.navView.itemIconTintList = null

        //Translation Manager 初始化
        SdkHelper.instance.initTranslationManager()
    }

    override fun initListener() {
        super.initListener()
        viewModel.homeUnReadObservable.observe(this) { readCount: String? ->
            if (!readCount.isNullOrEmpty())
                binding.navView.getBadge(R.id.em_main_nav_home)?.number = readCount.toInt()
        }

        contactsViewModel.loadContactList(fetchServer = true)
        arrayOf(
            DemoConstant.GROUP_CHANGE,
            DemoConstant.NOTIFY_CHANGE,
            DemoConstant.MESSAGE_CHANGE_CHANGE,
            DemoConstant.CONVERSATION_DELETE,
            DemoConstant.CONTACT_CHANGE,
            DemoConstant.CONVERSATION_READ,
        ).obs<EaseEvent>(this) { _, result ->
            checkUnReadMsg(event = result)
        }
        binding.navView.setOnItemSelectedListener { menuItem ->
            showMenu = true
            when (menuItem.itemId) {
                R.id.em_main_nav_home -> {
                    binding.viewPage.setCurrentItem(0, false)
                    binding.titleBarMain.setTitle(resources.getString(R.string.em_main_title_home))
                }
                R.id.em_main_nav_friends -> {
                    binding.viewPage.setCurrentItem(1, false)
                    binding.titleBarMain.setTitle(resources.getString(R.string.em_main_title_friends))
                }
                R.id.em_main_nav_me -> {
                    //获取自己用户信息
                    fetchSelfInfo()
                    binding.viewPage.setCurrentItem(2, false)
                    binding.titleBarMain.setTitle(resources.getString(R.string.em_main_title_me))
                    showMenu = false
                }
            }
            invalidateOptionsMenu()
            return@setOnItemSelectedListener true
        }
    }

    override fun initData() {
        super.initData()
        viewModel.checkUnreadMsg()
        ChatPresenter.getInstance().init()
        // 获取华为 HMS 推送 token
        HMSPushHelper.get().getHMSToken(this)

        //判断是否为来电推送
        if (PushUtils.isRtcCall) {
            val dist =
                if (EaseCallType.getfrom(PushUtils.type) != EaseCallType.CONFERENCE_CALL) {
                    VideoCallActivity::class.java
                } else
                    MultipleVideoActivity::class.java
            startActivity(Intent(this, dist).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            PushUtils.isRtcCall = false
        }
        if (SpDbModel.instance.isUseFCM()
            && GoogleApiAvailabilityLight.getInstance()
                .isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS
        ) {
            // 启用 FCM 自动初始化
            if (!FirebaseMessaging.getInstance().isAutoInitEnabled) {
                FirebaseMessaging.getInstance().isAutoInitEnabled = true
                FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true)
            }
            // 获取FCM 推送 token 并上传
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    EMLog.d("FCM", "Fetching FCM registration token failed:" + task.exception)
                    return@OnCompleteListener
                }
                // Get new FCM registration token
                val token = task.result
                EMLog.d("FCM", token)
                EMClient.getInstance().sendFCMTokenToServer(token)
            })
        }
    }


    private fun checkUnReadMsg(event: EaseEvent?) {
        event ?: return
        viewModel.checkUnreadMsg()
    }


    private fun fetchSelfInfo() {
        val userId = arrayOf(EMClient.getInstance().currentUser)
        val userInfoTypes = arrayOf(EMUserInfoType.NICKNAME, EMUserInfoType.AVATAR_URL)

        EMClient.getInstance().userInfoManager().fetchUserInfoByAttribute(userId, userInfoTypes,
            object : EMValueCallBack<Map<String?, EMUserInfo?>> {
                override fun onSuccess(userInfos: Map<String?, EMUserInfo?>) {
                    runOnUiThread {
                        val userInfo =
                            userInfos[EMClient.getInstance().currentUser]
                                ?: return@runOnUiThread
                        //昵称
                        if (!userInfo.nickname.isNullOrEmpty()) {
                            val event = EaseEvent.create(
                                DemoConstant.NICK_NAME_CHANGE,
                                EaseEvent.TYPE.CONTACT
                            )
                            event.message = userInfo.nickname
                            LiveDataBus.get().use(DemoConstant.NICK_NAME_CHANGE)
                                .postValue(event)
                            PreferenceManager.getInstance().currentUserNick = userInfo.nickname
                        }
                        //头像
                        if (!userInfo.avatarUrl.isNullOrEmpty()) {
                            val event = EaseEvent.create(
                                DemoConstant.AVATAR_CHANGE,
                                EaseEvent.TYPE.CONTACT
                            )
                            event.message = userInfo.avatarUrl
                            LiveDataBus.get().use(DemoConstant.AVATAR_CHANGE).postValue(event)
                            PreferenceManager.getInstance().currentUserAvatar =
                                userInfo.avatarUrl
                        }
                    }
                }

                override fun onError(error: Int, errorMsg: String) {
                    EMLog.e(
                        "MainActivity",
                        "fetchUserInfoByIds error:$error errorMsg:$errorMsg"
                    )
                }
            })
    }

    companion object {
        fun startAction(context: Context) {
            context.jump<MainActivity>()
        }
    }
}