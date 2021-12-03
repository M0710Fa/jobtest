package com.example.jobtest

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.io.BufferedReader
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.Comparator

class MainActivity : AppCompatActivity() {
    private val fileName = "datafile.txt"

    companion object {
        private val TAG = "MainActivity"
        val JOB_ID_A = 100
        val JOB_ID_B = 200
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!checkForPermission()) {
            Log.i(ContentValues.TAG, "The user may not allow the access to apps usage. ")
            Toast.makeText(
                this,
                "Failed to retrieve app usage statistics. " +
                        "You may need to enable access for this app through " +
                        "Settings > Security > Apps with usage access",
                Toast.LENGTH_LONG
            ).show()
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        } else {
            // We have the permission. Query app usage stats.
            val filePath = filesDir.path + "/myText.txt"
            Log.d("Test", "path : $filePath")
        }

        val js = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val serviceComponet = ComponentName(this,MyJobService::class.java)
        val jobInfo = JobInfo.Builder(JOB_ID_A,serviceComponet)
            .setPersisted(true)
            .setPeriodic(TimeUnit.HOURS.toHours(24))
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
            .build()
        js.schedule(jobInfo)

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkForPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }
}