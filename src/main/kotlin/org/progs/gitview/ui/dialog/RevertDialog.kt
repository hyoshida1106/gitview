package org.progs.gitview.ui.dialog

import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.ListChangeListener
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.*
import javafx.util.Callback
import org.progs.gitview.MainApp.Companion.resourceBundle
import org.progs.gitview.git.commit.CommitFile
import org.progs.gitview.ui.util.ColumnAdjuster
import org.progs.gitview.ui.util.typeLabel


class RevertDialog(
    files: List<CommitFile>,
    selectedIndices: List<Int>
): CustomDialog<RevertDialog.Control>(
    resourceBundle.getString("Message.RevertFiles"),
    Control(files, selectedIndices),
    ButtonType.OK, ButtonType.CANCEL
) {
    /** 表示行データ */
    class RowData(
        val file: CommitFile
    ) {
        val typeLabel get() = file.typeLabel
        val pathLabel get() = Label(file.path)
    }

    /** 初期化 */
    init {
        addButtonHandler(ButtonType.OK, controller.btnOkDisable)
    }

    /** コントロールクラス */
    class Control(
        private val files: List<CommitFile>,
        private val selectedIndices: List<Int>
    ): DialogControl() {

        @FXML private lateinit var revertFileTable: TableView<RowData>
        @FXML private lateinit var typeColumn: TableColumn<RowData, Node>
        @FXML private lateinit var pathColumn: TableColumn<RowData, Node>

        /** OKボタンの無効を指示するプロパティ */
        val btnOkDisable = SimpleBooleanProperty(true)

        /** セルクラス */
        class Cell : TableCell<RowData, Node>() {
            override fun updateItem(label: Node?, empty: Boolean) {
                super.updateItem(label, empty)
                this.graphic = if(label != null && !empty ) label else null
                this.text = null
            }
        }

        /** 選択されたファイル */
        val selectedFiles: List<CommitFile> get() =
            revertFileTable.selectionModel.selectedItems.map { it.file }

        /** テーブルのカラム幅を調整する処理クラス */
        private lateinit var fileTableAdjuster: ColumnAdjuster

        /** 初期化 */
        override fun initialize() {
            revertFileTable.placeholder = Label("")
            revertFileTable.selectionModel.selectionMode = SelectionMode.MULTIPLE

            typeColumn.cellValueFactory = Callback { row ->
                ReadOnlyObjectWrapper(row.value.typeLabel) }
            pathColumn.cellValueFactory = Callback { row ->
                ReadOnlyObjectWrapper(row.value.pathLabel) }
            typeColumn.cellFactory = Callback { Cell() }
            pathColumn.cellFactory = Callback { Cell() }

            revertFileTable.selectionModel.selectedIndices.addListener(
                ListChangeListener {
                    btnOkDisable.value = selectedFiles.isEmpty()
                })

            revertFileTable.setRowFactory {
                object : TableRow<RowData>() {
                    override fun updateItem(rowData: RowData?, empty: Boolean) {
                        super.updateItem(rowData, empty)
                        this.isDisable = (rowData?.file?.mode == CommitFile.Mode.ADD)
                    }
                }
            }

            revertFileTable.items.setAll(files.map { model -> RowData(model) })
            selectedIndices.forEach { revertFileTable.selectionModel.select(it) }

            //テーブル幅の調整
            fileTableAdjuster = ColumnAdjuster(revertFileTable, pathColumn)
        }

    }

}