package org.progs.gitview.ui.menu

import javafx.event.EventHandler
import javafx.scene.control.ContextMenu
import javafx.scene.control.SeparatorMenuItem
import org.progs.gitview.MainApp
import org.progs.gitview.model.RepositoryModel
import org.progs.gitview.model.TagModel
import org.progs.gitview.ui.alert.RemoveTagConfirm
import org.progs.gitview.ui.util.IconCode

class TagContextMenu(
    repositoryModel: RepositoryModel,
    private val model: TagModel
): ContextMenu()  {

    private val tagOperations = TagOperations(repositoryModel, model)

    /** ジャンプ */
    private val jumpMenuItem = MenuItem(
        text = MainApp.resourceBundle.getString("TagContextMenu.Jump"),
        iconCode = IconCode.SHARE_FORWARD_LINE
    ) {
        tagOperations.jump()
    }

    /** 削除 */
    private val removeMenuItem = MenuItem(
        text = MainApp.resourceBundle.getString("TagContextMenu.Remove"),
        iconCode = IconCode.DELETE_BIN_LINE
    ) {
        if(RemoveTagConfirm(model.name).showDialog()) {
            tagOperations.remove()
        }
    }

    /** 初期化 */
    init {
        items.setAll(
            jumpMenuItem,
            SeparatorMenuItem(),
            removeMenuItem
        )
        onShowing = EventHandler { onMyShowing() }
    }

    /** メニュー表示時処理 */
    private fun onMyShowing() {
    }
}