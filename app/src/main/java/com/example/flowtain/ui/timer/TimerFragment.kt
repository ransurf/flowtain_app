package com.example.flowtain.ui.timer


import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.flowtain.R
import com.example.flowtain.R.*
import com.example.flowtain.TimerExpiredReceiver
import com.example.flowtain.util.NotificationUtil
import com.example.flowtain.util.PointsUtil
import com.example.flowtain.util.PrefUtil
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_timer.*
import java.util.*


class TimerFragment : Fragment(), View.OnClickListener {

    companion object {
        //sets alarm for the timer
        fun setAlarm(context: Context, nowSeconds: Long, secondsRemaining: Long): Long {
            val wakeUpTime = (nowSeconds + secondsRemaining) * 1000
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeUpTime, pendingIntent)
            PrefUtil.setAlarmSetTime(nowSeconds, context)
            return wakeUpTime
        }
        //removes the alarm for the timer
        fun removeAlarm(context: Context) {
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            PrefUtil.setAlarmSetTime(0, context)
        }
        //current time
        val nowSeconds: Long
            get() = Calendar.getInstance().timeInMillis / 1000
    }

    private lateinit var timerViewModel: TimerViewModel //yet to be fully implemented
    private lateinit var timer: CountDownTimer
    private var timerLengthSeconds = 0L
    private var timerState = TimerState.Stopped
    private var secondsRemaining = 0L
    private lateinit var fab_start: FloatingActionButton //buttons for changing timer state
    private lateinit var fab_pause: FloatingActionButton
    private lateinit var fab_stop: FloatingActionButton

    enum class TimerState {
        Stopped, Paused, Running
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?

    ): View? {
        timerViewModel =
                ViewModelProvider(this).get(TimerViewModel::class.java)
        val root = inflater.inflate(layout.fragment_timer, container, false)
        fab_start = root.findViewById(R.id.fab_start)
        fab_pause = root.findViewById(R.id.fab_pause)
        fab_stop = root.findViewById(R.id.fab_stop)

        fab_start.setOnClickListener {
            startTimer()
            timerState = TimerState.Running
            updateButtons()
        }

        fab_pause.setOnClickListener {
            timer.cancel()
            timerState = TimerState.Paused
            updateButtons()
        }

        fab_stop.setOnClickListener {
            timer.cancel()
            PointsUtil.addPoints((timerLengthSeconds-secondsRemaining)/60, requireActivity())
            onTimerFinished()
        }
        setHasOptionsMenu(true)
        return root
    }


    override fun onResume() {
        super.onResume()
        initTimer()

        removeAlarm(requireActivity())
        NotificationUtil.hideTimerNotification(requireActivity())
    }

    override fun onPause() {
        super.onPause()

        if (timerState == TimerState.Running) {
            timer.cancel()
            val wakeUpTime = setAlarm(requireActivity(), nowSeconds, secondsRemaining)
            NotificationUtil.showTimerRunning(requireActivity(), wakeUpTime)
        } else if (timerState == TimerState.Paused) {
            NotificationUtil.showTimerPaused(requireActivity())
        }
        //stores variables on fragment exit
        PrefUtil.setPreviousTimerLengthSeconds(timerLengthSeconds, requireActivity())
        PrefUtil.setSecondsRemaining(secondsRemaining, requireActivity())
        PrefUtil.setTimerState(timerState, requireActivity())
    }

    private fun initTimer() {
        //Log.i("TimerFragment", "InitTimer called")
        timerState = PrefUtil.getTimerState(requireActivity())
        if (timerState == TimerState.Stopped) {
            PointsUtil.addPoints(timerLengthSeconds/60, requireActivity())
            setNewTimerLength()
        } else {
            setPreviousTimerLength()
        }

        secondsRemaining = if (timerState == TimerState.Running || timerState == TimerState.Paused) {
            PrefUtil.getSecondsRemaining(requireActivity())
        } else {
            PrefUtil.getTimerLength(requireActivity()).toLong() * 60
        }

        val alarmSetTime = PrefUtil.getAlarmSetTime(requireActivity())
        if (alarmSetTime > 0) //if alarm is set
            secondsRemaining -= nowSeconds - alarmSetTime

        if (secondsRemaining <= 0) {
            onTimerFinished()
        } else if (timerState == TimerState.Running)
            startTimer()
        //Log.i("TimerFragment", "$secondsRemaining")
        updateButtons()
        updateCountdownUI()
    }

    private fun onTimerFinished() {
        //Log.i("TimerFragment", "onTimerFinished called")
        timerState = TimerState.Stopped
        setNewTimerLength()

        progress_countdown.progress = 0
        PrefUtil.setSecondsRemaining(timerLengthSeconds, requireActivity())
        secondsRemaining = timerLengthSeconds

        updateButtons()
        updateCountdownUI()
    }

    private fun startTimer() {
        Log.i("TimerFragment", "StartTimer called")
        timerState = TimerState.Running
        timer = object : CountDownTimer(secondsRemaining * 1000, 1000) {
            override fun onFinish() {
                onTimerFinished()
                PointsUtil.addPoints(timerLengthSeconds/60, requireActivity())
                //points +=  // 60
            }

            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000

                updateButtons()
                updateCountdownUI()
            }
        }.start()
    }

    private fun setNewTimerLength() {
        val lengthInMinutes = PrefUtil.getTimerLength(requireActivity())
        timerLengthSeconds = (lengthInMinutes * 60L)
        progress_countdown.max = timerLengthSeconds.toInt()
    }

    private fun setPreviousTimerLength() {
        timerLengthSeconds = PrefUtil.getPreviousTimerLengthSeconds(requireActivity())
        progress_countdown.max = timerLengthSeconds.toInt()
    }

    private fun updateCountdownUI() {
        val minutesUntilFinished = secondsRemaining / 60
        val secondsInMinuteUntilFinished = secondsRemaining - minutesUntilFinished * 60
        val secondsStr = secondsInMinuteUntilFinished.toString()
        text_countdown.text = "$minutesUntilFinished:${
            if (secondsStr.length == 2) secondsStr
            else "0" + secondsStr
        }"
        if (timerState == TimerState.Stopped) {
            if (minutesUntilFinished<1) {
                //PointsUtil.addPoints(timerLengthSeconds, requireActivity())
            }
            text_countdown.text = "${PrefUtil.getTimerLength(requireActivity()).toLong()}:00"
        }
        progress_countdown.progress = (timerLengthSeconds - secondsRemaining).toInt()
    }

    private fun updateButtons() {
        when (timerState) {
            TimerState.Running -> {
                fab_start.isEnabled = false
                fab_pause.isEnabled = true
                fab_stop.isEnabled = true
            }
            TimerState.Stopped -> {
                fab_start.isEnabled = true
                fab_pause.isEnabled = false
                fab_stop.isEnabled = false
            }
            TimerState.Paused -> {
                fab_start.isEnabled = true
                fab_pause.isEnabled = false
                fab_stop.isEnabled = true
            }
        }
    }

    override fun onClick(v: View?) {

    }

}
