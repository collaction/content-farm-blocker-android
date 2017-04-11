package hk.collaction.contentfarmblocker.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import hk.collaction.contentfarmblocker.C;

public class DetectorActivity extends BaseActivity {

	private SharedPreferences settings;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		settings = android.preference.PreferenceManager.getDefaultSharedPreferences(mContext);

		String urlString = getIntent().getDataString();
		checking(urlString);
	}

	private void checking(String urlString) {
		new AsyncTask<String, Void, String>() {
			@Override
			protected String doInBackground(String... strings) {
				String urlString = strings[0];

				if (settings.getBoolean("pref_short_url_checking", true)) {
					String domain = getBaseDomain(urlString);
					if (isShortenUrl(domain)) {
						try {
							URL url = new URL(urlString);

							HttpURLConnection connection = (HttpURLConnection) url.openConnection();
							connection.connect();
							connection.getInputStream();
							return connection.getURL().toString();
						} catch (IOException ignored) {
						}
					}
				}

				return urlString;
			}

			@Override
			protected void onPostExecute(String urlString) {
				String domain = getBaseDomain(urlString);
				if (isContentFarm(domain)) {
					Intent intent = new Intent().setClass(mContext, BlockerActivity.class);
					intent.putExtra("url", urlString);
					intent.putExtra("domain", domain);

					startActivity(intent);
					mContext.finish();
				} else {
					C.goToUrl(mContext, urlString);
				}
			}
		}.execute(urlString);
	}

	private boolean isContentFarm(String domain) {
		List<String> siteList = Arrays.asList(contentFarmDomainArray);
		return siteList.contains(domain);
	}

	private boolean isShortenUrl(String domain) {
		List<String> siteList = Arrays.asList(shortenDomainArray);
		return siteList.contains(domain);
	}


	/**
	 * Will take a url to host, such as http://www.stackoverflow.com and return www.stackoverflow.com
	 *
	 * @param url String
	 * @return String
	 */
	private String getHost(String url) {
		if (url == null || url.length() == 0)
			return "";

		int doubleSlash = url.indexOf("//");
		if (doubleSlash == -1)
			doubleSlash = 0;
		else
			doubleSlash += 2;

		int end = url.indexOf('/', doubleSlash);
		end = end >= 0 ? end : url.length();

		int port = url.indexOf(':', doubleSlash);
		end = (port > 0 && port < end) ? port : end;

		return url.substring(doubleSlash, end);
	}

	/**
	 * Will take a host to domain, such as www.stackoverflow.com and return stackoverflow.com
	 *
	 * @param url String
	 * @return String
	 */
	private String getBaseDomain(String url) {
		String host = getHost(url);

		int startIndex = 0;
		int nextIndex = host.indexOf('.');
		int lastIndex = host.lastIndexOf('.');
		while (nextIndex < lastIndex) {
			startIndex = nextIndex + 1;
			nextIndex = host.indexOf('.', startIndex);
		}
		if (startIndex > 0) {
			return host.substring(startIndex);
		} else {
			return host;
		}
	}

	private String[] shortenDomainArray = {
			"goo.gl",
			"bit.ly"
	};

	private String[] contentFarmDomainArray = {
			"163nvren.com",
			"360doc.com",
			"7jiu.com.hk",
			"a-gui.com",
			"aboutfighter.com",
			"apple01.net",
			"appnews.fanswong.com",
			"asia01.club",
			"axn2000.blogspot.*",
			"babymaycry.com",
			"baiyongqin.cc",
			"bananadaily.net",
			"beauties.life",
			"beefun01.com",
			"biginfo4u.com",
			"bignews01.com",
			"bomb01.com",
			"bottle01.tw",
			"bubuko.com",
			"bucktop.com",
			"bunnygo.net",
			"buzz01.com",
			"buzzhand.com",
			"buzzhand.net",
			"buzzjoker.com",
			"buzzlife.com.tw",
			"buzznews.news",
			"ccolorsky.blogspot.com",
			"changepw.com",
			"chaxf.com",
			"chunew.com",
			"cibeiwen.com",
			"circle01.com",
			"classicofloves.com",
			"clickme.net",
			"cmoney.tw",
			"coco01.net",
			"cocomy.net",
			"cocotw.net",
			"contw.com",
			"dailyfun.cc",
			"dailyhearter.net",
			"daleba.net",
			"daliulian.net",
			"dayhot.news",
			"dayspot.net",
			"discoss.com",
			"dnbcw.info",
			"dongqiuxiang.net",
			"dsy39.com",
			"eazon.com",
			"enews.com.tw",
			"eryunews.com",
			"ezgoe.com",
			"eznewlife.com",
			"ezp9.com",
			"ezvivi.com",
			"ezvivi2.com",
			"f.duckhk.com",
			"faminereports.blogspot.*",
			"fanli7.net",
			"friends.hk",
			"fullyu.com",
			"fun.youngboysgirls.com",
			"fun01.cc",
			"fun01.net",
			"funnies.online",
			"funssy.com",
			"funvdo01.com",
			"getez.info",
			"getfunfun.com",
			"getjoyz.com",
			"gigacircle.com",
			"gigcasa.com",
			"gjoyz.com",
			"guowenme.cc",
			"guudo.cn",
			"handread.cc",
			"haolookr.com",
			"happies.news",
			"happiness.beauties.life",
			"happyeverydaymovie.com",
			"healthalover.com",
			"hehuancui.com", // buzz01
			"hk.maheshbhusal.com.np",
			"hkwall.com",
			"honey99.net",
			"hothk.com",
			"hotnews.hk",
			"hottimes.org",
			"housekook.com",
			"how01.com",
			"howfunny.org",
			"hssszn.com",
			"icovideos.com",
			"ideapit.com",
			"ifuun.com",
			"ihot.news",
			"ilife97.com",
			"ilife99.com",
			"immediates.net",
			"ipetgroup.com",
			"ireaded.com",
			"ispot.news",
			"izhentoo.cc",
			"jimmyfans.com",
			"juksy.com",
			"justfenxiang.net",
			"justhotnews.site",
			"kan.world",
			"kikinote.net",
			"kknews.cc",
			"kuso01.tv",
			"laughbombclub.com",
			"letu.life",
			"life.com.tw",
			"life.cx", // This domain will redirect to life.tw
			"life.tw",
			"likea.ezvivi.com",
			"line-share.tw",
			"mama.tw",
			"mamicode.com",
			"mango01.com",
			"maoanbo.net",
			"media8.me",
			"medialnk.com",
			"menclub.co",
			"metalballs.com",
			"mili010.com",
			"mili010.net",
			"mimi186.com",
			"ml.design-fabrica.com", // Whole HK
			"ml.yubhar.com",
			"money83.com",
			"moneyaaa.com",
			"muratify.cc",
			"myfbshare.net",
			"mytimes.org",
			"news.95lady.com",
			"news.knowing.asia",
			"news.qzapp.net",
			"news01.cc",
			"newstube01.tv",
			"novelfeed.com",
			"ohwonews.com",
			"onefunnyjoke.com",
			"onnewlife.com",
			"orange01.org",
			"pcasg.com",
			"peopleinsider.blogspot.*",
			"peopleinsider.net",
			"picallies.com",
			"play01.cc",
			"plays01.com",
			"post01.com",
			"programgo.com",
			"ptt01.cc",
			"pttbook.com",
			"push01.com",
			"push01.net",
			"qianqu.cc",
			"qilook.com",
			"qiqu.news",
			"quiz321.com",
			"qzapp.net",
			"read01.com",
			"reg.youthwant.com.tw",
			"share.youthwant.com.tw",
			"share001.com",
			"share001.net",
			"share2fb.net",
			"shareba.com",
			"shareonion.com",
			"sharetify.com",
			"sk2zone.com",
			"spicemami.com",
			"story.bazzfly.com",
			"superfun-e.com",
			"teepr.com",
			"thefundaily.com",
			"thegreatdaily.com",
			"thegreendaily.net",
			"thehealthdaily.org",
			"tipelse.com",
			"toments.com",
			"ttshow.tw",
			"tw.anyelse.com",
			"tw.jdkartsports.nl/", // Whole HK
			"twgreatdaily.com",
			"vdoobv.com",
			"video-lab.net", //Whole HK,
			"viralane.com",
			"watchinese.com",
			"whatfunny.org",
			"wholehk.com",
			"whyhow.online",
			"womanaaa.com",
			"wonder4.co",
			"ww.happy123.org",
			"ww.share001.org",
			"xianso.com",
			"xibao.tw",
			"xuxianghui.cc",
			"xuxinfang.xyz",
			"yibihan.net",
			"ymiit.com",
			"yourbabb.com",
			"yourfacts.club", // FB Share trap
			"yourhope.info",
			"youthwant.com",
			"youthwant.com.tw",
			"youthwant.ufc.com.tw",
			"youthwant.xnnow.com",
			"zhentoo.com",
			"zhentoo.net",
			"zhoucuimei.cc",
			"zhulinlin.net",
			"8im.me",
			"8md.me",
			"buzzbooklet.com",
			"dungwa.com",
			"gjoyz.co",
			"hkappleweekly.com",
			"hktimes.org/",
			"imama.tw",
			"interestingpo.com",
			"meishuile.com",
			"nowlooker.com",
			"ourstarsky.com",
			"popdaily.com.tw",
			"post01.net",
			"readhouse.net",
			"thehealther.com",
			"topnews8.com",
			"twtimes.org",
			"example-contentfarm.com"
	};
}
