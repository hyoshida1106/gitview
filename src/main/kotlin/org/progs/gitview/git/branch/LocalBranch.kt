package org.progs.gitview.git.branch

import org.eclipse.jgit.lib.*
import org.eclipse.jgit.lib.Repository
import org.progs.gitview.git.git
import org.progs.gitview.git.getTrackingStatus

/**
 * ローカルブランチ操作
 *
 * ローカルブランチに関する操作を定義するインターフェース
 */
interface LocalBranchOperations {

    /**
     * このブランチをリモートブランチからチェックアウトする
     * @param monitor   プログレスモニタ、使用しない場合は *null* を設定する
     */
    fun checkoutFromRemote(monitor: ProgressMonitor? = null)

    /**
     * カレントブランチにマージする
     * @param message   ブランチメッセージ
     * @param monitor   プログレスモニタ、使用しない場合は *null* を設定する
     */
    fun mergeToCurrentBranch(message: String, monitor: ProgressMonitor? = null)

    /**
     * ブランチを新規作成する
     * @param newBranch 作成するブランチの名称
     * @param checkout  作成したブランチをチェックアウトする場合、*true* を指定する
     * @param monitor   プログレスモニタ、使用しない場合は *null* を設定する
     */
    fun createNewBranch(newBranch: String, checkout: Boolean, monitor: ProgressMonitor? = null)

    /**
     * カレントブランチをこのブランチにリベースする
     * @param monitor   プログレスモニタ、使用しない場合はnullを設定する
     */
    fun rebaseCurrentBranch(monitor: ProgressMonitor? = null)

    /**
     * プッシュ
     * @param pushTag   タグ情報をプッシュする場合 *true* を指定する
     * @param monitor   プログレスモニタ、使用しない場合は *null* を設定する
     */
    fun push(pushTag: Boolean, monitor: ProgressMonitor? = null)

    /**
     * プル
     * @param monitor   プログレスモニタ、使用しない場合は *null* を設定する
     */
    fun pull(monitor: ProgressMonitor? = null)

    /**
     * 削除
     * @param force     強制的に削除する場合 *true* を指定する
     * @param monitor   プログレスモニタ、使用しない場合は *null* を設定する
     */
    fun remove(force: Boolean, monitor: ProgressMonitor? = null)

    /**
     * 名称を変更する
     * @param newName   新たな名称
     */
    fun rename(newName: String)

}


/**
 * ローカルブランチ具象クラス
 *
 * @param repository    Gitリポジトリ
 * @param ref           ブランチの[Ref]インスタンス
 */
class LocalBranch(
    repository: Repository,
    ref: Ref
): AbstractBranch(repository, ref), LocalBranchOperations {

    /**
     * 表示名<br>
     * [Ref]名称のリモート短縮名を使用する
     */
    override val name: String
        get() = Repository.shortenRefName(ref.name)

    /**
     * 取得元リモートブランチの名称<br>
     * リモート未登録の場合 *null* を返す
     */
    val remoteTrackingBranch: String?
        get() = repository.getTrackingStatus(ref.name)?.remoteTrackingBranch

    override fun checkoutFromRemote(
        monitor: ProgressMonitor?
    ) {
        repository.git.checkoutLocal(name, monitor)
    }

    // マージ
    override fun mergeToCurrentBranch(
        message: String,
        monitor: ProgressMonitor?
    ) {
        repository.git.merge(ref, message, monitor)
    }

    // ブランチ作成
    override fun createNewBranch(
        newBranch: String,
        checkout: Boolean,
        monitor: ProgressMonitor?
    ) {
        repository.git.createBranch(name, newBranch, checkout, monitor)
    }

    // リベース
    override fun rebaseCurrentBranch(
        monitor: ProgressMonitor?
    ) {
        repository.git.rebaseBranch(path, monitor)
    }

    // プッシュ
    override fun push(
        pushTag: Boolean,
        monitor: ProgressMonitor?
    ) {
        repository.git.push(name, pushTag, monitor)
    }

    // プル
    override fun pull(
        monitor: ProgressMonitor?
    ) {
        repository.git.pull(monitor)
    }

    // 削除
    override fun remove(
        force: Boolean,
        monitor: ProgressMonitor?
    ) {
        repository.git.removeLocal(name, force, monitor)
    }

    // 名称変更
    override fun rename(
        newName: String
    ) {
        repository.git.renameBranch(name, newName)
    }

}

