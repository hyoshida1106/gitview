package org.progs.gitview.git.commit

import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.lib.Repository
import org.progs.gitview.git.getTrees
import org.progs.gitview.git.git


/**
 * ワークツリーファイル情報
 *
 * ワークファイルの状態を保持する
 * @property repository     リポジトリインスタンス
 */
class WorkTreeFiles(
    private val repository: Repository?
) {
    /**
     * ステージングされたファイルの一覧
     */
    val stagedFiles: List<CommitFile>

    /**
     * 更新されている(ステージングされていない)ファイルの一覧
     */
    val modifiedFiles: List<CommitFile>

    /**
     * コンフリクト中のファイルの一覧
     */
    val conflictingFiles: List<ConflictingFile>


    /**
     * ファイル有無を示すフラグ値<br>
     * *false* ならば更新ファイルは存在しない
     */
    val isEmpty: Boolean
        get() = stagedFiles.isEmpty() && modifiedFiles.isEmpty() && conflictingFiles.isEmpty()

    /**
     * コンフリクトファイルがあれば *true* を返す
     */
    val isConflicting: Boolean get() = conflictingFiles.isNotEmpty()

    /**
     * 初期化処理
     */
    init {
        if(repository != null) {
            val status = repository.git.status
            //インデックス済、更新、コンフリクトの各ファイル名を取得する
            val stagedFiles = status.added + status.changed + status.removed
            val modifiedFiles = status.missing + status.modified + status.untracked + status.untrackedFolders
            val conflictedFiles = status.conflictingStageState
            //ファイル名リストからWorkTreeFilesインスタンスを生成する
            this.stagedFiles = stagedFiles.mapNotNull { path ->
                getDiffEntry(repository, path, true)?.let { CommitFile(repository, it, true) } }
            this.modifiedFiles = modifiedFiles.mapNotNull { path ->
                getDiffEntry(repository, path, false)?.let { CommitFile(repository, it, false) } }
            this.conflictingFiles = conflictedFiles.mapNotNull { ConflictingFile(it.key, it.value) }
        } else {
            stagedFiles = emptyList()
            modifiedFiles = emptyList()
            conflictingFiles = emptyList()
        }
    }

    /**
     * Diff情報取得
     *
     * 指定されたファイルの更新状態をDiff実行結果として取得する
     * @param repository        リポジトリインスタンス
     * @param path              対象ファイルのパス
     * @param cached            対象ファイルが更新済であれば *true*
     * @return      Diff結果を[DiffEntry]形式で返す、*null* ならば差分なし
     */
    private fun getDiffEntry(
        repository: Repository,
        path: String,
        cached: Boolean
    ): DiffEntry? {
        val (oldTree, newTree) = repository.getTrees(cached)
        return repository.git.getDiffEntry(path, oldTree, newTree)
    }

    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(other !is WorkTreeFiles) return false
        return hashCode() == other.hashCode()
    }

    override fun hashCode(): Int {
        var result = stagedFiles.hashCode()
        result = 31 * result + modifiedFiles.hashCode()
        result = 31 * result + conflictingFiles.hashCode()
        return result
    }
}