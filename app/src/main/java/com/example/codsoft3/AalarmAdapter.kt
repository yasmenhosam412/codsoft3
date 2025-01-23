package com.example.codsoft3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class AalarmAdapter(private val alarmList: ArrayList<alarms> , var alarmAction : alarmActions) : RecyclerView.Adapter<AalarmAdapter.AlarmViewHolder>() {


    interface alarmActions{
        fun activeAalrm(position: Int)
        fun deleteAalrm(position: Int)
        fun editAalrm(position: Int)
    }



    class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val alarmTime: TextView = itemView.findViewById(R.id.textView3)
        val alarmId: TextView = itemView.findViewById(R.id.textView)
        val alarmLabel: TextView = itemView.findViewById(R.id.textView4)
        val alarmSwitch: Switch = itemView.findViewById(R.id.switch1)
        val image: ImageView = itemView.findViewById(R.id.imageView)
        var cardView : CardView = itemView.findViewById(R.id.card)
    }

    // Create a new view holder instance
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.alarm_item, parent, false)
        return AlarmViewHolder(view)
    }

    // Bind the data to each view holder
    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = alarmList[position]
        holder.alarmTime.text = alarm.alarmName
        holder.alarmLabel.text = alarm.alarmDate


        holder.image.setImageResource(alarm.alarmImage)
        holder.alarmSwitch.isChecked = alarm.alarmActive
        holder.alarmId.text = (alarm.alarmId + 1).toString()

        holder.image.setOnClickListener {
         alarmAction.deleteAalrm(position)
        }

        holder.cardView.setOnLongClickListener {
            alarmAction.editAalrm(position)
            true
        }

        holder.alarmSwitch.setOnClickListener {
            alarmAction.activeAalrm(position)
        }

    }

    // Return the total number of items
    override fun getItemCount(): Int {
        return alarmList.size
    }
}
