package hk.collaction.contentfarmblocker.ui.receiver;

/*
  Created by Himphen on 2/9/2017.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import hk.collaction.contentfarmblocker.C;

public class BootCompleteReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		C.toggleDefaultApp(context, settings.getBoolean("pref_enable", false));
	}
}