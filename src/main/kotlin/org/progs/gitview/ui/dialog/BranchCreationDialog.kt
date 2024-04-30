package org.progs.gitview.ui.dialog

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.fxml.FXML
import javafx.scene.control.ButtonType
import javafx.scene.control.CheckBox
import javafx.scene.control.TextField
import org.progs.gitview.MainApp.Companion.resourceBundle


class BranchCreationDialog: CustomDialog<BranchCreationDialog.Control>(
    resourceBundle.getString("CreateBranchByNameDialog.Title"),
    Control(),
    ButtonType.OK, ButtonType.CANCEL
) {
    init {
        addButtonHandler(ButtonType.OK, controller.btnOkDisable)
    }

    class Control : DialogControl() {
        @FXML private lateinit var branchName: TextField
        @FXML private lateinit var checkout: CheckBox

        /**
         *  OKボタンの無効を指示するプロパティ
         */
        val btnOkDisable = SimpleBooleanProperty(true)

        /**
         * 設定されたブランチ名称を参照する
         */
        val newBranchName get() = branchName.text.trim()

        /**
         *  チェックアウトフラグの設定結果を参照する
         */
        val checkoutFlag get() = checkout.isSelected

        /**
         * 初期化
         */
        override fun initialize() {
            checkout.isSelected = true
            branchName.text = ""
            branchName.lengthProperty().addListener { _ ->
                btnOkDisable.value = branchName.text.isBlank()
            }
            Platform.runLater {
                branchName.requestFocus()
            }
        }
    }
}
