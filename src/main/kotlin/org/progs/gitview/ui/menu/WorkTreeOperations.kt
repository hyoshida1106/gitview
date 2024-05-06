package org.progs.gitview.ui.menu

import javafx.scene.control.ButtonType
import org.eclipse.jgit.diff.DiffEntry
import org.progs.gitview.git.commit.CommitFile
import org.progs.gitview.model.RepositoryModel
import org.progs.gitview.ui.alert.RevertFilesConfirm
import org.progs.gitview.ui.dialog.CommitDialog
import org.progs.gitview.ui.dialog.ErrorDialog
import org.progs.gitview.ui.dialog.RevertDialog
import org.progs.gitview.ui.dialog.UserNameDialog
import org.progs.gitview.ui.window.main.mainWindow

class WorkTreeOperations(
    private val repositoryModel: RepositoryModel,
) {
    /** ステージ */
    fun stage(
        files: List<CommitFile>,
        onError: () -> Unit = {}
    ) {
        mainWindow.runTask(
            function = {
                val fileToAdd = files.filter {
                    arrayOf(
                        DiffEntry.ChangeType.ADD,
                        DiffEntry.ChangeType.MODIFY,
                        DiffEntry.ChangeType.RENAME
                    ).contains(it.entry.changeType)
                }
                val fileToDelete = files.filter {
                    it.entry.changeType == DiffEntry.ChangeType.DELETE
                }
                repositoryModel.add(fileToAdd)
                repositoryModel.delete(fileToDelete)
                repositoryModel.commitListModel.updateWorkTreeFiles()
            },
            onError = { e ->
                ErrorDialog(e).showDialog()
                onError()
            }
        )
    }

    /** アンステージ */
    fun unStage(
        files: List<CommitFile>,
        onError: () -> Unit = {}
    ) {
        mainWindow.runTask(
            function = {
                repositoryModel.unStage(files)
                repositoryModel.commitListModel.updateWorkTreeFiles()
            },
            onError = { e ->
                ErrorDialog(e).showDialog()
                onError()
            }
        )
    }

    /** コミット */
    fun commit(
        files: List<CommitFile>,
        selectedIndices: List<Int>,
        onError: () -> Unit = {}
    ) {
        // ユーザ名、メールアドレス取得
        var userName = repositoryModel.userName ?: ""
        var mailAddr = repositoryModel.mailAddr ?: ""
        while(userName.isEmpty() || mailAddr.isEmpty()) {
            val dialog = UserNameDialog(userName, mailAddr)
            if(dialog.showDialog() != ButtonType.OK) return
            userName = dialog.userName
            mailAddr = dialog.mailAddr
        }

        // コミットメッセージ入力
        val dialog = CommitDialog(files, selectedIndices, repositoryModel.mergeMessage)
        if(dialog.showDialog() == ButtonType.OK) {
            val message = dialog.message
            mainWindow.runTask(
                function = {
                    repositoryModel.commit(userName, mailAddr, message, dialog.controller.selectedFiles)
                    repositoryModel.branchListModel.updateLocalBranchList()
                },
                onError = { e ->
                    ErrorDialog(e).showDialog()
                    onError()
                }
            )
        }
    }

    /** リバース */
    fun revert(
        files: List<CommitFile>,
        selectedIndices: List<Int>,
        onError: () -> Unit = {}
    ) {
        val dialog = RevertDialog(files, selectedIndices)
        if(dialog.showDialog() != ButtonType.OK) {
            return
        }
        val selectedFiles = dialog.controller.selectedFiles

        if(RevertFilesConfirm(selectedFiles.map { it.path }).showDialog()) {
            mainWindow.runTask(
                function = {
                    repositoryModel.checkout(selectedFiles)
                    repositoryModel.commitListModel.updateWorkTreeFiles()
                },
                onError = { e ->
                    ErrorDialog(e).showDialog()
                    onError()
                }
            )
        }
    }
}