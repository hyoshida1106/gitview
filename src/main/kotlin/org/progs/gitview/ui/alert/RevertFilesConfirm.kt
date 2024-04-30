package org.progs.gitview.ui.alert

import javafx.scene.control.Alert.AlertType
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import org.progs.gitview.MainApp.Companion.resourceBundle
import org.progs.gitview.ui.dialog.DialogInterface

/**
 * ファイルリバース確認ダイアログ
 */
class RevertFilesConfirm(
    private val files: List<String>
) : DialogInterface<Boolean> {

    override fun showDialog(): Boolean {
        val alert = createAlert(
            type = AlertType.CONFIRMATION,
            message = "",
            title = resourceBundle.getString("ConfirmationDialog.title"),
            headerText = null,
            buttons = arrayOf(ButtonType.OK, ButtonType.CANCEL),
        ).apply {
            dialogPane.content = VBox().apply {
                children.addFirst(Label(resourceBundle.getString("Message.RevertFilesDialog")))
                children.addAll(files.map { Label("  - %s".format(it)) })
            }
        }
        val result = alert.showAndWait()
        return result.isPresent && result.get().buttonData == ButtonBar.ButtonData.OK_DONE
    }

}