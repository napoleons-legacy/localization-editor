package com.zelkatani.gui.view

import com.zelkatani.model.Localization
import com.zelkatani.model.LocalizationData
import javafx.scene.control.TableView

/**
 * A table to visualize some [LocalizationData].
 *
 * @param localization The other tables to query for duplication.
 * @param data The data to visualize.
 */
class LocalizationTable(localization: Localization, data: LocalizationData) : TableView<String>()
