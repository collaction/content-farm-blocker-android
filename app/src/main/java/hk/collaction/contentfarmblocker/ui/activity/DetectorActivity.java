package hk.collaction.contentfarmblocker.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import hk.collaction.contentfarmblocker.C;
import hk.collaction.contentfarmblocker.R;

public class DetectorActivity extends BaseActivity {

	@SuppressLint("StaticFieldLeak")
	private class CheckingAsyncTask extends AsyncTask<String, Void, String> {

		private Activity mContext;
		private SharedPreferences settings;

		CheckingAsyncTask(Activity mContext, SharedPreferences settings) {
			this.mContext = mContext;
			this.settings = settings;
		}

		@Override
		protected String doInBackground(String... strings) {
			String urlString = strings[0];

			if (settings.getBoolean("pref_short_url_checking", true)) {
				String domain = getBaseDomain(urlString);
				if (isShortenUrl(domain)) {
					if (mContext != null) {
						Handler handler = new Handler(mContext.getMainLooper());
						handler.post(new Runnable() {
							public void run() {
								Toast.makeText(mContext, R.string.toast_redirecting, Toast.LENGTH_LONG).show();
							}
						});
					}

					urlString = getRedirectUrl(urlString);
				}
			}

			return urlString;
		}

		@Override
		protected void onPostExecute(String urlString) {
			if (mContext == null) {
				return;
			}

			String domain = getBaseDomain(urlString);
			if (isContentFarm(domain, settings)) {
				Intent intent = new Intent().setClass(mContext, BlockerActivity.class);
				intent.putExtra("url", urlString);
				intent.putExtra("domain", domain);

				mContext.startActivity(intent);
				mContext.finish();
			} else {
				C.goToUrl(mContext, urlString);
			}
		}

		private boolean isContentFarm(String domain, SharedPreferences settings) {
			domain = domain.toLowerCase();

			String[] whitelistArray = settings.getString("pref_whitelist", "").split("\\n");
			String[] blacklistArray = settings.getString("pref_blacklist", "").split("\\n");

			InputStream inputStream = mContext.getResources().openRawResource(R.raw.site);
			BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));

			HashSet<String> defaultSet = new HashSet<>();
			try {
				String line;
				while ((line = r.readLine()) != null) {
					defaultSet.add(line);
				}
			} catch (Exception ignored) {
			}

			if (blacklistArray.length > 0) {
				defaultSet.addAll(Arrays.asList(blacklistArray));
			}

			if (whitelistArray.length > 0) {
				defaultSet.removeAll(Arrays.asList(whitelistArray));
			}

			return defaultSet.contains(domain);
		}

		private String getRedirectUrl(String urlString) {
			String domain = getBaseDomain(urlString);
			if (isShortenUrl(domain)) {
				try {
					URL url = new URL(urlString);
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setInstanceFollowRedirects(false);

					String redirectUrl = connection.getHeaderField("Location");
					if (redirectUrl == null) {
						List<String> entrySet = connection.getHeaderFields().get("Refresh");
						if (entrySet != null) {
							for (String refreshUrl : entrySet) {
								redirectUrl = refreshUrl.replace("1;URL=", "");
							}
						}
					}

					if (redirectUrl != null) {
						urlString = getRedirectUrl(redirectUrl);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			return urlString;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);

		String urlString = getIntent().getDataString();
		new CheckingAsyncTask(mContext, settings).execute(urlString);
	}

	private static boolean isShortenUrl(String domain) {
		return new HashSet<>(Arrays.asList(shortenDomainArray)).contains(domain);
	}

	/**
	 * Will get a domain
	 * - Parse any blogspot to blogspot.*
	 * - Remove www.
	 *
	 * @param urlString String
	 * @return String
	 */
	private static String getBaseDomain(String urlString) {
		try {
			URL aURL = new URL(urlString);
			String domain = aURL.getHost();

			// Parse any blogspot to blogspot.*
			if (domain.contains(".blogspot.")) {
				domain = domain.replaceAll("(.*)(\\.blogspot\\..*)", ".$1.*");
			}

			// Remove www.
			return domain.startsWith("www.") ? domain.substring(4) : domain;
		} catch (MalformedURLException ignored) {
			return urlString;
		}
	}

	private final static String[] shortenDomainArray = {
			"7.ly",
			"al.ly",
			"bit.do",
			"bit.ly",
			"goo.gl",
			"tiny.cc",
			"tr.im",
			"y2u.be",
			"lm.facebook.com"
	};
}
