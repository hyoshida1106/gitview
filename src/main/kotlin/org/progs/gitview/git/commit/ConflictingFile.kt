package org.progs.gitview.git.commit

import org.eclipse.jgit.lib.IndexDiff.StageState


/**
 * コンフリクトファイル情報
 *
 * コンフリクトの発生したファイルのパスと状態を保持する
 * @property path       ファイルパス
 * @param stageState    コンフリクトの状態を示す*enum*
 */
class ConflictingFile(
    val path: String,
    stageState: StageState
){
    /**
     * コンフリクト状態
     */
    enum class State {
        ADDED_BY_THEM,
        ADDED_BY_US,
        BOTH_ADDED,
        BOTH_MODIFIED,
        BOTH_DELETED,
        DELETED_BY_THEM,
        DELETED_BY_US
    }

    /**
     * コンフリクトの状態を参照する
     */
    val state: State = when (stageState) {
        StageState.ADDED_BY_THEM -> State.ADDED_BY_THEM
        StageState.ADDED_BY_US -> State.ADDED_BY_US
        StageState.BOTH_ADDED -> State.BOTH_ADDED
        StageState.BOTH_MODIFIED -> State.BOTH_MODIFIED
        StageState.BOTH_DELETED -> State.BOTH_DELETED
        StageState.DELETED_BY_THEM -> State.DELETED_BY_THEM
        StageState.DELETED_BY_US -> State.DELETED_BY_US
    }

    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(other !is ConflictingFile) return false
        return this.path == other.path && this.state == other.state
    }

    override fun hashCode(): Int {
        var result = path.hashCode()
        result = 31 * result + state.hashCode()
        return result
    }

}