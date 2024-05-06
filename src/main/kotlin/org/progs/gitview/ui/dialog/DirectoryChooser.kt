package org.progs.gitview.ui.dialog

import javafx.stage.DirectoryChooser
import org.progs.gitview.ui.window.main.mainWindow
import java.io.File

fun DirectoryChooser(title: String): DirectoryChooser
        = DirectoryChooser().apply { setTitle(title) }

fun DirectoryChooser.selectDirectory(): File? =
    showDialog(mainWindow.rootWindow.scene.window)