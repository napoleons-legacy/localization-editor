package com.zelkatani.gui.controller

import com.zelkatani.gui.app.GAME_PATH
import com.zelkatani.gui.app.MOD_PATH
import com.zelkatani.gui.app.PREFERENCES_NAME
import com.zelkatani.gui.view.DirectoryView
import com.zelkatani.gui.view.LocalizationScope
import com.zelkatani.gui.view.LocalizationView
import com.zelkatani.model.GameLocation
import com.zelkatani.model.Mod
import tornadofx.Controller
import tornadofx.find

/**
 * A controller for [DirectoryView].
 */
class DirectoryController : Controller() {
    /**
     * Load preferences if they exist.
     * If they exist, they are NOT committed, but loaded in from [DirectoryView].
     */
    fun init() {
        preferences(PREFERENCES_NAME) {
            val gamePath = get(GAME_PATH, null) ?: return@preferences
            val modPath = get(MOD_PATH, null) ?: return@preferences

            GameLocation.gamePath = gamePath
            GameLocation.modPath = modPath
        }
    }

    /**
     * Commit changes to what was entered.
     * This parses the inputted files,
     * Validation of directories is done here.
     * An instance of [LocalizationView] is loaded in if successful.
     *
     * @return A [LocalizationView] if successful otherwise a [Result.failure]
     */
    fun commitGameLocation(): Result<LocalizationView> = try {
        val mod = Mod.from(GameLocation.modFolder)
        val localizationScope = LocalizationScope(mod)

        val localizationView = find<LocalizationView>(localizationScope)
        Result.success(localizationView)
    } catch (e: Exception) {
        Result.failure(e)
    }
}