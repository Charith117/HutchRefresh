package com.hutchrefresh.app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var isOn = false
    private var isDark = false
    private var refreshCount = 0
    private var intervalSeconds = 10 // default 10 seconds minimum
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rootLayout = findViewById<ScrollView>(R.id.rootLayout)
        val powerButton = findViewById<FrameLayout>(R.id.powerButton)
        val powerLabel = findViewById<TextView>(R.id.powerLabel)
        val powerIcon = findViewById<android.widget.ImageView>(R.id.powerIcon)
        val statusText = findViewById<TextView>(R.id.statusText)
        val refreshCountText = findViewById<TextView>(R.id.refreshCount)
        val nextRefreshText = findViewById<TextView>(R.id.nextRefresh)
        val intervalText = findViewById<TextView>(R.id.intervalText)
        val intervalLabel = findViewById<TextView>(R.id.intervalLabel)
        val statusVal = findViewById<TextView>(R.id.statusVal)
        val logText = findViewById<TextView>(R.id.logText)
        val intervalSlider = findViewById<SeekBar>(R.id.intervalSlider)
        val themeToggle = findViewById<ImageButton>(R.id.themeToggle)
        val outerRing = findViewById<android.view.View>(R.id.outerRing)
        val logoText = findViewById<TextView>(R.id.logoText)

        // Slider: 0-based, each step = 10 seconds, min=10s, max=600s (10 mins)
        // progress 0 = 10sec, progress 1 = 20sec ... progress 59 = 600sec
        intervalSlider.max = 59
        intervalSlider.progress = 0 // starts at 10 seconds

        fun formatTime(seconds: Int): String {
            return if (seconds < 60) "${seconds}s"
            else if (seconds % 60 == 0) "${seconds / 60} min"
            else "${seconds / 60}m ${seconds % 60}s"
        }

        // Set initial labels
        intervalText.text = "10s"
        intervalLabel.text = "Refresh interval: 10 seconds"

        themeToggle.setOnClickListener {
            isDark = !isDark
            if (isDark) {
                rootLayout.setBackgroundColor(Color.parseColor("#121212"))
                logText.setBackgroundColor(Color.parseColor("#0a0a14"))
            } else {
                rootLayout.setBackgroundColor(Color.parseColor("#F8F8F8"))
                logText.setBackgroundColor(Color.parseColor("#1a1a2e"))
            }
        }

        intervalSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                intervalSeconds = (progress + 1) * 10 // 10, 20, 30 ... 600
                val label = formatTime(intervalSeconds)
                intervalText.text = label
                intervalLabel.text = "Refresh interval: $label"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (isOn) {
                    restartTimer(nextRefreshText, refreshCountText, logText)
                    addLog(logText, "[config] Interval changed to ${formatTime(intervalSeconds)}")
                }
            }
        })

        powerButton.setOnClickListener {
            isOn = !isOn
            if (isOn) {
                powerButton.setBackgroundResource(R.drawable.circle_green)
                outerRing.setBackgroundResource(R.drawable.ring_on)
                logoText.setBackgroundResource(R.drawable.circle_green)
                powerLabel.text = "TAP TO STOP"
                powerLabel.setTextColor(Color.WHITE)
                powerIcon.setColorFilter(Color.WHITE)
                statusText.text = "● Service running"
                statusText.setTextColor(Color.parseColor("#1a7a44"))
                statusVal.text = "Active"
                addLog(logText, "[start] Auto-refresh service started")
                startService(Intent(this, RefreshService::class.java))
                restartTimer(nextRefreshText, refreshCountText, logText)
            } else {
                powerButton.setBackgroundResource(R.drawable.circle_grey)
                outerRing.setBackgroundResource(R.drawable.ring_off)
                logoText.setBackgroundResource(R.drawable.circle_red)
                powerLabel.text = "TAP TO START"
                powerLabel.setTextColor(Color.parseColor("#888888"))
                powerIcon.setColorFilter(Color.parseColor("#888888"))
                statusText.text = "● Service stopped"
                statusText.setTextColor(Color.parseColor("#AAAAAA"))
                statusVal.text = "Idle"
                nextRefreshText.text = "--:--"
                countDownTimer?.cancel()
                addLog(logText, "[stop] Service stopped by user")
                stopService(Intent(this, RefreshService::class.java))
            }
        }
    }

    private fun formatTime(seconds: Int): String {
        return if (seconds < 60) "${seconds}s"
        else if (seconds % 60 == 0) "${seconds / 60} min"
        else "${seconds / 60}m ${seconds % 60}s"
    }

    private fun restartTimer(next: TextView, count: TextView, log: TextView) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(intervalSeconds * 1000L, 1000) {
            override fun onTick(ms: Long) {
                val s = ms / 1000
                if (s < 60) {
                    next.text = "${s}s"
                } else {
                    next.text = "${s / 60}:${(s % 60).toString().padStart(2, '0')}"
                }
            }
            override fun onFinish() {
                refreshCount++
                count.text = refreshCount.toString()
                addLog(log, "[refresh #$refreshCount] Hutch app launched")
                restartTimer(next, count, log)
            }
        }.start()
    }

    private fun addLog(logText: TextView, msg: String) {
        logText.text = "${logText.text}\n$msg"
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
