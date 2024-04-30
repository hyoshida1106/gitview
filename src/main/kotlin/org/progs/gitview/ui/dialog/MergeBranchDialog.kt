package org.progs.gitview.ui.dialog

import javafx.fxml.FXML
import javafx.scene.control.ButtonType
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import org.progs.gitview.MainApp.Companion.resourceBundle

class MergeBranchDialog(
    srcBranch: String,
    dstBranch: String
): CustomDialog<MergeBranchDialog.Control>(
    resourceBundle.getString("Message.MergeComment"),
    Control(srcBranch, dstBranch),
    ButtonType.OK, ButtonType.CANCEL
) {
    val message: String get() = controller.message.trim()

    class Control(
        private val srcBranch: String,
        private val dstBranch: String
    ): DialogControl() {
        @FXML private lateinit var srcBranchLabel: Label
        @FXML private lateinit var dstBranchLabel: Label
        @FXML private lateinit var messageText: TextArea

        //初期化
        override fun initialize() {
            srcBranchLabel.text = resourceBundle.getString("Message.Merge.SrcBranch").format(srcBranch)
            dstBranchLabel.text = resourceBundle.getString("Message.Merge.DstBranch").format(dstBranch)
        }

        //メッセージ
        val message: String get() = messageText.text
    }
}

