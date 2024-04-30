package org.progs.gitview.ui.menu

import javafx.event.EventHandler
import javafx.scene.control.ButtonType
import javafx.scene.control.ContextMenu
import javafx.scene.control.SeparatorMenuItem
import javafx.stage.FileChooser.ExtensionFilter
import org.progs.gitview.MainApp.Companion.resourceBundle
import org.progs.gitview.model.item.CommitInfoItem
import org.progs.gitview.model.RepositoryModel
import org.progs.gitview.ui.alert.CherryPickConfirm
import org.progs.gitview.ui.alert.ConfirmationType
import org.progs.gitview.ui.alert.confirm
import org.progs.gitview.ui.dialog.*
import org.progs.gitview.ui.util.IconCode

class CommitContextMenu(
    repositoryModel: RepositoryModel,
    private val model: CommitInfoItem
): ContextMenu()  {

    private val commitOperations = CommitOperations(repositoryModel, model)

    /** チェックアウト */
    private val checkoutMenu = MenuItem(
        text = resourceBundle.getString("CommitContextMenu.Checkout"),
        iconCode = IconCode.DOWNLOAD_2_FILL
    ) {
        if(model.localBranches.isNotEmpty()) {
            LocalBranchOperations(model.localBranches.first()).checkout()
        } else {
            commitOperations.checkout()
        }
    }

    /** ブランチ */
    private val createBranchMenu = MenuItem(
        text = resourceBundle.getString("CommitContextMenu.CreateBranch"),
        iconCode = IconCode.GIT_BRANCH_LINE
    ) {
        val dialog = BranchCreationDialog()
        if (dialog.showDialog() == ButtonType.OK) {
            val branchName = dialog.controller.newBranchName
            val checkout = dialog.controller.checkoutFlag
            commitOperations.createBranch(branchName, checkout)
        }
    }

    /** マージ */
    private val mergeMenu = MenuItem(
        text = resourceBundle.getString("CommitContextMenu.Merge"),
        iconCode = IconCode.GIT_MERGE_LINE
    ) {
        val dialog = MergeCommitDialog(repositoryModel.branchListModel.currentBranch)
        if (dialog.showDialog() == ButtonType.OK) {
            commitOperations.merge(dialog.message)
        }
    }

    /** タグ */
    private val tagMenu = MenuItem(
        text = resourceBundle.getString("CommitContextMenu.Tag"),
        iconCode = IconCode.PRICE_TAG_3_LINE
    ) {
        val dialog = TagCreationDialog()
        if(dialog.showDialog() == ButtonType.OK) {
            commitOperations.createTag(dialog.tagName, dialog.message)
        }
    }

    /** リベース */
    private val rebaseMenu = MenuItem(
        text = resourceBundle.getString("CommitContextMenu.Rebase")
    ) {
        val srcBranch = repositoryModel.currentBranch ?: "HEAD"
        val message = resourceBundle.getString("Message.CommitContextMenu.Rebase")
            .format(srcBranch)
        if (confirm(ConfirmationType.YesNo, message)) {
            commitOperations.rebase()
        }
    }

    /** リセット */
    private val resetMenu = MenuItem(
        text = resourceBundle.getString("CommitContextMenu.Reset"),
        iconCode = IconCode.ARROW_TURN_BACK_LINE
    ) {
        val dialog = ResetDialog()
        if(dialog.showDialog() == ButtonType.OK) {
            commitOperations.reset(dialog.option)
        }
    }

    /** パッチ生成 */
    private val patchMenu = MenuItem(
        text = resourceBundle.getString("CommitContextMenu.CreatePatch"),
    ) {
        val title = resourceBundle.getString("Message.PatchFile")
        val filter = ExtensionFilter("patch", "*.patch")
        FileChooser(title).apply { extensionFilters.add(filter) }.selectSaveFile()?.let { file ->
            commitOperations.createPatch(file)
        }
    }

    /** チェリーピック */
    private val cherryPickMenu = MenuItem(
        text = resourceBundle.getString("CommitContextMenu.CherryPick"),
        iconCode = IconCode.ARROW_TURN_FORWARD_LINE
    ) {
        val srcBranch = repositoryModel.currentBranch ?: "HEAD"
        val dialog = CherryPickConfirm(srcBranch)
        if(dialog.showDialog()) {
            commitOperations.cherryPick(dialog.doCommit)
        }
    }

    /** 初期化 */
    init {
        items.setAll(
            createBranchMenu,
            tagMenu,
            SeparatorMenuItem(),
            checkoutMenu,
            mergeMenu,
            rebaseMenu,
            resetMenu,
            SeparatorMenuItem(),
            patchMenu,
            cherryPickMenu
        )
        onShowing = EventHandler { onMyShowing() }
    }

    /** メニュー表示時処理 */
    private fun onMyShowing() {
        checkoutMenu.isDisable = model.isHead
        mergeMenu.isDisable = model.isHead
        rebaseMenu.isDisable = model.isHead
        resetMenu.isDisable = model.isHead
        checkoutMenu.isDisable = model.isHead
    }
}