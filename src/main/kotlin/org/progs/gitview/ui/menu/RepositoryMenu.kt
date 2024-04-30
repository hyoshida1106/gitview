package org.progs.gitview.ui.menu

import javafx.event.EventHandler
import javafx.scene.control.ButtonType
import javafx.scene.control.Menu
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import org.progs.gitview.Database
import org.progs.gitview.LastOpenedFilesQueries
import org.progs.gitview.MainApp
import org.progs.gitview.MainApp.Companion.resourceBundle
import org.progs.gitview.model.RepositoryModel
import org.progs.gitview.ui.dialog.DirectoryChooser
import org.progs.gitview.ui.dialog.RepositoryCloneDialog
import org.progs.gitview.ui.dialog.selectDirectory
import org.progs.gitview.ui.util.IconCode


/**
 * 「リポジトリ」メニュー
 */
class RepositoryMenu(
    private val repositoryModel: RepositoryModel
): Menu(resourceBundle.getString("RepositoryMenu.title")) {

    private val repositoryOperations = RepositoryOperations(repositoryModel)

    /** 過去にオープンしたリポジトリを記録するデータベース */
    private val lastOpenedFilesNumber = 8
    private val databaseQuery = Database(MainApp.sqlDriver).lastOpenedFilesQueries
    private val lastOpenedFilesQuery = databaseQuery.select(lastOpenedFilesNumber.toLong())

    /** 履歴の先頭に移動するために、削除と再設定を実施する */
    private fun LastOpenedFilesQueries.moveToTop(filePath: String) {
        remove(filePath)
        insert(filePath)
    }

    /** リポジトリを開く(_O)... */
    private val openMenu = MenuItem(
        text = resourceBundle.getString("RepositoryMenu.open"),
        accelerator = KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN),
        iconCode = IconCode.FOLDER_OPEN_LINE
    ) {
        val title = resourceBundle.getString("Message.OpenRepositoryPathMessage")
        DirectoryChooser(title).selectDirectory()?.let { file ->
            val filePath = file.absolutePath
            databaseQuery.moveToTop(filePath)
            repositoryOperations.open(filePath){
                //エラーになったものは削除する
                databaseQuery.remove(filePath)
            }
        }
    }

    /**  新規リポジトリ(_N)... */
    private val createMenu = MenuItem(
        text = resourceBundle.getString("RepositoryMenu.create"),
        accelerator = KeyCodeCombination(
            KeyCode.N,
            KeyCombination.SHORTCUT_DOWN
        ),
        iconCode = IconCode.FOLDER_ADD_LINE
    ) {
        val title = resourceBundle.getString("Message.CreateNewRepositoryPathMessage")
        DirectoryChooser(title).selectDirectory()?.let { file ->
            val filePath = file.absolutePath
            databaseQuery.moveToTop(filePath)
            repositoryOperations.create(filePath) {
                //エラーになったものは削除する
                databaseQuery.remove(filePath)
            }
        }
    }

    /** クローン(_C)... */
    private val cloneMenu = MenuItem(
        text = resourceBundle.getString("RepositoryMenu.clone"),
        accelerator = KeyCodeCombination(
            KeyCode.C,
            KeyCombination.SHORTCUT_DOWN
        ),
        iconCode = IconCode.FOLDER_DOWNLOAD_LINE
    ) {
        val dialog = RepositoryCloneDialog("", "")
        if (dialog.showDialog() == ButtonType.OK) {
            databaseQuery.moveToTop(dialog.localPath)
            repositoryOperations.clone(dialog.localPath, dialog.remotePath, dialog.bareRepo) {
                //エラーになったものは削除する
                databaseQuery.remove(dialog.localPath)
            }
        }
    }

    /** フェッチ */
    private val fetchMenu = MenuItem(
        text = resourceBundle.getString("RepositoryMenu.fetch"),
        accelerator = KeyCodeCombination(
            KeyCode.F,
            KeyCombination.SHORTCUT_DOWN
        ),
    ) {
        repositoryOperations.fetch()
    }

    /** 閉じる */
    private val closeMenu = MenuItem(
        text = resourceBundle.getString("RepositoryMenu.close"),
        accelerator = KeyCodeCombination(
            KeyCode.L,
            KeyCombination.SHORTCUT_DOWN
        ),
        iconCode = IconCode.FOLDER_CLOSE_LINE
    ) {
        repositoryOperations.close()
    }

    /** 終了(_X) */
    private val quitMenu = MenuItem(
        text = resourceBundle.getString("RepositoryMenu.Quit"),
        iconCode = IconCode.SHUT_DOWN_LINE
    ) {
        MainApp.confirmToQuit()
    }

    /**  以前に開いたファイル用メニューのプレースホルダ */
    private val lastFileMenuArray = Array(lastOpenedFilesNumber){ index ->
        MenuItem(
            text = "",
            iconCode = '1'.code + index
        ) { event ->
            //リポジトリを開く - ファイル指定時と同じ処理
            val filePath = (event.source as MenuItem).text
            databaseQuery.moveToTop(filePath)
            repositoryOperations.open(filePath) {
                //エラー時は履歴から削除
                databaseQuery.remove(filePath)
            }
        }.apply {
            isVisible = false
            isDisable = true
        }
    }

    /** インスタンス初期化 */
    init {
        items.setAll(
            createMenu,             //新規
            openMenu,               //開く
            cloneMenu,              //クローン
            SeparatorMenuItem()
        )
        items.addAll(               //以前使用したリポジトリのリスト
            lastFileMenuArray
        )
        items.addAll(
            SeparatorMenuItem(),
            fetchMenu,              //フェッチ
            closeMenu,              //閉じる
            SeparatorMenuItem(),
            quitMenu                //アプリケーションの終了
        )
        onShowing = EventHandler { onShowingMenu() }
    }

    /** メニュー表示時処理 */
    private fun onShowingMenu() {
        // メニューの有効/無効を設定
        fetchMenu.isDisable = repositoryModel.remoteRepositoryName == null
        closeMenu.isDisable = !repositoryModel.available

        //　過去にオープンしたリポジトリのリストを表示
        lastFileMenuArray.forEach { menuItem ->
            menuItem.isVisible = false
            menuItem.isDisable = true
        }
        val lastOpenedFiles = lastOpenedFilesQuery.executeAsList()
        if(lastOpenedFiles.isEmpty()) {
            lastFileMenuArray[0].apply {
                text = resourceBundle.getString("RepositoryMenu.noFile")
                isVisible = true
            }
        } else for(index in 0..lastOpenedFiles.lastIndex) {
            lastFileMenuArray[index].apply {
                text = lastOpenedFiles[index].file_name
                isVisible = true
                isDisable = false
            }
        }
    }

}
