package com.example.flowtain

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.flowtain.ui.timer.TimerFragment
import com.example.flowtain.ui.timer.TimerFragment.Companion.nowSeconds
import com.example.flowtain.util.NotificationUtil
import com.example.flowtain.util.PrefUtil

class TimerNotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            TimerActionTypes.STOP.value -> {
                TimerFragment.removeAlarm(context)
                PrefUtil.setTimerState(TimerFragment.TimerState.Stopped, context)
                NotificationUtil.hideTimerNotification(context)
            }
            TimerActionTypes.PAUSE.value -> {
                var secondsRemaining = PrefUtil.getSecondsRemaining(context)
                val alarmSetTime = PrefUtil.getAlarmSetTime(context)

                secondsRemaining -= nowSeconds - alarmSetTime
                PrefUtil.setSecondsRemaining(secondsRemaining, context)

                TimerFragment.removeAlarm(context)
                PrefUtil.setTimerState(TimerFragment.TimerState.Paused, context)
                NotificationUtil.showTimerPaused(context)
            }
            TimerActionTypes.RESUME.value -> {
                val secondsRemaining = PrefUtil.getSecondsRemaining(context)
                val wakeUpTime = TimerFragment.setAlarm(context, TimerFragment.nowSeconds, secondsRemaining)
                PrefUtil.setTimerState(TimerFragment.TimerState.Running, context)
                NotificationUtil.showTimerRunning(context, wakeUpTime)
            }
            TimerActionTypes.START.value -> {
                val minutesRemaining = PrefUtil.getTimerLength(context)
                val secondsRemaining = minutesRemaining * 60L
                val wakeUpTime = TimerFragment.setAlarm(context, TimerFragment.nowSeconds, secondsRemaining)
                PrefUtil.setTimerState(TimerFragment.TimerState.Running, context)
                PrefUtil.setSecondsRemaining(secondsRemaining, context)
                NotificationUtil.showTimerRunning(context, wakeUpTime)
            }

        }
    }
}