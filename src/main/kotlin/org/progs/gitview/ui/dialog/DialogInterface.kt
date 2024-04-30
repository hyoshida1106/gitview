package org.progs.gitview.ui.dialog

interface DialogInterface<T> {
    /** ダイアログを表示する */
    fun showDialog(): T
}