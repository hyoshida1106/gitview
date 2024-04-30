package org.progs.gitview.ui.alert

import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import org.progs.gitview.MainApp

class TagOutOfRangeAlert(
    private val tagName: String
) {
    fun show() {
        createAlert(
            type = Alert.AlertType.ERROR,
            message = MainApp.resourceBundle.getString("Message.TagOutOfRange")
                .format(tagName),
            title = MainApp.resourceBundle.getString("Title.TagOutOfRange"),
            buttons = arrayOf( ButtonType.OK )
        ).showAndWait()
    }
}