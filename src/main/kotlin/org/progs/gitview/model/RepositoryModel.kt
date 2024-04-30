package org.progs.gitview.model

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.WeakChangeListener
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffEntry.ChangeType
import org.eclipse.jgit.dircache.DirCache
import org.eclipse.jgit.lib.*
import org.progs.gitview.git.*
import org.progs.gitview.git.commit.*
import org.progs.gitview.model.item.CommitInfoItem
import java.io.File


class RepositoryModel {

    /*
        リポジトリ情報の保持と更新通知
     */

    /* リポジトリが有効であれば true */
    val available: Boolean get() = repository != null

    /* 現在のリポジトリ */
    private val repositoryProperty = SimpleObjectProperty<Repository?>(null)
    var repository: Repository?
        get() = repositoryProperty.value
        private set(value) {
            closeRepository()
            repositoryProperty.value = value
        }

    /* リスナ */
    private val repositoryListeners = mutableListOf<ChangeListener<Repository?>>()

    /* ハンドラ登録 */
    fun addListener(handler: (Repository?) -> Unit ) {
        val listener = ChangeListener { _, _, repository -> handler(repository) }
        repositoryListeners.add(listener)
        repositoryProperty.addListener(WeakChangeListener(listener))
    }

    /* 閉じる */
    fun closeRepository(){
        branchListModel.close()
        commitListModel.close()
        repositoryProperty.value?.close()
        repositoryProperty.value = null
    }


    /*
        リポジトリに関連する情報を保持するプロパティ
     */

    /* ブランチ一覧モデル */
    val branchListModel: BranchListModel = BranchListModel(this)

    /* コミット一覧モデル */
    val commitListModel: CommitListModel = CommitListModel(this, branchListModel)


    /*
        HEAD操作および参照
     */

    /** HEAD */
    val head: Id? get() = repository?.head

    /*
        リポジトリ関連情報の取得および生成
     */

    /** 現在チェックアウトされているブランチ名称 */
    val currentBranch: String? get() = repository?.branch

    /** ローカルブランチ一覧 */
    val localBranchList get() = repository?.git?.localBranchList ?: emptyList()

    /** リモートブランチ一覧 */
    val remoteBranchList get() = repository?.git?.remoteBranchList ?: emptyList()

    /** タグ一覧 */
    val tagList get() = repository?.git?.tagList ?: emptyList()

    /** リモートリポジトリの名称 */
    val remoteRepositoryName: String?
        get() = repository?.remoteRepositoryName

    /**  ローカルリポジトリのパス */
    val localRepositoryPath: String?
        get() = repository?.workTree?.absolutePath

    /** リモートリポジトリのパス */
    val remoteRepositoryPath: String?
        get() = repository?.remoteRepositoryPath

    /** Git登録ファイルの絶対パス */
    fun absoluteFile(filePath: String): File
        = File(localRepositoryPath, filePath)

    /** Remote Configuration情報 */
//    val remoteConfigList: List<RemoteConfig>
//        get() = repository?.remoteConfigList ?: emptyList()

    /** User Configuration情報 */
    private val userConfig: UserConfig?
        get() = repository?.config?.get(UserConfig.KEY)
    val userName: String?
        get() = userConfig?.committerName
    val mailAddr: String?
        get() = userConfig?.committerEmail

    /** ローカルブランチに対応するコミット一覧を生成する */
    fun getCommitList(
        branchList: List<LocalBranchModel>,
        maxCommits: Int
    ): List<CommitInfoItem> {
        return repository?.let { repo ->
            val commitList =  repo.getCommitList(branchList.map { it.localBranch }, maxCommits)
            commitList.map { CommitInfoItem(this, CommitInfo(repo, commitList, it)) }
        } ?: emptyList()
    }

    /** このリポジトリのWorkTreeFilesを取得する */
    fun getWorkTreeFiles(): WorkTreeFiles {
        return WorkTreeFiles(repository)
    }

    /** コミットに属するファイルの差分エントリ一覧を取得する */
    fun getDiffEntriesOfCommit(
        commitInfo: CommitInfo
    ): List<DiffEntry> {
        return repository?.let { commitInfo.getDiffEntries(repository!!) } ?: emptyList()
    }

    /** 差分エントリからCommitFileインスタンスを生成する */
    fun getCommitFile(
        diffEntry: DiffEntry,
        cached: Boolean
    ): CommitFile? {
        return repository?.let { CommitFile(it, diffEntry, cached) }
    }

    /*
        リポジトリ操作
     */

    /** リポジトリを開く */
    fun open(dirPath: String) {
        repository = RepositoryFactory.open(dirPath)
    }

    /** リポジトリを新規作成する */
    fun create(dirPath: String) {
        repository = RepositoryFactory.create(dirPath)
    }

    /** クローンを生成する */
    fun clone(
        monitor: ProgressMonitor,
        localPath: String,
        remotePath: String,
        bare: Boolean
    ) {
        repository = RepositoryFactory.clone(monitor, localPath, remotePath, bare)
    }

    /** リポジトリのフェッチ */
    fun fetch(
        monitor: ProgressMonitor
    ) {
        repository?.git?.fetch(monitor)
    }

    /** ファイル追加・更新をステージング */
    fun add(files: List<CommitFile>): DirCache? {
        return repository?.git?.add(files.map { it.path })
    }

    /** ファイル削除をステージング */
    fun delete(files: List<CommitFile>): DirCache? {
        return repository?.git?.delete(files.map { it.path })
    }

    /** ファイルをアンステージ */
    fun unStage(files: List<CommitFile>): Ref? {
        return repository?.git?.unStage(files.map { it.path })
    }

    /** ファイルをチェックアウト(更新をリセット) */
    fun checkout(files: List<CommitFile>): Ref? {
        return repository?.git?.checkout(files
            .filter { arrayOf(ChangeType.MODIFY, ChangeType.DELETE, ChangeType.RENAME).contains(it.entry.changeType) }
            .map { it.path })
    }

    /** ファイルをコミット */
    fun commit(
        userName: String,
        mailAddr: String,
        message: String,
        files: List<CommitFile>
    ) {
        repository?.git?.commit(userName, mailAddr, message, files.map { it.path })
    }

    /** コンフリクトファイルを”解決”マーク */
    fun markAsCorrect(file: ConflictingFile): DirCache? {
        return repository?.git?.add(listOf(file.path))
    }

    /** ファイルをチェックアウト(更新をリセット) */
    fun restore(file: ConflictingFile): Ref? {
        return repository?.git?.restore(file.path)
    }

    /** マージ中断 */
    fun abortMerge(
        monitor: ProgressMonitor? = null
    ) {
        repository?.let { repo ->
            repo.writeMergeCommitMsg(null)
            repo.writeMergeHeads(null)
            repo.git.reset(repo.resolve(Constants.HEAD), ResetOption.HARD, monitor)
        }
    }

    /** チェリーピック中断 */
    fun abortCherryPick(
        monitor: ProgressMonitor? = null
    ) {
        repository?.let { repo ->
            repo.writeMergeCommitMsg(null)
            repo.writeCherryPickHead(null)
            repo.git.reset(repo.resolve(Constants.HEAD), ResetOption.HARD, monitor)
        }
    }

    /** マージ状態の取得 */
    val mergeInProgress: Id? get() = repository?.mergeInProcess

    /** チェリーピック状態の取得 */
    val cherryPickInProgress: Id? get() = repository?.cherryPickInProcess

    /** Merge Message取得 */
    val mergeMessage: String get() = repository?.mergeMessage ?: ""
}