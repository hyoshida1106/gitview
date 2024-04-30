package org.progs.gitview.ui.menu

import org.progs.gitview.model.RepositoryModel
import org.progs.gitview.model.TagModel
import org.progs.gitview.ui.alert.TagOutOfRangeAlert
import org.progs.gitview.ui.dialog.ErrorDialog
import org.progs.gitview.ui.window.main.MainWindow

class TagOperations(
    private val repositoryModel: RepositoryModel,
    private val model: TagModel
) {
    /** タグ削除 */
    fun remove(
        onError: () -> Unit = {}
    ) {
            MainWindow.runTask(
                function = {
                    model.remove()
                    repositoryModel.branchListModel.updateTagList()
                    repositoryModel.commitListModel.updateCommitList()
                },
                onError = { e ->
                    ErrorDialog(e).showDialog()
                    onError()
                }
            )
    }

    /** ジャンプ */
    fun jump() {
        var index = repositoryModel.commitListModel.getIndexById(model.id)
        if(index != null) {
            if (!repositoryModel.commitListModel.workTreeFiles.isEmpty) {
                //先頭がWorkTree行なら１増やす
                index += 1
            }
            MainWindow.controller.jumpCommitList(index)
        } else {
            TagOutOfRangeAlert(model.name).show()
        }
    }
}
