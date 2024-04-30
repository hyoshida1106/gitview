package org.progs.gitview.git.commit

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.lib.Repository
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class CommitFileTest {

    @MockK(relaxed = true)
    lateinit var repository: Repository

    @MockK(relaxed = true)
    lateinit var entry: DiffEntry


    @Test
    fun getMode() {
        val args = arrayOf(
            DiffEntry.ChangeType.ADD,
            DiffEntry.ChangeType.COPY,
            DiffEntry.ChangeType.DELETE,
            DiffEntry.ChangeType.MODIFY,
            DiffEntry.ChangeType.RENAME
        )

        val vals = arrayOf(
            CommitFile.Mode.ADD,
            CommitFile.Mode.COPY,
            CommitFile.Mode.DELETE,
            CommitFile.Mode.MODIFY,
            CommitFile.Mode.RENAME
        )

        for(i in 0..4) {
            every { entry.changeType } returns args[i]
            assertEquals(CommitFile(repository, entry, true).mode, vals[i])
        }
    }

    @Test
    fun getPath() {
        every { entry.oldPath } returns "Old Path"
        every { entry.newPath } returns "New Path"

        every { entry.changeType } returns DiffEntry.ChangeType.ADD
        assertEquals(CommitFile(repository, entry, true).path, "New Path")

        every { entry.changeType } returns DiffEntry.ChangeType.COPY
        assertEquals(CommitFile(repository, entry, true).path, "New Path")

        every { entry.changeType } returns DiffEntry.ChangeType.DELETE
        assertEquals(CommitFile(repository, entry, true).path, "Old Path")

        every { entry.changeType } returns DiffEntry.ChangeType.MODIFY
        assertEquals(CommitFile(repository, entry, true).path, "New Path")

        every { entry.changeType } returns DiffEntry.ChangeType.RENAME
        assertEquals(CommitFile(repository, entry, true).path, "New Path")
    }

//    @Test
//    fun getDiffLines() {
//    }

//    @Test
//    fun getDiffTextLines() {
//    }
}