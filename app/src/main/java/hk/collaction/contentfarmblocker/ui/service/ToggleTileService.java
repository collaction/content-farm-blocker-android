package hk.collaction.contentfarmblocker.ui.service;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
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

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		toggleTile(settings.getBoolean("pref_enable", false));
	}

	@Override
	public void onClick() {
		super.onClick();

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		boolean currentEnabled = settings.getBoolean("pref_enable", false);
		boolean targetEnabled = !currentEnabled;
		toggleTile(targetEnabled);

		settings.edit().putBoolean("pref_enable", targetEnabled).apply();
		C.toggleDefaultApp(this, targetEnabled);
	}

	private void toggleTile(boolean enable) {
		Tile tile = getQsTile();

		if (enable) {
			tile.setLabel(getString(R.string.ui_enable));
			tile.setState(Tile.STATE_ACTIVE);
			tile.setIcon(Icon.createWithResource(this, R.drawable.qs_locked));
			tile.updateTile();
		} else {
			tile.setLabel(getString(R.string.ui_disable));
			tile.setState(Tile.STATE_INACTIVE);
			tile.setIcon(Icon.createWithResource(this, R.drawable.qs_unlocked));
			tile.updateTile();
		}
	}


}
