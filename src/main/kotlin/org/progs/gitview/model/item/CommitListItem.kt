package org.progs.gitview.model.item

import javafx.scene.control.ContextMenu


/**
 * コミットツリーの基本抽象クラスデータモデル
 */
interface CommitListItem {
    val headLane: Int?
    val isCurrentBranch: Boolean
    val isHead: Boolean
    val isMerge: Boolean
    val laneNumber: Int
    val maxLaneNumber: Int
    val passThroughLanes: List<Int>
    val exitingLanes: List<Int>
    val enteringLanes: List<Int>

    val contextMenu: ContextMenu?
}