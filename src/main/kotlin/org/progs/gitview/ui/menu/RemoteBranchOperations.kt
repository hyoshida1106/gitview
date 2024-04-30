package org.progs.gitview.ui.menu

import org.progs.gitview.model.RemoteBranchModel
import org.progs.gitview.ui.dialog.ErrorDialog
import org.progs.gitview.ui.window.main.MainWindow

class RemoteBranchOperations(
    private val model: RemoteBranchModel
){

    /**
     * チェックアウト(Remote)
     */
    fun checkout(
        onError: () -> Unit = {}
    ) {
        MainWindow.runTaskWithProgress(
            function = { monitor ->
                model.checkoutToLocal(monitor)
                model.updateLocalBranchList()
            },
            onError = { e ->
                ErrorDialog(e).showDialog()
                onError()
            }
        )
    }

    /**
     * ブランチ削除(Remote)
     */
    fun remove(
        onError: () -> Unit = {}
    ) {
        MainWindow.runTaskWithProgress(
            function = { monitor ->
                model.remove(monitor)
                model.updateRemoteBranchList()
            },
            onError = { e ->
                ErrorDialog(e).showDialog()
                onError()
            }
        )
    }
}