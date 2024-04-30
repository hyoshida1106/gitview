package org.progs.gitview.ui.dialog

import org.progs.gitview.MainApp
import java.io.File

class EditorSelectionDialog: DialogInterface<File?> {

    override fun showDialog(): File? {
        return FileChooser(MainApp.resourceBundle.getString("Message.SelectEditor")).apply {
            extensionFilters.addAll(
                javafx.stage.FileChooser.ExtensionFilter("Execution File", "*.exe"),
                javafx.stage.FileChooser.ExtensionFilter("All Files", "*.*")
            )
        }.selectOpenFile()
    }
}