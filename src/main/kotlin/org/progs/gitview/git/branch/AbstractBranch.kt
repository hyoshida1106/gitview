package org.progs.gitview.git.branch

import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository

/**
 * Gitブランチ抽象クラス
 *
 * [LocalBranch]、[RemoteBranch]のベースとなる抽象クラス
 *
 * @property repository Gitリポジトリ
 * @property ref        ブランチの[Ref]インスタンス
 */
abstract class AbstractBranch(
    val repository: Repository,
    val ref: Ref
) {
    /** ブランチのパスを[Ref]から参照する */
    val path: String = ref.name

    /** 表示名称 */
    abstract val name: String
}

