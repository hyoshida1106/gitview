package org.progs.gitview.ui.window.commitinfo

import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.SplitPane
import javafx.scene.layout.AnchorPane
import org.progs.gitview.Database
import org.progs.gitview.MainApp
import org.progs.gitview.model.RepositoryModel
import org.progs.gitview.model.item.CommitInfoItem
import org.progs.gitview.ui.window.BaseControl
import org.progs.gitview.ui.window.BaseWindow

class CommitDetailWindow(
    repositoryModel: RepositoryModel
): BaseWindow<CommitDetailWindow.Control>(Control(repositoryModel)) {

    class Control(
        private val repositoryModel: RepositoryModel
    ): BaseControl() {
        @FXML private lateinit var commitDetailSplit: SplitPane
        @FXML private lateinit var commitFileListPane: AnchorPane
        @FXML private lateinit var commitFileDiffPane: AnchorPane

        private val modalInfoQueries = Database(MainApp.sqlDriver).commitDetailWindowPropsQueries
        private val props = modalInfoQueries.selectAll().executeAsOne()

        /** Split分割位置 */
        private var splitRate: Double
            get() = commitDetailSplit.dividerPositions[0]
            set(value) { commitDetailSplit.setDividerPositions(value) }

        private val commitFileDiffPaneImpl = CommitFileDiffPane()
        private val commitFileListPaneImpl = CommitFileListPane() { file ->
            commitFileDiffPaneImpl.controller.updateContents(file) }

        /** 初期化 */
        fun initialize() {
            commitFileListPane.children.setAll(commitFileListPaneImpl.rootWindow)
            commitFileDiffPane.children.setAll(commitFileDiffPaneImpl.rootWindow)
        }

        /** 表示完了時の処理 */
        override fun displayCompleted() {

            //コミット選択変更時の処理
            repositoryModel.commitListModel.addSelectedCommitInfoItemListener { item ->
                Platform.runLater { selectCommit(item) }
            }

            commitDetailSplit.isVisible = false

            //分割ウィンドウの位置を再生する
            splitRate = props
        }

        /** 操作完了(アイドル)時の処理 */
        override fun enterIdleState() {
            //ウィンドウの分割位置をデータベースに保存する
            modalInfoQueries.update(splitRate)
        }

        /** コミット選択変更時の表示変更 */
        private fun selectCommit(item: CommitInfoItem?) {
            commitDetailSplit.isVisible = (item != null)
            commitFileListPaneImpl.controller.updateContents(item)
        }
    }
}