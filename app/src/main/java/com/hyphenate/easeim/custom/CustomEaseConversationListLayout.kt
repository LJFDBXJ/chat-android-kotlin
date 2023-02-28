package com.hyphenate.easeim.custom

import android.content.Context
import android.util.AttributeSet
import com.hyphenate.easeui.modules.conversation.EaseConversationListLayout
import com.hyphenate.easeui.modules.conversation.model.EaseConversationInfo

/**
 * Created by LXJDBXJ
 * @Date 2022/10/7 14:27
 * @Description
 */
class CustomEaseConversationListLayout:EaseConversationListLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun sortConversationListSuccess(data: MutableList<EaseConversationInfo>) {
        super.sortConversationListSuccess(data)

    }
}