package com.hyphenate.easeim.common.widget

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.hyphenate.easeim.R
import com.hyphenate.easeim.databinding.LayoutItemSwitchBinding

class SwitchItemView : ConstraintLayout {
    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var title: String? = null
    private val content: String? = null
    private var titleColor = 0
    private val contentColor = 0
    private var titleSize = 0f
    private val contentSize = 0f
    private var hint: String? = null
    lateinit var switch: SwitchCompat
    private var listener: OnCheckedChangeListener? = null

     var bind: LayoutItemSwitchBinding? = null

    fun init(context: Context, attrs: AttributeSet?) {
        bind = LayoutItemSwitchBinding.inflate(LayoutInflater.from(context),this,true)
        bind?.let { bind ->
            switch = bind.switchItem
            val a = context.obtainStyledAttributes(attrs, R.styleable.SwitchItemView)
            val titleResourceId = a.getResourceId(R.styleable.SwitchItemView_switchItemTitle, -1)
            title = a.getString(R.styleable.SwitchItemView_switchItemTitle)
            if (titleResourceId != -1) {
                title = getContext().getString(titleResourceId)
            }
            bind.tvTitle.text = title
            val titleColorId = a.getResourceId(R.styleable.SwitchItemView_switchItemTitleColor, -1)
            titleColor = a.getColor(
                R.styleable.SwitchItemView_switchItemTitleColor,
                ContextCompat.getColor(getContext(), R.color.em_color_common_text_black)
            )
            if (titleColorId != -1) {
                titleColor = ContextCompat.getColor(getContext(), titleColorId)
            }
            bind.tvTitle.setTextColor(titleColor)
            val titleSizeId = a.getResourceId(R.styleable.SwitchItemView_switchItemTitleSize, -1)
            titleSize =
                a.getDimension(
                    R.styleable.SwitchItemView_switchItemTitleSize,
                    sp2px(getContext(), 14f)
                )
            if (titleSizeId != -1) {
                titleSize = resources.getDimension(titleSizeId)
            }
            bind.tvTitle.paint.textSize = titleSize
            val showDivider = a.getBoolean(R.styleable.SwitchItemView_switchItemShowDivider, true)
            bind.viewDivider.visibility = if (showDivider) VISIBLE else GONE
            val hintResourceId = a.getResourceId(R.styleable.SwitchItemView_switchItemHint, -1)
            hint = a.getString(R.styleable.SwitchItemView_switchItemHint)
            if (hintResourceId != -1) {
                hint = getContext().getString(hintResourceId)
            }
            bind.tvHint.text = hint
            val checkEnable = a.getBoolean(R.styleable.SwitchItemView_switchItemCheckEnable, true)
            bind.switchItem.isEnabled = checkEnable
            val clickable = a.getBoolean(R.styleable.SwitchItemView_switchItemClickable, true)
            bind.switchItem.isClickable = clickable
            a.recycle()
            setListener()
            bind.tvHint.visibility = if (TextUtils.isEmpty(hint)) GONE else VISIBLE
        }

    }

    private fun setListener() {
        switch?.setOnCheckedChangeListener { buttonView, isChecked ->
            listener?.onCheckedChanged(this@SwitchItemView, isChecked)
        }
    }

    fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        this.listener = listener
    }

    /**
     * Interface definition for a callback to be invoked when the checked state
     * of a compound button changed.
     */
    interface OnCheckedChangeListener {
        /**
         * Called when the checked state of a compound button has changed.
         *
         * @param buttonView The compound button view whose state has changed.
         * @param isChecked  The new checked state of buttonView.
         */
        fun onCheckedChanged(buttonView: SwitchItemView, isChecked: Boolean)
    }

    companion object {
        /**
         * sp to px
         * @param context
         * @param value
         * @return
         */
        fun sp2px(context: Context, value: Float): Float {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                value,
                context.resources.displayMetrics
            )
        }
    }
}