package hk.collaction.contentfarmblocker.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewConfiguration;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

import hk.collaction.contentfarmblocker.C;

/**
 * UtilHelper Class
 * Created by Himphen on 10/1/2016.
 */
public class UtilHelper {

	public static final String PREF_IAP = "iap";
	public static final String PREF_LANGUAGE = "PREF_LANGUAGE";

	@SuppressWarnings("JavaReflectionMemberAccess")
	public static void forceShowMenu(Context mContext) {
		try {
			ViewConfiguration config = ViewConfiguration.get(mContext);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			menuKeyField.setAccessible(true);
			menuKeyField.setBoolean(config, false);
		} catch (Exception ignored) {
		}
	}

	public static String getCurrentVersionName(Context c) {
		try {
			PackageInfo pInfo = c.getPackageManager().getPackageInfo(
					c.getPackageName(), 0);
			return pInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			return "NA";
		}
	}

	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	public static String formatSignificant(double value, int significant) {
		MathContext mathContext = new MathContext(significant, RoundingMode.DOWN);
		BigDecimal bigDecimal = new BigDecimal(value, mathContext);
		return bigDecimal.toPlainString();
	}

	public static void detectLanguage(Context context) {
		SharedPreferences setting = PreferenceManager
				.getDefaultSharedPreferences(context);
		String language = setting.getString(UtilHelper.PREF_LANGUAGE, "auto");
		Resources res = context.getResources();
		Configuration conf = res.getConfiguration();
		switch (language) {
			case "en":
			case "zh":
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
					conf.setLocale(new Locale(language));
				} else {
					conf.locale = new Locale(language);
				}
				break;
			default:
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
					conf.setLocale(Resources.getSystem().getConfiguration().getLocales().get(0));
				} else {
					conf.locale = Resources.getSystem().getConfiguration().locale;
				}
		}
		DisplayMetrics dm = res.getDisplayMetrics();
		res.updateConfiguration(conf, dm);
	}

	public static void log(String message) {
		Log.d(C.TAG, message);
	}

}
