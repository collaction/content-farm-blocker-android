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
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import hk.collaction.contentfarmblocker.BuildConfig;
import hk.collaction.contentfarmblocker.C;
import hk.collaction.contentfarmblocker.R;
import hk.collaction.contentfarmblocker.model.AppItem;
import hk.collaction.contentfarmblocker.ui.activity.DetectorActivity;
import hk.collaction.contentfarmblocker.ui.activity.MainActivity;
import hk.collaction.contentfarmblocker.ui.adapter.AppItemAdapter;

public class MainFragment extends BasePreferenceFragment {

	private Preference prefBrowser;
	private SharedPreferences settings;
	private ArrayList<AppItem> appList = new ArrayList<>();
	private MaterialDialog browserDialog;
	private boolean isShowMeow = false;
	private CheckBoxPreference prefPreviousAppDetect;
	private BillingProcessor billingProcessor;
	private Preference prefDonate;

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

		billingProcessor = new BillingProcessor(mContext, BuildConfig.GOOGLE_IAP_KEY,
				new BillingProcessor.IBillingHandler() {
					@Override
					public void onProductPurchased(String productId, TransactionDetails details) {
						if (productId.equals(C.IAP_PID_10)
								|| productId.equals(C.IAP_PID_20)
								|| productId.equals(C.IAP_PID_50)) {
							settings.edit().putBoolean(C.PREF_IAP, true).apply();
							purchased();
							showDonateDialog();
						}
					}

					@Override
					public void onPurchaseHistoryRestored() {
						if (billingProcessor.isPurchased(C.IAP_PID_10)
								|| billingProcessor.isPurchased(C.IAP_PID_20)
								|| billingProcessor.isPurchased(C.IAP_PID_50)) {
							settings.edit().putBoolean(C.PREF_IAP, true).apply();
							purchased();
						}
					}

					@Override
					public void onBillingError(int errorCode, Throwable error) {
					}

					@Override
					public void onBillingInitialized() {
					}
				});
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		prefBrowser = findPreference("pref_browser");
		prefBrowser.setSummary(settings.getString("pref_browser_app_name", getString(R.string.pref_no_brwoser)));
		prefBrowser.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				loadBrowserList();
				return false;
			}
		});

		prefDonate = findPreference("pref_donate");
		prefDonate.setSummary(getDonateSummary());
		if (C.isPurchased(settings)) {
			purchased();
		} else {
			prefDonate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					MaterialDialog.Builder dialog = new MaterialDialog.Builder(mContext)
							.title(R.string.action_language)
							.items(R.array.language_choose)
							.itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
								@Override
								public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
									switch (which) {
										case 0:
											checkPayment(C.IAP_PID_10);
											break;
										case 1:
											checkPayment(C.IAP_PID_20);
											break;
										case 2:
											checkPayment(C.IAP_PID_50);
											break;
									}
									startActivity(new Intent(mContext, MainActivity.class));
									mContext.finish();
									return false;
								}
							})
							.negativeText(R.string.ui_cancel);
					dialog.show();

					return false;
				}
			});
		}

		findPreference("pref_reset").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				clearAllBrowsersDefaultAction();
				return false;
			}
		});

		findPreference("pref_whitelist").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				String whitelist = settings.getString("pref_whitelist", "");

				View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_custom_list, null);
				EditText inputEt = (EditText) view.findViewById(R.id.inputEt);
				inputEt.setText(whitelist);

				MaterialDialog.Builder dialog = new MaterialDialog.Builder(mContext)
						.title(preference.getTitle())
						.customView(view, true)
						.cancelable(false)
						.negativeText(R.string.ui_cancel)
						.positiveText(R.string.ui_okay)
						.onPositive(new MaterialDialog.SingleButtonCallback() {
							@Override
							public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
								EditText inputEt = (EditText) dialog.getView().findViewById(R.id.inputEt);
								String result = inputEt.getText().toString().trim();
								settings.edit().putString("pref_whitelist", result).apply();
							}
						});
				dialog.show();

				return false;
			}
		});

		findPreference("pref_blacklist").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				String blacklist = settings.getString("pref_blacklist", "");

				View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_custom_list, null);
				EditText inputEt = (EditText) view.findViewById(R.id.inputEt);
				inputEt.setText(blacklist);

				MaterialDialog.Builder dialog = new MaterialDialog.Builder(mContext)
						.title(preference.getTitle())
						.customView(view, true)
						.cancelable(false)
						.negativeText(R.string.ui_cancel)
						.positiveText(R.string.ui_okay)
						.onPositive(new MaterialDialog.SingleButtonCallback() {
							@Override
							public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
								EditText inputEt = (EditText) dialog.getView().findViewById(R.id.inputEt);
								String result = inputEt.getText().toString().trim();
								settings.edit().putString("pref_blacklist", result).apply();
							}
						});
				dialog.show();

				return false;
			}
		});

		findPreference("pref_language").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				String language = settings.getString(C.PREF_LANGUAGE, "auto");
				int a = 0;
				switch (language) {
					case "auto":
						a = 0;
						break;
					case "en":
						a = 1;
						break;
					case "zh":
						a = 2;
						break;
				}

				MaterialDialog.Builder dialog = new MaterialDialog.Builder(mContext)
						.title(R.string.action_language)
						.items(R.array.language_choose)
						.itemsCallbackSingleChoice(a, new MaterialDialog.ListCallbackSingleChoice() {
							@Override
							public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
								switch (which) {
									case 0:
										settings.edit().putString(C.PREF_LANGUAGE, "auto").apply();
										break;
									case 1:
										settings.edit().putString(C.PREF_LANGUAGE, "en").apply();
										break;
									case 2:
										settings.edit().putString(C.PREF_LANGUAGE, "zh").apply();
										break;
								}
								startActivity(new Intent(mContext, MainActivity.class));
								mContext.finish();
								return false;
							}
						})
						.negativeText(R.string.ui_cancel);
				dialog.show();

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

				intent.setType("message/rfc822");
				intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"hello@collaction.hk"});
				intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.pref_report_title));
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
				Uri uri = Uri.parse("http://www.example.com");
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
				intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.pref_share_desc) +
						"https://play.google.com/store/apps/details?id=hk.collaction.contentfarmblocker");
				intent.setType("text/plain");
				startActivity(Intent.createChooser(intent, getString(R.string.ui_share)));
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

		findPreference("pref_collaction").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Uri uri = Uri.parse("https://www.collaction.hk/s/collactionopensource");
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
					prefPreviousAppDetect.setSummary(getString(R.string.pref_previous_app_summary_on));
				} else {
					prefPreviousAppDetect.setSummary(getString(R.string.pref_previous_app_summary_off));
				}
				prefPreviousAppDetect.setOnPreferenceClickListener(null);
				prefPreviousAppDetect.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue) {
						if ((boolean) newValue) {
							prefPreviousAppDetect.setSummary(getString(R.string.pref_previous_app_summary_on));
						} else {
							prefPreviousAppDetect.setSummary(getString(R.string.pref_previous_app_summary_off));
						}
						return true;
					}
				});
			} else {
				prefPreviousAppDetect.setChecked(false);
				prefPreviousAppDetect.setSummary(getString(R.string.pref_previous_app_permission));
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
			prefPreviousAppDetect.setChecked(false);
			prefPreviousAppDetect.setEnabled(false);
			prefPreviousAppDetect.setSummary(getString(R.string.pref_previous_app_not_support));
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (billingProcessor != null)
			billingProcessor.release();
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
				intent.setData(Uri.parse("https://www.google.com"));
				List<ResolveInfo> pkgAppsList = packageManager.queryIntentActivities(intent,
						0);

				for (ResolveInfo info : pkgAppsList) {
					String packageName = info.activityInfo.packageName;

					/* Just skip this app */
					if (packageName.equals(mContext.getPackageName())) {
						continue;
					}

					/* Add the browser to the appList */
					try {
						ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);

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
					Thread.sleep(500);
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
						.title(R.string.pref_browser_title)
						.adapter(appItemAdapter, null)
						.negativeText(R.string.ui_cancel);

				browserDialog = browserDialogBuilder.build();
				browserDialog.show();
			}
		}.execute();
	}


	/**
	 * Get all browser apps
	 */
	private void clearAllBrowsersDefaultAction() {
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
				intent.setData(Uri.parse("https://www.google.com"));
				List<ResolveInfo> pkgAppsList = packageManager.queryIntentActivities(intent,
						PackageManager.MATCH_DEFAULT_ONLY);

				for (ResolveInfo info : pkgAppsList) {
//					String packageName = info.activityInfo.packageName;
//					packageManager.clearPackagePreferredActivities(packageName);
				}

				/* Making the loading longer would make the world better */
				try {
					Thread.sleep(500);
				} catch (InterruptedException ignored) {
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void aVoid) {
				super.onPostExecute(aVoid);
				progressDialog.dismiss();
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

	public void checkPayment(String productId) {
		boolean isAvailable = BillingProcessor.isIabServiceAvailable(mContext);
		if (isAvailable) {
			billingProcessor.purchase(mContext, productId);
		} else {
			Toast.makeText(mContext, R.string.ui_error, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (!billingProcessor.handleActivityResult(requestCode, resultCode, data)) {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void purchased() {
		prefDonate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				showDonateDialog();
				return false;
			}
		});
	}

	private void showDonateDialog() {
		MaterialDialog.Builder dialog = new MaterialDialog.Builder(mContext)
				.customView(R.layout.dialog_donate, true)
				.positiveText(R.string.ui_okay);
		dialog.show();
	}

	private String getDonateSummary() {
		String[] array = getResources().getStringArray(R.array.donate_summary);
		int rnd = new Random().nextInt(array.length);
		return array[rnd];
	}
}