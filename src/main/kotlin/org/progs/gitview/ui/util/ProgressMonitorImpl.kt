package org.progs.gitview.ui.util

import javafx.application.Platform
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import org.eclipse.jgit.lib.ProgressMonitor


/**
 * 処理経過を表示するプログレスモニタ
 */
class ProgressMonitorImpl: ProgressMonitor {

    val titleProperty = SimpleStringProperty("")
    val title get() = titleProperty.value ?: ""

    val scaleProperty = SimpleIntegerProperty(0)
    val scale get() = scaleProperty.value ?: 0

    val valueProperty = SimpleIntegerProperty(0)
    val value get() = valueProperty.value ?: 0

    var cancel = false

    override fun start(p0: Int) {
    }

    override fun showDuration(p0: Boolean) {
    }

    override fun beginTask(title: String?, totalWork: Int) {
        Platform.runLater {
            titleProperty.value = title
            scaleProperty.value = totalWork
            valueProperty.value = 0
        }
    }

    override fun endTask() {
    }

    override fun update(completed: Int) {
        valueProperty.value += completed
    }

    override fun isCancelled(): Boolean {
        return cancel
    }

}