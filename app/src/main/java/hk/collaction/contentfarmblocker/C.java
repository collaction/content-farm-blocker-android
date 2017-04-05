package hk.collaction.contentfarmblocker;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Browser;
import android.support.annotation.Nullable;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import hk.collaction.contentfarmblocker.ui.activity.MainActivity;

public class C extends Util {

	@SuppressWarnings("unused")
	public static final String TAG = "TAG";

	public static void goToUrl(Activity mContext, String url) {

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
		String packageName = settings.getString("pref_browser", "");

		/* Make sure it has http prefix */
		if (!url.startsWith("https://") && !url.startsWith("http://")) {
			url = "http://" + url;
		}

		try {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(url));
			intent.setPackage(packageName);
			if (isUsingSameTab(mContext)) {
				intent.putExtra(Browser.EXTRA_APPLICATION_ID, packageName);
			}
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			mContext.startActivity(intent);
			mContext.overridePendingTransition(0, 0);
		} catch (Exception e) {
			Intent intent = new Intent().setClass(mContext, MainActivity.class);
			intent.putExtra("no_browser", true);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			mContext.startActivity(intent);
		}

		mContext.finish();
	}

	/**
	 * If last app is browser, use same tab
	 * else use new tab
	 *
	 * @param mContext Activity
	 * @return boolean
	 */
	private static boolean isUsingSameTab(Activity mContext) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);

		if (C.isGrantedSystemPermission(mContext) && settings.getBoolean("pref_previous_app_detect", false)) {
			String browserPackageName = settings.getString("pref_browser", "");
			String lastAppPackageName = getRunningApp(mContext);

			if (!browserPackageName.equals(lastAppPackageName)) {
				return false;
			}
		}

		return true;
	}


	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Nullable
	private static String getRunningApp(Context mContext) {
		String topPackageName = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			//noinspection WrongConstant
			UsageStatsManager mUsageStatsManager = (UsageStatsManager) mContext.getSystemService("usagestats");
			long time = System.currentTimeMillis();
			// We get usage stats for the last 10 seconds
			List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time);
			// Sort the stats by the last time used
			if (stats != null) {
				SortedMap<Long, String> mySortedMap = new TreeMap<>();
				for (UsageStats usageStats : stats) {
					mySortedMap.put(usageStats.getLastTimeUsed(), usageStats.getPackageName());
				}
				if (!mySortedMap.isEmpty()) {
					do {
						topPackageName = mySortedMap.get(mySortedMap.lastKey());
						if (mContext.getPackageName().equals(topPackageName) || "android".equals(topPackageName)) {
							mySortedMap.remove(mySortedMap.lastKey());

							if (mySortedMap.isEmpty())
								break;
						} else {
							break;
						}
					} while (true);
				}
			}
		}
		return topPackageName;
	}

	public static boolean isGrantedSystemPermission(Context mContext) {
		boolean granted = false;
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			AppOpsManager appOps = (AppOpsManager) mContext
					.getSystemService(Context.APP_OPS_SERVICE);
			int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
					android.os.Process.myUid(), mContext.getPackageName());

			granted = (mode == AppOpsManager.MODE_ALLOWED);
		}

		return granted;
	}

}
