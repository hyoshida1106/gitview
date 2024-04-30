package org.progs.gitview.ui.alert

import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import org.progs.gitview.MainApp.Companion.resourceBundle
import org.progs.gitview.ui.dialog.DialogInterface

/**
 * ローカルブランチ削除警告ダイアログ
 */
class RemoveLocalBranchConfirm(
    private val branchName: String
) : DialogInterface<Boolean> {

    private val forceCheckBox = CheckBox(resourceBundle.getString("RemoveLocalBranchDialog.Force"))
    val forceRemove: Boolean get() = forceCheckBox.isSelected

    private val dialogPane = object: DialogPane() {
        override fun createButtonBar(): Node {
            val checkBox = forceCheckBox
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
        forceCheckBox.isDisable = false
        forceCheckBox.isSelected = false
    }

    override fun showDialog(): Boolean {
        val alert = createCustomAlert(
            type = Alert.AlertType.CONFIRMATION,
            message = resourceBundle.getString("Message.RemoveLocalBranchDialog").format(branchName),
            title = resourceBundle.getString("ConfirmationDialog.title"),
            headerText = null,
            buttons = arrayOf(ButtonType.OK, ButtonType.CANCEL),
            customDialogPane = dialogPane
        )
        val result = alert.showAndWait()
        return result.isPresent && result.get().buttonData == ButtonBar.ButtonData.OK_DONE
    }
}