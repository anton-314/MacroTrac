# Handoff: MacroTrac Redesign — "Tinte auf Papier" (Ink & Paper)

## Overview
A full visual redesign of the MacroTrac Android app (Kotlin, Jetpack Compose, Material3, repo `anton-314/MacroTrac`). Direction: dark-first, minimalist, editorial/notebook feel — serif display type, warm neutral (monochrome) surfaces, and color used *only* for meaningful data: the macro nutrients and the clean-eating tags. All existing app functionality is preserved 1:1; only visual language and a few motion/interaction details change.

## About the Design Files
The files in this bundle (`*.dc.html`, `*.jsx`) are **design references built in HTML/React for prototyping only** — they are not production code and must not be copied into the Android app as-is. The task is to **recreate this visual design natively in Jetpack Compose**, inside the app's existing architecture (see `CLAUDE.md` in the app repo). Per the app's own documented convention — *"Theme isolation: replacing `ui/theme/Color.kt` and `Theme.kt` is sufficient to restyle the entire app"* — most of this redesign should land in `ui/theme/Color.kt`, `Theme.kt`, and `Type.kt`, plus targeted composable changes noted per-screen below (new icon set, ring/bar motion, chip and card shapes).

## Fidelity
**High-fidelity.** Exact colors, type, spacing, radii, and motion timings are specified below and should be matched closely. Copy strings for German UI text are unchanged from the current app (see original `OverviewScreen.kt` etc.) except where noted.

---

## Design Tokens

### Color — Dark mode (default)
| Token | Hex | Usage |
|---|---|---|
| `paper` (canvas behind content, e.g. status bar / outside cards) | `#0D0D0C` | app chrome / scaffold background |
| `bg` | `#161615` | screen background |
| `surface` | `#1E1E1C` | cards, sheets, nav bar |
| `surface-2` | `#282826` | chart tracks, inset fields, progress-bar tracks |
| `line` (hairline) | `rgba(255,255,255,.08)` | dividers, card borders |
| `text` (primary) | `#EDECE8` | headings, values |
| `text-2` (secondary) | `#9C9A94` | supporting labels |
| `text-3` (tertiary) | `#68665F` | micro-labels, placeholders, inactive nav |
| `accent` | **`#B79FCB`** (pastel lilac) — user-selected, fixed default | primary actions (FAB, filled buttons, "Speichern"), selected tab/chip, active nav icon, links |

### Color — Light mode ("Paper")
Activated automatically when the **device is in light mode** (system light/dark, not a manual in-app toggle — see Theming behavior below).
| Token | Hex |
|---|---|
| `paper` | `#E7DECC` |
| `bg` | `#F5F0E4` |
| `surface` | `#FCF8EF` |
| `surface-2` | `#EDE4D2` |
| `line` | `rgba(40,32,18,.12)` |
| `text` | `#26211A` |
| `text-2` | `#6E6455` |
| `text-3` | `#9A8F7C` |

Accent and all data colors below stay identical in both modes.

### Color — Data (unchanged meaning from the current app, only hex-tuned for the new surfaces)
| Token | Hex | Maps to existing `Color.kt` |
|---|---|---|
| `cal` | `#FFAB76` | `CalorieColor` |
| `protein` | `#82C8F5` | `ProteinColor` |
| `carbs` | `#82D48A` | `CarbsColor` |
| `fat` | `#E8A4D4` | `FatColor` |
| `health` (Gesund) | `#7CC47F` | `TagHealthyColor` |
| `neutral` (Neutral) | `#E3A85A` | `TagNeutralColor` |
| `unhealthy` (Ungesund) | `#E07670` | `TagUnhealthyColor` |

`ink` (`#1B1409`) is the on-accent text/icon color for filled accent buttons/FAB (dark text on the light lilac accent).

### Typography
Three families, Google Fonts, loaded at weights actually used:
- **Serif — `Newsreader`** (400/500/600, italic 400): screen titles, big numbers (kcal, kg), meal-section names, dialog titles. Gives the "notebook/editorial" character.
- **Sans — `Hanken Grotesk`** (400/500/600/700): body text, list items, buttons, field values.
- **Mono — `JetBrains Mono`** (400/500): micro-labels in uppercase with wide letter-spacing (e.g. `PROTEIN (G)`, `KCAL`, tab labels, chart axis labels, chip labels) — this mono-caps treatment is a signature detail of the new system, used consistently for all "meta" labels.

Reference sizes used in the mockups (scale proportionally to Compose `sp`):
- Big ring number: 46px serif / 500
- Screen title: 24–26px serif
- Dialog/section title: 15–20px serif
- Body / list item: 14–15px sans
- Secondary caption: 11–13px sans or mono
- Micro/mono label: 9–12px mono, letter-spacing ~0.1–0.22em, uppercase

### Iconography
**Material Symbols Rounded**, weight 300, 24pt optical size — same icon family the app already effectively uses (Material Icons), just switched to the lighter *Rounded* outline style for a calmer, less "boxy" feel. Filled variant (`FILL 1`) reserved for the active bottom-nav icon only.

### Shape & spacing
- Card / sheet radius: 18–24px (cards), 28px top corners (bottom sheets), 34–36px (phone/screen corners in mockup only — not applicable to real device).
- Buttons / chips: full pill (`border-radius: 99px`) for chips and switches; 13–14px rounded rect for primary/secondary buttons and input fields.
- Hairline borders (`1px solid var(--line)`) replace Material's tonal elevation for card separation — flat, no drop shadows on in-content cards (only the outer phone frame in the mockup has a shadow, which doesn't apply on-device).
- Progress track tint: bar track = 20% tint of its own fill color over the surface (`color-mix`), not a flat grey — keeps the whole card monochrome while the color story stays legible.

---

## Screens / Views

### 1. Übersicht (Overview) — `OverviewScreen.kt`
**Purpose:** Daily log — calorie ring, macro progress, weight, meals.
- **App bar:** day navigation unchanged (‹ date › + conditional "Heute"), date now set in serif, centered.
- **Summary card:** single `surface` card, 24px radius, 20px padding, contains (top to bottom, 18–20px gaps):
  1. **Calorie ring** — 176×176 (was 160), stroke 15–16px, same segmented-by-tag arc logic as today (`kcalForTag` order: healthy → neutral → unhealthy → untagged, butt caps, track = `surface-2`). Center: kcal in serif 46px, "KCAL" mono micro-label, "noch X" / "+X zuviel" / "Ziel erreicht" below in sans.
  2. **Clean-eating summary** — "N % CLEAN" in mono (uppercase, replaces sentence-case "% clean"), legend dots unchanged (Gesund/Neutral/Ungesund), only shown when any tag has kcal (same condition as today).
  3. Hairline divider.
  4. **Macro bars** (Protein/Kohlenhydrate/Fett) — label in sans-medium, value in mono (`118 / 160 g`), 8px pill track tinted 20% of the bar color, animated fill (see Motion below).
  5. Hairline divider.
  6. **Secondary macros** (Zucker/Ballaststoffe/Salz) — value now in serif (was body-medium), unit as smaller inline sans, label in `text-3` beneath.
- **Weight card:** separate `surface` card below, unchanged content/behavior (tap → dialog to log kg), value in serif, small edit icon in `text-3`.
- **Meal sections:** unchanged grouping/order (Frühstück/Mittagessen/Abendessen/Snack) and swipe-to-edit/delete behavior. Section header: meal name in **serif, accent color**; kcal total in mono, `text-2`. Entries: tag dot (7px) + name/brand/amount in sans; macro breakdown line in mono, `text-2`, indented under the dot.
- **Copy-from-yesterday button:** unchanged function, styled as hairline-outlined pill row instead of Material `OutlinedButton`, icon + label, hover/press → accent border+text.
- **FAB:** 56×56, 20px radius (squircle, not circle), accent fill, dark `ink` plus icon.
- **Bottom nav:** flat `surface` bar, hairline top border (no shadow/elevation), 3 items unchanged (Übersicht/Ziele/Statistik), active = accent + filled icon variant, inactive = `text-3` + outline icon variant.

### 2. Essen hinzufügen — `AddFoodScreen.kt`
- Top: back icon + rounded search field (14px radius, `surface` fill, hairline border, placeholder "Lebensmittel suchen") + separate square icon-button for barcode scan (was inline `IconButton`, now a distinct tappable tile matching field height).
- **Tabs** ("Verlauf" / "Meine Lebensmittel"): unchanged order/default (Verlauf first), styled as mono uppercase labels with a 2px accent underline on the active tab instead of Material `TabRow` indicator.
- **List:** grouped by date (`HEUTE`, `GESTERN`, …) with mono uppercase date-group labels; each row = tag dot + name(+brand) in sans, kcal/100g in mono beneath. Same swipe gestures as today (custom foods: StartToEnd = edit, EndToStart = delete+undo; history: EndToStart = delete+undo).
- **Amount entry:** currently a Compose `AlertDialog`; redesign uses a **bottom sheet** instead (slides up from bottom, 28px top radius, scrim behind) — this is the one structural change, purely presentational, same fields/flow:
  - Food name (serif)
  - "MENGE" mono label + numeric field (accent-bordered when focused) with unit suffix
  - "MAHLZEIT" mono label + pill chips (`MealCategory`, horizontally scrollable if needed)
  - "TAG" mono label + `TagSelector` chips (dot + label; tapping the selected chip clears to `NONE`, same as today)
  - Macro preview strip (`surface-2` pill, mono text)
  - Full-width accent "Hinzufügen"/"Speichern" button
  Same `NumericTextField` select-all-on-focus behavior must be preserved.

### 3. Barcode-Scanner — `BarcodeScannerScreen.kt`
- Camera preview unchanged (CameraX). Scan reticle redrawn as four accent-colored corner brackets (not a full rounded-rect outline) with a thin center scan-line for feedback; caption below in mono ("BARCODE IN DEN RAHMEN HALTEN").
- Torch toggle: circular translucent button, top-left, unchanged logic (`hasFlashUnit()` gate, `FlashOn`/`FlashOff` → swap to Material Symbols `flash_on`/`flash_off`).
- Manual barcode entry bar: unchanged function (`NumericTextField` + search `IconButton`, `imePadding()`), restyled to match the new field/button language.

### 4. Statistik — `StatsScreen.kt`
- Time range chips (Woche/Monat/Jahr): pill chips, mono labels, active = accent fill + `ink` text.
- **Kalorien** card: bar chart restyled — bars use `cal` color, rounded tops (4px), dashed accent goal line (unchanged data logic, `CalorieBarChart`).
- **Clean-Ernährung** card: unchanged 0–100% fixed-scale bars in `health` color; header now shows "Ø N %" in mono next to the serif card title.
- **Gewicht** card: summary row (Aktuell/Veränderung/Ziel) as label(mono)/value(serif) pairs; line chart keeps time-proportional x-axis, moving-average trend overlay, dashed target line — recolor raw series to `protein`, trend to `fat` (or theme tertiary), target line to `accent`, gridlines to `surface-2`.
- Export/Import buttons unchanged function, restyled to the new filled/outlined button language.

### 5. Ziele — `GoalsScreen.kt`
- All fields/logic unchanged (`MacroCalculator` recommendations, kcal↔macro consistency warning card, target weight). Visual changes only: mono uppercase field labels above each input (label moves from Material's floating label to a static mono caption), serif value text inside fields, small colored tick beside each macro field (protein/fat/carbs/kcal use their data color as an 8×20px accent bar), full-width accent "Speichern" button.
- Consistency-warning card keeps its function; restyle with the app's `unhealthy`/error tone rather than Material's `errorContainer`.

### 6. Daten — `DataScreen.kt`
- Same three blocks (Export, Import, Tägliche Erinnerung toggle) — card-per-block, serif card titles, sans descriptions, restyled buttons/switch. Switch track = accent when on, `surface-2` when off, matching the new pill-switch look (custom-drawn or `SwitchDefaults.colors` override — no functional change to `SettingsRepository`/`meal_reminder_enabled`).

---

## Interactions & Behavior

- **All existing navigation, gestures, dialogs, and data behavior are unchanged** — this is a visual/motion redesign, not a UX/IA change, except the AlertDialog → bottom-sheet swap on the amount-entry flow (§2) which is a pure presentation change (same fields, same confirm/cancel semantics).
- **Entrance motion, and when it (re)plays:** the calorie ring and the three macro bars animate in from empty on:
  1. **App open / Overview screen becoming visible** (initial composition / navigating back onto the screen),
  2. **Any tap on the Overview screen** (a lightweight "replay" affordance — reuse existing tap surfaces, e.g. wrap the summary card or trigger from `previousDay()`/`nextDay()`/`goToToday()`),
  3. **Day change** (`previousDay()`, `nextDay()`, `goToToday()` — i.e. whenever `OverviewViewModel`'s date changes and new totals load).
  In Compose, drive this with a `key(state.date)` (or an explicit `animationTrigger` counter bumped on tap) around the ring/bar composables so `animateFloatAsState`/`Animatable` restart from 0 rather than animating from the previous value — mirrors the prototype's approach of remounting the animated subtree via a changing React `key`.
- **Ring segment draw:** segments draw in sequence in kcal order (healthy → neutral → unhealthy), ~350–650ms each, `easeOutCubic`-ish easing, immediately followed by/overlapping the kcal number count-up (0 → total, ~1.6s, `easeOutExpo`-ish — starts fast, settles slowly), then the "noch X" caption and clean-% legend fade+slide in last (~400–600ms, ~200ms stagger). See `Calorie Ring Motion.dc.html` for the exact reference timeline (in seconds): track fade 0–0.4s, healthy arc 0.5–1.35s, neutral 1.35–1.75s, unhealthy 1.75–2.05s, kcal count 0.4–2.0s, sub-caption 2.1–2.6s, clean summary 2.6–3.2s.
- **Macro bars:** the three bars grow left-to-right (width 0 → target %) with a ~100–150ms stagger between Protein/Kohlenhydrate/Fett, `easeOutCubic`, ~900ms each, numeric value counts up in sync with the fill. See `Macro Bars Motion.dc.html` reference timeline: Protein 0.4–1.3s, Kohlenhydrate 0.55–1.45s, Fett 0.7–1.6s.
- **Card/list entrance:** cards and list sections fade up (~12px translate + fade, ~600ms, staggered ~80ms) on first paint of a screen — subtle, not repeated on every recomposition, only on screen entry.
- **Theming — auto light/dark:** the app should **not** expose a manual "paper vs. dark" switch. Instead it should follow the **system** light/dark setting exactly like the current app's `MacroTracTheme(darkTheme: Boolean = isSystemInDarkTheme())` already does: system light → "Paper" palette, system dark → dark palette. The prototype's Tweaks panel has an `auto` / `dark` / `paper` control only for previewing all three states in this HTML mockup — that control itself should **not** be built into the shipped app.
- **Accent color:** fixed to `#B79FCB` (pastel lilac) per the user's decision — not user-configurable in the shipped app (the color picker in the prototype's Tweaks panel is a prototyping aid only).

## State Management
No new state beyond what `OverviewViewModel` already exposes. The only addition is a small UI-local "replay trigger" (e.g. an `Int` bumped on tap, or simply keying animations off `state.date` which already changes appropriately) to restart entrance animations — this does not need to be persisted or affect the ViewModel/domain layer.

## Assets
No bitmap/vector assets used. All icons are Material Symbols Rounded (Google Fonts icon font — Compose equivalent: keep using `androidx.compose.material.icons` but prefer the **Rounded** icon set (`androidx.compose.material.icons.rounded.*`) where available to match the mockup's weight/style, e.g. `Icons.Rounded.Home`, `Icons.Rounded.Flag`, `Icons.Rounded.BarChart`, `Icons.Rounded.Add`, `Icons.Rounded.ChevronLeft/Right`, `Icons.Rounded.Edit`, `Icons.Rounded.ContentCopy`, `Icons.Rounded.Search`, `Icons.Rounded.FlashOn/Off`, `Icons.Rounded.FileDownload/Upload`). Fonts are all Google Fonts (Newsreader, Hanken Grotesk, JetBrains Mono) — bundle as Android downloadable fonts or package as app assets (`res/font/`).

## Files
- `MacroTrac Redesign.dc.html` — main visual reference: all 6 screens as annotated phone mockups (open in a browser; pan/zoom enabled). Includes a live Tweaks panel for accent color and theme preview (prototyping aid only, see above).
- `Calorie Ring Motion.dc.html` + `calorie-ring-scene.jsx` — looping motion reference for the calorie ring entrance animation, with exact timeline.
- `Macro Bars Motion.dc.html` + `macro-bars-scene.jsx` — looping motion reference for the macro-bar entrance animation, with exact timeline.
- `animations.jsx` — shared timeline/animation engine used by the two motion references (not relevant to the Android implementation itself, just infrastructure for the HTML prototypes).
- `support.js` — runtime required to open the `.dc.html` files in a browser.

Open any `.dc.html` file directly in a browser to view/interact with it.
