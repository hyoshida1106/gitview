package org.progs.gitview.ui.dialog

import javafx.stage.FileChooser
import org.progs.gitview.ui.window.main.mainWindow
import java.io.File

fun FileChooser(title: String): FileChooser =
    FileChooser().apply { setTitle(title) }

fun FileChooser.selectSaveFile(): File? =
    showSaveDialog(mainWindow.rootWindow.scene.window)

fun FileChooser.selectOpenFile(): File? =
    showOpenDialog(mainWindow.rootWindow.scene.window)