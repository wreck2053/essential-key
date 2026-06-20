package com.wreck2053.essentialkey

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.net.Uri
import android.view.accessibility.AccessibilityManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var preferences: AppPreferences
    private val handler = Handler(Looper.getMainLooper())
    private val refreshTask = object : Runnable {
        override fun run() {
            refreshStatus()
            handler.postDelayed(this, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        preferences = AppPreferences(this)

        setupMethodSpinner(R.id.singleMethod)
        setupMethodSpinner(R.id.doubleMethod)
        setupMethodSpinner(R.id.longMethod)
        loadActions()

        findViewById<Button>(R.id.appInfoButton).setOnClickListener {
            startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                },
            )
        }
        findViewById<Button>(R.id.accessibilityButton).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        findViewById<Button>(R.id.detectButton).setOnClickListener {
            if (isServiceEnabled()) {
                preferences.learning = true
                findViewById<TextView>(R.id.keyDetails).setText(R.string.detect_waiting)
            } else {
                Toast.makeText(this, R.string.detect_requires_service, Toast.LENGTH_LONG).show()
            }
        }
        findViewById<Button>(R.id.saveButton).setOnClickListener {
            saveActions()
            Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(refreshTask)
    }

    override fun onPause() {
        handler.removeCallbacks(refreshTask)
        super.onPause()
    }

    private fun setupMethodSpinner(id: Int) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("GET", "POST"))
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        findViewById<Spinner>(id).adapter = adapter
    }

    private fun loadActions() {
        bindAction(PressAction.SINGLE, R.id.singleMethod, R.id.singleUrl)
        bindAction(PressAction.DOUBLE, R.id.doubleMethod, R.id.doubleUrl)
        bindAction(PressAction.LONG, R.id.longMethod, R.id.longUrl)
    }

    private fun bindAction(action: PressAction, methodId: Int, urlId: Int) {
        val config = preferences.loadAction(action)
        findViewById<Spinner>(methodId).setSelection(if (config.method == "POST") 1 else 0)
        findViewById<EditText>(urlId).setText(config.url)
    }

    private fun saveActions() {
        saveAction(PressAction.SINGLE, R.id.singleMethod, R.id.singleUrl)
        saveAction(PressAction.DOUBLE, R.id.doubleMethod, R.id.doubleUrl)
        saveAction(PressAction.LONG, R.id.longMethod, R.id.longUrl)
    }

    private fun saveAction(action: PressAction, methodId: Int, urlId: Int) {
        preferences.saveAction(
            action,
            ActionConfig(
                method = findViewById<Spinner>(methodId).selectedItem.toString(),
                url = findViewById<EditText>(urlId).text.toString(),
            ),
        )
    }

    private fun refreshStatus() {
        val serviceEnabled = isServiceEnabled()
        findViewById<TextView>(R.id.serviceStatus).setText(
            if (serviceEnabled) R.string.service_enabled else R.string.service_disabled,
        )
        findViewById<Button>(R.id.detectButton).isEnabled = serviceEnabled
        if (!serviceEnabled && preferences.learning) preferences.learning = false
        if (preferences.learning) {
            findViewById<TextView>(R.id.keyDetails).setText(R.string.detect_waiting)
        } else {
            findViewById<TextView>(R.id.keyDetails).text =
                preferences.loadKey()?.displayText() ?: getString(R.string.no_button)
        }
        showResult(PressAction.SINGLE, R.id.singleResult)
        showResult(PressAction.DOUBLE, R.id.doubleResult)
        showResult(PressAction.LONG, R.id.longResult)
    }

    private fun showResult(action: PressAction, viewId: Int) {
        findViewById<TextView>(viewId).text = preferences.loadResult(action)?.let { "Last result: $it" }
            ?: getString(R.string.last_result_none)
    }

    private fun isServiceEnabled(): Boolean {
        val manager = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        return manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            .any { it.resolveInfo.serviceInfo.packageName == packageName && it.resolveInfo.serviceInfo.name == KeyAccessibilityService::class.java.name }
    }
}
