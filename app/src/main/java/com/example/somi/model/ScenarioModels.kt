package com.example.somi.model

enum class WoundType(val displayName: String) {
    ARTICOLARE("Articolare"),
    GIUNZIONALE("Giunzionale")
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

data class ScenarioData(
    val woundType: WoundType,
    val woundLocationName: String,
    val exitWound: ExitWoundState,
    val consciousness: ConsciousnessState,
    val airwayState: AirwayState,
    val id: String = System.currentTimeMillis().toString().takeLast(6)
)

enum class SimulationStatus {
    IDLE,              // Nessuno scenario generato
    READY,             // Scenario generato, pronto per l'avvio
    RUNNING,           // Simulazione in corso
    SAVED,             // Paziente salvato (entrambe le checkbox spuntate)
    DEAD_BLEEDING,     // Morto dissanguato (timer emorragia = 0)
    DEAD_SUFFOCATED,   // Morto soffocato (timer vie aeree = 0)
    DEAD_BOTH          // Morto per entrambe le cause
}
