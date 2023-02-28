package com.hyphenate.easeim.section.dialog_ktx

import android.content.res.Resources.NotFoundException
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TimePicker
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.fragment.app.FragmentTransaction
import com.hyphenate.easeim.R
import com.hyphenate.easeim.databinding.FragmentTimePickerDialogBinding
import com.hyphenate.easeim.section.base_ktx.BaseActivityKtx
import com.hyphenate.easeim.section.base_ktx.BaseDialogKtxFragment
import com.hyphenate.easeui.utils.EaseCommonUtils
import com.hyphenate.util.DensityUtil
import java.lang.reflect.Field
import java.util.*

class TimePickerDialogFragment(override val layoutId: Int = R.layout.fragment_time_picker_dialog) :
    BaseDialogKtxFragment<FragmentTimePickerDialogBinding>() {
    private var preStartTime: String? = null
    private var preEndTime: String? = null
    private var startTime: String? = null
    private var endTime: String? = null
    private var divider: String? = null
    private var showMinute = false
    private var title: String? = null
    private var titleColor = 0
    private var titleSize = 0f
    private var showCancel = false
    private var confirmText: String? = null
    private var listener: OnTimePickSubmitListener? = null
    private var cancelClickListener: OnTimePickCancelListener? = null
    private var confirmColor = 0
    private var cancel: String? = null


    override fun onStart() {
        super.onStart()
        try {
            val dialogWindow = dialog?.window
            dialogWindow?.attributes?.let { lp ->
                lp.dimAmount = 0.6f
                lp.width = (EaseCommonUtils.getScreenInfo(requireContext())[0] - DensityUtil.dip2px(
                    requireContext(),
                    20f
                ) * 2).toInt()
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
                lp.gravity = Gravity.BOTTOM
                lp.y = DensityUtil.dip2px(requireContext(), 10f)
                setDialogParams(lp)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun initListener() {
        super.initListener()
        bind.pickerStart.setOnTimeChangedListener { _, hourOfDay, minute ->
            startTime = getTime(hourOfDay, minute)
        }
        bind.pickerEnd.setOnTimeChangedListener { _, hourOfDay, minute ->
            endTime = getTime(hourOfDay, minute)
        }
        bind.btnCancel.setOnClickListener { v ->
            cancelClickListener?.onClickCancel(v)
            dismiss()
        }
        bind.btnSubmit.setOnClickListener { v ->
            listener?.onClickSubmit(v, startTime, endTime)
            dismiss()
        }
    }

    private fun getTime(hour: Int, minute: Int): String {
        val h = getDoubleDigit(hour)
        val m = getDoubleDigit(minute)
        return if (showMinute) "$h:$m" else "$h:00"
    }

    private fun getDoubleDigit(time: Int): String {
        return if (time < 10) "0$time" else "" + time
    }

    override fun initData() {
        super.initData()
        if (!title.isNullOrEmpty()) {
            bind.tvTitle.text = title
        }
        if (titleColor != 0) {
            bind.tvTitle.setTextColor(titleColor)
        }
        if (titleSize != 0f) {
            bind.tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, titleSize)
        }
        if (!confirmText.isNullOrEmpty()) {
            bind.btnSubmit.text = confirmText
        }
        if (confirmColor != 0) {
            bind.btnSubmit.setTextColor(confirmColor)
        }
        if (!cancel.isNullOrEmpty()) {
            bind.btnCancel.text = cancel
        }
        if (showCancel) {
            bind.btnCancel.visibility = View.VISIBLE
        } else {
            bind.btnCancel.visibility = View.GONE
        }
        bind.pickerStart.setIs24HourView(true)
        bind.pickerEnd.setIs24HourView(true)
        bind.pickerStart.descendantFocusability = TimePicker.FOCUS_BLOCK_DESCENDANTS
        bind.pickerEnd.descendantFocusability = TimePicker.FOCUS_BLOCK_DESCENDANTS
        setTimePickerDividerColor(bind.pickerStart)
        setTimePickerDividerColor(bind.pickerEnd)
        hideMinute(bind.pickerStart)
        hideMinute(bind.pickerEnd)
        val calendar = Calendar.getInstance()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bind.pickerStart.hour = calendar[Calendar.HOUR_OF_DAY]
            bind.pickerStart.minute = calendar[Calendar.MINUTE]
            calendar.add(Calendar.HOUR_OF_DAY, 1)
            bind.pickerEnd.hour = calendar[Calendar.HOUR_OF_DAY]
            bind.pickerEnd.minute = calendar[Calendar.MINUTE]
        } else {
            bind.pickerStart.currentHour = calendar[Calendar.HOUR_OF_DAY]
            bind.pickerStart.currentMinute = calendar[Calendar.MINUTE]
            calendar.add(Calendar.HOUR_OF_DAY, 1)
            bind.pickerEnd.currentHour = calendar[Calendar.HOUR_OF_DAY]
            bind.pickerEnd.currentMinute = calendar[Calendar.MINUTE]
        }
        if (!TextUtils.isEmpty(preStartTime) && !TextUtils.isEmpty(preEndTime)) {
            val time = preStartTime!!
            val endTime = preEndTime!!
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                bind.pickerStart.hour = time.substring(0, time.indexOf(":")).toInt()
                bind.pickerStart.minute = time.substring(time.indexOf(":") + 1).toInt()

                bind.pickerEnd.hour = endTime.substring(0, endTime.indexOf(":")).toInt()
                bind.pickerEnd.minute = endTime.substring(endTime.indexOf(":") + 1).toInt()
            } else {
                bind.pickerStart.currentHour =
                    time.substring(0, time.indexOf(":")).toInt()
                bind.pickerStart.currentMinute =
                    time.substring(time.indexOf(":") + 1).toInt()
                bind.pickerEnd.currentHour =
                    endTime.substring(0, endTime.indexOf(":")).toInt()
                bind.pickerEnd.currentMinute =
                    endTime.substring(endTime.indexOf(":") + 1).toInt()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            startTime = getTime(bind.pickerStart.hour, bind.pickerStart.minute)
            endTime = getTime(bind.pickerEnd.hour, bind.pickerEnd.minute)
        } else {
            startTime = getTime(bind.pickerStart.currentHour, bind.pickerStart.currentMinute)
            endTime = getTime(bind.pickerEnd.currentHour, bind.pickerEnd.currentMinute)
        }
    }

    /**
     * 设置分割线的颜色
     *
     * @param timePicker
     */
    private fun setTimePickerDividerColor(timePicker: TimePicker) {
        val child = timePicker.getChildAt(0)
        if (child is LinearLayout) {
            val subChild = child.getChildAt(1)
            if (subChild is LinearLayout) {
                subChild.forEach { view ->
                    if (view is NumberPicker) {
                        val pickerFields = NumberPicker::class.java.declaredFields
                        //setPickerMargin((NumberPicker) mSpinners.getChildAt(i));
                        for (pf in pickerFields) {
                            if (pf.name == "mSelectionDivider") {
                                pf.isAccessible = true
                                try {
                                    pf[view] = ColorDrawable()
                                } catch (e: IllegalArgumentException) {
                                    e.printStackTrace()
                                } catch (e: NotFoundException) {
                                    e.printStackTrace()
                                } catch (e: IllegalAccessException) {
                                    e.printStackTrace()
                                }
                                break
                            }
                        }
                    }
                }

            }
        }
    }

    private fun hideMinute(timePicker: TimePicker) {
        val declaredFields = timePicker.javaClass.declaredFields
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (datePickerField in declaredFields) {
                if ("mDelegate" == datePickerField.name) {
                    datePickerField.isAccessible = true
                    var dayPicker: Any? = Any()
                    try {
                        dayPicker = datePickerField[timePicker]
                    } catch (e: IllegalAccessException) {
                        e.printStackTrace()
                    } catch (e: IllegalArgumentException) {
                        e.printStackTrace()
                    }
                    if (dayPicker == null) {
                        return
                    }
                    val fields = dayPicker.javaClass.declaredFields
                    for (field in fields) {
                        hideMinuteAndDivider(dayPicker, field)
                    }
                }
            }
        } else {
            for (datePickerField in declaredFields) {
                hideMinuteAndDivider(timePicker, datePickerField)
            }
        }
    }

    private fun hideMinuteAndDivider(dayPicker: Any, field: Field) {
        if ("mMinuteSpinner" == field.name) {
            field.isAccessible = true
            var minute: Any? = null
            try {
                minute = field[dayPicker]
                (minute as View?)?.visibility = View.GONE
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        }
        if ("mDivider" == field.name) {
            field.isAccessible = true
            var divider: Any? = null
            try {
                divider = field[dayPicker]
                (divider as View?)?.visibility = View.GONE
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        }
    }

    interface OnTimePickCancelListener {
        fun onClickCancel(view: View?)
    }

    interface OnTimePickSubmitListener {
        fun onClickSubmit(view: View?, start: String?, end: String?)
    }

    class Builder(var context: BaseActivityKtx) {
        private var title: String? = null
        private var titleColor = 0
        private var titleSize = 0f
        private var showCancel = false
        private var confirmText: String? = null
        private var listener: OnTimePickSubmitListener? = null
        private var cancelClickListener: OnTimePickCancelListener? = null
        private var confirmColor = 0
        private var cancel: String? = null
        private var showMinute = false
        private var divider: String? = null
        private var startTime: String? = null
        private var endTime: String? = null
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

        fun showCancelButton(showCancel: Boolean): Builder {
            this.showCancel = showCancel
            return this
        }

        fun setOnTimePickSubmitListener(
            @StringRes confirm: Int,
            listener: OnTimePickSubmitListener?
        ): Builder {
            confirmText = context.getString(confirm)
            this.listener = listener
            return this
        }

        fun setOnTimePickSubmitListener(
            confirm: String?,
            listener: OnTimePickSubmitListener?
        ): Builder {
            confirmText = confirm
            this.listener = listener
            return this
        }

        fun setOnTimePickSubmitListener(listener: OnTimePickSubmitListener?): Builder {
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

        fun setOnTimePickCancelListener(
            @StringRes cancel: Int,
            listener: OnTimePickCancelListener?
        ): Builder {
            this.cancel = context.getString(cancel)
            cancelClickListener = listener
            return this
        }

        fun setOnTimePickCancelListener(
            cancel: String?,
            listener: OnTimePickCancelListener?
        ): Builder {
            this.cancel = cancel
            cancelClickListener = listener
            return this
        }

        fun setOnTimePickCancelListener(listener: OnTimePickCancelListener?): Builder {
            cancelClickListener = listener
            return this
        }

        fun showMinute(showMinute: Boolean): Builder {
            this.showMinute = showMinute
            return this
        }

        fun setDividerText(text: String?): Builder {
            divider = text
            return this
        }

        fun setDividerText(@StringRes text: Int): Builder {
            divider = context.getString(text)
            return this
        }

        fun setStartTime(startTime: String?): Builder {
            this.startTime = startTime
            return this
        }

        fun setEndTime(endTime: String?): Builder {
            this.endTime = endTime
            return this
        }

        fun build(): TimePickerDialogFragment {
            val fragment = fragment
            fragment.setTitle(title)
            fragment.setTitleColor(titleColor)
            fragment.setTitleSize(titleSize)
            fragment.showCancelButton(showCancel)
            fragment.setConfirmText(confirmText)
            fragment.setOnConfirmClickListener(listener)
            fragment.setConfirmColor(confirmColor)
            fragment.setCancelText(cancel)
            fragment.setOnCancelClickListener(cancelClickListener)
            fragment.showMinute(showMinute)
            fragment.setDividerText(divider)
            fragment.setStartTime(startTime)
            fragment.setEndTime(endTime)
            return fragment
        }

        protected val fragment: TimePickerDialogFragment
            protected get() = TimePickerDialogFragment()

        fun show(): TimePickerDialogFragment {
            val fragment = build()
            val transaction = context.supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            fragment.show(transaction, null)
            return fragment
        }
    }

    private fun setDividerText(divider: String?) {
        this.divider = divider
    }

    private fun setStartTime(startTime: String?) {
        preStartTime = startTime
    }

    private fun setEndTime(endTime: String?) {
        preEndTime = endTime
    }

    private fun showMinute(showMinute: Boolean) {
        this.showMinute = showMinute
    }

    private fun setTitle(title: String?) {
        this.title = title
    }

    private fun setTitleSize(titleSize: Float) {
        this.titleSize = titleSize
    }

    private fun setTitleColor(titleColor: Int) {
        this.titleColor = titleColor
    }

    private fun showCancelButton(showCancel: Boolean) {
        this.showCancel = showCancel
    }

    private fun setConfirmText(confirmText: String?) {
        this.confirmText = confirmText
    }

    private fun setOnConfirmClickListener(listener: OnTimePickSubmitListener?) {
        this.listener = listener
    }

    private fun setConfirmColor(confirmColor: Int) {
        this.confirmColor = confirmColor
    }

    private fun setCancelText(cancel: String?) {
        this.cancel = cancel
    }

    private fun setOnCancelClickListener(cancelClickListener: OnTimePickCancelListener?) {
        this.cancelClickListener = cancelClickListener
    }
}