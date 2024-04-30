package org.progs.gitview.ui.util

import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.HBox

class IconButton(
    leftIconCode: Int?,
    text: String? = null,
    rightIconCode: Int? = null
): Button() {
    init {
        val box = HBox(3.0).apply {
            alignment = Pos.CENTER
        }
        leftIconCode?.let { code -> box.children.add(code.iconLabel) }
        text?.let { label -> box.children.add(Label(label)) }
        rightIconCode?.let { code -> box.children.add(code.iconLabel) }
        this.graphic = box
        this.styleClass.add("icon-button")
    }
}