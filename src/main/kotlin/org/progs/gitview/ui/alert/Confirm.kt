package org.progs.gitview.ui.alert

import javafx.scene.control.ButtonType
import org.progs.gitview.MainApp
import javafx.scene.control.Alert.AlertType

enum class ConfirmationType { YesNo, OkCancel }

fun confirm(
    type: ConfirmationType,
    message: String
): Boolean {
    val buttonTypes: Array<ButtonType> = when (type) {
        ConfirmationType.YesNo -> arrayOf(ButtonType.YES, ButtonType.NO)
        ConfirmationType.OkCancel -> arrayOf(ButtonType.OK, ButtonType.CANCEL)
    }
    val title = MainApp.resourceBundle.getString("ConfirmationDialog.title")
    val result = createAlert(AlertType.CONFIRMATION, message, title, null, buttonTypes).showAndWait()
    return if(result.isPresent) result.get().buttonData == buttonTypes[0].buttonData else false
}