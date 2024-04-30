package org.progs.gitview.ui.window.worktree

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import org.progs.gitview.MainApp.Companion.resourceBundle
import org.progs.gitview.git.commit.CommitFile
import org.progs.gitview.git.commit.WorkTreeFiles
import org.progs.gitview.model.RepositoryModel
import org.progs.gitview.ui.alert.ConfirmationType
import org.progs.gitview.ui.alert.confirm
import org.progs.gitview.ui.menu.LocalBranchOperations
import org.progs.gitview.ui.menu.WorkTreeOperations
import org.progs.gitview.ui.util.IconButton
import org.progs.gitview.ui.util.IconCode
import org.progs.gitview.ui.window.BaseControl
import org.progs.gitview.ui.window.BaseWindow


class WorkTreeFileListPane(
    repositoryModel: RepositoryModel,
    onFileSelect: (fileInfo: CommitFile?) -> Unit = { }
): BaseWindow<WorkTreeFileListPane.Control>(Control(repositoryModel, onFileSelect)) {

    class Control(
        private val repositoryModel: RepositoryModel,
        private val onFileSelect: (fileInfo: CommitFile?) -> Unit
    ) : BaseControl() {
        @FXML private lateinit var mergeControlPane: AnchorPane
        @FXML private lateinit var cherryPickControlPane: AnchorPane
        @FXML private lateinit var fileListPane: HBox
        @FXML private lateinit var stagedFilesList: AnchorPane
        @FXML private lateinit var commandPane: BorderPane
        @FXML private lateinit var commandBox: VBox
        @FXML private lateinit var modifiedFilesList: AnchorPane

        private val workTreeOperations = WorkTreeOperations(repositoryModel)

        private val stageButton = commandButton(IconCode.ARROW_LEFT_FILL,"Term.Stage", null)
        private val unStageButton = commandButton(null,"Term.UnStage", IconCode.ARROW_RIGHT_FILL)

        /** ステージ/アンステージボタン用Buttonインスタンス */
        private fun commandButton(
            leftIcon: Int?,
            textCode: String,
            rightIcon: Int?
        ): Button {
            return IconButton(leftIcon, resourceBundle.getString(textCode), rightIcon).apply {
                maxWidth = Double.POSITIVE_INFINITY
            }
        }

        /** "Staged"ファイル一覧 */
        private val stagedFilesTable = WorkTreeFileTablePane(
            resourceBundle.getString("Term.StagedFiles"),
            IconCode.GIT_COMMIT_LINE,
            resourceBundle.getString("Term.Commit"),
            { files, indices -> workTreeOperations.commit(files, indices) },
            { file -> onStagedFileSelect(file) }
        )

        /** StagedFile選択時処理 */
        private fun onStagedFileSelect(file: CommitFile?) {
            updateButtonStatus()
            onFileSelect(file)
        }

        /** "Modified"ファイル一覧 */
        private val modifiedFilesTable = WorkTreeFileTablePane(
            resourceBundle.getString("Term.ModifiedFiles"),
            IconCode.ARROW_TURN_BACK_LINE,
            resourceBundle.getString("Term.Revert"),
            { files, indices -> workTreeOperations.revert(files, indices) },
            { file -> onModifiedFileSelect(file) }
        )

        /** ModifiedFile選択時処理 */
        private fun onModifiedFileSelect(file: CommitFile?) {
            updateButtonStatus()
            onFileSelect(file)
        }


        /** 初期化 */
        fun initialize() {
            stagedFilesList.children.addAll(stagedFilesTable.rootWindow)
            modifiedFilesList.children.addAll(modifiedFilesTable.rootWindow)
            commandBox.children.addAll(
                stageButton,
                unStageButton
            )
        }

        /** 表示完了時の処理 */
        override fun displayCompleted() {
            //幅変更時の配置修正
            fileListPane.widthProperty().addListener { _, _, width ->
                val paneWidth = width.toDouble()
                val commandWidth = commandPane.width
                val listWidth = (paneWidth - commandWidth) / 2.0 - 1.0
                stagedFilesList.prefWidth = listWidth
                modifiedFilesList.prefWidth = /* paneWidth - commandWidth - */ listWidth
            }

            //データ更新時の表示更新
            repositoryModel.commitListModel.addWorkTreeFilesListener { files ->
                Platform.runLater { updateContents(files) } }

            //ファイルチェックに対応してボタンの有効/無効を設定する
            updateButtonStatus()

            //ボタン押下時の処理
            stageButton.onAction = EventHandler { _ ->
                workTreeOperations.stage( modifiedFilesTable.controller.selectedFiles) }
            unStageButton.onAction = EventHandler { _ ->
                workTreeOperations.unStage(stagedFilesTable.controller.selectedFiles) }
        }

        /** 表示更新 */
        private fun updateContents(files: WorkTreeFiles) {
            //マージ状態の表示
            val mergeId = repositoryModel.mergeInProgress
            if(mergeId != null) {
                mergeControlPane.children.setAll(
                    WorkTreeMessagePane(
                        resourceBundle.getString("Message.MergeInProgress"),
                        mergeId
                    ) { cancelMerge() }.rootWindow
                )
            } else {
                mergeControlPane.children.clear()
            }

            //チェリーピック状態の表示
            val cherryPickId = repositoryModel.cherryPickInProgress
            if(cherryPickId != null) {
                cherryPickControlPane.children.setAll(
                    WorkTreeMessagePane(
                        resourceBundle.getString("Message.CherryPickInProgress"),
                        cherryPickId
                    ) { cancelCherryPick() }.rootWindow
                )
            } else {
                cherryPickControlPane.children.clear()
            }

            stagedFilesTable.controller.updateContents(files.stagedFiles)
            modifiedFilesTable.controller.updateContents(files.modifiedFiles)
            updateButtonStatus()
        }

        /** ボタン有効/無効状態の変更 */
        private fun updateButtonStatus() {
            unStageButton.isDisable = stagedFilesTable.controller.selectedFiles.isEmpty()
            stageButton.isDisable = modifiedFilesTable.controller.selectedFiles.isEmpty()
        }

        /** 進行中のマージをキャンセルする */
        private fun cancelMerge() {
            if(confirm(ConfirmationType.YesNo, resourceBundle.getString("Message.AbortMerge"))) {
                LocalBranchOperations.cancelMerge(repositoryModel)
            }
        }

        /** 進行中のチェリーピックをキャンセルする */
        private fun cancelCherryPick() {
            if(confirm(ConfirmationType.YesNo, resourceBundle.getString("Message.AbortCherryPick"))) {
                LocalBranchOperations.cancelCherryPick(repositoryModel)
            }
        }

    }
}