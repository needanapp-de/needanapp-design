# needanapp-design

Design-Paket von needanapp.de für Android-Apps: Theme, Darstellungs-Einstellungen und Bausteine im
Material-3-Expressive-Stil.
Kotlin, Jetpack Compose, Material 3, **minSdk 31 (Android 12)**.

Lizenz: [GPL-3.0](LICENSE) (nur Version 3), © 2026 myzel.dev; bis 0.33.0 Apache 2.0. Alles hier ist eigener Code,
fremde Bibliotheken nur unter GPL-verträglichen Lizenzen (siehe unten).

## Inhalt

| Bereich | Was |
|---|---|
| `theme` | `NeedanappTheme` (Compose), `DesignStyle` (die Stile NeedAnApp und Myzel, siehe unten), `DesignFonts` + `DesignType` (Geist, Geist Mono und die Punktschrift Doto), `NeedanappViewTheme` + `Theme.Needanapp` (XML für Views), `ThemeSettings`, `ThemeSettingsStore` (DataStore), `DesignColors` (Farben des Kartenlooks), `contrastRatio`, `readsLight`, `readableOn` und `fadedOn` (Kontrast), Bewegungs-Kurven |
| `settings` | `AppearanceSettings`: fertiger Bildschirm „Darstellung“ mit allen Theme-Einstellungen |
| `components` | `AppScaffold` (Kopfzeile mit Blur, Suche), `SubScreen` (Unterseite mit Zurück-Pfeil), `NavigationPill` (eine Leiste mit 12-dp-Ecken, Knöpfe links/rechts darin; `trailingMenu` als Menüknopf, der nach oben aufklappt), `SelectionCard` (schwebende Auswahlkarte mit den Aktionen einer Karte), `FloatingItemCard` (schwebende Karte für einen Eintrag, z. B. im Bildbetrachter; Abstand darunter `FloatingItemCardDefaults.SpaceBelowContent`), `ItemCard` (Listeneintrag als Karte, klappt seine Aktionen auf), `PillDefaults` (Knopffarben und Ecken auf der Pille), `FloatingSurface` (schwebende Fläche, mit `SurfaceProgress` als ganzer Fortschrittsbalken), gruppierte Listen (`SettingsGroup`, `ListRow`, `SwitchRow`, `SliderRow`, `SegmentedRow`, `ContentRow`), `OptionsSheet`, `EmptyState`, `LoadingState`, `ColorPicker`, `ChipRow` (Chips seitlich scrollend), `TabChip` (Tab mit langem Druck); Fakten als Kacheln (`Detail`, `DetailTile` – auch antippbar, z. B. ein Ort –, `detailLines`, `StatusTile`, `CardSpacing`); aufklappbare Aktionskarte (`CardActionRow`, `CardActionGroups`, `CardTileGroups`, `SelectionMark`); Kacheln (`TileCard` und `TileGroup`, auch mit `TileCardAction` neben dem Titel, `TileGrid`, `ChoiceTile`, `ToggleTile`, `SwitchTile`, `ActionTile` – auch mit Füllstand und kompakt, verteilt mit `rememberCompactTileLines` –, `StepperTile`, `TextFieldTile`); `LoadingBar`, Material-Taste und Fortschritt in der Signalfarbe (`SignalButton`, `signalButtonColors`, `SignalCircularProgressIndicator`, `SignalLinearProgressIndicator`; Farben für Schalter, Regler und Kästchen `signalSwitchColors`, `signalSliderColors`, `signalCheckboxColors`), `TagPill` (auch mit LED), `Modifier.designCard`; Teile eines Geräts (`DeviceDisplay` – Anzeige mit Punktschrift, Detail und Punktreihe –, `Led`, `DotProgress`, `MonoLabel`, `DeviceKey` und `RoundKey` mit `KeyStyle`, `keyColors`, `Modifier.keyEdge`, `deviceSwitchColors`); ein schwebendes Menü (`FloatingKnob`, `FloatingMenuEntry` – auch mit Wert –, `FloatingMenuSwitch`, `FloatingMenuLabel`, `CountBadge`, `Modifier.pressedIn`). Veraltet seit 0.42.0 (funktionieren weiter): `SectionCard`, `StatusBand`, `LabeledRow`, `PillButton`, `ActionBar`, `DetailRow`, `SheetAction`, `ToolbarPill`, `SelectionPill`, `NavigationPillWithToolbar` |
| `myzel` | intern: dieselben Bausteine im Stil Myzel; die öffentlichen schalten selbst dorthin um |
| `sample` | Beispiel-App zum Ausprobieren aller Bausteine und Einstellungen |

### Stile

Das Paket kennt zwei Stile (`ThemeSettings.design`, im Theme als `LocalDesignStyle`, abfragbar mit `isNeedanappDesign`
und `isMyzelDesign`):

- **NeedAnApp** (Standard aller Apps): der Kartenlook von needafile, unten beschrieben.
- **Myzel**: das Aussehen eines Geräts – Aluminium (hell) und Graphit (dunkel), Tasten mit Kante, schwarze Anzeigen
  mit Punktschrift, Pille und Menüs in Graphit, gesetzt in Geist. Ecken (22 dp) und Schrift sind fest. Alu und Graphit
  nehmen die Signalfarbe, wie sie ist (Paletten-Stil ohne Wirkung); jede andere Farbwelt färbt auch im Stil Myzel die
  Flächen, Pille, Menüs und Anzeigen bleiben dunkel. Der Stil hat eigene Farbwelten (`myzelLightWorld`,
  `myzelDarkWorld`, Standard Alu/Graphit) und eigene Signalfarben (Orange, Gelb, Grün) als erste Vorlagen.

Dieselben Bausteine zeichnen beide Stile; eine App muss dafür nichts ändern. Die Teile eines Geräts und das schwebende
Menü stehen im Stil NeedAnApp als Vorrat bereit: Anzeigen sind dort dunkle Felder mit Schrift in der Akzentfarbe, Tasten Felder der
Karte. Solange eine App sie nicht nutzt, sieht sie aus wie vorher. `NavigationPillWithMenu(myzelMenu = true)` zeigt im
Stil NeedAnApp die Pille mit Logo, Trennlinie und dem Menü mit Anzeige und Werten.

Eine App wählt ihren Stil über `ThemeSettingsStore.defaults` (siehe Einbinden) und bietet die Wahl mit
`AppearanceSettings(styles = listOf(DesignStyle.MYZEL, DesignStyle.NEEDANAPP))` an; ohne `styles` gibt es keine Wahl.

### Darstellungs-Einstellungen

- Stil NeedAnApp oder Myzel, nur bei Apps, die beide anbieten; im Stil Myzel ohne Kartenecken und eigene Schrift
- Modus System / Hell / Dunkel, reines Schwarz (AMOLED), Helligkeit im Dunkelmodus (hellere Flächen und Karten)
- Farbwelt je Modus: feste Grund-, Karten- und Textfarben (hell: Wolke (Standard), Papier, Nordschnee, Latte; dunkel:
  Graphit, Nordnacht (Standard), Mitternacht, Gedimmt) oder Getönt (aus der Akzentfarbe); dazu kräftige Welten mit
  eigener Akzentfarbe für „Standard“, hell und dunkel: Ozean (Petrol/Koralle), Aubergine (Pflaume/Pink), Senf
  (Senfgelb/Tannengrün), Feuer (Gelb/Rot)
- Akzentquelle: Standard (#3D63D8), Material You, eigene Farbe (Farbwähler, Hex, gespeicherte Farben),
  Vorlagen (Saphir, Smaragd, Bernstein, Violett, Koralle, Petrol, Limette, Rosé, Schiefer)
- Paletten-Stil: Tonal Spot, Neutral, Kräftig, Expressiv, Regenbogen, Obstsalat, Monochrom, Farbtreu (Standard), Inhalt
- Flächen-Tönung (0–2), Farbverlauf im Hintergrund an/aus (Standard seit 0.28.0: aus), Kartenhelligkeit (−100 bis +100 %, Hintergrund bleibt)
- Progressiver Blur, Transparenz der Pille (0–80 %), „Effekte reduzieren“ (schaltet Blur, Schatten und
  Übergänge ab; die System-Einstellung „Animationen entfernen“ wird ebenfalls beachtet)
- UI-Skalierung (85–115 %), Kartenecken (0–32 dp, Standard 14), eigene Schriftart (.ttf/.otf)
- „Nur ein Abschnitt offen“: einklappbare Abschnitte als exklusive Akkordeons (Standard seit 0.27.0)
- Sprache (System oder manuell, über Androids App-Sprachen), nur bei Apps mit mehr als einer Sprache

### Kartenlook

Seit 0.20 hat jede App dasselbe Aussehen, ohne Schalter: Flächen und Ränder sind etwas zur Hauptfarbe
getönt, im Dunkeln zusätzlich ein Stück zu Schwarz gezogen, damit nichts grell wirkt. Der Hintergrund ist
einfarbig; zuschaltbar ist ein Verlauf mit einem Schein der Hauptfarbe oben (bei reinem Schwarz bleibt er schwarz, mit
Flächen-Tönung 0 ohne Schein). Inhalte stehen auf Karten mit feinem Rand und weichem Schatten (ohne bei
„Effekte reduzieren“). Alle Farboptionen wirken weiter; der Look folgt Akzentfarbe, Palette, Hell/Dunkel und
Tönung.

- `NeedanappTheme` stellt die zusätzlichen Farben als `LocalDesignColors` bereit: Verlauf, Kartenfläche
  und -rand, Hinweistext, Grün für einen guten Zustand, Leisten unten.
- Alles bleibt lesbar, was Akzent, Farbwelt und Helligkeitsregler auch daraus machen: `buildColorScheme` und die
  `DesignColors` schieben jede Schrift- und „on“-Farbe, die auf ihrer Fläche unter 4,5:1 bliebe, um so wenig Ton wie
  nötig heller oder dunkler (`readableOn`); was schon liest, bleibt, wie es ist. Abgeschwächte Schrift (Hinweise,
  Nebentexte) wird nur so weit durchsichtig, wie sie lesbar bleibt (`fadedOn`). `ContrastTest` prüft das für beide
  Stile, alle Farbwelten, hell und dunkel, alle Akzente und die Enden der Regler.
- Die Akzentfarbe hat zwei Töne: `colorScheme.primary` ist ihr Ton für Schrift und Symbole auf der Seite, ein helles
  Signal-Orange wird dort also ein tieferes Orange. `LocalDesignColors.current.signal` (mit `onSignal`) ist die
  Akzentfarbe, wie sie gewählt ist, für alles, was sie füllt oder leuchten lässt: Tasten, LEDs, Zähler, die Knöpfe
  der Pille, Schalter und Fortschritt. Eigene Flächen in der Akzentfarbe nehmen `signal`, Schrift `primary`.
  Material füllt `Button`, die Fortschrittsanzeigen, Schalter, Regler und Kästchen mit `primary`; Apps nehmen dafür
  `SignalButton` (oder `colors = signalButtonColors()`), `SignalCircularProgressIndicator`,
  `SignalLinearProgressIndicator` und geben `Switch`, `Slider` und `Checkbox` `colors = signalSwitchColors()`,
  `signalSliderColors()` und `signalCheckboxColors()`. `FilledTonalButton`, `TextButton`, `OutlinedButton` und
  `RadioButton` (nur Ring und Punkt) bleiben, wie Material sie färbt.
- `AppScaffold` zeichnet den Verlauf selbst (`backgroundColor` ersetzt ihn); eigene Bildschirme nehmen
  `Modifier.designBackground(LocalDesignColors.current)`. Eine Leiste oben, die nahtlos übergehen soll,
  zeichnet `colors.backgroundBrush(höheDesFensters)`.
- Maßgeblich ist der Stil von needafile: Fakten stehen als Kacheln in einer `TileCard` (`DetailTile`, mehrere mit
  `detailLines`, zwei kurze teilen sich eine Zeile), ein Zustand als `StatusTile` darüber. Aktionen in einem Blatt sind
  `ActionTile`s in einer `TileCard` (zwei nebeneinander, eine allein flach), ein Eintrag einer Liste ist eine
  `ItemCard`, die beim Lang-Drücken oder über „Mehr“ ihre Aktionen aufklappt. Unterseiten wie Betrachter zeigen ihren
  Eintrag in einer schwebenden `FloatingItemCard` statt einer Leiste mit ⋮. Chips sind 12 dp rund (Form `small`).
  Dialoge (`ContentDialog`) lassen unten Platz für den Schatten einer Karte.
- Gespeicherte Werte, die noch auf den alten Standards stehen (Tonal Spot, 20 dp), stellt der
  `ThemeSettingsStore` einmalig auf die neuen um; was danach gewählt wird, bleibt.

Die Einstellungen liegen im DataStore `needanapp_theme` der App. Einklappbare Abschnitte starten immer
eingeklappt; nur der zuletzt geöffnete Abschnitt eines Bildschirms bleibt beim nächsten Besuch offen und wird dafür
ebenfalls dort gespeichert. Einklappbare Abschnitte (`SettingsGroup` mit `id`, `sectionState` und `icon`) haben als
Kopf eine Zeile wie die übrigen, mit Pfeil am Ende; aufgeklappt hängen ihre Zeilen darunter. Aufeinanderfolgende Gruppen
mit `block = RowPosition.FIRST/MIDDLE/LAST` bilden einen Block ohne großen Zwischenraum.

## Einbinden

Als **Gradle Composite Build**: Das Paket liegt im Ordner neben der App.

```bash
git clone https://github.com/needanapp-de/needanapp-design.git
```

Jede veröffentlichte Version hat einen Tag (ab `v0.56.0`); eine App, die eine bestimmte Version braucht, holt sie mit
`git clone --branch v<Version> …`.

```kotlin
// settings.gradle.kts der App
includeBuild("../needanapp-design")
```

```kotlin
// app/build.gradle.kts
dependencies {
    implementation("de.needanapp:design")
}
```

### Compose

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val store = ThemeSettingsStore(this)
        setContent {
            // gespeicherte Einstellungen abwarten statt kurz die Standardwerte zu zeigen
            val settings by store.settings.collectAsStateWithLifecycle(initialValue = store.cachedSettings)
            settings?.let { NeedanappTheme(it) { App(it) } }
        }
    }
}

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // nur bei einer App mit eigenen Standards, z. B. dem Stil Myzel; vor dem ersten Lesen
        ThemeSettingsStore.defaults = ThemeSettings(design = DesignStyle.MYZEL)
        // liest die Einstellungen schon beim Start, dann zeigt das erste Bild die App
        ThemeSettingsStore(this).preload()
    }
}

@Composable
fun App(settings: ThemeSettings) {
    val search = rememberSearchState()
    AppScaffold(
        title = stringResource(R.string.title),
        search = search,
        bottomBar = { hazeState ->
            NavigationPill(items, selected, onSelect = { … }, hazeState = hazeState,
                modifier = Modifier.align(Alignment.BottomCenter))
        },
    ) { padding ->
        // Inhalt scrollt unter Kopfzeile und Pille: padding als contentPadding verwenden
    }
}
```

Der Darstellungs-Bildschirm ist ein einziger Aufruf:
`AppearanceSettings(settings, contentPadding = …)`.

Die Sample-App (`sample/…/MainActivity.kt`) zeigt alle Bausteine im Zusammenspiel.

### Views (Übergangszeit)

Noch nicht umgestellte Bildschirme bekommen dieselben Farben:

```xml
<style name="AppTheme" parent="Theme.Needanapp" />
```

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    NeedanappViewTheme.apply(this) // vor super.onCreate
    super.onCreate(savedInstanceState)
}
```

## Sprachen

Gepflegt werden nur **Englisch** (`values/`, Standard) und **Deutsch** (`values-de/`). Alle Texte liegen in
String-Ressourcen mit dem Präfix `nd_`. Apps liefern per `androidResources.localeFilters += listOf("en", "de")`
nur diese beiden Sprachen aus. Für die Sprachauswahl sollten sie `generateLocaleConfig = true` setzen
(siehe `sample/build.gradle.kts`).

## Abhängigkeiten

| Bibliothek | Lizenz | Zweck |
|---|---|---|
| AndroidX, Compose, Material 3, Material Components | Apache 2.0 | Grundlage |
| [MaterialKolor](https://github.com/jordond/MaterialKolor) | MIT | Farbschemata aus Akzentfarbe und Paletten-Stil |
| [Haze](https://github.com/chrisbanes/haze) | Apache 2.0 | Hintergrund-Blur für Kopfzeile und Pille |
| Schriften [Geist, Geist Mono](https://github.com/vercel/geist-font), [Doto](https://github.com/oliverlalan/Doto) | SIL OFL 1.1 ([fonts/](fonts)) | Stil Myzel: Text, Tasten, Anzeigen |

## Bauen

Voraussetzungen: JDK 17 oder neuer und das Android-SDK (Pfad in `local.properties` als `sdk.dir` oder in
`ANDROID_HOME`).

```bash
./gradlew :sample:assembleDebug
```

Versionierung nach Semantic Versioning (`DESIGN_VERSION` in `gradle.properties`), Änderungen in
[CHANGELOG.md](CHANGELOG.md).
