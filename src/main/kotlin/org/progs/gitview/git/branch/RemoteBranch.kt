package org.progs.gitview.git.branch

import org.eclipse.jgit.lib.*
import org.progs.gitview.git.git

/**
 * リモートブランチ操作
 *
 * リモートブランチに関する操作を定義するインターフェース
 */
interface RemoteBranchOperations {

    /**
     * リモートブランチをチェックアウトする
     * @param monitor   プログレスモニタ、使用しない場合は *null* を設定する
     */
    fun checkoutToLocal(monitor: ProgressMonitor?)

    /**
     * リモートブランチを削除する
     * @param monitor   プログレスモニタ、使用しない場合は *null* を設定する
     */
    fun remove(monitor: ProgressMonitor?)
}


/**
 * リモートブランチ具象クラス
 *
 * @param repository    Gitリポジトリ
 * @param ref           ブランチの[Ref]インスタンス
 */
class RemoteBranch(
    repository: Repository,
    ref: Ref
): AbstractBranch(repository, ref), RemoteBranchOperations {

    /**
     * 表示名<br>
     * [Ref]名称のリモート短縮名を使用する
     */
    override val name: String
        get() = repository.shortenRemoteBranchName(ref.name) ?: ""

    override fun checkoutToLocal(monitor: ProgressMonitor?) {
        repository.git.checkoutRemote(name, path, monitor)
    }

    override fun remove(monitor: ProgressMonitor?) {
        repository.git.removeRemote(path, monitor)
    }
}