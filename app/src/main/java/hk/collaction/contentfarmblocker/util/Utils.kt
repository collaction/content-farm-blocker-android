package hk.collaction.contentfarmblocker.util

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Browser
import androidx.preference.PreferenceManager
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.orhanobut.logger.Logger
import hk.collaction.contentfarmblocker.BuildConfig
import hk.collaction.contentfarmblocker.ui.detector.DetectorActivity
import hk.collaction.contentfarmblocker.ui.main.MainActivity
import java.util.Locale

/**
 * Created by Himphen on 10/1/2016.
 */
object UtilHelper {
    const val PREF_IAP = "iap"
    const val PREF_LANGUAGE = "PREF_LANGUAGE"
    const val PREF_LANGUAGE_COUNTRY = "PREF_LANGUAGE_COUNTRY"

    @Suppress("DEPRECATION")
    fun detectLanguage(context: Context) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        var language = preferences.getString(PREF_LANGUAGE, "") ?: ""
        var languageCountry = preferences.getString(PREF_LANGUAGE_COUNTRY, "") ?: ""
        if (language == "") {
            val locale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Resources.getSystem().configuration.locales[0]
            } else {
                Resources.getSystem().configuration.locale
            }
            language = locale.language
            languageCountry = locale.country
        }
        val res = context.resources
        val conf = res.configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conf.setLocale(Locale(language, languageCountry))
        } else {
            conf.locale = Locale(language, languageCountry)
        }
        val dm = res.displayMetrics
        res.updateConfiguration(conf, dm)
    }

    const val IAP_PID_10 = "iap_10"
    const val IAP_PID_20 = "iap_20"
    const val IAP_PID_50 = "iap_50"

    /**
     * Go to specific url then finish the activity
     *
     * @param activity Activity
     * @param url      String
     */
    fun goToUrl(activity: Activity?, url: String?) {
        activity?.let {
            var tempUrl = url
            var packageName: String? = null
            try {
                val settings = PreferenceManager.getDefaultSharedPreferences(activity)
                packageName = settings.getString("pref_browser", "")
                if (url == null) {
                    throw Exception()
                }
                /* Make sure it has http prefix */
                if (!url.startsWith("https://") && !url.startsWith("http://")) {
                    tempUrl = "http://$url"
                }
                /* Make sure it has packageName */
                if ("" == packageName) {
                    throw Exception()
                }
                /* Try to disable the default app behavior temperately */toggleDefaultApp(
                    activity,
                    false
                )
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(tempUrl)
                if (isUsingSameTab(activity)) {
                    intent.putExtra(Browser.EXTRA_APPLICATION_ID, packageName)
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                activity.startActivity(intent)
            } catch (e: Exception) { /* If something wrong, fallback to using default browser */
                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    intent.setPackage(packageName)
                    if (isUsingSameTab(activity)) {
                        intent.putExtra(Browser.EXTRA_APPLICATION_ID, packageName)
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    activity.startActivity(intent)
                } catch (e2: Exception) {
                    val intent = Intent(activity, MainActivity::class.java)
                    intent.putExtra("no_browser", true)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    activity.startActivity(intent)
                }
            } finally {
                Handler(Looper.getMainLooper()).postDelayed({
                    toggleDefaultApp(activity, true)
                }, 1000)
            }
            activity.finish()
        }
    }

    /**
     * If last app is browser, use same tab
     * else use new tab
     *
     * @param activity Activity
     * @return boolean
     */
    private fun isUsingSameTab(activity: Activity): Boolean {
        val settings = PreferenceManager.getDefaultSharedPreferences(activity)
        if (AppUsageUtils.checkAppUsagePermission(activity) && settings.getBoolean(
                "pref_previous_app_detect",
                false
            )
        ) {
            val browserPackageName = settings.getString("pref_browser", "")
            val lastAppPackageName = AppUsageUtils.getTopActivityPackageName(activity)
            return browserPackageName == lastAppPackageName
        }
        return true
    }

    fun isPurchased(settings: SharedPreferences): Boolean {
        return settings.getBoolean(PREF_IAP, false)
    }

    fun toggleDefaultApp(context: Context?, isEnable: Boolean) {
        if (context != null) {
            val pm = context.packageManager
            val component = ComponentName(context, DetectorActivity::class.java)
            if (isEnable) {
                pm.setComponentEnabledSetting(
                    component,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
            } else {
                pm.setComponentEnabledSetting(
                    component,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
            }
            Logger.d("toggleDefaultApp() $isEnable")
        }
    }

    fun logException(e: Exception) {
        if (BuildConfig.DEBUG) {
            e.printStackTrace()
        } else {
            Firebase.crashlytics.recordException(e)
        }
    }
}