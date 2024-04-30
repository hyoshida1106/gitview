package org.progs.gitview.ui.util

import javafx.animation.Animation.INDEFINITE
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.event.Event
import javafx.stage.Stage
import javafx.util.Duration

/**
 * Animation Timelineを使用したアイドル時間タイマ
 */
class IdleTimer(
    private val stage: Stage,
    idleTime: Int,
    repeat: Boolean,
    val handler: () -> Unit
) {
    private val idleTimeline: Timeline = Timeline(
        KeyFrame(Duration(idleTime.toDouble()), { handler() })
    )

    init {
        idleTimeline.cycleCount = if (repeat) INDEFINITE else 1
        stage.scene.addEventFilter(Event.ANY) { resetTimer() }
        stage.focusedProperty().addListener { _, _, _ -> resetTimer() }
        resetTimer()
    }

    private fun resetTimer() {
        if (stage.isFocused) {
            idleTimeline.playFromStart()
        } else {
            idleTimeline.stop()
        }
    }
}
