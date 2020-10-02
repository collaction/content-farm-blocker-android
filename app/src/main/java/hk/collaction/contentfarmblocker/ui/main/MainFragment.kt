package hk.collaction.contentfarmblocker.ui.main

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.list.customListAdapter
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.BillingProcessor.IBillingHandler
import com.anjlab.android.iab.v3.TransactionDetails
import com.blankj.utilcode.util.AppUtils
import hk.collaction.contentfarmblocker.BuildConfig
import hk.collaction.contentfarmblocker.R
import hk.collaction.contentfarmblocker.model.AppItem
import hk.collaction.contentfarmblocker.ui.main.AppItemAdapter.ItemClickListener
import hk.collaction.contentfarmblocker.util.AppUsageUtils
import hk.collaction.contentfarmblocker.util.Utils
import hk.collaction.contentfarmblocker.util.Utils.IAP_PID_10
import hk.collaction.contentfarmblocker.util.Utils.IAP_PID_20
import hk.collaction.contentfarmblocker.util.Utils.IAP_PID_50
import hk.collaction.contentfarmblocker.util.Utils.PREF_IAP
import hk.collaction.contentfarmblocker.util.Utils.isPurchased
import hk.collaction.contentfarmblocker.util.Utils.toggleDefaultApp
import java.util.ArrayList
import java.util.Random

class MainFragment : PreferenceFragmentCompat() {
    private lateinit var sharedPreferences: SharedPreferences
    private var prefBrowser: Preference? = null
    private var prefDonate: Preference? = null
    private var prefPreviousAppDetect: CheckBoxPreference? = null
    private lateinit var billingProcessor: BillingProcessor

    private var isShowMeow = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.pref_general)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        billingProcessor = BillingProcessor(context, BuildConfig.GOOGLE_IAP_KEY,
            object : IBillingHandler {
                override fun onProductPurchased(productId: String, details: TransactionDetails?) {
                    if (productId == IAP_PID_10 || productId == IAP_PID_20 || productId == IAP_PID_50) {
                        sharedPreferences.edit().putBoolean(PREF_IAP, true).apply()
                        purchased()
                        showDonateDialog()
                    }
                }

                override fun onPurchaseHistoryRestored() {
                    if (billingProcessor.isPurchased(IAP_PID_10)
                        || billingProcessor.isPurchased(IAP_PID_20)
                        || billingProcessor.isPurchased(IAP_PID_50)
                    ) {
                        sharedPreferences.edit().putBoolean(PREF_IAP, true).apply()
                        purchased()
                    }
                }

                override fun onBillingError(errorCode: Int, error: Throwable?) {}
                override fun onBillingInitialized() {}
            })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefBrowser = findPreference("pref_browser")
        prefBrowser?.summary = sharedPreferences.getString(
            "pref_browser_app_name",
            getString(R.string.pref_no_browser)
        )
        prefBrowser?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            loadBrowserList()
            false
        }
        prefDonate = findPreference("pref_donate")
        prefDonate?.summary = donateSummary
        if (isPurchased(sharedPreferences)) {
            purchased()
        } else {
            prefDonate?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                MaterialDialog(it.context)
                    .title(R.string.ui_donate_title)
                    .listItemsSingleChoice(
                        R.array.donate_choose,
                        initialSelection = -1,
                        waitForPositiveButton = false
                    ) { dialog, index, _ ->
                        when (index) {
                            0 -> checkPayment(IAP_PID_10)
                            1 -> checkPayment(IAP_PID_20)
                            2 -> checkPayment(IAP_PID_50)
                        }
                        dialog.dismiss()
                    }
                    .negativeButton(R.string.ui_cancel)
                    .show()
                false
            }
        }
        findPreference<Preference>("pref_whitelist")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { preference ->
                val whitelist = sharedPreferences.getString("pref_whitelist", "")
                val dialogView = View.inflate(context, R.layout.dialog_custom_list, null)
                dialogView.findViewById<EditText>(R.id.inputEt).setText(whitelist)
                MaterialDialog(preference.context)
                    .title(text = preference.title.toString())
                    .customView(view = dialogView)
                    .cancelable(false)
                    .negativeButton(R.string.ui_cancel)
                    .positiveButton(R.string.ui_okay) { dialog ->
                        var result =
                            dialog.view.findViewById<EditText>(R.id.inputEt).text.toString()
                        result = result.replace("https://", "", true)
                        result = result.replace("http://", "", true)
                        sharedPreferences.edit().putString("pref_whitelist", result).apply()
                    }
                    .show()
                false
            }
        findPreference<Preference>("pref_default_list")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                try {
                    val uri =
                        Uri.parse("https://github.com/collaction/content-farm-blocker-android/blob/master/app/src/main/res/raw/site.txt")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, R.string.ui_error, Toast.LENGTH_SHORT).show()
                }
                false
            }
        findPreference<Preference>("pref_blacklist")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { preference ->
                val blacklist = sharedPreferences.getString("pref_blacklist", "")
                val dialogView = View.inflate(context, R.layout.dialog_custom_list, null)
                dialogView.findViewById<EditText>(R.id.inputEt).setText(blacklist)
                MaterialDialog(preference.context)
                    .title(text = preference.title.toString())
                    .customView(view = dialogView)
                    .cancelable(false)
                    .negativeButton(R.string.ui_cancel)
                    .positiveButton(R.string.ui_okay) { dialog ->
                        var result =
                            dialog.view.findViewById<EditText>(R.id.inputEt).text.toString()
                        result = result.replace("https://", "", true)
                        result = result.replace("http://", "", true)
                        sharedPreferences.edit().putString("pref_blacklist", result).apply()
                    }
                    .show()
                false
            }
        findPreference<Preference>("pref_language")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { preference ->
                MaterialDialog(preference.context)
                    .title(R.string.action_language)
                    .listItemsSingleChoice(
                        R.array.language_choose,
                        waitForPositiveButton = false
                    ) { dialog, index, _ ->
                        dialog.dismiss()
                        val editor = sharedPreferences.edit()
                        val languageLocaleCodeArray =
                            resources.getStringArray(R.array.language_locale_code)
                        val languageLocaleCountryCodeArray =
                            resources.getStringArray(R.array.language_locale_country_code)
                        editor.putString(Utils.PREF_LANGUAGE, languageLocaleCodeArray[index])
                            .putString(
                                Utils.PREF_LANGUAGE_COUNTRY,
                                languageLocaleCountryCodeArray[index]
                            )
                            .apply()
                        startActivity(Intent(preference.context, MainActivity::class.java))
                        (preference.context as Activity?)?.finish()
                    }
                    .negativeButton(R.string.ui_cancel)
                    .show()
                false
            }
        findPreference<Preference>("pref_enable")?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference, newValue ->
                toggleDefaultApp(preference.context, newValue as Boolean)
                true
            }

        prefPreviousAppDetect = findPreference("pref_previous_app_detect")
        /* Set version */
        val prefVersion = findPreference<Preference>("pref_version")
        prefVersion?.summary = AppUtils.getAppVersionName()
        prefVersion?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { preference ->
                if (!isShowMeow) {
                    preference.summary = ""
                    isShowMeow = true
                }
                preference.summary = preference.summary.toString() + "\uD83D\uDC31"
                false
            }
        findPreference<Preference>("pref_report")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                val intent = Intent(Intent.ACTION_SEND)
                var meta = "Android Version: " + Build.VERSION.RELEASE + "\n"
                meta += "SDK Level: " + Build.VERSION.SDK_INT + "\n"
                meta += "Version: " + AppUtils.getAppVersionName() + "\n"
                meta += "Brand: " + Build.BRAND + "\n"
                meta += "Model: " + Build.MODEL + "\n\n"
                intent.type = "message/rfc822"
                intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("hello@collaction.hk"))
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.pref_report_title))
                intent.putExtra(Intent.EXTRA_TEXT, meta)
                startActivity(intent)
                false
            }
        findPreference<Preference>("pref_rate")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                try {
                    val uri = Uri.parse("market://details?id=hk.collaction.contentfarmblocker")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, R.string.ui_error, Toast.LENGTH_SHORT).show()
                }
                false
            }
        findPreference<Preference>("pref_testing")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                try {
                    val uri = Uri.parse("http://www.example.com")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, R.string.ui_error, Toast.LENGTH_SHORT).show()
                }
                false
            }
        findPreference<Preference>("pref_report_work")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                try {
                    val uri =
                        Uri.parse("https://docs.google.com/forms/d/e/1FAIpQLScP-XrmmYs3hP_uYw1rF2lotOFzVfTFKJN_MGQDNL27lO2Pkg/viewform")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, R.string.ui_error, Toast.LENGTH_SHORT).show()
                }
                false
            }
        findPreference<Preference>("pref_share")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                val intent = Intent(Intent.ACTION_SEND)
                intent.putExtra(
                    Intent.EXTRA_TEXT, getString(R.string.pref_share_desc) +
                            "https://play.google.com/store/apps/details?id=hk.collaction.contentfarmblocker"
                )
                intent.type = "text/plain"
                startActivity(Intent.createChooser(intent, getString(R.string.ui_share)))
                false
            }
        findPreference<Preference>("pref_author")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                try {
                    val uri = Uri.parse("market://search?q=pub:\"Collaction 小隊\"")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, R.string.ui_error, Toast.LENGTH_SHORT).show()
                }
                false
            }
        findPreference<Preference>("pref_collaction")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                try {
                    val uri = Uri.parse("https://www.collaction.hk/s/collactionopensource")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, R.string.ui_error, Toast.LENGTH_SHORT).show()
                }
                false
            }

        arguments?.getBoolean("no_browser")?.let {
            if (it) {
                loadBrowserList()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            prefPreviousAppDetect?.isEnabled = true
            if (AppUsageUtils.checkAppUsagePermission(context)) {
                if (sharedPreferences.getBoolean("pref_previous_app_detect", false)) {
                    prefPreviousAppDetect?.summary =
                        getString(R.string.pref_previous_app_summary_on)
                } else {
                    prefPreviousAppDetect?.summary =
                        getString(R.string.pref_previous_app_summary_off)
                }
                prefPreviousAppDetect?.onPreferenceClickListener = null
                prefPreviousAppDetect?.onPreferenceChangeListener =
                    Preference.OnPreferenceChangeListener { _, newValue ->
                        if (newValue as Boolean) {
                            prefPreviousAppDetect?.summary =
                                getString(R.string.pref_previous_app_summary_on)
                        } else {
                            prefPreviousAppDetect?.summary =
                                getString(R.string.pref_previous_app_summary_off)
                        }
                        true
                    }
            } else {
                prefPreviousAppDetect?.isChecked = false
                prefPreviousAppDetect?.summary = getString(R.string.pref_previous_app_permission)
                prefPreviousAppDetect?.onPreferenceClickListener =
                    Preference.OnPreferenceClickListener {
                        AppUsageUtils.requestAppUsagePermission(context)
                        false
                    }
                prefPreviousAppDetect?.onPreferenceChangeListener =
                    Preference.OnPreferenceChangeListener { _, _ -> false }
            }
        } else {
            prefPreviousAppDetect?.isChecked = false
            prefPreviousAppDetect?.isEnabled = false
            prefPreviousAppDetect?.summary = getString(R.string.pref_previous_app_not_support)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        billingProcessor.release()
    }

    /**
     * Get all browser apps
     */
    private fun loadBrowserList() {
        activity?.let { activity ->
            val appList = ArrayList<AppItem>()
            var browserDialog: MaterialDialog? = null

            toggleDefaultApp(activity, false)

            val packageManager = activity.packageManager
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
            val pkgAppsList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                packageManager?.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            } else {
                packageManager?.queryIntentActivities(intent, 0)
            }

            pkgAppsList?.let {
                for (info in pkgAppsList) {
                    val packageName = info.activityInfo.packageName
                    /* Just skip this app */
                    if (packageName == activity.packageName) {
                        continue
                    }
                    /* Add the browser to the appList */
                    try {
                        val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
                        appList.add(
                            AppItem(
                                appName = applicationInfo.loadLabel(packageManager).toString(),
                                packageName = applicationInfo.packageName,
                                icon = applicationInfo.loadIcon(packageManager)
                            )
                        )
                    } catch (ignored: PackageManager.NameNotFoundException) {
                    }
                }
            }

            if (sharedPreferences.getBoolean("pref_enable", false)) {
                toggleDefaultApp(activity, true)
            }

            val appItemAdapter = AppItemAdapter(appList, object : ItemClickListener {
                override fun onItemDetailClick(appItem: AppItem) {
                    sharedPreferences.edit()
                        .putString("pref_browser", appItem.packageName)
                        .putString("pref_browser_app_name", appItem.appName)
                        .apply()
                    prefBrowser?.summary =
                        sharedPreferences.getString("pref_browser_app_name", appItem.appName)
                    browserDialog?.dismiss()
                }
            })
            browserDialog = MaterialDialog(activity)
                .title(R.string.pref_browser_title)
                .customListAdapter(appItemAdapter)
                .negativeButton(R.string.ui_cancel)
            browserDialog.show()
        }
    }

    private fun checkPayment(productId: String) {
        val isAvailable = BillingProcessor.isIabServiceAvailable(context)
        if (isAvailable) {
            billingProcessor.purchase(activity, productId)
        } else {
            Toast.makeText(context, R.string.ui_error, Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!billingProcessor.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    }

    private fun purchased() {
        prefDonate?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            showDonateDialog()
            false
        }
    }

    private fun showDonateDialog() {
        context?.let { context ->
            MaterialDialog(context)
                .customView(R.layout.dialog_donate)
                .positiveButton(R.string.ui_okay)
                .show()
        }
    }

    private val donateSummary: String
        get() {
            val array = resources.getStringArray(R.array.donate_summary)
            val rnd = Random().nextInt(array.size)
            return array[rnd]
        }

    companion object {
        fun newInstance(isNoBrowser: Boolean): MainFragment {
            val fragment = MainFragment()
            val args = Bundle()
            args.putBoolean("no_browser", isNoBrowser)
            fragment.arguments = args
            return fragment
        }
    }
}