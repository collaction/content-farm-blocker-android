package hk.collaction.contentfarmblocker.ui.fragment;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

import hk.collaction.contentfarmblocker.C;
import hk.collaction.contentfarmblocker.R;
import hk.collaction.contentfarmblocker.model.AppItem;
import hk.collaction.contentfarmblocker.ui.activity.DetectorActivity;
import hk.collaction.contentfarmblocker.ui.adapter.AppItemAdapter;

public class MainFragment extends BasePreferenceFragment {

	private Preference prefBrowser;
	private SharedPreferences settings;
	private ArrayList<AppItem> appList = new ArrayList<>();
	private MaterialDialog browserDialog;
	private boolean isShowMeow = false;
	private CheckBoxPreference prefPreviousAppDetect;

	public static MainFragment newInstance(boolean isNoBrowser) {
		MainFragment fragment = new MainFragment();
		Bundle args = new Bundle();
		args.putBoolean("no_browser", isNoBrowser);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_general);
		settings = PreferenceManager.getDefaultSharedPreferences(mContext);

		if (getArguments() != null) {
			boolean isNoBrowser = getArguments().getBoolean("no_browser");
			if (isNoBrowser) {
				loadBrowserList();
			}
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		prefBrowser = findPreference("pref_browser");
		prefBrowser.setSummary(settings.getString("pref_browser_app_name", "沒有選擇"));
		prefBrowser.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				loadBrowserList();
				return false;
			}
		});

		CheckBoxPreference prefEnable = (CheckBoxPreference) findPreference("pref_enable");
		prefEnable.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				toggleDefaultApp((boolean) newValue);
				return true;
			}
		});

		prefPreviousAppDetect = (CheckBoxPreference) findPreference("pref_previous_app_detect");

		/* Set version */
		Preference prefVersion = findPreference("pref_version");
		prefVersion.setSummary(C.getCurrentVersionName(mContext));
		prefVersion.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if (!isShowMeow) {
					preference.setSummary("");
					isShowMeow = true;
				}
				preference.setSummary(preference.getSummary() + "\uD83D\uDC31");
				return false;
			}
		});

		findPreference("pref_report").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(Intent.ACTION_SEND);

				String meta = "Android Version: " + android.os.Build.VERSION.RELEASE + "\n";
				meta += "SDK Level: " + String.valueOf(android.os.Build.VERSION.SDK_INT) + "\n";
				meta += "Version: " + C.getCurrentVersionName(mContext) + "\n";
				meta += "Brand: " + Build.BRAND + "\n";
				meta += "Model: " + Build.MODEL + "\n\n";
				meta += "我的訊息是如下\n\n";

				intent.setType("message/rfc822");
				intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"hello@collaction.hk"});
				intent.putExtra(Intent.EXTRA_SUBJECT, "內容農場檢查器回報問題");
				intent.putExtra(Intent.EXTRA_TEXT, meta);
				startActivity(intent);
				return false;
			}
		});

		findPreference("pref_rate").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Uri uri = Uri.parse("market://details?id=hk.collaction.contentfarmblocker");
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
				return false;
			}
		});

		findPreference("pref_testing").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Uri uri = Uri.parse("http://www.example-contentfarm.com");
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
				return false;
			}
		});

		findPreference("pref_report_work").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Uri uri = Uri.parse("https://docs.google.com/forms/d/e/1FAIpQLScP-XrmmYs3hP_uYw1rF2lotOFzVfTFKJN_MGQDNL27lO2Pkg/viewform");
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
				return false;
			}
		});

		findPreference("pref_share").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_SEND);
				intent.putExtra(Intent.EXTRA_TEXT, "擋下內容農場網站，你我有責任，快下載「內容農場檢查器」Android App！！" +
						"\n\nhttps://play.google.com/store/apps/details?id=hk.collaction.contentfarmblocker");
				intent.setType("text/plain");
				startActivity(Intent.createChooser(intent, "分享此程式"));
				return false;
			}
		});

		findPreference("pref_author").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Uri uri = Uri.parse("market://search?q=pub:\"Collaction 小隊\"");
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
				return false;
			}
		});

	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public void onResume() {
		super.onResume();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			prefPreviousAppDetect.setEnabled(true);

			if (C.isGrantedSystemPermission(mContext)) {
				if (settings.getBoolean("pref_previous_app_detect", false)) {
					prefPreviousAppDetect.setSummary("瀏覽器使用最後分頁開啟網址，外部程式使用瀏覽器新分頁開啟網址");
				} else {
					prefPreviousAppDetect.setSummary("一律使用瀏覽器最後分頁開啟網址");
				}
				prefPreviousAppDetect.setOnPreferenceClickListener(null);
				prefPreviousAppDetect.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue) {
						if ((boolean) newValue) {
							prefPreviousAppDetect.setSummary("瀏覽器使用最後分頁開啟網址，外部程式使用新分頁開啟網址");
						} else {
							prefPreviousAppDetect.setSummary("一律使用瀏覽器最後分頁開啟網址");
						}
						return true;
					}
				});
			} else {
				prefPreviousAppDetect.setSummary("此切換兼容需要偵測最近應用程式列表，按此手動允許本程式取得「使用記錄存取權」。");
				prefPreviousAppDetect.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
						return false;
					}
				});
				prefPreviousAppDetect.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue) {
						return false;
					}
				});
			}
		} else {
			prefPreviousAppDetect.setEnabled(false);
			prefPreviousAppDetect.setSummary("只適用於 Android 5.1 或以上");
		}
	}

	/**
	 * Get all browser apps
	 */
	private void loadBrowserList() {
		new AsyncTask<Void, Void, Void>() {
			private MaterialDialog progressDialog;

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				progressDialog = new MaterialDialog.Builder(mContext)
						.content(R.string.ui_loading)
						.progress(true, 0)
						.cancelable(false)
						.show();
				toggleDefaultApp(false);
			}

			@Override
			protected Void doInBackground(Void... voids) {
				PackageManager packageManager = mContext.getPackageManager();
				appList.clear();

				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.addCategory(Intent.CATEGORY_BROWSABLE);
				intent.setData(Uri.parse("https://www.google.com"));
				List<ResolveInfo> pkgAppsList = mContext.getPackageManager().queryIntentActivities(intent, 0);

				for (ResolveInfo info : pkgAppsList) {
					String packageName = info.activityInfo.packageName;

					/* Just skip this app */
					if (packageName.equals(mContext.getPackageName())) {
						continue;
					}

					/* Add the browser to the appList */
					try {
						ApplicationInfo applicationInfo = mContext.getPackageManager().getApplicationInfo(packageName, 0);

						AppItem appItem = new AppItem();
						appItem.setAppName(applicationInfo.loadLabel(packageManager).toString());
						appItem.setPackageName(applicationInfo.packageName);
						appItem.setIcon(applicationInfo.loadIcon(packageManager));

						appList.add(appItem);
					} catch (PackageManager.NameNotFoundException ignored) {
					}
				}

				/* Making the loading longer would make the world better */
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ignored) {
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void aVoid) {
				super.onPostExecute(aVoid);
				if (settings.getBoolean("pref_enable", true)) {
					toggleDefaultApp(true);
				}

				progressDialog.dismiss();

				AppItemAdapter appItemAdapter = new AppItemAdapter(appList, new AppItemAdapter.ItemClickListener() {
					@Override
					public void onItemDetailClick(AppItem appItem) {
						settings.edit()
								.putString("pref_browser", appItem.getPackageName())
								.putString("pref_browser_app_name", appItem.getAppName())
								.apply();
						prefBrowser.setSummary(settings.getString("pref_browser_app_name", appItem.getAppName()));
						browserDialog.dismiss();
					}
				});

				MaterialDialog.Builder browserDialogBuilder = new MaterialDialog.Builder(mContext)
						.title("請選擇預設瀏覽器")
						.adapter(appItemAdapter, null)
						.negativeText(R.string.ui_cancel);

				browserDialog = browserDialogBuilder.build();
				browserDialog.show();
			}
		}.execute();
	}

	private void toggleDefaultApp(boolean isEnable) {
		PackageManager pm = mContext.getPackageManager();
		ComponentName component = new ComponentName(mContext, DetectorActivity.class);

		if (isEnable) {
			pm.setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
		} else {
			pm.setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
		}
	}
}