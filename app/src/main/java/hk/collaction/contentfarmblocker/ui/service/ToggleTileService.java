package hk.collaction.contentfarmblocker.ui.service;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import hk.collaction.contentfarmblocker.C;
import hk.collaction.contentfarmblocker.R;

@TargetApi(Build.VERSION_CODES.N)
public class ToggleTileService extends TileService {
	@Override
	public void onStartListening() {
		super.onStartListening();
		Log.d("TEST", "Launch onStartListening()");

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		toggleTile(settings.getBoolean("pref_enable", false));
	}

	@Override
	public void onStopListening() {
		super.onStopListening();
		Log.d("TEST", "Launch onStopListening()");

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		toggleTile(settings.getBoolean("pref_enable", false));
	}

	@Override
	public void onClick() {
		super.onClick();
		Log.d("TEST", "Launch onClick()");

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		boolean result = !settings.getBoolean("pref_enable", false);
		toggleTile(result);
		settings.edit().putBoolean("pref_enable", result).apply();
	}

	private void toggleTile(boolean isEnable) {
		Tile tile = getQsTile();

		if (isEnable) {
			tile.setLabel(getString(R.string.app_name));
			tile.setContentDescription(getString(R.string.ui_enable));
			tile.setState(Tile.STATE_ACTIVE);
			tile.updateTile();
			Log.d("TEST", "toggleTile true");
		} else {
			tile.setLabel(getString(R.string.app_name));
			tile.setContentDescription(getString(R.string.ui_disable));
			tile.setState(Tile.STATE_INACTIVE);
			tile.updateTile();
			Log.d("TEST", "toggleTile false");
		}

		C.toggleDefaultApp(this, isEnable);
	}
}
