package org.progs.gitview.git.commit

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.eclipse.jgit.lib.EmptyProgressMonitor
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.ProgressMonitor
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revplot.PlotCommit
import org.eclipse.jgit.revplot.PlotCommitList
import org.eclipse.jgit.revplot.PlotLane
import org.eclipse.jgit.revwalk.RevCommit
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.progs.gitview.git.GitCommand
import org.progs.gitview.git.ResetOption
import org.progs.gitview.git.git


@ExtendWith(MockKExtension::class)
class CommitInfoTest {

    @MockK
    lateinit var repository: Repository

    @MockK(relaxed = true)
    lateinit var plotLanes: PlotCommitList<PlotLane>

    @MockK(relaxed = true)
    lateinit var plotCommit: PlotCommit<PlotLane>

    @MockK(relaxed = true)
    lateinit var gitCommand: GitCommand

    //    private lateinit var commitInfo: CommitInfo


    @BeforeEach
    fun setUp() {
        mockkStatic(Repository::git)
        every { repository.git } returns gitCommand
//        every { plotCommit.lane.position } returns 5
//        every { plotCommit.parentCount } returns 2
    }

    @Test
    fun getId() {
        val id: Id = ObjectId(1,2,3,4,5)
        every { plotCommit.id } returns id
        val commitInfo = CommitInfo(repository, plotLanes, plotCommit)
        assertEquals(commitInfo.id, id)
    }

    @Test
    fun getFullMessage() {
        val message = "Full Message"
        every { plotCommit.fullMessage } returns message
        val commitInfo = CommitInfo(repository, plotLanes, plotCommit)
        assertEquals(commitInfo.fullMessage, message)
    }

    @Test
    fun getShortMessage() {
        val message = "Short Message"
        every { plotCommit.shortMessage } returns message
        val commitInfo = CommitInfo(repository, plotLanes, plotCommit)
        assertEquals(commitInfo.shortMessage, message)
    }

    @Test
    fun getCommitter() {
        val name = "Commiter"
        every { plotCommit.committerIdent.name } returns name
        val commitInfo = CommitInfo(repository, plotLanes, plotCommit)
        assertEquals(commitInfo.committer, name)
    }

    @Test
    fun getAuthor() {
        val name = "Author"
        every { plotCommit.authorIdent.name } returns name
        val commitInfo = CommitInfo(repository, plotLanes, plotCommit)
        assertEquals(commitInfo.author, name)
    }

//    @Test
//    fun getCommitTime() {
//        val time = Date()
//        val timeFormat = DateFormat.getDateTimeInstance().format(time)
//        every { plotCommit.committerIdent.getWhen() } returns time
//        assertEquals(commitInfo.commitTime, timeFormat)
//    }

    @Test
    fun getLaneNumber() {
        val laneNumber = 123
        every { plotCommit.lane.position } returns laneNumber
        val commitInfo = CommitInfo(repository, plotLanes, plotCommit)
        assertEquals(commitInfo.laneNumber, laneNumber)
    }

    @Test
    fun getParentCount() {
        val parentCount = 12
        every { plotCommit.parentCount } returns parentCount
        val commitInfo = CommitInfo(repository, plotLanes, plotCommit)
        assertEquals(commitInfo.parentCount, parentCount)
    }

    @Test
    fun getPassingThrough() {
        every {
            plotLanes.findPassingThrough(any(), any())
        } answers {
            val answers = listOf(
                mockk<PlotLane>(relaxed = true) { every { position } returns 1 },
                mockk<PlotLane>(relaxed = true) { every { position } returns 3 },
                mockk<PlotLane>(relaxed = true) { every { position } returns 2 },
            )
            secondArg<MutableList<PlotLane>>().addAll(answers)
        }
        val commitInfo = CommitInfo(repository, plotLanes, plotCommit)
        assertEquals(commitInfo.passingThrough, listOf(1,3,2))
    }

    @Test
    fun getParentList() {
        every { plotCommit.parents } returns
            arrayOf<RevCommit>(
                mockk<RevCommit>(relaxed = true) { every { id } returns ObjectId(1,2,3,4, 5) },
                mockk<RevCommit>(relaxed = true) { every { id } returns ObjectId(6,7,8,9,10) },
                mockk<RevCommit>(relaxed = true) { every { id } returns ObjectId(11,12,13,14,15) }
            )
        val commitInfo = CommitInfo(repository, plotLanes, plotCommit)
        assertEquals(commitInfo.parentList, listOf(
            ObjectId(1,2,3,4, 5),
            ObjectId(6,7,8,9,10),
            ObjectId(11,12,13,14,15)
        ))
    }

    @Test
    fun getChildList() {
        every { plotCommit.childCount } returns 3   // 0～2
        every { plotCommit.getChild(0) } returns mockk<PlotCommit<PlotLane>>(relaxed = true) {
            every { id } returns ObjectId(21,22,23,24,25) }
        every { plotCommit.getChild(1) } returns mockk<PlotCommit<PlotLane>>(relaxed = true) {
            every { id } returns ObjectId(31,32,33,34,35) }
        every { plotCommit.getChild(2) } returns mockk<PlotCommit<PlotLane>>(relaxed = true) {
            every { id } returns ObjectId(41,42,43,44,45) }
        val commitInfo = CommitInfo(repository, plotLanes, plotCommit)
        assertEquals(commitInfo.childList, listOf(
            ObjectId(21,22,23,24, 25),
            ObjectId(31,32,33,34, 35),
            ObjectId(41,42,43,44, 45),
        ))
    }

//    @Test
//    fun getDiffEntries() {
//    }

    @Test
    fun checkout() {
        val monitor: ProgressMonitor = object: EmptyProgressMonitor() {}
        val commitInfo = CommitInfo(repository, plotLanes, plotCommit)
        commitInfo.checkout(monitor)
        verify { gitCommand.checkoutCommit(plotCommit, monitor) }
    }

    @Test
    fun createBranch() {
        val branchName = "newBranch"
        val checkout = true
        val monitor: ProgressMonitor = object: EmptyProgressMonitor() {}
        val commitInfo = CommitInfo(repository, plotLanes, plotCommit)
        commitInfo.createBranch(branchName, checkout, monitor)
        verify { gitCommand.createBranch(plotCommit, branchName, checkout, monitor) }
    }

    @Test
    fun merge() {
        val id = ObjectId(1,2,3,4,5)
        every { plotCommit.id } returns id
        val message = "Merge Message"
        val monitor: ProgressMonitor = object: EmptyProgressMonitor() {}
        val commitInfo = CommitInfo(repository, plotLanes, plotCommit)
        commitInfo.merge(message, monitor)
        verify { gitCommand.merge(id, message, monitor) }
    }

    @Test
    fun createTag() {
        val tagName = "Tag Name"
        val message = "Tag Message"
        val commitInfo = CommitInfo(repository, plotLanes, plotCommit)
        commitInfo.createTag(tagName, message)
        verify { gitCommand.createTag(plotCommit, tagName, message) }
    }

    @Test
    fun rebaseBranch() {
        val monitor: ProgressMonitor = object: EmptyProgressMonitor() {}
        val commitInfo = CommitInfo(repository, plotLanes, plotCommit)
        commitInfo.rebaseBranch(monitor)
        verify { gitCommand.rebaseBranch(plotCommit, monitor) }
    }

    @Test
    fun reset() {
        val id = ObjectId(11,12,13,14,15)
        every { plotCommit.id } returns id
        val option = ResetOption.HARD
        val monitor: ProgressMonitor = object: EmptyProgressMonitor() {}
        val commitInfo = CommitInfo(repository, plotLanes, plotCommit)
        commitInfo.reset(option, monitor)
        verify { gitCommand.reset(id, option, monitor) }
    }

    @Test
    fun cherryPick() {
        val id = ObjectId(21,22,23,24,25)
        every { plotCommit.id } returns id
        val doCommit = true
        val monitor: ProgressMonitor = object: EmptyProgressMonitor() {}
        val commitInfo = CommitInfo(repository, plotLanes, plotCommit)
        commitInfo.cherryPick(doCommit, monitor)
        verify { gitCommand.cherryPick(id, doCommit, monitor) }
    }
}