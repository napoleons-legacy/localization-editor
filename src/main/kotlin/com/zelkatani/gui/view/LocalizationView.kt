package com.zelkatani.gui.view

import com.zelkatani.gui.app.APPLICATION_NAME
import com.zelkatani.model.Mod
import javafx.stage.Screen
import tornadofx.*

class LocalizationScope(val mod: Mod) : Scope()

class LocalizationView : View(APPLICATION_NAME) {
    override val scope = super.scope as LocalizationScope

    /**
     * The tab pane for localization data visualization.
     */
    private val localizationTabPane = LocalizationTabPane()

    /**
     * The localization for the mod.
     */
    private val localization = scope.mod.localization

    override fun onDock() {
        primaryStage.apply {
            isResizable = true

            val screen = Screen.getPrimary()
            val bounds = screen.bounds

            width = bounds.width / 1.15
            height = bounds.height / 1.2
        }
    }

    override val root = borderpane {
        val squeezeBox = SqueezeBox(fillHeight = true).apply {
            fold("Mod Localization", expanded = true) {
                listview<String> {
                    items.addAll(localization.modFiles.keys)

                    onUserSelect {
                        localizationTabPane.focusData(it, localization, localization.modFiles.getValue(it))
                    }
                }
            }

            fold("Game Localization") {
                listview<String> {
                    items.addAll(localization.gameFiles.keys)

                    onUserSelect {
                        localizationTabPane.focusData(it, localization, localization.gameFiles.getValue(it))
                    }
                }
            }
        }

        left = squeezeBox

        center = localizationTabPane.apply {
            prefWidthProperty().bind(primaryStage.widthProperty() - squeezeBox.widthProperty())
            prefHeightProperty().bind(primaryStage.heightProperty())
        }
    }
}