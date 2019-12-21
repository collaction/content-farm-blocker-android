package hk.collaction.contentfarmblocker;

import android.app.Application;
import android.content.Context;

import com.blankj.utilcode.util.Utils;

public class App extends Application {

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		Utils.init(this);
	}

}