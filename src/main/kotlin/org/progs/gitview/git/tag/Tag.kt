package org.progs.gitview.git.tag

import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevTag
import org.eclipse.jgit.revwalk.RevWalk
import org.progs.gitview.git.commit.Id
import org.progs.gitview.git.git

/**
 * タグ操作
 *
 * タグ操作インターフェース
 */
interface TagOperations {
    /**
     * タグの削除
     *
     * このタグを削除する
     */
    fun remove()
}


/**
 * タグ情報
 *
 * タグに関する情報を保持する
 * @property repository         リポジトリインスタンス
 * @property name               タグ名
 * @property id                 タグの[Id]
 */
data class Tag(
    val repository: Repository,
    val name: String,
    val id: Id
): TagOperations {
    companion object {
        /**
         * インスタンス生成
         *
         * 新規インスタンス生成
         * @param repository        リポジトリインスタンス
         * @param revWalk           タグ検索で使用している[RevWalk]
         * @param ref               タグ情報を保持する[Ref]
         */
        fun newInstance(
            repository: Repository,
            revWalk: RevWalk,
            ref: Ref
        ): Tag? {
            return when (val obj = revWalk.parseAny(ref.objectId)) {
                is RevTag -> {
                    Tag(
                        repository = repository,
                        name = obj.tagName,
                        id = obj.getObject().id
                    )
                }
                is RevCommit -> {
                    Tag(
                        repository = repository,
                        name = Repository.shortenRefName(ref.name),
                        id = obj.id
                    )
                }
                else -> {
                    null
                }
            }
        }

    }

    override fun remove() {
        repository.git.deleteTag(name)
    }
}