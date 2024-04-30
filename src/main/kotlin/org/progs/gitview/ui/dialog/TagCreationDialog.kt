package org.progs.gitview.ui.dialog

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.fxml.FXML
import javafx.scene.control.ButtonType
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import org.progs.gitview.MainApp.Companion.resourceBundle

class TagCreationDialog(
): CustomDialog<TagCreationDialog.Control>(
    resourceBundle.getString("TagCreationDialog.Title"),
    Control(),
    ButtonType.OK, ButtonType.CANCEL
) {
    init {
        addButtonHandler(ButtonType.OK, controller.btnOkDisable)
    }

    val tagName get() = controller.enteredTagName
    val message get() = controller.enteredMessage

    class Control : DialogControl() {
        @FXML private lateinit var tagName: TextField
        @FXML private lateinit var message: TextArea

        /** OKボタンの無効を指示するプロパティ */
        val btnOkDisable = SimpleBooleanProperty(true)

        /** 設定されたタグ名称を参照する */
        val enteredTagName: String get() = tagName.text.trim()

        /** 設定されたメッセージ文字列を参照する */
        val enteredMessage: String get() = message.text

        /** 初期化 */
        override fun initialize() {
            tagName.lengthProperty().addListener { _ -> btnOkDisable.value = enteredTagName.isEmpty() }
            Platform.runLater { tagName.requestFocus() }
        }
    }
}