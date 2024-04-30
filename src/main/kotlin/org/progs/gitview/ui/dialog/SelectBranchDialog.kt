package org.progs.gitview.ui.dialog

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import org.progs.gitview.MainApp.Companion.resourceBundle
import org.progs.gitview.model.*
import org.progs.gitview.ui.util.ColumnAdjuster


class SelectBranchDialog(
    repositoryModel: RepositoryModel,
    message: String,
    selectionFilter: (RemoteBranchModel?, LocalBranchModel?) -> Boolean = { _, _ -> true }
): CustomDialog<SelectBranchDialog.Control>(
    resourceBundle.getString("SelectBranchDialog.Title"),
    Control(repositoryModel, message) { rowData: RowData? ->
        selectionFilter(rowData?.branchModel?.remote, rowData?.branchModel?.local) },
    ButtonType.OK, ButtonType.CANCEL
)  {
    /**
     * データモデルクラス
     */
    data class BranchDataModel(
        val remote: RemoteBranchModel?,
        val local : LocalBranchModel?
    )

    /**
     * 行表示データクラス
     */
    data class RowData(val branchModel: BranchDataModel) {
        val remote get() = branchModel.remote?.name ?: ""
        val local  get() = branchModel.local?.name ?: ""
    }

    /**
     * 選択されたモデル(リモートブランチ、ローカルブランチ)を返す
     */
    val selectedModel get() = controller.selectedModel

    /**
     * 初期化
     */
    init {
        //コントロールクラスからOKボタンの有効/無効を制御する
        addButtonHandler(ButtonType.OK, controller.btnOkDisable)
    }

    /**
     * コントロールクラス
     */
    class Control(
        private val repositoryModel: RepositoryModel,
        private val message: String,
        //選択可能/不可を行単位で選択するフィルタ
        private val rowFilter: (RowData?) -> Boolean
    ): DialogControl() {
        @FXML private lateinit var selectBranchMessage: Label
        @FXML private lateinit var selectBranchTable : TableView<RowData>
        @FXML private lateinit var remoteBranchColumn: TableColumn<RowData, String>
        @FXML private lateinit var localBranchColumn : TableColumn<RowData, String>

        /**
         *  OKボタンの無効を指示するプロパティ
         */
        val btnOkDisable = SimpleBooleanProperty(true)

        /**
         * テーブルのカラム幅を調整する処理クラス
         */
        private lateinit var columnAdjuster: ColumnAdjuster

        /**
         * 選択されたモデルを取得する
         */
        val selectedModel: BranchDataModel?
            get() = selectBranchTable.selectionModel.selectedItem?.branchModel

        /**
         * 初期化
         */
        override fun initialize() {
            selectBranchMessage.text = message

            selectBranchTable.placeholder = Label("")
            selectBranchTable.selectionModel.selectionMode = SelectionMode.SINGLE

            remoteBranchColumn.cellValueFactory = PropertyValueFactory("remote")
            localBranchColumn .cellValueFactory = PropertyValueFactory("local" )
            columnAdjuster = ColumnAdjuster(selectBranchTable, localBranchColumn).apply {
                adjustColumnWidth()
            }

            //無効行の選択と表示を行うためにRowFactoryをオーバーロードする
            selectBranchTable.setRowFactory { _ ->
                object : TableRow<RowData>() {
                    override fun updateItem(rowData: RowData?, empty: Boolean) {
                        styleClass.setAll(listOf("cell", "table-row-cell"))
                        isDisable = !rowFilter(rowData)
                        if(isDisable) { styleClass.add("DisabledLine") }
                        super.updateItem(rowData, empty)
                    }
                }
            }

            //選択状態に対応してOKボタンの有効/無効を切り替える
            selectBranchTable.selectionModel.selectedItemProperty().addListener { _, _, rowData ->
                btnOkDisable.value = !rowFilter(rowData)
            }

            //表示データの設定
            selectBranchTable.items.setAll(branchList())

            //カレントブランチを選択する
            repositoryModel.currentBranch?.let { branch ->
                selectBranchTable.items.firstOrNull { it.local == branch }?.let { rowData ->
                    selectBranchTable.scrollTo(rowData)
                    selectBranchTable.selectionModel.select(rowData)
                }
                Platform.runLater { selectBranchTable.requestFocus() }
            }
        }

        /**
         * ブランチ一覧の更新
         */
        private fun branchList(): List<RowData> {
            val remoteBranchList = repositoryModel.branchListModel.remoteBranchList
            val localBranchList  = repositoryModel.branchListModel.localBranchList
            val branchNameList =
                (remoteBranchList.map { it.name } + localBranchList.map { it.name }).sorted().distinct()
            return branchNameList.map { name ->
                RowData(BranchDataModel(
                    remoteBranchList.firstOrNull { it.name == name },
                    localBranchList.firstOrNull { it.name == name }
                ))
            }
        }

    }
}