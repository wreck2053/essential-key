package com.wreck2053.essentialkey

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import java.time.Instant
import java.util.concurrent.Executors

class KeyAccessibilityService : AccessibilityService() {
    private lateinit var preferences: AppPreferences
    private lateinit var classifier: GestureClassifier
    private val networkExecutor = Executors.newSingleThreadExecutor()
    private val requestExecutor = HttpRequestExecutor()

    override fun onServiceConnected() {
        preferences = AppPreferences(this)
        serviceInfo = serviceInfo.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        }
        classifier = GestureClassifier(
            scheduler = HandlerScheduler(Handler(Looper.getMainLooper())),
            onAction = ::executeAction,
        )
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (!::preferences.isInitialized) return false
        val identity = event.toIdentity()

        if (preferences.learning) {
            if (event.action == KeyEvent.ACTION_UP && !event.isCanceled) {
                preferences.saveKey(identity)
                classifier.reset()
            }
            return true
        }

        val mapped = preferences.loadKey() ?: return false
        if (!mapped.matches(identity)) return false

        when (event.action) {
            KeyEvent.ACTION_DOWN -> classifier.onKeyDown(event.repeatCount)
            KeyEvent.ACTION_UP -> classifier.onKeyUp(event.isCanceled)
        }
        return true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() {
        if (::classifier.isInitialized) classifier.reset()
    }

    override fun onDestroy() {
        if (::classifier.isInitialized) classifier.reset()
        networkExecutor.shutdownNow()
        super.onDestroy()
    }

    private fun executeAction(action: PressAction) {
        val config = preferences.loadAction(action)
        networkExecutor.execute {
            val result = requestExecutor.execute(config)
            val status = if (result.statusCode >= 0) {
                "HTTP ${result.statusCode} ${result.message}".trim()
            } else {
                "Error: ${result.message}"
            }
            preferences.saveResult(action, "${Instant.now()} — $status")
        }
    }

    private fun KeyEvent.toIdentity(): KeyIdentity {
        val inputDevice = device
        return KeyIdentity(
            keyCode = keyCode,
            scanCode = scanCode,
            source = source,
            deviceId = deviceId,
            descriptor = inputDevice?.descriptor.orEmpty(),
            vendorId = inputDevice?.vendorId ?: 0,
            productId = inputDevice?.productId ?: 0,
        )
    }

    private class HandlerScheduler(private val handler: Handler) : GestureClassifier.Scheduler {
        override fun schedule(delayMs: Long, task: () -> Unit): GestureClassifier.Cancellable {
            val runnable = Runnable(task)
            handler.postDelayed(runnable, delayMs)
            return object : GestureClassifier.Cancellable {
                override fun cancel() {
                    handler.removeCallbacks(runnable)
                }
            }
        }
    }
}
