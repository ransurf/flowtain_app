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
import com.example.flowtain.ui.settings.SettingsActivity
import com.example.flowtain.ui.settings.SettingsActivityFragment
import com.example.flowtain.TimerExpiredReceiver
import com.example.flowtain.util.NotificationUtil
import com.example.flowtain.util.PrefUtil
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_timer.*
import java.util.*


class TimerFragment : Fragment(), View.OnClickListener {

    companion object {
        var points = 0L

        fun setAlarm(context: Context, nowSeconds: Long, secondsRemaining: Long): Long {
            val wakeUpTime = (nowSeconds + secondsRemaining) * 1000
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeUpTime, pendingIntent)
            PrefUtil.setAlarmSetTime(nowSeconds, context)
            return wakeUpTime
        }

        fun removeAlarm(context: Context) {
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            PrefUtil.setAlarmSetTime(0, context)
        }

        val nowSeconds: Long
            get() = Calendar.getInstance().timeInMillis / 1000
    }

    private lateinit var timerViewModel: TimerViewModel
    private lateinit var timer : CountDownTimer
    private var timerLengthSeconds = 0L
    private var timerState = TimerState.Stopped
    private var secondsRemaining = 0L
    private lateinit var fab_start:FloatingActionButton
    private lateinit var fab_pause:FloatingActionButton
    private lateinit var fab_stop:FloatingActionButton

    enum class TimerState{
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
            points += (timerLengthSeconds - secondsRemaining)// 60
            onTimerFinished()
        }
        setHasOptionsMenu(true)
        return root
    }

    override fun onResume(){
        super.onResume()
        initTimer()
        points = PrefUtil.getNumPoints(this.requireActivity())

        removeAlarm(this.requireActivity())
        NotificationUtil.hideTimerNotification(this.requireActivity())
    }

    override fun onPause() {
        super.onPause()

        if (timerState == TimerState.Running) {
            timer.cancel()
            val wakeUpTime = setAlarm(this.requireActivity(), nowSeconds, secondsRemaining)
            NotificationUtil.showTimerRunning(this.requireActivity(), wakeUpTime)
        } else if (timerState ==TimerState.Paused) {
            NotificationUtil.showTimerPaused(this.requireActivity())
        }

        PrefUtil.setPreviousTimerLengthSeconds(timerLengthSeconds, this.requireActivity())
        PrefUtil.setSecondsRemaining(secondsRemaining, this.requireActivity())
        PrefUtil.setTimerState(timerState, this.requireActivity())
        PrefUtil.setNumPoints(points, this.requireActivity())


    }

    private fun initTimer() {
        timerState = PrefUtil.getTimerState(this.requireActivity())

        if (timerState == TimerState.Stopped)
            setNewTimerLength()
        else
            setPreviousTimerLength()

        secondsRemaining = if (timerState ==TimerState.Running || timerState == TimerState.Paused)
            PrefUtil.getSecondsRemaining(this.requireActivity())
        else
            timerLengthSeconds

        val alarmSetTime = PrefUtil.getAlarmSetTime(this.requireActivity())
        if (alarmSetTime > 0) //if alarm is set
            secondsRemaining -= nowSeconds - alarmSetTime

        if (secondsRemaining <= 0) {
            onTimerFinished()
        }  else if (timerState ==TimerState.Running)
            startTimer()

        updateButtons()
        updateCountdownUI()
    }

    private fun onTimerFinished() {
        timerState = TimerState.Stopped
        setNewTimerLength()
        progress_countdown.progress = 0
        PrefUtil.setSecondsRemaining(timerLengthSeconds, this.requireActivity())
        secondsRemaining = timerLengthSeconds

        updateButtons()
        updateCountdownUI()
    }

    private fun startTimer() {
        Log.i("MainActvity", "pointsEarned: $points")
        timerState = TimerState.Running
        timer = object : CountDownTimer(secondsRemaining * 1000, 1000) {
            override fun onFinish() {
                onTimerFinished()
                points += timerLengthSeconds // 60
            }

            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000

                updateButtons()
                updateCountdownUI()
            }
        }.start()
    }

    private fun setNewTimerLength() {
        val lengthInMinutes = PrefUtil.getTimerLength(this.requireActivity())
        timerLengthSeconds = (lengthInMinutes*60L)
        progress_countdown.max = timerLengthSeconds.toInt()
    }

    private fun setPreviousTimerLength() {
        timerLengthSeconds = PrefUtil.getPreviousTimerLengthSeconds(this.requireActivity())
        progress_countdown.max = timerLengthSeconds.toInt()
    }

    private fun updateCountdownUI() {
        val minutesUntilFinished = secondsRemaining/60
        val secondsInMinuteUntilFinished = secondsRemaining - minutesUntilFinished * 60
        val secondsStr = secondsInMinuteUntilFinished.toString()
        textView_countdown.text = "$minutesUntilFinished:${
            if (secondsStr.length ==2) secondsStr
            else "0" + secondsStr}"
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

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_timer_popup, menu)
        (drawable.ic_menu_timer)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                SettingsActivityFragment.preference = "timer"
                val intent = Intent(this.requireActivity(), SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onClick(v: View?) {

    }

}
