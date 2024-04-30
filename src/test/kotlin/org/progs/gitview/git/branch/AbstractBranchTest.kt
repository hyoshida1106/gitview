package org.progs.gitview.git.branch

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith


@ExtendWith(MockKExtension::class)
class AbstractBranchTest {

    @MockK
    lateinit var ref: Ref

    @MockK
    lateinit var repository: Repository

    @Test
    fun getPathTest() {
        val testPath = "TestPath"
        every { ref.name } returns testPath
        Assertions.assertEquals(object:AbstractBranch(repository, ref){
            override val name: String = "name"
        }.path, testPath)
    }
}