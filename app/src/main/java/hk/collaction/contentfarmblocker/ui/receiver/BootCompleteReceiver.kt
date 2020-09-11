package hk.collaction.contentfarmblocker.ui.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import hk.collaction.contentfarmblocker.util.UtilHelper.toggleDefaultApp

/*
  Created by Himphen on 2/9/2017.
 */
class BootCompleteReceiver : BroadcastReceiver() {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        val settings = PreferenceManager.getDefaultSharedPreferences(context)
        toggleDefaultApp(context, settings.getBoolean("pref_enable", false))
    }
}