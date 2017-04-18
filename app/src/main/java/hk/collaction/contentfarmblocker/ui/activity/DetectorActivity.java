package hk.collaction.contentfarmblocker.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;

import hk.collaction.contentfarmblocker.C;

public class DetectorActivity extends BaseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int currentOrientation = getResources().getConfiguration().orientation;
		if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		}

		String urlString = getIntent().getDataString();
		checking(urlString);
	}

	private void checking(String urlString) {
		new AsyncTask<String, Void, String>() {
			@Override
			protected String doInBackground(String... strings) {
				String urlString = strings[0];
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);

				if (settings.getBoolean("pref_short_url_checking", true)) {
					String domain = getBaseDomain(urlString);
					if (isShortenUrl(domain)) {
						try {
							URL url = new URL(urlString);

							HttpURLConnection connection = (HttpURLConnection) url.openConnection();
							connection.setInstanceFollowRedirects(false);
							URL secondURL = new URL(connection.getHeaderField("Location"));
							urlString = secondURL.toString();
							connection.disconnect();
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
		return new HashSet<>(Arrays.asList(contentFarmDomainArray)).contains(domain);
	}

	private boolean isShortenUrl(String domain) {
		return new HashSet<>(Arrays.asList(shortenDomainArray)).contains(domain);
	}

	/**
	 * Will take a domain
	 * Parse any blogspot to blogspot.*
	 *
	 * @param urlString String
	 * @return String
	 */
	private String getBaseDomain(String urlString) {
		try {
			URL aURL = new URL(urlString);
			String domain = aURL.getHost();

			if (domain.contains(".blogspot.")) {
				domain = domain.replaceAll("(.*)(\\.blogspot\\..*)", ".$1.*");
			}

			return domain.startsWith("www.") ? domain.substring(4) : domain;
		} catch (MalformedURLException ignored) {
			return urlString;
		}
	}

	private final String[] shortenDomainArray = {
			"7.ly",
			"al.ly",
			"bit.do",
			"bit.ly",
			"goo.gl",
			"tiny.cc",
			"tr.im",
			"y2u.be"
	};

	/**
	 * https://github.com/benlau/ihatecontentfarms/blob/master/chrome/sites.js
	 */
	private final String[] contentFarmDomainArray = {
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
			"ccolorsky.blogspot.*",
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
			"trithe.com",
			"ttshow.tw",
			"tw.anyelse.com",
			"tw.jdkartsports.nl", // Whole HK
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
			"hktimes.org",
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
			"example-contentfarm.com",
			"example-contentfarm.com.hk"
	};
}
