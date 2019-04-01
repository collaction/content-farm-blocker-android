package hk.collaction.contentfarmblocker;

import android.content.Context;
import androidx.multidex.MultiDexApplication;

import com.blankj.utilcode.util.Utils;

public class App extends MultiDexApplication {

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		Utils.init(this);
	}

}