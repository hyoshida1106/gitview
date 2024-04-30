package org.progs.gitview.model

import org.progs.gitview.git.branch.AbstractBranch
import org.progs.gitview.git.commit.Id


/**
 * ブランチモデルの基本クラス
 */
open class BranchModel(
    val branchListModel: BranchListModel,
    branch: AbstractBranch
) {
    /** ブランチのID */
    val id: Id = branch.ref.objectId

    /** 表示名 */
    val name: String = branch.name

    /** パス情報 */
    val path: String = branch.path

    /** ローカルブランチ一覧の更新 */
    fun updateLocalBranchList()  = branchListModel.updateLocalBranchList()

    /** リモートブランチ一覧の更新 */
    fun updateRemoteBranchList() = branchListModel.updateRemoteBranchList()
}
