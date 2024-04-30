package org.progs.gitview.ui.util

import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import org.progs.gitview.MainApp.Companion.resourceBundle
import org.progs.gitview.git.commit.CommitFile

/** 更新モードに対応するアイコン */
private val CommitFile.code: Int
    get() = when (this.mode) {
        CommitFile.Mode.ADD    -> IconCode.FILE_ADD_FILL
        CommitFile.Mode.COPY   -> IconCode.FILE_COPY_FILL
        CommitFile.Mode.DELETE -> IconCode.FILE_CLOSE_FILL
        CommitFile.Mode.MODIFY -> IconCode.FILE_EDIT_FILL
        CommitFile.Mode.RENAME -> IconCode.FILE_TRANSFER_FILL
        else -> IconCode.FILE_UNKNOW_LINE
    }

/** ファイルの更新タイプを文字列として取得する */
private val CommitFile.className: String
    get() = when (this.mode) {
        CommitFile.Mode.ADD -> "Add"
        CommitFile.Mode.COPY -> "Copy"
        CommitFile.Mode.DELETE -> "Delete"
        CommitFile.Mode.MODIFY -> "Modify"
        CommitFile.Mode.RENAME -> "Rename"
        else -> "Unknown"
    }

/** ファイルの更新タイプを文字列として取得する */
private val CommitFile.type: String
    get() = when (this.mode) {
        CommitFile.Mode.ADD -> resourceBundle.getString("Term.Add")
        CommitFile.Mode.COPY -> resourceBundle.getString("Term.Copy")
        CommitFile.Mode.DELETE -> resourceBundle.getString("Term.Delete")
        CommitFile.Mode.MODIFY -> resourceBundle.getString("Term.Modify")
        CommitFile.Mode.RENAME -> resourceBundle.getString("Term.Rename")
        else -> "Unknown"
    }

/** コミットタイプをアイコン付きで取得する */
val CommitFile.typeLabel: Node
    get() = HBox(
                code.iconLabel,
                Label(type)
            ).apply { styleClass.addAll(className, "commit-type") }
