package org.progs.gitview.model

import javafx.beans.property.SimpleBooleanProperty
import org.progs.gitview.git.branch.LocalBranch
import org.progs.gitview.git.branch.LocalBranchOperations

/**
 * ローカルブランチ
 */
class LocalBranchModel(
    private val listModel: BranchListModel,
    val localBranch: LocalBranch
): BranchModel(listModel, localBranch), LocalBranchOperations by localBranch {

    /** コミット表示対象フラグ */
    val isSelectedProperty = SimpleBooleanProperty(true)
    val isSelected: Boolean get() = isSelectedProperty.value

    /** ハンドラ登録 */
    fun addSelectionListener(
        handler: (Boolean) -> Unit
    ) {
        isSelectedProperty.addListener { _, _, value -> handler(value) }
    }

    /** リモートブランチ追跡名 */
    val remoteBranchPath = localBranch.remoteTrackingBranch

    /** 選択中のブランチならば true */
    val isCurrentBranch: Boolean get() = (listModel.currentBranch == this.name)
}

