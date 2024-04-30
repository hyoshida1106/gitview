package org.progs.gitview.ui.dialog

import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.ButtonType
import javafx.scene.control.TextField
import javafx.beans.property.SimpleBooleanProperty
import org.progs.gitview.MainApp.Companion.resourceBundle

class RenameBranchDialog(
    private val lastBranchName: String
): CustomDialog<RenameBranchDialog.Control>(
    resourceBundle.getString("RenameBranchDialog.Title"),
    Control(),
    ButtonType.OK, ButtonType.CANCEL
) {
    init {
        addButtonHandler(ButtonType.OK, controller.btnOkDisable)
    }

    override fun showDialog(): ButtonType? {
        controller.newBranchName = lastBranchName
        return super.showDialog()
    }

    val branchName get() = controller.newBranchName
    class Control : DialogControl() {
        @FXML private lateinit var branchName: TextField

         /** OKボタンの無効を指示するプロパティ */
        val btnOkDisable = SimpleBooleanProperty(true)

         /** 設定されたブランチ名称を参照する */
        var newBranchName: String
            get() = branchName.text.trim()
            set(value) { branchName.text = value }

         /** 初期化 */
        override fun initialize() {
            branchName.lengthProperty().addListener { _ -> btnOkDisable.value = newBranchName.isEmpty() }
            Platform.runLater { branchName.requestFocus() }
        }
    }
}