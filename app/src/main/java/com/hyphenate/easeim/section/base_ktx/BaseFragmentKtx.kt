package com.hyphenate.easeim.section.base_ktx

import android.content.Context
import com.hyphenate.easeui.ui.base.EaseBaseFragment
import androidx.annotation.StringRes
import com.hyphenate.easeim.common.interfaceOrImplement.DialogCallBack
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.interfaceOrImplement.OnResourceParseCallback
import androidx.appcompat.app.AppCompatActivity
import android.graphics.PorterDuff
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.hyphenate.easeim.common.net.Resource

open class BaseFragmentKtx : EaseBaseFragment() {
   lateinit var baseActivity: BaseActivityKtx
    override fun onAttach(context: Context) {
        super.onAttach(context)
        baseActivity = context as BaseActivityKtx
    }

    fun showDialog(@StringRes message: Int, callBack: DialogCallBack?) {
        showDialog(
            resources.getString(R.string.em_dialog_default_title),
            resources.getString(message),
            callBack
        )
    }

    fun showDialog(message: String?, callBack: DialogCallBack?) {
        showDialog(resources.getString(R.string.em_dialog_default_title), message, callBack)
    }

    fun showDialog(@StringRes title: Int, @StringRes message: Int, callBack: DialogCallBack?) {
        showDialog(resources.getString(title), resources.getString(message), callBack)
    }

    fun showDialog(title: String?, message: String?, callBack: DialogCallBack?) {
        AlertDialog.Builder(mContext)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(mContext.getString(R.string.confirm)) { dialog, which ->
                callBack?.onClick(
                    dialog,
                    which
                )
            }
            .setNegativeButton(mContext.getString(R.string.cancel), null)
            .show()
    }

    fun <T> parseResource(response: Resource<T>, callback: OnResourceParseCallback<T>) {
        baseActivity.parseResource<T>(response, callback)
    }

    fun showLoading() {
        baseActivity.showLoading()
    }

    fun showLoading(message: String?) {
        baseActivity.showLoading(message)
    }

    fun dismissLoading() {
        baseActivity.dismissLoading()
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