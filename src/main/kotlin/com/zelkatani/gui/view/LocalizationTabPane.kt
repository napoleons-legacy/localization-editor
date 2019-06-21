package com.zelkatani.gui.view

import com.zelkatani.model.Localization
import com.zelkatani.model.LocalizationData
import javafx.scene.control.Tab
import javafx.scene.control.TabPane

/**
 * A tab pane for visualizing localization data.
 */
class LocalizationTabPane : TabPane() {

    init {
        tabClosingPolicy = TabClosingPolicy.ALL_TABS
    }

    /**
     * Focus a tab in the tabs listing. If it does not exist, a new tab is created and focused.
     *
     * @param name The tab name.
     * @param localization The defined localization for analyzing purposes.
     * @param data The data to forward into the tab content.
     */
    fun focusData(name: String, localization: Localization, data: LocalizationData) {
        val tab = tabs.find {
            it.text == name
        } ?: Tab(name, LocalizationTable(localization, data)).also {
            tabs += it
        }

        tab.let {
            selectionModel.select(it)
            it.content?.requestFocus()
        }
    }

}