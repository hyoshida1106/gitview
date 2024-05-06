package org.progs.gitview.ui.window.commitinfo

import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.*
import org.progs.gitview.git.commit.CommitFile
import org.progs.gitview.ui.util.ColumnAdjuster
import org.progs.gitview.ui.window.BaseControl
import org.progs.gitview.ui.window.BaseWindow


interface CommitFileDiffPaneOperations {
    /**
     * 表示更新
     */
    fun updateContents(model: CommitFile?)
}


class CommitFileDiffPane(
    control: Control
): BaseWindow<CommitFileDiffPane.Control>(control), CommitFileDiffPaneOperations by control {

    constructor() : this(Control())

    class Control: BaseControl(), CommitFileDiffPaneOperations {
        @FXML private lateinit var commitFileDiffTable: TableView<RowData>
        @FXML private lateinit var line1Column: TableColumn<RowData, String?>
        @FXML private lateinit var line2Column: TableColumn<RowData, String?>
        @FXML private lateinit var textColumn: TableColumn<RowData, String>

        /**
         * 追加行・削除行の判別方法を定義
         */
        private val String.isAddedLine get() = startsWith('+')
        private val String.isDeletedLine get() = startsWith('-')

        /**
         * 行データクラス (基本クラス → ヘッダ、追加行、削除行、共通行)
         */
        open class RowData(
            val line1: String,
            val line2: String,
            val text : String,
            val style: String? = null
        )
        class HeaderRowData(text: String):
            RowData("", "", text, "HeaderLine")
        class AddedRowData(lineNumber: Int, text: String):
            RowData("", lineNumber.toString(), text, "AddedLine")
        class DeletedRowData(lineNumber: Int, text: String):
            RowData(lineNumber.toString(), "", text, "DeletedLine")
        class CommonRowData(lineNumber1: Int, lineNumber2: Int, text: String):
            RowData(lineNumber1.toString(), lineNumber2.toString(), text)

        /**
         * セルクラス
         */
        class Cell: TableCell<RowData, String?>() {
            override fun updateItem(
                data: String?,
                empty: Boolean
            ) {
                this.graphic = null
                this.text = if(!empty) data else null
            }
        }

        /**
         * テーブルのカラム幅を調整する処理クラス
         */
        private lateinit var commitListAdjuster: ColumnAdjuster

        /**
         * JavaFX初期化
         */
        fun initialize() {
            commitFileDiffTable.placeholder = Label("")
            commitFileDiffTable.selectionModel = null

            //各列のFactoryを登録する
            line1Column.setCellValueFactory { row -> ReadOnlyObjectWrapper(row.value.line1) }
            line2Column.setCellValueFactory { row -> ReadOnlyObjectWrapper(row.value.line2) }
            textColumn.setCellValueFactory  { row -> ReadOnlyObjectWrapper(row.value.text) }
            line1Column.setCellFactory { Cell() }
            line2Column.setCellFactory { Cell() }
            textColumn.setCellFactory  { Cell() }

            // 行のCSS Classを設定するためにRowFactoryを更新する
            commitFileDiffTable.setRowFactory {
                object : TableRow<RowData>() {
                    override fun updateItem(rowData: RowData?, empty: Boolean) {
                        styleClass.setAll(listOf("cell", "table-row-cell"))
                        if(!empty && rowData?.style != null) {
                            styleClass.add(rowData.style)
                        }
                        super.updateItem(rowData, empty)
                    }
                }
            }


            //カラム幅調整クラスのインスタンス生成 ※JavaFX初期化後に生成する必要がある
            commitListAdjuster = ColumnAdjuster(commitFileDiffTable, textColumn)

            //初期状態はinvisible
            commitFileDiffTable.isVisible = false
        }

        /**
         * 表示完了時の処理
         */
        override fun displayCompleted() {
            // テーブルのカラム幅を調整
            commitListAdjuster.adjustColumnWidth()
        }

        /**
         * 表示更新
         */
        override fun updateContents(
            model: CommitFile?
        ) {
            val rowDataList = mutableListOf<RowData>()
            if (model != null) {
                model.getDiffLines().forEach { header ->
                    rowDataList.add(HeaderRowData(header.header))
                    var leftLine = header.leftLine
                    var rightLine = header.rightLine
                    rowDataList.addAll(header.textLines.map { text ->
                        when  {
                            text.isAddedLine -> AddedRowData(rightLine++, text)
                            text.isDeletedLine -> DeletedRowData(leftLine++, text)
                            else -> CommonRowData(leftLine++, rightLine++, text)
                        }
                    })
                }
                commitFileDiffTable.isVisible = true
            }
            commitFileDiffTable.itemsProperty().value = FXCollections.observableList(rowDataList)
        }
    }

}
