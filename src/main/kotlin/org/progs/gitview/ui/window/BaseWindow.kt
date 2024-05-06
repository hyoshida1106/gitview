package org.progs.gitview.ui.window

import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import org.progs.gitview.MainApp

/**
 * Window基本クラス
 */
open class BaseWindow<Controller>(
    controller: Controller,
) where Controller: BaseControl {

    /**
     * 継承クラスの名称
     */
    private val className: String = javaClass.name.substringAfterLast(".")

    /**
     * ルートウィンドウ
     */
    val rootWindow: Parent

    /**
     * 初期化
     */
    init {
        val formPath = "/window/${className}.fxml"
        val loader = FXMLLoader(javaClass.getResource(formPath), MainApp.resourceBundle)
        loader.setController(controller)
        rootWindow = loader.load()

        //クラス名称をCSSファイルとCSSクラスとして追加する
        javaClass.getResource("/window/$className.css")?.let {
            rootWindow.stylesheets.add(it.toExternalForm())
        }
        rootWindow.styleClass.add(className)
    }
}
