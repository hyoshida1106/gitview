package org.progs.gitview.git.commit

import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.treewalk.filter.PathFilter
import org.progs.gitview.git.getTrees
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset


/**
 * コミット対象ファイル情報
 *
 * コミットに含まれる更新ファイル個々の情報を保持するクラス
 * @property repository     リポジトリインスタンス
 * @property entry          ファイルの差分情報
 * @property cached         キャッシュ情報の場合 true
 */
class CommitFile(
    val repository: Repository,
    val entry: DiffEntry,
    private val cached: Boolean
) {
    companion object {
        /**
         * 差分(diff)情報から定義行を取得するための正規表現
         */
        private val headerRegExp = Regex("""@@ +-(\d+),\d+ +\+(\d+).*""")
    }

    /**
     * 変更モードの定義
     */
    enum class  Mode {
        ADD,
        COPY,
        DELETE,
        MODIFY,
        RENAME,
        UNKNOWN }

    /**
     * 更新ファイルの変更モード
     *
     * 更新ファイルの変更モードを取得する
     */
    val mode: Mode = when (entry.changeType) {
        DiffEntry.ChangeType.ADD    -> Mode.ADD
        DiffEntry.ChangeType.COPY   -> Mode.COPY
        DiffEntry.ChangeType.DELETE -> Mode.DELETE
        DiffEntry.ChangeType.MODIFY -> Mode.MODIFY
        DiffEntry.ChangeType.RENAME -> Mode.RENAME
        else -> Mode.UNKNOWN
    }

    /**
     * 更新ファイルのパス
     *
     * 更新ファイルのパスを取得する<br>
     * [Mode.DELETE]の場合は削除前のパス、それ以外は更新後のパスを返す
     * */
    val path: String = if (entry.changeType == DiffEntry.ChangeType.DELETE) entry.oldPath else entry.newPath

    /**
     * ファイル更新情報ヘッダ
     *
     * ファイル更新情報をHunk単位で管理するためのヘッダクラス<br>
     * 更新前(左)後(右)の先頭行番号とテキスト列を保持する
     */
    inner class DiffLineHeader(val header: String) {
        val textLines = mutableListOf<String>() //テキストリスト
        var leftLine : Int  //左行No.
        var rightLine: Int  //右行No.
        init {
            val result = headerRegExp.matchEntire(header)
            leftLine  = result?.groupValues?.getOrNull(1)?.toInt() ?: 0
            rightLine = result?.groupValues?.getOrNull(2)?.toInt() ?: 0
        }
    }

    /**
     *  ファイル更新情報の取得と解析
     *
     *  Unified Diffリストを解析し、[DiffLineHeader]のリストに変換する<br>
     *  解析対象となるDiffリストは[getDiffTextLines]で取得する
     *  @return [DiffLineHeader]リスト
     */
    fun getDiffLines(): List<DiffLineHeader> {
        return mutableListOf<DiffLineHeader>().also { list ->
            getDiffTextLines().forEach { line ->
                if (line.startsWith("@@")) {
                    list.add(DiffLineHeader(line))
                } else {
                    list.lastOrNull()?.textLines?.add(line)
                }
            }
        }
    }

    /**
     * ファイル差分の取得
     *
     * United Diffテキスト形式で差分(ファイル更新内容)を取得する
     * @return  テキストを文字列リストとして返す
     * @see     [Repository.getTrees]
     */
    fun getDiffTextLines(): List<String> {
        val output = ByteArrayOutputStream()
        val formatter = DiffFormatter(output)
        formatter.setRepository(repository)
        formatter.pathFilter = PathFilter.create(path)
        val (oldTree, newTree) = repository.getTrees(cached)
        formatter.scan(oldTree, newTree)
        formatter.format(entry)
        formatter.flush()
        return output.toByteArray().inputStream()
                .bufferedReader(Charset.forName("utf-8")).readLines()
    }

    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(other !is CommitFile) return false
        return this.mode == other.mode && this.path == other.path
    }

    override fun hashCode(): Int {
        var result = mode.hashCode()
        result = 31 * result + path.hashCode()
        return result
    }
}