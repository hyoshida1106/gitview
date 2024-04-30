package org.progs.gitview.ui.menu

import javafx.event.EventHandler
import javafx.scene.control.ButtonType
import javafx.scene.control.ContextMenu
import javafx.scene.control.SeparatorMenuItem
import org.progs.gitview.MainApp.Companion.resourceBundle
import org.progs.gitview.model.LocalBranchModel
import org.progs.gitview.model.RepositoryModel
import org.progs.gitview.ui.alert.RemoveLocalBranchConfirm
import org.progs.gitview.ui.alert.ConfirmationType
import org.progs.gitview.ui.alert.confirm
import org.progs.gitview.ui.dialog.BranchCreationDialog
import org.progs.gitview.ui.dialog.MergeBranchDialog
import org.progs.gitview.ui.dialog.RenameBranchDialog
import org.progs.gitview.ui.util.IconCode

class LocalBranchContextMenu(
    private val repositoryModel: RepositoryModel,
    private val model: LocalBranchModel
): ContextMenu() {

    private val localBranchOperations = LocalBranchOperations(model)

    /**
     *  選択したブランチをチェックアウト
     */
    private val checkOutMenuItem = MenuItem(
        text = resourceBundle.getString("LocalBranchContextMenu.Checkout"),
        iconCode = IconCode.DOWNLOAD_2_FILL,
        bold = true
    ) {
        localBranchOperations.checkout()
    }

    /**
     *  選択したブランチを現在のブランチにマージ
     */
    private val mergeMenuItem = MenuItem(
        text = resourceBundle.getString("LocalBranchContextMenu.Merge"),
        iconCode = IconCode.GIT_MERGE_LINE
    ) {
        val srcBranch = model.name
        val dstBranch = model.branchListModel.currentBranch
        val dialog = MergeBranchDialog(srcBranch, dstBranch)
        if (dialog.showDialog() == ButtonType.OK) {
            localBranchOperations.merge(dialog.message)
        }
    }

    /**
     *  選択したブランチからのブランチ生成
     */
    private val branchMenuItem = MenuItem(
        text = resourceBundle.getString("LocalBranchContextMenu.Branch"),
        iconCode = IconCode.GIT_BRANCH_LINE
    ) {
        val dialog = BranchCreationDialog()
        if (dialog.showDialog() == ButtonType.OK) {
            val branchName = dialog.controller.newBranchName
            val checkout = dialog.controller.checkoutFlag
            localBranchOperations.createBranch(branchName, checkout)
        }
    }

    /**
     *  現在のブランチをリベース
     */
    private val rebaseMenuItem = MenuItem(
        text = resourceBundle.getString("LocalBranchContextMenu.Rebase"),
        iconCode = IconCode.GIT_BRANCH_FILL
    ) {
        val srcBranch = repositoryModel.currentBranch ?: "HEAD"
        val message = resourceBundle.getString("Message.LocalBranchContextMenu.Rebase")
            .format(srcBranch)
        if (confirm(ConfirmationType.YesNo, message)) {
            localBranchOperations.rebase()
        }
    }

    /**
     *  プル
     */
    private val pullMenuItem = MenuItem(
        text = resourceBundle.getString("LocalBranchContextMenu.Pull"),
        iconCode = IconCode.ARROW_DOWN_CIRCLE_LINE,
    ) {
        localBranchOperations.pull()
    }

    /**
     * プッシュ
     */
    private val pushMenuItem = MenuItem(
        text = resourceBundle.getString("LocalBranchContextMenu.Push"),
        iconCode = IconCode.ARROW_UP_CIRCLE_LINE
    ) {
        localBranchOperations.push()
    }

    /**
     *  名前の変更
     */
    private val renameMenuItem = MenuItem(
        text = resourceBundle.getString("LocalBranchContextMenu.Rename"),
        iconCode = IconCode.BALL_PEN_LINE
    ) {
        val dialog = RenameBranchDialog(model.name)
        if(dialog.showDialog() == ButtonType.OK) {
            localBranchOperations.rename(dialog.branchName)
        }
    }

    /** 削除 */
    private val removeMenuItem = MenuItem(
        text = resourceBundle.getString("LocalBranchContextMenu.Remove"),
        iconCode = IconCode.DELETE_BIN_LINE
    ) {
        val alert = RemoveLocalBranchConfirm(model.name)
        if(alert.showDialog()) {
            localBranchOperations.remove(alert.forceRemove)
        }
    }

    /**
     * 初期化
     */
    init {
        items.setAll(
            checkOutMenuItem,
            SeparatorMenuItem(),
            mergeMenuItem,
            branchMenuItem,
            rebaseMenuItem,
            SeparatorMenuItem(),
            pushMenuItem,
            pullMenuItem,
            SeparatorMenuItem(),
            renameMenuItem,
            removeMenuItem
        )
        onShowing = EventHandler { onMyShowing() }
    }


    /**
     * メニュー表示時処理
     */
    private fun onMyShowing() {
        checkOutMenuItem.isDisable = model.isCurrentBranch
        mergeMenuItem.isDisable = model.isCurrentBranch
        rebaseMenuItem.isDisable = model.isCurrentBranch
        pushMenuItem.isDisable = !model.isCurrentBranch
        pullMenuItem.isDisable = !model.isCurrentBranch || model.remoteBranchPath == null
        removeMenuItem.isDisable = model.isCurrentBranch
    }
}
