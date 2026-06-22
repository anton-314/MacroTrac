package dev.antonlammers.macrotrac.data.backup

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.antonlammers.macrotrac.domain.repository.CustomFoodRepository
import dev.antonlammers.macrotrac.domain.repository.FoodEntryRepository
import dev.antonlammers.macrotrac.domain.repository.GoalRepository
import dev.antonlammers.macrotrac.domain.repository.WeightRepository
import java.io.InputStream
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

internal enum class CsvType {
    FOOD_ENTRIES, WEIGHT_ENTRIES, DAILY_GOAL, CUSTOM_FOODS, UNKNOWN
}

internal fun detectCsvType(headers: Map<String, Int>): CsvType = when {
    CsvColumns.FOOD_NAME in headers -> CsvType.FOOD_ENTRIES
    "weight_kg" in headers -> CsvType.WEIGHT_ENTRIES
    "kcal_per_100g" in headers -> CsvType.CUSTOM_FOODS
    "kcal" in headers -> CsvType.DAILY_GOAL
    else -> CsvType.UNKNOWN
}

@Singleton
class BackupImporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val foodEntryRepository: FoodEntryRepository,
    private val weightRepository: WeightRepository,
    private val goalRepository: GoalRepository,
    private val customFoodRepository: CustomFoodRepository,
) {
    data class Result(
        val foodImported: Int = 0,
        val foodSkipped: Int = 0,
        val weightImported: Int = 0,
        val goalRestored: Boolean = false,
        val customFoodsImported: Int = 0,
    )

    suspend fun import(uri: Uri): Result =
        if (isZip(uri)) {
            context.contentResolver.openInputStream(uri)?.use { input ->
                importZipEntries(input, foodEntryRepository, weightRepository, goalRepository, customFoodRepository)
            } ?: Result()
        } else {
            val lines = context.contentResolver.openInputStream(uri)
                ?.bufferedReader(Charsets.UTF_8)
                ?.readLines()
                ?.filter { it.isNotBlank() }
                ?: return Result()
            importCsvLines(lines, foodEntryRepository, weightRepository, goalRepository, customFoodRepository)
        }

    private fun isZip(uri: Uri): Boolean =
        context.contentResolver.openInputStream(uri)?.use { stream ->
            val header = ByteArray(2)
            stream.read(header) == 2 && header[0] == 0x50.toByte() && header[1] == 0x4B.toByte()
        } ?: false
}

/**
 * Reads a backup ZIP, dispatching each known entry to its CSV parser and repository.
 * Pure with respect to Android (takes a plain [InputStream]) so it is directly unit-testable.
 * Unknown or missing entries are ignored, so backups from older app versions still import.
 */
internal suspend fun importZipEntries(
    input: InputStream,
    foodEntryRepository: FoodEntryRepository,
    weightRepository: WeightRepository,
    goalRepository: GoalRepository,
    customFoodRepository: CustomFoodRepository,
): BackupImporter.Result {
    var foodImported = 0
    var foodSkipped = 0
    var weightImported = 0
    var goalRestored = false
    var customFoodsImported = 0

    ZipInputStream(input).use { zip ->
        var entry = zip.nextEntry
        while (entry != null) {
            val content = zip.readBytes().toString(Charsets.UTF_8)
            val lines = content.lines().filter { it.isNotBlank() }
            when (entry.name) {
                "food_entries.csv" -> if (lines.size > 1) {
                    val headers = CsvFormat.parseHeaders(lines.first())
                    lines.drop(1).forEach { line ->
                        val e = runCatching { CsvFormat.fromRow(line, headers) }.getOrNull()
                        if (e != null) { foodEntryRepository.add(e); foodImported++ }
                        else foodSkipped++
                    }
                }
                "weight_entries.csv" -> if (lines.size > 1) {
                    val headers = CsvFormat.parseHeaders(lines.first())
                    lines.drop(1).forEach { line ->
                        val e = runCatching { WeightCsvFormat.fromRow(line, headers) }.getOrNull()
                        if (e != null) { weightRepository.save(e); weightImported++ }
                    }
                }
                "daily_goal.csv" -> if (lines.size > 1) {
                    val headers = CsvFormat.parseHeaders(lines.first())
                    val goal = runCatching { GoalCsvFormat.fromRow(lines[1], headers) }.getOrNull()
                    if (goal != null) { goalRepository.save(goal); goalRestored = true }
                }
                "custom_foods.csv" -> if (lines.size > 1) {
                    val headers = CsvFormat.parseHeaders(lines.first())
                    lines.drop(1).forEach { line ->
                        val f = runCatching { CustomFoodCsvFormat.fromRow(line, headers) }.getOrNull()
                        if (f != null) { customFoodRepository.save(f); customFoodsImported++ }
                    }
                }
            }
            zip.closeEntry()
            entry = zip.nextEntry
        }
    }

    return BackupImporter.Result(foodImported, foodSkipped, weightImported, goalRestored, customFoodsImported)
}

/**
 * Imports a single CSV (already split into non-blank lines), routing by detected type.
 * Pure with respect to Android so it is directly unit-testable.
 */
internal suspend fun importCsvLines(
    lines: List<String>,
    foodEntryRepository: FoodEntryRepository,
    weightRepository: WeightRepository,
    goalRepository: GoalRepository,
    customFoodRepository: CustomFoodRepository,
): BackupImporter.Result {
    if (lines.size < 2) return BackupImporter.Result()

    val headers = CsvFormat.parseHeaders(lines.first())
    return when (detectCsvType(headers)) {
        CsvType.FOOD_ENTRIES -> {
            var imported = 0; var skipped = 0
            lines.drop(1).forEach { line ->
                val e = runCatching { CsvFormat.fromRow(line, headers) }.getOrNull()
                if (e != null) { foodEntryRepository.add(e); imported++ } else skipped++
            }
            BackupImporter.Result(foodImported = imported, foodSkipped = skipped)
        }
        CsvType.WEIGHT_ENTRIES -> {
            var imported = 0
            lines.drop(1).forEach { line ->
                val e = runCatching { WeightCsvFormat.fromRow(line, headers) }.getOrNull()
                if (e != null) { weightRepository.save(e); imported++ }
            }
            BackupImporter.Result(weightImported = imported)
        }
        CsvType.DAILY_GOAL -> {
            val goal = runCatching { GoalCsvFormat.fromRow(lines[1], headers) }.getOrNull()
            if (goal != null) { goalRepository.save(goal); BackupImporter.Result(goalRestored = true) }
            else BackupImporter.Result()
        }
        CsvType.CUSTOM_FOODS -> {
            var imported = 0
            lines.drop(1).forEach { line ->
                val f = runCatching { CustomFoodCsvFormat.fromRow(line, headers) }.getOrNull()
                if (f != null) { customFoodRepository.save(f); imported++ }
            }
            BackupImporter.Result(customFoodsImported = imported)
        }
        CsvType.UNKNOWN -> BackupImporter.Result()
    }
}
