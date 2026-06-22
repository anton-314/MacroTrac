package dev.antonlammers.macrotrac.data.backup

import dev.antonlammers.macrotrac.fake.FakeCustomFoodRepository
import dev.antonlammers.macrotrac.fake.FakeFoodEntryRepository
import dev.antonlammers.macrotrac.fake.FakeGoalRepository
import dev.antonlammers.macrotrac.fake.FakeWeightRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class BackupImporterTest {

    @Test
    fun `detectCsvType returns FOOD_ENTRIES when food_name column is present`() {
        val headers = CsvFormat.parseHeaders(CsvColumns.HEADER)
        assertEquals(CsvType.FOOD_ENTRIES, detectCsvType(headers))
    }

    @Test
    fun `detectCsvType returns WEIGHT_ENTRIES when weight_kg column is present`() {
        val headers = CsvFormat.parseHeaders(WeightCsvFormat.HEADER)
        assertEquals(CsvType.WEIGHT_ENTRIES, detectCsvType(headers))
    }

    @Test
    fun `detectCsvType returns DAILY_GOAL when kcal is present but no food_name or weight_kg`() {
        val headers = CsvFormat.parseHeaders(GoalCsvFormat.HEADER)
        assertEquals(CsvType.DAILY_GOAL, detectCsvType(headers))
    }

    @Test
    fun `detectCsvType returns CUSTOM_FOODS when kcal_per_100g column is present`() {
        val headers = CsvFormat.parseHeaders(CustomFoodCsvFormat.HEADER)
        assertEquals(CsvType.CUSTOM_FOODS, detectCsvType(headers))
    }

    @Test
    fun `detectCsvType returns UNKNOWN for unrecognised headers`() {
        val headers = CsvFormat.parseHeaders("foo,bar,baz")
        assertEquals(CsvType.UNKNOWN, detectCsvType(headers))
    }

    @Test
    fun `detectCsvType returns FOOD_ENTRIES for old export without salt_g`() {
        val oldHeader = listOf(
            CsvColumns.DATE, CsvColumns.FOOD_NAME, CsvColumns.BRAND, CsvColumns.AMOUNT_GRAMS,
            CsvColumns.KCAL, CsvColumns.PROTEIN_G, CsvColumns.CARBS_G, CsvColumns.FAT_G,
            CsvColumns.SUGAR_G, CsvColumns.FIBER_G, CsvColumns.MEAL_CATEGORY, CsvColumns.TIMESTAMP_MS,
        ).joinToString(",")
        assertEquals(CsvType.FOOD_ENTRIES, detectCsvType(CsvFormat.parseHeaders(oldHeader)))
    }

    @Test
    fun `legacy ZIP backup without salt and target columns imports with defaults`() = runTest {
        val food = FakeFoodEntryRepository()
        val weight = FakeWeightRepository()
        val goal = FakeGoalRepository()
        val custom = FakeCustomFoodRepository()

        // Headers as an older app version (pre-salt food/custom, pre-target goal) would have written.
        val zip = zipOf(
            "food_entries.csv" to """
                date,food_name,brand,amount_grams,kcal,protein_g,carbs_g,fat_g,sugar_g,fiber_g,meal_category,timestamp_ms
                2026-06-01,Apfel,,150,80,0.5,21,0.3,18,2,SNACK,1717200000000
            """.trimIndent(),
            "weight_entries.csv" to """
                date,weight_kg,timestamp_ms
                2026-06-01,80.5,1717200000000
            """.trimIndent(),
            "daily_goal.csv" to """
                kcal,protein_g,carbs_g,fat_g
                2000,150,250,70
            """.trimIndent(),
            "custom_foods.csv" to """
                name,brand,kcal_per_100g,protein_per_100g,carbs_per_100g,fat_per_100g,sugar_per_100g,fiber_per_100g
                Magerquark,,67,12,4,0.3,4,0
            """.trimIndent(),
        )

        val result = importZipEntries(ByteArrayInputStream(zip), food, weight, goal, custom)

        assertEquals(1, result.foodImported)
        assertEquals(0, result.foodSkipped)
        assertEquals(1, result.weightImported)
        assertTrue(result.goalRestored)
        assertEquals(1, result.customFoodsImported)

        // Columns absent in the old backup default cleanly — no parse failure, no schema mismatch.
        assertEquals(0.0, food.allEntries().first().saltG, 0.001)
        assertNull(goal.goal().first().targetWeightKg)
        assertEquals(0.0, custom.allFoods().first().first().saltPer100g, 0.001)
        assertEquals(80.5, weight.allEntries().first().weightKg, 0.001)
    }

    @Test
    fun `legacy single daily_goal CSV without target column restores goal with null target`() = runTest {
        val goal = FakeGoalRepository()

        val result = importCsvLines(
            listOf("kcal,protein_g,carbs_g,fat_g", "1800,140,200,60"),
            FakeFoodEntryRepository(), FakeWeightRepository(), goal, FakeCustomFoodRepository(),
        )

        assertTrue(result.goalRestored)
        val saved = goal.goal().first()
        assertEquals(1800.0, saved.kcal, 0.001)
        assertNull(saved.targetWeightKg)
    }

    private fun zipOf(vararg entries: Pair<String, String>): ByteArray {
        val bytes = ByteArrayOutputStream()
        ZipOutputStream(bytes).use { zip ->
            entries.forEach { (name, content) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(content.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
        }
        return bytes.toByteArray()
    }
}
