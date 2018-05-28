package hk.collaction.contentfarmblocker;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Browser;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import hk.collaction.contentfarmblocker.ui.activity.DetectorActivity;
import hk.collaction.contentfarmblocker.ui.activity.MainActivity;

public class C extends Util {

	@SuppressWarnings("unused")
	public static final String TAG = "TAG";
	public static final String IAP_PID_10 = "iap_10";
	public static final String IAP_PID_20 = "iap_20";
	public static final String IAP_PID_50 = "iap_50";

	/**
	 * Go to specific url then finish the activity
	 *
	 * @param mContext Activity
	 * @param url      String
	 */
	public static void goToUrl(Activity mContext, String url) {
		String packageName = null;
		try {
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
			packageName = settings.getString("pref_browser", "");

			if (url == null) {
				throw new Exception();
			}

			/* Make sure it has http prefix */
			if (!url.startsWith("https://") && !url.startsWith("http://")) {
				url = "http://" + url;
			}

			/* Make sure it has packageName */
			if ("".equals(packageName)) {
				throw new Exception();
			}

			/* Try to disable the default app behavior temperately */
			toggleDefaultApp(mContext, false);

			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(url));
			if (isUsingSameTab(mContext)) {
				intent.putExtra(Browser.EXTRA_APPLICATION_ID, packageName);
			}
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			mContext.startActivity(intent);
			mContext.overridePendingTransition(0, 0);
		} catch (Exception e) {
			/* If something wrong, fallback to using default browser */
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
			} catch (Exception e2) {
				Intent intent = new Intent().setClass(mContext, MainActivity.class);
				intent.putExtra("no_browser", true);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

				mContext.startActivity(intent);
			}
		} finally {
			new toggleDefaultAppAsyncTask(mContext).execute();
		}

		mContext.finish();
	}

	private static class toggleDefaultAppAsyncTask extends AsyncTask<Void, Void, Void> {
		private WeakReference<Activity> weakReference;

		toggleDefaultAppAsyncTask(Activity mContext) {
			weakReference = new WeakReference<>(mContext);
		}

		@Override
		protected Void doInBackground(Void... voids) {
			SystemClock.sleep(1000);
			toggleDefaultApp(weakReference.get(), true);
			return null;
		}
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

			return browserPackageName.equals(lastAppPackageName);
		}

		return true;
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Nullable
	private static String getRunningApp(Context mContext) {
		String topPackageName = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			@SuppressLint("WrongConstant")
			UsageStatsManager mUsageStatsManager = (UsageStatsManager) mContext.getSystemService("usagestats");
			if (mUsageStatsManager != null) {
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
		}
		return topPackageName;
	}

	public static boolean isGrantedSystemPermission(Context mContext) {
		boolean granted = false;
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			AppOpsManager appOps = (AppOpsManager) mContext
					.getSystemService(Context.APP_OPS_SERVICE);
			if (appOps != null) {
				int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
						android.os.Process.myUid(), mContext.getPackageName());

				granted = (mode == AppOpsManager.MODE_ALLOWED);
			}
		}

		return granted;
	}

	public static boolean isPurchased(SharedPreferences settings) {
		return settings.getBoolean(Util.PREF_IAP, false);
	}

	public static void toggleDefaultApp(Context mContext, boolean isEnable) {
		PackageManager pm = mContext.getPackageManager();
		ComponentName component = new ComponentName(mContext, DetectorActivity.class);

		if (isEnable) {
			pm.setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
		} else {
			pm.setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
		}

		Log.d(C.TAG, "toggleDefaultApp() " + isEnable);
	}
}
