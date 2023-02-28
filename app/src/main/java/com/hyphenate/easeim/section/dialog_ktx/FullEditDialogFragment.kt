package com.hyphenate.easeim.section.dialog_ktx

import android.os.Bundle
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import com.hyphenate.easeim.R
import com.hyphenate.easeim.databinding.FragmentGroupEditBinding
import com.hyphenate.easeim.section.base_ktx.BaseActivityKtx
import com.hyphenate.easeim.section.base_ktx.BaseDialogKtxFragment
import com.hyphenate.easeui.utils.StatusBarCompat
import com.hyphenate.easeui.widget.EaseTitleBar.OnBackPressListener
import com.hyphenate.easeui.widget.EaseTitleBar.OnRightClickListener

class FullEditDialogFragment : BaseDialogKtxFragment<FragmentGroupEditBinding>(),
    OnRightClickListener, OnBackPressListener {
    private var content: String? = null
    private var hint: String? = null
    private var listener: OnSaveClickListener? = null
    private var title: String? = null
    private var titleSize = 0f
    private var titleColor = 0
    private var titleRight: String? = null
    private var titleRightColor = 0
    private var enableEdit //是否可以进行编辑
            = false
    override val layoutId: Int
        get() = R.layout.fragment_group_edit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.AppTheme)
        StatusBarCompat.setLightStatusBar(requireActivity(), true)
    }

    override fun onStart() {
        super.onStart()
        setDialogFullParams()
    }

    override fun initArgument() {
        super.initArgument()
        val bundle = arguments
        if (bundle != null) {
            title = bundle.getString("title")
            content = bundle.getString("content")
            hint = bundle.getString("hint")
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        if (content.isNullOrEmpty()) {
            bind.etContent.hint = hint
        } else {
            bind.etContent.setText(content)
        }
        if (!title.isNullOrEmpty()) {
            bind.titleBar.setTitle(title)
        }
        if (titleColor != 0) {
            bind.titleBar.title.setTextColor(titleColor)
        }
        if (titleSize != 0f) {
            bind.titleBar.title.textSize = titleSize
        }
        if (!titleRight.isNullOrEmpty()) {
            bind.titleBar.rightText.text = titleRight
        }
        if (titleRightColor != 0) {
            bind.titleBar.rightText.setTextColor(titleRightColor)
        }
        if (!enableEdit) {
            bind.titleBar.setRightLayoutVisibility(View.GONE)
            bind.etContent.isEnabled = false
        }
    }

    override fun initListener() {
        super.initListener()
        bind.titleBar.setOnBackPressListener(this)
        bind.titleBar.setOnRightClickListener(this)
    }

    fun setOnSaveClickListener(listener: OnSaveClickListener?) {
        this.listener = listener
    }

    override fun onRightClick(view: View) {
        val content = bind.etContent.text.toString()
        listener?.onSaveClick(view, content)
        dismiss()
    }

    override fun onBackPress(view: View) {
        dismiss()
    }

    interface OnSaveClickListener {
        fun onSaveClick(view: View, content: String)
    }

    class Builder(private val context: BaseActivityKtx) {
        private var title: String? = null
        private var hint: String? = null
        private var content: String? = null
        private var titleColor = 0
        private var titleSize = 0f
        private var confirmText: String? = null
        private var listener: OnSaveClickListener? = null
        private var confirmColor = 0
        private var enableEdit = true //默认可以编辑
        private var bundle: Bundle? = null
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

        fun setOnConfirmClickListener(
            @StringRes confirm: Int,
            listener: OnSaveClickListener?
        ): Builder {
            confirmText = context.getString(confirm)
            this.listener = listener
            return this
        }

        fun setOnConfirmClickListener(confirm: String?, listener: OnSaveClickListener?): Builder {
            confirmText = confirm
            this.listener = listener
            return this
        }

        fun setOnConfirmClickListener(listener: OnSaveClickListener?): Builder {
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

        fun setHint(@StringRes hint: Int): Builder {
            this.hint = context.getString(hint)
            return this
        }

        fun setHint(hint: String?): Builder {
            this.hint = hint
            return this
        }

        fun setContent(content: String?): Builder {
            this.content = content
            return this
        }

        fun enableEdit(enableEdit: Boolean): Builder {
            this.enableEdit = enableEdit
            return this
        }

        fun setArgument(bundle: Bundle?): Builder {
            this.bundle = bundle
            return this
        }

        fun build(): FullEditDialogFragment {
            val fragment = FullEditDialogFragment()
            fragment.setTitle(title)
            fragment.setTitleColor(titleColor)
            fragment.setTitleSize(titleSize)
            fragment.setConfirmText(confirmText)
            fragment.setOnConfirmClickListener(listener)
            fragment.setConfirmColor(confirmColor)
            fragment.setHint(hint)
            fragment.setContent(content)
            fragment.setEnableEdit(enableEdit)
            fragment.arguments = bundle
            return fragment
        }

        fun show(): FullEditDialogFragment {
            val fragment = build()
            val transaction = context.supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            fragment.show(transaction, null)
            return fragment
        }
    }

    private fun setEnableEdit(enableEdit: Boolean) {
        this.enableEdit = enableEdit
    }

    private fun setContent(content: String?) {
        this.content = content
    }

    private fun setTitleSize(titleSize: Float) {
        this.titleSize = titleSize
    }

    private fun setConfirmText(confirmText: String?) {
        titleRight = confirmText
    }

    private fun setOnConfirmClickListener(listener: OnSaveClickListener?) {
        this.listener = listener
    }

    private fun setConfirmColor(confirmColor: Int) {
        titleRightColor = confirmColor
    }

    private fun setHint(hint: String?) {
        this.hint = hint
    }

    private fun setTitleColor(titleColor: Int) {
        this.titleColor = titleColor
    }

    private fun setTitle(title: String?) {
        this.title = title
    }

    companion object {
        fun showDialog(
            activity: BaseActivityKtx,
            title: String?,
            content: String?,
            hint: String?,
            listener: OnSaveClickListener?
        ) {
            val fragment = FullEditDialogFragment()
            fragment.setOnSaveClickListener(listener)
            val bundle = Bundle()
            bundle.putString("title", title)
            bundle.putString("content", content)
            bundle.putString("hint", hint)
            fragment.arguments = bundle
            val transaction = activity.supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            fragment.show(transaction, null)
        }
    }
}