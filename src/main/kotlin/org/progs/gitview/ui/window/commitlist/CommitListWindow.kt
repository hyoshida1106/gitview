package org.progs.gitview.ui.window.commitlist

import javafx.application.Platform
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.AccessibleAttribute
import javafx.scene.Node
import javafx.scene.control.*
import org.progs.gitview.model.CommitListModel
import org.progs.gitview.model.RepositoryModel
import org.progs.gitview.model.item.CommitListItem
import org.progs.gitview.model.item.WorkTreeItem
import org.progs.gitview.ui.util.ColumnAdjuster
import org.progs.gitview.ui.window.BaseControl
import org.progs.gitview.ui.window.BaseWindow
import kotlin.math.max
import kotlin.math.min


class CommitListWindow(
    repositoryModel: RepositoryModel
): BaseWindow<CommitListWindow.Control>(Control(repositoryModel)) {

    class Control(
        private val repositoryModel: RepositoryModel
    ) : BaseControl() {
        @FXML private lateinit var commitListTable: TableView<RowData<*>>
        @FXML private lateinit var treeColumn: TableColumn<RowData<*>, CellData>
        @FXML private lateinit var infoColumn: TableColumn<RowData<*>, CellData>

         /** 行データインターフェース */
        interface RowData<T> where T: CommitListItem {
            val item: T
            val treeCellValue: CellData
            val infoCellValue: CellData
            val toolTipText: String?
            fun setStyle(row: TableRow<RowData<*>>)
        }

         /** セルデータインターフェース */
        interface CellData {
            fun update(): Pair<Node?, String?>
            fun layout(tableCell: Cell) {}
            val contextMenu: ContextMenu?
        }

         /** セルクラス */
        class Cell : TableCell<RowData<*>, CellData>() {
            private var cellData: CellData? = null

             /** 表示内容を更新 */
            override fun updateItem(
                data: CellData?,
                empty: Boolean
            ) {
                super.updateItem(data, empty)
                this.cellData = data
                val (graphic, text) =
                    if(data != null && !empty) data.update() else Pair(null, null)
                this.graphic = graphic
                this.text = text
                contextMenu = data?.contextMenu
            }

             /** セル表示の再構成および更新 */
            override fun layoutChildren() {
                super.layoutChildren()
                cellData?.layout(this)
            }
        }

        companion object {
            /** レーンピッチの初期値 */
            const val DEFAULT_LANE_PITCH = 11.0
            /** 初期表示時の最大レーン数 */
            const val DEFAULT_LANE_NUMBER = 8.0
            /** レーンピッチの最大値 */
            const val MAX_LANE_PITCH = DEFAULT_LANE_PITCH * 4.0
        }

         /** レーンピッチ初期値 */
        private val defaultTreeLanePitch = DEFAULT_LANE_PITCH

         /** 描画するレーンのピッチ(間隔)を更新する */
        var treeLinePitch: Double = defaultTreeLanePitch

         /** 指定されたレーンの横位置をピッチから計算する */
        fun treeLinePosition(
            laneNumber: Int,
            pitch: Double = treeLinePitch
        ) = pitch * ( laneNumber + 1.0 )

        /* 最大レーン番号 ( = レーン数 - 1 ) */
        private var maxLaneNumber: Int = 0

         /** ツリー表示列の幅の初期値と最大幅を算出する */
        private val treeColumnDefaultWidth
            get() = min(
                a = treeLinePosition(maxLaneNumber, defaultTreeLanePitch),
                b = defaultTreeLanePitch * (DEFAULT_LANE_NUMBER + 0.5))
        private val treeColumnMaximumWidth
            get() = treeLinePosition(maxLaneNumber, MAX_LANE_PITCH)

         /** テーブルのカラム幅を調整する処理クラス */
        private lateinit var commitListAdjuster: ColumnAdjuster

         /** JavaFX初期化 */
        fun initialize() {
            commitListTable.placeholder = Label("")

            //各列のFactoryを登録する
            treeColumn.setCellValueFactory { row -> ReadOnlyObjectWrapper(row.value.treeCellValue) }
            infoColumn.setCellValueFactory { row -> ReadOnlyObjectWrapper(row.value.infoCellValue) }
            treeColumn.setCellFactory { Cell() }
            infoColumn.setCellFactory { Cell() }

            //行のCSS Classを設定するためにRowFactoryを更新する
            commitListTable.setRowFactory {
                object : TableRow<RowData<*>>() {
                    private val styleClassArray = listOf("cell", "table-row-cell")
                    override fun updateItem(rowData: RowData<*>?, empty: Boolean) {
                        styleClass.setAll(styleClassArray)
                        rowData?.setStyle(this)
                        super.updateItem(rowData, empty)

                        val toolTipText = if(!empty) rowData?.toolTipText else null
                        tooltip = if(toolTipText != null) Tooltip(toolTipText) else null
                    }
                }
            }

            //カラム幅調整クラスのインスタンス生成 ※JavaFX初期化後に生成する必要がある
            commitListAdjuster = ColumnAdjuster(commitListTable, infoColumn)

            //初期状態はinvisible
            commitListTable.isVisible = false
        }

         /** 表示完了時の処理 */
        override fun displayCompleted() {
            val commitListModel = repositoryModel.commitListModel

            //リポジトリ更新時に選択をクリアする
            repositoryModel.addListener {
                Platform.runLater { commitListTable.selectionModel.clearSelection() }
            }

            //描画対象ブランチ選択変更時の描画更新
            commitListModel.addCommitInfoListener { commitInfo ->
                Platform.runLater { drawCommitList(commitInfo) }
            }

            //Treeカラム幅変更時にレーンピッチを更新する
            treeColumn.widthProperty().addListener { _, _, value ->
                treeLinePitch = max(value.toDouble() / (maxLaneNumber + 1).toDouble(), defaultTreeLanePitch)
            }

            //Commit選択時のモデル更新処理
            commitListTable.selectionModel.selectedItemProperty().addListener { _, _, rowData ->
                commitListModel.selectCommitItem(rowData?.item)
            }

            // テーブルのカラム幅を調整
            commitListAdjuster.adjustColumnWidth()
        }

        private fun adjustColumnWidth() {
            if(commitListTable.itemsProperty().value.getOrNull(0)?.item is WorkTreeItem) {
                commitListAdjuster.margin = 4.0
            } else {
                commitListAdjuster.margin = 0.0
            }
            commitListAdjuster.adjustColumnWidth()
        }

        /** 表示更新 */
        private fun drawCommitList(
            commitInfo: CommitListModel.CommitInfo?
        ) {
            if(commitInfo != null) {
                val workTreeFiles = commitInfo.workTreeFiles
                val commitInfoList = commitInfo.commitList

                //レーン数が変更されていれば、カラム幅を既定値に戻す
                val newMaxLaneNumber = (commitInfoList.maxOfOrNull { it.maxLaneNumber })?.let { it + 1 } ?: 0
                if (newMaxLaneNumber != maxLaneNumber) {
                    maxLaneNumber = newMaxLaneNumber
                    //一旦変更した上で最大値を再設定する
                    treeColumn.maxWidth = treeColumnDefaultWidth
                    treeColumn.prefWidth = treeColumnDefaultWidth
                    treeColumn.maxWidth = treeColumnMaximumWidth
                }

                //選択中の行番号を保存
                val selectedIndex = commitListTable.selectionModel.selectedIndex
                val selectedItem = commitListTable.selectionModel.selectedItem

                //コミット一覧表示リスト
                val commits = mutableListOf<RowData<*>>()

                //WorkingTree情報を取得
                val headLane = commitInfoList.getOrNull(0)?.headLane ?: 0
                val workTreeItem = WorkTreeItem(headLane, workTreeFiles)
                if (!workTreeItem.isEmpty) {
                    //WorkingTree情報があれば先頭に格納
                    commits.add(WorkingTreeRowData(this, workTreeItem))
                }

                //Commit情報を表示バッファに格納
                commits.addAll(commitInfoList.map { CommitInfoRowData(this, it) })

                //表示バッファの内容をテーブルのItemとして設定
                commitListTable.itemsProperty().value = FXCollections.observableList(commits)

                //可能であれば選択行を復帰
                if (canReselectItem(selectedIndex, selectedItem)) {
                    Platform.runLater { commitListTable.selectionModel.select(selectedIndex) }
                }

                // テーブルのカラム幅を調整
                adjustColumnWidth()
            }

            //リストを可視化
            commitListTable.isVisible = commitListTable.itemsProperty().value.isNotEmpty()

        }

         /** 選択行の復帰が可能であるか判断する */
        private fun canReselectItem(
            selectedIndex: Int,
            selectedItem: RowData<*>?
        ): Boolean {
            if(0 <= selectedIndex && selectedIndex <= commitListTable.items.lastIndex) {
                when (val newRowData = commitListTable.items[selectedIndex]) {
                    //WorkingTree行が選択されていれば復帰
                    is WorkingTreeRowData -> {
                        return selectedItem is WorkingTreeRowData
                    }
                    //コミット行は選択対象が変わっていなければ復帰
                    is CommitInfoRowData -> {
                        return (selectedItem is CommitInfoRowData) &&
                                (newRowData.item.id == selectedItem.item.id)
                    }
                }
            }
            return false
        }

        /** 指定レコードへジャンプ */
        fun jumpToIndex(index: Int) {
            commitListTable.requestFocus()
            if(!isRowVisible(index)) {
                commitListTable.scrollTo(index)
            }
            commitListTable.selectionModel.select(index)
            commitListTable.focusModel.focus(index)
        }

        /** 指定された行がViewPort内であるか判定する */
        private fun isRowVisible(index: Int): Boolean {
            return commitListTable.queryAccessibleAttribute(AccessibleAttribute.ROW_AT_INDEX, index) != null
        }
    }
}