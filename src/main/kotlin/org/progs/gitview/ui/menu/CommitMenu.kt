package org.progs.gitview.ui.menu

import javafx.event.EventHandler
import javafx.scene.control.ButtonType
import javafx.scene.control.Menu
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.stage.FileChooser.ExtensionFilter
import org.progs.gitview.MainApp.Companion.resourceBundle
import org.progs.gitview.model.RepositoryModel
import org.progs.gitview.model.item.CommitInfoItem
import org.progs.gitview.ui.alert.CherryPickConfirm
import org.progs.gitview.ui.alert.ConfirmationType
import org.progs.gitview.ui.alert.confirm
import org.progs.gitview.ui.dialog.*
import org.progs.gitview.ui.util.IconCode

class CommitMenu(
    private val repositoryModel: RepositoryModel
): Menu(resourceBundle.getString("CommitMenu.Title")) {

    /** 選択されているコミット情報 */
    private val selectedModel: CommitInfoItem? get() = repositoryModel.commitListModel.selectedCommitInfoItem

    private val commitOperations: CommitOperations? get() = selectedModel?.let { CommitOperations(repositoryModel, it) }

    /** チェックアウト */
    private val checkoutMenu = MenuItem(
        text = resourceBundle.getString("CommitMenu.Checkout"),
        iconCode = IconCode.DOWNLOAD_2_FILL
    ) {
        selectedModel?.let { model ->
            if (model.localBranches.isNotEmpty()) {
                LocalBranchOperations(model.localBranches.first()).checkout()
            } else {
                commitOperations?.checkout()
            }
        }
    }

    /** ブランチ */
    private val createBranchMenu = MenuItem(
        text = resourceBundle.getString("CommitMenu.CreateBranch"),
        iconCode = IconCode.GIT_BRANCH_LINE
    ) {
        commitOperations?.let { commitOperations ->
            val dialog = BranchCreationDialog()
            if (dialog.showDialog() == ButtonType.OK) {
                val branchName = dialog.controller.newBranchName
                val checkout = dialog.controller.checkoutFlag
                commitOperations.createBranch(branchName, checkout)
            }
        }
    }

    /** マージ */
    private val mergeMenu = MenuItem(
        text = resourceBundle.getString("CommitMenu.Merge"),
        iconCode = IconCode.GIT_MERGE_LINE
    ) {
        commitOperations?.let { commitOperations ->
            val dialog = MergeCommitDialog(repositoryModel.branchListModel.currentBranch)
            if (dialog.showDialog() == ButtonType.OK) {
                commitOperations.merge(dialog.message)
            }
        }
    }

    /** タグ */
    private val tagMenu = MenuItem(
        text = resourceBundle.getString("CommitMenu.Tag"),
        accelerator = KeyCodeCombination(
            KeyCode.T,
            KeyCombination.SHORTCUT_DOWN,
            KeyCombination.SHIFT_DOWN
        ),
        iconCode = IconCode.PRICE_TAG_3_LINE
    ) {
        commitOperations?.let { commitOperations ->
            val dialog = TagCreationDialog()
            if (dialog.showDialog() == ButtonType.OK) {
                commitOperations.createTag(dialog.tagName, dialog.message)
            }
        }
    }

    /** リベース */
    private val rebaseMenu = MenuItem(
        text = resourceBundle.getString("CommitMenu.Rebase")
    ) {
        commitOperations?.let { commitOperations ->
            val srcBranch = repositoryModel.currentBranch ?: "HEAD"
            val message = resourceBundle.getString("Message.CommitMenu.Rebase")
                .format(srcBranch)
            if (confirm(ConfirmationType.YesNo, message)) {
                commitOperations.rebase()
            }
        }
    }

    /** リセット */
    private val resetMenu = MenuItem(
        text = resourceBundle.getString("CommitMenu.Reset"),
        accelerator = KeyCodeCombination(
            KeyCode.R,
            KeyCombination.SHORTCUT_DOWN,
            KeyCombination.SHIFT_DOWN
        ),
        iconCode = IconCode.ARROW_TURN_BACK_LINE
    ) {
        commitOperations?.let { commitOperations ->
            val dialog = ResetDialog()
            if (dialog.showDialog() == ButtonType.OK) {
                commitOperations.reset(dialog.option)
            }
        }
    }

    /** パッチ生成 */
    private val patchMenu = MenuItem(
        text = resourceBundle.getString("CommitMenu.CreatePatch"),
    ) {
        commitOperations?.let { commitOperations ->
            val title = resourceBundle.getString("Message.PatchFile")
            val filter = ExtensionFilter("patch", "*.patch")
            FileChooser(title).apply { extensionFilters.add(filter) }.selectSaveFile()?.let { file ->
                commitOperations.createPatch(file)
            }
        }
    }

    /** チェリーピック */
    private val cherryPickMenu = MenuItem(
        text = resourceBundle.getString("CommitMenu.CherryPick"),
        iconCode = IconCode.ARROW_TURN_FORWARD_LINE
    ) {
        commitOperations?.let { commitOperations ->
            val srcBranch = repositoryModel.currentBranch ?: "HEAD"
            val dialog = CherryPickConfirm(srcBranch)
            if (dialog.showDialog()) {
                commitOperations.cherryPick(dialog.doCommit)
            }
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
        arrayOf(
            createBranchMenu,
            tagMenu,
            checkoutMenu,
            mergeMenu,
            rebaseMenu,
            resetMenu,
            patchMenu,
            cherryPickMenu
        ).forEach { it.isDisable = selectedModel == null }
    }
}