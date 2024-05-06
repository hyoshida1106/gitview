package org.progs.gitview.ui.window.conflictfile

import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.SplitPane
import javafx.scene.layout.AnchorPane
import org.progs.gitview.Database
import org.progs.gitview.MainApp
import org.progs.gitview.model.RepositoryModel
import org.progs.gitview.model.item.WorkTreeItem
import org.progs.gitview.ui.window.BaseControl
import org.progs.gitview.ui.window.BaseWindow


class ConflictingFilesWindow(
    control: Control
): BaseWindow<ConflictingFilesWindow.Control>(control) {

    constructor(repositoryModel: RepositoryModel): this(Control(repositoryModel))

    class Control(
        private val repositoryModel: RepositoryModel
    ): BaseControl() {
        @FXML private lateinit var conflictingFilesSplit: SplitPane
        @FXML private lateinit var conflictingFileTablePane: AnchorPane
        @FXML private lateinit var conflictingFileListPane: AnchorPane

        //データベース定義
        private val modalInfoQueries = Database(MainApp.sqlDriver).conflictingFilesWindowPropsQueries
        private val props = modalInfoQueries.selectAll().executeAsOne()

        /** Split分割位置 */
        private var splitRate: Double
            get() = conflictingFilesSplit.dividerPositions[0]
            set(value) { conflictingFilesSplit.setDividerPositions(value) }

        /** ファイル一覧 */
        private val conflictingFileTablePaneImpl = ConflictingFileTablePane(repositoryModel) { file ->
            conflictingFileListPaneImpl.updateContents(file) }

        /** ファイルリスト */
        private val conflictingFileListPaneImpl = ConflictingFileListPane()

        /** 初期化 */
        fun initialize() {

            conflictingFileTablePane.children.setAll(conflictingFileTablePaneImpl.rootWindow)
            conflictingFileListPane.children.setAll(conflictingFileListPaneImpl.rootWindow)

            //更新時処理
            repositoryModel.commitListModel.addSelectedWorkTreeItemListener { item ->
                Platform.runLater { selectCommit(item) }
            }
        }

        /** 表示完了時の処理 */
        override fun displayCompleted() {
            //初期は非表示
            conflictingFilesSplit.isVisible = false
            //分割ウィンドウの位置を再生する
            splitRate = props
        }

        /** 操作完了(アイドル)時の処理 */
        override fun enterIdleState() {
            //ウィンドウの分割位置をデータベースに保存する
            modalInfoQueries.update(splitRate)
        }

        /** コミット選択変更時の表示変更 */
        private fun selectCommit(item: WorkTreeItem?) {
            conflictingFilesSplit.isVisible = (item?.isConflicting == true)
            conflictingFileTablePaneImpl.updateContents()
        }
    }
}