package com.example.workertracking.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.workertracking.data.entity.Worker
import org.junit.Rule
import org.junit.Test

class AddWorkerDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val referrer = Worker(id = 1, name = "דוד", phoneNumber = "050-1111111")
    private val workerWithRef = Worker(id = 2, name = "משה", phoneNumber = "050-2222222", referenceId = 1)
    private val workerNoRef = Worker(id = 3, name = "יוסי", phoneNumber = "050-3333333")
    private val allWorkers = listOf(referrer, workerWithRef, workerNoRef)

    private fun setDialog(
        workers: List<Worker> = listOf(workerWithRef, workerNoRef),
        showPaymentType: Boolean = true,
        showHours: Boolean = false,
        eventHours: Double? = 5.0,
        onAddWorker: (Long, Boolean, Double, Double?, Boolean) -> Unit = { _, _, _, _, _ -> },
        onAddWorkerWithHours: ((Long, Double, Double, Double?, Boolean) -> Unit)? = { _, _, _, _, _ -> }
    ) {
        var searchQuery by mutableStateOf("")
        composeTestRule.setContent {
            AddWorkerDialog(
                workers = workers,
                allWorkers = allWorkers,
                searchQuery = searchQuery,
                onSearchQueryChange = { },
                onDismiss = {},
                onAddWorker = onAddWorker,
                showPaymentType = showPaymentType,
                showHours = showHours,
                eventHours = eventHours,
                onAddWorkerWithHours = onAddWorkerWithHours
            )
        }
    }

    @Test
    fun phase1_showsWorkerList_andSearchField() {
        setDialog()
        composeTestRule.onNodeWithText("חפש עובדים").assertIsDisplayed()
        composeTestRule.onNodeWithText("משה").assertIsDisplayed()
        composeTestRule.onNodeWithText("יוסי").assertIsDisplayed()
        // Pay rate field should not be visible in phase 1
        composeTestRule.onNodeWithText("שכר שעתי (ש\"ח)").assertDoesNotExist()
    }

    @Test
    fun selectingWorker_transitionsToPhase2() {
        setDialog()
        composeTestRule.onNodeWithText("משה").performClick()
        // Phase 2: pay rate field visible, search field gone
        composeTestRule.onNodeWithText("שכר שעתי (ש\"ח)").assertIsDisplayed()
        composeTestRule.onNodeWithText("חפש עובדים").assertDoesNotExist()
    }

    @Test
    fun phase2_showsChangeWorkerButton() {
        setDialog()
        composeTestRule.onNodeWithText("יוסי").performClick()
        composeTestRule.onNodeWithContentDescription("שנה עובד").assertIsDisplayed()
    }

    @Test
    fun changeWorkerButton_returnsToPhase1() {
        setDialog()
        composeTestRule.onNodeWithText("משה").performClick()
        composeTestRule.onNodeWithText("שכר שעתי (ש\"ח)").assertIsDisplayed()
        // Click change worker
        composeTestRule.onNodeWithContentDescription("שנה עובד").performClick()
        // Back to phase 1
        composeTestRule.onNodeWithText("חפש עובדים").assertIsDisplayed()
        composeTestRule.onNodeWithText("שכר שעתי (ש\"ח)").assertDoesNotExist()
    }

    @Test
    fun referenceFields_shownOnlyForWorkerWithReferenceId() {
        setDialog()
        // Select worker WITH referenceId
        composeTestRule.onNodeWithText("משה").performClick()
        composeTestRule.onNodeWithText("תשלום לעובד מפנה: דוד").assertIsDisplayed()
    }

    @Test
    fun referenceFields_notShownForWorkerWithoutReferenceId() {
        setDialog()
        composeTestRule.onNodeWithText("יוסי").performClick()
        composeTestRule.onNodeWithText("תשלום לעובד מפנה: דוד").assertDoesNotExist()
        composeTestRule.onNodeWithText("סכום קבוע").assertDoesNotExist()
    }

    @Test
    fun confirmButton_disabledWithoutPayRate() {
        setDialog()
        composeTestRule.onNodeWithText("יוסי").performClick()
        // No pay rate entered - confirm should be disabled
        composeTestRule.onNodeWithText("הוסף").assertIsNotEnabled()
    }

    @Test
    fun confirmButton_enabledWithValidPayRate() {
        setDialog()
        composeTestRule.onNodeWithText("יוסי").performClick()
        composeTestRule.onNodeWithText("שכר שעתי (ש\"ח)").performClick()
        composeTestRule.onNodeWithText("שכר שעתי (ש\"ח)").performTextInput("50")
        composeTestRule.onNodeWithText("הוסף").assertIsEnabled()
    }

    @Test
    fun confirmButton_disabledWhenReferencePayRateMissing() {
        setDialog()
        // Select worker with referenceId
        composeTestRule.onNodeWithText("משה").performClick()
        // Enter worker pay rate but NOT reference pay rate
        composeTestRule.onNodeWithText("שכר שעתי (ש\"ח)").performClick()
        composeTestRule.onNodeWithText("שכר שעתי (ש\"ח)").performTextInput("50")
        composeTestRule.onNodeWithText("הוסף").assertIsNotEnabled()
    }

    @Test
    fun confirmButton_enabledWhenBothPayRatesFilled() {
        setDialog()
        composeTestRule.onNodeWithText("משה").performClick()
        composeTestRule.onNodeWithText("שכר שעתי (ש\"ח)").performClick()
        composeTestRule.onNodeWithText("שכר שעתי (ש\"ח)").performTextInput("50")
        composeTestRule.onNodeWithText("שכר שעתי לעובד מפנה (ש\"ח)").performClick()
        composeTestRule.onNodeWithText("שכר שעתי לעובד מפנה (ש\"ח)").performTextInput("10")
        composeTestRule.onNodeWithText("הוסף").assertIsEnabled()
    }

    @Test
    fun switchingWorker_resetsReferencePayRate() {
        setDialog()
        // Select worker with ref, fill in reference pay
        composeTestRule.onNodeWithText("משה").performClick()
        composeTestRule.onNodeWithText("שכר שעתי לעובד מפנה (ש\"ח)").performClick()
        composeTestRule.onNodeWithText("שכר שעתי לעובד מפנה (ש\"ח)").performTextInput("10")
        // Switch worker
        composeTestRule.onNodeWithContentDescription("שנה עובד").performClick()
        // Select worker without ref
        composeTestRule.onNodeWithText("יוסי").performClick()
        // Reference fields should not appear
        composeTestRule.onNodeWithText("תשלום לעובד מפנה: דוד").assertDoesNotExist()
    }
}
