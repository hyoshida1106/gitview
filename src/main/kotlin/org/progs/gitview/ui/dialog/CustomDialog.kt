package org.progs.gitview.ui.dialog

import javafx.beans.property.BooleanProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import org.progs.gitview.MainApp

/** カスタムダイアログ */
open class CustomDialog<Controller>(
    title: String,
    val controller: Controller,
    vararg buttons: ButtonType
) : Dialog<ButtonType>(), DialogInterface<ButtonType?>
        where Controller: CustomDialog.DialogControl {

    /** 継承クラスの名称 */
    private val className: String = javaClass.name.substringAfterLast(".")

    /** 初期化 */
    init {
        //タイトルとボタンを追加する
        this.title = title
        this.dialogPane.buttonTypes.addAll(buttons)

        //FXMLファイルをロードして、コントローラ参照を設定する
        val formPath = "/dialog/${className}.fxml"
        val loader = FXMLLoader(javaClass.getResource(formPath), MainApp.resourceBundle)
        loader.setController(controller)
        dialogPane.content = loader.load()

        //StyleSheetを登録
        javaClass.getResource("/GitView.css")?.let {
            dialogPane.stylesheets.add(it.toExternalForm())
        }
        javaClass.getResource("/dialog/$className.css")?.let {
            dialogPane.stylesheets.add(it.toExternalForm())
        }
        dialogPane.styleClass.add("CustomDialog")
        dialogPane.content.styleClass.add(className)

        // "X"で閉じないようにする
        dialogPane.scene.window.onCloseRequest = EventHandler { it.consume() }
    }

    /**
     *  ボタンにプロパティ、ハンドラを関連付ける<br>
     *  ボタンは @see ButtonType で指定する
     *  @param buttonType   対象にするボタン
     *  @param disable      Disabledプロパティを関連付ける
     *  @param handler      ボタン押下ハンドラを関連付ける
     */
    protected fun addButtonHandler(
        buttonType: ButtonType,
        disable: BooleanProperty?,
        handler: EventHandler<ActionEvent>? = null
    ) {
        val button = dialogPane.lookupButton(buttonType) ?: return
        disable?.let { button.disableProperty().bind(it) }
        handler?.let { button.addEventFilter(ActionEvent.ACTION, it) }
    }

    /**
     *  ダイアログをモーダル表示する
     *  @return OK/NGなどで終了した場合、そのButtonTypeが返される
     */
    override fun showDialog(): ButtonType? {
        val result = super.showAndWait()
        return if (result.isPresent) result.get() else null
    }

    /** コントローラ基本クラス */
    abstract class DialogControl {
        abstract fun initialize()
    }

}