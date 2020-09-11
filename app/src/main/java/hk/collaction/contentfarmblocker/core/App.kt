package hk.collaction.contentfarmblocker.core

import android.app.Application
import android.content.Context
import android.os.Build
import com.blankj.utilcode.util.Utils
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import hk.collaction.contentfarmblocker.BuildConfig
import java.util.ArrayList

class App : Application() {
    companion object {
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext

        // init logger
        Logger.addLogAdapter(object : AndroidLogAdapter() {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return BuildConfig.DEBUG
            }
        })

        Utils.init(this)

        // init Crashlytics
        initCrashlytics()
    }

    private fun initCrashlytics() {
        var isGooglePlay = false
        val allowedPackageNames = ArrayList<String>()
        allowedPackageNames.add("com.android.vending")
        allowedPackageNames.add("com.google.android.feedback")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            packageManager.getInstallSourceInfo(packageName).initiatingPackageName?.let { initiatingPackageName ->
                isGooglePlay = allowedPackageNames.contains(initiatingPackageName)
            }
        } else {
            @Suppress("DEPRECATION")
            packageManager.getInstallerPackageName(packageName)?.let { installerPackageName ->
                isGooglePlay = allowedPackageNames.contains(installerPackageName)
            }
        }

        if (isGooglePlay || BuildConfig.DEBUG) {
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.setCrashlyticsCollectionEnabled(true)
            crashlytics.setCustomKey("isGooglePlay", isGooglePlay)
        }
    }
}