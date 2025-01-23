package com.example.codsoft3

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.codsoft3.databinding.ActivityMainBinding
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var selectedToneUri: String? = null
    var listOfImages = arrayListOf(
        R.drawable.welcome,
        R.drawable.j27,
        R.drawable.j31,
        R.drawable.ninefive,
        R.drawable.ninefour,
        R.drawable.nineseven,
        R.drawable.nininine,
        R.drawable.seven,
        R.drawable.twelve,
        R.drawable.onee,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.setAlarm.setOnClickListener {

            val hour = binding.timePicker.hour
            val minute = binding.timePicker.minute
            val now = Calendar.getInstance()

            val alarmTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val remainingTimeMillis = alarmTime.timeInMillis - now.timeInMillis
            val remainingHours = (remainingTimeMillis / (1000 * 60 * 60)) % 24
            val remainingMinutes = (remainingTimeMillis / (1000 * 60)) % 60


            val remainingTimeText =
                "Alarm set for $remainingHours hours and $remainingMinutes minutes from now"
            Toast.makeText(this, remainingTimeText, Toast.LENGTH_LONG).show()

            if (alarmTime.timeInMillis <= now.timeInMillis) {
                Toast.makeText(this, "Select a future time!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            saveAlarm(alarmTime, binding.alarmName.text.toString(), selectedToneUri, true)

        }

        binding.imageButton.setOnClickListener {
            openTonePicker()
        }
    }

    private fun saveAlarm(
        alarmTime: Calendar, alarmName: String, selectedToneUri: String?, isActive: Boolean
    ) {
        val sharedPreferences = getSharedPreferences("Alarm", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Format the alarm time as a string (same way as before)
        val alarmTimeFormatted = android.text.format.DateFormat.format(
            "MMM dd, yyyy hh:mm a", alarmTime
        ).toString()

        val totalAlarms = sharedPreferences.getInt("alarmId", 0)

        editor.putInt("alarmId", totalAlarms + 1)
        editor.putString("alarmName$totalAlarms", alarmName )
        editor.putString("alarmTone$totalAlarms", selectedToneUri)
        editor.putString("alarmTime$totalAlarms", alarmTimeFormatted)
        editor.putBoolean("isActive$totalAlarms", isActive)
        editor.putInt("alarmAva$totalAlarms", listOfImages.random())
        editor.apply()

        // Provide feedback to the user
        Toast.makeText(this, "Alarm created and saved $totalAlarms", Toast.LENGTH_SHORT).show()

        // Start the next activity
        startActivity(Intent(this, MainActivity2::class.java))
        finish()
    }

    private fun openTonePicker() {
        val tonePickerIntent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Tone")
            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                selectedToneUri?.let { RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) })
        }
        startActivityForResult(tonePickerIntent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK) {
            val toneUri: Uri? = data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)

            if (toneUri != null) {
                selectedToneUri = toneUri.toString()
                Toast.makeText(this, "Tone selected: $selectedToneUri", Toast.LENGTH_SHORT).show()
                binding.AlarmTone.text = "$selectedToneUri"
            } else {
                Toast.makeText(this, "No tone selected!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Tone selection canceled.", Toast.LENGTH_SHORT).show()
        }
    }
}
