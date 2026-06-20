package com.wreck2053.essentialkey

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.wreck2053.essentialkey.data.SettingsRepository
import com.wreck2053.essentialkey.domain.AppSettings
import com.wreck2053.essentialkey.domain.KeyIdentity
import com.wreck2053.essentialkey.domain.PressAction
import com.wreck2053.essentialkey.haptics.HapticEngine
import java.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KeyAccessibilityService : AccessibilityService() {
    private lateinit var repository: SettingsRepository
    private lateinit var hapticEngine: HapticEngine
    private lateinit var classifier: GestureClassifier
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val requestExecutor = HttpRequestExecutor()
    @Volatile private var currentSettings = AppSettings()

    override fun onServiceConnected() {
        val container = (application as EssentialKeyApplication).container
        repository = container.repository
        hapticEngine = container.hapticEngine
        serviceInfo = serviceInfo.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        }
        classifier = GestureClassifier(
            scheduler = HandlerScheduler(Handler(Looper.getMainLooper())),
            onAction = ::executeAction,
        )
        serviceScope.launch {
            repository.settings.collectLatest { currentSettings = it }
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (!::repository.isInitialized) return false
        val identity = event.toIdentity()

        if (currentSettings.learning) {
            if (event.action == KeyEvent.ACTION_UP && !event.isCanceled) {
                currentSettings = currentSettings.copy(mappedKey = identity, learning = false)
                serviceScope.launch { repository.saveMappedKey(identity) }
                classifier.reset()
            }
            return true
        }

        val mapped = currentSettings.mappedKey ?: return false
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
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun executeAction(action: PressAction) {
        val config = currentSettings.actions.getValue(action)
        hapticEngine.perform(config.hapticStrength)
        if (config.url.isBlank()) {
            serviceScope.launch {
                repository.saveResult(action, "${Instant.now()} — HTTP disabled")
            }
            return
        }
        serviceScope.launch {
            val result = withContext(Dispatchers.IO) { requestExecutor.execute(config) }
            val status = if (result.statusCode >= 0) {
                "HTTP ${result.statusCode} ${result.message}".trim()
            } else {
                "Error: ${result.message}"
            }
            repository.saveResult(action, "${Instant.now()} — $status")
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
