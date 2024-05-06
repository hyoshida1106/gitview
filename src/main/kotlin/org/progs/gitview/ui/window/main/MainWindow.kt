package org.progs.gitview.ui.window.main

import javafx.application.Platform
import javafx.concurrent.Task
import javafx.fxml.FXML
import javafx.scene.Cursor
import javafx.scene.control.SplitPane
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage
import org.progs.gitview.Database
import org.progs.gitview.MainApp
import org.progs.gitview.ui.util.ProgressMonitorImpl
import org.progs.gitview.model.RepositoryModel
import org.progs.gitview.ui.window.BaseControl
import org.progs.gitview.ui.window.BaseWindow
import org.progs.gitview.ui.window.ProgressWindow
import org.progs.gitview.ui.window.branchlist.BranchListWindow
import org.progs.gitview.ui.window.commitinfo.CommitDetailWindow
import org.progs.gitview.ui.window.commitlist.CommitListWindow
import org.progs.gitview.ui.window.conflictfile.ConflictingFilesWindow
import org.progs.gitview.ui.window.worktree.WorkTreeDetailWindow


interface MainWindowOperations {
    val propWidth  : Double
    val propHeight : Double
    /**　指定されたレコードへジャンプ */
    fun jumpCommitList(index: Int)
}

val mainWindow = MainWindow(RepositoryModel())

/**
 * メインウィンドウ
 */
class MainWindow(
    control: Control
): BaseWindow<MainWindow.Control>(control), MainWindowOperations by control {

    constructor(
        repositoryModel: RepositoryModel
    ): this(Control(repositoryModel))

    //コントローラ
    class Control(
        private val repositoryModel: RepositoryModel
    ): BaseControl(), MainWindowOperations {
        @FXML private lateinit var mainSplit: SplitPane
        @FXML private lateinit var menuBar: AnchorPane
        @FXML private lateinit var branchList: AnchorPane
        @FXML private lateinit var commitList: AnchorPane
        @FXML private lateinit var commitInfo: AnchorPane
        @FXML private lateinit var statusBar: AnchorPane

        /** モーダル情報にアクセスするためのデータクラス */
        private val modalInfoQueries = Database(MainApp.sqlDriver).mainWindowPropsQueries
        private val props = modalInfoQueries.selectAll().executeAsOne()
        override val propWidth  get() = props.width.toDouble()
        override val propHeight get() = props.height.toDouble()

        private val branchListWindow = BranchListWindow(repositoryModel)
        private val commitListWindow = CommitListWindow(repositoryModel)
        private val commitDetailWindow = CommitDetailWindow(repositoryModel)
        private val workTreeDetailWindow = WorkTreeDetailWindow(repositoryModel)
        private val conflictingFilesWindow = ConflictingFilesWindow(repositoryModel)

        /** JavaFX 初期化 */
        fun initialize() {
            //各ペインにウィンドウのルートインスタンスを設定する
            branchList.children.add(branchListWindow.rootWindow)
            commitList.children.add(commitListWindow.rootWindow)
            commitInfo.children.addAll(
                commitDetailWindow.rootWindow,
                workTreeDetailWindow.rootWindow,
                conflictingFilesWindow.rootWindow
            )
            menuBar.children.add(MenuBar(repositoryModel).rootWindow)
            statusBar.children.add(StatusBar(repositoryModel).rootWindow)
        }

        /** 表示完了時処理 */
        override fun displayCompleted() {
            //分割ウィンドウの位置を再生する
            mainSplit.setDividerPositions(props.split1, props.split2)

            //クローズ時処理を定義する
            mainWindow.rootWindow.scene.window.setOnCloseRequest {
                MainApp.confirmToQuit()
                it.consume()
            }
        }

        /** アイドル時処理 */
        override fun enterIdleState() {
            //ウィンドウのサイズと分割位置をデータベースに保存する
            val stage: Stage = mainWindow.rootWindow.scene.window as Stage
            val width = if(stage.isMaximized) -1 else stage.scene.width.toLong()
            val height = if(stage.isMaximized) -1 else stage.scene.height.toLong()
            modalInfoQueries.updateWindowSize(
                width, height,
                mainSplit.dividerPositions[0],
                mainSplit.dividerPositions[1]
            )
        }

        /**　指定されたレコードへジャンプ */
        override fun jumpCommitList(index: Int) {
            commitListWindow.jumpToIndex(index)
        }

    }

    /** 別スレッドで処理を実行する */
    private fun runTaskInternal(
        function: () -> Unit,
        onStart:  () -> Unit,
        onFinish: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        onStart()
        Thread(object : Task<Unit>() {
            override fun call() {
                try {
                    function()
                } catch (e: Exception) {
                    Platform.runLater { onError(e) }
                } finally {
                    Platform.runLater { onFinish() }
                }
            }
        }).start()
    }

    /** 実行中カーソル表示付きで処理を行う */
    fun runTask(
        function: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val scene = rootWindow.scene
        runTaskInternal(
            function,
            { scene.cursor = Cursor.WAIT    },
            { scene.cursor = Cursor.DEFAULT },
            onError
        )
    }

    /** プログレスバー表示付きで処理を行う */
    fun runTaskWithProgress(
        function: (ProgressMonitorImpl) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val scene = mainWindow.rootWindow.scene
        val window = ProgressWindow()
        runTaskInternal(
            { function(window.monitor) },
            {
                scene.cursor = Cursor.WAIT
                window.show()
            },
            {
                window.hide()
                scene.cursor = Cursor.DEFAULT
            },
            onError
        )
    }
}