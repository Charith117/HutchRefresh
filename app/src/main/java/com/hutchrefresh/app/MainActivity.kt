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
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private var isOn = false
    private var isDark = false
    private var refreshCount = 0
    private var intervalMin = 5
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
                intervalMin = progress + 1
                val label = if (intervalMin < 60) "$intervalMin min" else "1 hr"
                intervalText.text = label
                intervalLabel.text = "Refresh interval: $label"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (isOn) {
                    restartTimer(nextRefreshText, refreshCountText, logText)
                    addLog(logText, "[config] Interval changed to $intervalMin min")
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

    private fun restartTimer(next: TextView, count: TextView, log: TextView) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(intervalMin * 60 * 1000L, 1000) {
            override fun onTick(ms: Long) {
                val m = ms / 60000
                val s = (ms % 60000) / 1000
                next.text = "$m:${s.toString().padStart(2, '0')}"
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
