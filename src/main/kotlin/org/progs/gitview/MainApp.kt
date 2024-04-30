package org.progs.gitview

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import javafx.application.Application
import javafx.application.HostServices
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import org.progs.gitview.ui.alert.ConfirmationType
import org.progs.gitview.ui.alert.confirm
import org.progs.gitview.ui.util.IdleTimer
import org.progs.gitview.ui.util.myLogger
import org.progs.gitview.ui.window.BaseControl
import org.progs.gitview.ui.window.main.MainWindow
import java.io.File
import java.lang.System.getProperty
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.exists
import kotlin.system.exitProcess

/**
 * アプリケーションクラス
 */
class MainApp : Application() {

    companion object {
        /** データベースを格納するディレクトリ名称 */
        private val DATABASE_DIR = getProperty("user.home") + "/.gitview/"

        /** データベースファイル名 */
        private val DATABASE_PATH = DATABASE_DIR + "Database.db"

        val resourceBundle: ResourceBundle = ResourceBundle.getBundle("GitView")
        val sqlDriver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:${File(DATABASE_PATH).absolutePath}")

        init {
            if(!File(DATABASE_PATH).exists()) {
                val path = Path(DATABASE_DIR)
                if(!path.exists()) {
                    path.createDirectory()
                }
                Database.Schema.create(sqlDriver)
            }
        }
        fun confirmToQuit() {
            val message = resourceBundle.getString("Message.QuitConfirmation")
            if (confirm(ConfirmationType.YesNo, message)) {
                exitProcess(0)
            }
        }

        var myHostServices: HostServices? = null
    }

    private lateinit var idleTimer: IdleTimer

    override fun start(stage: Stage) {
        myLogger(this).info("start")

        try {
            if(MainWindow.propWidth > 0.0) {
                stage.scene = Scene(MainWindow.rootWindow, MainWindow.propWidth, MainWindow.propHeight)
            } else {
                stage.scene = Scene(MainWindow.rootWindow)
                stage.isMaximized = true
            }
            stage.title = "GitView"
            javaClass.getResource("/GitView.css")?.let {
                stage.scene.stylesheets.add(it.toExternalForm())
            }
            stage.icons.add(Image(Companion::class.java.getResourceAsStream("/image/gitview_icon.png")))

            //表示完了時の処理
            stage.setOnShown {
                myLogger(this).debug("OnShown")
                BaseControl.displayCompleted()
            }

            //アイドル処理
            idleTimer = IdleTimer(stage, 500, false) {
                BaseControl.enterIdleState()
            }

            myHostServices = this.hostServices

            //Main Window表示
            stage.show()

        } catch (e: java.lang.Exception) {
            //例外発生時、StackTraceを表示して終了する
            e.printStackTrace()
            exitProcess(-1)
        }
    }
}
