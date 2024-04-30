package org.progs.gitview.ui.alert

import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import org.progs.gitview.MainApp

class PatchCompleteAlert(
    private val fileName: String
) {
    fun show() {
        createAlert(
            type = Alert.AlertType.INFORMATION,
            message = MainApp.resourceBundle.getString("Message.PatchFile.Complete")
                .format(fileName),
            title = MainApp.resourceBundle.getString("Message.PatchFile.Title"),
            buttons = arrayOf( ButtonType.OK )
        ).showAndWait()
    }
}