package com.example.somi.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.somi.model.AirwayState
import com.example.somi.model.ExitWoundState
import com.example.somi.model.ScenarioData
import com.example.somi.model.SimulationStatus
import com.example.somi.theme.ArmyBorder
import com.example.somi.theme.ArmyBorderBright
import com.example.somi.theme.ArmyCardBg
import com.example.somi.theme.ArmyCardElevated
import com.example.somi.theme.ArmyGold
import com.example.somi.theme.ArmyGreenLight
import com.example.somi.theme.ArmyGreenPrimary
import com.example.somi.theme.TacticalRed
import com.example.somi.theme.TacticalRedBright
import com.example.somi.theme.TacticalRedContainer
import com.example.somi.theme.TacticalSuccessBright
import com.example.somi.theme.TacticalSuccessContainer
import com.example.somi.theme.TacticalSuccessGreen
import com.example.somi.theme.TextMuted
import com.example.somi.theme.TextPrimary
import com.example.somi.theme.TextSecondary
import com.example.somi.ui.SomiUiState

@Composable
fun TacticalDebriefDialog(
    status: SimulationStatus,
    scenario: ScenarioData?,
    uiState: SomiUiState,
    onDismiss: () -> Unit,
    onNewScenario: () -> Unit
) {
    val isSaved = status == SimulationStatus.SAVED
    val titleColor = if (isSaved) TacticalSuccessBright else TacticalRedBright
    val borderColor = if (isSaved) TacticalSuccessBright else TacticalRed
    val headerBg = if (isSaved) TacticalSuccessContainer else TacticalRedContainer

    val titleText = when (status) {
        SimulationStatus.SAVED -> "PAZIENTE SALVATO"
        SimulationStatus.DEAD_BLEEDING -> "DECESSO PER DISSANGUAMENTO"
        SimulationStatus.DEAD_SUFFOCATED -> "DECESSO PER SOFFOCAMENTO"
        SimulationStatus.DEAD_BOTH -> "DECESSO COMBINATO"
        else -> "DEBRIEFING SCENARIO"
    }

    val narrativeDescription = when (status) {
        SimulationStatus.SAVED ->
            "MISSIONE COMPIUTA: Il soccorritore militare ha eseguito con successo tutte le manovre salvavita previste dal protocollo TCCC prima dello scadere dei timer critici.\n\n" +
            "• Emorragia Massiva: Arrestata con successo (tempo residuo: ${uiState.formattedBleedingTimer}).\n" +
            "• Vie Aeree: Pervietà garantita (tempo residuo: ${uiState.formattedAirwayTimer}).\n\n" +
            "Il ferito è stabilizzato ed idoneo per l'evacuazione sanitaria 9-Line MEDEVAC."

        SimulationStatus.DEAD_BLEEDING ->
            "CRITICITÀ LETALE: Il tempo limite di 5 minuti (05:00) per l'arresto dell'emorragia massiva è scaduto prima del completamento dell'intervento.\n\n" +
            "La perdita ematica incontrollata dalla ferita (${scenario?.woundType?.displayName ?: ""} in sede ${scenario?.woundLocationName ?: ""}) ha innescato uno shock ipovolemico irreversibile e il decesso del militare per dissanguamento sul campo."

        SimulationStatus.DEAD_SUFFOCATED ->
            "CRITICITÀ LETALE: Il tempo limite di 15 minuti (15:00) per il controllo delle vie aeree è scaduto prima che venisse garantita la pervietà.\n\n" +
            "La compromissione respiratoria non risolta ha provocato ipossia acuta grave, acidosi e conseguente arresto cardio-respiratorio irreversibile per soffocamento."

        SimulationStatus.DEAD_BOTH ->
            "CRITICITÀ TOTALE: Entrambi i parametri vitali prioritari TCCC (Emorragia Massiva e Vie Aeree) sono falliti.\n\n" +
            "Il paziente è deceduto per la combinazione letale di shock emorragico catastrofico e asfissia acuta."

        else -> "Simulazione conclusa."
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ArmyCardBg),
            border = BorderStroke(2.dp, borderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                // Header Pop-up
                Surface(
                    color = headerBg,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, borderColor.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "RAPPORTO CLINICO-TATTICO",
                            style = MaterialTheme.typography.labelSmall,
                            color = ArmyGold,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = titleText,
                            style = MaterialTheme.typography.titleLarge,
                            color = titleColor,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sezione: Cosa è Successo
                Text(
                    text = "COSA È SUCCESSO:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = ArmyCardElevated,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, ArmyBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = narrativeDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        modifier = Modifier.padding(12.dp),
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sezione: Dati Scenario
                scenario?.let { sc ->
                    Text(
                        text = "RIASSUNTO SCENARIO TRATTATO:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = ArmyCardElevated,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, ArmyBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Ferita:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text("${sc.woundType.displayName} (${sc.woundLocationName})", style = MaterialTheme.typography.bodySmall, color = ArmyGold, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Foro di Uscita:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(if (sc.exitWound == ExitWoundState.PRESENTE) "Presente (80%)" else "Assente", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Stato Coscienza:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text("[${sc.consciousness.avpuLetter}] ${sc.consciousness.displayName}", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Vie Aeree Iniziali:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(if (sc.airwayState == AirwayState.OSTRUITE) "Ostruite (15%)" else "Pervie (85%)", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pulsanti Azione
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, ArmyBorderBright),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "RIVEDI",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                    }

                    Button(
                        onClick = onNewScenario,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ArmyGold,
                            contentColor = Color(0xFF161200)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Text(
                            text = "NUOVO SCENARIO",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
