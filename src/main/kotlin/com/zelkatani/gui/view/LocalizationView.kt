package com.zelkatani.gui.view

import com.zelkatani.gui.app.APPLICATION_NAME
import com.zelkatani.model.Mod
import tornadofx.Scope
import tornadofx.View
import tornadofx.textarea

class LocalizationScope(val mod: Mod) : Scope()

class LocalizationView : View(APPLICATION_NAME) {
    override val scope = super.scope as LocalizationScope

    /**
     * The localization for the mod.
     */
    private val localization = scope.mod.localization

    override val root = textarea {
        text = buildString {
            appendln("Game files:")
            localization.gameFiles.forEach { (fileName: String, _) ->
                appendln(fileName)
            }
            appendln("Mod files")
            localization.modFiles.forEach { (fileName: String, _) ->
                appendln(fileName)
            }
        }
    }
}