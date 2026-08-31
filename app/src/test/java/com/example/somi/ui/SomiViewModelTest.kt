package com.example.somi.ui

import com.example.somi.model.ArticularLocation
import com.example.somi.model.ConsciousnessState
import com.example.somi.model.JunctionalLocation
import com.example.somi.model.PnxState
import com.example.somi.model.SimulationStatus
import com.example.somi.model.TorsoLocation
import com.example.somi.model.WoundType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SomiViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `generateScenario creates valid scenario with valid fields and vital signs`() {
        val viewModel = SomiViewModel()
        val state = viewModel.uiState.value

        assertNotNull(state.scenario)
        assertEquals(SimulationStatus.READY, state.simulationStatus)
        assertEquals(300, state.massiveBleedingSecondsRemaining)
        assertEquals(420, state.airwaySecondsRemaining) // 7 minuti
        assertEquals(600, state.pnxSecondsRemaining)    // 10 minuti
        assertFalse(state.isMassiveBleedingStopped)
        assertFalse(state.isAirwaySecured)
        assertFalse(state.isPnxTreated)
        assertFalse(state.showDebriefDialog)

        val scenario = state.scenario!!
        when (scenario.woundType) {
            WoundType.ARTO -> {
                val validArticularNames = ArticularLocation.entries.map { it.displayName }
                assertTrue(validArticularNames.contains(scenario.woundLocationName))
                assertEquals(PnxState.ASSENTE, scenario.pnxState)
            }
            WoundType.GIUNZIONALE -> {
                val validJunctionalNames = JunctionalLocation.entries.map { it.displayName }
                assertTrue(validJunctionalNames.contains(scenario.woundLocationName))
                assertEquals(PnxState.ASSENTE, scenario.pnxState)
            }
            WoundType.TORSO -> {
                val validTorsoNames = TorsoLocation.entries.map { it.displayName }
                assertTrue(validTorsoNames.contains(scenario.woundLocationName))
                assertEquals(PnxState.PRESENTE, scenario.pnxState)
            }
        }

        assertTrue(ConsciousnessState.entries.contains(scenario.consciousness))

        // Verifica Parametri Vitali
        assertTrue(scenario.vitalSigns.heartRate in 60..100)
        assertEquals(15, scenario.vitalSigns.respiratoryRate)
        assertTrue(scenario.vitalSigns.oxygenSaturation in 95..99)
    }

    @Test
    fun `startScenario updates status to RUNNING`() {
        val viewModel = SomiViewModel()
        viewModel.startScenario()

        val state = viewModel.uiState.value
        assertEquals(SimulationStatus.RUNNING, state.simulationStatus)
        assertFalse(state.showDebriefDialog)
    }

    @Test
    fun `saving scenario works appropriately based on wound type`() {
        val viewModel = SomiViewModel()
        viewModel.startScenario()

        val isTorso = viewModel.uiState.value.isTorsoScenario

        if (isTorso) {
            viewModel.toggleAirwaySecured(true)
            viewModel.togglePnxTreated(true)
        } else {
            viewModel.toggleStopMassiveBleeding(true)
            viewModel.toggleAirwaySecured(true)
        }

        val state = viewModel.uiState.value
        assertEquals(SimulationStatus.SAVED, state.simulationStatus)
        assertTrue(state.showDebriefDialog)

        viewModel.dismissDebriefDialog()
        assertFalse(viewModel.uiState.value.showDebriefDialog)

        viewModel.openDebriefDialog()
        assertTrue(viewModel.uiState.value.showDebriefDialog)
    }

    @Test
    fun `unchecking a checkbox when SAVED resumes RUNNING status and closes debrief dialog`() {
        val viewModel = SomiViewModel()
        viewModel.startScenario()

        val isTorso = viewModel.uiState.value.isTorsoScenario

        if (isTorso) {
            viewModel.toggleAirwaySecured(true)
            viewModel.togglePnxTreated(true)
            assertEquals(SimulationStatus.SAVED, viewModel.uiState.value.simulationStatus)

            viewModel.togglePnxTreated(false)
            assertEquals(SimulationStatus.RUNNING, viewModel.uiState.value.simulationStatus)
        } else {
            viewModel.toggleStopMassiveBleeding(true)
            viewModel.toggleAirwaySecured(true)
            assertEquals(SimulationStatus.SAVED, viewModel.uiState.value.simulationStatus)

            viewModel.toggleAirwaySecured(false)
            assertEquals(SimulationStatus.RUNNING, viewModel.uiState.value.simulationStatus)
        }

        assertFalse(viewModel.uiState.value.showDebriefDialog)
    }

    @Test
    fun `resetScenario resets timers, checkboxes, and closes dialog`() {
        val viewModel = SomiViewModel()
        viewModel.startScenario()
        viewModel.toggleStopMassiveBleeding(true)
        viewModel.toggleAirwaySecured(true)
        viewModel.togglePnxTreated(true)

        viewModel.resetScenario()
        val state = viewModel.uiState.value

        assertEquals(SimulationStatus.READY, state.simulationStatus)
        assertEquals(300, state.massiveBleedingSecondsRemaining)
        assertEquals(420, state.airwaySecondsRemaining)
        assertEquals(600, state.pnxSecondsRemaining)
        assertFalse(state.isMassiveBleedingStopped)
        assertFalse(state.isAirwaySecured)
        assertFalse(state.isPnxTreated)
        assertFalse(state.showDebriefDialog)
    }
}
