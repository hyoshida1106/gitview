package org.progs.gitview.ui.menu

import org.progs.gitview.model.RepositoryModel
import org.progs.gitview.ui.dialog.ErrorDialog
import org.progs.gitview.ui.window.main.MainWindow


class RepositoryOperations(
    val repositoryModel: RepositoryModel
) {

    /** リポジトリを開く */
    fun open(
        filePath: String,
        onError: () -> Unit = {}
    ) {
        MainWindow.runTask(
            function = {
                repositoryModel.open(filePath)
            },
            onError = { e ->
                ErrorDialog(e).showDialog()
                onError()
            }
        )
    }

    /** リポジトリを新規作成する */
    fun create(
        filePath: String,
        onError: () -> Unit = {}
    ) {
        MainWindow.runTask(
            function = {
                repositoryModel.create(filePath)
            },
            onError = { e ->
                ErrorDialog(e).showDialog()
                onError()
            }
        )
    }

    /** リポジトリのクローンを作成する */
    fun clone(
        localPath: String,
        remotePath: String,
        isBare: Boolean,
        onError: () -> Unit = {}
    ) {
        MainWindow.runTaskWithProgress(
            function = { monitor ->
                repositoryModel.clone(monitor, localPath, remotePath, isBare)
            },
            onError = { e ->
                ErrorDialog(e).showDialog()
                onError()
            }
        )
    }

    /** フェッチ */
    fun fetch(
        onError: () -> Unit = {}
    ) {
        MainWindow.runTaskWithProgress(
            function = { monitor ->
                repositoryModel.fetch(monitor)
            },
            onError = { e ->
                ErrorDialog(e).showDialog()
                onError()
            }
        )
    }

    /** 閉じる */
    fun close() {
        repositoryModel.closeRepository()
    }
}