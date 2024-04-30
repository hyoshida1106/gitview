package org.progs.gitview.ui.alert

import javafx.scene.control.Alert.AlertType
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import org.progs.gitview.MainApp.Companion.resourceBundle
import org.progs.gitview.ui.dialog.DialogInterface

/**
 * タグ削除確認ダイアログ
 */
class RemoveTagConfirm(
    private val tagName: String
) : DialogInterface<Boolean> {

    override fun showDialog(): Boolean {
        val alert = createAlert(
            type = AlertType.CONFIRMATION,
            message = resourceBundle.getString("Message.RemoveTag").format(tagName),
            title = resourceBundle.getString("ConfirmationDialog.title"),
            headerText = "",
            buttons = arrayOf(ButtonType.OK, ButtonType.CANCEL),
        )
        val result = alert.showAndWait()
        return result.isPresent && result.get().buttonData == ButtonBar.ButtonData.OK_DONE
    }

}