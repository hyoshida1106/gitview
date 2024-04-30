package org.progs.gitview.ui.util

import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region

private const val boldStyle = "-fx-font-weight: bold;"

/**
 * ラベル付きテキスト表示
 */
class TextMessage(
    title: Label,
    message:Label
): HBox() {

    /**
     * 文字列を指定するコンストラクタ
     */
    constructor(title:String, message:String):
        this(Label(title), Label(message))

    init {
//        styleClass.add("TextMessage")
//        title.styleClass.add("Title")
//        message.styleClass.add("Message")
//
        title.style = boldStyle
        title.minWidth = Region.USE_PREF_SIZE
        setHgrow(title, Priority.NEVER)
        children.addAll(title, message)
    }
}

