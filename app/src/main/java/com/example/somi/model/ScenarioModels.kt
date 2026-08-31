package com.example.somi.model

enum class WoundType(val displayName: String) {
    ARTO("Arto"),
    GIUNZIONALE("Giunzionale"),
    TORSO("Torso")
}

enum class ArticularLocation(val displayName: String) {
    BRACCIO_DX("Braccio dx"),
    BRACCIO_SX("Braccio sx"),
    GAMBA_DX("Gamba dx"),
    GAMBA_SX("Gamba sx")
}

enum class JunctionalLocation(val displayName: String) {
    SPALLA_SX("Spalla sx"),
    SPALLA_DX("Spalla dx"),
    COLLO_SX("Collo sx"),
    COLLO_DX("Collo dx"),
    BACINO_SX("Bacino sx"),
    BACINO_DX("Bacino dx")
}

enum class TorsoLocation(val displayName: String) {
    TORACE_ANT_SX("Torace anteriore sx"),
    TORACE_ANT_DX("Torace anteriore dx"),
    TORACE_POST_SX("Torace posteriore sx"),
    TORACE_POST_DX("Torace posteriore dx")
}

enum class ConsciousnessState(val displayName: String, val avpuLetter: String, val description: String) {
    ALERT("Alert", "A", "Paziente sveglio, cosciente e orientato"),
    VERBAL("Verbal", "V", "Risponde solo a stimoli verbali"),
    PAIN("Pain", "P", "Risponde solo a stimoli dolorosi"),
    UNRESPONSIVE("Unresponsive", "U", "Nessuna risposta agli stimoli (incosciente)")
}

enum class AirwayState(val displayName: String, val isObstructed: Boolean) {
    PERVIE("Pervie", false),
    OSTRUITE("Ostruite", true)
}

enum class ExitWoundState(val displayName: String, val isPresent: Boolean) {
    PRESENTE("Presente", true),
    ASSENTE("Assente", false)
}

enum class PnxState(val displayName: String, val isPresent: Boolean) {
    PRESENTE("Presente (Pneumotorace)", true),
    ASSENTE("Assente", false)
}

data class VitalSigns(
    val heartRate: Int,        // 60 - 100 bpm
    val systolicBp: Int,       // 120 mmHg
    val diastolicBp: Int,      // 80 mmHg (80/120)
    val respiratoryRate: Int,  // 15 atti/min
    val oxygenSaturation: Int  // 95 - 99%
) {
    val formattedBp: String get() = "$systolicBp/$diastolicBp mmHg"
    val formattedHr: String get() = "$heartRate bpm"
    val formattedRr: String get() = "$respiratoryRate atti/min"
    val formattedSpo2: String get() = "$oxygenSaturation%"
}

data class ScenarioData(
    val woundType: WoundType,
    val woundLocationName: String,
    val exitWound: ExitWoundState,
    val consciousness: ConsciousnessState,
    val airwayState: AirwayState,
    val pnxState: PnxState,
    val vitalSigns: VitalSigns,
    val id: String = System.currentTimeMillis().toString().takeLast(6)
)

enum class SimulationStatus {
    IDLE,              // Nessuno scenario generato
    READY,             // Scenario generato, pronto per l'avvio
    RUNNING,           // Simulazione in corso
    SAVED,             // Paziente salvato
    DEAD_BLEEDING,     // Morto dissanguato (timer emorragia = 0)
    DEAD_SUFFOCATED,   // Morto soffocato (timer vie aeree = 0)
    DEAD_PNX,          // Morto per PNX iperteso (timer PNX = 0)
    DEAD_MULTIPLE      // Morto per più cause contemporanee
}
