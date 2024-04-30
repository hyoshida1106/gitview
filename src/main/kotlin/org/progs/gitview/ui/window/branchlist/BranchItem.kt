package org.progs.gitview.ui.window.branchlist

import javafx.scene.Node
import javafx.scene.control.CheckBox
import javafx.scene.control.ContextMenu
import javafx.scene.control.Label
import javafx.scene.control.TreeItem
import org.progs.gitview.model.LocalBranchModel
import org.progs.gitview.model.RemoteBranchModel
import org.progs.gitview.model.RepositoryModel
import org.progs.gitview.model.TagModel
import org.progs.gitview.ui.menu.LocalBranchContextMenu
import org.progs.gitview.ui.menu.RemoteBranchContextMenu
import org.progs.gitview.ui.menu.TagContextMenu


/** クラスのStyle定義 */
private const val RemoteBranchStyle  = "RemoteBranch"
private const val LocalBranchStyle   = "LocalBranch"
private const val CurrentBranchStyle = "CurrentBranch"

/**
 * リスト表示モデル基本インターフェース
 */
interface TreeModel {
    val name: String
}

/**
 * ローカルブランチ表示モデル
 */
class LocalBranchTreeModel(
    val model: LocalBranchModel
): TreeModel {
    override val name = model.name
}

/**
 * リモートブランチ表示モデル
 */
class RemoteBranchTreeModel(
    var model: RemoteBranchModel
): TreeModel {
    override val name = model.name
}

/**
 * タグ表示モデル
 */
class TagTreeModel(
    model: TagModel
): TreeModel {
    override val name = model.name
}

/**
 * BranchTreeList項目の基本抽象クラス
 */
abstract class BaseItem(
    model: TreeModel? = null
): TreeItem<TreeModel>(model) {
    abstract val cellImage: Node?
    abstract val cellText: String?
    open val contextMenu: ContextMenu? = null
    open val styleClass: List<String>? = null
    open val toolTipText: () -> String? = { null }
}

/**
 * BranchTreeList項目のルートクラス
 */
class RootItem(
    icon: Label,
    val name: String?,
    toolTip: () -> String? = { null }
) : BaseItem() {
    override val cellImage = icon
    override val cellText = name
    override fun isLeaf(): Boolean = false
    override val toolTipText = toolTip
    init { isExpanded = true }
}

/**
 * BranchTreeList項目のTreeクラス
 */
open class FolderItem(
    val name: String
): BaseItem() {
    override val cellImage = null
    override val cellText  = name
    override fun isLeaf(): Boolean = false
    init { isExpanded = true }
}

/**
 * BranchTreeList項目のLeaf基本クラス
 */
abstract class BranchLeafItem(
    model: TreeModel
): BaseItem(model) {
    val path: List<String>
    val name: String
    override fun isLeaf(): Boolean = true

    init {
        val pathNodes = model.name.split("/")
        name = pathNodes.last()
        path = pathNodes.dropLast(1)
    }
}

/**
 * リモートブランチ
 */
class RemoteBranchItem(
    model: RemoteBranchModel
): BranchLeafItem(RemoteBranchTreeModel(model)) {
    override val cellImage = null
    override val cellText = name
    override val contextMenu = RemoteBranchContextMenu(model)
    override val styleClass = listOf(RemoteBranchStyle)
}

/**
 * ローカルブランチ
 */
class LocalBranchItem(
    repositoryModel: RepositoryModel,
    model: LocalBranchModel
): BranchLeafItem(LocalBranchTreeModel(model)) {
    private val showInTree = CheckBox()
    override val cellImage = showInTree
    override val cellText = name
    override val contextMenu = LocalBranchContextMenu(repositoryModel, model)
    override val styleClass: List<String>
    init {
        //カレントブランチならば強調表示かつ非選択不可
        if(model.isCurrentBranch) {
            styleClass = listOf(LocalBranchStyle, CurrentBranchStyle)
            showInTree.isDisable = true
        } else {
            styleClass = listOf(LocalBranchStyle)
            showInTree.isDisable = false
        }
        //チェックボックスをモデルのプロパティにバインドする
        showInTree.selectedProperty().bindBidirectional(
            model.isSelectedProperty)
    }
}

/**
 * タグ
 */
class TagItem(
    repositoryModel: RepositoryModel,
    model: TagModel
): BranchLeafItem(TagTreeModel(model)) {
    override val cellImage: Node? = null
    override val cellText: String = model.name
    override val contextMenu = TagContextMenu(repositoryModel, model)
}
