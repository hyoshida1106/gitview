package org.progs.gitview.ui.menu

import javafx.event.EventHandler
import javafx.scene.control.ButtonType
import javafx.scene.control.Menu
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import org.progs.gitview.MainApp.Companion.resourceBundle
import org.progs.gitview.model.RepositoryModel
import org.progs.gitview.ui.alert.ConfirmationType
import org.progs.gitview.ui.alert.RemoveLocalBranchConfirm
import org.progs.gitview.ui.alert.confirm
import org.progs.gitview.ui.dialog.BranchCreationDialog
import org.progs.gitview.ui.dialog.MergeBranchDialog
import org.progs.gitview.ui.dialog.RenameBranchDialog
import org.progs.gitview.ui.dialog.SelectBranchDialog
import org.progs.gitview.ui.util.IconCode

class BranchMenu(
    private val repositoryModel: RepositoryModel
): Menu(resourceBundle.getString("BranchMenu.Title")) {

    /** 選択したブランチをチェックアウト */
    private val checkOutMenuItem = MenuItem(
        text = resourceBundle.getString("BranchMenu.Checkout"),
        accelerator = KeyCodeCombination(
            KeyCode.O,
            KeyCombination.SHORTCUT_DOWN,
            KeyCombination.SHIFT_DOWN
        ),
        iconCode = IconCode.DOWNLOAD_2_FILL
    ) {
        val dialog = SelectBranchDialog(
            repositoryModel,
            resourceBundle.getString("SelectBranchDialog.Checkout")
        )
        if(dialog.showDialog() == ButtonType.OK) {
            dialog.selectedModel?.let { model ->
                if(model.local != null) {
                    LocalBranchOperations(model.local).checkout()
                } else if(model.remote != null){
                    RemoteBranchOperations(model.remote).checkout()
                }
            }
        }
    }

    /** 選択したブランチをプッシュ */
    private val pushMenuItem = MenuItem(
        text = resourceBundle.getString("BranchMenu.Push"),
        accelerator = KeyCodeCombination(
            KeyCode.P,
            KeyCombination.SHORTCUT_DOWN,
            KeyCombination.SHIFT_DOWN
        ),
        iconCode = IconCode.UPLOAD_CLOUD_2_LINE
    ) {
        val dialog = SelectBranchDialog(
            repositoryModel,
            resourceBundle.getString("SelectBranchDialog.Push")
        ) { _, local -> local != null }
        if(dialog.showDialog() == ButtonType.OK) {
            dialog.selectedModel?.local?.let { model ->
                LocalBranchOperations(model).push() }
        }
    }

    /** 選択したブランチをプル */
    private val pullMenuItem = MenuItem(
        text = resourceBundle.getString("BranchMenu.Pull"),
        accelerator = KeyCodeCombination(
            KeyCode.L,
            KeyCombination.SHORTCUT_DOWN,
            KeyCombination.SHIFT_DOWN
        ),
        iconCode = IconCode.DOWNLOAD_CLOUD_2_LINE
    ) {
        val dialog = SelectBranchDialog(
            repositoryModel,
            resourceBundle.getString("SelectBranchDialog.Pull")
        ) { remote, local -> remote != null && local != null }
        if(dialog.showDialog() == ButtonType.OK) {
            dialog.selectedModel?.local?.let { model ->
                LocalBranchOperations(model).pull() }
        }
    }

    /** 選択したブランチを現在のブランチにマージ */
    private val mergeMenuItem = MenuItem(
        text = resourceBundle.getString("BranchMenu.Merge"),
        accelerator = KeyCodeCombination(
            KeyCode.M,
            KeyCombination.SHORTCUT_DOWN,
            KeyCombination.SHIFT_DOWN
        ),
        iconCode = IconCode.GIT_MERGE_LINE
    ) {
        //対象ブランチの選択
        val selectBranchDialog = SelectBranchDialog(
            repositoryModel,
            resourceBundle.getString("SelectBranchDialog.Merge")
        ) { _, local -> local != null && !local.isCurrentBranch }
        if(selectBranchDialog.showDialog() == ButtonType.OK) {
            //実行確認
            selectBranchDialog.selectedModel?.local?.let { model ->
                val srcBranch = model.name
                val dstBranch = model.branchListModel.currentBranch
                val mergeDialog = MergeBranchDialog(srcBranch, dstBranch)
                if (mergeDialog.showDialog() == ButtonType.OK) {
                    LocalBranchOperations(model).merge(mergeDialog.message)
                }
            }
        }
    }

    /** 現在のブランチをリベース */
    private val rebaseMenuItem = MenuItem(
        text = resourceBundle.getString("BranchMenu.Rebase"),
        iconCode = IconCode.GIT_BRANCH_FILL
    ) {
        //対象ブランチの選択
        val selectBranchDialog = SelectBranchDialog(
            repositoryModel,
            resourceBundle.getString("SelectBranchDialog.Rebase")
        ) { _, local -> local != null && !local.isCurrentBranch }
        if(selectBranchDialog.showDialog() == ButtonType.OK) {
            //実行確認
            selectBranchDialog.selectedModel?.local?.let { model ->
                val srcBranch = repositoryModel.currentBranch ?: "HEAD"
                val message = resourceBundle.getString("Message.LocalBranchContextMenu.Rebase")
                    .format(srcBranch)
                if (confirm(ConfirmationType.YesNo, message)) {
                    LocalBranchOperations(model).rebase()
                }
            }
        }
    }

    /** 新規作成 */
    private val createMenuItem = MenuItem(
        text = resourceBundle.getString("BranchMenu.Create"),
        iconCode = IconCode.GIT_BRANCH_LINE
    ) {
        //対象ブランチの選択
        val selectBranchDialog = SelectBranchDialog(
            repositoryModel,
            resourceBundle.getString("SelectBranchDialog.Create")
        ) { _, local -> local != null }
        if(selectBranchDialog.showDialog() == ButtonType.OK) {
            //実行確認
            selectBranchDialog.selectedModel?.local?.let { model ->
                val dialog = BranchCreationDialog()
                if (dialog.showDialog() == ButtonType.OK) {
                    val branchName = dialog.controller.newBranchName
                    val checkout = dialog.controller.checkoutFlag
                    LocalBranchOperations(model).createBranch(branchName, checkout)
                }
            }
        }
    }

    /** 名前の変更 */
    private val renameMenuItem = MenuItem(
        text = resourceBundle.getString("BranchMenu.Rename"),
        iconCode = IconCode.BALL_PEN_LINE
    ) {
        //対象ブランチの選択
        val selectBranchDialog = SelectBranchDialog(
            repositoryModel,
            resourceBundle.getString("SelectBranchDialog.Rename")
        ) { _, local -> local != null }
        if(selectBranchDialog.showDialog() == ButtonType.OK) {
            //実行確認
            selectBranchDialog.selectedModel?.local?.let { model ->
                val dialog = RenameBranchDialog(model.name)
                if (dialog.showDialog() == ButtonType.OK) {
                    LocalBranchOperations(model).rename(dialog.branchName)
                }
            }
        }
    }

    /** 削除 */
    private val removeMenuItem = MenuItem(
        text = resourceBundle.getString("BranchMenu.Remove"),
        iconCode = IconCode.DELETE_BIN_LINE
    ) {
        //対象ブランチの選択
        val selectBranchDialog = SelectBranchDialog(
            repositoryModel,
            resourceBundle.getString("SelectBranchDialog.Remove")
        ) { _, local -> local != null && !local.isCurrentBranch}
        if(selectBranchDialog.showDialog() == ButtonType.OK) {
            //実行確認
            selectBranchDialog.selectedModel?.local?.let { model ->
                val dialog = RemoveLocalBranchConfirm(model.name)
                if(dialog.showDialog()) {
                    LocalBranchOperations(model).remove(dialog.forceRemove)
                }
            }
        }
    }

    /** 初期化 */
    init {
        items.setAll(
            createMenuItem,             //新規作成
            checkOutMenuItem,           //チェックアウト
            SeparatorMenuItem(),
            mergeMenuItem,              //マージ
            rebaseMenuItem,             //リベース
            renameMenuItem,             //名称変更
            SeparatorMenuItem(),
            pushMenuItem,               //プッシュ
            pullMenuItem,               //プル
            SeparatorMenuItem(),
            removeMenuItem              //削除
        )
        onShowing = EventHandler { onShowingMenu() }
    }

    /** メニュー表示 */
    private fun onShowingMenu() {
        listOf(
            createMenuItem,             //新規作成
            checkOutMenuItem,           //チェックアウト
            mergeMenuItem,              //マージ
            rebaseMenuItem,             //リベース
            renameMenuItem,             //名称変更
            pushMenuItem,               //プッシュ
            pullMenuItem,               //プル
            removeMenuItem              //削除
        ).forEach { it.isDisable = !repositoryModel.available }
    }
}