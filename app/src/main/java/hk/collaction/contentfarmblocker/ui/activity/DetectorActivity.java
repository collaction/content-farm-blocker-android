package hk.collaction.contentfarmblocker.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import hk.collaction.contentfarmblocker.C;
import hk.collaction.contentfarmblocker.R;

public class DetectorActivity extends BaseActivity {

	private SharedPreferences settings;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		settings = PreferenceManager.getDefaultSharedPreferences(mContext);

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

				if (settings.getBoolean("pref_short_url_checking", true)) {
					String domain = getBaseDomain(urlString);
					if (isShortenUrl(domain)) {
						Handler handler = new Handler(mContext.getMainLooper());
						handler.post(new Runnable() {
							public void run() {
								Toast.makeText(mContext, R.string.toast_redirecting, Toast.LENGTH_LONG).show();
							}
						});

						urlString = getRedirectUrl(urlString);
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
		String[] whitelistArray = settings.getString("pref_whitelist", "").split("\\r\\n|\\n|\\r");
		String[] blacklistArray = settings.getString("pref_blacklist", "").split("\\r\\n|\\n|\\r");

		HashSet<String> defaultSet = new HashSet<>(Arrays.asList(contentFarmDomainArray));

		if (blacklistArray.length > 0) {
			defaultSet.addAll(Arrays.asList(blacklistArray));
		}

		if (whitelistArray.length > 0) {
			defaultSet.removeAll(Arrays.asList(whitelistArray));
		}

		return defaultSet.contains(domain);
	}

	private boolean isShortenUrl(String domain) {
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
	private String getBaseDomain(String urlString) {
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

	private final String[] shortenDomainArray = {
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

	/**
	 * https://github.com/benlau/ihatecontentfarms/blob/master/chrome/sites.js
	 * https://danny0838.github.io/content-farm-terminator/files/blocklist/content-farms.txt
	 */
	private final String[] contentFarmDomainArray = {
			// For testing
			"example.com",
			// ihatecontentfarms
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
			// content-farm-terminator
			"15jb.net",
			"168ptt.com",
			"168ptt.tk",
			"168ptt.xyz",
			"2936366.com",
			"4000000958.com",
			"51manager.net",
			"52114.org",
			"7624.net",
			"94ptt.com",
			"98watch.com",
			"aboluowang.com",
			"about.com",
			"allexperts.com",
			"amassly.com",
			"americantowns.com",
			"answerbag.com",
			"answers.com",
			"anytime999.blogspot.*",
			"aol.com",
			"articlesbase.com",
			"ask.com",
			"associatedcontent.com",
			"assuer.com",
			"bangdezs.com",
			"baoxiaovideo.tv",
			"bbstoplist.blogspot.*",
			"bc3ts.com",
			"bcwebworks.com",
			"beep01.com",
			"beforeout.com",
			"beyondnewsnet.com",
			"bhlvhuagai.com",
			"bigshuang.com",
			"bizrate.com",
			"bottle01.com",
			"brothersoft.com",
			"business.com",
			"buzzfeed.com",
			"buzzle.com",
			"buzznewshk.com",
			"bytes.com",
			"cacanews.com",
			"caretify.com",
			"catha.life",
			"ccolorsky.blogspot.com",
			"chacha.com",
			"chen-ton.com",
			"chsjtl.com",
			"chuansong.me",
			"cnread.news",
			"coachinga01.com",
			"coco01.findx2.net",
			"commreader.com",
			"cooco.pixnet.net",
			"cooktin.com",
			"coolfity.com",
			"coowx.com",
			"daliulian.*.com",
			"daliulian.*.net",
			"daliulian.com",
			"datansuo.net",
			"datougou.cn",
			"ddnews.me",
			"decibo.com",
			"dgboligang.com",
			"doggyhouse.idv.tw",
			"dqlujin.com",
			"easylife.tw",
			"efreedom.com",
			"ehow.com",
			"ellell.cc",
			"entrepreneur.com",
			"essortment.com",
			"examiner.com",
			"experts-exchange.com",
			"expertvillage.com",
			"ez3c.tw",
			"ezinearticles.com",
			"ezp8.com",
			"ezp8.com.tw",
			"fafaup.com",
			"fanpiece.com",
			"fbs.one",
			"findarticles.com",
			"fixya.com",
			"flxdaily.com",
			"formomo.com",
			"fun-vdo.com",
			"fun.key8.com",
			"funny199.com",
			"funptt.com",
			"funstory.org",
			"gegugu.com",
			"giftflower.com",
			"gjczz.com",
			"gmter.com",
			"go1p.com",
			"goez1.com",
			"gohong01.com",
			"golflink.com",
			"goodlifeyou.bumbnews.com",
			"gooread.com",
			"greatdailytw.com",
			"guestspres.com",
			"gyfunnews.com",
			"happyfoo.cc",
			"happytify.cc",
			"have8.com",
			"hbzrhf.com",
			"healthnews99.com",
			"helium.com",
			"hirobocup.com",
			"historymore.com",
			"hk.maheshbhusal.com.np",
			"hlcydb.com",
			"hotlah.com",
			"how321.com",
			"howtodothings.com",
			"hrbgzw.com",
			"hubpages.com",
			"ifunny.tw",
			"ihealth3.com",
			"imtopsales.com",
			"info.jinlisting.com",
			"infobarrel.com",
			"iranshao.com",
			"iread.one",
			"ishare777.blogspot.*",
			"japare.com",
			"jiangkang.host",
			"jinianlai.com",
			"juhangye.com",
			"kknews-hk.net",
			"kknewshk.info",
			"kmg360.com",
			"kyjh.funnytw.com",
			"letptt.com",
			"lieqi.com",
			"lieqinews.com",
			"like3c.com",
			"linjiamm.com",
			"lisa513889.pixnet.net",
			"livestrong.com",
			"lookforward.cc",
			"loser.news",
			"lovelifes.net",
			"lovetoknow.com",
			"mahalo.com",
			"mail-archive.com",
			"make9.com",
			"mamaknews.com",
			"mamatify.com",
			"mania.com",
			"manta.com",
			"media8.info",
			"media8.news",
			"metalballs.co",
			"micpost.com",
			"mini.eastday.com",
			"missu.co.com",
			"mofun.ml",
			"mogujiela.com",
			"momdata.blogspot.com",
			"mooner.orgs.one",
			"moretify.com",
			"myfunnews.com",
			"myptt.tk",
			"myytaoli.blogspot.*",
			"njyfw.cn",
			"npnt.com.tw",
			"oacte.cn",
			"oquoy.cn",
			"oqvcx.cn",
			"orgs.one",
			"orzhd.com",
			"petonea.com",
			"pixiu88.net",
			"pixpo.net",
			"please.news",
			"pluck.com",
			"poad.net",
			"posttw.com",
			"ppap01.com",
			"psychology-spot.com",
			"ptt.social",
			"pttbbs.cc",
			"pttbbs.com.tw",
			"pttbee.com",
			"pttdaily.com",
			"pttdata.com",
			"pttgame.com",
			"pttgossip.com",
			"pttlocal.com",
			"pttman.com",
			"pttread.com",
			"pttreader.com",
			"pttview.com",
			"pttview.tk",
			"pttweb.com",
			"pttweb.tw",
			"pushme.news",
			"pushnews.net",
			"qinggua.net",
			"qqnews2u.com",
			"questionhub.com",
			"rkkdg.com",
			"rtysk.com",
			"saydigi.com",
			"saydigitech.pixnet.net",
			"seed.com",
			"sis8.com",
			"smil3y.co",
			"sos.tw",
			"soul99.com",
			"soulbay.tw",
			"soulmate888.blogspot.*",
			"squidoo.com",
			"stamp3.com",
			"suite101.com",
			"szfusen.com",
			"t6tt.com",
			"taiwanfansclub.com",
			"taogelist.com",
			"teepr.net",
			"teepr.tv",
			"thesharedaily.com",
			"tjfer.com",
			"tjlonghui.com",
			"toutiao.com",
			"towngag.com.hk",
			"trytohear.com",
			"tw.ptt01.cc",
			"tw.ucptt.com",
			"tw520.me",
			"twenga.com",
			"ucptt.com",
			"uuread.cc",
			"viralcham.com",
			"w.ebptt.com",
			"webptt.com",
			"webptt.com.tw",
			"wechat.kanfb.com",
			"wisegeek.com",
			"womenclub.co",
			"wonderhowto.com",
			"wp.news365.my",
			"wtsfamen.com",
			"wx.abbao.cn",
			"xazxad.com",
			"xhbxgg.com",
			"xiqiangnm.com",
			"xjzdu.cn",
			"xomba.com",
			"yanjiaoluntan.com",
			"yc.cn",
			"yeslib.com",
			"yourdictionary.com",
			"z9x9.com",
			"zengtoo.com",
			"zhizhejie.com",
			"zixundingzhi.com",
			"zonepeer.com",
			"zuopy.com",
	};

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
