package org.progs.gitview.model.item

import javafx.scene.control.ContextMenu
import org.progs.gitview.git.commit.CommitFile
import org.progs.gitview.git.commit.ConflictingFile
import org.progs.gitview.git.commit.WorkTreeFiles

/**
 * ワークツリー情報を保持するツリーデータモデル
 */
class WorkTreeItem(
    lane: Int,
    private val files: WorkTreeFiles
): CommitListItem {

    /* ツリー描画用の設定情報 */
    override val headLane: Int = lane
    override val isCurrentBranch: Boolean = false
    override val isHead: Boolean = false
    override val isMerge: Boolean = false
    override val laneNumber: Int = lane
    override val maxLaneNumber: Int = lane
    override val passThroughLanes: List<Int> = emptyList()
    override val exitingLanes: List<Int> = emptyList()
    override val enteringLanes: List<Int> = listOf(lane)
    override val contextMenu: ContextMenu? = null

    /* ファイル情報 */
    val stagedFiles: List<CommitFile> get() = files.stagedFiles
    val modifiedFiles: List<CommitFile> get() = files.modifiedFiles
    val conflictingFiles: List<ConflictingFile> get() = files.conflictingFiles
    val isConflicting: Boolean get() = files.isConflicting
    val isEmpty: Boolean get() = files.isEmpty
}