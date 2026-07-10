# MacroMind — Trainings-Erweiterung: Anforderungsspezifikation

> **Zweck dieses Dokuments.** Verbindliche Referenz für den Umbau von MacroMind vom reinen
> Kalorien-Tracker zur kombinierten Ernährungs- & Trainings-App. Dieses Dokument wird bei der
> Entwicklung **jedes** Features als Kontext mitgegeben, damit der Überblick über das Gesamtziel
> erhalten bleibt. Es ergänzt `CLAUDE.md` (Architektur & Konventionen) — dort festgehaltene Regeln
> gelten unverändert weiter.
>
> **Status:** 🟡 In Ausarbeitung (v0.1). Offene Punkte sind in Abschnitt 10 gesammelt.

---

## 1. Leitprinzipien (nicht verhandelbar)

1. **Designsprache „Ink & Paper" bleibt exakt erhalten.** Jede neue Fläche verwendet dieselben
   Tokens, Formen, Typo- und Motion-Regeln wie der Bestand (siehe `CLAUDE.md` → „Key design
   decisions"). Keine neuen Farben außer den bereits definierten Daten-Farben; kein Material-You;
   Serif für Titel/Zahlen, Sans für Body, Mono für Mikro-Labels; flache Karten mit Hairline-Border.
   Neue Bausteine werden aus den bestehenden abgeleitet (`NumericTextField`, Bottom-Sheets,
   FilterChips, `MonoTab`, flache Karten).
2. **Domain bleibt Android-frei.** Trainingslogik (Volumen, 1RM, PR-Erkennung, Aggregation) lebt in
   reinem Kotlin unter `domain/` und ist per JVM-Unit-Test erreichbar — analog zu `WeightSeries`,
   `MacroCalculator`, `CsvFormat`.
3. **Sauber isoliertes Trainings-Modul.** Neue `workout`-Subdomäne, die parallel zur Ernährung
   steht und nur an definierten Punkten andockt (Körpergewicht, Datum). Keine Vermischung von
   Ernährungs- und Trainings-Code.
4. **Volle Datenportabilität, rückwärtskompatibel.** Alle Ernährungs- **und** Trainingsdaten
   bleiben vollständig ex-/importierbar. Backups aus älteren Versionen **und** aus App-Versionen
   ganz ohne Trainingsdaten müssen weiter fehlerfrei importieren (siehe Abschnitt 8).
5. **FOSS-konform.** Nur Daten/Assets mit klarer, kompatibler Lizenz werden gebündelt.
6. **Test- & Workflow-Disziplin** wie in `CLAUDE.md`: plan → implement → dead code entfernen →
   validieren → Tests → `assembleDebug` + `testDebugUnitTest` → Doku → Commit.
7. **Keine Abhängigkeit von externen Quellen.** Der Übungskatalog wird als versionierter
   Offline-Snapshot gebündelt (keine laufende Fremd-API zur Laufzeit). Verschwindet die Quelle,
   bleibt die App voll funktionsfähig. Der Katalog liegt hinter einer austauschbaren Abstraktion,
   sodass Quelle/Update ohne Eingriff in Feature-Code wechselbar sind.

---

## 2. Navigation & Gesamtstruktur

Die Bottom-Navigation bekommt **vier** Bereiche (bisher drei). „Ziele" verlässt die Leiste, da im
Alltag selten gebraucht, und wandert in einen Einstellungs-/Profil-Hub.

| Position | Bereich | Route | Inhalt |
|---|---|---|---|
| 1 | **Ernährung** (bisher „Übersicht"/„Heute") | `overview` | Tages-Essenslog, Kalorienring, Makrobalken — **unverändert** |
| 2 | **Training** | `workout` | Trainings-Startpunkt: Vorlagen, aktive Session, Zugang zur Historie |
| 3 | **Statistik** | `stats` | Auswertungen **beider** Welten + spätere Korrelations-Insights |
| 4 | **Einstellungen** | `settings` | Ziele, Backup Export/Import, Erinnerungs-Toggle, Zielgewicht |

**Konsequenzen:**
- `GoalsScreen` raus aus `bottomNavItems`, erreichbar über den Einstellungs-Hub.
- Die „Daten"-Sektion (Backup + Reminder-Toggle), die aktuell auf `StatsScreen` sitzt, wandert
  mit nach Einstellungen. → **Statistik wird reine Insight-Fläche**, Einstellungen = alles
  Konfigurierbare an einem Ort.
- Jeder Haupt-Screen hat seinen eigenen kontextuellen FAB (Ernährung → „Essen hinzufügen",
  Training → „Workout starten").
- Icons aus `Icons.Rounded.*` (aktiv) / `Icons.Outlined.*` (inaktiv), passend zum Bestand:
  Ernährung → `Home`, Training → `FitnessCenter`, Statistik → `BarChart`, Einstellungen →
  `Settings`.
- **Einheiten:** kg-only (App ist durchgängig kg-basiert); lb ggf. später als globale Einstellung.

---

## 3. Funktionsumfang Training

### 3.1 Übungs-Datenbank ✅ Quelle entschieden
- Bibliothek vordefinierter Übungen + Möglichkeit, **eigene Übungen** anzulegen/zu bearbeiten.
- **Quelle: `free-exercise-db` (yuhonas), gebündelter Offline-Snapshot.** ~800 Übungen als JSON,
  **Public Domain** (keine Namensnennung/Share-Alike-Pflicht → maximale Unabhängigkeit, siehe
  Leitprinzip §1.7). Englische Namen sind akzeptiert; Custom-Übungen erlauben eigene (deutsche)
  Namen.
- **Kein Runtime-API-Zugriff.** Das JSON wird als versioniertes App-Asset ausgeliefert und beim
  ersten Start (bzw. bei Snapshot-Versionswechsel) einmalig in Room importiert („seeding"). Danach
  ist der Katalog eine ganz normale lokale Tabelle; Custom-Übungen liegen gleichberechtigt daneben.
- **Austauschbarkeit:** Zugriff nur über ein `ExerciseCatalog`-Repository-Interface. Der konkrete
  Seed/Parse liegt in `data/` und ist ersetzbar, ohne Feature-Code zu berühren. Der Snapshot trägt
  eine Versionsnummer, damit spätere Updates idempotent nachgezogen werden können, ohne
  Custom-Übungen zu überschreiben.
- Übungs-Metadaten (aus dem Datensatz): Name, Muskelgruppe(n) (`primaryMuscles`/`secondaryMuscles`),
  Equipment, Mechanik (compound/isolation), Kategorie, Anleitung; Bilder optional (siehe F12).
- **Animationen:** keine saubere FOSS-Quelle → nicht vorgesehen.

> **Offen (F12):** Bilder des Datensatzes in v1 bündeln (APK-Größe ~viele MB durch ~1600 Bilder),
> später als optionalen Download, oder v1 ohne Bilder?

### 3.2 Vorlagen (Templates / Routinen) ✅ Inhalt entschieden
- Nutzer erstellt, benennt und speichert Workouts im Voraus (z. B. „Push Day").
- **Eine Vorlage = geordnete Liste von Übungen, je mit geplanter Ziel-Satzanzahl.** Gewicht und
  Reps sind **nicht** Teil der Vorlage — sie kommen beim Training und werden über die
  Inline-Historie (§3.3) aus dem letzten Mal vorbelegt.
- Vorlagen sind Ausgangspunkt für eine Live-Session.

### 3.3 Live-Session (Protokollierung)
- Aus Vorlage **oder** leer gestartet.
- **Übungstypen (entschieden):** Gewicht × Reps **und** Körpergewichts-Übungen (Reps, mit
  optionalem Zusatzgewicht). Zeit-basiert (Halten) und Cardio/Distanz sind **nicht** Teil von v1,
  aber das Datenmodell wird so gewählt, dass sie additiv ergänzbar bleiben.
- Echtzeit-Eingabe von **Gewicht** und **Wiederholungen** pro Satz; Satz „abhaken".
- **Satz-Typen:** Warm-up, Normal, Drop-Satz, Failure (Kennzeichnung pro Satz).
- **Inline-Historie:** grau hinterlegte Werte des letzten Trainings derselben Übung als
  Platzhalter/Orientierung im Eingabefeld.
- **Ruhe-Timer (entschieden):** startet automatisch nach dem Abhaken eines Satzes. **Default 90 s,
  pro Übung überschreibbar.** Läuft im **Hintergrund** weiter (App zu/Handy weggelegt) und meldet
  Ablauf per **Notification + Ton/Vibration**. Nutzt WorkManager/Notification-Infrastruktur analog
  zum bestehenden `notification/`-Paket (eigener Kanal).
- **Supersätze:** in v1 **nicht** unterstützt. Datenmodell aber so wählen, dass eine spätere
  Gruppierung additiv möglich ist (z. B. optionale `supersetGroupId` an `SessionExercise`).
- Session-Ende: speichert die Einheit in die Historie, berechnet Volumen/1RM, prüft PRs.

### 3.4 Berechnungen ✅ entschieden
- **Volumen** je Übung/Session = Σ (Gewicht × Reps). **Warm-up-Sätze zählen nicht ins Volumen.**
  Bei Körpergewichts-Übungen geht das (optionale) Zusatzgewicht ein; ohne Zusatzgewicht trägt der
  Satz kein Volumen bei (Reps werden trotzdem protokolliert).
- **Geschätztes 1RM** je Satz = **Epley**: `Gewicht × (1 + Reps/30)`. Wird als Info angezeigt,
  ist aber **kein** PR-Auslöser.

### 3.5 Persönliche Rekorde (PRs) ✅ entschieden
- **PR = neues Höchstgewicht pro Übung** (über alle Nicht-Warm-up-Sätze). Ein neuer PR löst ein
  visuelles Highlight (Pokal-Badge im monochromen Stil) aus — live in der Session **und** in der
  Historie/Auswertung markiert. Volumen- und 1RM-Rekorde sind bewusst **nicht** Teil von v1
  (später additiv ergänzbar).

### 3.6 Historie ✅ entschieden
- **Monatskalender** mit Markierung an Trainingstagen (Punkt/Akzent). Tippen auf einen Tag zeigt
  die Einheit(en) dieses Tages.
- Einheiten **editierbar** (Sätze/Gewicht/Reps/Typ nachträglich korrigieren, Einheit löschbar).

### 3.7 Auswertung (Statistik-Tab)
- Charts für **Kraftsteigerung** (pro Übung, z. B. 1RM-Verlauf), **Trainingsfrequenz**,
  **Körpergewicht** (existiert bereits — Weight-Chart).
- Canvas-basiert im Stil der bestehenden Charts (kein Chart-Library-Dependency).

> **Offen:** Welche Übungen/Metriken standardmäßig? Übungsauswahl für den Kraft-Chart? (§10)

---

## 4. Ernährung ↔ Training Korrelation (Ausblick) ✅ Phase entschieden
Der eigentliche Mehrwert der Zusammenführung. Lebt im Statistik-Tab. **Entschieden: spätere Phase**
— erst ein vollständiges, solides Trainings-MVP, dann die Korrelations-Insights obendrauf (Phase 10
in §9). Konkrete Korrelationen werden zu Beginn dieser Phase spezifiziert.

---

## 5. Datenmodell (Entwurf, wird verfeinert)
Neue Room-Entities (Arbeitsstand, Android-frei gespiegelt in `domain/model`):
- `Exercise` — Übungsdefinition (Katalog + Custom).
- `WorkoutTemplate` + `TemplateExercise` — Vorlage und ihre Übungen (geordnet).
- `WorkoutSession` — absolvierte/aktive Einheit (Datum, Dauer, Notiz).
- `SessionExercise` + `SetEntry` — Übungen einer Session und ihre Sätze (Gewicht, Reps, Typ,
  abgehakt).

> **Offen:** finale Felder nach Klärung der offenen Fragen. DB-Version steigt (aktuell 7 → 8+),
> reine additive Migrationen mit Defaults.

---

## 6. Designsprache — konkrete Anwendung
Alle in §1.1 genannten Regeln gelten. Neue, trainings-spezifische UI leitet sich ab:
- Set-Zeilen, Timer, PR-Badges, Kalender im „Ink & Paper"-Look (flache Karten, Hairline, Serif-
  Zahlen, Mono-Mikrolabels).
- Daten-Farben wiederverwenden; keine neuen Akzentfarben einführen.

> **Offen:** Mapping der Satz-Typen auf bestehende Farbtoken; PR-Pokal-Darstellung im monochromen
> System. (§10)

---

## 7. Teststrategie
- Reine Rechenlogik (Volumen, 1RM, PR-Erkennung, Aggregation) → JVM-Unit-Tests unter
  `app/src/test/` mit Fakes (kein Mocking-Framework), turbine für Flows.
- Neue Repositories bekommen Fakes analog zu den bestehenden.
- Compose-UI-Verhalten (z. B. Timer, Set-Eingabe) → Instrumented-Tests unter `androidTest/`.

---

## 8. Datenportabilität & Rückwärtskompatibilität (Pflicht)
Erweiterung des bestehenden CSV/ZIP-Backups (siehe `CLAUDE.md` → „Backup schema evolution"):
- Neue Datentypen = neue `*CsvFormat`-Objekte + eigene ZIP-Einträge (z. B. `exercises.csv`,
  `workout_templates.csv`, `workout_sessions.csv`, `set_entries.csv`).
- `detectCsvType` + Import-Dispatch um die neuen Typen erweitern.
- **Namensbasiertes Parsen** beibehalten → alte Backups ohne Trainingsdaten importieren weiter
  fehlerfrei (fehlende ZIP-Einträge = keine Trainingsdaten, kein Fehler).
- Export enthält immer alle vorhandenen Datentypen; fehlende Sektionen werden beim Import
  toleriert.
- End-to-End-Unit-Tests inkl. Legacy-Backups (wie `BackupImporterTest`).

---

## 9. Phasierung (grob, wird in Dokument 2 zu Prompts)
Reihenfolge so, dass nach jedem Schritt etwas Lauffähiges/Testbares entsteht. Feinschnitt erfolgt,
sobald die offenen Fragen geklärt sind. Grobe Blöcke:
1. Navigations-Umbau (4 Tabs, Ziele→Einstellungen, Daten-Sektion verschieben).
2. Trainings-Datenmodell + Repositories + Room-Migration + Fakes/Tests.
3. Übungs-Datenbank (Import/Bundle + Custom-Übungen).
4. Vorlagen-Verwaltung.
5. Live-Session (Eingabe, Satz-Typen, Inline-Historie, Timer).
6. Berechnungen (Volumen, 1RM) + PRs.
7. Historie/Kalender + Editieren.
8. Statistik-Charts (Kraft, Frequenz).
9. Backup-Erweiterung + Kompatibilitätstests.
10. (Optional/spätere Phase) Korrelations-Insights.

---

## 10. Offene Fragen (zu klären, bevor die Prompt-Kette steht)
Nummeriert; werden nach Klärung in die jeweiligen Abschnitte eingearbeitet und hier abgehakt.

- [x] **F1 — Übungs-DB-Quelle & Umfang:** ✅ `free-exercise-db`, gebündelter Offline-Snapshot,
  Public Domain, hinter `ExerciseCatalog`-Abstraktion (§1.7, §3.1). Bilder → siehe F12.
- [x] **F2 — Übungstypen:** ✅ Gewicht×Reps **+** Körpergewicht (opt. Zusatzgewicht). Zeit/Cardio
  nicht in v1, aber additiv erweiterbar modelliert.
- [x] **F3 — Supersätze** ✅ in v1 weglassen (Modell additiv erweiterbar via `supersetGroupId`).
- [x] **F4 — Korrelations-Feature:** ✅ spätere Phase (Phase 10).
- [x] **F5 — Vorlagen-Inhalt:** ✅ Übungen + Ziel-Satzanzahl (kein Ziel-Gewicht/-Reps).
- [x] **F6 — PR-Definition:** ✅ nur Max-Gewicht pro Übung; live + in Historie markiert.
- [x] **F7 — Ruhe-Timer:** ✅ Default 90 s, pro Übung überschreibbar, Hintergrund + Notification +
  Ton/Vibration.
- [x] **F8 — 1RM-Formel:** ✅ Epley; Warm-up-Sätze zählen nicht ins Volumen.
- [x] **F9 — Historie-Darstellung:** ✅ Monatskalender + Tages-Detail, editierbar.
- [x] **F10 — Namen/Icons:** ✅ „Einstellungen"; Training=`FitnessCenter`, Einstellungen=`Settings`.
- [x] **F11 — Einheiten:** ✅ kg-only.
- [x] **F12 — Übungsbilder:** ✅ v1 ohne Bilder (Text-Katalog); später nachrüstbar.

**Alle offenen Fragen geklärt → Spec bereit für die Prompt-Kette (Schritt 2).**

---

## Änderungshistorie
- **v0.1** — Erststruktur aus grober Vision; offene Fragen gesammelt.
- **v0.2** — Fragerunden 1 & 2 eingearbeitet; F1–F12 geklärt (Übungs-DB, Übungstypen, Vorlagen,
  Korrelation-Phase, PRs, Timer, Supersätze, 1RM, Historie, Icons/Namen, Einheiten, Bilder). Spec
  vollständig für die Prompt-Kette.
