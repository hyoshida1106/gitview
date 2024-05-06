package org.progs.gitview.ui.alert

import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.ButtonType
import javafx.scene.control.DialogPane
import org.progs.gitview.ui.window.main.mainWindow

fun createAlert(
    type: AlertType,
    message: String,
    title: String,
    headerText: String? = null,
    buttons: Array<ButtonType>
) = Alert(type).apply {
        initOwner(mainWindow.rootWindow.scene.window)
        setTitle(title)
        setHeaderText(headerText)
        dialogPane.contentText = message
        dialogPane.buttonTypes.setAll(buttons.asList())
    }


fun createCustomAlert(
    type: AlertType,
    message: String,
    title: String,
    headerText: String? = null,
    buttons: Array<ButtonType>,
    customDialogPane: DialogPane
) = Alert(type).apply {
        val lastStyleClass = dialogPane.styleClass
        initOwner(mainWindow.rootWindow.scene.window)
        setTitle(title)
        setHeaderText(headerText)
        dialogPane = customDialogPane.apply {
            contentText = message
            buttonTypes.addAll(buttons)
            styleClass.setAll(lastStyleClass)
            applyCss()
            javaClass.getResource("/alert/Alert.css")?.toExternalForm()?.let {
                stylesheets.add(it)
            }
        }
    }