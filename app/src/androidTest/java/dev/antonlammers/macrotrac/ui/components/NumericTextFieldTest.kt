package dev.antonlammers.macrotrac.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.TextRange
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Verifies the app-wide numeric-field design language: a pre-filled value is fully selected when
 * the field gains focus, so the next keystroke replaces it instead of appending.
 */
class NumericTextFieldTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun selectsWholeValueWhenFocused() {
        rule.setContent {
            var value by remember { mutableStateOf("100") }
            NumericTextField(value = value, onValueChange = { value = it }, label = "Menge")
        }

        rule.onNodeWithText("100").performClick()

        rule.onNodeWithText("100").assert(
            SemanticsMatcher.expectValue(SemanticsProperties.TextSelectionRange, TextRange(0, 3)),
        )
    }

    @Test
    fun typingAfterFocusReplacesPrefilledValue() {
        var latest = "100"
        rule.setContent {
            var value by remember { mutableStateOf("100") }
            NumericTextField(
                value = value,
                onValueChange = { value = it; latest = it },
                label = "Menge",
            )
        }

        rule.onNodeWithText("100").performClick()
        rule.onNodeWithText("100").performTextInput("5")

        rule.runOnIdle { assertEquals("5", latest) }
    }
}
