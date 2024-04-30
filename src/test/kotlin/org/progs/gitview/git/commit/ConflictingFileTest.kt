package org.progs.gitview.git.commit

import io.mockk.junit5.MockKExtension
import org.eclipse.jgit.lib.IndexDiff.StageState
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith


@ExtendWith(MockKExtension::class)
class ConflictingFileTest {

    data class StateResult(
        val stageState: StageState,
        val state: ConflictingFile.State
    )

    private val results = arrayOf(
        StateResult(StageState.ADDED_BY_THEM, ConflictingFile.State.ADDED_BY_THEM),
        StateResult(StageState.ADDED_BY_US, ConflictingFile.State.ADDED_BY_US),
        StateResult(StageState.BOTH_ADDED, ConflictingFile.State.BOTH_ADDED),
        StateResult(StageState.BOTH_MODIFIED, ConflictingFile.State.BOTH_MODIFIED),
        StateResult(StageState.BOTH_DELETED, ConflictingFile.State.BOTH_DELETED),
        StateResult(StageState.DELETED_BY_THEM, ConflictingFile.State.DELETED_BY_THEM),
        StateResult(StageState.DELETED_BY_US, ConflictingFile.State.DELETED_BY_US)
    )

    @Test
    fun getState() {
        val path = "Path String"

        results.forEach {
            val conflictingFile = ConflictingFile(path, it.stageState)
            assertEquals(conflictingFile.state, it.state)
        }

    }
}