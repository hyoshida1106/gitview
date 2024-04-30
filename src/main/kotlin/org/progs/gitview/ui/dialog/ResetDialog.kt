package org.progs.gitview.ui.dialog

import javafx.fxml.FXML
import javafx.scene.control.ButtonType
import javafx.scene.control.RadioButton
import javafx.scene.control.ToggleGroup
import org.progs.gitview.MainApp
import org.progs.gitview.git.ResetOption

/**
 * リセット
 */
class ResetDialog: CustomDialog<ResetDialog.Control>(
    MainApp.resourceBundle.getString("ResetDialog.Title"),
    Control(),
    ButtonType.OK, ButtonType.CANCEL
) {
    val option get() = controller.option

    class Control : DialogControl() {
        @FXML private lateinit var optionHard: RadioButton
        @FXML private lateinit var optionMixed: RadioButton
        @FXML private lateinit var optionSoft: RadioButton

        private val group = ToggleGroup()

        val option: ResetOption get() =
            when (group.selectedToggle) {
                optionHard -> ResetOption.HARD
                optionMixed -> ResetOption.MIXED
                else -> ResetOption.SOFT
            }

        override fun initialize() {
            optionHard.toggleGroup = group
            optionMixed.toggleGroup = group
            optionSoft.toggleGroup = group
            optionHard.isSelected = true
        }

    }
}