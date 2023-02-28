package com.hyphenate.easeim.section.base_ktx

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.hyphenate.EMCallBack
import com.hyphenate.easeim.AppClient
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.enums.Status
import com.hyphenate.easeim.common.interfaceOrImplement.OnResourceParseCallback
import com.hyphenate.easeim.common.ktx.toast
import com.hyphenate.easeim.common.livedatas.obs
import com.hyphenate.easeim.common.net.Resource
import com.hyphenate.easeim.common.widget.EaseProgressDialog
import com.hyphenate.easeim.login.activity.LoginActivity
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.easeui.utils.StatusBarCompat
import com.hyphenate.util.EMLog

/**
 * @author LXJDBXJ
 * @date 2022/9/16
 * 作为基础activity,放置一些公共的方法
 */
open class BaseActivityKtx : AppCompatActivity() {
    private var dialog: EaseProgressDialog? = null
    private var logoutDialog: AlertDialog? = null
    private var dialogCreateTime: Long = 0//dialog生成事件，用以判断dialog的展示时间
    private val handler = Handler() //用于dialog延迟消失
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        clearFragmentsBeforeCreate()
        registerAccountObservable()
    }

    /**
     * 添加账号异常监听
     */
    fun registerAccountObservable() {
        DemoConstant.ACCOUNT_CHANGE.obs<EaseEvent>(this) { event ->
            event ?: return@obs
            if (!event.isAccountChange) {
                return@obs
            }
            val accountEvent = event.event
            if (accountEvent == DemoConstant.ACCOUNT_REMOVED ||
                accountEvent == DemoConstant.ACCOUNT_KICKED_BY_CHANGE_PASSWORD ||
                accountEvent == DemoConstant.ACCOUNT_KICKED_BY_OTHER_DEVICE
            ) {
                SdkHelper.instance.logOut(false, object : EMCallBack {
                    override fun onSuccess() {
                        finishOtherActivities()
                        startActivity(Intent(this@BaseActivityKtx, LoginActivity::class.java))
                        finish()
                    }

                    override fun onError(code: Int, error: String) {
                        EMLog.e(
                            "logout",
                            "logout error: error code = $code error message = $error"
                        )
                        toast("logout error: error code = $code error message = $error")
                    }

                    override fun onProgress(progress: Int, status: String) {}
                })
            } else if (TextUtils.equals(accountEvent, DemoConstant.ACCOUNT_CONFLICT)
                || TextUtils.equals(accountEvent, DemoConstant.ACCOUNT_REMOVED)
                || TextUtils.equals(accountEvent, DemoConstant.ACCOUNT_FORBIDDEN)
            ) {
                SdkHelper.instance.logOut(false, null)
                showExceptionDialog(accountEvent)
            }
        }

    }

    private fun showExceptionDialog(accountEvent: String) {
        if (logoutDialog != null && logoutDialog!!.isShowing && !isFinishing) {
            logoutDialog!!.dismiss()
        }
        logoutDialog = AlertDialog.Builder(this)
            .setTitle(R.string.em_account_logoff_notification)
            .setMessage(getExceptionMessageId(accountEvent))
            .setPositiveButton(R.string.ok) { _, _ ->
                finishOtherActivities()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setCancelable(false)
            .create()
        logoutDialog!!.show()
    }

    private fun getExceptionMessageId(exceptionType: String): Int {
        return when (exceptionType) {
            DemoConstant.ACCOUNT_CONFLICT -> {
                R.string.em_account_connect_conflict
            }
            DemoConstant.ACCOUNT_REMOVED -> {
                R.string.em_account_user_remove
            }
            DemoConstant.ACCOUNT_FORBIDDEN -> {
                R.string.em_account_user_forbidden
            }
            else -> R.string.Network_error
        }
    }

    /**
     * 结束除了当前Activity外的其他Activity
     */
    protected fun finishOtherActivities() {
        val lifecycleCallbacks = AppClient.instance.lifecycleCallbacks
        if (lifecycleCallbacks == null) {
            finish()
            return
        }
        val activities = lifecycleCallbacks.activityList
        if (activities == null || activities.isEmpty()) {
            finish()
            return
        }
        for (activity in activities) {
            if (activity !== lifecycleCallbacks.current()) {
                activity.finish()
            }
        }
    }

    /**
     * 初始化toolbar
     * @param toolbar
     */
    fun initToolBar(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) //有返回
        supportActionBar?.setDisplayShowTitleEnabled(false) //不显示title
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onBackPressed() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (window.attributes.softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (currentFocus != null) {
                imm.hideSoftInputFromWindow(
                    currentFocus!!.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
                super.onBackPressed()
            } else {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissLoading()
    }

    /**
     * hide keyboard
     */
    fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (window.attributes.softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (currentFocus != null) {
                imm.hideSoftInputFromWindow(
                    currentFocus!!.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            }
        }
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (null != this.currentFocus) {
            /**
             * 点击空白位置 隐藏软键盘
             */
            val mInputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            return mInputMethodManager.hideSoftInputFromWindow(this.currentFocus!!.windowToken, 0)
        }
        return super.onTouchEvent(event)
    }

    /**
     * 通用页面设置
     */
    fun setFitSystemForTheme() {
        setFitSystemForTheme(true, R.color.white)
        setStatusBarTextColor(true)
    }

    /**
     * 通用页面，需要设置沉浸式
     * @param fitSystemForTheme
     */
    fun setFitSystemForTheme(fitSystemForTheme: Boolean) {
        setFitSystemForTheme(fitSystemForTheme, R.color.white)
        setStatusBarTextColor(false)
    }

    /**
     * 通用页面，需要设置沉浸式
     * @param fitSystemForTheme
     */
    fun setFitSystemForTheme2(fitSystemForTheme: Boolean) {
        setFitSystemForTheme(fitSystemForTheme, "#ffffffff")
        setStatusBarTextColor(true)
    }

    /**
     * 设置是否是沉浸式，并可设置状态栏颜色
     * @param fitSystemForTheme
     * @param colorId 颜色资源路径
     */
    fun setFitSystemForTheme(fitSystemForTheme: Boolean, @ColorRes colorId: Int) {
        setFitSystem(fitSystemForTheme)
        //初始设置
        StatusBarCompat.compat(this, ContextCompat.getColor(this, colorId))
    }

    /**
     * 修改状态栏文字颜色
     * @param isLight 是否是浅色字体
     */
    fun setStatusBarTextColor(isLight: Boolean) {
        StatusBarCompat.setLightStatusBar(this, !isLight)
    }

    /**
     * 设置是否是沉浸式，并可设置状态栏颜色
     * @param fitSystemForTheme true 不是沉浸式
     * @param color 状态栏颜色
     */
    fun setFitSystemForTheme(fitSystemForTheme: Boolean, color: String?) {
        setFitSystem(fitSystemForTheme)
        //初始设置
        StatusBarCompat.compat(this, Color.parseColor(color))
    }

    /**
     * 设置是否是沉浸式
     * @param fitSystemForTheme
     */
    fun setFitSystem(fitSystemForTheme: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
        if (fitSystemForTheme) {
            val contentFrameLayout = findViewById<View>(Window.ID_ANDROID_CONTENT) as ViewGroup
            val parentView = contentFrameLayout.getChildAt(0)
            if (parentView != null && Build.VERSION.SDK_INT >= 14) {
                parentView.fitsSystemWindows = true
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        }
    }


    /**
     * 解析Resource<T>
     * @param response
     * @param callback
     * @param <T>
    </T></T> */
    fun <T> parseResource(response: Resource<T>?, callback: OnResourceParseCallback<T>) {
        if (response == null) {
            return
        }
        when (response.status) {
            Status.SUCCESS -> {
                callback.hideLoading()
                callback.onSuccess(response.data)
            }
            Status.ERROR -> {
                callback.hideLoading()
                if (!callback.hideErrorMsg) {
                    toast(response.message ?: "")
                }
                callback.onError(response.errorCode, response.message)
            }
            Status.LOADING -> {
                callback.onLoading(response.data)
            }
        }
    }

    fun isMessageChange(message: String): Boolean {
        if (TextUtils.isEmpty(message)) {
            return false
        }
        return message.contains("message")
    }

    fun isContactChange(message: String): Boolean {
        if (TextUtils.isEmpty(message)) {
            return false
        }
        return message.contains("contact")
    }

    fun isGroupInviteChange(message: String): Boolean {
        if (TextUtils.isEmpty(message)) {
            return false
        }
        return message.contains("invite")
    }

    fun isNotify(message: String): Boolean {
        if (TextUtils.isEmpty(message)) {
            return false
        }
        return message.contains("invite")
    }

    @JvmOverloads
    fun showLoading(message: String? = getString(R.string.loading)) {
        if (dialog?.isShowing == true) {
            dialog!!.dismiss()
        }
        if (isFinishing) {
            return
        }
        dialogCreateTime = System.currentTimeMillis()
        dialog = EaseProgressDialog.Builder(this)
            .setLoadingMessage(message)
            .setCancelable(true)
            .setCanceledOnTouchOutside(true)
            .show()
    }

    fun dismissLoading() {
        if (dialog != null && dialog!!.isShowing) {
            //如果dialog的展示时间过短，则延迟1s再消失
            if (System.currentTimeMillis() - dialogCreateTime < 500 && !isFinishing) {
                handler.postDelayed({
                    if (dialog != null && dialog!!.isShowing) {
                        dialog!!.dismiss()
                        dialog = null
                    }
                }, 1000)
            } else {
                dialog!!.dismiss()
                dialog = null
            }
        }
    }

    /**
     * 处理因为Activity重建导致的fragment叠加问题
     */
    fun clearFragmentsBeforeCreate() {
        val fragments = supportFragmentManager.fragments
        if (fragments.isEmpty()) {
            return
        }
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        for (fragment in fragments) {
            fragmentTransaction.remove(fragment!!)
        }
        fragmentTransaction.commitNow()
    }

    companion object {
        /**
         * 设置返回按钮的颜色
         * @param mContext
         * @param colorId
         */
        fun setToolbarCustomColor(mContext: AppCompatActivity, colorId: Int) {
            val leftArrow = ContextCompat.getDrawable(mContext, R.drawable.abc_ic_ab_back_material)
            if (leftArrow != null) {
                leftArrow.setColorFilter(
                    ContextCompat.getColor(mContext, colorId),
                    PorterDuff.Mode.SRC_ATOP
                )
                if (mContext.supportActionBar != null) {
                    mContext.supportActionBar!!.setHomeAsUpIndicator(leftArrow)
                }
            }
        }
    }
}