package org.progs.gitview.ui.alert

import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import org.progs.gitview.MainApp.Companion.resourceBundle
import org.progs.gitview.ui.dialog.DialogInterface

/**
 * チェリーピック実行確認ダイアログ
 */
class CherryPickConfirm(
    private val currentBranch: String
) : DialogInterface<Boolean> {

    private val doCommitCheckBox = CheckBox(resourceBundle.getString("CherryPickDialog.DoCommit"))
    val doCommit: Boolean get() = doCommitCheckBox.isSelected

    private val dialogPane = object: DialogPane() {
        override fun createButtonBar(): Node {
            val checkBox = doCommitCheckBox
            val buttonBar = super.createButtonBar()
            HBox.setHgrow(checkBox, Priority.ALWAYS)
            HBox.setHgrow(buttonBar, Priority.NEVER)
            checkBox.maxWidth = Double.MAX_VALUE
            val hBox = HBox(5.0, checkBox, buttonBar)
            hBox.padding = Insets(10.0, 10.0, 10.0, 30.0)
            return hBox
        }
    }

    init {
        doCommitCheckBox.isDisable = false
        doCommitCheckBox.isSelected = true
    }

    override fun showDialog(): Boolean {
        val alert = createCustomAlert(
            type = Alert.AlertType.CONFIRMATION,
            message = resourceBundle.getString("Message.CherryPick").format(currentBranch),
            title = resourceBundle.getString("ConfirmationDialog.title"),
            headerText = null,
            buttons = arrayOf(ButtonType.OK, ButtonType.CANCEL),
            customDialogPane = dialogPane
        )
        val result = alert.showAndWait()
        return result.isPresent && result.get().buttonData == ButtonBar.ButtonData.OK_DONE
    }
}