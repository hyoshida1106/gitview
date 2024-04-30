package org.progs.gitview.git

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ProgressMonitor
import org.eclipse.jgit.lib.Repository
import java.io.File


object RepositoryFactory {

    /**
     * 既存リポジトリをオープン
     */
    fun open(
        directoryPath: String
    ): Repository {
        return Git
            .open(File(directoryPath))
            .repository
    }

    /**
     * 新規作成
     */
    fun create(
        directoryPath: String,
        isBare: Boolean = false
    ): Repository {
        return Git
            .init()
            .setBare(isBare)
            .setDirectory(File(directoryPath))
            .setGitDir(File(directoryPath, ".git"))
            .call()
            .repository
    }

    /**
     * クローン
     */
    fun clone(
        monitor: ProgressMonitor,
        directoryPath: String,
        remoteUrl: String,
        isBare: Boolean = false
    ): Repository {
        return Git
            .cloneRepository()
            .setProgressMonitor(monitor)
            .setURI(remoteUrl)
            .setDirectory(File(directoryPath))
            .setBare(isBare)
            .call()
            .repository
    }
}