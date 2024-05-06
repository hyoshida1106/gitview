package org.progs.gitview.ui.menu

import org.progs.gitview.git.ResetOption
import org.progs.gitview.model.item.CommitInfoItem
import org.progs.gitview.model.RepositoryModel
import org.progs.gitview.ui.alert.PatchCompleteAlert
import org.progs.gitview.ui.dialog.ErrorDialog
import org.progs.gitview.ui.window.main.mainWindow
import java.io.File

class CommitOperations(
    private val repositoryModel: RepositoryModel,
    private val model: CommitInfoItem
) {

    /** チェックアウト(Local) */
    fun checkout(
        onError: () -> Unit = {}
    ) {
        mainWindow.runTaskWithProgress(
            function = { monitor ->
                model.checkout(monitor)
                repositoryModel.branchListModel.updateLocalBranchList()
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
                model.createBranch(branchName, checkout)
                repositoryModel.branchListModel.updateLocalBranchList()
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
        mainWindow.runTask(
            function = {
                model.merge(message)
                repositoryModel.branchListModel.updateLocalBranchList()
            },
            onError = { e ->
                ErrorDialog(e).showDialog()
                onError()
            }
        )
    }

    /** タグ生成 */
    fun createTag(
        tagName: String,
        message: String,
        onError: () -> Unit = {}
    ) {
        mainWindow.runTask(
            function = {
                model.createTag(tagName, message)
                repositoryModel.branchListModel.updateTagList()
                repositoryModel.commitListModel.updateCommitList()
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
                model.rebaseBranch()
                repositoryModel.branchListModel.updateLocalBranchList()
            },
            onError = { e ->
                ErrorDialog(e).showDialog()
                onError()
            }
        )
    }

    /** リセット */
    fun reset(
        option: ResetOption,
        onError: () -> Unit = {}
    ) {
        mainWindow.runTaskWithProgress(
            function = { monitor ->
                model.reset(option, monitor)
                repositoryModel.branchListModel.updateLocalBranchList()
            },
            onError = { e ->
                ErrorDialog(e).showDialog()
                onError()
            }
        )
    }

    /** パッチ生成 */
    fun createPatch(
        file: File,
        onError: () -> Unit = {}
    ) {
        try {
            //パッチファイル書き出し
            val obw = file.outputStream().bufferedWriter()
            model.commitFiles.forEach { commitFile ->
                commitFile.getDiffTextLines().forEach { line ->
                    obw.write(line)
                    obw.newLine()
                }
            }
            obw.close()

            //完了メッセージ
            PatchCompleteAlert(file.name).show()

        } catch(e: Exception) {
            ErrorDialog(e).showDialog()
            onError()
        }
    }

    /** チェリーピック */
    fun cherryPick(
        doCommit: Boolean,
        onError: () -> Unit = {}
    ) {
        mainWindow.runTask(
            function = {
                model.cherryPick(doCommit)
                repositoryModel.branchListModel.updateLocalBranchList()
            },
            onError = { e ->
                ErrorDialog(e).showDialog()
                onError()
            }
        )
    }

}