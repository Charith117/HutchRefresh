package com.hutchrefresh.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val statusText = findViewById<TextView>(R.id.statusText)
        val startButton = findViewById<Button>(R.id.startButton)
        val stopButton = findViewById<Button>(R.id.stopButton)

        startButton.setOnClickListener {
            val serviceIntent = Intent(this, RefreshService::class.java)
            startService(serviceIntent)
            statusText.text = "✅ Auto-refresh is RUNNING\nHutch app will be refreshed every 5 minutes."
        }

        stopButton.setOnClickListener {
            val serviceIntent = Intent(this, RefreshService::class.java)
            stopService(serviceIntent)
            statusText.text = "🛑 Auto-refresh is STOPPED"
        }
    }
}
