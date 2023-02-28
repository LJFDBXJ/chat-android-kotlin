package com.hyphenate.easeim.section.dialog_ktx

import com.hyphenate.easeim.section.base_ktx.BaseDialogKtxFragment
import com.hyphenate.easeim.R
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import com.hyphenate.easeui.utils.EaseCommonUtils
import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import androidx.annotation.StringRes
import androidx.annotation.ColorRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import com.hyphenate.easeim.databinding.FragmentDialogBaseBinding
import com.hyphenate.easeim.section.base_ktx.BaseActivityKtx
import java.lang.Exception

class DialogFragment(override val layoutId: Int = R.layout.fragment_dialog_base) :
    BaseDialogKtxFragment<FragmentDialogBaseBinding>(),
    View.OnClickListener {
    var mOnConfirmClickListener: OnConfirmClickListener? = null
    var mOnCancelClickListener: onCancelClickListener? = null
    var mTitle: String? = null
    private var confirmColor = 0
    private var confirm: String? = null
    private var showCancel = false
    private var titleColor = 0
    private var cancel: String? = null
    private var titleSize = 0f
    var mContent: String? = null


    override fun setChildView() {
        super.setChildView()
        val layoutId = middleLayoutId
        if (layoutId > 0) {
            LayoutInflater.from(requireContext()).inflate(layoutId, bind.rlDialogMiddle)
            //同时使middleParent可见
            bind.groupMiddle.visibility = View.VISIBLE
        }
    }

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
            val dismissed = DialogFragment::class.java.getDeclaredField("mDismissed")
            dismissed.isAccessible = true
            dismissed[this] = false
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        try {
            val shown = DialogFragment::class.java.getDeclaredField("mShownByMe")
            shown.isAccessible = true
            shown[this] = true
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        transaction.add(this, tag)
        try {
            val viewDestroyed = DialogFragment::class.java.getDeclaredField("mViewDestroyed")
            viewDestroyed.isAccessible = true
            viewDestroyed[this] = false
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        val mBackStackId = transaction.commitAllowingStateLoss()
        try {
            val backStackId = DialogFragment::class.java.getDeclaredField("mBackStackId")
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
    val middleLayoutId: Int
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
    fun setOnCancelClickListener(cancelClickListener: onCancelClickListener?) {
        mOnCancelClickListener = cancelClickListener
    }

    /**
     * 点击了取消按钮
     * @param v
     */
    fun onCancelClick(v: View?) {
        dismiss()
        if (mOnCancelClickListener != null) {
            mOnCancelClickListener!!.onCancelClick(v)
        }
    }

    /**
     * 点击了确认按钮
     * @param v
     */
    fun onConfirmClick(v: View?) {
        dismiss()
        if (mOnConfirmClickListener != null) {
            mOnConfirmClickListener!!.onConfirmClick(v)
        }
    }

    /**
     * 确定事件的点击事件
     */
    interface OnConfirmClickListener {
        fun onConfirmClick(view: View?)
    }

    /**
     * 点击取消
     */
    interface onCancelClickListener {
        fun onCancelClick(view: View?)
    }

    class Builder(var context: BaseActivityKtx) {
        private var title: String? = null
        private var titleColor = 0
        private var titleSize = 0f
        private var showCancel = false
        private var confirmText: String? = null
        private var listener: OnConfirmClickListener? = null
        private var cancelClickListener: onCancelClickListener? = null
        private var confirmColor = 0
        private var bundle: Bundle? = null
        private var cancel: String? = null
        private var content: String? = null
        fun setTitle(@StringRes title: Int): Builder {
            this.title = context.getString(title)
            return this
        }

        fun setTitle(title: String?): Builder {
            this.title = title
            return this
        }

        fun setTitleColor(@ColorRes color: Int): Builder {
            titleColor = ContextCompat.getColor(context, color)
            return this
        }

        fun setTitleColorInt(@ColorInt color: Int): Builder {
            titleColor = color
            return this
        }

        fun setTitleSize(size: Float): Builder {
            titleSize = size
            return this
        }

        fun setContent(@StringRes content: Int): Builder {
            this.content = context.getString(content)
            return this
        }

        fun setContent(content: String?): Builder {
            this.content = content
            return this
        }

        fun showCancelButton(showCancel: Boolean): Builder {
            this.showCancel = showCancel
            return this
        }

        fun setOnConfirmClickListener(
            @StringRes confirm: Int,
            listener: OnConfirmClickListener?
        ): Builder {
            confirmText = context.getString(confirm)
            this.listener = listener
            return this
        }

        fun setOnConfirmClickListener(
            confirm: String?,
            listener: OnConfirmClickListener?
        ): Builder {
            confirmText = confirm
            this.listener = listener
            return this
        }

        fun setOnConfirmClickListener(listener: OnConfirmClickListener?): Builder {
            this.listener = listener
            return this
        }

        fun setConfirmColor(@ColorRes color: Int): Builder {
            confirmColor = ContextCompat.getColor(context, color)
            return this
        }

        fun setConfirmColorInt(@ColorInt color: Int): Builder {
            confirmColor = color
            return this
        }

        fun setOnCancelClickListener(
            @StringRes cancel: Int,
            listener: onCancelClickListener?
        ): Builder {
            this.cancel = context.getString(cancel)
            cancelClickListener = listener
            return this
        }

        fun setOnCancelClickListener(cancel: String?, listener: onCancelClickListener?): Builder {
            this.cancel = cancel
            cancelClickListener = listener
            return this
        }

        fun setOnCancelClickListener(listener: onCancelClickListener?): Builder {
            cancelClickListener = listener
            return this
        }

        fun setArgument(bundle: Bundle?): Builder {
            this.bundle = bundle
            return this
        }

        fun build(): DialogFragment {
            val fragment = fragment
            fragment.setTitle(title)
            fragment.setTitleColor(titleColor)
            fragment.setTitleSize(titleSize)
            fragment.setContent(content)
            fragment.showCancelButton(showCancel)
            fragment.setConfirmText(confirmText)
            fragment.setOnConfirmClickListener(listener)
            fragment.setConfirmColor(confirmColor)
            fragment.setCancelText(cancel)
            fragment.setOnCancelClickListener(cancelClickListener)
            fragment.arguments = bundle
            return fragment
        }

        val fragment: DialogFragment
            get() = DialogFragment()

        fun show(): DialogFragment {
            val fragment = build()
            val transaction = context.supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            fragment.showAllowingStateLoss(transaction, null)
            return fragment
        }
    }

    private fun setTitleSize(titleSize: Float) {
        this.titleSize = titleSize
    }

    private fun setCancelText(cancel: String?) {
        this.cancel = cancel
    }

    private fun setTitleColor(titleColor: Int) {
        this.titleColor = titleColor
    }

    private fun showCancelButton(showCancel: Boolean) {
        this.showCancel = showCancel
    }

    private fun setConfirmText(confirmText: String?) {
        confirm = confirmText
    }

    private fun setConfirmColor(confirmColor: Int) {
        this.confirmColor = confirmColor
    }

    private fun setTitle(title: String?) {
        this.mTitle = title
    }

    private fun setContent(content: String?) {
        this.mContent = content
    }
}