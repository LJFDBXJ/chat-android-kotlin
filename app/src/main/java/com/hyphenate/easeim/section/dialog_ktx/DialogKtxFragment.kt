package com.hyphenate.easeim.section.dialog_ktx

import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import com.hyphenate.easeim.R
import com.hyphenate.easeim.databinding.FragmentDialogBaseBinding
import com.hyphenate.easeim.section.base_ktx.BaseActivityKtx
import com.hyphenate.easeim.section.base_ktx.BaseDialogKtxFragment
import com.hyphenate.easeui.utils.EaseCommonUtils

/**
 * @author LJFDBXJ
 * @date 2022/9/16
 */
typealias  OnConfirmClickListener = Function1<View?, Unit>

open class DialogKtxFragment(override val layoutId: Int = R.layout.fragment_dialog_base) :
    BaseDialogKtxFragment<FragmentDialogBaseBinding>(), View.OnClickListener {
    var mOnConfirmClickListener: OnConfirmClickListener? = null
    var mOnCancelClickListener: OnConfirmClickListener? = null
    private var confirm: String? = null
    var mTitle: String? = null
    var mContent: String? = null
    private var showCancel = false
    private var cancel: String? = null
    private var titleSize = 0f
    private var titleColor = 0
    private var confirmColor = 0
    override fun setChildView() {
        super.setChildView()
        if (middleLayoutId > 0) {
            val view =
                LayoutInflater.from(mContext).inflate(middleLayoutId, bind.rlDialogMiddle, true)
//           DataBindingUtil.inflate(
//                LayoutInflater.from(mContext),
//                layoutId,
//                bind.rlDialogMiddle,
//                false
//            )
            //同时使middleParent可见
            bind.groupMiddle.visibility = View.VISIBLE
            initMiddleView(view)
        }
    }

    open fun initMiddleView(view: View) {}
    override fun onStart() {
        super.onStart()
        //宽度填满，高度自适应
        try {
            val dialogWindow = dialog!!.window
            val lp = dialogWindow!!.attributes
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialogWindow.attributes = lp
            val view = view
            if (view != null) {
                val params = view.layoutParams
                if (params is FrameLayout.LayoutParams) {
                    val margin = EaseCommonUtils.dip2px(mContext, 30f).toInt()
                    params.setMargins(margin, 0, margin, 0)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showAllowingStateLoss(transaction: FragmentTransaction, tag: String?): Int {
        try {
            val dismissed = DialogKtxFragment::class.java.getDeclaredField("mDismissed")
            dismissed.isAccessible = true
            dismissed[this] = false
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        try {
            val shown = DialogKtxFragment::class.java.getDeclaredField("mShownByMe")
            shown.isAccessible = true
            shown[this] = true
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        transaction.add(this, tag)
        try {
            val viewDestroyed = DialogKtxFragment::class.java.getDeclaredField("mViewDestroyed")
            viewDestroyed.isAccessible = true
            viewDestroyed[this] = false
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        val mBackStackId = transaction.commitAllowingStateLoss()
        try {
            val backStackId = DialogKtxFragment::class.java.getDeclaredField("mBackStackId")
            backStackId.isAccessible = true
            backStackId[this] = mBackStackId
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        return mBackStackId
    }

    /**
     * 获取中间布局的id
     * @return
     */
    open val middleLayoutId: Int
        get() = 0

    override fun initView(savedInstanceState: Bundle?) {
        if (!TextUtils.isEmpty(mTitle)) {
            bind.tvDialogTitle.text = mTitle
        }
        if (titleColor != 0) {
            bind.tvDialogTitle.setTextColor(titleColor)
        }
        if (titleSize != 0f) {
            bind.tvDialogTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, titleSize)
        }
        if (!TextUtils.isEmpty(confirm)) {
            bind.btnDialogConfirm.text = confirm
        }
        if (confirmColor != 0) {
            bind.btnDialogConfirm.setTextColor(confirmColor)
        }
        if (!TextUtils.isEmpty(cancel)) {
            bind.btnDialogCancel.text = cancel
        }
        if (showCancel) {
            bind.groupMiddle.visibility = View.VISIBLE
        }
    }

    override fun initListener() {
        bind.btnDialogCancel.setOnClickListener(this)
        bind.btnDialogConfirm.setOnClickListener(this)
    }

    override fun initData() {}
    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_dialog_cancel -> onCancelClick(v)
            R.id.btn_dialog_confirm -> onConfirmClick(v)
        }
    }

    /**
     * 设置确定按钮的点击事件
     * @param listener
     */
    fun setOnConfirmClickListener(listener: OnConfirmClickListener?) {
        mOnConfirmClickListener = listener
    }

    /**
     * 设置取消事件
     * @param cancelClickListener
     */
    fun setOnCancelClickListener(cancelClickListener: OnConfirmClickListener?) {
        mOnCancelClickListener = cancelClickListener
    }

    /**
     * 点击了取消按钮
     * @param v
     */
    fun onCancelClick(v: View?) {
        dismiss()
        mOnCancelClickListener?.invoke(v)
    }

    /**
     * 点击了确认按钮
     * @param v
     */
    open fun onConfirmClick(v: View?) {
        dismiss()
        mOnConfirmClickListener?.invoke(v)
    }


    open class Builder(var context: BaseActivityKtx) {
        private var title: String? = null
        private var titleColor = 0
        private var titleSize = 0f
        private var showCancel = false
        private var confirmText: String? = null
        private var listener: OnConfirmClickListener? = null
        private var cancelClickListener: OnConfirmClickListener? = null
        private var confirmColor = 0
        private var bundle: Bundle? = null
        private var cancel: String? = null
        private var content: String? = null
        open fun setTitle(@StringRes title: Int): Builder {
            this.title = context.getString(title)
            return this
        }

        open fun setTitle(title: String?): Builder {
            this.title = title
            return this
        }

        open fun setTitleColor(@ColorRes color: Int): Builder {
            titleColor = ContextCompat.getColor(context, color)
            return this
        }

        open fun setTitleColorInt(@ColorInt color: Int): Builder {
            titleColor = color
            return this
        }

        open fun setTitleSize(size: Float): Builder {
            titleSize = size
            return this
        }

        open fun setContent(@StringRes content: Int): Builder {
            this.content = context.getString(content)
            return this
        }

        open fun setContent(content: String?): Builder {
            this.content = content
            return this
        }

        open fun showCancelButton(showCancel: Boolean): Builder {
            this.showCancel = showCancel
            return this
        }

        open fun setOnConfirmClickListener(
            @StringRes confirm: Int,
            listener: OnConfirmClickListener?
        ): Builder {
            confirmText = context.getString(confirm)
            this.listener = listener
            return this
        }

        open fun setOnConfirmClickListener(
            confirm: String?,
            listener: OnConfirmClickListener?
        ): Builder {
            confirmText = confirm
            this.listener = listener
            return this
        }

        open fun setOnConfirmClickListener(listener: OnConfirmClickListener?): Builder {
            this.listener = listener
            return this
        }

        open fun setConfirmColor(@ColorRes color: Int): Builder {
            confirmColor = ContextCompat.getColor(context, color)
            return this
        }

        open fun setConfirmColorInt(@ColorInt color: Int): Builder {
            confirmColor = color
            return this
        }

        open fun setOnCancelClickListener(
            @StringRes cancel: Int,
            listener: OnConfirmClickListener?
        ): Builder {
            this.cancel = context.getString(cancel)
            cancelClickListener = listener
            return this
        }

        open fun setOnCancelClickListener(
            cancel: String?,
            listener: OnConfirmClickListener?
        ): Builder {
            this.cancel = cancel
            cancelClickListener = listener
            return this
        }

        open fun setOnCancelClickListener(listener: OnConfirmClickListener?): Builder {
            cancelClickListener = listener
            return this
        }

        open fun setArgument(bundle: Bundle?): Builder {
            this.bundle = bundle
            return this
        }

        open fun build(): DialogKtxFragment {
            if (currentFragment == null)
                currentFragment = DialogKtxFragment()
            currentFragment?.let {
                it.setTitle(title)
                it.setTitleColor(titleColor)
                it.setTitleSize(titleSize)
                it.setContent(content)
                it.showCancelButton(showCancel)
                it.setConfirmText(confirmText)
                it.setOnConfirmClickListener(listener)
                it.setConfirmColor(confirmColor)
                it.setCancelText(cancel)
                it.setOnCancelClickListener(cancelClickListener)
                it.arguments = bundle
            }
            return currentFragment!!
        }

        open var currentFragment: DialogKtxFragment?=null

        open fun  iniFragment():Builder{
            return this
        }


        open fun show(): DialogKtxFragment {
            val fragment = build()
            val transaction = context.supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            fragment.showAllowingStateLoss(transaction, null)
            return fragment
        }
    }

    fun setTitleSize(titleSize: Float) {
        this.titleSize = titleSize
    }

    fun setCancelText(cancel: String?) {
        this.cancel = cancel
    }

    fun setTitleColor(titleColor: Int) {
        this.titleColor = titleColor
    }

    fun showCancelButton(showCancel: Boolean) {
        this.showCancel = showCancel
    }

    fun setConfirmText(confirmText: String?) {
        confirm = confirmText
    }

    fun setConfirmColor(confirmColor: Int) {
        this.confirmColor = confirmColor
    }

    fun setTitle(title: String?) {
        this.mTitle = title
    }

    fun setContent(content: String?) {
        this.mContent = content
    }
}