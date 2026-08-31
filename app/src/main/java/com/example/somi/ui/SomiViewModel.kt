package com.example.somi.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.somi.model.AirwayState
import com.example.somi.model.ArticularLocation
import com.example.somi.model.ConsciousnessState
import com.example.somi.model.ExitWoundState
import com.example.somi.model.JunctionalLocation
import com.example.somi.model.PnxState
import com.example.somi.model.ScenarioData
import com.example.somi.model.SimulationStatus
import com.example.somi.model.TorsoLocation
import com.example.somi.model.VitalSigns
import com.example.somi.model.WoundType
import com.example.somi.update.UpdateDownloadState
import com.example.somi.update.UpdateInfo
import com.example.somi.update.UpdateManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.random.Random

data class SomiUiState(
    val scenario: ScenarioData? = null,
    val simulationStatus: SimulationStatus = SimulationStatus.IDLE,
    val massiveBleedingSecondsRemaining: Int = TOTAL_BLEEDING_SECONDS,
    val airwaySecondsRemaining: Int = TOTAL_AIRWAY_SECONDS,
    val pnxSecondsRemaining: Int = TOTAL_PNX_SECONDS,
    val isMassiveBleedingStopped: Boolean = false,
    val isAirwaySecured: Boolean = false,
    val isPnxTreated: Boolean = false,
    val showDebriefDialog: Boolean = false,
    val updateInfo: UpdateInfo = UpdateInfo(),
    val downloadState: UpdateDownloadState = UpdateDownloadState.Idle,
    val showUpdateDialog: Boolean = false,
    val isCheckingUpdate: Boolean = false,
    val downloadedApkFile: File? = null
) {
    val totalBleedingSeconds: Int get() = TOTAL_BLEEDING_SECONDS
    val totalAirwaySeconds: Int get() = TOTAL_AIRWAY_SECONDS
    val totalPnxSeconds: Int get() = TOTAL_PNX_SECONDS

    val bleedingProgress: Float
        get() = massiveBleedingSecondsRemaining.toFloat() / TOTAL_BLEEDING_SECONDS.toFloat()

    val airwayProgress: Float
        get() = airwaySecondsRemaining.toFloat() / TOTAL_AIRWAY_SECONDS.toFloat()

    val pnxProgress: Float
        get() = pnxSecondsRemaining.toFloat() / TOTAL_PNX_SECONDS.toFloat()

    val formattedBleedingTimer: String
        get() = formatTime(massiveBleedingSecondsRemaining)

    val formattedAirwayTimer: String
        get() = formatTime(airwaySecondsRemaining)

    val formattedPnxTimer: String
        get() = formatTime(pnxSecondsRemaining)

    val isTorsoScenario: Boolean
        get() = scenario?.woundType == WoundType.TORSO

    companion object {
        const val TOTAL_BLEEDING_SECONDS = 5 * 60  // 5 minuti (300 sec)
        const val TOTAL_AIRWAY_SECONDS = 7 * 60    // 7 minuti (420 sec)
        const val TOTAL_PNX_SECONDS = 10 * 60      // 10 minuti (600 sec)

        fun formatTime(totalSeconds: Int): String {
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }
    }
}

class SomiViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SomiUiState())
    val uiState: StateFlow<SomiUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        // Genera scenario di default all'avvio
        generateScenario()
        // Controllo automatico aggiornamenti in background
        checkForUpdates(manual = false)
    }

    /**
     * Controlla la presenza di nuovi rilasci su GitHub Releases
     */
    fun checkForUpdates(manual: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingUpdate = true) }
            val info = UpdateManager.checkLatestRelease(UpdateManager.CURRENT_VERSION_NAME)
            _uiState.update {
                it.copy(
                    isCheckingUpdate = false,
                    updateInfo = info,
                    showUpdateDialog = if (manual) true else info.isUpdateAvailable
                )
            }
        }
    }

    /**
     * Avvia il download dell'APK da GitHub Releases
     */
    fun startApkDownload(context: Context) {
        val downloadUrl = _uiState.value.updateInfo.downloadUrl
        if (downloadUrl.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(downloadState = UpdateDownloadState.Downloading(0f)) }
            val file = UpdateManager.downloadApk(
                context = context,
                downloadUrl = downloadUrl,
                onProgress = { progress ->
                    _uiState.update { it.copy(downloadState = UpdateDownloadState.Downloading(progress)) }
                }
            )

            if (file != null && file.exists()) {
                _uiState.update {
                    it.copy(
                        downloadState = UpdateDownloadState.ReadyToInstall,
                        downloadedApkFile = file
                    )
                }
                // Avvia subito l'installazione
                UpdateManager.installApk(context, file)
            } else {
                _uiState.update {
                    it.copy(downloadState = UpdateDownloadState.Error("Impossibile scaricare il pacchetto APK."))
                }
            }
        }
    }

    /**
     * Installa l'APK già scaricato
     */
    fun installDownloadedApk(context: Context) {
        _uiState.value.downloadedApkFile?.let { file ->
            UpdateManager.installApk(context, file)
        }
    }

    fun dismissUpdateDialog() {
        _uiState.update { it.copy(showUpdateDialog = false) }
    }

    fun openUpdateDialog() {
        _uiState.update { it.copy(showUpdateDialog = true) }
    }

    /**
     * Genera un nuovo scenario clinico-tattico:
     * - 20% Giunzionale, 40% Arto, 40% Torso
     * - Foro di uscita con probabilità 80%
     * - Stato di coscienza: Alert, Verbal, Pain, Unresponsive
     * - Modulo A: probabilità vie aeree ostruite 15%
     * - Se Torso: PNX Presente (100%)
     * - Parametri Vitali: battiti 60-100, pressione 80/120 (120/80), atti respiratori 15, saturazione 95-99%
     */
    fun generateScenario() {
        timerJob?.cancel()

        val rand = Random.nextFloat()
        val woundType = when {
            rand < 0.20f -> WoundType.GIUNZIONALE // 20%
            rand < 0.60f -> WoundType.ARTO        // 40%
            else -> WoundType.TORSO              // 40%
        }

        val woundLocationName = when (woundType) {
            WoundType.ARTO -> {
                val locations = ArticularLocation.entries
                locations[Random.nextInt(locations.size)].displayName
            }
            WoundType.GIUNZIONALE -> {
                val locations = JunctionalLocation.entries
                locations[Random.nextInt(locations.size)].displayName
            }
            WoundType.TORSO -> {
                val locations = TorsoLocation.entries
                locations[Random.nextInt(locations.size)].displayName
            }
        }

        // 80% probabilità foro di uscita presente
        val exitWound = if (Random.nextFloat() < 0.80f) {
            ExitWoundState.PRESENTE
        } else {
            ExitWoundState.ASSENTE
        }

        // Stato di coscienza casuale tra Alert, Verbal, Pain, Unresponsive
        val consciousnessValues = ConsciousnessState.entries
        val consciousness = consciousnessValues[Random.nextInt(consciousnessValues.size)]

        // 15% probabilità vie aeree ostruite
        val airway = if (Random.nextFloat() < 0.15f) {
            AirwayState.OSTRUITE
        } else {
            AirwayState.PERVIE
        }

        // Se penetra il torso -> PNX presente
        val pnxState = if (woundType == WoundType.TORSO) {
            PnxState.PRESENTE
        } else {
            PnxState.ASSENTE
        }

        // Parametri Vitali: battiti 60-100, pressione 80/120, atti 15, saturazione 95-99%
        val heartRate = Random.nextInt(60, 101)
        val systolicBp = Random.nextInt(118, 125)
        val diastolicBp = Random.nextInt(78, 85)
        val respiratoryRate = 15
        val oxygenSaturation = Random.nextInt(95, 100)

        val vitalSigns = VitalSigns(
            heartRate = heartRate,
            systolicBp = systolicBp,
            diastolicBp = diastolicBp,
            respiratoryRate = respiratoryRate,
            oxygenSaturation = oxygenSaturation
        )

        val newScenario = ScenarioData(
            woundType = woundType,
            woundLocationName = woundLocationName,
            exitWound = exitWound,
            consciousness = consciousness,
            airwayState = airway,
            pnxState = pnxState,
            vitalSigns = vitalSigns
        )

        _uiState.update {
            SomiUiState(
                scenario = newScenario,
                simulationStatus = SimulationStatus.READY,
                massiveBleedingSecondsRemaining = SomiUiState.TOTAL_BLEEDING_SECONDS,
                airwaySecondsRemaining = SomiUiState.TOTAL_AIRWAY_SECONDS,
                pnxSecondsRemaining = SomiUiState.TOTAL_PNX_SECONDS,
                isMassiveBleedingStopped = false,
                isAirwaySecured = false,
                isPnxTreated = false,
                showDebriefDialog = false,
                updateInfo = it.updateInfo,
                downloadState = it.downloadState,
                showUpdateDialog = false,
                isCheckingUpdate = it.isCheckingUpdate,
                downloadedApkFile = it.downloadedApkFile
            )
        }
    }

    /**
     * Avvia il conteggio dei timer dello scenario
     */
    fun startScenario() {
        if (_uiState.value.simulationStatus == SimulationStatus.RUNNING) return
        if (_uiState.value.scenario == null) {
            generateScenario()
        }

        _uiState.update { it.copy(simulationStatus = SimulationStatus.RUNNING, showDebriefDialog = false) }
        startTimerTicker()
    }

    private fun startTimerTicker() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                val currentState = _uiState.value

                if (currentState.simulationStatus != SimulationStatus.RUNNING) {
                    break
                }

                val isTorso = currentState.isTorsoScenario
                var bleedingSeconds = currentState.massiveBleedingSecondsRemaining
                var airwaySeconds = currentState.airwaySecondsRemaining
                var pnxSeconds = currentState.pnxSecondsRemaining

                if (isTorso) {
                    // Scenario TORSO: Timer Vie Aeree (7 min) + Timer PNX (10 min)
                    if (!currentState.isAirwaySecured && airwaySeconds > 0) {
                        airwaySeconds -= 1
                    }
                    if (!currentState.isPnxTreated && pnxSeconds > 0) {
                        pnxSeconds -= 1
                    }

                    val isAirwayDead = airwaySeconds <= 0
                    val isPnxDead = pnxSeconds <= 0

                    val newStatus = when {
                        isAirwayDead && isPnxDead -> SimulationStatus.DEAD_MULTIPLE
                        isAirwayDead -> SimulationStatus.DEAD_SUFFOCATED
                        isPnxDead -> SimulationStatus.DEAD_PNX
                        currentState.isAirwaySecured && currentState.isPnxTreated -> SimulationStatus.SAVED
                        else -> SimulationStatus.RUNNING
                    }

                    val hasEnded = newStatus != SimulationStatus.RUNNING

                    _uiState.update {
                        it.copy(
                            airwaySecondsRemaining = airwaySeconds,
                            pnxSecondsRemaining = pnxSeconds,
                            simulationStatus = newStatus,
                            showDebriefDialog = if (hasEnded) true else it.showDebriefDialog
                        )
                    }

                    if (hasEnded) break

                } else {
                    // Scenario ARTO o GIUNZIONALE: Timer Emorragia Massiva (5 min) + Timer Vie Aeree (7 min)
                    if (!currentState.isMassiveBleedingStopped && bleedingSeconds > 0) {
                        bleedingSeconds -= 1
                    }
                    if (!currentState.isAirwaySecured && airwaySeconds > 0) {
                        airwaySeconds -= 1
                    }

                    val isBleedingDead = bleedingSeconds <= 0
                    val isAirwayDead = airwaySeconds <= 0

                    val newStatus = when {
                        isBleedingDead && isAirwayDead -> SimulationStatus.DEAD_MULTIPLE
                        isBleedingDead -> SimulationStatus.DEAD_BLEEDING
                        isAirwayDead -> SimulationStatus.DEAD_SUFFOCATED
                        currentState.isMassiveBleedingStopped && currentState.isAirwaySecured -> SimulationStatus.SAVED
                        else -> SimulationStatus.RUNNING
                    }

                    val hasEnded = newStatus != SimulationStatus.RUNNING

                    _uiState.update {
                        it.copy(
                            massiveBleedingSecondsRemaining = bleedingSeconds,
                            airwaySecondsRemaining = airwaySeconds,
                            simulationStatus = newStatus,
                            showDebriefDialog = if (hasEnded) true else it.showDebriefDialog
                        )
                    }

                    if (hasEnded) break
                }
            }
        }
    }

    /**
     * Checkbox Stop emorragia massiva (Arto / Giunzionale)
     */
    fun toggleStopMassiveBleeding(stopped: Boolean) {
        _uiState.update { current ->
            val updated = current.copy(isMassiveBleedingStopped = stopped)
            evaluateStatusOnCheckboxChange(updated)
        }
    }

    /**
     * Checkbox Vie aeree pervie (Tutti gli scenari)
     */
    fun toggleAirwaySecured(secured: Boolean) {
        _uiState.update { current ->
            val updated = current.copy(isAirwaySecured = secured)
            evaluateStatusOnCheckboxChange(updated)
        }
    }

    /**
     * Checkbox Trattamento PNX / Decompressione (Torso)
     */
    fun togglePnxTreated(treated: Boolean) {
        _uiState.update { current ->
            val updated = current.copy(isPnxTreated = treated)
            evaluateStatusOnCheckboxChange(updated)
        }
    }

    private fun evaluateStatusOnCheckboxChange(state: SomiUiState): SomiUiState {
        // Se il paziente è già morto, non si modifica lo stato
        if (state.simulationStatus in listOf(
            SimulationStatus.DEAD_BLEEDING,
            SimulationStatus.DEAD_SUFFOCATED,
            SimulationStatus.DEAD_PNX,
            SimulationStatus.DEAD_MULTIPLE
        )) {
            return state
        }

        // Se non è mai stato avviato
        if (state.simulationStatus == SimulationStatus.READY || state.simulationStatus == SimulationStatus.IDLE) {
            return state
        }

        val isSaved = if (state.isTorsoScenario) {
            state.isAirwaySecured && state.isPnxTreated
        } else {
            state.isMassiveBleedingStopped && state.isAirwaySecured
        }

        return if (isSaved) {
            timerJob?.cancel()
            state.copy(simulationStatus = SimulationStatus.SAVED, showDebriefDialog = true)
        } else {
            // Se prima era salvato e viene tolta una spunta, riprende la simulazione
            if (state.simulationStatus == SimulationStatus.SAVED) {
                val resumeState = state.copy(simulationStatus = SimulationStatus.RUNNING, showDebriefDialog = false)
                startTimerTicker()
                resumeState
            } else {
                state
            }
        }
    }

    fun dismissDebriefDialog() {
        _uiState.update { it.copy(showDebriefDialog = false) }
    }

    fun openDebriefDialog() {
        if (_uiState.value.simulationStatus in listOf(
            SimulationStatus.SAVED,
            SimulationStatus.DEAD_BLEEDING,
            SimulationStatus.DEAD_SUFFOCATED,
            SimulationStatus.DEAD_PNX,
            SimulationStatus.DEAD_MULTIPLE
        )) {
            _uiState.update { it.copy(showDebriefDialog = true) }
        }
    }

    /**
     * Reset completo dello scenario corrente
     */
    fun resetScenario() {
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                simulationStatus = SimulationStatus.READY,
                massiveBleedingSecondsRemaining = SomiUiState.TOTAL_BLEEDING_SECONDS,
                airwaySecondsRemaining = SomiUiState.TOTAL_AIRWAY_SECONDS,
                pnxSecondsRemaining = SomiUiState.TOTAL_PNX_SECONDS,
                isMassiveBleedingStopped = false,
                isAirwaySecured = false,
                isPnxTreated = false,
                showDebriefDialog = false
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
