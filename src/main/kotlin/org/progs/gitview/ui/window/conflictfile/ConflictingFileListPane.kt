package org.progs.gitview.ui.window.conflictfile

import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import org.progs.gitview.ui.util.ColumnAdjuster
import org.progs.gitview.ui.window.BaseControl
import org.progs.gitview.ui.window.BaseWindow
import org.progs.gitview.ui.window.conflictfile.ConflictingFileListPane.Control.LineAttribute.*
import java.io.File


class ConflictingFileListPane : BaseWindow<ConflictingFileListPane.Control>(Control()) {

    class Control : BaseControl() {
        @FXML private lateinit var conflictingFileList: TableView<RowData>
        @FXML private lateinit var lineColumn: TableColumn<RowData, String>
        @FXML private lateinit var textColumn: TableColumn<RowData, String>

        enum class LineAttribute { StartTheirs, StartMine, EndMine, Other }

        /** 表示行データ */
        data class RowData(
            val line: String,
            val text: String,
            val subClass: String
        ) {
            val lineAttribute: LineAttribute =
                when {
                    text.startsWith("<<<<<<<" ) -> StartTheirs
                    text.startsWith("=======" ) -> StartMine
                    text.startsWith(">>>>>>>" ) -> EndMine
                    else -> Other
                }
        }

        /** テーブルのカラム幅を調整する処理クラス */
        private lateinit var fileTableAdjuster: ColumnAdjuster

        /** 初期化 */
        fun initialize() {
            //テーブルの設定
            conflictingFileList.placeholder = Label("")
            conflictingFileList.selectionModel.selectionMode = SelectionMode.SINGLE
            lineColumn.cellValueFactory = PropertyValueFactory("line")
            textColumn.cellValueFactory = PropertyValueFactory("text")

            //無効行の選択と表示を行うためにRowFactoryをオーバーロードする
            conflictingFileList.setRowFactory { _ ->
                object : TableRow<RowData>() {
                    override fun updateItem(rowData: RowData?, empty: Boolean) {
                        if(rowData?.lineAttribute == Other) {
                            styleClass.add(rowData.subClass)
                        }
                        super.updateItem(rowData, empty)
                    }
                }
            }

            //テーブル幅の調整
            fileTableAdjuster = ColumnAdjuster(conflictingFileList, textColumn)
        }

        /** 表示完了時の処理 */
        override fun displayCompleted() {
            conflictingFileList.isVisible = false
        }

        /** 表示更新 */
        fun updateContents(
            file: File?
        ) {
            if(file != null) {
                var lineNumber = 0
                var subClass = "Other"
                val lines = file.readLines().map {
                    RowData((++lineNumber).toString(), it, subClass).also { rowData ->
                        when (rowData.lineAttribute) {
                            StartTheirs -> subClass = "Theirs"
                            StartMine -> subClass = "Mine"
                            EndMine -> subClass = "Other"
                            Other -> {}
                        }
                    }
                }
                conflictingFileList.itemsProperty().value = FXCollections.observableList(lines)
                conflictingFileList.isVisible = true
            } else {
                conflictingFileList.items.clear()
                conflictingFileList.isVisible = false
            }
            conflictingFileList.selectionModel.clearSelection()
        }
    }
}