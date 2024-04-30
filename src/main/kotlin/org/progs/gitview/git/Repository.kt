package org.progs.gitview.git

import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.dircache.DirCacheIterator
import org.eclipse.jgit.lib.*
import org.eclipse.jgit.revplot.PlotCommitList
import org.eclipse.jgit.revplot.PlotLane
import org.eclipse.jgit.revplot.PlotWalk
import org.eclipse.jgit.transport.RemoteConfig
import org.eclipse.jgit.treewalk.AbstractTreeIterator
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.eclipse.jgit.treewalk.EmptyTreeIterator
import org.eclipse.jgit.treewalk.FileTreeIterator
import org.progs.gitview.git.branch.LocalBranch
import java.io.ByteArrayOutputStream
import java.io.File


/** リモートブランチのトラッキング状態 */
fun Repository.getTrackingStatus(path: String): BranchTrackingStatus? =
    BranchTrackingStatus.of(this, path)

/** GITコマンドインスタンス */
val Repository.git: GitCommand
    get() = GitCommand(this)

/** Commit一覧 */
fun Repository.getCommitList(
    localBranchList: List<LocalBranch>,
    commitSize: Int
): PlotCommitList<PlotLane> {
    val commitList = PlotCommitList<PlotLane>()
    PlotWalk(this).use { plotWalk ->
        localBranchList.forEach { plotWalk.markStart(plotWalk.parseCommit(it.ref.objectId)) }
        commitList.source(plotWalk)
        commitList.fillTo(commitSize)
    }
    return commitList
}

/** HEAD */
val Repository.head: ObjectId? get() = resolve(Constants.HEAD)

/** Remote Configuration情報 */
val Repository.remoteConfigList: List<RemoteConfig>
    get() = RemoteConfig.getAllRemoteConfigs(config)

/** リモートリポジトリ名称(未定義はnull) */
val Repository.remoteRepositoryName: String?
    get() = remoteConfigList.firstOrNull()?.name

/** リモートリポジトリのパス(未定義はnull) */
val Repository.remoteRepositoryPath: String?
    get() = remoteConfigList.firstOrNull()?.urIs?.firstOrNull()?.toPrivateString()

/** DiffFormatterの取得 */
fun Repository.diffFormatter(
    output: ByteArrayOutputStream
): DiffFormatter {
    return DiffFormatter(output).apply { setRepository(this@diffFormatter) }
}

/** cacheに対応したTreeIteratorの取得 */
fun Repository.getTrees(
    cached: Boolean
): Pair<AbstractTreeIterator, AbstractTreeIterator> {
    if(cached) {
        val headTree = resolve(Constants.HEAD + "^{tree}")
        val oldTree = if(headTree != null) {
            CanonicalTreeParser().apply { reset(newObjectReader(), headTree) }
        } else {
            EmptyTreeIterator()
        }
        val newTree = DirCacheIterator(readDirCache())
        return Pair(oldTree, newTree)
    } else {
        val oldTree = DirCacheIterator(readDirCache())
        val newTree = FileTreeIterator(this)
        return Pair(oldTree, newTree)
    }
}

val Repository.cherryPickHeadPath: String? get() = directory?.absolutePath?.plus("/CHERRY_PICK_HEAD")
val Repository.mergeHeadPath: String? get() = directory?.absolutePath?.plus("/MERGE_HEAD")
val Repository.mergeMessagePath: String? get() = directory?.absolutePath?.plus("/MERGE_MSG")

private fun readFileContents(path: String?): String? {
    return path?.let {
        val file = File(path)
        if (file.exists()) file.readText() else null
    }
}

private fun readObjectId(path: String?): ObjectId? {
    return path?.let {
        val file = File(path)
        if(file.exists()) {
            file.useLines { it.firstOrNull()?.let { str -> ObjectId.fromString(str) } }
        } else null
    }
}

/** CherryPick実行中 */
val Repository.cherryPickInProcess: ObjectId? get() = readObjectId(cherryPickHeadPath)

/** Merge実行中 */
val Repository.mergeInProcess: ObjectId? get() = readObjectId(mergeHeadPath)

/** Merge Message取得 */
val Repository.mergeMessage: String? get() = readFileContents(mergeMessagePath)