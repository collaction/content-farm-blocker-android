package hk.collaction.contentfarmblocker.util

import android.annotation.SuppressLint
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import java.util.SortedMap
import java.util.TreeMap

object AppUsageUtils {
    private const val PACKAGE_NAME_UNKNOWN = "unknown"

    /**
     * Android 5.0 support usagestats
     *
     * @param context Context
     * @return boolean
     */
    @SuppressLint("WrongConstant")
    fun checkAppUsagePermission(context: Context?): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val usageStatsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                context?.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager?
            } else {
                context?.getSystemService("usagestats") as UsageStatsManager?
            }
            val currentTime = System.currentTimeMillis()
            // try to get app usage state in last 1 min
            val stats = usageStatsManager?.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                currentTime - 60 * 1000,
                currentTime
            )
            stats?.isNullOrEmpty()?.let {
                return !it
            }
        }

        return false
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun requestAppUsagePermission(context: Context?) {
        try {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context?.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("WrongConstant")
    fun getTopActivityPackageName(context: Context): String {
        var topActivityPackageName = PACKAGE_NAME_UNKNOWN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val usageStatsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager?
            } else {
                context.getSystemService("usagestats") as UsageStatsManager?
            }
            val time = System.currentTimeMillis()
            // We get usage stats for the last 10 seconds
            val usageStatsList = usageStatsManager?.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                time - 1000 * 10,
                time
            )
            // Sort the stats by the last time used
            if (usageStatsList != null) {
                val sortedMap: SortedMap<Long, UsageStats> = TreeMap()
                for (usageStats in usageStatsList) {
                    sortedMap[usageStats.lastTimeUsed] = usageStats
                }
                if (sortedMap.isNotEmpty()) {
                    sortedMap[sortedMap.lastKey()]?.let {
                        topActivityPackageName = it.packageName
                    }
                }
            }
        }
        return topActivityPackageName
    }
}