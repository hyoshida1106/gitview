package org.progs.gitview.ui.util

import javafx.beans.InvalidationListener
import javafx.beans.value.ChangeListener
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.ScrollBar
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import kotlin.math.floor


/**
 * Tableのサイズ変更時に、指定されたカラムの幅を調整する
 * @param table         監視対象テーブル
 * @param adjustColumn  調整対象カラム
 */
class ColumnAdjuster(
    private val table: TableView<*>,
    private val adjustColumn: TableColumn<*, *>
) {
    /**
     * スクロールバーが表示されている場合、そのハンドルを保持する
     */
    private var verticalScrollBar: ScrollBar? = null

    /**
     * サイズ変更時の更新処理
     */
    private val invalidateListener = InvalidationListener { _ -> adjustColumnWidth() }
    private val valChangedListener = ChangeListener<Number> { _, _, _ -> adjustColumnWidth() }

    /**
     * 初期化
     */
    init {
        //テーブルサイズ変更リスナ
        table.widthProperty().addListener(valChangedListener)

        //テーブルのカラム幅変更リスナ
        table.columns.filter { it != adjustColumn }.forEach {
            it.widthProperty().addListener(valChangedListener)
        }
    }

    var margin: Double = 0.0

    /** 縦スクロールバー表示の有無を確認した上で、カラム幅を決定する */
    fun adjustColumnWidth() {
        //テーブル幅に収まるカラム幅を計算する
        var width = table.width -
                table.snappedLeftInset() -
                table.snappedRightInset() -
                table.columns.filter { it != adjustColumn }.sumOf { it.width }

        //スクロールバーが未検出であれば、探しておく
        if(verticalScrollBar == null) {
            findVerticalScrollBar(table)?.let {
                verticalScrollBar = it
                //幅と可視/不可視が変更された場合には、カラム幅を再計算する
                it.widthProperty().addListener(invalidateListener)
                it.visibleProperty().addListener(invalidateListener)
            }
        }

        //スクロールバーが表示されていれば、その幅を引いておく
        if(verticalScrollBar?.isVisible == true) {
            width -= verticalScrollBar!!.width
        }

        //カラム幅を設定する
        if(width >= 0) {
            width = floor(width) - margin    //少数以下切り捨て
            adjustColumn.prefWidth = width
            adjustColumn.minWidth = width
            adjustColumn.maxWidth = width
        }
    }

    /**
     * 縦スクロールバーを検索する
     * @param node      検索対象node
     * @return          縦スクロールバーのハンドルまたはnull
     */
    private fun findVerticalScrollBar(node: Node): ScrollBar? {
        return node.lookupAll(".scroll-bar").find {
            (it as ScrollBar).orientation == Orientation.VERTICAL
        } as ScrollBar?
    }
}