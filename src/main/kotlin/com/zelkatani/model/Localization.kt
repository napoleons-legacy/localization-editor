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
 * A map of filename to localization records.
 */
typealias LocalizationFile = Map<String, LocalizationData>

/**
 * A model for the `localisation` folder.
 */
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
            val gameCSVFilenames = gameCSVFiles.map { it.name }

            // Don't go over files already visited
            modCSVFiles.filter {
                it.name !in gameCSVFilenames
            }.forEach {
                val data = LocalizationData.from(it)
                modFiles[it.nameWithoutExtension] = data
            }

            return Localization(gameFiles, modFiles)
        }
    }

    /**
     * Get the set of all filename to data pairings.
     */
    operator fun get(key: String): Set<Pair<String, LocalizationData>> =
        mutableSetOf<Pair<String, LocalizationData>>().apply {
            val addOccurrence: (Map.Entry<String, LocalizationData>) -> Unit = { (file, data) ->
                if (data.records.containsKey(key)) {
                    this += file to data
                }
            }

            modFiles.forEach(addOccurrence)
            gameFiles.forEach(addOccurrence)
        }
}

/**
 * A mapping of identifier -> record, or the rows of a localization file.
 */
typealias LocalizationRows = Map<String, LocalizationRecord>

/**
 * A model for every .csv file in `localisation/`.
 */
// TODO: duplicates should be kept track of.
data class LocalizationData(val records: LocalizationRows) {
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
        private fun parse(file: File): LocalizationRows {
            require(file.path.endsWith(".csv")) {
                "File must be a .csv file."
            }

            val records = LinkedHashMap<String, LocalizationRecord>(1000)
            val parser = CSVParser(file.reader(), format)

            parser.use { csv ->
                csv.removeComments().forEach { entry ->
                    val first = entry[0]
                    var count = 0
                    var containsX = false

                    val rest = entry.toList().takeWhile {
                        containsX = it.length == 1 && it.toLowerCase() == "x"
                        count++ <= 14 && !containsX
                    }

                    val goodCount = count >= 15

                    val state = when {
                        !(goodCount || containsX) -> LocalizationState.BAD_END // Will cause errors for the game.
                        !goodCount && containsX -> LocalizationState.TOO_SHORT // Can cause errors for specific languages of the game.
                        else -> LocalizationState.UNUSED // Usage validation will be when used, will become OK if so.
                    }

                    records[first] = LocalizationRecord(entry.recordNumber.toInt(), rest.drop(1), state)
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
     * Get a localization record with [key] for [language] if [key] is valid.
     */
    operator fun get(key: String, language: LocalizationLanguage = LocalizationLanguage.ENGLISH) =
        records[key]?.get(language)
}

/**
 * A record containing the localization for a specific record.
 *
 * Every record starts off with an [LocalizationState.UNUSED] state until it is proven otherwise.
 */
data class LocalizationRecord(val index: Int, val entries: List<String>, var state: LocalizationState) {
    /**
     * Get a localization entry for the specified [language].
     */
    operator fun get(language: LocalizationLanguage) = entries[language.ordinal]
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