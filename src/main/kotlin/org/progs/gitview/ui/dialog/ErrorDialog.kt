package org.progs.gitview.ui.dialog

import javafx.scene.control.Alert
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.progs.gitview.MainApp.Companion.resourceBundle
import org.progs.gitview.ui.window.main.MainWindow

class ErrorDialog(
    val message: String?
): Alert(AlertType.ERROR), DialogInterface<Unit> {

    companion object {
        private val messageMap = mapOf(
            RepositoryNotFoundException::class to ""
        )
    }

    constructor(e: Exception): this(e.localizedMessage) {
        e.printStackTrace()
    }

    override fun showDialog() {
        initOwner(MainWindow.rootWindow.scene.window)
        title = resourceBundle.getString("ErrorDialog.Title")
        contentText = message
        headerText = null
        showAndWait()
    }
}