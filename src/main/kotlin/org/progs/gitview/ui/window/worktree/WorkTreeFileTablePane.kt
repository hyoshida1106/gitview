package org.progs.gitview.ui.window.worktree

import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.HBox
import javafx.util.Callback
import org.progs.gitview.git.commit.CommitFile
import org.progs.gitview.ui.util.ColumnAdjuster
import org.progs.gitview.ui.util.IconButton
import org.progs.gitview.ui.util.typeLabel
import org.progs.gitview.ui.window.BaseControl
import org.progs.gitview.ui.window.BaseWindow


interface WorkTreeFileTablePaneOperations {
    /** 選択ファイル */
    val selectedFiles:List<CommitFile>
    /** 表示データ設定 */
    fun updateContents(files: List<CommitFile>)
}

class WorkTreeFileTablePane(
    control: Control
): BaseWindow<WorkTreeFileTablePane.Control>(control), WorkTreeFileTablePaneOperations by control {

    constructor(
        title: String,
        actionIconCode: Int?,
        actionName: String,
        actionCb: (List<CommitFile>, List<Int>) -> Unit,
        onFileSelect: (fileInfo: CommitFile?) -> Unit
    ): this(Control(title, actionIconCode, actionName, actionCb, onFileSelect))

    /** 表示行データ */
    class RowData(
        val file: CommitFile
    ) {
        val typeLabel get() = file.typeLabel
        val path = file.path
    }

    class Control(
        private val title: String,
        actionIconCode: Int?,
        action: String,
        private val actionCb: (List<CommitFile>, List<Int>) -> Unit,
        private val onFileSelect: (fileInfo: CommitFile?) -> Unit
    ): BaseControl(), WorkTreeFileTablePaneOperations {
        @FXML private lateinit var fileTable: TableView<RowData>
        @FXML private lateinit var typeColumn: TableColumn<RowData, Node>
        @FXML private lateinit var pathColumn: TableColumn<RowData, String>
        @FXML private lateinit var titleLabel: Label
        @FXML private lateinit var buttonPane: HBox

        private val actionButton = IconButton(actionIconCode, action).apply {
            maxWidth = Double.POSITIVE_INFINITY
            styleClass.add("ActionButton")
        }

        /** 選択ファイル */
        override val selectedFiles:List<CommitFile>
            get() = fileTable.selectionModel.selectedItems.map { item -> item.file }

        /** テーブルのカラム幅を調整する処理クラス */
        private lateinit var fileTableAdjuster: ColumnAdjuster

        /** セルクラス */
        class Cell : TableCell<RowData, Node>() {
            override fun updateItem(label: Node?, empty: Boolean) {
                super.updateItem(label, empty)
                this.graphic = if(label != null && !empty ) label else null
                this.text = null
            }
        }

        /** 初期化 */
        fun initialize() {
            titleLabel.text = title

            fileTable.placeholder = Label("")
            fileTable.selectionModel.selectionMode = SelectionMode.MULTIPLE
            typeColumn.cellValueFactory = Callback { row -> ReadOnlyObjectWrapper(row.value.typeLabel) }
            typeColumn.cellFactory = Callback { Cell() }
            pathColumn.cellValueFactory = PropertyValueFactory("path")
            buttonPane.children.add(actionButton)

            //テーブル幅の調整
            fileTableAdjuster = ColumnAdjuster(fileTable, pathColumn)
        }

        /** 表示完了時の処理 */
        override fun displayCompleted() {

            //テーブル幅の調整
            fileTableAdjuster.adjustColumnWidth()

            //ファイル選択時の処理
            fileTable.selectionModel.selectedItemProperty().addListener { _, _, model ->
                onFileSelect(model?.file)
            }

            //ボタン押下時のコールバック
            actionButton.onAction = EventHandler {
                val files = fileTable.items.map { row -> row.file }
                val selectedIndices = fileTable.selectionModel.selectedIndices.toList()
                actionCb(files, selectedIndices)
            }
        }

        /** 表示データ設定 */
        override fun updateContents(
            files: List<CommitFile>
        ) {
            fileTable.selectionModel.clearSelection()
            fileTable.itemsProperty().value = FXCollections.observableList(
                files.sortedBy { it.path }.map { model -> RowData(model) })
            actionButton.isDisable = files.isEmpty()
        }

    }
}