package org.progs.gitview.git.commit

import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.lib.ProgressMonitor
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revplot.PlotCommit
import org.eclipse.jgit.revplot.PlotCommitList
import org.eclipse.jgit.revplot.PlotLane
import org.progs.gitview.git.ResetOption
import org.progs.gitview.git.diffFormatter
import org.progs.gitview.git.git
import org.progs.gitview.result.CherryPickRes
import org.progs.gitview.result.result
import java.io.ByteArrayOutputStream
import java.text.DateFormat
import java.util.*


/**
 * IDの別名定義
 */
typealias Id = org.eclipse.jgit.lib.ObjectId


/**
 * コミット操作
 *
 * コミットを対象とする操作を定義するインターフェース
 */
interface CommitOperations {

    /** コミットID */
    val id: Id

    /** コミットメッセージ */
    val fullMessage: String

    /** コミットメッセージ(短縮形式) */
    val shortMessage: String

    /** コミット実施者名 */
    val committer: String

    /**　コミット作者名 */
    val author: String

    /** コミット日時(文字列表記) */
    val commitTime: String

    /** 樹形図上のレーン番号 */
    val laneNumber: Int

    /** 親コミットの数 */
    val parentCount: Int


    /**
     * チェックアウト
     *
     * チェックアウトして新たな*HEAD*にする
     * @param monitor       プログレスモニタ、使用しない場合は *null* を設定する
     */
    fun checkout(monitor: ProgressMonitor? = null)

    /**
     * ブランチ新規作成
     *
     * このコミットから新たなブランチを作成する
     * @param branchName    作成するブランチの名称
     * @param checkout      *true*を指定すると作成したブランチをカレントブランチにする
     * @param monitor       プログレスモニタ、使用しない場合は *null* を設定する
     */
    fun createBranch(branchName: String, checkout: Boolean, monitor: ProgressMonitor? = null)

    /**
     * マージ
     *
     * このコミットをカレントブランチにマージする
     * @param message       マージメッセージ
     * @param monitor       プログレスモニタ、使用しない場合は *null* を設定する
     */
    fun merge(message: String, monitor: ProgressMonitor? = null)

    /**
     * タグ作成
     *
     * このコミットにタグを新規作成する
     * @param tagName       作成するタグの名称
     * @param message       タグメッセージ
     */
    fun createTag(tagName: String, message:String)

    /**
     * リベース
     *
     * カレントブランチをこのコミットにリベースする
     * @param monitor       プログレスモニタ、使用しない場合は *null* を設定する
     */
    fun rebaseBranch(monitor: ProgressMonitor? = null)

    /**
     * ブランチのリセット
     *
     * カレントブランチをこのコミットまでリセットする
     * @param option        リセット方法の指定
     * @param monitor       プログレスモニタ、使用しない場合は *null* を設定する
     */
    fun reset(option: ResetOption, monitor: ProgressMonitor? = null)

    /**
     * チェリーピック
     *
     * このコミットを*HEAD*にチェリーピックする
     * @param doCommit      チェリーピック後コミットする場合は*true*を指定する
     * @param monitor       プログレスモニタ、使用しない場合は *null* を設定する
     * @return              チェリーピック結果
     */
    fun cherryPick(doCommit: Boolean, monitor: ProgressMonitor? = null): CherryPickRes
}


/**
 * コミット情報
 *
 * * コミットの属性を保持する
 * * [CommitOperations]を実装する
 * @property repository     リポジトリインスタンス
 * @property plotLanes      コミットリスト
 * @property plotCommit     コミット情報を保持する[PlotCommit]インスタンス
 */
class CommitInfo(
    private val repository: Repository,
    private val plotLanes: PlotCommitList<PlotLane>,
    private val plotCommit: PlotCommit<PlotLane>
): CommitOperations {

    private val commitDateTime: Date = plotCommit.committerIdent.getWhen()

    // コミット情報の実装
    override val id: Id = plotCommit.id
    override val fullMessage: String = plotCommit.fullMessage
    override val shortMessage: String = plotCommit.shortMessage
    override val committer: String = plotCommit.committerIdent.name
    override val author: String = plotCommit.authorIdent.name
    override val commitTime: String = DateFormat.getDateTimeInstance().format(commitDateTime)
    override val laneNumber: Int = plotCommit.lane.position
    override val parentCount: Int = plotCommit.parentCount

    /**
     * 通過レーン一覧
     */
    val passingThrough: List<Int> get() = mutableListOf<PlotLane>().also { list ->
        plotLanes.findPassingThrough(plotCommit, list)
    }.map { it.position }

    /**
     * 親(祖先)コミット一覧
     */
    val parentList: List<Id>
        get() = plotCommit.parents.map { it.id }

    /**
     * 子(子孫)コミット一覧
     */
    val childList: List<Id>
        get() = (0 until plotCommit.childCount).mapNotNull { plotCommit.getChild(it)?.id }

    /**
     * 更新ファイルリストの取得
     *
     * コミットで更新されたファイルのリストを取得する
     * @param repository        リポジトリインスタンス
     * @return                  ファイルの[DiffEntry]一覧
     */
    fun getDiffEntries(
        repository: Repository
    ): List<DiffEntry> {
        val formatter = repository.diffFormatter(ByteArrayOutputStream())
        val prevTree = if (plotCommit.parentCount > 0) plotCommit.getParent(0).tree else null
        return formatter.scan(prevTree, plotCommit.tree)
    }

    override fun checkout(
        monitor: ProgressMonitor?
    ) {
        repository.git.checkoutCommit(plotCommit, monitor)
    }

    override fun createBranch(
        branchName: String,
        checkout: Boolean,
        monitor: ProgressMonitor?
    ) {
        repository.git.createBranch(plotCommit, branchName, checkout, monitor)
    }

    override fun merge(
        message: String,
        monitor: ProgressMonitor?
    ) {
        repository.git.merge(plotCommit.id, message, monitor)
    }

    override fun createTag(
        tagName: String,
        message: String
    ) {
        repository.git.createTag(plotCommit, tagName, message)
    }

    override fun rebaseBranch(
        monitor: ProgressMonitor?
    ) {
        repository.git.rebaseBranch(plotCommit, monitor)
    }

    override fun reset(
        option: ResetOption,
        monitor: ProgressMonitor?
    ) {
        repository.git.reset(plotCommit.id, option, monitor)
    }

    override fun cherryPick(
        doCommit: Boolean,
        monitor: ProgressMonitor?
    ): CherryPickRes {
        return repository.git.cherryPick(plotCommit.id, doCommit, monitor).result
    }
}