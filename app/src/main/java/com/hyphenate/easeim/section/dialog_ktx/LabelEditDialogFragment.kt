package com.hyphenate.easeim.section.dialog_ktx

import android.app.Activity
import android.graphics.Rect
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.Window
import android.widget.FrameLayout
import androidx.fragment.app.FragmentTransaction
import com.hyphenate.easeim.R
import com.hyphenate.easeim.databinding.LabelPopwindowBinding
import com.hyphenate.easeim.section.base_ktx.BaseActivityKtx
import com.hyphenate.easeim.section.base_ktx.BaseDialogKtxFragment
import com.hyphenate.easeim.section.dialog_ktx.LabelEditDialogFragment.KeyboardStateObserver.OnKeyboardVisibilityListener
import com.hyphenate.util.EMLog

class LabelEditDialogFragment : BaseDialogKtxFragment<LabelPopwindowBinding>() {
    private var listener: OnConfirmClickListener? = null
    private var dialogWindow: Window? = null

    interface OnGetSoftHeightListener {
        fun onShowed(height: Int)
    }

    interface OnSoftKeyWordShowListener {
        fun hasShow(isShow: Boolean)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.AppTheme)
    }

    override val layoutId: Int
        get() = R.layout.label_popwindow

    override fun initListener() {
        bind.confirm.setOnClickListener { v: View ->
            val content = bind.etContent.text.toString().trim { it <= ' ' }
            listener?.onConfirm(v, content)
            dismiss()
        }
        bind.cancel.setOnClickListener { v: View? -> dismiss() }
        KeyboardStateObserver.getKeyboardStateObserver(activity)
            .setKeyboardVisibilityListener(object : OnKeyboardVisibilityListener {
                override fun onKeyboardShow() {
                    EMLog.d("onGlobalLayout", "listener: onKeyboardShow")
                    if (activity == null) return
                    getSoftKeyboardHeight(
                        activity!!.currentFocus,
                        object : OnGetSoftHeightListener {
                            override fun onShowed(height: Int) {
                                EMLog.d("onGlobalLayout", "onShowed: $height")
                                try {
                                    val lp = dialogWindow!!.attributes
                                    lp.dimAmount = 0.6f
                                    lp.width = ViewGroup.LayoutParams.MATCH_PARENT
                                    lp.height = height
                                    lp.gravity = Gravity.TOP
                                    if (dialog != null) setDialogParams(lp)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        })
                }

                override fun onKeyboardHide() {
                    EMLog.d("onGlobalLayout", "listener: onKeyboardHide")
                    try {
                        val lp = dialogWindow!!.attributes
                        lp.dimAmount = 0.6f
                        lp.width = ViewGroup.LayoutParams.MATCH_PARENT
                        lp.height = ViewGroup.LayoutParams.MATCH_PARENT
                        lp.gravity = Gravity.CENTER
                        if (dialog != null) setDialogParams(lp)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
    }

    override fun onStart() {
        super.onStart()
        setDialogFullParams()
        dialogWindow = dialog!!.window
    }

    override fun initArgument() {
        super.initArgument()
    }

    fun setOnConfirmClickListener(listener: OnConfirmClickListener?) {
        this.listener = listener
    }

    interface OnConfirmClickListener {
        fun onConfirm(view: View, content: String)
    }

    class Builder(private val context: BaseActivityKtx) {
        var listener: OnConfirmClickListener? = null
        fun setOnConfirmClickListener(listener: OnConfirmClickListener?): Builder {
            this.listener = listener
            return this
        }

        fun build(): LabelEditDialogFragment {
            val fragment = LabelEditDialogFragment()
            fragment.setOnConfirmClickListener(listener)
            return fragment
        }

        fun show(): LabelEditDialogFragment {
            val fragment = build()
            val transaction = context.supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            fragment.show(transaction, null)
            return fragment
        }
    }

    class KeyboardStateObserver private constructor(activity: Activity?) {
        private val mChildOfContent: View
        private var usableHeightPrevious = 0
        private var listener: OnKeyboardVisibilityListener? = null
        fun setKeyboardVisibilityListener(listener: OnKeyboardVisibilityListener?) {
            this.listener = listener
        }

        init {
            val content = activity!!.findViewById<View>(android.R.id.content) as FrameLayout
            mChildOfContent = content.getChildAt(0)
            mChildOfContent.viewTreeObserver.addOnGlobalLayoutListener { possiblyResizeChildOfContent() }
        }

        private fun possiblyResizeChildOfContent() {
            val usableHeightNow = computeUsableHeight()
            if (usableHeightNow != usableHeightPrevious) {
                val usableHeightSansKeyboard = mChildOfContent.rootView.height
                val heightDifference = usableHeightSansKeyboard - usableHeightNow
                if (heightDifference > usableHeightSansKeyboard / 4) {
                    if (listener != null) {
                        listener!!.onKeyboardShow()
                    }
                } else {
                    if (listener != null) {
                        listener!!.onKeyboardHide()
                    }
                }
                usableHeightPrevious = usableHeightNow
                EMLog.d(
                    TAG,
                    "usableHeightNow: $usableHeightNow | usableHeightSansKeyboard:$usableHeightSansKeyboard | heightDifference:$heightDifference"
                )
            }
        }

        private fun computeUsableHeight(): Int {
            val r = Rect()
            mChildOfContent.getWindowVisibleDisplayFrame(r)
            EMLog.d(TAG, "rec bottom>" + r.bottom + " | rec top>" + r.top)
            return r.bottom - r.top // 全屏模式下： return r.bottom
        }

        interface OnKeyboardVisibilityListener {
            //软键盘弹出
            fun onKeyboardShow()

            //软键盘隐藏
            fun onKeyboardHide()
        }

        companion object {
            private val TAG = KeyboardStateObserver::class.java.simpleName
            fun getKeyboardStateObserver(activity: Activity?): KeyboardStateObserver {
                return KeyboardStateObserver(activity)
            }
        }
    }

    companion object {
        /** * 获取软键盘的高度 * *
         * @param rootView *
         * @param listener
         */
        fun getSoftKeyboardHeight(rootView: View?, listener: OnGetSoftHeightListener?) {
            val layoutListener: OnGlobalLayoutListener = object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val rect = Rect()
                    rootView!!.getWindowVisibleDisplayFrame(rect)
                    val screenHeight = rootView.rootView.height
                    EMLog.d("onGlobalLayout", "screenHeight: $screenHeight")
                    val heightDifference = screenHeight - rect.bottom
                    EMLog.d("onGlobalLayout", rect.bottom.toString() + "#" + screenHeight)
                    EMLog.d("onGlobalLayout", "heightDifference: $heightDifference")
                    //设置一个阀值来判断软键盘是否弹出
                    val visible = heightDifference > screenHeight / 4
                    if (visible) {
                        if (listener != null) {
                            listener.onShowed(screenHeight - heightDifference)
                            EMLog.d("onGlobalLayout", "listener: onShowed")
                        }
                        rootView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                }
            }
            rootView!!.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
        }
    }
}