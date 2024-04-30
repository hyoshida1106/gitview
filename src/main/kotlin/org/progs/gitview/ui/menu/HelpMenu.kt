package org.progs.gitview.ui.menu

import javafx.event.EventHandler
import javafx.scene.control.Menu
import org.progs.gitview.MainApp.Companion.resourceBundle
import org.progs.gitview.model.RepositoryModel
import org.progs.gitview.ui.dialog.AboutDialog
import org.progs.gitview.ui.dialog.PreferenceDialog

class HelpMenu(
    private val repositoryModel: RepositoryModel
): Menu(resourceBundle.getString("HelpMenu.Title")) {

    /** ライセンス */
    private val licenceMenuItem = MenuItem(
        text = resourceBundle.getString("HelpMenu.About")
    ) {
        AboutDialog().showDialog()
    }

    // 設定
    private val preferenceMenuItem = MenuItem(
        text = resourceBundle.getString("HelpMenu.Preference")
    ) {
        PreferenceDialog().showDialog()
    }

    /** 初期化 */
    init {
        items.setAll(
            licenceMenuItem,
            preferenceMenuItem
        )
        onShowing = EventHandler { onShowingMenu() }
    }

    /** メニュー表示 */
    private fun onShowingMenu() {
    }
}