package org.progs.gitview.model

import org.progs.gitview.git.branch.RemoteBranch
import org.progs.gitview.git.branch.RemoteBranchOperations

/**
 * リモートブランチ
 */
class RemoteBranchModel(
    listModel: BranchListModel,
    private val remoteBranch: RemoteBranch
): BranchModel(listModel, remoteBranch), RemoteBranchOperations by remoteBranch {

    /** 対応するローカルブランチ(未取得の場合はnull) */
    val localBranch: LocalBranchModel?
        get() = branchListModel.localBranchList.firstOrNull {
            localBranch -> localBranch.remoteBranchPath == this.path }
}
