package hk.collaction.contentfarmdetector.model;

import android.graphics.drawable.Drawable;

/**
 * Created by himphen on 24/5/16.
 */

public class AppItem {
	private String appName;
	private String packageName;
	private Drawable icon;

	public AppItem() {
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public Drawable getIcon() {
		return icon;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

}