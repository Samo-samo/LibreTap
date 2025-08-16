package com.example.libretap.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent

class AutoClickService : AccessibilityService() {

    private val handler by lazy { Handler(Looper.getMainLooper()) }
    @Volatile private var isClicking: Boolean = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance === this) instance = null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) { /* no-op */ }

    override fun onInterrupt() { /* no-op */ }

    /**
     * Seçilen noktaya [times] kez, her tıklama arası [delayMs] bekleyerek tıklar.
     */
    fun startClicking(x: Float, y: Float, times: Int, delayMs: Long) {
        if (isClicking) return
        if (times <= 0) return
        isClicking = true
        var remaining = times

        fun oneClick() {
            if (!isClicking || remaining <= 0) { isClicking = false; return }
            performClick(x, y) {
                remaining--
                if (remaining > 0 && isClicking) {
                    handler.postDelayed({ oneClick() }, delayMs)
                } else {
                    isClicking = false
                }
            }
        }
        oneClick()
    }

    fun stopClicking() { isClicking = false }

    /**
     * Tek bir tıklama enjekte eder. İş bittiğinde [onDone] çağrılır.
     */
    fun performClick(x: Float, y: Float, onDone: (() -> Unit)? = null) {
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 50) // 50ms kısa dokunuş
        val gesture = GestureDescription.Builder().addStroke(stroke).build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                onDone?.invoke()
            }
            override fun onCancelled(gestureDescription: GestureDescription?) {
                onDone?.invoke()
            }
        }, null)
    }

    companion object {
        @JvmStatic
        var instance: AutoClickService? = null
            private set
    }
}
