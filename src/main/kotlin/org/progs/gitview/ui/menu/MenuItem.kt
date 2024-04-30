package org.progs.gitview.ui.menu

import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.MenuItem
import javafx.scene.input.KeyCodeCombination
import org.progs.gitview.ui.util.iconLabel

class MenuItem(
    text: String,
    bold: Boolean = false,
    iconCode: Int? = null,
    accelerator: KeyCodeCombination? = null,
    eventHandler: EventHandler<ActionEvent>
) : MenuItem(text) {

    init {
        if (bold) {
            this.style = "-fx-font-weight: bold;"
        }
        iconCode?.let { code ->
            this.graphic = code.iconLabel
        }
        accelerator?.let {
            this.accelerator = it
        }
        this.onAction = eventHandler
    }
}

