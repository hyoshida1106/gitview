package org.progs.gitview.ui.window.main

import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.Label
import org.progs.gitview.model.RepositoryModel
import org.progs.gitview.ui.window.BaseControl
import org.progs.gitview.ui.window.BaseWindow

/**
 * ステータスバー
 */
class StatusBar(
    repositoryModel: RepositoryModel
): BaseWindow<StatusBar.Control>(Control(repositoryModel)){

    class Control(
        private val repositoryModel: RepositoryModel
    ): BaseControl() {
        @FXML private lateinit var repositoryPath: Label

        /** 表示完了時の処理 */
        override fun displayCompleted() {
            repositoryModel.addListener { Platform.runLater { updateContents() } }
        }

        /** リポジトリモデル更新時処理 */
        private fun updateContents() {
            repositoryPath.text = "%s [%s]".format(
                repositoryModel.localRepositoryPath ?: "",
                repositoryModel.currentBranch ?: ""
            )
        }
    }
}
