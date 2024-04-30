package org.progs.gitview.ui.window.commitlist

import javafx.scene.Node
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.TableRow
import javafx.scene.layout.Pane
import javafx.scene.paint.Paint
import javafx.scene.shape.ArcType
import javafx.scene.transform.Affine
import org.progs.gitview.model.item.CommitListItem

/**
 * コミットツリーセル データクラス
 */
class CommitTreeCellData(
    private val commitList: CommitListWindow.Control,
    private val commitTreeData: CommitListItem
): CommitListWindow.Control.CellData {

    //コンテキストメニュー
    override val contextMenu = commitTreeData.contextMenu

    override fun update(): Pair<Node?, String?> {
        return Pair(null, null)
    }

    companion object {
        private const val MERGE_MARK_COLOR = "white"
        private const val MARK_RADIUS = 3.5
        private const val LINE_WIDTH  = 2.5

        /** レーン番号に対する表示色を定義する */
        private val colors = arrayOf(
            "blue", "red", "teal", "slateGrey", "green", "darkMagenta", "cadetBlue",
            "darkOliveGreen", "purple", "maroon"
        )
    }

    /** Border幅を考慮したX座標移動量 */
    private var leftMargin = 0.0

    /** セル表示を更新する */
    override fun layout(
        tableCell: CommitListWindow.Control.Cell
    ) {
        val canvas = Canvas(tableCell.width, tableCell.height)
        val height = tableCell.height

        //Rowのボーダー量を取得してX座標移動量を算出する
        leftMargin = (tableCell.parent as TableRow<*>).border?.strokes?.getOrNull(0)?.widths?.left ?: 0.0

        //ライン描画
        commitTreeData.passThroughLanes.forEach { p -> drawPassThroughLine(canvas, p, height) }
        commitTreeData.exitingLanes.forEach { b -> drawBranchLine(canvas, commitTreeData.laneNumber, b, height) }
        commitTreeData.enteringLanes.forEach { b -> drawMergeLine(canvas, commitTreeData.laneNumber, b, height) }

        //ヘッドへ向かうラインを描画
        val headLane = commitTreeData.headLane
        if(headLane != null) {
            if(commitTreeData.isHead) {
                drawBranchLine(canvas, commitTreeData.laneNumber, headLane, height)
            } else if(headLane != commitTreeData.laneNumber) {
                drawPassThroughLine(canvas, headLane, height)
            }
        }

        //マークを描画
        drawCommitMark(canvas, commitTreeData, height)
        tableCell.graphic = Pane(canvas)
        tableCell.text = null
    }

    /** 樹形図描画用のGraphic Contextを取得する */
    private fun getGraphicContext(
        canvas: Canvas,
        lane: Int
    ): GraphicsContext {
        val p = Paint.valueOf(colors[lane % colors.size])
        val a = Affine().apply { tx = -leftMargin }
        return canvas.graphicsContext2D.apply {
            this.lineWidth = LINE_WIDTH
            this.fill = p
            this.stroke = p
            this.transform = a
        }
    }

    /** コミットマーク円を描画する */
    private fun drawCommitMark(
        canvas: Canvas,
        commitInfo: CommitListItem,
        height: Double
    ) {
        val lane = commitInfo.laneNumber
        val gc = getGraphicContext(canvas, lane)
        val xc = commitList.treeLinePosition(lane)
        val yc = height / 2.0
        val xr = MARK_RADIUS
        val yr = MARK_RADIUS
        gc.fillOval(xc - xr, yc - yr, xr * 2.0, yr * 2.0)
        if (commitInfo.isMerge) {
            gc.fill = Paint.valueOf(MERGE_MARK_COLOR)
            gc.fillOval(xc - xr / 2.0, yc - yr / 2.0, xr, yr)
        }
    }

    /** 通過線を描画する */
    private fun drawPassThroughLine(
        canvas: Canvas,
        lane: Int,
        height: Double
    ) {
        val gc = getGraphicContext(canvas, lane)
        val x = commitList.treeLinePosition(lane)
        gc.strokeLine(x, 0.0, x, height)
    }

    /** ブランチ線を描画する */
    private fun drawBranchLine(
        canvas: Canvas,
        currentLane: Int,
        branchLane: Int,
        height: Double
    ) {
        val gc = getGraphicContext(canvas, branchLane)
        val xs = commitList.treeLinePosition(currentLane)
        val xe = commitList.treeLinePosition(branchLane)
        val xc = commitList.treeLinePitch
        val yr = height / 2.0 - 2.0
        when {
            xs < xe -> {
                gc.strokeLine(xs, yr, xe - xc, yr)
                gc.strokeLine(xe, yr - yr, xe, 0.0)
                gc.strokeArc(
                    xe - 2.0 * xc, yr - 2.0 * yr, 2.0 * xc, 2.0 * yr,
                    270.0, 90.0, ArcType.OPEN
                )
            }
            xs > xe -> {
                gc.strokeLine(xs, yr, xe + xc, yr)
                gc.strokeLine(xe, yr - yr, xe, 0.0)
                gc.strokeArc(
                    xe, yr - 2.0 * yr, 2.0 * xc, 2.0 * yr,
                    180.0, 90.0, ArcType.OPEN
                )
            }
            else -> {
                gc.strokeLine(xs, 0.0, xs, yr)
            }
        }
    }

    /** マージ線を描画する */
    private fun drawMergeLine(
        canvas: Canvas,
        currentLane: Int,
        mergeLane: Int,
        height: Double
    ) {
        val gc = getGraphicContext(canvas, mergeLane)
        val xs = commitList.treeLinePosition(mergeLane)
        val xe = commitList.treeLinePosition(currentLane)
        val ym = height / 2.0 + 2.0
        val xr = commitList.treeLinePitch
        val yr = height - ym
        when {
            xs > xe -> {
                gc.strokeLine(xs, height, xs, ym + yr)
                gc.strokeLine(xe, ym, xs - xr, ym)
                gc.strokeArc(
                    xs - 2.0 * xr, ym, 2.0 * xr, 2.0 * yr,
                    0.0, 90.0, ArcType.OPEN
                )
            }
            xs < xe -> {
                gc.strokeLine(xs, height, xs, ym + yr)
                gc.strokeLine(xs + xr, ym, xe, ym)
                gc.strokeArc(
                    xs, ym, 2.0 * xr, 2.0 * yr,
                    90.0, 90.0, ArcType.OPEN
                )
            }
            else -> {
                gc.strokeLine(xe, height, xe, ym)
            }
        }
    }
}
