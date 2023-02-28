package com.hyphenate.easeim.section.dialog_ktx

import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.hyphenate.easeim.R
import com.hyphenate.easeim.section.base_ktx.BaseActivityKtx
import com.hyphenate.easeim.section.dialog_ktx.DialogKtxFragment

typealias ConfirmClickListener = Function2<View?, String?, Unit>

class EditTextDialogFragment : DialogKtxFragment() {
    private var contentColor = 0
    private var contentSize = 0f
    private var inputType = -1
    private var listener: ConfirmClickListener? = null
    private var contentHint: String? = null
    override fun initArgument() {
        super.initArgument()
        val bundle = arguments
        if (bundle != null) {
            setTitle(bundle.getString("title"))
            setContent(content = bundle.getString("content"))
            inputType = bundle.getInt("inputType", 0)
        }
    }

    override val middleLayoutId: Int
        get() = R.layout.fragment_dialog_edit

    private var resultMuddle: String = ""
    override fun initMiddleView(view: View) {
        super.initMiddleView(view)
        if (!mTitle.isNullOrEmpty()) {
            super.bind.tvDialogTitle.text = mTitle
        }
        val etInput = view.findViewById<AppCompatEditText>(R.id.et_input)
        etInput.doAfterTextChanged {
            resultMuddle=it.toString()
        }
        if (!TextUtils.isEmpty(contentHint)) {
            etInput.hint = contentHint
        }
        if (!mContent.isNullOrEmpty()) {
            etInput.setText(mContent)
        }
        if (contentColor != 0) {
            etInput.setTextColor(contentColor)
        }
        if (contentSize != 0f) {
            etInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, contentSize)
        }
        if (inputType != -1) {
            etInput.inputType = inputType
        }
    }

    override fun onConfirmClick(v: View?) {
        dismiss()
        val content = resultMuddle.trim { it <= ' ' }
        listener?.invoke(v, content)
    }

    fun setOnConfirmClickListener(listener: ConfirmClickListener?) {
        this.listener = listener
    }


    class Builder(context: BaseActivityKtx) : DialogKtxFragment.Builder(context) {
        private var content: String? = null
        private var contentColor = 0
        private var contentSize = 0f
        private var inputType = -1
        private var hint: String? = null
        private var listener: ConfirmClickListener? = null
        override fun setContent(@StringRes title: Int): Builder {
            this.content = context.getString(title)
            return this
        }

        override fun setContent(content: String?): Builder {
            this.content = content
            return this
        }

        fun setContentColor(@ColorRes color: Int): Builder {
            contentColor = ContextCompat.getColor(context, color)
            return this
        }

        fun setContentColorInt(@ColorInt color: Int): Builder {
            contentColor = color
            return this
        }

        fun setContentSize(size: Float): Builder {
            contentSize = size
            return this
        }

        fun setContentInputType(inputType: Int): Builder {
            this.inputType = inputType
            return this
        }

        fun setContentHint(@StringRes hint: Int): Builder {
            this.hint = context.getString(hint)
            return this
        }

        fun setContentHint(hint: String?): Builder {
            this.hint = hint
            return this
        }

        fun setConfirmClickListener(listener: ConfirmClickListener?): Builder {
            this.listener = listener
            return this
        }

        override fun iniFragment(): Builder {
            val editFragment = EditTextDialogFragment()
            currentFragment = editFragment
            return this
        }

        override fun build(): DialogKtxFragment {
            super.build()
            val editFragment = super.build() as EditTextDialogFragment
            editFragment.setContent(content)
            editFragment.setContentColor(contentColor)
            editFragment.setContentSize(contentSize)
            editFragment.setInputType(inputType)
            setContentHint(hint)
            editFragment.setOnConfirmClickListener(listener)
            return editFragment
        }
    }


    private fun setContentColor(contentColor: Int) {
        this.contentColor = contentColor
    }

    private fun setContentSize(contentSize: Float) {
        this.contentSize = contentSize
    }

    private fun setInputType(inputType: Int) {
        this.inputType = inputType
    }

    private fun setContentHint(hint: String?) {
        contentHint = hint
    }
}