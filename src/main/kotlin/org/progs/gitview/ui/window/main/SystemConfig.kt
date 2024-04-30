package org.progs.gitview.ui.window.main

import org.progs.gitview.Database
import org.progs.gitview.MainApp


object SystemConfig {

    //データベース定義
    private val systemConfigQueries = Database(MainApp.sqlDriver).systemConfigQueries

    // editor: 編集コマンド設定
    var editorPath: String
        get() = systemConfigQueries.select().executeAsOne().editor ?: ""
        set(value) { systemConfigQueries.updateEditor(value) }

    // maxCommits: リスト表示するコミットの最大数
    var maxCommits: Long
        get() = systemConfigQueries.select().executeAsOne().max_commits
        set(value) { systemConfigQueries.updateMaxCommits(value) }
}