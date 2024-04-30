package org.progs.gitview.git.branch

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.verify
import org.eclipse.jgit.lib.*
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.progs.gitview.git.GitCommand
import org.progs.gitview.git.getTrackingStatus
import org.progs.gitview.git.git

@ExtendWith(MockKExtension::class)
class LocalBranchTest {

    @MockK
    lateinit var ref: Ref

    @MockK(relaxed = true)
    lateinit var gitCommand: GitCommand

    @MockK
    lateinit var repository: Repository

    @MockK
    lateinit var status: BranchTrackingStatus

    private val refName = "Name"

    private lateinit var localBranch: LocalBranch


    @BeforeEach
    fun beforeEach() {
        every { ref.name } returns refName

        mockkStatic(Repository::git)
        every { repository.git } returns gitCommand

        every { repository.getTrackingStatus(any()) } returns status

        mockkStatic(Repository::class)
        every { Repository.shortenRefName(any()) } answers { "path/to/" + firstArg<String>() }

        localBranch = LocalBranch(repository, ref)
    }

    @Test
    fun getName() {
        assertEquals(localBranch.name, "path/to/$refName")
    }

    @Test
    fun getRemoteTrackingBranch() {
        every { status.remoteTrackingBranch } returns "branch"
        assertEquals( localBranch.remoteTrackingBranch, "branch" )

        every { status.remoteTrackingBranch } returns null
        assertEquals( localBranch.remoteTrackingBranch, null )
    }

    @Test
    fun checkoutFromRemote() {
        val monitor1: ProgressMonitor? = null
        val monitor2: ProgressMonitor = object: EmptyProgressMonitor() {}

        localBranch.checkoutFromRemote(monitor1)
        verify { gitCommand.checkoutLocal("path/to/$refName", null) }

        localBranch.checkoutFromRemote(monitor2)
        verify { gitCommand.checkoutLocal("path/to/$refName", monitor2) }
    }

    @Test
    fun mergeToCurrentBranch() {
        val monitor: ProgressMonitor = object: EmptyProgressMonitor() {}
        val message = "This is merge message"

        localBranch.mergeToCurrentBranch(message, monitor)
        verify { gitCommand.merge(ref, message, monitor)}
    }

    @Test
    fun createNewBranch() {
        val monitor: ProgressMonitor = object: EmptyProgressMonitor() {}
        val branchName = "NewBranch"

        localBranch.createNewBranch(branchName, true, monitor)
        every { gitCommand.createBranch(refName, branchName, true, monitor) }
    }

    @Test
    fun rebaseCurrentBranch() {
        val monitor: ProgressMonitor = object: EmptyProgressMonitor() {}
        localBranch.rebaseCurrentBranch(monitor)
        every { gitCommand.rebaseBranch(refName, monitor)}
    }

    @Test
    fun push() {
        val monitor: ProgressMonitor = object: EmptyProgressMonitor() {}
        localBranch.push(true, monitor)
        every { gitCommand.push(refName, true, monitor) }
    }

    @Test
    fun pull() {
        val monitor: ProgressMonitor = object: EmptyProgressMonitor() {}
        localBranch.pull(monitor)
        every { gitCommand.pull(monitor) }
    }

    @Test
    fun remove() {
        val monitor: ProgressMonitor = object: EmptyProgressMonitor() {}
        localBranch.remove(false, monitor)
        every { gitCommand.removeLocal(refName, false, monitor)}
    }

    @Test
    fun rename() {
        val newName = "newName"
        localBranch.rename(newName)
        every { gitCommand.renameBranch(refName, newName)}
    }

}