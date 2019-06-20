package com.zelkatani.model

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.File
import java.io.FileFilter

/**
 * Get a list of all records without any comments.
 */
private fun CSVParser.removeComments() = filter {
    val first = it[0].trimStart()
    first.isNotBlank() && first[0] != '#'
}

/**
 * A map of filename to localization records
 */
typealias LocalizationFile = Map<String, LocalizationData>

data class Localization(val gameFiles: LocalizationFile, val modFiles: LocalizationFile) {
    companion object : ModelBuilder<Localization> {
        override fun from(file: File): Localization {
            val filter = FileFilter { it.extension == "csv" }
            val gameCSVFiles = file.listFiles(filter).orEmpty()

            val gameFiles = mutableMapOf<String, LocalizationData>()
            val modFiles = mutableMapOf<String, LocalizationData>()
            gameCSVFiles.forEach {
                val trueCSV = GameLocation.fileFromMod(it)
                val name = it.nameWithoutExtension

                if (trueCSV.exists()) {
                    modFiles[name] = LocalizationData.from(trueCSV)
                } else {
                    gameFiles[name] = LocalizationData.from(it)
                }
            }

            val modLocalization = GameLocation.modFolder.resolve("localisation")

            val modCSVFiles = modLocalization.listFiles(filter).orEmpty()
            val gameCSVFilenames = gameCSVFiles.map { it.nameWithoutExtension }
            // Don't go over files already visited
            modCSVFiles.filter {
                it.nameWithoutExtension !in gameCSVFilenames
            }.forEach {
                val data = LocalizationData.from(it)
                modFiles[it.nameWithoutExtension] = data
            }

            return Localization(gameFiles, modFiles)
        }
    }
}

/**
 * A model for every file in `localisation/`.
 */
data class LocalizationData(val records: Map<String, LocalizationRecord>) {
    companion object : ModelBuilder<LocalizationData> {
        /**
         * The csv format with ; as a delimiter.
         */
        private val format = CSVFormat.newFormat(';')

        /**
         * Parse a localization file.
         * @param file The [File] to get records of.
         * @return All records indexed by their name.
         */
        private fun parse(file: File): Map<String, LocalizationRecord> {
            require(file.path.endsWith(".csv")) {
                "File must be a .csv file."
            }

            val records = LinkedHashMap<String, LocalizationRecord>(1000)
            val parser = CSVParser(file.reader(), format)

            parser.use { csv ->
                csv.removeComments().forEach { record ->
                    val first = record[0]
                    var count = 0
                    var containsX = false

                    val rest = record.toList().takeWhile {
                        containsX = it.length == 1 && it.toLowerCase() == "x"
                        count++ <= 14 && !containsX
                    }

                    val goodCount = count >= 15

                    val state = when {
                        !(goodCount || containsX) -> LocalizationState.BAD_END // Will cause errors for the game.
                        !goodCount && containsX -> LocalizationState.TOO_SHORT // Can cause errors for specific languages of the game.
                        else -> LocalizationState.UNUSED // Usage validation will be when used, will become OK if so.
                    }

                    records[first] = LocalizationRecord(rest.drop(1), state)
                }
            }

            return records
        }

        /**
         * Read the localisation directory from [GameLocation.gameFolder]
         */
        override fun from(file: File): LocalizationData {
            val records = parse(file)
            return LocalizationData(records)
        }
    }

    /**
     * Get a localization record with [key] for [language].
     */
    operator fun get(key: String, language: LocalizationLanguage) =
        records[key]?.getEntryForLanguage(language)
}

/**
 * A record containing the localization for a specific record.
 *
 * Every record starts off with an [LocalizationState.UNUSED] state until it is proven otherwise.
 */
data class LocalizationRecord(val entries: List<String>, var state: LocalizationState) {
    operator fun get(index: Int) = entries[index]

    fun getEntryForLanguage(language: LocalizationLanguage) = get(language.ordinal)
}

/**
 * State of a [LocalizationRecord].
 */
enum class LocalizationState {
    OK,
    UNUSED,
    TOO_SHORT,
    BAD_END
}

/**
 * Composition of a [LocalizationRecord].
 *
 * Can be used to explain TOO_SHORT warnings, and for selections.
 */
enum class LocalizationLanguage {
    ENGLISH,
    FRENCH,
    GERMAN,
    POLISH,
    SPANISH,
    ITALIAN,
    SWEDISH,
    CZECH,
    HUNGARIAN,
    DUTCH,
    PORTUGUESE,
    RUSSIAN,
    FINNISH
}