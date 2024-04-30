package org.progs.gitview.ui.window.conflictfile

import javafx.application.Platform
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.util.Callback
import org.progs.gitview.MainApp.Companion.resourceBundle
import org.progs.gitview.git.commit.ConflictingFile
import org.progs.gitview.git.commit.ConflictingFile.State.*
import org.progs.gitview.model.RepositoryModel
import org.progs.gitview.ui.alert.ConfirmationType
import org.progs.gitview.ui.alert.confirm
import org.progs.gitview.ui.dialog.EditorSelectionDialog
import org.progs.gitview.ui.menu.LocalBranchOperations
import org.progs.gitview.ui.util.ColumnAdjuster
import org.progs.gitview.ui.util.IconButton
import org.progs.gitview.ui.util.IconCode
import org.progs.gitview.ui.window.BaseControl
import org.progs.gitview.ui.window.BaseWindow
import org.progs.gitview.ui.window.main.SystemConfig
import java.io.File


class ConflictingFileTablePane(
    repositoryModel: RepositoryModel,
    onSelectFile: (File?) -> Unit
): BaseWindow<ConflictingFileTablePane.Control>(Control(repositoryModel, onSelectFile)) {

    class Control(
        private val repositoryModel: RepositoryModel,
        private val onSelectFile: (File?) -> Unit
    ) : BaseControl() {
        @FXML private lateinit var mergeControlPane: AnchorPane
        @FXML private lateinit var cherryPickControlPane: AnchorPane
        @FXML private lateinit var conflictingFileTable: TableView<RowData>
        @FXML private lateinit var typeColumn: TableColumn<RowData, String>
        @FXML private lateinit var pathColumn: TableColumn<RowData, String>
        @FXML private lateinit var controlColumn: TableColumn<RowData, ConflictingFile>

        val commitListModel = repositoryModel.commitListModel

        /** 表示行データ */
        class RowData(
            val file: ConflictingFile
        ) {
            val type: String = when(file.state) {
                ADDED_BY_THEM -> "ADDED BY THEM"
                ADDED_BY_US -> "ADDED BY US"
                BOTH_ADDED -> "BOTH ADDED"
                BOTH_MODIFIED -> "BOTH MODIFIED"
                BOTH_DELETED -> "BOTH DELETED"
                DELETED_BY_THEM -> "DELETED BY THEM"
                DELETED_BY_US -> "DELETED BY US"
            }
            val path: String = file.path
        }

        /** テーブルのカラム幅を調整する処理クラス */
        private lateinit var fileTableAdjuster: ColumnAdjuster

        /** セルクラス */
        inner class Cell : TableCell<RowData, ConflictingFile>() {
            override fun updateItem(file: ConflictingFile?, empty: Boolean) {
                super.updateItem(file, empty)
                this.graphic = if (file == null || empty) null else
                    HBox(
                        IconButton(IconCode.EDIT_BOX_LINE).also {
                            it.onAction = EventHandler { editFile(file) }
                            it.tooltip = Tooltip(
                                resourceBundle.getString("Message.EditConflictingFile")) },
                        IconButton(IconCode.CHECKBOX_LINE).also {
                            it.onAction = EventHandler { markToFix(file) }
                            it.tooltip = Tooltip(
                                resourceBundle.getString("Message.MarkConflictingFileAsCorrect")) },
                        IconButton(IconCode.ARROW_TURN_BACK_LINE).also {
                            it.onAction = EventHandler { restore(file) }
                            it.tooltip = Tooltip(
                                resourceBundle.getString("Message.ResetConflictingFile")) }
                    ).apply { styleClass.add("hbox") }
                this.text = null
            }
        }

        /** ファイル編集 */
        private fun editFile(file: ConflictingFile) {
            //エディタ指定を取得、未定義ならばダイアログを表示して指定
            var editorPath = SystemConfig.editorPath.trim()
            if(editorPath.isEmpty()) {
                EditorSelectionDialog().showDialog()?.let { editor ->
                    editorPath = editor.absolutePath
                    SystemConfig.editorPath = editorPath
                } ?: return
            }
            //エディタを起動してファイル編集
            val editFile = repositoryModel.absoluteFile(file.path)
            ProcessBuilder(listOf(editorPath, editFile.absolutePath))
                .directory(File(repositoryModel.localRepositoryPath!!))
                .start()
            conflictingFileTable.selectionModel.clearSelection()
        }

        /** ファイルを修正済とマークする */
        private fun markToFix(file: ConflictingFile) {
            if(confirm(ConfirmationType.YesNo, resourceBundle.getString("Message.MarkAsCorrect"))) {
                repositoryModel.markAsCorrect(file)
                commitListModel.updateCommitList()
            }
        }

        /** ファイル変更を取り消す */
        private fun restore(file: ConflictingFile) {
            if(confirm(ConfirmationType.YesNo, resourceBundle.getString("Message.RestoreFile"))) {
                repositoryModel.restore(file)
                commitListModel.updateCommitList()
            }
        }

        /** 初期化 */
        fun initialize() {
            //テーブルの設定
            conflictingFileTable.placeholder = Label("")
            conflictingFileTable.selectionModel.selectionMode = SelectionMode.SINGLE
            typeColumn.cellValueFactory = PropertyValueFactory("type")
            pathColumn.cellValueFactory = PropertyValueFactory("path")
            controlColumn.cellValueFactory = Callback { row -> ReadOnlyObjectWrapper(row.value.file) }
            controlColumn.cellFactory = Callback { Cell() }

            //テーブル幅の調整
            fileTableAdjuster = ColumnAdjuster(conflictingFileTable, pathColumn)
        }

        /** 表示完了時の処理 */
        override fun displayCompleted() {

            //データ更新時の表示更新
            commitListModel.addWorkTreeFilesListener { _ ->
                Platform.runLater { updateContents() }
            }

            //ファイル選択時の処理
            conflictingFileTable.selectionModel.selectedItemProperty().addListener { _, _, item ->
                onSelectFile(item?.let { repositoryModel.absoluteFile(item.path) })
            }
        }

        /** 表示更新 */
        fun updateContents() {
            //マージ状態の表示
            val mergeId = repositoryModel.mergeInProgress
            if(mergeId != null) {
                mergeControlPane.children.setAll(
                    ConflictingMessagePane(
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
                    ConflictingMessagePane(
                        resourceBundle.getString("Message.CherryPickInProgress"),
                        cherryPickId
                    ) { cancelCherryPick() }.rootWindow
                )
            } else {
                cherryPickControlPane.children.clear()
            }

            //コンフリクト一覧表示リスト
            val files = commitListModel.workTreeFiles.conflictingFiles.map { file -> RowData(file) }

            //表示バッファの内容をテーブルのItemとして設定
            conflictingFileTable.itemsProperty().value = FXCollections.observableList(files)

            //選択解除
            conflictingFileTable.selectionModel.clearSelection()
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