package org.progs.gitview.ui.dialog

import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.ListChangeListener
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.util.Callback
import org.progs.gitview.MainApp.Companion.resourceBundle
import org.progs.gitview.git.commit.CommitFile
import org.progs.gitview.ui.util.typeLabel


class CommitDialog(
    files: List<CommitFile>,
    selectedIndices: List<Int>,
    comment: String = ""
): CustomDialog<CommitDialog.Control>(
    resourceBundle.getString("Message.CommitComment"),
    Control(files, selectedIndices, comment),
    ButtonType.OK, ButtonType.CANCEL
) {
    /** 表示行データ */
    class RowData(
        val file: CommitFile
    ) {
        val typeLabel get() = file.typeLabel
        val path = file.path
    }

    /** 初期化 */
    init {
        addButtonHandler(ButtonType.OK, controller.btnOkDisable)
    }

    /** メッセージ文字列 */
    val message: String get() = controller.message

    /** コントロールクラス */
    class Control(
        private val files: List<CommitFile>,
        private val selectedIndices: List<Int>,
        private val comment: String
    ): DialogControl() {

        @FXML private lateinit var commitFileTable: TableView<RowData>
        @FXML private lateinit var typeColumn: TableColumn<RowData, Node>
        @FXML private lateinit var pathColumn: TableColumn<RowData, String>
        @FXML private lateinit var commitMessageText: TextArea

        /** OKボタンの無効を指示するプロパティ */
        val btnOkDisable = SimpleBooleanProperty(true)

        /** メッセージ文字列 */
        val message: String get() = commitMessageText.text

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
            commitFileTable.selectionModel.selectedItems.map { it.file }

        /** 初期化 */
        override fun initialize() {
            commitFileTable.placeholder = Label("")
            commitFileTable.selectionModel.selectionMode = SelectionMode.MULTIPLE
            typeColumn.cellValueFactory = Callback { row ->
                ReadOnlyObjectWrapper(row.value.typeLabel) }
            typeColumn.cellFactory = Callback { Cell() }
            pathColumn.cellValueFactory = PropertyValueFactory("path")

            commitFileTable.selectionModel.selectedIndices.addListener(
                ListChangeListener {
                    btnOkDisable.value = selectedFiles.isEmpty()
                })

            commitFileTable.items.setAll(files.map { model -> RowData(model) })
            selectedIndices.forEach { commitFileTable.selectionModel.select(it) }

            commitMessageText.text = comment
        }

    }

}