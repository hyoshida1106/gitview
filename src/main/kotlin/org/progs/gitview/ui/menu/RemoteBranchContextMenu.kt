package org.progs.gitview.ui.menu

import javafx.event.EventHandler
import javafx.scene.control.ContextMenu
import org.progs.gitview.MainApp.Companion.resourceBundle
import org.progs.gitview.model.RemoteBranchModel
import org.progs.gitview.ui.alert.ConfirmationType
import org.progs.gitview.ui.alert.confirm
import org.progs.gitview.ui.util.IconCode

class RemoteBranchContextMenu(
    private val model: RemoteBranchModel
): ContextMenu() {

    /**
     * チェックアウト
     */
    private val checkOutMenuItem = MenuItem(
        text = resourceBundle.getString("RemoteBranchContextMenu.Checkout"),
        iconCode = IconCode.DOWNLOAD_2_FILL
    ) {
        RemoteBranchOperations(model).checkout()
    }

    /**
     * 削除
     */
    private val removeMenuItem = MenuItem(
        text = resourceBundle.getString("RemoteBranchContextMenu.Remove"),
        iconCode = IconCode.DELETE_BIN_LINE
    ) {
        val message = resourceBundle.getString("Message.RemoteBranchContextMenu.Remove")
            .format(model.name)
        if (confirm(ConfirmationType.YesNo, message)) {
            RemoteBranchOperations(model).remove()
        }
    }

    /**
     * 初期化
     */
    init {
        items.setAll(
            checkOutMenuItem,
            removeMenuItem
        )
        onShowing = EventHandler { onMyShowing() }
    }

    /**
     * 表示処理
     */
    private fun onMyShowing() {
        checkOutMenuItem.isDisable = model.localBranch != null
        removeMenuItem.isDisable = true    //とりあえず
    }

}