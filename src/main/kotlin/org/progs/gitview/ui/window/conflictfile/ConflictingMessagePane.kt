package org.progs.gitview.ui.window.conflictfile

import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label
import org.progs.gitview.MainApp.Companion.resourceBundle
import org.progs.gitview.git.commit.Id
import org.progs.gitview.ui.window.BaseControl
import org.progs.gitview.ui.window.BaseWindow

class ConflictingMessagePane(
    message: String,
    id: Id,
    onRevert: () -> Unit
): BaseWindow<ConflictingMessagePane.Control>(Control(message, id, onRevert)) {

    class Control(
        private val message: String,
        private val id: Id,
        private val onRevert: () -> Unit
    ): BaseControl() {
        @FXML private lateinit var conflictingMessage: Label
        @FXML private lateinit var conflictingId: Label
        @FXML private lateinit var revertButton: Button

        /** 初期化 */
        fun initialize() {
            conflictingMessage.text = message
            conflictingId.text = resourceBundle.getString("Message.CommitID").format(Id.toString(id))
            revertButton.onAction = EventHandler { _ -> onRevert() }
        }
    }
}