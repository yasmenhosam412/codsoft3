package com.example.codsoft3

import android.net.Uri

data class alarms(
    var alarmId : Int,
    var alarmName  :String,
    var alarmDate  :String,
    var alarmActive  :Boolean,
    var alarmImage : Int,
    var alarmTone : String
)
