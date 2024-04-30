package org.progs.gitview.git

import org.eclipse.jgit.api.*
import org.eclipse.jgit.api.ResetCommand.ResetType
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.dircache.DirCache
import org.eclipse.jgit.lib.*
import org.eclipse.jgit.revplot.PlotCommit
import org.eclipse.jgit.revplot.PlotLane
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevObject
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.transport.FetchResult
import org.eclipse.jgit.transport.PushResult
import org.eclipse.jgit.transport.RefSpec
import org.eclipse.jgit.treewalk.AbstractTreeIterator
import org.eclipse.jgit.treewalk.filter.PathFilter
import org.progs.gitview.git.branch.LocalBranch
import org.progs.gitview.git.branch.RemoteBranch
import org.progs.gitview.git.commit.Id
import org.progs.gitview.git.tag.Tag

/**
 * Resetオプション
 *
 * GIT RESET オプションを定義する enum
 */
enum class ResetOption { HARD, MIXED, SOFT }


/**
 * GITコマンド実装
 *
 * GITコマンドを起動するためのインターフェース定義
 * @property repository     リポジトリインスタンス
 */
class GitCommand(
    private val repository: Repository
) {
    private val git = Git(repository)

    /**
     * フェッチ
     *
     * 現在のリポジトリをフェッチする
     * @param monitor       モニタインスタンス、省略時は [NullProgressMonitor] を使用
     * @param remote        取得元URL文字列、省略時は現在関連付けられているリモートリポジトリを使用
     * @param prune         *Prune* 指定の場合 *true* とする
     * @return              実行結果 [FetchResult] または *null*
     */
    fun fetch(
        monitor: ProgressMonitor = NullProgressMonitor.INSTANCE,
        remote: String? = null,
        prune: Boolean = false
    ): FetchResult? {
        val remoteName = repository.remoteRepositoryName ?: Constants.DEFAULT_REMOTE_NAME
        return git.fetch()
            .setProgressMonitor(monitor)
            .setRemote(remote ?: remoteName)
            .setRemoveDeletedRefs(prune)
            .call()
    }

    /**
     * リモートブランチ一覧
     *
     * 取得元リモートリポジトリのブランチ一覧を[RemoteBranch]のリストとして取得する
     */
    val remoteBranchList: List<RemoteBranch>
        get() = git.branchList()
            .setListMode(ListBranchCommand.ListMode.REMOTE)
            .call()
            .map { ref -> RemoteBranch(repository, ref) }

    /**
     * ローカルブランチ一覧
     *
     * リポジトリに取得済のブランチ一覧を[LocalBranch]のリストとして取得する
     */
    val localBranchList: List<LocalBranch>
        get() = git.branchList()
            .call()
            .map { ref -> LocalBranch(repository, ref) }

    /**
     * タグ一覧
     *
     * リポジトリのタグ一覧を[Tag]のリストとして取得する
     */
    val tagList: List<Tag>
        get() {
            val revWalk = RevWalk(repository)
            return git.tagList().call().mapNotNull { Tag.newInstance(repository, revWalk, it) }
        }

    /**
     * Statusの取得
     *
     * Git Statusコマンド実行結果を[Status]インスタンスとして取得する
     */
    val status: Status
        get() = git.status().call()

    /**
     * チェックアウト(Remote)
     *
     * リモートリポジトリのブランチをチェックアウトする
     * @param branchName        取得するブランチ名(ローカル)
     * @param branchPath        取得するリモートブランチのパス
     * @param monitor           プログレスモニタ、省略時は *null* を指定
     */
    fun checkoutRemote(
        branchName: String,
        branchPath: String,
        monitor: ProgressMonitor?
    ): Ref? {
        return git.checkout()
            .setProgressMonitor(monitor ?: NullProgressMonitor.INSTANCE)
            .setName(branchName)
            .setStartPoint(branchPath)
            .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
            .setCreateBranch(true)
            .call()
    }

    /**
     * チェックアウト(Local)
     *
     * 指定したローカルブランチをカレントブランチにする
     * @param branchName        ブランチ名
     * @param monitor           プログレスモニタ、省略時は *null* を指定
     */
    fun checkoutLocal(
        branchName: String,
        monitor: ProgressMonitor?
    ): Ref? {
        return git.checkout()
            .setProgressMonitor(monitor ?: NullProgressMonitor.INSTANCE)
            .setName(branchName)
            .call()
    }

    /**
     * チェックアウト(コミット指定)
     *
     * 指定したコミットを*HEAD*にする
     * @param plotCommit        コミット指定
     * @param monitor           プログレスモニタ、省略時は *null* を指定
     */
    fun checkoutCommit(
        plotCommit: PlotCommit<PlotLane>,
        monitor: ProgressMonitor?
    ): Ref? {
        return git.checkout()
            .setProgressMonitor(monitor ?: NullProgressMonitor.INSTANCE)
            .setName(Id.toString(plotCommit.id))
            .call()
    }

    /**
     * ブランチ削除(Remote)
     *
     * 指定したリモートブランチを削除する
     * @param branchName        対象ブランチ
     * @param monitor           プログレスモニタ、省略時は *null* を指定
     */
    fun removeRemote(
        branchName: String,
        monitor: ProgressMonitor?
    ): PushResult? {
        //未テスト
        return repository.remoteRepositoryPath?.replace(
            Constants.R_REMOTES + branchName + "/", Constants.R_HEADS
        )?.let { destination ->
            val refSpec = RefSpec().setSource(null).setDestination(destination)
            git.push()
                .setProgressMonitor(monitor ?: NullProgressMonitor.INSTANCE)
                .setRefSpecs(refSpec)
                .setRemote(branchName)
                .call()
        }?.elementAt(0)
    }

    /**
     * ブランチ削除(Local)
     *
     * 指定したローカルブランチを削除する
     * @param branchName        対象ブランチ
     * @param force             強制削除の場合 *true* を指定
     * @param monitor           プログレスモニタ、省略時は *null* を指定
     */
    fun removeLocal(
        branchName: String,
        force: Boolean,
        monitor: ProgressMonitor?
    ): MutableList<String>? {
        return git.branchDelete()
            .setProgressMonitor(monitor ?: NullProgressMonitor.INSTANCE)
            .setBranchNames(branchName)
            .setForce(force)
            .call()
    }

    /**
     * マージ(ブランチ)
     *
     * 指定したブランチをカレントブランチにマージする
     * @param ref               ブランチ指定
     * @param message           マージメッセージ
     * @param monitor           プログレスモニタ、省略時は *null* を指定
     */
    fun merge(
        ref: Ref,
        message: String,
        monitor: ProgressMonitor?
    ): MergeResult? {
        return git.merge()
            .setProgressMonitor(monitor ?: NullProgressMonitor.INSTANCE)
            .include(ref)
            .setMessage(message)
            .setCommit(true)
            .setFastForward(MergeCommand.FastForwardMode.FF)
            .call()
    }

    /**
     * マージ(コミット)
     *
     * 指定したコミットをカレントブランチにマージする
     * @param id                コミット指定
     * @param message           マージメッセージ
     * @param monitor           プログレスモニタ、省略時は *null* を指定
     */
    fun merge(
        id: Id,
        message: String,
        monitor: ProgressMonitor?
    ): MergeResult? {
        return git.merge()
            .setProgressMonitor(monitor ?: NullProgressMonitor.INSTANCE)
            .include(id)
            .setMessage(message)
            .setCommit(true)
            .setFastForward(MergeCommand.FastForwardMode.FF)
            .call()
    }

    /**
     * ブランチ作成(ブランチ名指定)
     *
     * 指定したブランチから新たなブランチを生成する
     * @param orgBranch         作成元ブランチ名
     * @param newBranch         新たに作成するブランチ名
     * @param checkout          作成したブランチをカレントブランチにする場合 *true*
     * @param monitor           プログレスモニタ、省略時は *null* を指定
     */
    fun createBranch(
        orgBranch: String,
        newBranch: String,
        checkout: Boolean,
        monitor: ProgressMonitor?
    ): Ref? {
        return if(checkout) {
            git.checkout()
                .setProgressMonitor(monitor ?: NullProgressMonitor.INSTANCE)
                .setName(newBranch)
                .setStartPoint(orgBranch)
                .setCreateBranch(true)
                .call()
        } else {
            git.branchCreate()
                .setName(newBranch)
                .setStartPoint(orgBranch)
                .call()
        }
    }

    /** ブランチ作成(コミット指定)
     *
     * 指定したコミットから新たなブランチを生成する
     * @param commit            作成元コミット
     * @param newBranch         新たに作成するブランチ名
     * @param checkout          作成したブランチをカレントブランチにする場合 *true*
     * @param monitor           プログレスモニタ、省略時は *null* を指定
     */
    fun createBranch(
        commit: PlotCommit<PlotLane>,
        newBranch: String,
        checkout: Boolean,
        monitor: ProgressMonitor?
    ): Ref? {
        return if(checkout) {
            git.checkout()
                .setProgressMonitor(monitor ?: NullProgressMonitor.INSTANCE)
                .setName(newBranch)
                .setStartPoint(commit)
                .setCreateBranch(true)
                .call()
        } else {
            git.branchCreate()
                .setName(newBranch)
                .setStartPoint(commit)
                .call()
        }
    }

    /**
     * Diff情報取得
     *
     * 指定されたファイルの更新状態をDiff実行結果として取得する
     * @param path              対象ファイルのパス
     * @param oldTree           変更前の *TreeIterator*
     * @param newTree           変更後の *TreeIterator*
     * @return                  Diff結果を[DiffEntry]形式で返す、*null* ならば差分なし
     */
    fun getDiffEntry(
        path: String,
        oldTree: AbstractTreeIterator,
        newTree: AbstractTreeIterator
    ): DiffEntry? {
        return git.diff()
            .setPathFilter(PathFilter.create(path))
            .setOldTree(oldTree)
            .setNewTree(newTree)
            .call()
            .getOrNull(0)
    }

    /**
     * リベース(ブランチ指定)
     *
     * カレントブランチを指定したブランチにリベースする
     * @param path              リベースするブランチ
     * @param monitor           プログレスモニタ、省略時は *null* を指定
     * @return                  リベース結果を [RebaseResult] で返す
     */
    fun rebaseBranch(
        path: String,
        monitor: ProgressMonitor?
    ): RebaseResult? {
        return git.rebase()
            .setProgressMonitor(monitor ?: NullProgressMonitor.INSTANCE)
            .setUpstream(path)
            .call()
    }

    /**
     * リベース(コミット指定)

     * カレントブランチを指定したコミットにリベースする
     * @param commit            リベースするコミット
     * @param monitor           プログレスモニタ、省略時は *null* を指定
     * @return                  リベース結果を [RebaseResult] で返す
     */
    fun rebaseBranch(
        commit: PlotCommit<PlotLane>,
        monitor: ProgressMonitor?
    ): RebaseResult? {
        return git.rebase()
            .setProgressMonitor(monitor ?: NullProgressMonitor.INSTANCE)
            .setUpstream(commit)
            .call()
    }

    /**
     * プッシュ
     *
     * ブランチをプッシュする
     * @param branchName        ブランチ名称
     * @param pushTag           タグをプッシュする場合 *true* を指定
     * @param monitor           プログレスモニタ、省略時は *null* を指定
     * @return                  プッシュ結果を [PushResult] で返す
     */
    fun push(
        branchName: String,
        pushTag: Boolean,
        monitor: ProgressMonitor?
    ): PushResult? {
        return git.push()
            .setProgressMonitor(monitor ?: NullProgressMonitor.INSTANCE)
            .add(branchName)
            .apply { if(pushTag) setPushTags() }
            .call()
            ?.elementAt(0)
    }

    /**
     * プル
     *
     * ブランチをプルする
     * @param monitor           プログレスモニタ、省略時は *null* を指定
     * @return                  プル結果を [PullResult] で返す
     */
    fun pull(
        monitor: ProgressMonitor?
    ): PullResult? {
        return git.pull()
            .setProgressMonitor(monitor ?: NullProgressMonitor.INSTANCE)
            .setRebase(BranchConfig.BranchRebaseMode.REBASE)
            .setRecurseSubmodules(SubmoduleConfig.FetchRecurseSubmodulesMode.ON_DEMAND)
            .call()
    }

    /**
     * 名称変更
     *
     * ブランチの名称を変更する
     * @param oldName           変更前の名前
     * @param newName           変更後の名前
     */
    fun renameBranch(
        oldName: String,
        newName: String
    ): Ref? {
        return git.branchRename()
            .setOldName(oldName)
            .setNewName(newName)
            .call()
    }

    /**
     * ステージング(追加・更新)
     *
     * ファイルの追加・更新をステージングする
     * @param files             対象ファイルのリスト
     * @return                  追加後の [DirCache]
     */
    fun add(
        files: List<String>
    ): DirCache? {
        return if(files.isNotEmpty()) {
            git.add()
                .apply { files.forEach { file -> this.addFilepattern(file) } }
                .call()
        } else null
    }

    /**
     * ステージング(削除)
     *
     * ファイルの削除をステージングする
     * @param files             対象ファイルのリスト
     * @return                  追加後の [DirCache]
     */
    fun delete(
        files: List<String>
    ): DirCache? {
        return if(files.isNotEmpty()) {
            git.rm()
                .apply { files.forEach { file -> this.addFilepattern(file) } }
                .call()
        } else null
    }

    /**
     * アンステージ
     *
     * 指定したステージング済ファイルをアンステージングする
     * @param files             対象ファイルのリスト
     */
    fun unStage(
        files: List<String>
    ): Ref? {
        return if(files.isNotEmpty()) {
            git.reset()
                .setRef(Constants.HEAD)
                .apply { files.forEach { file -> this.addPath(file) } }
                .call()
        } else null
    }

    /**
     * ファイル修正の取り消し
     *
     * 指定したファイルの更新を破棄し、リポジトリ登録状態に戻す
     * @param files             対象ファイルのリスト
     */
    fun checkout(
        files: List<String>
    ): Ref? {
        return if(files.isNotEmpty()) {
            git.checkout()
                .apply { files.forEach { file -> this.addPath(file) } }
                .call()
        } else null
    }

    /**
     * コミット
     *
     * 指定したステージング済ファイルをコミットする
     * @param userName          コミット実施ユーザ名
     * @param mailAddr          コミット実施ユーザのメールアドレス
     * @param message           コミットメッセージ
     * @param files             対象ファイルのリスト
     * @return                  実行したコミットを示す[RevCommit]を返す
     */
    fun commit(
        userName: String,
        mailAddr: String,
        message: String,
        files: List<String>
    ): RevCommit? {
        return if(files.isNotEmpty()) {
            git.commit()
                .setCommitter(userName, mailAddr)
                .setMessage(message)
                .apply { files.forEach { file -> setOnly(file) } }
                .call()
        } else null
    }

    /**
     * タグ作成
     *
     * 指定したコミットにタグを作成する
     * @param id                コミット指定
     * @param tagName           タグ名
     * @param message           タグメッセージ
     */
    fun createTag(
        id: RevObject,
        tagName: String,
        message: String
    ): Ref? {
        return git.tag()
            .setObjectId(id)
            .setName(tagName)
            .setMessage(message)
            .call()
    }

    /**
     * リセット
     *
     * 指定したコミットまで *HEAD* を戻す
     * @param id                コミット指定
     * @param option            リセット方法を [ResetOption] で指定する
     * @param monitor           プログレスモニタ、省略時は *null* を指定
     */
    fun reset(
        id: Id,
        option: ResetOption,
        monitor: ProgressMonitor?
    ): Ref? {
        val resetType: ResetType = when (option) {
            ResetOption.HARD -> ResetType.HARD
            ResetOption.MIXED -> ResetType.MIXED
            ResetOption.SOFT -> ResetType.SOFT
        }
        return git.reset()
            .setProgressMonitor(monitor ?: NullProgressMonitor.INSTANCE)
            .setMode(resetType)
            .setRef(Id.toString(id))
            .call()
    }

    /**
     * チェリーピック
     *
     * 指定したコミットを *HEAD* にチェリーピックする
     * @param id            対象とするコミット
     * @param doCommit      チェリーピック結果をコミットする場合 *true*
     * @param monitor           プログレスモニタ、省略時は *null* を指定
     */
    fun cherryPick(
        id: Id,
        doCommit: Boolean,
        monitor: ProgressMonitor?
    ): CherryPickResult {
        return git.cherryPick()
            .setProgressMonitor(monitor)
            .include(id)
            .setNoCommit(!doCommit)
            .call()
    }

    /**
     * タグ削除
     *
     * 指定したタグを削除する
     * @param name              削除するタグ名
     */
    fun deleteTag(
        name: String
    ): MutableList<String>? {
        return git.tagDelete()
            .setTags(name)
            .call()
    }

    /**
     * リストア
     *
     * ファイルの更新状態を破棄し、*HEAD*に戻す
     * @param path          対象とするリポジトリ
     */
    fun restore(
        path: String
    ): Ref? {
        return git.checkout()
            .addPath(path)
            .setStartPoint(Constants.HEAD)
            .call()
    }
}