package org.progs.gitview.ui.window.commitlist

import javafx.css.PseudoClass
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.TableRow
import javafx.scene.layout.VBox
import org.progs.gitview.MainApp.Companion.resourceBundle
import org.progs.gitview.model.item.WorkTreeItem
import java.text.DateFormat
import java.util.*


class WorkingTreeRowData(
    commitList: CommitListWindow.Control,
    override var item: WorkTreeItem
) : CommitListWindow.Control.RowData<WorkTreeItem> {

    override val treeCellValue: CommitListWindow.Control.CellData = CommitTreeCellData(commitList, item)
    override val infoCellValue: CommitListWindow.Control.CellData = CellData(item)

    /** コミット情報セル */
    inner class CellData(
        private val model: WorkTreeItem
    ) : CommitListWindow.Control.CellData {
        override val contextMenu = item.contextMenu

        override fun update(): Pair<Node?, String?> {
            val dateLabel = Label(DateFormat.getDateTimeInstance().format(Date()))
                .apply { this.styleClass.add("commit-date") }
            val fileLabel = Label(resourceBundle.getString("Message.WorkTreeCell").format(
                model.stagedFiles.size, model.modifiedFiles.size, model.conflictingFiles.size))
            val vbox = VBox(dateLabel, fileLabel).apply { styleClass.add("WorkingTreeCell") }
            return Pair(vbox, null)
        }
    }

    /** ツールチップなし */
    override val toolTipText: String?
        get() = null

    private val conflict = PseudoClass.getPseudoClass("conflict")

    /** スタイル設定 */
    override fun setStyle(row: TableRow<CommitListWindow.Control.RowData<*>>) {
        row.styleClass.add("WorkingTreeRow")
        row.pseudoClassStateChanged(conflict, item.isConflicting)
    }
}
