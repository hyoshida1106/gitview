package org.progs.gitview.ui.window.branchlist

import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.input.MouseButton
import org.progs.gitview.model.*
import org.progs.gitview.ui.menu.RemoteBranchOperations
import org.progs.gitview.ui.menu.LocalBranchOperations
import org.progs.gitview.ui.util.IconCode
import org.progs.gitview.ui.util.iconLabel
import org.progs.gitview.ui.window.BaseControl
import org.progs.gitview.ui.window.BaseWindow


class BranchListWindow(
    repositoryModel: RepositoryModel
): BaseWindow<BranchListWindow.Control>(Control(repositoryModel)) {

    /** コントロールクラス */
    class Control(
        private val repositoryModel: RepositoryModel
    ) : BaseControl() {
        @FXML private lateinit var branchTree: TreeView<TreeModel>

        //ブランチ一覧モデル
        private val branchListModel: BranchListModel = repositoryModel.branchListModel

        //リモートブランチ・ローカルブランチ・タグのアイコン
        private val remoteBranchIcon = IconCode.GIT_BRANCH_LINE.iconLabel.apply {
            styleClass.addAll("CommitLabel", "RemoteBranchLabel", "tree-icon")
        }
        private val localBranchIcon = IconCode.GIT_BRANCH_LINE.iconLabel.apply {
            styleClass.addAll("CommitLabel", "LocalBranchLabel", "tree-icon")
        }
        private val tagIcon = IconCode.PRICE_TAG_3_LINE.iconLabel.apply {
            styleClass.addAll("CommitLabel", "TagLabel", "tree-icon")
        }

        //リモートブランチ・ローカルブランチ・タグのルートノード
        private val remoteTreeRoot = RootItem(remoteBranchIcon, "Remote Branch") {
            repositoryModel.remoteRepositoryPath }
        private val localTreeRoot = RootItem(localBranchIcon, "Local Branch") {
            repositoryModel.localRepositoryPath }
        private val tagTreeRoot = RootItem(tagIcon, "Tag")

        /** 初期化 */
        fun initialize() {
            branchTree.root = FolderItem("Root Item")
            branchTree.root.children.addAll(remoteTreeRoot, localTreeRoot, tagTreeRoot)
            branchTree.isShowRoot = false
            branchTree.selectionModel.clearSelection()
            branchTree.setCellFactory { BranchTreeCell() }
        }

        /** 表示完了時処理 */
        override fun displayCompleted() {

            //ハンドラ登録
            branchListModel.addLocalBranchListChangeListener  { Platform.runLater { setLocalBranches() }  }
            branchListModel.addRemoteBranchListChangeListener { Platform.runLater { setRemoteBranches() } }
            branchListModel.addTagListChangeListener          { Platform.runLater { setTags() }           }

            //ダブルクリックでチェックアウト
            branchTree.setOnMouseClicked {
                if(it.button == MouseButton.PRIMARY && it.clickCount == 2) {
                    branchTree.selectionModel.selectedItem?.value?.let { model ->
                        if(model is LocalBranchTreeModel && !model.model.isCurrentBranch) {
                            LocalBranchOperations(model.model).checkout()
                        } else if(model is RemoteBranchTreeModel) {
                            RemoteBranchOperations(model.model).checkout()
                        }
                    }
                }
            }
        }

        /** TreeView表示セルクラス */
        private class BranchTreeCell: TreeCell<TreeModel>() {
            override fun updateItem(model: TreeModel?, empty: Boolean) {
                super.updateItem(model, empty)
                val item = if(empty) null else treeItem as? BaseItem
                graphic = item?.cellImage
                contextMenu = item?.contextMenu
                text = item?.cellText
                item?.toolTipText?.let { it() }?.let { toolTipText -> tooltip = Tooltip(toolTipText) }
                styleClass.setAll("cell", "indexed-cell", "tree-cell")
                item?.styleClass?.let { list -> styleClass.addAll(list) }
            }
        }

        /** ブランチツリーに項目を追加する */
        private fun addTreeNode(
            root: BaseItem,
            leaf: BranchLeafItem,
            path:List<String>,
            rootConstructor: (String) -> FolderItem
        ) {
            //パスを参照して再帰的に登録する
            if(path.isEmpty()) {
                root.children.add(leaf)
            } else {
                addTreeNode(
                    findTreeNode(root, path.first(), rootConstructor),
                    leaf,
                    path.subList(1, path.size),
                    rootConstructor)
            }
        }

        /**パス名に対応するノードを取得または生成する */
        private fun findTreeNode(
            root: BaseItem,
            name: String,
            rootConstructor: (String) -> FolderItem
        ): FolderItem {
            return root.children.filterIsInstance<FolderItem>().find { it.name == name }
                ?: rootConstructor(name).apply { root.children.add(this) }
        }

        /** ローカルブランチ一覧の表示 */
        private fun setLocalBranches() {
            localTreeRoot.children.clear()
            branchListModel.localBranchList
                .filter { it.name != "HEAD" }
                .map { LocalBranchItem(repositoryModel, it) }
                .forEach { addTreeNode(localTreeRoot, it, it.path) { name -> FolderItem(name) } }
            branchTree.selectionModel.clearSelection()
        }

        /** リモートブランチ一覧の表示 */
        private fun setRemoteBranches() {
            remoteTreeRoot.children.clear()
            branchListModel.remoteBranchList
                .filter { it.name != "HEAD" }
                .map { RemoteBranchItem(it) }
                .forEach { addTreeNode(remoteTreeRoot, it, it.path){ name -> FolderItem(name) } }
            branchTree.selectionModel.clearSelection()
        }

        /** タグ一覧表示 */
        private fun setTags() {
            tagTreeRoot.children.clear()
            tagTreeRoot.children.setAll(
                branchListModel.tagList.sortedBy { it.name }.map { TagItem(repositoryModel, it) } )
            branchTree.selectionModel.clearSelection()
        }
    }
}