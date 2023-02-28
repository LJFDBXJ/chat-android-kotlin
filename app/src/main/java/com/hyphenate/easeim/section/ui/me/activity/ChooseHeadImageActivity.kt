package com.hyphenate.easeim.section.ui.me.activity

import android.os.Bundle
import android.view.View
import com.hyphenate.EMValueCallBack
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMUserInfo.EMUserInfoType
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.ktx.toast
import com.hyphenate.easeim.common.livedatas.LiveDataBus.Companion.get
import com.hyphenate.easeim.common.utils.PreferenceManager
import com.hyphenate.easeim.databinding.ActivityChooseHeadimageBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeim.section.ui.me.headImage.HeadImageAdapter
import com.hyphenate.easeim.section.ui.me.headImage.HeadImageInfo
import com.hyphenate.easeui.model.EaseEvent

/**
 * author LXJDBXK
 * date: 2022/9/17
 */
class ChooseHeadImageActivity(override val layoutId: Int = R.layout.activity_choose_headimage) :
    BaseInitActivityKtx<ActivityChooseHeadimageBinding>(),
    View.OnClickListener {
    private var imageUrl: String? = null
    private val adapter by lazy { HeadImageAdapter() }
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        imageUrl = intent.getStringExtra("headUrl")
        binding.btnHeadImageSave.setOnClickListener(this)
        binding.headImageListView.adapter = adapter
        val data = arrayListOf(
            HeadImageInfo(
                "https://lmg.jj20.com/up/allimg/1112/0F919110I8/1ZF9110I8-1-1200.jpg",
                "阳光"
            ),
            HeadImageInfo(
                "https://lmg.jj20.com/up/allimg/1112/0F919110I8/1ZF9110I8-4-1200.jpg",
                "开心"
            ),
            HeadImageInfo(
                "https://c-ssl.dtstatic.com/uploads/blog/202207/31/20220731114255_d73a7.thumb.400_0.jpg",
                "伤感"
            ),
            HeadImageInfo(
                "https://c-ssl.dtstatic.com/uploads/blog/202207/31/20220731114233_cd93e.thumb.400_0.jpg",
                "成功"
            ),
            HeadImageInfo(
                "https://c-ssl.dtstatic.com/uploads/blog/202207/31/20220731114131_9280e.thumb.400_0.jpg",
                "你大大"
            ),
            HeadImageInfo(
                "https://c-ssl.dtstatic.com/uploads/blog/202207/31/20220731114040_3873f.thumb.400_0.jpg_webp",
                "小萝莉"
            ),
        )
        adapter.setList(data)
    }

    override fun initListener() {
        super.initListener()

        binding.titleBar.setOnBackPressListener { onBackPressed() }
    }

    override fun onClick(v: View) {
        if (v.id == R.id.btn_headImage_save) {
            if (adapter.selectHeadUrl.isNotEmpty()) {
                EMClient.getInstance().userInfoManager().updateOwnInfoByAttribute(
                    EMUserInfoType.AVATAR_URL,
                    adapter.selectHeadUrl,
                    object : EMValueCallBack<String> {
                        override fun onSuccess(value: String) {
                            toast(R.string.demo_head_image_update_success)
                            PreferenceManager.getInstance().currentUserAvatar =
                                adapter.selectHeadUrl
                            SdkHelper.instance.userProfileManager
                                .updateUserAvatar(adapter.selectHeadUrl)
                            val event =
                                EaseEvent.create(DemoConstant.AVATAR_CHANGE, EaseEvent.TYPE.CONTACT)
                            //发送联系人更新事件
                            event.message = adapter.selectHeadUrl
                            get().use(DemoConstant.AVATAR_CHANGE).postValue(event)
                            intent.putExtra("headImage", adapter.selectHeadUrl)
                            setResult(RESULT_OK, intent)
                            finish()
                        }

                        override fun onError(error: Int, errorMsg: String) {
                            toast(R.string.demo_head_image_update_failed)
                        }
                    })
            }
        }
    }
}