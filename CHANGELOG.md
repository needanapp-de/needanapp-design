# Changelog

Format nach [Keep a Changelog](https://keepachangelog.com/de/1.1.0/), Versionen nach
[Semantic Versioning](https://semver.org/lang/de/).

## [Unreleased]

Geplant:

- Screenshot-Tests (Roborazzi) für alle Bausteine, hell/dunkel, große Schrift
- Material-3-Expressive-APIs (z. B. `HorizontalFloatingToolbar`) hinter den eigenen Bausteinen nutzen,
  sobald sie in der verwendeten Material-3-Version verfügbar sind
- Veröffentlichung über JitPack (`maven-publish`)
- GitHub Actions bzw. Gitea-Actions: Build + Tests bei jedem Push
- Weitere Bausteine: Toasts im eigenen Stil

## [0.57.1] – 2026-11-04

### Behoben

- `FastScrollbar(LazyListState, …)`: `label` ist wieder der letzte Parameter, als nachgestelltes Lambda und an vierter
  Stelle nutzbar wie vor 0.57.0. `reverseLayout` hat dafür eine eigene Überladung (`reverseLayout` vor `label`, ohne
  Standardwert); Aufrufe mit benannten Argumenten wie `reverseLayout = true, label = …` bleiben gültig.

## [0.57.0] – 2026-11-04

### Hinzugefügt

- `Modifier.scrollbar(LazyListState, …, reverseLayout)` und `FastScrollbar(LazyListState, …, reverseLayout)` für Listen
  mit `reverseLayout = true`, etwa einen Chat mit der neuesten Nachricht unten: Balken, Griff, Ziehen und die Blase
  folgen dann dem Bildschirm statt der Reihenfolge der Einträge; die Blase nennt den obersten sichtbaren Eintrag. Ohne
  Angabe bleibt alles wie bisher.

## [0.56.1] – 2026-11-01

### Behoben

- `NavigationPillWithMenu` und `NavigationPill(trailingMenu = …)`: Das offene Menü klappt zu, sobald eine Seite der
  Pille gewählt wird, per Tipp auf einen Eintrag oder per Wischen (neuer `selectedIndex`), in allen Stilen. Bisher
  blieb es offen über der neuen Seite.

## [0.56.0] – 2026-10-31

### Hinzugefügt

- `NavigationPill(leadingBar = PillBar(…))`: Der Knopf links verwandelt die Pille selbst in eine Leiste von Aktionen –
  je Symbol über kurzer Beschriftung, Gruppen durch feine Linien getrennt, vorne ein Kreuz zum Zurückverwandeln, darüber
  optional eine Beschriftung (`caption`, z. B. „Neu in „Download““). Eine Aktion, das Kreuz, Zurück, eine andere Seite
  oder das Ausblenden der Pille verwandeln sie zurück. Alle Tasten gleich breit, so breit wie die längste Beschriftung
  braucht und der Bildschirm erlaubt.
- `PillAction(shortLabel)`: kurze Beschriftung unter dem Symbol in der Leiste; TalkBack liest weiter
  `contentDescription`.

## [0.55.0] – 2026-10-31

### Geändert

- Dialoge im Stil NeedAnApp neu (`ConfirmDialog`, `ContentDialog`, `InfoDialog`, `TextInputDialog`): oben ein farbiges
  Band mit Symbol und Titel – `errorContainer` bei `isDestructive`, sonst die Signalfarbe, ruhig (`secondaryContainer`)
  bei `InfoDialog` –, unten eine durchgehende Tastenleiste, durch feine Linien geteilt, die Haupttaste in der Farbe des
  Bands gefüllt. Beschriftungen werden gemessen und nie gekürzt: Passt es nebeneinander nicht, fallen erst die Symbole
  weg, dann liegen die Tasten untereinander. Der Stil Myzel bleibt unverändert.

### Hinzugefügt

- `ConfirmDialog(icon)`, `ContentDialog(confirmIcon, extraIcon)`, `InfoDialog(icon)`, `TextInputDialog(icon)` für die
  Symbole in Band und Tasten.
- `BusyDialog`: Dialog ohne Tasten, solange etwas läuft, z. B. ein Tresor sich öffnet.

## [0.54.0] – 2026-10-31

### Hinzugefügt

- `FloatingSurface(progressColor, onProgressColor)`: Die Fortschrittsfüllung kann eine andere Farbe als die Signalfarbe
  haben, z. B. `errorContainer` beim Löschen.

## [0.53.1] – 2026-10-30

### Hinzugefügt

- `signalSwitchColors()`, `signalSliderColors()` und `signalCheckboxColors()`: Schalter, Regler und Kästchen in der
  Signalfarbe. `deviceSwitchColors()` ist jetzt dasselbe wie `signalSwitchColors()`.

### Behoben

- Die Schalter von `SwitchTile`, von `SwitchRow` und im schwebenden Menü waren im Stil NeedAnApp noch im Schrift-Ton
  `primary` statt in der Signalfarbe (z. B. #AF3400 statt #FF4F00); jetzt die Spur in `signal`, der Daumen in
  `onSignal`. Die Regler von `SliderRow` nehmen in beiden Stilen die Signalfarbe.

## [0.53.0] – 2026-10-30

### Hinzugefügt

- `SignalButton`, `signalButtonColors()`, `SignalCircularProgressIndicator` und `SignalLinearProgressIndicator`:
  Materials gefüllte Taste und Fortschrittsanzeigen in der Signalfarbe (`DesignColors.signal` mit `onSignal`) statt im
  Schrift-Ton `primary`. Apps nehmen sie statt `Button`, `CircularProgressIndicator` und `LinearProgressIndicator`.

### Geändert

- Die Tasten der Einführung und der Farbauswahl, `LoadingBar` und die Wartesymbole der Blätter nehmen die Signalfarbe.

## [0.52.0] – 2026-10-30

### Hinzugefügt

- `Color.readableOn(hintergründe)` schiebt eine Farbe um so wenig Ton wie nötig heller oder dunkler, bis sie auf allen
  Hintergründen 4,5:1 erreicht (`TEXT_CONTRAST`, für Symbole `ICON_CONTRAST`); eine lesbare Farbe bleibt unverändert.
  `Color.fadedOn(alpha, hintergründe)` macht Schrift nur so weit durchsichtig, wie sie lesbar bleibt.
- `DesignColors.signal` und `onSignal`: die Akzentfarbe, wie sie gewählt ist, für Flächen und Lichter. Tasten
  (`KeyStyle.SIGNAL`), LEDs, `CountBadge`, die Knöpfe und der Fortschritt der Pille, Schalter, gewählte Kacheln,
  Häkchen und die Schritte der Einführung nehmen sie statt `colorScheme.primary`.
- `ContrastTest` (JVM): baut die Farben beider Stile in allen Farbwelten, hell und dunkel, mit allen Akzenten, allen
  Paletten und den Enden der Regler (Tönung, Leuchten, Kartenhelligkeit, Dunkel-Helligkeit, reines Schwarz) wie
  `NeedanappTheme` und prüft jedes Paar aus Schrift oder Symbol und Fläche, das die Bausteine zeichnen.

### Geändert

- `colorScheme.primary` ist jetzt der Ton der Akzentfarbe, der als Schrift und Symbol auf allen Flächen liest. Ein
  helles Signal (z. B. Myzel-Orange auf Aluminium, Signal-Orange und Neon-Pink in den hellen Welten) wird dort tiefer;
  als Fläche bleibt es über `signal` hell. Ebenso `secondary`.

### Behoben

- Die Leuchtschrift der Anzeigen (`displayLit`, z. B. die Überschrift eines Menüs) war in der hellen Welt Warnweste,
  bei Latte und mit dunklen Akzenten kaum zu lesen (bis 1:1). Sie nimmt jetzt die Signalfarbe der Welt (das Gelb der
  Warnweste, das Limette von Ultraviolett, das Kupfer, das Himmelblau von Oliv), sonst die Akzentfarbe so hell wie
  nötig. Auch die leise Schrift der Anzeigen (`displayDim`) ist in Latte lesbar.
- Helle Welten Signal und Neon-Pink: `surfaceContainerHighest` und `surfaceDim` waren wegen der schwarzen Linien fast
  schwarz, dunkle Schrift darauf unlesbar (Kacheln, Etiketten, gedrückte Knöpfe). Sie nehmen jetzt ein helles Grau.
- Fehlerrot auf Aluminium, Hinweistexte auf Karten, Nebentext gewählter Einträge, die gedimmte Welt, Text auf dem
  Grün im Myzel-Stil (`onTertiary`), weiße Schrift auf dem Rot der dunklen Welten Kupfer und Oliv, Schrift auf der
  Akzentfarbe bei manchen Vorlagen im Myzel-Stil und Text auf Karten bei hoch gestellten Helligkeitsreglern hatten
  zu wenig Kontrast; alles erreicht jetzt 4,5:1.

## [0.51.1] – 2026-10-30

### Behoben

- Stil NeedAnApp mit einer Farbwelt, deren Pille dunkel ist (z. B. Gelb): Das Menü der Pille mit `myzelMenu`, die
  Einträge der Pille selbst und die Menüs aus `FloatingMenuEntry`, `FloatingMenuSwitch` und `FloatingMenuLabel` hatten
  dunkle Schrift auf der dunklen Pille. Sie nehmen jetzt `onPill` und `onPillVariant` wie die übrigen Bausteine auf
  `FloatingSurface`.

## [0.51.0] – 2026-10-30

### Hinzugefügt

- `SteadyDeleteInput { … }`: Textfelder darin löschen gleichmäßig langsam – eine gehaltene Löschtaste nimmt ein
  Zeichen nach dem anderen, höchstens etwa zehn pro Sekunde, auch wo die Tastatur sonst beschleunigt oder ganze
  Wörter löscht. Dafür bekommt die Tastatur das Feld als einfache Eingabe (`TYPE_NULL`) gemeldet und schickt
  einzelne Tastendrücke; Vorschläge und Wischeingabe entfallen dort.

### Geändert

- `TextInputDialog` (beide Stile) nutzt das zustandsbasierte Textfeld (`TextFieldState`) und `SteadyDeleteInput`:
  Den Cursor-Griff über den Rand gezogen, scrollt das Feld weiter (bisher blieb er am sichtbaren Ende hängen), und
  die Löschtaste löscht gleichmäßig langsam.

## [0.50.0] – 2026-10-28

### Hinzugefügt

- Einführung (Onboarding) mit Live-Vorschau:
  - `OnboardingScreen`: oben eine verkleinerte, nicht bedienbare Vorschau der App (echte Bausteine, nimmt jede Wahl
    sofort an), darunter der Schritt mit Fortschritt, Titel, optionalem Hinweis („Optional“), Inhalt und Knöpfen;
    „Überspringen“ oben rechts.
  - `OnboardingButton` (gefüllt oder umrandet, optional mit Symbol), `OnboardingPoint` (Symbol + Satz),
    `OnboardingStatusRow` (erledigt = gefüllt mit Haken, offen = nur Rahmen).
  - `OnboardingAppearance`: Stil, Hell/Dunkel/System und Farbwelt als kompakte Kacheln; speichert sofort.
- Texte `nd_onboarding_*` (de/en).
- Beispiel-App: „Einführung“ in der Gruppe „Rückmeldung“.

### Geändert

- `ColorWorldPicker` kann kompakt: sechs Kacheln je Zeile ohne Namen (TalkBack liest sie vor).

## [0.49.0] – 2026-10-28

### Geändert

- Stil Myzel: Der Akzent „Standard“ ist dort Myzel-Orange (`NeedanappDefaults.MyzelAccent`, #FF5A1F) statt der
  Farbe der App, solange die Farbwelt keinen eigenen Akzent mitbringt. Gilt für die Farben, die Vorschau der
  Farbwelten und den Hinweis unter „Standard“.

### Hinzugefügt

- `ThemeSettings.styleAccent(appAccent)`: der Standard-Akzent des Stils, für Apps, die ihn selbst brauchen (z. B. fürs
  App-Symbol).

## [0.48.0] – 2026-10-28

### Hinzugefügt

- Scrollbalken: `Modifier.scrollbar(state, contentPadding)` für `LazyListState`, `LazyGridState` und `ScrollState`
  zeigt am Rand einen dünnen Balken, solange die Liste scrollt, und blendet ihn eine Sekunde danach aus; nur Anzeige.
  `Modifier.verticalScrollWithBar()` scrollt eine Spalte und zeigt den Balken dazu.
- `FastScrollbar(state, modifier, contentPadding, label)` für lange Listen und Raster: Beim Scrollen erscheint ein
  Griff, der sich ziehen lässt; eine Blase daneben zeigt, was `label` für das oberste Element liefert (z. B.
  Anfangsbuchstabe, Monat, Größe). Listen unter zwei Bildschirmen bekommen nur den dünnen Balken.
- `ScrollbarDefaults.barColor()` und `handleColor()`.

### Geändert

- `AppearanceSettings`, die Dialoge, `FloatingItemCard` und `SelectionCard` zeigen beim Scrollen den dünnen Balken.

## [0.47.0] – 2026-10-27

### Hinzugefügt

- Zwei Stile, `ThemeSettings.design` (`DesignStyle`): **NeedAnApp**, der bisherige Kartenlook und Standard aller
  Apps, und **Myzel**, das Aussehen eines Geräts aus dem Zweig `geraet` (Aluminium und Graphit, Tasten mit Kante,
  Anzeigen mit Punktschrift, Pille und Menüs in Graphit, Geist). Die öffentlichen Bausteine schalten selbst um, die
  Fassungen des Stils Myzel liegen intern in `de.needanapp.design.myzel`. Abfrage mit `LocalDesignStyle`,
  `isNeedanappDesign`, `isMyzelDesign`; `String.labelCase()` schreibt Beschriftungen, wie der Stil sie setzt.
- Stil Myzel: eigene Farbwelten `myzelLightWorld` und `myzelDarkWorld` (Standard Alu, neu, und Graphit), die mit den
  festen Farben eines Geräts gezeichnet werden; alle anderen Farbwelten färben auch hier die Flächen, Pille, Menüs und
  Anzeigen bleiben dunkel. Signalfarben Orange, Gelb und Grün als erste Vorlagen (`accentPresets`), feste Ecken
  (22 dp) und Schrift.
- `AppearanceSettings(styles = …)`: mit mindestens zwei Stilen die Zeile „Stil“ oben im Abschnitt Modus. Im Stil
  Myzel fehlen Kartenecken und eigene Schrift; bei Alu und Graphit erklärt ein Hinweis, dass der Paletten-Stil dort
  nicht wirkt.
- `ThemeSettingsStore.defaults`: die Standards einer App, bevor etwas gespeichert ist, z. B. der Stil Myzel. Stil und
  Myzel-Farbwelten stehen auch in der Sicherung.
- Teile eines Geräts für beide Stile: `DeviceDisplay`, `Led`, `DotProgress`, `MonoLabel`, `DeviceKey`, `RoundKey`,
  `KeyStyle`, `keyColors`, `Modifier.keyEdge`, `deviceSwitchColors`, `StatusTone.displayColor()`; ein schwebendes
  Menü mit `FloatingKnob`, `FloatingMenuEntry`, `FloatingMenuSwitch`, `FloatingMenuLabel`, `CountBadge`,
  `Modifier.pressedIn`, `PillBarHeight`. `DesignColors` hat dafür Anzeige-, LED-, Tasten- und Kachelfarben, im Stil
  NeedAnApp aus dem Schema abgeleitet (dunkle Anzeige mit Schrift in der Akzentfarbe).
- `NavigationPillWithMenu(myzelMenu = true)`: im Stil NeedAnApp die Pille mit Logo, Trennlinie und dem Menü mit
  Anzeige, Werten (`PillAction.value`) und Schaltern.
- `CardStatus(progress, detail)` für die Anzeige, `TagPill(led = …)`, `PillButton(style = …)`.
- Schriften Geist, Geist Mono und Doto (SIL OFL 1.1, Lizenzen in `fonts/`), `DesignFonts`, `DesignType`.
- Beispiel-App: Wahl des Stils unter Darstellung.

### Geändert

- `NavigationPillWithMenu` ist nicht mehr veraltet.
- `Color.mixedWith` ist öffentlich.
- `SheetAction(tint)` ist ohne Angabe `Color.Unspecified` und wird wie bisher zu `onSurfaceVariant`, im Stil Myzel zur
  Textfarbe.

Für Apps ohne Wahl des Stils (needafile und die übrigen) ändert sich nichts am Aussehen; sie werden um die Schriften
(rund 800 KB) größer.

## [0.46.0] – 2026-10-25

### Hinzugefügt

- `TabChip(onClickLabel = …)`: benennt den normalen Druck für TalkBack, z. B. wenn ein Druck auf den gezeigten Tab
  dessen Karte öffnet statt nur zu ihm zu wechseln.

## [0.45.3] – 2026-10-21

### Behoben

- `CardActionGroups`: Ein Knopf mit `isActive` (z. B. ein eingeschalteter Modus) ist unter „Mehr“ jetzt markiert
  wie in der Knopfreihe darüber und meldet TalkBack „ausgewählt“; bisher wurde `isActive` dort nicht angezeigt.

## [0.45.2] – 2026-10-19

### Geändert

- `ActionPill`: Eine Aktion ohne kleine Knöpfe daneben schwebt allein als Knopf in der Akzentfarbe, so hoch wie die
  Pille und in ihrer Form, ohne die Leiste darum. Die Leiste rahmte nur den einen Knopf ein und wirkte in manchen
  Farbwelten wie ein dunkler Rand. Ein Hinweis ohne Aktion behält die Leiste, damit sein Text über dem Inhalt lesbar
  bleibt.

## [0.45.1] – 2026-10-18

### Behoben

- `SelectionCard`: Der Kreis mit der Zahl der ausgewählten Einträge war in Farbwelten mit gelber Akzentfarbe
  unsichtbar (Akzent auf der gelben Auswahlfarbe). Er nimmt jetzt die Farben des Hakens (`DesignColors.mark` und
  `onMark`), die sich auf ausgewählten Einträgen abheben.

## [0.45.0] – 2026-10-18

### Hinzugefügt

- `TabChip`: Tab als Chip (Optik wie `FilterChip`) mit optionalem Symbol und langem Druck (`onLongClick`,
  `onLongClickLabel` für TalkBack), z. B. um den ganzen Pfad eines Tabs zu zeigen.
- `TileGroup`: Kacheln wie eine gruppierte Liste ohne äußere Karte; die Kacheln nehmen die Kartenfarbe an.
- `TileCard(action = TileCardAction(…))` bzw. `TileGroup(action = …)`: Textknopf mit Symbol rechts neben dem Titel,
  z. B. „Hinzufügen“.
- `ActionTile(fill = …)`: Füllstand als dünner Balken unter den Texten (z. B. Speicher). `ActionTile(compact = true)`:
  kleine Kachel (kleinere Schrift, weniger Rand, Symbol ohne Kreis), drei nebeneinander auf dem Handy.
- `rememberCompactTileLines`: verteilt kompakte Kacheln so auf Zeilen, dass kein Wort mitten drin umbricht
  (Zeilen mit langen Namen nehmen weniger Kacheln, z. B. 2 + 3).
- `PillDefaults.shape` (12 dp) und `PillDefaults.buttonShape` (8 dp) für eigene schwebende Leisten.
- `LoadingState`: ruhige Ladeanzeige für leere Listen (Symbol in einem drehenden Ring, Text darunter, z. B.
  „NAS wird geöffnet …“); erscheint erst nach einem Augenblick, damit schnelle Ladevorgänge nicht aufblitzen.

### Geändert

- Pille (`NavigationPill` mit `leading`/`trailing`/`trailingMenu`): eine einzige Leiste mit 12-dp-Ecken statt
  runder Pille mit runden Knöpfen daneben. Die Knöpfe links und rechts stehen in der Leiste, durch dünne Striche
  abgetrennt; aktiver Eintrag und Knöpfe mit 8-dp-Ecken, das aufklappende Menü mit 12 dp. Gilt für alle Apps
  mit dem Paket.

## [0.44.1] – 2026-10-18

### Geändert

- `SurfaceProgress`: Die Fläche füllt sich jetzt in voller Akzentfarbe (`primary`). Wo die Füllung hinreicht,
  erscheint alles darauf in den Farben auf der Akzentfarbe: Schrift in `onPrimary`, Knöpfe nach ihrer Helligkeit
  umgefärbt, sodass sie Knöpfe bleiben. An der Kante wechselt die Schrift mitten im Wort die Farbe.
  Ohne Bewegung bei unbekannter Dauer bleibt die lesbare Tönung. Beispiel-App: Knopf „Abbrechen“ in der Demo.

## [0.44.0] – 2026-10-18

### Hinzugefügt

- `FloatingSurface(progress = …)` mit `SurfaceProgress.Fraction(wert)` bzw. `SurfaceProgress.Unknown`: Die ganze
  schwebende Fläche ist der Fortschrittsbalken (z. B. einer Kopie) und füllt sich von ihrem Anfang in einem Ton der
  Akzentfarbe. Der Ton ist so kräftig, wie es die Schrift auf der Pille zulässt (Kontrast mindestens 4,5:1).
  Unbekannte Dauer: Ein Band läuft durch; ohne Bewegung ist die Fläche durchgehend getönt.
  Beispiel-App: „Fortschritt“ mit Regler.

## [0.43.0] – 2026-10-17

### Hinzugefügt

- `DetailTile(onClick, actionIcon, actionLabel)`: Eine Fakt-Kachel kann eine Schaltfläche sein, z. B. ein Ort, der eine
  Karte öffnet. Rechts zeigt ein Symbol im Kreis (wie bei `ActionTile`), was ein Tipp tut; der Wert ist dann nicht
  markierbar. Beispiel-App: Ort im Betrachter.

## [0.42.0] – 2026-10-17

Der Stil von needafile ist jetzt der des Pakets.

### Hinzugefügt

- Fakten als Kacheln: `Detail`, `DetailTile`, `TileCardScope.detailLines` (zwei kurze teilen sich eine Zeile),
  `StatusTile`; `CardSpacing` für Karten untereinander.
- `ItemCard`: Listeneintrag als Karte mit Symbol, Untertitel, Auswahl und aufklappbaren Aktionen (`CardMenu`).
- `SubScreen`: Unterseite mit Zurück-Pfeil, optional mit schwebender Karte unten.
- `ChipRow`: Chips seitlich scrollend, startet auf Wunsch bei einem Eintrag weiter rechts.
- `FloatingItemCardDefaults.SpaceBelowContent`: Platz unter dem Inhalt, damit die schwebende Karte nichts verdeckt.

### Geändert

- Form `small` (u. a. Chips) ist 12 dp statt 8 dp rund.
- `ContentDialog` lässt unten 4 dp Platz, damit der Schatten einer Karte im Dialog nicht abgeschnitten wird.
- `CardTileGroups` nimmt Text- und Kartenfarbe als Standard (vorher Pflichtangaben).
- Beispiel-App zeigt Kacheln, Chips, Listenkarten und den Betrachter mit schwebender Karte.

### Veraltet

- `SectionCard`, `StatusBand`, `LabeledRow`, `PillButton`, `ActionBar`, `ActionBarButton`, `ActionBarSlot`,
  `DetailRow`, `SheetAction`, `ToolbarPill`, `SelectionPill`, `NavigationPillWithToolbar`, `NavigationPillWithMenu`.
  Sie funktionieren weiter; die Meldung nennt jeweils den Ersatz.

## [0.41.0] – 2026-10-17

### Hinzugefügt

- `PillScrollBehavior.onScroll(delta)`: Scrollen von Inhalten außerhalb von Compose (z. B. WebView, PDF-Ansicht)
  melden, damit Pille oder schwebende Karte beim Weiterlesen ausweichen und beim Zurückscrollen wiederkommen.

## [0.40.1] – 2026-10-17

### Geändert

- `FloatingItemCard` gleitet auch dann herein, wenn sie zusammen mit ihrem Bildschirm oder der Anfrage erscheint,
  die sie zeigt (vorher nur bei späterem Einblenden).

## [0.40.0] – 2026-10-17

### Hinzugefügt

- `CardTileGroups`: Kacheln im aufgeklappten Teil einer Karte, in Kästen wie die `CardActionGroups` – jedes
  `label` beginnt einen Kasten mit Titel, die Zeilen darin liegen wie in einer `TileCard` (z. B. Infos zu einem Bild).
- `FloatingItemCard(details = …)`: klappt statt der Gruppen eigene Inhalte unter den Aktionen auf, etwa
  `CardTileGroups`; Zurück klappt sie zu.

### Geändert

- `MarqueeText` steht vor jedem Durchlauf 3 statt 2 Sekunden still, damit der Anfang des Textes
  in Ruhe lesbar ist.

## [0.39.0] – 2026-10-17

### Hinzugefügt

- `FloatingItemCard`: die Aktionen eines einzelnen Eintrags als schwebende Karte unten statt der Pille, z. B. im
  Bildbetrachter – Name (läuft durch) und eine Zeile mit Angaben, darunter `CardActionRow`; „Mehr“ klappt die
  `CardActionGroups` darunter auf, Zurück klappt sie zu. Gleiche Farben wie eine geöffnete Karte in einer Liste.

## [0.38.1] – 2026-10-15

### Behoben

- `ActionTile` (`centered`) und `ChoiceTile` (`stacked` mit Symbol): Kacheln nebeneinander richten Symbol und
  Titel oben aus, statt sie mittig zu setzen – bei unterschiedlich langen Beschreibungen lagen Symbole und
  Titel sonst auf verschiedenen Höhen

## [0.38.0] – 2026-10-15

### Hinzugefügt

- `TextFieldTile`: Eingabefeld als Kachel einer `TileCard`, z. B. das Passwort unter dem Schalter, der danach fragt.

### Geändert

- `ChoiceTile`: Der `summary` gestapelter Kacheln bricht auf eine zweite Zeile um, statt kleiner zu werden. In
  Zeilenform steht das Symbol mit `iconInCircle` jetzt im Kreis, wie bei `ActionTile`.

### Behoben

- `ChoiceTile` in Zeilenform mit `iconInCircle`: Das Symbol der gewählten Kachel war unsichtbar (Akzent auf Akzent).

## [0.37.1] – 2026-10-15

### Behoben

- `TileCard` und `TileGrid` sind jetzt `inline` (wie `Column`). Liest der Inhalt einen Zustand direkt (nicht erst in einer
  Kachel), z. B. ob eine Zeile erscheint, wird die ganze Karte neu aufgebaut. Vorher änderte sich dann nichts.

## [0.37.0] – 2026-10-15

### Hinzugefügt

- `ActionTile`:
  - `trailing` setzt etwas ans Ende einer `inline`-Kachel, z. B. einen eigenen Knopf;
  - `iconInCircle = false` zeigt das Symbol ohne Kreis;
  - `centered` und `inline` zusammen ergeben eine knopfartige Kachel, Symbol und Titel mittig in einer Zeile (z. B. „Öffnen“ und „Bearbeiten“ nebeneinander).
- `ChoiceTile(summary = …)`: eine zweite, kleinere Zeile unter der Beschriftung, z. B. „Port 22“.
- `SwipeActionRow(shape = …)`: für Zeilen anderer Form, z. B. Kacheln in einer `TileCard`.

## [0.36.0] – 2026-10-15

### Geändert

- `ToggleTile` ist kompakt wie die Schnelleinstellungen des Systems: Symbol (im Kreis) vorn, daneben der kurze Titel und
  darunter „An“/„Aus“, mindestens 64 dp hoch; die An/Aus-Marke oben rechts entfällt. Ein `summary` steht als weitere
  Zeile darunter.

### Hinzugefügt

- `ToggleTile(description = …)`: wird statt des kurzen Titels vorgelesen, z. B. der ganze Satz, für den der Titel steht.

## [0.35.0] – 2026-10-15

### Hinzugefügt

- Kacheln für Einstellungsseiten und Optionsblätter:
  - `TileCard`: Karte mit Zeilen gleich breiter Kacheln (eine Zeile so hoch wie ihre höchste Kachel) und kleinen
    Zwischenüberschriften; außen die Kartenecken, innen kleine Radien und 3 dp Abstand wie bei den gruppierten Listen.
  - `ChoiceTile`: Auswahl mit Symbol vor oder (`stacked`) über der Beschriftung, mit `iconInCircle` im großen
    Kreis. Schmale Beschriftungen werden kleiner statt abgeschnitten; die gewählte Kachel hat die Akzentfarbe, ihr
    Kreis wird dunkel.
  - `ToggleTile`: Schalter als Kachel mit An/Aus-Marke, eingeschaltet in der Akzentfarbe.
  - `SwitchTile`: Schalterzeile als Kachel (Symbol, Titel, Untertitel, Switch am Ende).
  - `ActionTile`: Symbol im Kreis, Titel, Kurzinhalt; `centered` als große Ziel-Kachel, `inline` als Zeile, rot bei `destructive`,
    `iconContent` für eigenen Kreisinhalt (z. B. App-Symbol).
  - `StepperTile`: Wert mit Knöpfen für kleiner/größer.
  - `TileGrid`: Kacheln ohne Karte, jede wie eine Karte gerundet, mit Abschnittsüberschriften in der Akzentfarbe;
    `ToggleTile(iconInCircle = true)` setzt das Symbol dafür in einen Kreis.
  - `TileDefaults`: Farben der Kacheln.
- Beispiel-App: Abschnitt „Kacheln“.

## [0.34.0] – 2026-10-15

### Hinzugefügt

- Aufklappbare Aktionskarte aus needafile: `CardAction`, `CardActionGroup`, `CardMenu`, `CardActionRow` (Aktionen
  nebeneinander, destruktive immer auf Rot, aktive markiert, lange Beschriftungen werden kleiner statt umzubrechen)
  und `CardActionGroups` (die Gruppen unter „Mehr“ in eigenen Kästen, vier Knöpfe je Zeile). Die Kästen bleiben in
  jeder Farbwelt lesbar: Wo die Kartenschrift mit ihnen versinken würde, werden sie tiefer und nehmen die
  Schriftfarbe des Hintergrunds; Gruppentitel in der Akzentfarbe nur bei Kontrast ≥ 4,5.
- `SelectionCard`: schwebende Auswahlkarte als Alternative zur `SelectionPill` – Anzahl im Abzeichen, Zusammenfassung
  über durchlaufenden Namen, „Alle auswählen“, darunter die Aktionen der Karte, „Mehr“ klappt die Gruppen auf, Zurück
  klappt sie zu; gleitet mit dem zuletzt Gezeigten weg und weicht der Tastatur.
- `SelectionMark`: das runde Häkchen in der Farbe `DesignColors.mark` anstelle des Symbols ausgewählter Einträge.
- `DesignColors.onSelectionVariant`: Nebentext (Größe, Datum) auf ausgewählten Einträgen.
- `contrastRatio(a, b)` (WCAG) und `Color.readsLight()` in `de.needanapp.design.theme`.
- `PillDefaults.tonalButtonColors()` / `tonalIconButtonColors()`: Hauptknöpfe auf einer `FloatingSurface` im
  primaryContainer, der sich in jeder Farbwelt von der Pille abhebt.
- Sample-App: Karte mit Aktionen, Leiste anstelle der Pille, Leistenart „Auswahl: schwebende Karte“.

## [0.33.1] – 2026-10-15

### Geändert

- Lizenz: GNU GPL 3.0 (nur Version 3) statt Apache 2.0, Rechteinhaber myzel.dev. Verträglich mit den GPL-Apps, die
  das Paket nutzen (Myzel, needamail); in eigenen Apps darf es der Rechteinhaber weiter frei einsetzen. Fassungen bis
  0.33.0 bleiben unter Apache 2.0.

## [0.33.0] – 2026-10-15

### Geändert

- Neue kräftige Farbwelten mit eigener Akzentfarbe, je hell und dunkel: **Signal** (Schwarz, Weiß, Signalorange),
  **Ultraviolett** (Violett, Neon-Limette), **Warnweste** (Graphit, Neongelb), **Kupfer** (Petrol, Kupfer),
  **Neon-Pink** (Graphit, Neon-Pink) und **Oliv** (Oliv, Himmelblau).

### Entfernt

- Die Farbwelten Ozean, Aubergine, Senf und Feuer. Wer eine davon gewählt hatte, bekommt wieder den Standard
  (Wolke bzw. Nordnacht).

### Hinzugefügt

- `WorldAccent` kann eigene Farben für ausgewählte Einträge (`selection`, secondaryContainer), das Häkchen (`mark`)
  und destruktive Aktionen (`danger`, errorContainer) mitbringen; `SurfaceColors` eine eigene Pille (`pill`/`onPill`)
  und eine kräftigere Kartenlinie (`lineWidth`).
- `DesignColors.pill`, `onPill`, `onPillVariant`, `pillMinOpacity`, `mark`, `onMark`, `cardBorderWidth`.
  `FloatingSurface` nutzt die Pillenfarbe und setzt `LocalContentColor`; eine Pille, die sich stark vom Bildschirm
  abhebt, bleibt über dem Blur zu mindestens 88 % deckend.

## [0.32.0] – 2026-10-11

### Hinzugefügt

- `SelectionPill`: die Pille, während Einträge ausgewählt sind, an Stelle der Navigation. Vorn ein Haken mit der Anzahl auf
  primaryContainer, dahinter Aktionen für die Auswahl (z. B. Teilen, Kopieren, Löschen, „Mehr“). TalkBack liest
  „3 ausgewählt“ (`nd_selected_count`, eigener Text möglich); wird die Pille zu schmal, weicht die Anzahl den Aktionen.
- Beispiel-App: Leistenvariante „Auswahl“.

### Behoben

- `RefreshBox` behält ihren Inhalt, wenn `enabled` wechselt. Bisher wurde er dabei neu aufgebaut, und eine laufende
  Geste darin brach ab (z. B. Ziehen zum Auswählen, wenn die Auswahl das Aktualisieren abschaltet).

## [0.31.0] – 2026-10-11

### Hinzugefügt

- Vier kräftige Farbwelten, je hell und dunkel: Ozean (Petrol mit Koralle), Aubergine (Pflaume mit Pink), Senf
  (Senfgelb mit Tannengrün) und Feuer (Signalgelb mit Rot, dunkel Rot mit Gelb). Sie bringen eine eigene, passende Akzentfarbe mit (`SurfaceColors.accent`, `WorldAccent`), die
  bei Akzentfarbe „Standard“ an die Stelle der App-Farbe tritt, samt Container; Vorlagen, eigene Farbe und Material You
  haben weiter Vorrang. Kontraste nach WCAG geprüft (Text ≥ 7:1, Zweittext und Akzent auf Karten ≥ 4,5:1).
- `ThemeSettings.worldAccent(isDark)`; `resolveAccent` hat dafür den optionalen Parameter `isDark`.
- Die Kacheln der Farbwelten zeigen die Akzentfarbe, die die Welt hätte; die Zeile „Standard“ nennt die Welt, deren
  Farbe gerade gilt.

## [0.30.0] – 2026-10-11

### Geändert

- Standard-Farbwelten sind jetzt Wolke (hell) und Nordnacht (dunkel) statt „Getönt“; „Getönt“ bleibt wählbar.
  Gespeicherte Einstellungen von vor dieser Version stellen ein gespeichertes „Getönt“ einmalig auf den neuen Standard
  (`look` = 4); was danach gewählt wird, bleibt.

## [0.29.1] – 2026-10-11

### Geändert

- Akzentfarbe „Standard“: Die Zeile sagt jetzt, wie man die Farbe ändert. Ein Tipp auf sie oder ihr Farbfeld öffnet den
  Farbwähler mit der Farbe der App; „Farbe übernehmen“ stellt auf „Eigene Farbe“ um, Schließen lässt den Standard.

## [0.29.0] – 2026-10-11

### Hinzugefügt

- Darstellung → Modus: „Farbwelt hell“ und „Farbwelt dunkel“ (`ThemeSettings.lightWorld`/`darkWorld`, `LightWorld`,
  `DarkWorld`, `SurfaceColors`). Eine Farbwelt legt Grund, Karten, erhöhte Teile (Pille, Leisten), Linien und Text fest
  statt der Tönung durch die Akzentfarbe; ausgewählte Einträge werden zu weichen Tönen der Akzentfarbe. Hell: Getönt
  (wie bisher, Standard), Wolke, Papier, Nordschnee, Latte. Dunkel: Getönt (Standard), Graphit, Nordnacht, Mitternacht,
  Gedimmt. Die Auswahl zeigt kleine Vorschauen; ein Tipp stellt die Farben sofort um, die Farbwelt des gerade
  sichtbaren Modus steht oben. Akzent, Palette, Tönung, Helligkeitsregler und reines Schwarz wirken weiter. Wird
  gespeichert und mit der Sicherung exportiert.

## [0.28.0] – 2026-10-11

### Geändert

- Einfarbiger Hintergrund ist jetzt Standard (`ThemeSettings.backgroundGlow = false`), der Farbverlauf ist zuschaltbar.
  Gespeicherte Einstellungen von vor dieser Version stellen ein gespeichertes „an“ einmalig auf „aus“ (`look` = 3);
  was danach gewählt wird, bleibt.

## [0.27.0] – 2026-10-09

### Hinzugefügt

- Darstellung → Flächen: Regler „Kartenhelligkeit“ (`ThemeSettings.cardBrightness`, −1 bis 1, Standard 0). Verschiebt
  die Töne der Container, aus denen Karten, Gruppen und Leisten bestehen (dunkel bis 14 Töne heller und 8 dunkler, hell
  bis 16 heller, also fast weiß, und 6 dunkler); Hintergrund und Verlauf bleiben, wie sie sind. Zusammen mit dem
  ausgeschalteten Farbverlauf: einfarbiger Hintergrund, Karten in eigener Helligkeit. Im dunklen Modus gehen die
  Akzent-Container halb mit, damit Ausgewähltes sichtbar bleibt. Wird gespeichert und mit der Sicherung exportiert.
  `NeedanappDefaults.MAX_CARD_BRIGHTNESS`

### Geändert

- „Nur ein Abschnitt offen“ ist jetzt Standard (`ThemeSettings.exclusiveSections = true`). Gespeicherte Einstellungen
  von vorher, in denen es aus war, werden einmalig umgestellt (Schlüssel `look` = 2, auch in der Sicherung); was danach
  gewählt wird, bleibt.

Dieselben Optionen samt dem Schalter aus 0.26.0 gibt es für Myzel auf dem Zweig 0.21.x (v0.21.2), das beim Aussehen von
0.21 bleibt.

## [0.26.0] – 2026-10-08

### Hinzugefügt

- Darstellung → Flächen: Schalter „Farbverlauf im Hintergrund“ (`ThemeSettings.backgroundGlow`, Standard an). Aus
  bleibt der Hintergrund einfarbig, ohne den Schein der Akzentfarbe oben; Karten bleiben getönt. Im dunklen Modus
  mit reinem Schwarz ist er ohnehin aus. Wird gespeichert und mit der Sicherung exportiert.

## [0.25.0] – 2026-10-06

### Hinzugefügt

- `LoadingBar`: schmaler, durchlaufender Balken über die volle Breite, der anzeigt, dass etwas lädt. Blendet weich
  ein und aus und bleibt mindestens 700 ms stehen, damit eine schnelle Ladung nicht nur aufblitzt.
- `StatusBand` und `SectionCard` mit `loading`: Der Balken läuft dann in der Textfarbe des Bands an dessen Unterkante,
  etwa solange die Daten hinter dem Zustand abgefragt werden.

### Geändert

- `RefreshBox`: Der Kreisel beim Herunterziehen hat die Akzentfarben (`primaryContainer`); vorher hatte er die Farbe
  der Karten darunter und war kaum zu sehen.

## [0.24.1] – 2026-10-04

### Geändert

- `SwipeActionRow`: Der rote Knopf schiebt sich jetzt vom Zeilenende herein und drückt die Zeile dabei schmaler,
  statt über der stehenden Zeile eingeblendet zu werden. Die Zeile behält ihre Höhe; ihr Inhalt wird beim Stauchen
  notfalls abgeschnitten.

## [0.24.0] – 2026-10-04

### Hinzugefügt

- `SwipeActionRow`: eine Zeile, die sich beim Wischen zur Seite schiebt und dahinter einen roten Knopf (z. B.
  „Entfernen“) freigibt; erst ein Tipp darauf löst die Aktion aus. Gedacht für Einträge, die sich nicht durch einen
  versehentlichen zweiten Tipp auf einen Knopf in der Zeile entfernen lassen sollen. Ein Tipp auf die offene Zeile
  schließt sie wieder; TalkBack bietet die Aktion als eigene Aktion der Zeile an. Rechts-nach-links-Sprachen wischen
  spiegelverkehrt.
- Beispiel-App: Zeile „Zum Entfernen wischen“

## [0.23.0] – 2026-10-04

### Hinzugefügt

- `NavigationPill(trailingMenu = PillMenu(…))`: rechts neben der Pille ein runder Menüknopf, der nach oben ein Menü
  mit beschrifteten Einträgen aufklappt (z. B. Ordneroptionen und Einstellungen). Er steht dort, solange
  `trailingAction` leer ist; ein Kontext wie eine Auswahl kann den Platz also zeitweise mit einem eigenen Knopf
  belegen. Ein Eintrag, eine Seite der Pille, ein Tipp daneben oder Zurück klappen das Menü wieder zu, ebenso das
  Ausblenden der Pille. Der letzte Eintrag steht dem Knopf am nächsten.
- `PillMenu`: Symbol, Beschriftung und Einträge eines solchen Menüs
- Beispiel-App: Leiste „Menüknopf neben der Navigation“

### Geändert

- `NavigationPillWithMenu` nutzt intern dieselbe Abdeckung und dasselbe Menü; Aussehen und Verhalten bleiben gleich.

## [0.22.0] – 2026-10-03

### Hinzugefügt

- `MarqueeText`: eine Zeile Text, die nie abgeschnitten wird. Passt sie nicht, läuft sie durch, wartet am Anfang
  und wiederholt sich; solange sie nicht passt, blendet sie am Ende weich aus. Ohne Bewegung (Animationen aus oder
  Effekte reduziert) bricht sie um, mit `wrapWithoutMotion = false` lässt sie sich von Hand schieben.
- Zeilen der gruppierten Liste (`ListRow`, `SwitchRow`, `SegmentedRow`, `SliderRow`, `ContentRow`,
  `GroupRowSurface`) liegen in einer `SectionCard` flach auf der Karte, bündig mit dem Titel. Mit
  `fullWidthContent` reagieren sie bis zum Kartenrand auf Berührung.
- `SectionCardPadding`, der Einzug einer Karte, für eigene Inhalte in Karten mit `fullWidthContent`
- `LabeledRow(alignFirstLines = true)`: die Beschriftung steht auf der ersten Zeile eines umbrechenden Werts
- Beispiel-App: Karte mit Zeilen und Laufschrift

### Geändert

- `AppScaffold`: Titel und Untertitel laufen durch, statt mit „…“ gekürzt zu werden

## [0.21.1] – 2026-10-03

### Behoben

- Modus „System“ nach einem Wechsel zur Laufzeit: Wer von „Dunkel“ oder „Hell“ auf „System“ umstellte, behielt
  bis zum Neustart den alten Modus, und die App folgte danach keinem Wechsel des Systems. AppCompat hält die
  Activity auf dem Nachtmodus, mit dem sie entstand; das Theme liest den Systemmodus deshalb jetzt vom System
  selbst und hört auf dessen Änderungen. `isDarkTheme()` nutzt ebenfalls den Systemmodus.

## [0.21.0] – 2026-10-03

### Hinzugefügt

- `ThemeSettingsStore.cachedSettings`: die zuletzt gelesenen Einstellungen als Startwert, damit das erste Bild
  die App zeigt statt auf das Lesen zu warten, auch wenn eine Activity neu entsteht
- `ThemeSettingsStore.preload()`: liest die Einstellungen schon in `Application.onCreate` im Hintergrund

## [0.20.0] – 2026-10-03

### Hinzugefügt

- Kartenlook als festes Aussehen aller Apps: Flächen und Ränder zur Hauptfarbe getönt (dunkel 12 % bzw.
  18 %, danach zu Schwarz gezogen; hell nur die Container 18 % und der feine Rand 28 %), dahinter ein Verlauf
  mit einem Schein der Hauptfarbe oben.
- `DesignColors` und `LocalDesignColors` (Verlauf, Kartenfläche und -rand, Hinweistext, Grün für einen guten
  Zustand, Leisten), `Modifier.designBackground`, `DesignColors.backgroundBrush`
- Karten: `Modifier.designCard` (Fläche, Rand, Schatten), `SectionCard` mit `StatusBand`, `CardStatus` und
  `StatusTone`, `LabeledRow`, `TagPill`, `PillButton`, `ActionBar` mit `ActionBarButton` und `ActionBarSlot`
- Beispiel-App: Karte mit Band, Zeile, Pillen und Hauptknopf auf der Bausteinseite

### Geändert

- Standard-Palette ist Farbtreu (Fidelity) statt Tonal Spot, Standard-Kartenecken 14 statt 20 dp.
  Gespeicherte Werte auf den alten Standards werden einmalig umgestellt (Schlüssel `look`, auch in der
  Sicherung).
- `AppScaffold` zeichnet den Verlauf als Hintergrund; die Kopfzeile blendet von dessen oberster Farbe aus.

## [0.19.0] – 2026-10-03

### Hinzugefügt

- `NavigationPillWithMenu`: die Navigationspille zentriert unten, rechts daneben ein kleiner runder Menüknopf. Er
  klappt ein Menü mit weiteren Zielen (`PillAction`) nach oben auf, in der Form und mit dem Blur der Pille. Ein
  leichter Schleier dahinter, Tippen daneben oder Zurück schließt es; der Knopf zeigt dann ein Kreuz. Die Zähler
  der Menüeinträge erscheinen zusammen als Badge am Knopf. Labels zeigt die Pille nur, wenn sie trotz Knopf
  mittig Platz haben.
- Text `nd_menu_close` („Menü schließen“) für den Menüknopf
- Beispiel-App: Optionen-Eintrag „Leiste unten“ wechselt zwischen seitlichen Knöpfen, Werkzeugpille und Menü

### Geändert

- Die Pillen sind kleiner: 52 dp hoch statt 56 dp, Einträge 42 dp, weniger Innenabstand. Alle Maße stehen als
  Konstanten in `NavigationPill.kt`.
- `BottomPillSpacer()` ist entsprechend 96 dp statt 104 dp hoch

## [0.18.0] – 2026-10-03

### Hinzugefügt

- `NavigationPillWithToolbar`: die Navigationspille mit einer Werkzeugpille direkt daneben, für Ziele außerhalb der
  Navigation wie Einstellungen (zuerst für Myzel). Der aktive Eintrag zeigt sein Label nur, wenn das längste Label
  neben der Werkzeugpille Platz hat, damit es nicht zwischen den Seiten kommt und geht; sonst kennzeichnet ihn nur
  die primaryContainer-Fläche.
- `PillItem.badgeCount`: Zähler am Symbol eines Pilleneintrags (z. B. verfügbare Updates)
- Beispiel-App: Umschalter „Werkzeugpille neben der Navigation“ im Optionen-Blatt, Zähler am Eintrag „Über“

## [0.17.0] – 2026-10-02

### Geändert

- Einklappbare Abschnitte starten immer eingeklappt; nur der zuletzt geöffnete Abschnitt eines Bildschirms bleibt
  beim nächsten Besuch (auch nach einem Neustart) offen. `SectionState` hält die offenen Abschnitte nur noch für die
  Dauer eines Besuchs, gespeichert wird je Bildschirm der zuletzt geöffnete (`ThemeSettingsStore.openSections`,
  `rememberOpenSection`). Beim Einschalten von „Nur ein Abschnitt offen“ bleibt nur der zuletzt geöffnete offen.
- `AppearanceSettings`: Standard-Sprachenliste Englisch vor Deutsch (`languages = listOf("en", "de")`)
- README, Beispiel-App und Kommentare ohne Verweise auf andere Apps

### Entfernt

- `rememberSectionState(collapsedByDefault)` (alle Abschnitte starten eingeklappt) sowie
  `ThemeSettingsStore.collapsedSections`, `expandedSections`, `setSectionCollapsed`, `setSectionsCollapsed`,
  `expandOnlySection` und `updateSections`. Die alten Schlüssel `collapsed_sections`/`expanded_sections` werden beim
  nächsten Speichern gelöscht.

## [0.16.0] – 2026-10-02

### Hinzugefügt

- `ThemeSettingsStore.exportBackup()` und `importBackup(json)`: die Darstellung als JSON für Sicherungen der Apps –
  alle `ThemeSettings`, eine importierte Schrift (Base64, bis 20 MB) und die App-Sprache. Eingeklappte Abschnitte
  bleiben gerätebezogen draußen. Der Import ersetzt die Darstellung, prüft Wertebereiche und Schriftdatei;
  fehlende oder unbekannte Werte bekommen ihre Standardwerte. Wie die Sicherung gespeichert und geschützt wird,
  entscheidet die App.
- Beispiel-App: „Darstellung kopieren/einfügen“ über die Zwischenablage im Optionen-Sheet.

## [0.15.0] – 2026-10-01

### Entfernt

- Hintergrundtransparenz samt Hintergrund-Blur (seit 0.10.0/0.11.0): `ThemeSettings.backgroundTransparency`,
  `ThemeSettings.backgroundBlur`, `DesignEffects.backgroundOpacity`, `NeedanappDefaults.MAX_BACKGROUND_TRANSPARENCY` und
  `Activity.prepareWindowForTransparency()`. Die übersetzte Activity und der System-Blur hinter dem Fenster machten
  Probleme (Launcher blenden ihre Icons aus, der Blur wirkte je nach Zwischenspeicherung des Systems unterschiedlich
  stark). Der App-Hintergrund ist wieder deckend; gespeicherte Werte werden beim nächsten Schreiben gelöscht.

## [0.14.0] – 2026-10-01

### Hinzugefügt

- `SettingsGroup(block = …)`: aufeinanderfolgende Gruppen bilden einen Block; sie schließen mit dem kleinen
  Zeilenabstand aneinander an, stark gerundet sind nur Anfang und Ende des Blocks. `RowPosition.of(isTop, isBottom)`,
  `joinsAbove` und `joinsBelow` helfen bei der Berechnung
- Darstellung: alle Abschnitte bilden einen Block

### Geändert

- Kopf einklappbarer Abschnitte: eine Zeile wie die übrigen (Karte `surfaceContainer`, Icon, Titel, Pfeil am Ende)
  statt farbiger Akkordeon-Karte mit Kreisen; aufgeklappt hängen die Zeilen direkt darunter, statt dass der Kopf zur
  Überschrift wird

## [0.13.0] – 2026-10-01

### Hinzugefügt

- `rememberSectionState(ids, collapsedByDefault)`: Abschnitte, die anfangs eingeklappt sind (z. B. „Erweitert“); geöffnet
  merkt sie sich der DataStore in einer eigenen Menge (`expanded_sections`)
- `ThemeSettingsStore.updateSections(collapse, expand, collapsedByDefault)`: klappt Abschnitte in einem Schritt zu und
  auf; `setSectionCollapsed`, `setSectionsCollapsed` und `expandOnlySection` laufen darüber

## [0.12.0] – 2026-10-01

### Hinzugefügt

- Darstellung → Modus: „Helligkeit im Dunkelmodus“ (0–100 %): hebt Hintergrund, Flächen und Karten des dunklen Designs
  an (bis etwa Ton 16 für den Hintergrund und 28–36 für Karten), die Akzent-Container gehen mit, damit Ausgewähltes
  sichtbar bleibt. 0 % entspricht dem bisherigen Aussehen, nicht mit reinem Schwarz. `ThemeSettings.darkBrightness`
- Einklappbare Abschnitte als Akkordeon: eingeklappt eine runde Karte (`secondaryContainer`) mit Symbol im hellen Kreis
  und Pfeil, aufgeklappt eine Überschrift in Akzentfarbe; der Wechsel ist animiert. `SettingsGroup(icon = …)`; Gruppen
  ohne Einklappen zeigen das Symbol neben dem Titel
- Darstellung → Bedienung: „Nur ein Abschnitt offen“ (exklusive Akkordeons). `ThemeSettings.exclusiveSections`,
  `SectionState.isExclusive`, `ThemeSettingsStore.expandOnlySection()`
- `rememberSectionState(ids)`: die Abschnitte eines Bildschirms, damit exklusives Aufklappen auch noch nicht
  angezeigte Abschnitte einer Lazy-Liste zuklappt

### Geändert

- Die Abschnitte der Darstellung haben Symbole; „Alle ausklappen“ entfällt bei exklusiven Abschnitten

## [0.11.0] – 2026-10-01

### Geändert

- „Hintergrundtransparenz“ zeigt jetzt den ganzen Startbildschirm samt Icons hinter der App (bzw. die App, aus der sie
  geöffnet wurde) statt nur das Hintergrundbild: Die Activity wird per `Activity.setTranslucent(true)` durchscheinend,
  solange die Transparenz über 0 steht. Manche Launcher blenden ihre Icons aus, sobald eine App im Vordergrund ist
- Der Blur dahinter nutzt `FLAG_BLUR_BEHIND` (`blurBehindRadius`), weil der Fenster-Hintergrund-Blur nur bei im
  Manifest durchscheinend deklarierten Themes greift
- `ThemeSettings.wallpaperBlur` heißt jetzt `backgroundBlur` („Hintergrund verwischen“); der gespeicherte Wert wird
  übernommen

## [0.10.0] – 2026-10-01

### Hinzugefügt

- Darstellung → Flächen: „Hintergrundtransparenz“ (0–80 %): Das Hintergrundbild des Geräts scheint durch die App
  (`FLAG_SHOW_WALLPAPER`, Hintergrund von `AppScaffold` entsprechend durchsichtig; Sheets, Dialoge und eigene
  Hintergründe wie Schwarz hinter Bildern bleiben deckend). `ThemeSettings.backgroundTransparency`,
  `DesignEffects.backgroundOpacity`
- „Hintergrundbild verwischen“ (Milchglas über den Fenster-Blur des Systems); `ThemeSettings.wallpaperBlur`. Aus bei
  „Effekte reduzieren“, ausgegraut mit Hinweis, wenn das System gerade keinen Blur erlaubt (z. B. Energiesparmodus)
- `Activity.prepareWindowForTransparency()`: in `onCreate` vor `setContent` aufrufen, damit das Fenster ab dem ersten
  Bild durchsichtig ist. Sonst kann nach einem Kaltstart das erste Bild hinter dem System-Blur stehen bleiben
  (versetzte „Geisterliste“)

### Entfernt

- Verlaufshintergrund: `ThemeSettings.gradientBackground`, `DesignEffects.gradientBackground` und die Einstellung;
  der gespeicherte Wert wird beim nächsten Speichern gelöscht

### Geändert

- `NeedanappTheme` wendet die Fenster-Einstellungen bei jedem `ON_RESUME` an, weil manche Activities (z. B. ältere View-Activities)
  dort ihren Fensterhintergrund selbst setzen
- Kopfzeile ohne Blur bleibt deckend, damit der Titel auch über dem Hintergrundbild lesbar ist
- „Effekte reduzieren“ beschreibt sich ohne Verläufe

## [0.9.0] – 2026-10-01

### Hinzugefügt

- Darstellung → Effekte: „Transparenz der Pille“ (0–80 %, Standard 45 % wie bisher), wie stark der verwischte
  Inhalt durch Navigations-/Werkzeug-Pille und ihre Seitenknöpfe scheint; `ThemeSettings.pillTransparency`,
  `DesignEffects.pillOpacity`. Wirkt nur mit Blur, ohne Blur bleibt die Pille deckend (Lesbarkeit). Hohe Werte
  sind über hellen Bildern schlechter lesbar, darauf weist der Text der Einstellung hin

### Behoben

- `AppScaffold`: Der Hintergrund gehört jetzt zur Blur-Quelle. Vorher füllten Pille und Seitenknöpfe leere
  Stellen hinter sich mit ihrer eigenen Farbe, sodass sie über Listen praktisch deckend wirkten; ein
  Verlaufshintergrund wird jetzt ebenfalls mit verwischt
- `FloatingSurface`: Rückfallfarbe unter dem Blur ist der App-Hintergrund statt der Pillenfarbe; die Deckkraft
  kommt aus der Einstellung statt fest 55 %

## [0.8.1] – 2026-09-30

### Geändert

- `AppearanceSettings`: Gibt die App höchstens eine Sprache an (`languages`), fehlt der Abschnitt „Sprache“, denn dort
  gäbe es nichts zu wählen (z. B. bei einer App nur auf Deutsch)

## [0.8.0] – 2026-09-30

### Hinzugefügt

- `ToolbarPill`: schwebende Pille mit reinen Aktionen (z. B. Bearbeiten, Teilen, Löschen bei einem Bild), gleiche
  Optik, Seitenknöpfe und Ein-/Ausblenden wie `NavigationPill`, aber ohne „aktiven“ Eintrag
- `PillAction.enabled`: Knöpfe lassen sich ausgrauen
- `AppScaffold`: `headerVisible` (Header gleitet für eine Vollbildansicht weg) und `backgroundColor` (eigener
  Hintergrund, z. B. Schwarz hinter Bildern)
- Beispiel-App: „Vollbildansicht“ mit Werkzeug-Pille; Tippen blendet Header und Pille aus

## [0.7.1] – 2026-09-30

### Geändert

- `EmptyState`: Titel und Erklärung zentriert (längere Texte, z. B. bei Berechtigungen, stehen sonst linksbündig
  unter dem zentrierten Symbol)

## [0.7.0] – 2026-09-30

### Hinzugefügt

- `AppScaffold`: `searchActions` (Knöpfe bzw. Anzeige neben dem Suchfeld, z. B. Trefferzahl, vorheriger/nächster
  Treffer) und `onSearchSubmit` (Suchtaste der Tastatur)

## [0.6.1] – 2026-09-30

### Geändert

- `ContentDialog`: mit drittem Knopf stehen die Knöpfe untereinander (rechtsbündig), statt unschön umzubrechen

## [0.6.0] – 2026-09-30

### Hinzugefügt

- `ContentDialog`: Dialog mit eigenem Inhalt (kleines Formular, Detailliste), scrollt bei langem Inhalt; Bestätigen
  schließt nicht automatisch (Eingaben lassen sich vorher prüfen), optional dritter Knopf links (z. B. „Überspringen“)
- `DetailRow`: Angabe als kleines Label über dem Wert, optional in Festbreitenschrift (Fingerabdrücke, Prüfsummen)
  und kopierbar
- `SecretTextField`: einzelnes Passwortfeld (z. B. im Dialog) mit Anzeigen/Verbergen
- Beispiel-App: „Dialog zeigen“ mit allen drei Bausteinen

## [0.5.0] – 2026-09-30

### Hinzugefügt

- `InfoDialog`: Hinweis mit nur „OK“, langer Text scrollt im Dialog (z. B. Lizenztexte)

## [0.4.0] – 2026-09-30

### Hinzugefügt

- `TextFieldRow`: Textfeld als Zeile einer gruppierten Liste für Formulare, mit Fehlertext und – für Passwörter –
  verdecktem Inhalt samt Anzeigen/Verbergen
- Texte `nd_show_secret`, `nd_hide_secret` (en/de)

## [0.3.0] – 2026-09-30

### Hinzugefügt

- `ActionPill`: die Pille für Abläufe ohne Navigation (z. B. wenn eine andere App eine Datei auswählt) – eine
  Hauptaktion im Stil des aktiven Eintrags oder nur ein Hinweis, kleine Knöpfe links/rechts wie bei
  `NavigationPill`

## [0.2.0] – 2026-09-30

### Hinzugefügt

- `ConfirmDialog` (optional destruktiv) und `TextInputDialog` (mit Prüfung und Vorauswahl) im Stil des Pakets
- `RefreshBox`: Pull-to-Refresh, dessen Anzeige unter der Kopfzeile erscheint
- `AppScaffold`: Untertitel und eine zusätzliche Zeile unter dem Titel (`headerBottom`, z. B. Pfad-Leiste);
  die Kopfzeile misst ihre Höhe selbst, der Inhalt rückt entsprechend ein
- `isDarkTheme()` und `buildColorScheme()` öffentlich, damit Apps die Farben auch an andere Theme-Systeme
  weitergeben können
- Texte `nd_ok`, `nd_cancel` (en/de)

### Geändert

- `SegmentedRow`: Titel ist optional

## [0.1.0] – 2026-09-30

Erste Version.

### Hinzugefügt

- Theme für Compose (`NeedanappTheme`) und Views (`Theme.Needanapp`, `NeedanappViewTheme`) aus denselben
  Einstellungen
- Darstellungs-Einstellungen mit Speicherung im DataStore: Modus, reines Schwarz, Akzentquelle (Standard,
  Material You, eigene Farbe mit Farbwähler und gespeicherten Farben, 9 Vorlagen), 9 Paletten-Stile,
  Flächen-Tönung, Verlaufshintergrund, progressiver Blur, „Effekte reduzieren“, UI-Skalierung, Kartenecken,
  eigene Schriftart, Sprache
- Fertiger Bildschirm `AppearanceSettings` mit einklappbaren Abschnitten und „Alle einklappen“
- `AppScaffold`: Edge-to-Edge, Inhalt scrollt unter die Kopfzeile, progressiver Blur, Suche als Feld im Header
- `NavigationPill`: schwebende Pille mit animiertem aktivem Eintrag, kleine Knöpfe links/rechts mit Zähler,
  Hintergrund-Blur, blendet sich beim Runterscrollen und bei offener Tastatur aus
- Gruppierte Listen im Expressive-Stil: `SettingsGroup`, `ListRow`, `SwitchRow`, `SliderRow`, `SegmentedRow`,
  `ContentRow`
- `OptionsSheet` und `SheetAction` (Bottom-Sheets statt Dialogen), `EmptyState`, `ColorPicker`
- Alle Texte auf Englisch und Deutsch
- Beispiel-App `sample`
