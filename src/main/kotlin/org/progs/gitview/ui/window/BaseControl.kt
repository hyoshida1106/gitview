package org.progs.gitview.ui.window


open class BaseControl {

    inner class WindowObserver {
        fun internalDisplayCompleted() = displayCompleted()
        fun internalEnterIdleState()   = enterIdleState()
    }

    init {
        observers.add(WindowObserver())
    }

    //表示完了時処理
    open fun displayCompleted() {}

    //アイドル時処理
    open fun enterIdleState() {}

    //インスタンスのリストを保持する
    companion object {
        val observers = mutableListOf<WindowObserver>()
        fun displayCompleted() { observers.forEach { it.internalDisplayCompleted() } }
        fun enterIdleState()   { observers.forEach { it.internalEnterIdleState()   } }
    }
}
