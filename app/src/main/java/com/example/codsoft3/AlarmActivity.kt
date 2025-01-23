package com.example.codsoft3


import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.media.RingtoneManager
import android.os.Handler
import android.os.Looper
import com.example.codsoft3.AlarmReceiver
import com.example.codsoft3.databinding.ActivityAlarmBinding
import kotlin.random.Random

class AlarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmBinding
    private lateinit var mediaPlayer: MediaPlayer
    public var alarmID: Int = 0
    public var alarmAVA: Int = 0
    public var alamrName: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val toneUriString = intent.getStringExtra("ALARM_TONE_URI")
        var toneUri =
            if (!toneUriString.isNullOrEmpty()) Uri.parse(toneUriString) else RingtoneManager.getDefaultUri(
                RingtoneManager.TYPE_ALARM
            )

        initializeMediaPlayer(toneUri)

        alarmID = intent.getIntExtra("ALARM_ID", 0)
        alarmAVA = intent.getIntExtra("ALARM_AVA", R.drawable.alarm)
        alamrName = intent.getStringExtra("ALARM_NAME") ?: "Default Alarm Name"
        Toast.makeText(this, "$alarmID", Toast.LENGTH_SHORT).show()


        binding.alarmText.setText(alamrName)

        binding.imageView3.setImageResource(alarmAVA)

        binding.snoozeButton.setOnClickListener {
            snoozeAlarm()
        }

        moveButtonRandomly()

        binding.dismissButton.setOnClickListener {
            dismissAlarm()
        }

    }

    fun moveButtonRandomly() {
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels
        val buttonWidth = binding.dismissButton.width
        val buttonHeight = binding.dismissButton.height

        val handler = Handler(Looper.getMainLooper())
        val startTime = System.currentTimeMillis()

        val moveRunnable = object : Runnable {
            override fun run() {
                if (System.currentTimeMillis() - startTime >= 3000) {
                    return // Stop after 3 seconds
                }

                val randomX = Random.nextInt(0, screenWidth - buttonWidth).toFloat()
                val randomY = Random.nextInt(0, screenHeight - buttonHeight).toFloat()

                val animatorX = ObjectAnimator.ofFloat(binding.dismissButton, "x", randomX)
                val animatorY = ObjectAnimator.ofFloat(binding.dismissButton, "y", randomY)
                animatorX.duration = 500
                animatorY.duration = 500

                animatorX.start()
                animatorY.start()

                handler.postDelayed(this, 500) // Move every 0.5 seconds
            }
        }

        handler.post(moveRunnable)
    }



    private fun initializeMediaPlayer(toneUri: Uri) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@AlarmActivity, toneUri)
                setOnPreparedListener { start() }
                setOnCompletionListener { release() }
                prepareAsync() // Use async preparation to avoid blocking UI
            }
        } catch (e: Exception) {
            Log.e("AlarmActivity", "Error initializing MediaPlayer: ${e.message}")
            Toast.makeText(this, "Error initializing the alarm tone.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun stopAndReleaseMediaPlayer() {
        if (this::mediaPlayer.isInitialized) {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.release()
        }
    }


    @SuppressLint("ScheduleExactAlarm")
    private fun snoozeAlarm() {
        Toast.makeText(this, "Alarm snoozed for 5 minutes", Toast.LENGTH_SHORT).show()

        // Stop the current alarm sound
        if (this::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }

        // Set up the snooze time (5 minutes later)
        val snoozeTimeInMillis = System.currentTimeMillis() + 5 * 60 * 1000

        // Create an intent to trigger the alarm again
        val snoozeIntent = Intent(this, AlarmReceiver::class.java)
        snoozeIntent.putExtra("ALARM_TONE_URI", intent.getStringExtra("ALARM_TONE_URI"))

        val snoozePendingIntent = PendingIntent.getBroadcast(
            this, 1, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Schedule the snooze alarm
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, snoozeTimeInMillis, snoozePendingIntent)

        // Close the activity
        finish()
    }


    private fun dismissAlarm() {
        Toast.makeText(this, "Alarm dismissed", Toast.LENGTH_SHORT).show()
        stopAndReleaseMediaPlayer() // Stop and release media

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val cancelIntent = Intent(this, AlarmReceiver::class.java)
        val cancelPendingIntent = PendingIntent.getBroadcast(
            this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(cancelPendingIntent)

        updateAlarm(alarmID)
        startActivity(Intent(this, MainActivity2::class.java))
        finish()
    }


    private fun updateAlarm(position: Int) {
        val sharedPreferences = getSharedPreferences("Alarm", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isActive${position}", false)
        editor.apply()
    }

}
