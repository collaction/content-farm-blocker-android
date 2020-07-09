package hk.collaction.contentfarmblocker

import android.app.Application
import android.content.Context
import com.blankj.utilcode.util.Utils
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import hk.collaction.contentfarmblocker.helper.UtilHelper
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

        // init AdMob
        initAdMob()

        // init Crashlytics
        initCrashlytics()
    }

    private fun initAdMob() {
        if (BuildConfig.DEBUG) {
            val testDevices = ArrayList<String>()
            testDevices.add(AdRequest.DEVICE_ID_EMULATOR)
            testDevices.add(UtilHelper.getAdMobDeviceID(this))

            val requestConfiguration = RequestConfiguration.Builder()
                .setTestDeviceIds(testDevices)
                .build()
            MobileAds.setRequestConfiguration(requestConfiguration)
        }
    }

    private fun initCrashlytics() {
        var isGooglePlay = false
        packageManager.getInstallerPackageName(packageName)?.let { installerPackageName ->
            val allowedPackageNames = ArrayList<String>()
            allowedPackageNames.add("com.android.vending")
            allowedPackageNames.add("com.google.android.feedback")
            isGooglePlay = allowedPackageNames.contains(installerPackageName)
        }

        if (isGooglePlay || BuildConfig.DEBUG) {
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.setCrashlyticsCollectionEnabled(true)
            crashlytics.setCustomKey("isGooglePlay", isGooglePlay)
        }
    }
}