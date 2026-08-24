package com.example.somi.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.somi.theme.ArmyBorder
import com.example.somi.theme.ArmyBorderBright
import com.example.somi.theme.ArmyCardBg
import com.example.somi.theme.ArmyCardElevated
import com.example.somi.theme.ArmyGold
import com.example.somi.theme.ArmyGoldContainer
import com.example.somi.theme.ArmyGreenLight
import com.example.somi.theme.TacticalRedBright
import com.example.somi.theme.TacticalSuccessBright
import com.example.somi.theme.TacticalSuccessContainer
import com.example.somi.theme.TextMuted
import com.example.somi.theme.TextPrimary
import com.example.somi.theme.TextSecondary
import com.example.somi.update.UpdateDownloadState
import com.example.somi.update.UpdateInfo

@Composable
fun TacticalUpdateDialog(
    updateInfo: UpdateInfo,
    downloadState: UpdateDownloadState,
    onStartDownload: () -> Unit,
    onInstallNow: () -> Unit,
    onDismiss: () -> Unit
) {
    val sizeMb = if (updateInfo.apkSize > 0) " (%.1f MB)".format(updateInfo.apkSize / (1024.0 * 1024.0)) else ""

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = downloadState !is UpdateDownloadState.Downloading)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ArmyCardBg),
            border = BorderStroke(2.dp, ArmyGold),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                // Header Pop-up
                Surface(
                    color = ArmyGoldContainer,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, ArmyGold.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "AGGIORNAMENTO SOFTWARE TCCC",
                            style = MaterialTheme.typography.labelSmall,
                            color = ArmyGreenLight,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "NUOVA VERSIONE ${updateInfo.latestVersion}",
                            style = MaterialTheme.typography.titleLarge,
                            color = ArmyGold,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Versione installata: v${updateInfo.currentVersion}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Note di Rilascio
                Text(
                    text = "NOTE DI RILASCIO (CHANGELOG):",
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
                        text = if (updateInfo.releaseNotes.isNotBlank()) updateInfo.releaseNotes else "Miglioramenti generali e ottimizzazioni del protocollo addestrativo SOMI.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        modifier = Modifier.padding(12.dp),
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stato di Download
                when (downloadState) {
                    is UpdateDownloadState.Idle -> {
                        // Nessuna azione in corso
                    }
                    is UpdateDownloadState.Downloading -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Download aggiornamento...", style = MaterialTheme.typography.labelSmall, color = ArmyGold)
                                Text("${(downloadState.progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = ArmyGold, fontFamily = FontFamily.Monospace)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { downloadState.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = ArmyGold,
                                trackColor = ArmyBorder
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                    is UpdateDownloadState.ReadyToInstall -> {
                        Surface(
                            color = TacticalSuccessContainer,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, TacticalSuccessBright),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Download completato! Pronto per l'installazione.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TacticalSuccessBright,
                                modifier = Modifier.padding(10.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                    is UpdateDownloadState.Error -> {
                        Surface(
                            color = Color(0xFF3D1515),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, TacticalRedBright),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Errore durante il download: ${downloadState.message}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TacticalRedBright,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }

                // Pulsanti
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, ArmyBorderBright),
                        shape = RoundedCornerShape(6.dp),
                        enabled = downloadState !is UpdateDownloadState.Downloading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "DOPO",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                    }

                    when (downloadState) {
                        is UpdateDownloadState.ReadyToInstall -> {
                            Button(
                                onClick = onInstallNow,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TacticalSuccessBright,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1.5f)
                            ) {
                                Text(
                                    text = "INSTALLA ORA",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        is UpdateDownloadState.Downloading -> {
                            Button(
                                onClick = {},
                                enabled = false,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1.5f)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = ArmyGold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("SCARICAMENTO...", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                        else -> {
                            Button(
                                onClick = onStartDownload,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ArmyGold,
                                    contentColor = Color(0xFF161200)
                                ),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1.5f)
                            ) {
                                Text(
                                    text = "SCARICA$sizeMb",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
