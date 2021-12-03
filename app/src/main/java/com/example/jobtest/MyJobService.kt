package com.example.jobtest

import android.app.job.JobParameters
import android.app.job.JobService
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.ContentValues
import android.content.Context
import android.icu.util.Calendar
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class MyJobService : JobService() {
    private val fileName = "datafile.txt"

    companion object {
        private val TAG = "MyJobService"
    }

    override fun onStartJob(params: JobParameters): Boolean {
        Log.d(TAG,"ジョブスタート: ${params.jobId}，日時${Date(System.currentTimeMillis())}")
        val mythred = thread {
            Log.d(TAG,"threadtest")
            val usage = getAppUsageStats()
            showAppUsageStats(usage)
        }
        if(!mythred.isAlive) {
            mythred.start()
        }
        return false
    }

    override fun onStopJob(params: JobParameters): Boolean {
        Log.d(TAG,"ジョブストップ: ${params.jobId}")
        return false
    }

    //for get UsageStats
    private fun getAppUsageStats(): MutableList<UsageStats> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, -1)
        // 取得する時間

        // queryUsageStats(取得する時間の単位, 取得する時間の始まり、取得する時間の終わり)
        // 取得する時間の単位 : 日単位(INTERVAL_DAILY)、週単位(INTERVAL_WEEKLY)、月単位(INTERVAL_MONTHLY)、
        //                    年単位(INTERVAL_YEARLY)、自動選択(INTERVAL_BEST)がある
        //
        // 取得する時間の始まり : 取得したいデータの時間帯のスタート地点。今回は、その日の午前0時。
        // 取得する時間の終わり : 取得したいデータの時間帯の終わり。今回は、現在時刻。

        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager // usageStatsManagerのオブジェクトの取得
        //val queryMap = usageStatsManager.queryAndAggregateUsageStats(cal.timeInMillis,System.currentTimeMillis())

        return usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            cal.timeInMillis,
            System.currentTimeMillis()
        )// インターバルなど決定
    }

    private fun showAppUsageStats(usageStats: MutableList<UsageStats>) {
        usageStats.sortWith(Comparator { right, left ->
            compareValues(right.lastTimeUsed, left.lastTimeUsed)
        })

        var str = readFiles(fileName)
        usageStats.forEach { it ->
            if (it.totalTimeInForeground.toInt() != 0) {
                val date = Date(it.lastTimeUsed)
                val eDate = Date(it.firstTimeStamp)
                val format = SimpleDateFormat("yyyy.MM.dd, E, HH:mm")
                str += "${it.packageName},${format.format(date)},${it.totalTimeInForeground},${format.format(eDate)}\n"
                //Log.d( ContentValues.TAG, "packageName: ${it.packageName}, lastTimeUsed: ${Date(it.lastTimeUsed)}" + "totalTimeInForeground: ${it.totalTimeInForeground}")
            }
        }
        if (str != null) {
            saveFile(fileName,str)
        }
    }
    private fun saveFile(file: String, str: String) {

        applicationContext.openFileOutput(file, Context.MODE_PRIVATE).use {
            it.write(str.toByteArray())
        }

        //File(applicationContext.filesDir, file).writer().use {
        //    it.write(str)
        //}
    }
    private fun readFiles(file: String): String? {
        // to check whether file exists or not
        val readFile = File(applicationContext.filesDir, file)

        if(!readFile.exists()){
            Log.d("debug","No file exists")
            return null
        }
        else{
            return readFile.bufferedReader().use(BufferedReader::readText)
        }
    }
}