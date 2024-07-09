package com.example.doan

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Looper
import android.util.Log
import android.widget.Toast


class UninstallIntentReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val packageNames = intent.getStringArrayExtra("android.intent.extra.PACKAGES")

        if(packageNames != null) {
            for(packageName in packageNames) {
                Log.d("UninstallIntentReceiver", "Package Name $packageName")
                if(packageName != null && packageName.equals(context.packageName)) {
                    // User has selected our application under the Manage Apps settings
                    // now initiating background thread to watch for activity
                    ListenActivities(context).start();
                }
            }
        }

    }
}

internal class ListenActivities(con: Context?) : Thread() {
    private var exit: Boolean = false
    private var am: ActivityManager? = null
    var context: Context? = null

    init {
        context = con
        am = context!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    }

    override fun run() {
        Looper.prepare()

        while (!exit) {
            // get the info from the currently running task

            val taskInfo = am!!.getRunningTasks(MAX_PRIORITY)

            val activityName = taskInfo[0].topActivity!!.className


            Log.d(
                "topActivity", "CURRENT Activity ::"
                        + activityName
            )

            if (activityName == "com.android.packageinstaller.UninstallerActivity") {
                // User has clicked on the Uninstall button under the Manage Apps settings

                //do whatever pre-uninstallation task you want to perform here
                // show dialogue or start another activity or database operations etc..etc..

                // context.startActivity(new Intent(context, MyPreUninstallationMsgActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

                exit = true
                Toast.makeText(
                    context,
                    "Done with preuninstallation tasks... Exiting Now",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (activityName == "com.android.settings.ManageApplications") {
                // back button was pressed and the user has been taken back to Manage Applications window
                // we should close the activity monitoring now
                exit = true
            }
        }
        Looper.loop()
    }
}