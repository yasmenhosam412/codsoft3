package com.example.codsoft3

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.codsoft3.databinding.ActivityMain2Binding
import java.util.Calendar
import java.util.Locale

class MainActivity2 : AppCompatActivity(), AalarmAdapter.alarmActions {

    private lateinit var binding: ActivityMain2Binding
    private lateinit var adapter: AalarmAdapter
    private val listOfAlarms = ArrayList<alarms>()
    private val PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = AalarmAdapter(listOfAlarms, this)
        binding.rec.layoutManager = LinearLayoutManager(this)
        binding.rec.adapter = adapter

        fetchAlarms()

        checkStoragePermission();

        binding.imageButton2.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permission
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE
            )
        } else {

        }
    }

    private fun fetchAlarms() {
        val sharedPreferences = getSharedPreferences("Alarm", Context.MODE_PRIVATE)
        val totalAlarms = sharedPreferences.getInt("alarmId", 0)

        listOfAlarms.clear()
        for (i in 0..totalAlarms) {
            val name = sharedPreferences.getString("alarmName$i", "UnKnown")
            val time = sharedPreferences.getString("alarmTime$i", null)
            val isActive = sharedPreferences.getBoolean("isActive$i", false)
            val toneUri = sharedPreferences.getString("alarmTone$i", "")
            val ava = sharedPreferences.getInt("alarmAva$i", R.drawable.delete)


            if (name != null && time != null) {
                listOfAlarms.add(alarms(i, name, time, isActive, ava, toneUri!!))
                if (isActive) {
                    setAlarmFromTime(time, toneUri, i, ava, name)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun setAlarmFromTime(
        alarmTime: String,
        toneUri: String?,
        i: Int,
        ava: Int,
        name: String
    ) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        val calendar = Calendar.getInstance()
        val formatter = java.text.SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
        calendar.time = formatter.parse(alarmTime) ?: return


        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("ALARM_TONE_URI", toneUri)
            putExtra("ALARM_ID", i)
            putExtra("ALARM_AVA", ava)
            putExtra("ALARM_NAME", name)
        }


        val pendingIntent = PendingIntent.getBroadcast(
            this,
            i,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    override fun activeAalrm(position: Int) {
        val alarm = listOfAlarms[position]
        val sharedPreferences = getSharedPreferences("Alarm", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val isActive = !alarm.alarmActive

        alarm.alarmActive = isActive
        editor.putBoolean("isActive${alarm.alarmId}", isActive)
        editor.apply()

        if (isActive) {
            setAlarmFromTime(
                alarm.alarmDate,
                sharedPreferences.getString("alarmTone${alarm.alarmId}", null),
                alarm.alarmId,
                alarm.alarmImage,
                alarm.alarmName
            )
        } else {
            cancelAlarm(alarm.alarmId)
        }

        adapter.notifyItemChanged(position)
    }

    private fun cancelAlarm(alarmId: Int) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    override fun deleteAalrm(position: Int) {
        deleteAlert(position)
    }

    private fun deleteAlert(position: Int) {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Delete Alarm")
            .setMessage("Are you sure you want to delete this alarm?")
            .setPositiveButton("Yes") { _, _ ->
                val sharedPreferences = getSharedPreferences("Alarm", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()

                val deletedAlarm = listOfAlarms.removeAt(position)
                cancelAlarm(deletedAlarm.alarmId)

                editor.remove("alarmName${deletedAlarm.alarmId}")
                editor.remove("alarmTime${deletedAlarm.alarmId}")
                editor.remove("alarmTone${deletedAlarm.alarmId}")
                editor.remove("isActive${deletedAlarm.alarmId}")
                editor.remove("alarmAva${deletedAlarm.alarmId}")

                val updatedList = ArrayList<alarms>()
                for (i in listOfAlarms.indices) {
                    val oldAlarm = listOfAlarms[i]
                    val newId = i

                    updatedList.add(
                        alarms(
                            newId,
                            oldAlarm.alarmName,
                            oldAlarm.alarmDate,
                            oldAlarm.alarmActive,
                            oldAlarm.alarmImage,
                            oldAlarm.alarmTone
                        )
                    )

                    editor.putInt("alarmAva$newId", oldAlarm.alarmImage)
                    editor.putString("alarmName$newId", oldAlarm.alarmName)
                    editor.putString("alarmTime$newId", oldAlarm.alarmDate)
                    editor.putString("alarmTone$newId", oldAlarm.alarmTone)
                    editor.putBoolean("isActive$newId", oldAlarm.alarmActive)

                    cancelAlarm(oldAlarm.alarmId)
                    if (oldAlarm.alarmActive) {
                        setAlarmFromTime(
                            oldAlarm.alarmDate,
                            oldAlarm.alarmTone,
                            newId,
                            oldAlarm.alarmImage,
                            oldAlarm.alarmName
                        )
                    }
                }

                editor.putInt("alarmId", updatedList.size)
                editor.apply()

                // Update the list and notify adapter
                listOfAlarms.clear()
                listOfAlarms.addAll(updatedList)
                adapter.notifyDataSetChanged()
            }
            .setNegativeButton("No", null)
            .create()

        alertDialog.show()
    }


    override fun editAalrm(position: Int) {
        var aal = listOfAlarms[position]
        editAlarm(this, position, aal.alarmName)
    }

    fun editAlarm(context: Context, position: Int, oldName: String) {
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_edit_alarm, null)

        val timePicker: TimePicker = dialogView.findViewById(R.id.timePicker)
        val alarmNameEditText: EditText = dialogView.findViewById(R.id.alarmNameEditText)
        alarmNameEditText.setText(oldName)

        val dialog = AlertDialog.Builder(context)
            .setTitle("Edit Alarm")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->


                val alarmName = alarmNameEditText.text.toString()

                val hour = timePicker.hour
                val minute = timePicker.minute
                val now = Calendar.getInstance()

                val alarmTime = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val alarmTimeFormatted = android.text.format.DateFormat.format(
                    "MMM dd, yyyy hh:mm a", alarmTime
                ).toString()

                val remainingTimeMillis = alarmTime.timeInMillis - now.timeInMillis
                val remainingHours = (remainingTimeMillis / (1000 * 60 * 60)) % 24
                val remainingMinutes = (remainingTimeMillis / (1000 * 60)) % 60

                // Display remaining time
                val remainingTimeText =
                    "Alarm set for $remainingHours hours and $remainingMinutes minutes from now"
                Toast.makeText(this, remainingTimeText, Toast.LENGTH_LONG).show()

                updateAlarm(position, alarmName, alarmTimeFormatted)
            }
            .setNegativeButton("Cancel", null)
            .create()

        // Show the dialog
        dialog.show()
    }

    private fun updateAlarm(position: Int, alarmName: String, alarmTimeFormatted: String) {
        val alarm = listOfAlarms[position]
        alarm.alarmName = alarmName
        alarm.alarmDate = alarmTimeFormatted

        val sharedPreferences = getSharedPreferences("Alarm", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        // Update alarm in SharedPreferences
        editor.putString("alarmName${alarm.alarmId}", alarmName)
        editor.putString("alarmTime${alarm.alarmId}", alarmTimeFormatted)
        editor.apply()

        if (alarm.alarmActive == true) {

            cancelAlarm(alarm.alarmId)
            setAlarmFromTime(
                alarmTimeFormatted,
                sharedPreferences.getString("alarmTone${alarm.alarmId}", null),
                alarm.alarmId,
                alarm.alarmImage, alarmName
            )
        } else {
            Toast.makeText(this, "Remember To Activate Alarm", Toast.LENGTH_SHORT).show()
        }


        adapter.notifyItemChanged(position)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                // Permission denied, show a message or fall back to default
                Toast.makeText(
                    this, "Permission denied. Using default alarm tone.", Toast.LENGTH_SHORT
                ).show()

            }
        }
    }

}
