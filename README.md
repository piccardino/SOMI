# SOMI V 0.1 - Soccorritore Militare (Esercito Italiano)

![SOMI Banner](app/src/main/res/drawable/ic_somi_logo.png)

Applicazione Android nativa per l'addestramento clinico-operativo delle truppe dell'**Esercito Italiano** secondo le linee guida internazionali **TCCC (Tactical Combat Casualty Care - Algoritmo MARCH)**.

---

## 🎯 Obiettivo Addestrativo

L'applicazione simula scenari realistici su campo per operatori SOMI (Soccorritore Militare) alle prese con il trattamento di ferite penetranti da arma da fuoco (1 colpo) in zone anatomiche critiche.

### Modulo M (Massive Bleeding - Emorragia Massiva)
- **Probabilità Ferita (50% / 50%)**:
  - **Articolare**: *Braccio dx, Braccio sx, Gamba dx, Gamba sx*
  - **Giunzionale**: *Spalla sx, Spalla dx, Collo sx, Collo dx, Bacino sx, Bacino dx*
- **Foro di Uscita**: 80% Presente, 20% Assente.
- **Stato di Coscienza (Scala AVPU)**: *Alert, Verbal, Pain, Unresponsive*.
- **Timer Critico**: 5 minuti (`05:00`). Se l'emorragia non viene bloccata entro il tempo limite $\rightarrow$ **Paziente Morto Dissanguato** (Shock ipovolemico).

### Modulo A (Airway - Vie Aeree)
- **Stato Vie Aeree**: 15% Ostruite, 85% Pervie.
- **Timer Critico**: 15 minuti (`15:00`). Se la pervietà non viene mantenuta $\rightarrow$ **Paziente Morto Soffocato** (Asfissia acuta).

### Esito e Debriefing Tattico
- Se entrambe le manovre salvavita vengono eseguite tempestivamente $\rightarrow$ **PAZIENTE SALVATO** (Idoneo per evacuazione 9-Line MEDEVAC).
- Al termine della simulazione compare automaticamente un **Popup di Debriefing Clinico-Tattico** con il resoconto analitico dell'intervento.

---

## 🔄 Sistema di Aggiornamento In-App (GitHub Releases)

L'applicazione integra un motore autonomo di verifica ed installazione degli aggiornamenti via **GitHub Releases**:
- Verifica all'avvio o manuale tramite il pulsante versione nell'header.
- Download diretto dell'APK con barra di avanzamento percentuale.
- Installazione automatica su dispositivo senza necessità di ADB o cavo USB.

---

## 📦 Installazione e Build

### Prerequisiti
- Android Studio / Android SDK 36 (minSdk 24).
- JDK 17 o superiore.
- Gradle Wrapper incluso.

### Compilazione APK Debug
```bash
./gradlew assembleDebug
```

### Installazione via ADB
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 🚀 CI/CD e Release Automatiche

Il repository include una GitHub Actions pipeline (`.github/workflows/release.yml`) che:
1. Compila automaticamente l'APK a ogni push di tag `v*`.
2. Crea la GitHub Release e allega l'APK pronto per il download e l'auto-aggiornamento in-app.
