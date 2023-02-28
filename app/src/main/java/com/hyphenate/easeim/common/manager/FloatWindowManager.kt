package com.hyphenate.easeim.common.manager

import android.Manifest
import android.app.AppOpsManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.hyphenate.easeui.utils.RomUtils
import com.hyphenate.util.EMLog

/**
 * 用于Float window的权限检查及权限请求
 */
object FloatWindowManager {
    private val TAG = FloatWindowManager::class.java.simpleName
    fun checkPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return checkOps(context)
        }
        return true
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private fun checkOps(context: Context): Boolean {
        try {
            val `object` = context.getSystemService(Context.APP_OPS_SERVICE) ?: return false
            val localClass: Class<*> = `object`.javaClass
            val arrayOfClass: Array<Class<*>?> = arrayOfNulls(3)
            arrayOfClass[0] = Integer.TYPE
            arrayOfClass[1] = Integer.TYPE
            arrayOfClass[2] = String::class.java
            val method = localClass.getMethod("checkOp", *arrayOfClass) ?: return false
            val arrayOfObject1 = arrayOfNulls<Any>(3)
            arrayOfObject1[0] = 24
            arrayOfObject1[1] = Binder.getCallingUid()
            arrayOfObject1[2] = context.packageName
            val m = method.invoke(`object`, *arrayOfObject1) as Int
            return m == AppOpsManager.MODE_ALLOWED || !RomUtils.isDomesticSpecialRom()
        } catch (ignore: Exception) {
        }
        return false
    }

    fun tryJumpToPermissionPage(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            when (RomUtils.getRomName()) {
                RomUtils.ROM_MIUI -> applyMiuiPermission(context)
                RomUtils.ROM_EMUI -> applyHuaweiPermission(context)
                RomUtils.ROM_VIVO -> applyVivoPermission(context)
                RomUtils.ROM_OPPO -> applyOppoPermission(context)
                RomUtils.ROM_QIKU -> apply360Permission(context)
                RomUtils.ROM_SMARTISAN -> applySmartisanPermission(context)
                RomUtils.ROM_COOLPAD -> applyCoolpadPermission(context)
                RomUtils.ROM_ZTE -> applyZTEPermission(context)
                RomUtils.ROM_LENOVO -> applyLenovoPermission(context)
                RomUtils.ROM_LETV -> applyLetvPermission(context)
                else -> true
            }
        } else {
            if (RomUtils.isMeizuRom()) {
                getAppDetailSettingIntent(context)
            } else if (RomUtils.isVivoRom()) {
                applyVivoPermission(context)
            } else if (RomUtils.isMiuiRom()) {
                applyMiuiPermission(context) || getAppDetailSettingIntent(
                    context
                )
            } else {
                applyCommonPermission(context)
            }
        }
    }

    private fun startActivitySafely(intent: Intent, context: Context): Boolean {
        return try {
            if (isIntentAvailable(intent, context)) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            EMLog.e(TAG, "启动Activity失败！")
            false
        }
    }

    fun isIntentAvailable(intent: Intent?, context: Context): Boolean {
        return intent != null && context.packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        ).size > 0
    }

    private fun applyCommonPermission(context: Context): Boolean {
        return try {
            val clazz: Class<*> = Settings::class.java
            val field = clazz.getDeclaredField("ACTION_MANAGE_OVERLAY_PERMISSION")
            val intent = Intent(field[null].toString())
            intent.data = Uri.parse("package:" + context.packageName)
            startActivitySafely(intent, context)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun applyCoolpadPermission(context: Context): Boolean {
        val intent = Intent()
        intent.setClassName(
            "com.yulong.android.seccenter",
            "com.yulong.android.seccenter.dataprotection.ui.AppListActivity"
        )
        return startActivitySafely(intent, context)
    }

    private fun applyLenovoPermission(context: Context): Boolean {
        val intent = Intent()
        intent.setClassName(
            "com.lenovo.safecenter",
            "com.lenovo.safecenter.MainTab.LeSafeMainActivity"
        )
        return startActivitySafely(intent, context)
    }

    private fun applyZTEPermission(context: Context): Boolean {
        val intent = Intent()
        intent.action = "com.zte.heartyservice.intent.action.startActivity.PERMISSION_SCANNER"
        return startActivitySafely(intent, context)
    }

    private fun applyLetvPermission(context: Context): Boolean {
        val intent = Intent()
        intent.setClassName(
            "com.letv.android.letvsafe",
            "com.letv.android.letvsafe.AppActivity"
        )
        return startActivitySafely(intent, context)
    }

    private fun applyVivoPermission(context: Context): Boolean {
        val intent = Intent()
        intent.putExtra("packagename", context.packageName)
        intent.action = "com.vivo.permissionmanager"
        intent.setClassName(
            "com.vivo.permissionmanager",
            "com.vivo.permissionmanager.activity.SoftPermissionDetailActivity"
        )
        val componentName1 = intent.resolveActivity(context.packageManager)
        if (componentName1 != null) {
            return startActivitySafely(intent, context)
        }
        intent.action = "com.iqoo.secure"
        intent.setClassName(
            "com.iqoo.secure",
            "com.iqoo.secure.safeguard.SoftPermissionDetailActivity"
        )
        val componentName2 = intent.resolveActivity(context.packageManager)
        if (componentName2 != null) {
            return startActivitySafely(intent, context)
        }
        intent.action = "com.iqoo.secure"
        intent.setClassName("com.iqoo.secure", "com.iqoo.secure.MainActivity")
        val componentName3 = intent.resolveActivity(context.packageManager)
        return if (componentName3 != null) {
            startActivitySafely(intent, context)
        } else startActivitySafely(
            intent,
            context
        )
    }

    private fun applyOppoPermission(context: Context): Boolean {
        val intent = Intent()
        intent.putExtra("packageName", context.packageName)
        intent.action = "com.oppo.safe"
        intent.setClassName(
            "com.oppo.safe",
            "com.oppo.safe.permission.PermissionAppListActivity"
        )
        return if (!startActivitySafely(intent, context)) {
            intent.action = "com.color.safecenter"
            intent.setClassName(
                "com.color.safecenter",
                "com.color.safecenter.permission.floatwindow.FloatWindowListActivity"
            )
            if (!startActivitySafely(intent, context)) {
                intent.action = "com.coloros.safecenter"
                intent.setClassName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.sysfloatwindow.FloatWindowListActivity"
                )
                startActivitySafely(intent, context)
            } else {
                true
            }
        } else {
            true
        }
    }

    private fun apply360Permission(context: Context): Boolean {
        val intent = Intent()
        intent.setClassName(
            "com.android.settings",
            "com.android.settings.Settings\$OverlaySettingsActivity"
        )
        return if (!startActivitySafely(intent, context)) {
            intent.setClassName(
                "com.qihoo360.mobilesafe",
                "com.qihoo360.mobilesafe.ui.index.AppEnterActivity"
            )
            startActivitySafely(intent, context)
        } else {
            true
        }
    }

    private fun applyMiuiPermission(context: Context): Boolean {
        val intent = Intent()
        intent.action = "miui.intent.action.APP_PERM_EDITOR"
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.putExtra("extra_pkgname", context.packageName)
        return startActivitySafely(intent, context)
    }

    fun getAppDetailSettingIntent(context: Context): Boolean {
        val localIntent = Intent()
        localIntent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        localIntent.data = Uri.fromParts("package", context.packageName, null)
        return startActivitySafely(localIntent, context)
    }

    private fun applyMeizuPermission(context: Context): Boolean {
        val intent = Intent("com.meizu.safe.security.SHOW_APPSEC")
        intent.setClassName(
            "com.meizu.safe",
            "com.meizu.safe.security.AppSecActivity"
        )
        intent.putExtra("packageName", context.packageName)
        return startActivitySafely(intent, context)
    }

    private fun applyHuaweiPermission(context: Context): Boolean {
        return try {
            val intent = Intent()
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            var comp = ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.addviewmonitor.AddViewMonitorActivity"
            )
            intent.component = comp
            if (!startActivitySafely(intent, context)) {
                comp = ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.notificationmanager.ui.NotificationManagmentActivity"
                )
                intent.component = comp
                context.startActivity(intent)
                true
            } else {
                true
            }
        } catch (e: SecurityException) {
            try {
                val intent = Intent()
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                val comp = ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.permissionmanager.ui.MainActivity"
                )
                intent.component = comp
                context.startActivity(intent)
                true
            } catch (e1: Exception) {
                EMLog.e(TAG, "Huawei跳转失败$e1")
                getAppDetailSettingIntent(context)
            }
        } catch (e: ActivityNotFoundException) {
            try {
                val intent = Intent()
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                val comp = ComponentName(
                    "com.Android.settings",
                    "com.android.settings.permission.TabItem"
                )
                intent.component = comp
                context.startActivity(intent)
                true
            } catch (e2: Exception) {
                EMLog.e(TAG, "Huawei跳转失败$e")
                getAppDetailSettingIntent(context)
            }
        } catch (e: Exception) {
            getAppDetailSettingIntent(context)
        }
    }

    private fun applySmartisanPermission(context: Context): Boolean {
        var intent = Intent("com.smartisanos.security.action.SWITCHED_PERMISSIONS_NEW")
        intent.setClassName(
            "com.smartisanos.security",
            "com.smartisanos.security.SwitchedPermissions"
        )
        intent.putExtra("index", 17) //有版本差异,不一定定位正确
        return if (startActivitySafely(intent, context)) {
            true
        } else {
            intent = Intent("com.smartisanos.security.action.SWITCHED_PERMISSIONS")
            intent.setClassName(
                "com.smartisanos.security",
                "com.smartisanos.security.SwitchedPermissions"
            )
            intent.putExtra(
                "permission",
                arrayOf(Manifest.permission.SYSTEM_ALERT_WINDOW)
            )
            startActivitySafely(intent, context)
        }
    }
}