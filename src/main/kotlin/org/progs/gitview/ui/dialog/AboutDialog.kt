package org.progs.gitview.ui.dialog

import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.util.Callback
import org.progs.gitview.MainApp
import org.progs.gitview.MainApp.Companion.resourceBundle
import org.progs.gitview.ui.util.ColumnAdjuster
import java.util.*

class AboutDialog: CustomDialog<AboutDialog.Control>(
    resourceBundle.getString("AboutDialog.Title"),
    Control(),
    ButtonType.OK
) {
    data class RowData(
        val software: String,
        val licence: String,
        val url: String
    )

    class Control : DialogControl() {

        @FXML private lateinit var aboutDialogVbox: VBox
        @FXML private lateinit var softwareInformation: HBox
        @FXML private lateinit var softwareDescription: AnchorPane
        @FXML private lateinit var licenceTable: TableView<RowData>
        @FXML private lateinit var softwareCol: TableColumn<RowData, RowData>
        @FXML private lateinit var licenceCol : TableColumn<RowData, String>

        //ライブラリ情報   (バージョン、ライセンス、URL)
        private val libraries = arrayOf(
            RowData( "kotlinx-coroutines-core:1.7.3",
                "Apache 2.0 licence",
                "https://github.com/Kotlin/kotlinx.coroutines"),
            RowData("controlsfx:11.2.0",
                "BSD-3-Clause license",
                "https://github.com/controlsfx/controlsfx"),
            RowData("sqldelight:2.0.1",
                "Apache 2.0 licence",
                "https://github.com/cashapp/sqldelight"),
            RowData("eclipse-jgit:6.8.0.202311291450-r",
                "Eclipse Distribution License v1.0",
                "https://github.com/eclipse-jgit/jgit"),
            RowData("slf4j:2.0.11",
                "MIT License",
                "https://slf4j.org/"),
            RowData("REMIX ICON:4.2.0",
                "Apache 2.0 License",
                "https://remixicon.com/"),
            RowData("source-han-code-jp:2.0.11",
                "SIL Open Font License v1.1",
                "https://github.com/adobe-fonts/source-han-code-jp"),
        )

        // テーブルのカラム幅を調整する処理クラス
        private lateinit var commitListAdjuster: ColumnAdjuster

        // ハイパーリンク列
        class Cell : TableCell<RowData, RowData>() {
            override fun updateItem(
                rowData: RowData?,
                empty: Boolean
            ) {
                super.updateItem(rowData, empty)
                if(rowData == null || empty ) {
                    this.graphic = null
                } else {
                    this.graphic = Hyperlink(rowData.software).apply {
                        this.setOnAction { _ -> MainApp.myHostServices?.showDocument(rowData.url) }
                    }
                }
                this.text = null
            }
        }

        // 初期化
        override fun initialize() {
            softwareDescription.children.setAll(softwareNote())

            softwareCol.cellValueFactory = Callback { row -> ReadOnlyObjectWrapper(row.value) }
            softwareCol.cellFactory = Callback { Cell() }
            licenceCol.cellValueFactory = PropertyValueFactory("licence")
            licenceTable.items.addAll(libraries)
            licenceTable.selectionModel = null

            commitListAdjuster = ColumnAdjuster(licenceTable, licenceCol).apply { adjustColumnWidth() }
        }

        // ソフトウェア説明欄
        private fun softwareNote(): Node {
            return VBox(
                Label("Preliminary Alpha Version"),
                Label(Date().toString())
            ).also { it.styleClass.add("SoftwareNode") }
        }
    }

}