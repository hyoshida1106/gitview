package org.progs.gitview.ui.window.worktree

import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.SplitPane
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage
import org.progs.gitview.Database
import org.progs.gitview.MainApp
import org.progs.gitview.model.RepositoryModel
import org.progs.gitview.model.item.WorkTreeItem
import org.progs.gitview.ui.util.IdleTimer
import org.progs.gitview.ui.window.BaseControl
import org.progs.gitview.ui.window.BaseWindow
import org.progs.gitview.ui.window.commitinfo.CommitFileDiffPane

class WorkTreeDetailWindow(
    repositoryModel: RepositoryModel
): BaseWindow<WorkTreeDetailWindow.Control>(Control(repositoryModel)) {

    class Control(
        private val repositoryModel: RepositoryModel
    ) : BaseControl() {
        private val modalInfoQueries = Database(MainApp.sqlDriver).workTreeDetailWindowPropsQueries
        private val props = modalInfoQueries.selectAll().executeAsOne()

        @FXML private lateinit var workTreeDetailSplit : SplitPane
        @FXML private lateinit var workTreeFileListPane: AnchorPane
        @FXML private lateinit var workTreeFileDiffPane: AnchorPane

        private lateinit var idleTimer: IdleTimer

        /**
         *  Split分割位置
         */
        private var splitRate: Double
            get() = workTreeDetailSplit.dividerPositions[0]
            set(value) { workTreeDetailSplit.setDividerPositions(value) }

        /**
         * ペイン定義
         */
        private val workTreeFileDiffPaneImpl = CommitFileDiffPane()
        private val workTreeFileListPaneImpl = WorkTreeFileListPane(repositoryModel){ file ->
            workTreeFileDiffPaneImpl.controller.updateContents(file)
        }

        /**
         * 初期化
         */
        fun initialize() {
            workTreeFileListPane.children.setAll(workTreeFileListPaneImpl.rootWindow)
            workTreeFileDiffPane.children.setAll(workTreeFileDiffPaneImpl.rootWindow)
        }

        /**
         * 表示完了時の処理
         */
        override fun displayCompleted() {
            //定周期で更新チェックを実施
            val stage = workTreeDetailSplit.scene.window as Stage
            idleTimer = IdleTimer(stage, 5000, true ) {
                repositoryModel.commitListModel.updateWorkTreeFiles()
            }

            //更新時処理
            repositoryModel.commitListModel.addSelectedWorkTreeItemListener { item ->
                Platform.runLater { selectCommit(item) }
            }

            workTreeDetailSplit.isVisible = false

            //分割ウィンドウの位置を再生する
            splitRate = props
        }

        /**
         * 操作完了(アイドル)時の処理
         */
        override fun enterIdleState() {
            //ウィンドウの分割位置をデータベースに保存する
            modalInfoQueries.update(splitRate)
        }

        /** コミット選択変更時の表示変更 */
        private fun selectCommit(item: WorkTreeItem?) {
            workTreeDetailSplit.isVisible = (item?.isConflicting == false)
        }
    }
}