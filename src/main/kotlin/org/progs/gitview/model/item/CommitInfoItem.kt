package org.progs.gitview.model.item

import org.progs.gitview.MainApp.Companion.resourceBundle
import org.progs.gitview.git.commit.CommitFile
import org.progs.gitview.git.commit.CommitInfo
import org.progs.gitview.git.commit.CommitOperations
import org.progs.gitview.model.*
import org.progs.gitview.ui.menu.CommitContextMenu


/**
 * コミット情報を保持するツリーデータモデル
 */
class CommitInfoItem(
    private val repositoryModel: RepositoryModel,
    private val commitInfo: CommitInfo
): CommitListItem, CommitOperations by commitInfo {

    private val commitList: CommitListModel = repositoryModel.commitListModel

    /** コンテキストメニュー */
    override val contextMenu = CommitContextMenu(repositoryModel, this)

    /** 更新ファイル一覧 */
    val commitFiles: List<CommitFile> by lazy { repositoryModel.getDiffEntriesOfCommit(commitInfo)
        .mapNotNull { diffEntry -> repositoryModel.getCommitFile(diffEntry, true) } }

    /** ローカルブランチ一覧 */
    val localBranches: Set<LocalBranchModel> by lazy {
        commitList.getLocalBranchesOfCommit(this) }

    /** リモートブランチ一覧 */
    val remoteBranches: Set<RemoteBranchModel> by lazy {
        commitList.getRemoteBranchesOfCommit(this) }

    /** タグ一覧 */
    val tags: Set<String> by lazy {
        repositoryModel.branchListModel.tagList.filter { tag -> tag.id == this.id }.map { tag -> tag.name }.toSet()
    }

    /** ヘッダへのレーン番号 */
    override var headLane: Int? = null

    /** このコミットがマージコミットの場合 true */
    override val isMerge: Boolean get() = (commitInfo.parentCount > 1)


    /** このコミットがカレントブランチの場合 true */
    override val isCurrentBranch: Boolean get() = localBranches.find { it.isCurrentBranch } != null

    /** このコミットがHEADの場合 true */
    override val isHead: Boolean get() = (commitInfo.id == repositoryModel.head)


    /** 親(祖先)コミットのリスト */
    private val parents: List<CommitInfoItem> by lazy {
        commitInfo.parentList.mapNotNull { repositoryModel.commitListModel.getCommitInfoItemById(it) }
    }

    /** 子(子孫)コミットのリスト */
    private val children: List<CommitInfoItem> by lazy {
        commitInfo.childList.mapNotNull { commitList.getCommitInfoItemById(it) }
    }

    /** 通過レーンのリスト */
    override val passThroughLanes: List<Int> by lazy {
        commitInfo.passingThrough
    }

    /** 分岐(このコミットから出る)レーンのリスト */
    override val exitingLanes: List<Int> by lazy {
        //1つ前のコミットの通過レーンからこのコミットの通過レーンを除く
        (commitList.getPrev(this)?.passThroughLanes?.minus(passThroughLanes.toSet()) ?: emptyList())
            //子供のレーン番号を加える(子供がマージコミットであれば、このコミットのレーン番号を加える
            .plus(children.map { if (it.isMerge) laneNumber else it.laneNumber }.toSet())
            //重複を削除
            .distinct()
    }

    /** 通過レーン + 分岐レーン */
    private val outLanes: List<Int> by lazy { exitingLanes.plus(passThroughLanes) }

    /** 収束(このコミットに入る)レーンのリスト */
    override val enteringLanes: List<Int> by lazy {
        //次のコミットの出力レーン(通過レーン + 分岐レーン)
        commitList.getNext(this)?.outLanes
            //このコミットの通過レーンを除く
            ?.minus(passThroughLanes.toSet())
            //このコミットのレーンに分岐していない親コミットのレーンを加える
            ?.plus(parents.filter { !it.exitingLanes.contains(this.laneNumber) }.map { it.laneNumber }.toSet())
            //重複削除
            ?.distinct()
        //次のコミットがなければこのコミットのレーンのみ描く
        ?: listOf(laneNumber)
    }

    /** このコミットのレーン数最大値 */
    override val maxLaneNumber: Int get() =
        ( passThroughLanes + exitingLanes + enteringLanes + listOf(headLane ?: 0) ).maxOfOrNull { it } ?: -1

    /**  ツールチップ情報 */
    val toolTipInformation: String get() =
        resourceBundle.getString("Message.CommitInformation").format(
            id.name(),
            commitTime,
            author,
            committer,
            shortMessage
        )

    /* デバッグ用ツールチップ */
//    val toolTipInformation: String get() {
//        return """passThroughLanes: $passThroughLanes\
//id $id
//exitingLanes $exitingLanes
//enteringLanes $enteringLanes
//parents ${parents.map { it.laneNumber }}
//children ${children.map { it.laneNumber }} - ${commitInfo.childList.map { it.toObjectId() }}}
//next ${commitList.getNext(this)?.commitTime ?: "null"}"""
//    }
}