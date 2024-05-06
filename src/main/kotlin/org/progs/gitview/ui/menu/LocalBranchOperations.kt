package org.progs.gitview.ui.menu

import org.progs.gitview.model.LocalBranchModel
import org.progs.gitview.model.RepositoryModel
import org.progs.gitview.ui.dialog.ErrorDialog
import org.progs.gitview.ui.window.main.mainWindow

class LocalBranchOperations(
    private val model: LocalBranchModel
) {

    /** チェックアウト(Local) */
    fun checkout(
        onError: () -> Unit = {}
    ) {
        mainWindow.runTaskWithProgress(
            function = { monitor ->
                model.checkoutFromRemote(monitor)
                model.updateLocalBranchList()
            },
            onError = { e ->
                ErrorDialog(e).showDialog()
                onError()
            }
        )
    }

    /** マージ */
    fun merge(
        message: String,
        onError: () -> Unit = {}
    ) {
        mainWindow.runTaskWithProgress(
            function = { monitor ->
                model.mergeToCurrentBranch(message, monitor)
                model.updateLocalBranchList()
            },
            onError = { e ->
                ErrorDialog(e).showDialog()
                onError()
            }
        )
    }

    /** ブランチ作成 */
    fun createBranch(
        branchName: String,
        checkout: Boolean,
        onError: () -> Unit = {}
    ) {
        mainWindow.runTask(
            function = {
                model.createNewBranch(branchName, checkout)
                model.updateLocalBranchList()
            },
            onError = { e ->
                ErrorDialog(e).showDialog()
                onError()
            }
        )
    }

    /** リベース */
    fun rebase(
        onError: () -> Unit = {}
    ) {
        mainWindow.runTask(
            function = {
                model.rebaseCurrentBranch()
                model.updateLocalBranchList()
            },
            onError = { e ->
                ErrorDialog(e).showDialog()
                onError()
            }
        )
    }

    /** プッシュ */
    fun push(
        onError: () -> Unit = {}
    ) {
        mainWindow.runTask(
            function = {
                model.push(false)    // Push all tags
                model.updateRemoteBranchList()
            },
            onError = { e ->
                ErrorDialog(e).showDialog()
                onError()
            }
        )
    }

    /** プル */
    fun pull(
        onError: () -> Unit = {}
    ) {
        mainWindow.runTaskWithProgress(
            function = { monitor ->
                model.pull(monitor)
                model.updateLocalBranchList()
            },
            onError = { e ->
                ErrorDialog(e).showDialog()
                onError()
            }
        )
    }

    /** ブランチ削除(Local) */
    fun remove(
        force: Boolean,
        onError: () -> Unit = {}
    ) {
        mainWindow.runTaskWithProgress(
            function = { monitor ->
                model.remove(force, monitor)
                model.updateLocalBranchList()
            },
            onError = { e ->
                ErrorDialog(e).showDialog()
                onError()
            }
        )
    }

    /** ブランチ名称変更 */
    fun rename(
        newName: String,
        onError: () -> Unit = {}
    ) {
        mainWindow.runTask(
            function = {
                model.rename(newName)
                model.updateLocalBranchList()
            },
            onError = { e ->
                ErrorDialog(e).showDialog()
                onError()
            }
        )
    }

    companion object {

        /** 進行中のマージをキャンセルする */
        fun cancelMerge(
            repositoryModel: RepositoryModel,
            onError: () -> Unit = {}
        ) {
            mainWindow.runTaskWithProgress(
                function = { monitor ->
                    repositoryModel.abortMerge(monitor)
                    repositoryModel.branchListModel.updateLocalBranchList()
                },
                onError = { e ->
                    ErrorDialog(e).showDialog()
                    onError()
                }
            )
        }

        /** 進行中のチェリーピックをキャンセルする */
        fun cancelCherryPick(
            repositoryModel: RepositoryModel,
            onError: () -> Unit = {}
        ) {
            mainWindow.runTaskWithProgress(
                function = { monitor ->
                    repositoryModel.abortCherryPick(monitor)
                    repositoryModel.branchListModel.updateLocalBranchList()
                },
                onError = { e ->
                    ErrorDialog(e).showDialog()
                    onError()
                }
            )
        }
    }
}