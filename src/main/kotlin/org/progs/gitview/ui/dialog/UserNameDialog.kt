package org.progs.gitview.ui.dialog

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ChangeListener
import javafx.fxml.FXML
import javafx.scene.control.ButtonType
import javafx.scene.control.TextField
import org.progs.gitview.MainApp.Companion.resourceBundle

class UserNameDialog(
    userName: String,
    mailAddr: String
): CustomDialog<UserNameDialog.Control>(
    resourceBundle.getString("UserNameDialog.Title"),
    Control(userName, mailAddr),
    ButtonType.OK, ButtonType.CANCEL
) {
    init {
        addButtonHandler(ButtonType.OK, controller.btnOkDisable)
    }
    val userName: String get() = controller.userName
    val mailAddr: String get() = controller.mailAddr

    class Control(
        private val userNameStr: String,
        private val mailAddrStr: String
    ) : DialogControl() {
        @FXML private lateinit var userNameText: TextField
        @FXML private lateinit var mailAddrText: TextField

        val userName: String get() = userNameText.text.trim()
        val mailAddr: String get() = mailAddrText.text.trim()

        /**
         *  OKボタンの無効を指示するプロパティ
         */
        val btnOkDisable = SimpleBooleanProperty(true)

        /**
         * 文字列長を監視するリスナ
         */
        private val textInputListener = ChangeListener<Number>{ _, _, _ ->
            btnOkDisable.value = userName.isEmpty() || mailAddr.isEmpty()
        }

        /**
         * 初期化
         */
        override fun initialize() {
            userNameText.text = userNameStr
            mailAddrText.text = mailAddrStr

            userNameText.textProperty().length().addListener(textInputListener)
            mailAddrText.textProperty().length().addListener(textInputListener)

            Platform.runLater { userNameText.requestFocus() }
        }
    }
}