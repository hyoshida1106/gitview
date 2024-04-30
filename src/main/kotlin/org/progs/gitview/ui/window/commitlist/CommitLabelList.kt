package org.progs.gitview.ui.window.commitlist

import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import org.progs.gitview.model.item.CommitInfoItem
import org.progs.gitview.ui.util.IconCode
import org.progs.gitview.ui.util.iconLabel


class CommitLabelList(
    model: CommitInfoItem
): ArrayList<Node>() {

    private fun styledLabel(
        code: Int,
        text: String,
        vararg styles:String
    ): Node {
        return HBox().apply {
            styleClass.addAll(styles)
            children.addAll(
                code.iconLabel,
                Label(text).apply { styleClass.add("label-text") }
            )
        }
    }

    init {
        //ローカルブランチ
        addAll(model.localBranches.filter { it.isSelected }.map { localBranchLabel(it.name) })
        //リモートブランチ
        addAll(model.remoteBranches.filter { !it.name.endsWith("HEAD") }.map { remoteBranchLabel(it.name) })
        //タグ
        addAll(model.tags.map { tagLabel(it) })
    }

    private fun localBranchLabel(name: String) =
        styledLabel(IconCode.GIT_BRANCH_LINE, name, "CommitLabel", "LocalBranchLabel")

    private fun remoteBranchLabel(name: String) =
        styledLabel(IconCode.GIT_BRANCH_LINE, "remote/$name", "CommitLabel", "RemoteBranchLabel")

    private fun tagLabel(name: String) =
        styledLabel(IconCode.PRICE_TAG_3_LINE, name, "CommitLabel", "TagLabel")
}