package com.example.somi.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.somi.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.somi.model.AirwayState
import com.example.somi.model.ExitWoundState
import com.example.somi.model.ScenarioData
import com.example.somi.model.SimulationStatus
import com.example.somi.model.WoundType
import com.example.somi.theme.ArmyBorder
import com.example.somi.theme.ArmyBorderBright
import com.example.somi.theme.ArmyCardBg
import com.example.somi.theme.ArmyCardElevated
import com.example.somi.theme.ArmyDarkBg
import com.example.somi.theme.ArmyGold
import com.example.somi.theme.ArmyGreenLight
import com.example.somi.theme.ArmyGreenPrimary
import com.example.somi.theme.TacticalAirwayBlue
import com.example.somi.theme.TacticalRed
import com.example.somi.theme.TacticalRedBright
import com.example.somi.theme.TacticalSuccessBright
import com.example.somi.theme.TacticalWarningOrange
import com.example.somi.theme.TextMuted
import com.example.somi.theme.TextPrimary
import com.example.somi.theme.TextSecondary
import com.example.somi.ui.components.TacticalDebriefDialog
import com.example.somi.ui.components.TacticalHeaderBadge
import com.example.somi.ui.components.TacticalStatusBanner
import com.example.somi.ui.components.TacticalTimerCard
import com.example.somi.ui.components.TacticalUpdateDialog

@Composable
fun SomiScreen(
    viewModel: SomiViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // POP-UP / DIALOG DI DEBRIEFING CLINICO-TATTICO (Esito salvataggio o decesso)
    if (state.showDebriefDialog) {
        TacticalDebriefDialog(
            status = state.simulationStatus,
            scenario = state.scenario,
            uiState = state,
            onDismiss = { viewModel.dismissDebriefDialog() },
            onNewScenario = { viewModel.generateScenario() }
        )
    }

    // POP-UP AGGIORNAMENTO SOFTWARE GITHUB RELEASES
    if (state.showUpdateDialog) {
        TacticalUpdateDialog(
            updateInfo = state.updateInfo,
            downloadState = state.downloadState,
            onStartDownload = { viewModel.startApkDownload(context) },
            onInstallNow = { viewModel.installDownloadedApk(context) },
            onDismiss = { viewModel.dismissUpdateDialog() }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ArmyDarkBg)
            .verticalScroll(scrollState)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // TOP MILITARY HEADER
        AppMilitaryHeader(
            isCheckingUpdate = state.isCheckingUpdate,
            isUpdateAvailable = state.updateInfo.isUpdateAvailable,
            onCheckUpdate = { viewModel.checkForUpdates(manual = true) }
        )

        // STATUS OPERATIVO BANNER (Cliccabile per riaprire il debrief)
        TacticalStatusBanner(
            status = state.simulationStatus,
            onClick = { viewModel.openDebriefDialog() }
        )

        // PULSANTI DI CONTROLLO OPERATIVO
        ActionControlBar(
            simulationStatus = state.simulationStatus,
            onGenerateScenario = { viewModel.generateScenario() },
            onStartScenario = { viewModel.startScenario() },
            onResetScenario = { viewModel.resetScenario() }
        )

        // CARD SCENARIO: MODULO M (MASSIVE BLEEDING)
        state.scenario?.let { scenario ->
            ModuloMCard(scenario = scenario)

            // CARD SCENARIO: MODULO A (AIRWAY)
            ModuloACard(scenario = scenario)
        }

        // SEZIONE TIMER & INTERVENTI TCCC
        Surface(
            color = ArmyCardBg,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, ArmyBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(ArmyGold)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "INTERVENTI TCCC & TIMER SOCCORSO",
                    style = MaterialTheme.typography.labelMedium,
                    color = ArmyGold,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }

        // Timer 1: Emorragia Massiva (5 Minuti)
        TacticalTimerCard(
            timerTitle = "Emorragia Massiva",
            timerSubtitle = "Tempo limite: 05:00 (Rischio dissanguamento)",
            timeFormatted = state.formattedBleedingTimer,
            progress = state.bleedingProgress,
            isStopped = state.isMassiveBleedingStopped,
            checkboxLabel = "Stop emorragia massiva",
            checkboxChecked = state.isMassiveBleedingStopped,
            onCheckedChange = { viewModel.toggleStopMassiveBleeding(it) },
            timerColor = TacticalRed,
            simulationActive = state.simulationStatus == SimulationStatus.RUNNING
        )

        // Timer 2: Pervietà delle vie aeree (15 Minuti)
        TacticalTimerCard(
            timerTitle = "Pervietà delle vie aeree",
            timerSubtitle = "Tempo limite: 15:00 (Rischio soffocamento)",
            timeFormatted = state.formattedAirwayTimer,
            progress = state.airwayProgress,
            isStopped = state.isAirwaySecured,
            checkboxLabel = "Vie aeree pervie",
            checkboxChecked = state.isAirwaySecured,
            onCheckedChange = { viewModel.toggleAirwaySecured(it) },
            timerColor = TacticalAirwayBlue,
            simulationActive = state.simulationStatus == SimulationStatus.RUNNING
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun AppMilitaryHeader(
    isCheckingUpdate: Boolean,
    isUpdateAvailable: Boolean,
    onCheckUpdate: () -> Unit
) {
    Surface(
        color = ArmyCardBg,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, ArmyBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                // Tactical insignia
                Image(
                    painter = painterResource(id = R.drawable.ic_somi_logo),
                    contentDescription = "SOMI Emblem",
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "SOMI V 0.1.1",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "SOCCORRITORE MILITARE • E.I.",
                        style = MaterialTheme.typography.labelSmall,
                        color = ArmyGreenLight,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Tactical Update Check Button / Badge
                Surface(
                    color = if (isUpdateAvailable) ArmyGold else ArmyCardElevated,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, if (isUpdateAvailable) ArmyGold else ArmyBorderBright),
                    modifier = Modifier.clickable { onCheckUpdate() }
                ) {
                    Text(
                        text = if (isCheckingUpdate) "SYNC..." else if (isUpdateAvailable) "★ UPDATE" else "v0.1.1",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isUpdateAvailable) Color.Black else ArmyGold,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }

                TacticalHeaderBadge(title = "TCCC")
            }
        }
    }
}

@Composable
private fun ActionControlBar(
    simulationStatus: SimulationStatus,
    onGenerateScenario: () -> Unit,
    onStartScenario: () -> Unit,
    onResetScenario: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Bottone GENERA SCENARIO
            Button(
                onClick = onGenerateScenario,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ArmyGold,
                    contentColor = Color(0xFF161200)
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "GENERA SCENARIO",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Bottone START SCENARIO
            val isRunning = simulationStatus == SimulationStatus.RUNNING
            val isFinished = simulationStatus in listOf(
                SimulationStatus.SAVED,
                SimulationStatus.DEAD_BLEEDING,
                SimulationStatus.DEAD_SUFFOCATED,
                SimulationStatus.DEAD_BOTH
            )

            Button(
                onClick = onStartScenario,
                enabled = !isRunning && !isFinished,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TacticalSuccessBright,
                    contentColor = Color.Black,
                    disabledContainerColor = ArmyCardElevated,
                    disabledContentColor = TextMuted
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (isRunning) "IN CORSO..." else "START SCENARIO",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Bottone Reset (se in corso o terminato)
        if (simulationStatus != SimulationStatus.READY && simulationStatus != SimulationStatus.IDLE) {
            OutlinedButton(
                onClick = onResetScenario,
                border = BorderStroke(1.dp, ArmyBorderBright),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "RESET SIMULAZIONE",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun ModuloMCard(scenario: ScenarioData) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ArmyCardBg),
        border = BorderStroke(1.dp, ArmyBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MODULO M - MASSIVE BLEEDING",
                    style = MaterialTheme.typography.titleMedium,
                    color = TacticalRedBright,
                    fontWeight = FontWeight.Bold
                )
                TacticalHeaderBadge(title = "1 COLPO")
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = ArmyBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Tipologia & Sede ferita
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoField(
                    label = "TIPO FERITA (50%)",
                    value = scenario.woundType.displayName,
                    badgeColor = if (scenario.woundType == WoundType.ARTICOLARE) ArmyGreenPrimary else ArmyGold,
                    modifier = Modifier.weight(1f)
                )

                InfoField(
                    label = "SEDE ANATOMICA",
                    value = scenario.woundLocationName,
                    badgeColor = TacticalRed,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Foro Uscita & Stato Coscienza
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoField(
                    label = "FORO DI USCITA (80%)",
                    value = if (scenario.exitWound == ExitWoundState.PRESENTE) "PRESENTE" else "ASSENTE",
                    badgeColor = if (scenario.exitWound == ExitWoundState.PRESENTE) TacticalRedBright else ArmyGreenPrimary,
                    modifier = Modifier.weight(1f)
                )

                InfoField(
                    label = "STATO COSCIENZA (AVPU)",
                    value = "[${scenario.consciousness.avpuLetter}] ${scenario.consciousness.displayName}",
                    badgeColor = when (scenario.consciousness) {
                        com.example.somi.model.ConsciousnessState.ALERT -> TacticalSuccessBright
                        com.example.somi.model.ConsciousnessState.VERBAL -> ArmyGold
                        com.example.somi.model.ConsciousnessState.PAIN -> TacticalWarningOrange
                        com.example.somi.model.ConsciousnessState.UNRESPONSIVE -> TacticalRedBright
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Dettaglio AVPU: ${scenario.consciousness.description}",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun ModuloACard(scenario: ScenarioData) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ArmyCardBg),
        border = BorderStroke(1.dp, ArmyBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MODULO A - AIRWAY",
                    style = MaterialTheme.typography.titleMedium,
                    color = TacticalAirwayBlue,
                    fontWeight = FontWeight.Bold
                )
                TacticalHeaderBadge(title = "VIE AEREE")
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = ArmyBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            InfoField(
                label = "VALUTAZIONE VIE AEREE (15% OSTRUZIONE)",
                value = if (scenario.airwayState == AirwayState.OSTRUITE) "OSTRUITE (Intervento immediato)" else "PERVIE (Libere)",
                badgeColor = if (scenario.airwayState == AirwayState.OSTRUITE) TacticalRedBright else TacticalSuccessBright,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun InfoField(
    label: String,
    value: String,
    badgeColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ArmyCardElevated,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, ArmyBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 9.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = badgeColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
