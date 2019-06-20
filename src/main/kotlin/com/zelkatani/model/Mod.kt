package com.zelkatani.model

import java.io.File

/**
 * The model for an entire mod. Currently only keeps track of [Localization].
 */
data class Mod(val localization: Localization) {
    companion object : ModelBuilder<Mod> {
        override fun from(file: File): Mod {
            val gameFolder = GameLocation.gameFolder

            val localization = Localization.from(gameFolder.resolve("localisation"))
            return Mod(localization)
        }
    }
}