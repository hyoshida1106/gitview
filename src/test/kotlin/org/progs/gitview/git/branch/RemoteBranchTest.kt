package org.progs.gitview.git.branch

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.verify
import org.eclipse.jgit.lib.ProgressMonitor
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.progs.gitview.git.GitCommand
import org.progs.gitview.git.git

@ExtendWith(MockKExtension::class)
class RemoteBranchTest {

    @MockK
    lateinit var ref: Ref

    @MockK
    lateinit var gitCommand: GitCommand

    @MockK
    lateinit var repository: Repository

    private val refName = "Name"

    @BeforeEach
    fun beforeEach() {
        every { ref.name } returns refName
        mockkStatic(Repository::git)
        every { repository.git } returns gitCommand
        every { repository.shortenRemoteBranchName(any()) } returnsArgument (0)
    }

    @Test
    fun getName() {
        assertEquals(RemoteBranch(repository, ref).name, refName)
    }

    @Test
    fun checkout() {
        val monitor: ProgressMonitor? = null
        every { gitCommand.checkoutRemote(any(), any(), any()) } returns null
        RemoteBranch(repository, ref).checkoutToLocal(monitor)
        verify {
            gitCommand.checkoutRemote(refName, refName, null)
        }
    }
}