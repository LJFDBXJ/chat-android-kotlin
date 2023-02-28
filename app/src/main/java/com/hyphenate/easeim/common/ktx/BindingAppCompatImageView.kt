package com.hyphenate.easeim.common.ktx

import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.widget.GlideApp

/**
 *  Created by LXJDBXJ on 2022/09/17.
 *  Describe :
 */
@BindingAdapter(
    "errorId",
    "url",
    "res",
    "type",
    "roundingRadius",
    "borderColor",
    "borderWidth",
    "skipCache",
    "imgClick",
    requireAll = false
)
fun AppCompatImageView.img(
    errorId: Int,
    url: String? = null,
    res: Int,
    type: Int = 0,
    roundingRadius: Int = 20,
    borderColor: Int,
    borderWidth: Float,
    skipCache: Boolean = false,
    action: (() -> Unit)? = null
) {
    when (type) {
        //正常
        0 -> GlideApp.with(this)
            .load(url).skipMemoryCache(false)
            .transform()
            .error(errorId.takeIf { it > 0 } ?: R.mipmap.ic_launcher)
            .into(this)
        //圆形
        1 -> GlideApp.with(this)
            .load(url)
            .transform(CircleCrop())
            .error(errorId.takeIf { it > 0 } ?: R.mipmap.ic_launcher)
            .into(this)

        //圆角矩形
        2 -> {
            val ootion = RequestOptions()
            GlideApp.with(this)
                .load(url)
                .transform(
                    MultiTransformation(CenterCrop(),
                        RoundedCorners(roundingRadius.takeIf { it > 0 } ?: 20))
                )
                .apply(ootion)
                .error(errorId.takeIf { it > 0 } ?: R.mipmap.ic_launcher)
                .into(this)
        }

        //设置本地图片资源
        3 -> setImageResource(res)



    }



    action?.let {
        setOnClickListener { it() }
    }
}


@BindingAdapter("isVisibility")
fun View.isVisibility(isVisibility: Boolean) {
    visibility = if (isVisibility) {
        View.VISIBLE
    } else
        View.GONE

}

@BindingAdapter("isInVisibility")
fun View.isInVisibility(isVisibility: Boolean) {
    visibility = if (isVisibility) {
        View.VISIBLE
    } else
        View.INVISIBLE

}