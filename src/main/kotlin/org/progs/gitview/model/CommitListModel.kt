package org.progs.gitview.model

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.WeakChangeListener
import org.progs.gitview.git.commit.Id
import org.progs.gitview.git.commit.WorkTreeFiles
import org.progs.gitview.model.item.CommitInfoItem
import org.progs.gitview.model.item.CommitListItem
import org.progs.gitview.model.item.WorkTreeItem
import org.progs.gitview.ui.window.main.SystemConfig
import kotlin.math.max

/**
 * 「コミット一覧」に表示されるWorkingTreeとCommitリストの情報を保持する
 */
class CommitListModel(
    private val repositoryModel: RepositoryModel,
    private val branchListModel: BranchListModel
) {
    /*
     * リスト上で選択されているWorkTree情報、無設定時はnull
     */
    private val selectedWorkTreeItemProperty = SimpleObjectProperty<WorkTreeItem?>(null)
    private var selectedWorkTreeItem: WorkTreeItem? by selectedWorkTreeItemProperty::value

    private val selectedWorkTreeItemListeners = mutableListOf<ChangeListener<WorkTreeItem?>>()

    fun addSelectedWorkTreeItemListener( handler: (WorkTreeItem?) -> Unit ) {
        val listener = ChangeListener<WorkTreeItem?> { _, _, value -> handler(value) }
        selectedWorkTreeItemProperty.addListener(listener)
        selectedWorkTreeItemListeners.add(WeakChangeListener(listener))
    }

    private fun releaseSelectedWorkTreeItem() {
        selectedWorkTreeItem = null
    }

    /*
     * リスト上で選択されているコミット、無選択時はnull
     */
    private val selectedCommitInfoItemProperty = SimpleObjectProperty<CommitInfoItem?>(null)
    var selectedCommitInfoItem: CommitInfoItem? by selectedCommitInfoItemProperty::value

    private val selectedCommitInfoItemListeners = mutableListOf<ChangeListener<CommitInfoItem?>>()

    fun addSelectedCommitInfoItemListener( handler: (CommitInfoItem?) -> Unit ) {
        val listener = ChangeListener { _, _, value -> handler(value) }
        selectedCommitInfoItemListeners.add(listener)
        selectedCommitInfoItemProperty.addListener(WeakChangeListener(listener))
    }

    private fun releaseSelectedCommitInfoItem() {
        selectedCommitInfoItem = null
    }


    /*
     * コミット一覧上の項目を選択(WorkTreeItemまたはCommitItem)
     */
    fun selectCommitItem(item: CommitListItem?) {
        selectedWorkTreeItem   = item as? WorkTreeItem
        selectedCommitInfoItem = item as? CommitInfoItem
    }


    /*
     *  コミット情報更新通知
     */
    data class CommitInfo(
        val workTreeFiles: WorkTreeFiles,
        val commitList: List<CommitInfoItem>
    )

    private val commitInfoProperty = SimpleObjectProperty<CommitInfo?>(null)
    private var commitInfo: CommitInfo? by commitInfoProperty::value

    private val commitInfoListeners = mutableListOf<ChangeListener<CommitInfo?>>()

    fun addCommitInfoListener(handler: (CommitInfo?) -> Unit) {
        val listener = ChangeListener { _, _, value -> handler(value) }
        commitInfoListeners.add(listener)
        commitInfoProperty.addListener(WeakChangeListener(listener))
    }

    /*
     * 作業ファイル情報更新通知
     */
    private val workTreeFilesProperty = SimpleObjectProperty(WorkTreeFiles(null))
    var workTreeFiles: WorkTreeFiles by workTreeFilesProperty::value

    private val workTreeFilesListeners = mutableListOf<ChangeListener<WorkTreeFiles>>()

    fun addWorkTreeFilesListener(handler: (WorkTreeFiles) -> Unit) {
        val listener = ChangeListener { _, _, files -> handler(files) }
        workTreeFilesListeners.add(listener)
        workTreeFilesProperty.addListener(WeakChangeListener(listener))
    }

    private fun releaseWorkTreeModel() {
        workTreeFiles = WorkTreeFiles(null)
    }


    /*
     *  コミット一覧
     */
    private var commitInfoItemList: List<CommitInfoItem> = emptyList()

    // Idからコミットモデルを検索するためのマップ
    private var commitInfoModelMap: Map<Id, Int> = emptyMap()

    private fun releaseCommitList() {
        commitInfoItemList = emptyList()
        commitInfoModelMap = emptyMap()
    }


    /** Idで指定したコミットのインデックスを取得 */
    fun getIndexById(id: Id): Int? = commitInfoModelMap.getOrDefault(id, null)
    
    /** Idで指定したコミットモデルを取得 */
    fun getCommitInfoItemById(
        id: Id
    ): CommitInfoItem? {
        return commitInfoModelMap[id]?.let { index -> commitInfoItemList.getOrNull(index) }
    }

    /** ひとつ前のコミットモデルを取得 */
    fun getPrev(
        commit: CommitInfoItem
    ): CommitInfoItem? {
        return commitInfoModelMap[commit.id]?.let { index -> commitInfoItemList.getOrNull(index - 1) }
    }

    /** ひとつ後のコミットモデルを取得 */
    fun getNext(
        commit: CommitInfoItem
    ): CommitInfoItem? {
        return commitInfoModelMap[commit.id]?.let { index -> commitInfoItemList.getOrNull(index + 1) }
    }

    /** 指定されたコミットのローカルブランチ一覧を取得 */
    fun getLocalBranchesOfCommit(
        commit: CommitInfoItem
    ): Set<LocalBranchModel> {
        return branchListModel.localBranchList.filter { model -> model.id == commit.id }.toSet()
    }

    /** 指定されたコミットのリモートブランチ一覧を取得 */
    fun getRemoteBranchesOfCommit(
        commit: CommitInfoItem
    ): Set<RemoteBranchModel> {
        return branchListModel.remoteBranchList.filter { model -> model.id == commit.id }.toSet()
    }


    /* ワークツリーファイル更新 */
    fun updateWorkTreeFiles() {
        val newWorkTreeFiles = repositoryModel.getWorkTreeFiles()
        if (workTreeFiles.isEmpty) {
            if(!newWorkTreeFiles.isEmpty)
                addHeadLane()
        } else {
            if(newWorkTreeFiles.isEmpty)
                removeHeadLane()
        }
        workTreeFiles = newWorkTreeFiles
        commitInfo = CommitInfo(workTreeFiles, commitInfoItemList)
    }

    /* コミット一覧更新 */
    fun updateCommitList( ) {

        //WorkTreeモデルを更新
        workTreeFiles = repositoryModel.getWorkTreeFiles()

        //コミット一覧
        val maxCommits = SystemConfig.maxCommits.toInt()
        commitInfoItemList = repositoryModel.getCommitList(branchListModel.selectedBranchList, maxCommits)

        //コミット一覧マップ
        commitInfoModelMap = commitInfoItemList.mapIndexed { index, commit -> commit.id to index }.toMap()

        //HEADから先頭コミットまでのレーンを追加する
        if (!workTreeFiles.isEmpty) {
            addHeadLane()
        }

        //更新情報を通知
        commitInfo = CommitInfo(workTreeFiles, commitInfoItemList)
    }

    /* HEADから先頭コミットまでのレーンを追加する */
    private fun addHeadLane() {
        val headIndex = commitInfoItemList.indexOfFirst { it.isHead }
        if(headIndex >= 0) {
            var headLane = commitInfoItemList[headIndex].laneNumber
            if (headIndex > 0) {
                headLane =
                    max(commitInfoItemList.subList(0, headIndex).maxOf { it.maxLaneNumber } + 1, headLane)
                commitInfoItemList.subList(0, headIndex).forEach { it.headLane = headLane }
            }
            commitInfoItemList[headIndex].headLane = headLane
        }
    }

    /* HEADから先頭コミットまでのレーンを削除する */
    private fun removeHeadLane() {
        val headIndex = commitInfoItemList.indexOfFirst { it.isHead }
        for(index in 0..headIndex) {
            commitInfoItemList[index].headLane = null
        }
    }

    /** 初期化 */
    init {
        branchListModel.addSelectedBranchListChangeHandler {
            updateCommitList()
        }
    }

    /** 閉じる */
    fun close() {
        releaseSelectedWorkTreeItem()
        releaseSelectedCommitInfoItem()
        releaseWorkTreeModel()
        releaseCommitList()
    }
}