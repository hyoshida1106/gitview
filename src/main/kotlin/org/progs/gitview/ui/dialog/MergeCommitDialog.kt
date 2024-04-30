package org.progs.gitview.ui.dialog

import javafx.fxml.FXML
import javafx.scene.control.ButtonType
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import org.progs.gitview.MainApp.Companion.resourceBundle

class MergeCommitDialog(
    dstBranch: String
): CustomDialog<MergeCommitDialog.Control>(
    resourceBundle.getString("Message.MergeComment"),
    Control(dstBranch),
    ButtonType.OK, ButtonType.CANCEL
) {
    val message: String get() = controller.message.trim()

    class Control(
        private val dstBranch: String
    ): DialogControl() {
        @FXML private lateinit var dstBranchLabel: Label
        @FXML private lateinit var messageText: TextArea

        //初期化
        override fun initialize() {
            dstBranchLabel.text = resourceBundle.getString("Message.Merge.DstBranch").format(dstBranch)
        }

        //メッセージ
        val message: String get() = messageText.text
    }
}

