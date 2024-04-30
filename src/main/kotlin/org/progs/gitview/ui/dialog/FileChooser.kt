package org.progs.gitview.ui.dialog

import javafx.stage.FileChooser
import org.progs.gitview.ui.window.main.MainWindow
import java.io.File

fun FileChooser(title: String): FileChooser =
    FileChooser().apply { setTitle(title) }

fun FileChooser.selectSaveFile(): File? =
    showSaveDialog(MainWindow.rootWindow.scene.window)

fun FileChooser.selectOpenFile(): File? =
    showOpenDialog(MainWindow.rootWindow.scene.window)