package com.hutchrefresh.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class RefreshReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("HutchRefresh", "Triggering Hutch app refresh...")

        val packageName = "com.omobio.etisalatone"
        val launchIntent = context?.packageManager?.getLaunchIntentForPackage(packageName)

        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            Log.d("HutchRefresh", "Hutch app launched successfully")
        } else {
            Log.e("HutchRefresh", "Hutch app not found! Is it installed?")
        }
    }
}
