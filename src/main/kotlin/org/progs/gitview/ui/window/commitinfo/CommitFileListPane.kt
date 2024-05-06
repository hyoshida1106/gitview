package org.progs.gitview.ui.window.commitinfo

import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.VBox
import javafx.scene.text.TextFlow
import javafx.util.Callback
import org.progs.gitview.Database
import org.progs.gitview.MainApp
import org.progs.gitview.MainApp.Companion.resourceBundle
import org.progs.gitview.git.commit.CommitFile
import org.progs.gitview.git.commit.Id
import org.progs.gitview.model.item.CommitInfoItem
import org.progs.gitview.ui.util.ColumnAdjuster
import org.progs.gitview.ui.util.TextMessage
import org.progs.gitview.ui.util.typeLabel
import org.progs.gitview.ui.window.BaseControl
import org.progs.gitview.ui.window.BaseWindow
import org.progs.gitview.ui.window.commitlist.CommitLabelList


interface CommitFileListPaneOperations {
    /** コミット選択変更時の表示変更 */
    fun updateContents(model: CommitInfoItem?)
}

class CommitFileListPane(
    control: Control
): BaseWindow<CommitFileListPane.Control>(control), CommitFileListPaneOperations by control {

    constructor(onFileSelect: (fileInfo: CommitFile?) -> Unit) : this(Control(onFileSelect))

    /** 行表示データクラス */
    data class RowData(val file: CommitFile) {
        val typeLabel get() = file.typeLabel
        val path get() = file.path
    }

    class Control(
        private val onFileSelect: (fileInfo: CommitFile?) -> Unit
    ) : BaseControl(), CommitFileListPaneOperations {
        @FXML private lateinit var commitFileListSplit: SplitPane
        @FXML private lateinit var commitProps: VBox
        @FXML private lateinit var commitMessage: TextArea
        @FXML private lateinit var commitFileList: TableView<RowData>
        @FXML private lateinit var typeColumn: TableColumn<RowData, Node>
        @FXML private lateinit var pathColumn: TableColumn<RowData, String>

        private val modalInfoQueries = Database(MainApp.sqlDriver).commitFileListPanePropsQueries
        private val props = modalInfoQueries.selectAll().executeAsOne()

        /** セルクラス */
        class Cell : TableCell<RowData, Node>() {
            override fun updateItem(
                label: Node?,
                empty: Boolean
            ) {
                super.updateItem(label, empty)
                if(label == null || empty ) {
                    this.graphic = null
                } else {
                    this.graphic = label
                }
                this.text = null
            }
        }

        /** テーブルのカラム幅を調整する処理クラス */
        private lateinit var commitFileListAdjuster: ColumnAdjuster

        /** Split分割位置 */
        private var splitRate: Double
            get() = commitFileListSplit.dividerPositions[0]
            set(value) { commitFileListSplit.setDividerPositions(value) }

        /** 初期化 */
        fun initialize() {
            commitFileList.placeholder = Label("")
            typeColumn.cellValueFactory = Callback { row -> ReadOnlyObjectWrapper(row.value.typeLabel) }
            typeColumn.cellFactory = Callback { Cell() }
            pathColumn.cellValueFactory = PropertyValueFactory("path")
            commitFileListAdjuster = ColumnAdjuster(commitFileList, pathColumn)
        }

        /** 選択されているコミット情報モデル */
        private var selectedCommit: CommitInfoItem? = null

        /** 表示完了時の処理 */
        override fun displayCompleted() {
            commitFileListAdjuster.adjustColumnWidth()
            commitProps.styleClass.add("ItemList")

            //画面分割の復帰
            splitRate = props

            //ファイル選択時のモデル更新処理
            commitFileList.selectionModel.selectedItemProperty().addListener { _, _, rowData ->
                onFileSelect(rowData?.file)
            }
        }

        /** 操作完了(アイドル)時の処理 */
        override fun enterIdleState() {
            //ウィンドウの分割位置をデータベースに保存する
            modalInfoQueries.update(splitRate)
        }

        /** コミット選択変更時の表示変更 */
        override fun updateContents(model: CommitInfoItem?) {
            updateCommitProps(model)
            updateCommitFileList(model)
        }

        /** コミット情報表示の更新 */
        private fun updateCommitProps(
            model: CommitInfoItem?
        ) {
            selectedCommit = model

            if(model != null) {
                commitProps.children.setAll(
                    TextMessage(resourceBundle.getString("Title.ID"), Id.toString(model.id)),
                    TextMessage(resourceBundle.getString("Title.Date"), model.commitTime),
                    TextMessage(resourceBundle.getString("Title.Author"), model.author),
                    TextMessage(resourceBundle.getString("Title.Committer"), model.committer),
                    TextFlow().apply {
                        this.styleClass.add("LabelList")
                        this.children.setAll(CommitLabelList(model))
                    }
                )
                commitMessage.text = model.fullMessage
            } else {
                commitProps.children.clear()
                commitMessage.text = ""
            }
        }

        /** コミットファイル一覧の更新 */
        private fun updateCommitFileList(
            commitInfoItem: CommitInfoItem?
        ) {
            commitFileList.items.setAll(commitInfoItem?.commitFiles?.map { RowData(it) } ?: emptyList())
        }

    }
}