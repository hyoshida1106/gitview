package org.progs.gitview.ui.window

import javafx.beans.binding.Bindings
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.progs.gitview.ui.util.ProgressMonitorImpl

class ProgressWindow(
    val monitor: ProgressMonitorImpl = ProgressMonitorImpl()
): BaseWindow<ProgressWindow.Control>(Control(monitor)) {

    private val stage: Stage = Stage(StageStyle.UTILITY)

    init {
        stage.title = "GitView"
        stage.scene = Scene(rootWindow)
        stage.initModality(Modality.APPLICATION_MODAL)
        stage.onCloseRequest = EventHandler { it.consume() }        // "X"で閉じないようにする
    }

    fun show() = stage.show()
    fun hide() = stage.hide()

    class Control(private val monitor: ProgressMonitorImpl): BaseControl() {
        @FXML private lateinit var message: Label
        @FXML private lateinit var progressBar: ProgressBar
        @FXML private lateinit var cancelButton: Button

        fun initialize() {
            message.textProperty().bind(monitor.titleProperty)
            progressBar.progressProperty().bind(
                Bindings.createDoubleBinding(
                    { if (monitor.scale > 0) monitor.value.toDouble() / monitor.scale.toDouble() else 0.0 },
                    monitor.scaleProperty,
                    monitor.valueProperty
                ))
            cancelButton.setOnAction { onCancel() }
        }

        private fun onCancel() {
            monitor.cancel = true
            cancelButton.isDisable = true
        }
    }
}