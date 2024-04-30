package org.progs.gitview.ui.window.commitlist

import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.TableRow
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import org.progs.gitview.model.item.CommitInfoItem


class CommitInfoRowData(
    commitList: CommitListWindow.Control,
    override val item: CommitInfoItem
) : CommitListWindow.Control.RowData<CommitInfoItem> {

    override val treeCellValue: CommitListWindow.Control.CellData = CommitTreeCellData(commitList, item)
    override val infoCellValue: CommitListWindow.Control.CellData = CellData(item)

    /** コミット情報セル */
    inner class CellData(
        private val model: CommitInfoItem
    ) : CommitListWindow.Control.CellData {
        override val contextMenu = model.contextMenu

        override fun update(): Pair<Node?, String?> {
            val dateLabel = Label(model.commitTime).apply { this.styleClass.add("commit-date") }
            val messageLabel = Label(model.shortMessage).apply { this.styleClass.add("commit-message") }
            val cellImage = VBox(
                HBox(dateLabel).apply { children.addAll(CommitLabelList(model)) },
                messageLabel
            ).apply { styleClass.add("CommitInfoCell") }
            if(model.isHead) cellImage.styleClass.add("header-commit")
            return Pair(cellImage, null)
        }
    }

    /** ツールチップテキスト */
    override val toolTipText: String
        get() = item.toolTipInformation

    /** スタイル設定 */
    override fun setStyle(row: TableRow<CommitListWindow.Control.RowData<*>>) {
        row.styleClass.add("CommitInfoRow")
    }
}