package org.progs.gitview.ui.dialog

import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.ButtonType
import org.controlsfx.control.PropertySheet
import org.controlsfx.control.PropertySheet.Item
import org.progs.gitview.MainApp.Companion.resourceBundle
import org.progs.gitview.ui.window.main.SystemConfig
import java.util.*
import kotlin.reflect.KMutableProperty0


class PreferenceDialog() : CustomDialog<PreferenceDialog.Control>(
    resourceBundle.getString("PreferenceDialog.Title"),
    Control(),
    ButtonType.OK
) {

    data class Property<T> (
        val category: String,
        val name: String,
        val description: String,
        var propertyName: KMutableProperty0<T>
    )

    class PropertyItem<T>(
        private val p: Property<T>
    ): Item {
        override fun getType(): Class<*> = p.propertyName()!!::class.java
        override fun getCategory(): String = p.category
        override fun getName(): String = p.name
        override fun getDescription(): String = resourceBundle.getString(p.description)
        override fun getValue(): T = p.propertyName()
        @Suppress("UNCHECKED_CAST")
        override fun setValue(value: Any) { (value as? T)?.let { p.propertyName.set(it) } }
        override fun getObservableValue(): Optional<ObservableValue<out Any>> = Optional.empty()
    }

    class Control: DialogControl() {

        private val properties = listOf(
            Property(
                category = "Config",
                name = "Editor Path",
                description = "PreferenceDialog.EditorPath",
                propertyName = SystemConfig::editorPath
            ),
            Property(
                category = "Config",
                name = "Max Commits",
                description = "PreferenceDialog.MaxCommits",
                propertyName = SystemConfig::maxCommits
            ),
        )

        @FXML private lateinit var preference: PropertySheet

        override fun initialize() {
            val list = FXCollections.observableArrayList<Item>()
            properties.forEach { list.add(PropertyItem(it)) }
            preference.items.setAll(list)
            preference.mode = PropertySheet.Mode.NAME
        }
    }

}