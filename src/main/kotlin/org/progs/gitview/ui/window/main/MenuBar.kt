package org.progs.gitview.ui.window.main

import javafx.fxml.FXML
import org.progs.gitview.model.RepositoryModel
import org.progs.gitview.ui.menu.BranchMenu
import org.progs.gitview.ui.menu.CommitMenu
import org.progs.gitview.ui.menu.HelpMenu
import org.progs.gitview.ui.menu.RepositoryMenu
import org.progs.gitview.ui.window.BaseControl
import org.progs.gitview.ui.window.BaseWindow

/**
 * メニューバー
 */
class MenuBar(
    repositoryModel: RepositoryModel
): BaseWindow<MenuBar.MenuBarControl>(MenuBarControl(repositoryModel)) {

    /**
     * コントロールクラス
     */
    class MenuBarControl(
        repositoryModel: RepositoryModel
    ): BaseControl() {
        @FXML private lateinit var menuBar: javafx.scene.control.MenuBar

        private val repositoryMenu = RepositoryMenu(repositoryModel)
        private val branchMenu = BranchMenu(repositoryModel)
        private val commitMenu = CommitMenu(repositoryModel)
        private val helpMenu = HelpMenu(repositoryModel)

        /** 初期化 */
        fun initialize() {
            menuBar.menus.addAll(
                repositoryMenu,
                branchMenu,
                commitMenu,
                helpMenu
            )
        }

    }
}
