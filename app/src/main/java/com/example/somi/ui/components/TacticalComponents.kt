package com.example.somi.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.example.somi.theme.TacticalWarningOrange
import com.example.somi.theme.TextMuted
import com.example.somi.theme.TextPrimary
import com.example.somi.theme.TextSecondary

@Composable
fun TacticalHeaderBadge(
    title: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ArmyGreenPrimary.copy(alpha = 0.25f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, ArmyGreenPrimary.copy(alpha = 0.6f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(ArmyGold)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = ArmyGreenLight,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
fun TacticalStatusBanner(
    status: SimulationStatus,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val config = when (status) {
        SimulationStatus.IDLE -> StatusBannerConfig(
            bgColor = ArmyCardElevated,
            borderColor = ArmyBorderBright,
            textColor = ArmyGold,
            title = "IN ATTESA",
            description = "Genera uno scenario per avviare l'addestramento"
        )
        SimulationStatus.READY -> StatusBannerConfig(
            bgColor = ArmyCardElevated,
            borderColor = ArmyGold,
            textColor = ArmyGold,
            title = "SCENARIO PRONTO",
            description = "Premi 'START SCENARIO' per avviare il countdown degli interventi"
        )
        SimulationStatus.RUNNING -> StatusBannerConfig(
            bgColor = TacticalRedContainer.copy(alpha = 0.4f),
            borderColor = TacticalWarningOrange,
            textColor = TacticalWarningOrange,
            title = "SIMULAZIONE IN CORSO",
            description = "Esegui gli interventi TCCC prima dello scadere dei timer!"
        )
        SimulationStatus.SAVED -> StatusBannerConfig(
            bgColor = TacticalSuccessContainer,
            borderColor = TacticalSuccessBright,
            textColor = TacticalSuccessBright,
            title = "PAZIENTE SALVATO",
            description = "Interventi completati con successo! Tocca per visualizzare il report."
        )
        SimulationStatus.DEAD_BLEEDING -> StatusBannerConfig(
            bgColor = TacticalRedContainer,
            borderColor = TacticalRedBright,
            textColor = TacticalRedBright,
            title = "PAZIENTE MORTO DISSANGUATO",
            description = "Il tempo per l'arresto emorragia è scaduto (00:00). Tocca per il report."
        )
        SimulationStatus.DEAD_SUFFOCATED -> StatusBannerConfig(
            bgColor = TacticalRedContainer,
            borderColor = TacticalRedBright,
            textColor = TacticalRedBright,
            title = "PAZIENTE MORTO SOFFOCATO",
            description = "Il tempo per le vie aeree è scaduto (00:00). Tocca per il report."
        )
        SimulationStatus.DEAD_BOTH -> StatusBannerConfig(
            bgColor = TacticalRedContainer,
            borderColor = TacticalRedBright,
            textColor = TacticalRedBright,
            title = "PAZIENTE DECEDUTO",
            description = "Decesso combinato (00:00). Tocca per visualizzare il report."
        )
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = config.bgColor),
        border = BorderStroke(2.dp, config.borderColor),
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "STATUS CLINICO-OPERATIVO",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = config.title,
                style = MaterialTheme.typography.headlineMedium,
                color = config.textColor,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = config.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary.copy(alpha = 0.9f)
            )
        }
    }
}

private data class StatusBannerConfig(
    val bgColor: Color,
    val borderColor: Color,
    val textColor: Color,
    val title: String,
    val description: String
)

@Composable
fun TacticalTimerCard(
    timerTitle: String,
    timerSubtitle: String,
    timeFormatted: String,
    progress: Float,
    isStopped: Boolean,
    checkboxLabel: String,
    checkboxChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    timerColor: Color,
    simulationActive: Boolean,
    modifier: Modifier = Modifier
) {
    val progressColor = when {
        isStopped -> TacticalSuccessGreen
        progress < 0.15f -> TacticalRed
        progress < 0.40f -> TacticalWarningOrange
        else -> timerColor
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ArmyCardBg),
        border = BorderStroke(1.dp, if (isStopped) TacticalSuccessBright.copy(alpha = 0.7f) else ArmyBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = timerTitle.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = timerSubtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }

                // Display Timer Digits (Garantita larghezza e non-wrapping)
                Surface(
                    color = if (isStopped) TacticalSuccessContainer else ArmyCardElevated,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isStopped) TacticalSuccessBright else progressColor.copy(alpha = 0.6f)
                    )
                ) {
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.displayMedium,
                        color = if (isStopped) TacticalSuccessBright else progressColor,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Barra di progresso
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = progressColor,
                trackColor = ArmyBorder.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Checkbox Interattiva
            Surface(
                color = if (checkboxChecked) TacticalSuccessContainer.copy(alpha = 0.6f) else ArmyCardElevated,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(
                    1.dp,
                    if (checkboxChecked) TacticalSuccessBright else ArmyBorderBright
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCheckedChange(!checkboxChecked) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Checkbox(
                        checked = checkboxChecked,
                        onCheckedChange = null,
                        colors = CheckboxDefaults.colors(
                            checkedColor = TacticalSuccessBright,
                            uncheckedColor = ArmyBorderBright,
                            checkmarkColor = Color.Black
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = checkboxLabel,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (checkboxChecked) TacticalSuccessBright else TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (checkboxChecked) "Timer bloccato (Intervento effettuato)" else "Spunta per bloccare il timer",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (checkboxChecked) TacticalSuccessBright.copy(alpha = 0.8f) else TextMuted
                        )
                    }
                }
            }
        }
    }
}
