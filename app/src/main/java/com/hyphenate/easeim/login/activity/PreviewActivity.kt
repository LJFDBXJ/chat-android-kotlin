package com.hyphenate.easeim.login.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import com.hyphenate.easeim.MainActivity
import com.hyphenate.easeim.login.viewmodels.SplashVm
import com.hyphenate.easeim.R
import androidx.activity.viewModels
import com.hyphenate.easeim.common.interfaceOrImplement.OnResourceParseCallback
import com.hyphenate.easeim.common.net.Resource
import com.hyphenate.easeim.databinding.ActivityPreviewBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.util.EMLog

/**
 * @author LXJDBXJ
 * @date 闪屏 界面 展示公司logo  无用
 */
class PreviewActivity(override val layoutId: Int = R.layout.activity_preview) :
    BaseInitActivityKtx<ActivityPreviewBinding>() {
    private val model by viewModels<SplashVm>()

    override fun initSystemFit() {
        setFitSystemForTheme(false, R.color.transparent)
    }

    override fun initData() {
        super.initData()
        binding.ivSplash.animate()
            .alpha(1f)
            .setDuration(500)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    model.login()
                }
            }).start()

        binding.ivProduct.animate()
            .alpha(1f)
            .setDuration(500)
            .start()

        binding.ivProduct.setOnClickListener {
            model.login()
        }
    }

    override fun initListener() {
        super.initListener()
        model.loginData.observe(this) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>(true) {
                override fun onSuccess(data: Boolean?) {
                    EMLog.i("TAG", "onSuccess = $data")
                    MainActivity.startAction(this@PreviewActivity)
                    finish()
                }

                override fun onError(code: Int, message: String) {
                    super.onError(code, message)
                    EMLog.i("TAG", "error message = " + response.message)
                    LoginActivity.startAction(this@PreviewActivity)
                    finish()
                }
            })
        }
    }
}