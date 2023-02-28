package com.hyphenate.easeim.common.interfaceOrImplement

import android.app.Activity
import android.app.ActivityManager
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import com.hyphenate.easecallkit.base.EaseCallFloatWindow
import com.hyphenate.easeim.R
import com.hyphenate.easeim.login.activity.PreviewActivity

/**
 * 专门用于维护声明周期
 */
class UserActivityLifecycleCallbacks : ActivityLifecycleCallbacks, ActivityState {
    override val activityList = ArrayList<Activity>()
    private val resumeActivity: MutableList<Activity> = ArrayList()
    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        Log.e("ActivityLifecycle", "onActivityCreated " + activity.localClassName)
        activityList.add(0, activity)
    }

    override fun onActivityStarted(activity: Activity) {
        Log.e("ActivityLifecycle", "onActivityStarted " + activity.localClassName)
    }

    override fun onActivityResumed(activity: Activity) {
        Log.e(
            "ActivityLifecycle",
            "onActivityResumed activity's taskId = " + activity.taskId + " name: " + activity.localClassName
        )
        if (!resumeActivity.contains(activity)) {
            resumeActivity.add(activity)
            if (resumeActivity.size == 1) {
                //do nothing
            }
            restartSingleInstanceActivity(activity)
        }
    }

    override fun onActivityPaused(activity: Activity) {
        Log.e("ActivityLifecycle", "onActivityPaused " + activity.localClassName)
    }

    override fun onActivityStopped(activity: Activity) {
        Log.e("ActivityLifecycle", "onActivityStopped " + activity.localClassName)
        resumeActivity.remove(activity)
        if (resumeActivity.isEmpty()) {
            val a = getOtherTaskSingleInstanceActivity(activity.taskId)
            if (isTargetSingleInstance(a) && !EaseCallFloatWindow.getInstance().isShowing) {
                makeTaskToFront(a)
            }
            Log.e("ActivityLifecycle", "在后台了")
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {
        Log.e("ActivityLifecycle", "onActivitySaveInstanceState " + activity.localClassName)
    }

    override fun onActivityDestroyed(activity: Activity) {
        Log.e("ActivityLifecycle", "onActivityDestroyed " + activity.localClassName)
        activityList.remove(activity)
    }

    override fun current(): Activity? {
        return if (activityList.size > 0) activityList[0] else null
    }

    override fun count(): Int {
        return activityList.size
    }

    override val isFront: Boolean
        get() = resumeActivity.size > 0

    /**
     * 跳转到目标activity
     * @param cls
     */
    fun skipToTarget(cls: Class<*>?) {
        if (activityList.isNotEmpty()) {
            current()?.startActivity(Intent(current(), cls))
            for (activity in activityList) {
                activity.finish()
            }
        }
    }

    /**
     * finish target activity
     * @param cls
     */
    fun finishTarget(cls: Class<*>) {
        if (activityList.isNotEmpty()) {
            for (activity in activityList) {
                if (activity.javaClass == cls) {
                    activity.finish()
                }
            }
        }
    }

    /**
     * 判断app是否在前台
     * @return
     */
    val isOnForeground: Boolean
        get() = resumeActivity.isNotEmpty()

    /**
     * 用于按下home键，点击图标，检查启动模式是singleInstance，且在activity列表中首位的Activity
     * 下面的方法，专用于解决启动模式是singleInstance, 为开启悬浮框的情况
     * @param activity
     */
    private fun restartSingleInstanceActivity(activity: Activity) {
        val isClickByFloat = activity.intent.getBooleanExtra("isClickByFloat", false)
        if (isClickByFloat) {
            return
        }
        //刚启动，或者从桌面返回app
        if (resumeActivity.size == 1 && resumeActivity[0] is PreviewActivity) {
            return
        }
        //至少需要activityList中至少两个activity
        if (resumeActivity.size >= 1 && activityList.size > 1) {
            val a = getOtherTaskSingleInstanceActivity(resumeActivity[0].taskId)
            if (a != null && !a.isFinishing //没有正在finish
                && a !== activity //当前activity和列表中首个activity不相同
                && a.taskId != activity.taskId && !EaseCallFloatWindow.getInstance().isShowing
            ) {
                Log.e("ActivityLifecycle", "启动了activity = " + a.javaClass.name)
                activity.startActivity(Intent(activity, a.javaClass))
            }
        }
    }

    private fun getOtherTaskSingleInstanceActivity(taskId: Int): Activity? {
        if (taskId != 0 && activityList.size > 1) {
            for (activity in activityList) {
                if (activity.taskId != taskId) {
                    if (isTargetSingleInstance(activity)) {
                        return activity
                    }
                }
            }
        }
        return null
    }

    /**
     * 此方法用于设置启动模式为singleInstance的activity调用
     * 用于解决点击悬浮框后，然后finish当前的activity，app回到桌面的问题
     * 需要如下两个权限：
     * <uses-permission android:name="android.permission.GET_TASKS"></uses-permission>
     * <uses-permission android:name="android.permission.REORDER_TASKS"></uses-permission>
     * @param activity
     */
    fun makeMainTaskToFront(activity: Activity) {
        //当前activity正在finish，且可见的activity列表中只有这个正在finish的activity,且没有销毁的activity个数大于等于2
        if (activity.isFinishing && resumeActivity.size == 1 &&
            resumeActivity[0] === activity && activityList.size > 1
        ) {
            val manager = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val runningTasks = manager.getRunningTasks(20)
            for (i in runningTasks.indices) {
                val taskInfo = runningTasks[i]
                val topActivity = taskInfo.topActivity
                //判断是否是相同的包名
                if (topActivity != null && topActivity.packageName == activity.packageName) {
                    val taskId: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        taskInfo.taskId
                    } else {
                        taskInfo.id
                    }
                    //将任务栈置于前台
                    Log.e(
                        "ActivityLifecycle",
                        "执行moveTaskToFront，current activity:" + activity.javaClass.name
                    )
                    manager.moveTaskToFront(taskId, ActivityManager.MOVE_TASK_WITH_HOME)
                }
            }
        }
    }

    private fun isTargetSingleInstance(activity: Activity?): Boolean {
        if (activity == null) {
            return false
        }
        val title = activity.title
        return (TextUtils.equals(
            title,
            activity.getString(R.string.demo_activity_label_video_call)
        )
                || TextUtils.equals(
            title,
            activity.getString(R.string.demo_activity_label_multi_call)
        ))
    }

    private fun makeTaskToFront(activity: Activity?) {
        Log.e("ActivityLifecycle", "makeTaskToFront activity: " + activity!!.localClassName)
        val manager = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        manager.moveTaskToFront(activity.taskId, ActivityManager.MOVE_TASK_WITH_HOME)
    }
}