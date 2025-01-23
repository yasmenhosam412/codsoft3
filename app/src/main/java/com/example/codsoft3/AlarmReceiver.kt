package com.example.codsoft3

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import java.util.Calendar
import java.util.Date

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val toneUri = intent.getStringExtra("ALARM_TONE_URI")
        val alarmID = intent.getIntExtra("ALARM_ID",0)
        val alarmAVA = intent.getIntExtra("ALARM_AVA",R.drawable.alarm)
        val alarmNAME= intent.getStringExtra("ALARM_NAME")
        val alarmTime = Calendar.getInstance()
        alarmTime.time = Date()

        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("ALARM_TONE_URI", toneUri)
            putExtra("ALARM_ID", alarmID)
            putExtra("ALARM_AVA", alarmAVA)
            putExtra("ALARM_NAME", alarmNAME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(alarmIntent)
    }
}
