package de.needanapp.design.sample

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.text.format.Formatter
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lan
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.needanapp.design.R as DesignR
import de.needanapp.design.components.ActionTile
import de.needanapp.design.components.AppScaffold
import de.needanapp.design.components.BottomPillSpacer
import de.needanapp.design.components.CardAction
import de.needanapp.design.components.CardActionGroup
import de.needanapp.design.components.CardMenu
import de.needanapp.design.components.CardSpacing
import de.needanapp.design.components.CardStatus
import de.needanapp.design.components.CardTileGroups
import de.needanapp.design.components.ChipRow
import de.needanapp.design.components.ChoiceTile
import de.needanapp.design.components.ContentDialog
import de.needanapp.design.components.Detail
import de.needanapp.design.components.DetailTile
import de.needanapp.design.components.EmptyState
import de.needanapp.design.components.FloatingItemCard
import de.needanapp.design.components.FloatingSurface
import de.needanapp.design.components.ItemCard
import de.needanapp.design.components.ListRow
import de.needanapp.design.components.LoadingState
import de.needanapp.design.components.MarqueeText
import de.needanapp.design.components.NavigationPill
import de.needanapp.design.components.OptionsSheet
import de.needanapp.design.components.PillAction
import de.needanapp.design.components.PillBar
import de.needanapp.design.components.PillDefaults
import de.needanapp.design.components.PillItem
import de.needanapp.design.components.PillMenu
import de.needanapp.design.components.SecretTextField
import de.needanapp.design.components.SegmentedRow
import de.needanapp.design.components.SelectionCard
import de.needanapp.design.components.SelectionMark
import de.needanapp.design.components.SettingsGroup
import de.needanapp.design.components.SignalButton
import de.needanapp.design.components.SliderRow
import de.needanapp.design.components.StatusTile
import de.needanapp.design.components.StatusTone
import de.needanapp.design.components.StepperTile
import de.needanapp.design.components.SurfaceProgress
import de.needanapp.design.components.SwipeActionRow
import de.needanapp.design.components.SwitchRow
import de.needanapp.design.components.SwitchTile
import de.needanapp.design.components.TabChip
import de.needanapp.design.components.TileCard
import de.needanapp.design.components.TileCardAction
import de.needanapp.design.components.TileGroup
import de.needanapp.design.components.ToggleTile
import de.needanapp.design.components.detailLines
import de.needanapp.design.components.rememberCompactTileLines
import de.needanapp.design.components.rememberPillScrollBehavior
import de.needanapp.design.components.rememberSearchState
import de.needanapp.design.components.scrollbar
import de.needanapp.design.settings.AppearanceSettings
import de.needanapp.design.theme.DesignStyle
import de.needanapp.design.theme.LocalDesignColors
import de.needanapp.design.theme.NeedanappTheme
import de.needanapp.design.theme.ThemeSettings
import de.needanapp.design.theme.ThemeSettingsStore
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val store = ThemeSettingsStore(this)
        setContent {
            val settings by store.settings.collectAsStateWithLifecycle(initialValue = store.cachedSettings)
            // wait for the stored settings instead of flashing the defaults
            settings?.let { NeedanappTheme(it) { SampleApp(it) } }
        }
    }
}

private enum class Page { COMPONENTS, APPEARANCE, ABOUT }

/** The arrangements of the bottom bar the sample shows, switched in the options sheet. */
private enum class BarStyle(@StringRes val label: Int) {
    SIDE_BUTTONS(R.string.sample_bar_side_buttons),
    SIDE_MENU(R.string.sample_bar_side_menu),
    SELECTION_CARD(R.string.sample_bar_selection_card),
}

@Composable
private fun SampleApp(settings: ThemeSettings) {
    var showViewer by rememberSaveable { mutableStateOf(false) }
    var showOnboarding by rememberSaveable { mutableStateOf(false) }
    if (showViewer) {
        ViewerDemo(onClose = { showViewer = false })
        return
    }
    if (showOnboarding) {
        OnboardingDemo(settings, onClose = { showOnboarding = false })
        return
    }
    var page by rememberSaveable { mutableStateOf(Page.COMPONENTS) }
    var sheet by rememberSaveable { mutableStateOf<String?>(null) }
    var barStyle by rememberSaveable { mutableStateOf(BarStyle.SIDE_BUTTONS) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val search = rememberSearchState()
    val pillScroll = rememberPillScrollBehavior()
    val entries = (1..30).map { stringResource(R.string.sample_entry, it) }
    val matches = entries.filter { search.query.isBlank() || it.contains(search.query, ignoreCase = true) }
    val pages = listOf(
        PillItem(Icons.Outlined.Widgets, stringResource(R.string.sample_page_components)),
        PillItem(Icons.Outlined.Palette, stringResource(DesignR.string.nd_appearance)),
        PillItem(Icons.Outlined.Info, stringResource(R.string.sample_page_about), badgeCount = 1),
    )

    AppScaffold(
        title = pages[page.ordinal].label,
        search = search.takeIf { page == Page.COMPONENTS },
        searchActions = {
            if (search.query.isNotBlank()) {
                Text(
                    pluralStringResource(R.plurals.sample_matches, matches.size, matches.size),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 16.dp),
                )
            }
        },
        bottomBar = { hazeState ->
            val onSelect = { index: Int ->
                page = Page.entries[index]
                pillScroll.show()
            }
            val add = PillAction(Icons.Outlined.Add, stringResource(R.string.sample_add)) { sheet = SHEET_ADD }
            val options = PillAction(Icons.Outlined.Tune, stringResource(R.string.sample_options), badgeCount = 2) { sheet = SHEET_OPTIONS }
            when (barStyle) {
                BarStyle.SIDE_BUTTONS -> NavigationPill(
                    items = pages,
                    selectedIndex = page.ordinal,
                    onSelect = onSelect,
                    hazeState = hazeState,
                    leadingAction = add,
                    trailingAction = options,
                    visible = pillScroll.isVisible,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
                // a button on the left, a menu button on the right that unfolds upwards
                BarStyle.SIDE_MENU -> NavigationPill(
                    items = pages,
                    selectedIndex = page.ordinal,
                    onSelect = onSelect,
                    hazeState = hazeState,
                    // the plus turns the pill into a bar of what can be created
                    leadingBar = PillBar(
                        Icons.Outlined.Add,
                        stringResource(R.string.sample_add),
                        listOf(
                            listOf(
                                PillAction(Icons.Outlined.CreateNewFolder, stringResource(R.string.sample_new_folder), shortLabel = stringResource(R.string.sample_folder)) {},
                                PillAction(Icons.Outlined.NoteAdd, stringResource(R.string.sample_new_file), shortLabel = stringResource(R.string.sample_file)) {},
                            ),
                            listOf(PillAction(Icons.Outlined.Share, stringResource(R.string.sample_share)) {}),
                        ),
                        caption = stringResource(R.string.sample_bar_caption),
                    ),
                    trailingMenu = PillMenu(
                        Icons.Outlined.Menu,
                        stringResource(R.string.sample_menu),
                        listOf(PillAction(Icons.Outlined.Share, stringResource(R.string.sample_share)) {}, options),
                    ),
                    visible = pillScroll.isVisible,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
                // what a list shows while entries are selected: their actions on a floating card
                BarStyle.SELECTION_CARD -> {
                    var expanded by rememberSaveable { mutableStateOf(false) }
                    val selected = entries.take(3)
                    SelectionCard(
                        count = selected.size,
                        summary = pluralStringResource(
                            R.plurals.sample_selection_summary,
                            selected.size,
                            selected.size,
                            Formatter.formatShortFileSize(context, SAMPLE_SELECTION_BYTES),
                        ),
                        names = selected.joinToString(", "),
                        menu = CardMenu(
                            actions = listOf(
                                CardAction(Icons.Outlined.ContentCopy, stringResource(R.string.sample_copy)) {},
                                CardAction(Icons.Outlined.Tune, stringResource(R.string.sample_bar_style)) { sheet = SHEET_OPTIONS },
                                CardAction(Icons.Outlined.Delete, stringResource(R.string.sample_delete), isDestructive = true) {},
                                CardAction(Icons.Outlined.MoreHoriz, stringResource(R.string.sample_more), isActive = expanded) { expanded = !expanded },
                            ),
                            groups = sampleActionGroups(),
                        ),
                        expanded = expanded,
                        onFold = { expanded = false },
                        onSelectAll = { Toast.makeText(context, R.string.sample_selected_all, Toast.LENGTH_SHORT).show() },
                        visible = true,
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }
        },
    ) { padding ->
        when (page) {
            Page.COMPONENTS -> ComponentsPage(padding, search.query, matches, Modifier.nestedScroll(pillScroll.nestedScrollConnection), onOpenOnboarding = { showOnboarding = true }) { showViewer = true }
            Page.APPEARANCE -> AppearanceSettings(
                settings,
                contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding() + 96.dp),
                nestedScrollConnection = pillScroll.nestedScrollConnection,
                styles = listOf(DesignStyle.NEEDANAPP, DesignStyle.MYZEL),
            )
            Page.ABOUT -> AboutPage(padding)
        }
    }

    when (sheet) {
        SHEET_ADD -> OptionsSheet(onDismiss = { sheet = null }, title = stringResource(R.string.sample_add)) {
            // actions in a sheet are tiles, two side by side
            TileCard(null, CardSpacing) {
                line(
                    { m, shape -> ActionTile(stringResource(R.string.sample_new_folder), { sheet = null }, shape, m, Icons.Outlined.CreateNewFolder, centered = true) },
                    { m, shape -> ActionTile(stringResource(R.string.sample_new_file), { sheet = null }, shape, m, Icons.Outlined.Inventory2, centered = true) },
                )
            }
        }
        SHEET_OPTIONS -> OptionsSheet(onDismiss = { sheet = null }, title = stringResource(R.string.sample_options)) {
            TileCard(null, CardSpacing) {
                line({ m, shape ->
                    ActionTile(stringResource(R.string.sample_view), { sheet = null }, shape, m, Icons.Outlined.ViewAgenda, summary = stringResource(R.string.sample_view_list), inline = true)
                })
                line({ m, shape ->
                    ActionTile(stringResource(R.string.sample_bar_style), {
                        sheet = null
                        barStyle = BarStyle.entries[(barStyle.ordinal + 1) % BarStyle.entries.size]
                    }, shape, m, Icons.Outlined.Tune, summary = stringResource(barStyle.label), inline = true)
                })
                line(
                    { m, shape -> ActionTile(stringResource(R.string.sample_copy), { sheet = null }, shape, m, Icons.Outlined.ContentCopy, centered = true) },
                    { m, shape -> ActionTile(stringResource(R.string.sample_share), { sheet = null }, shape, m, Icons.Outlined.Share, centered = true) },
                )
                // the appearance backup as JSON; a real app would encrypt it and save it as a file
                line(
                    { m, shape ->
                        ActionTile(stringResource(R.string.sample_backup_copy), {
                            sheet = null
                            scope.launch {
                                val json = ThemeSettingsStore(context).exportBackup().toString()
                                context.getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText(null, json))
                                Toast.makeText(context, R.string.sample_backup_copied, Toast.LENGTH_SHORT).show()
                            }
                        }, shape, m, Icons.Outlined.Backup, centered = true)
                    },
                    { m, shape ->
                        ActionTile(stringResource(R.string.sample_backup_paste), {
                            sheet = null
                            val text = context.getSystemService(ClipboardManager::class.java).primaryClip?.getItemAt(0)?.coerceToText(context)?.toString()
                            scope.launch {
                                val json = runCatching { JSONObject(text.orEmpty()) }.getOrNull()
                                if (json == null) {
                                    Toast.makeText(context, R.string.sample_backup_invalid, Toast.LENGTH_SHORT).show()
                                } else {
                                    ThemeSettingsStore(context).importBackup(json)
                                }
                            }
                        }, shape, m, Icons.Outlined.Restore, centered = true)
                    },
                )
                line({ m, shape -> ActionTile(stringResource(R.string.sample_delete), { sheet = null }, shape, m, Icons.Outlined.Delete, inline = true, destructive = true) })
            }
        }
    }
}

/** The card look: a card of tiles with its state and facts, one with a switch and actions, then the other demos. */
@Composable
private fun CardsDemo() {
    TileCard(stringResource(R.string.sample_card_title), CardSpacing) {
        line({ m, shape -> StatusTile(CardStatus(stringResource(R.string.sample_card_status), Icons.Rounded.Verified, StatusTone.POSITIVE), shape, m) })
        detailLines(
            listOf(
                Detail(stringResource(R.string.sample_card_version), "1.2.3"),
                Detail(stringResource(R.string.sample_sort_size), "2,4 MB"),
                Detail(stringResource(R.string.sample_detail_fingerprint), "SHA256:3q2+7w8n4yVh0kQm1XcZp9bR", isCode = true),
            ),
        )
    }
    var enabled by remember { mutableStateOf(true) }
    TileCard(stringResource(R.string.sample_card_controls), CardSpacing) {
        line({ m, shape -> SwitchTile(stringResource(R.string.sample_switch), enabled, { enabled = it }, shape, m, Icons.Outlined.Info) })
        line(
            { m, shape -> ActionTile(stringResource(R.string.sample_card_open), {}, shape, m, Icons.Outlined.FolderOpen, inline = true) },
            { m, shape -> ActionTile(stringResource(R.string.sample_delete), {}, shape, m, Icons.Outlined.Delete, inline = true, destructive = true) },
        )
    }
    DemoHeading(R.string.sample_chips)
    ChipsDemo()
    DemoHeading(R.string.sample_tabs)
    TabsDemo()
    DemoHeading(R.string.sample_places)
    PlacesDemo()
    DemoHeading(R.string.sample_action_card)
    ActionCardDemo()
    DemoHeading(R.string.sample_pill_bar)
    PillBarDemo(Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
    DemoHeading(R.string.sample_progress)
    ProgressDemo()
    DemoHeading(R.string.sample_loading)
    LoadingState(stringResource(R.string.sample_loading_title), icon = Icons.Outlined.Lan)
    DemoHeading(R.string.sample_tiles)
    TilesDemo(Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
}

/** A floating bar that is its own progress bar, e.g. of a copy; the slider sets how far it is, the switch makes it unknown. */
@Composable
private fun ProgressDemo() {
    var done by remember { mutableFloatStateOf(0.4f) }
    var unknown by remember { mutableStateOf(false) }
    FloatingSurface(
        RoundedCornerShape(24.dp),
        hazeState = null,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
        progress = if (unknown) SurfaceProgress.Unknown else SurfaceProgress.Fraction(done),
    ) {
        Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 10.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.sample_progress_title), style = MaterialTheme.typography.titleSmall)
                Text(stringResource(R.string.sample_progress_file), style = MaterialTheme.typography.bodySmall, color = LocalDesignColors.current.onPillVariant)
            }
            FilledTonalButton(onClick = {}, colors = PillDefaults.tonalButtonColors()) { Text(stringResource(DesignR.string.nd_cancel)) }
        }
    }
    SettingsGroup(null) {
        row {
            SliderRow(
                it,
                stringResource(R.string.sample_progress_done),
                done,
                onValueChange = { value -> done = value },
                valueRange = 0f..1f,
                valueText = stringResource(DesignR.string.nd_percent, (done * 100).toInt()),
                enabled = !unknown,
            )
        }
        row { SwitchRow(it, stringResource(R.string.sample_progress_unknown), unknown, onCheckedChange = { value -> unknown = value }) }
    }
}

/** Filters as a line of chips that scrolls sideways, one of them chosen. */
@Composable
private fun ChipsDemo() {
    val kinds = listOf(
        R.string.sample_chip_all to Icons.Outlined.Apps,
        R.string.sample_chip_pictures to Icons.Outlined.Image,
        R.string.sample_chip_videos to Icons.Outlined.Movie,
        R.string.sample_chip_documents to Icons.Outlined.Description,
        R.string.sample_chip_music to Icons.Outlined.MusicNote,
        R.string.sample_chip_archives to Icons.Outlined.FolderZip,
    )
    var chosen by remember { mutableIntStateOf(0) }
    ChipRow(kinds.indices.toList()) { index ->
        val (label, icon) = kinds[index]
        FilterChip(
            selected = chosen == index,
            onClick = { chosen = index },
            label = { Text(stringResource(label)) },
            leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) },
        )
    }
}

/** Tabs as chips: a tap chooses one, a long press tells which one was held, e.g. to show its whole path. */
@Composable
private fun TabsDemo() {
    val tabs = listOf(
        R.string.sample_tab_places to Icons.Outlined.Home,
        R.string.sample_tab_phone to Icons.Outlined.PhoneAndroid,
        R.string.sample_tab_camera to Icons.Outlined.PhoneAndroid,
    )
    var chosen by remember { mutableIntStateOf(0) }
    var held by remember { mutableStateOf<Int?>(null) }
    ChipRow(tabs.indices.toList()) { index ->
        val (label, icon) = tabs[index]
        TabChip(
            label = stringResource(label),
            selected = chosen == index,
            onClick = { chosen = index },
            icon = icon,
            onLongClick = { held = index },
            onLongClickLabel = stringResource(R.string.sample_tab_path),
        )
    }
    Text(
        held?.let { stringResource(R.string.sample_tab_held, stringResource(tabs[it].first)) } ?: stringResource(R.string.sample_tab_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
    )
}

/**
 * Places as small tiles in groups: a storage with how full it is, servers with "Add" beside the title, as many to a
 * line as the names allow without breaking a word.
 */
@Composable
private fun PlacesDemo() {
    TileGroup(stringResource(R.string.sample_places_device), Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        line({ m, shape ->
            ActionTile(
                stringResource(R.string.sample_tab_phone),
                {},
                shape,
                m,
                Icons.Outlined.PhoneAndroid,
                summary = stringResource(R.string.sample_places_free),
                inline = true,
                fill = 0.32f,
                compact = true,
            )
        })
    }
    val servers = listOf(
        stringResource(R.string.sample_server_nas) to "SMB",
        stringResource(R.string.sample_server_web) to "SFTP",
        stringResource(R.string.sample_server_cloud) to "WebDAV",
        stringResource(R.string.sample_server_media) to "SMB",
    )
    val width = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.width.toDp() } - 32.dp
    val lines = rememberCompactTileLines(servers, width, most = 3) { it }
    TileGroup(
        stringResource(R.string.sample_places_servers),
        Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        TileCardAction(stringResource(R.string.sample_places_add), Icons.Outlined.Add) {},
    ) {
        lines.forEach { line ->
            line(
                line.map { (name, protocol) ->
                    { m: Modifier, shape: Shape ->
                        ActionTile(name, {}, shape, m, Icons.Outlined.Lan, summary = protocol, inline = true, compact = true)
                    }
                },
            )
        }
    }
}

/** Tiles: choices in lines, a label between them, a switch tile, a stepper and large destination tiles. */
@Composable
private fun TilesDemo(modifier: Modifier) {
    var choice by remember { mutableIntStateOf(0) }
    var descending by remember { mutableStateOf(false) }
    var on by remember { mutableStateOf(true) }
    var steps by remember { mutableIntStateOf(2) }
    val choices = listOf(
        R.string.sample_tile_name to Icons.Outlined.SortByAlpha,
        R.string.sample_tile_date to Icons.Outlined.CalendarMonth,
        R.string.sample_tile_size to Icons.Outlined.Straighten,
    )
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TileCard(stringResource(R.string.sample_tiles_choices)) {
            choices.forEachIndexed { index, (label, icon) ->
                line({ m, shape -> ChoiceTile(stringResource(label), choice == index, { choice = index }, shape, m, icon = icon) })
            }
            label(stringResource(R.string.sample_tiles_direction))
            line(
                { m, shape -> ChoiceTile(stringResource(R.string.sample_tile_up), !descending, { descending = false }, shape, m, Icons.Outlined.ArrowUpward, stacked = true) },
                { m, shape -> ChoiceTile(stringResource(R.string.sample_tile_down), descending, { descending = true }, shape, m, Icons.Outlined.ArrowDownward, stacked = true) },
            )
            line({ m, shape -> ToggleTile(stringResource(R.string.sample_switch), on, { on = it }, shape, m, Icons.Outlined.Info, summary = stringResource(R.string.sample_row_title)) })
            line({ m, shape ->
                StepperTile(
                    pluralStringResource(R.plurals.sample_tile_columns, steps, steps),
                    onDecrease = if (steps > 1) ({ steps-- }) else null,
                    onIncrease = if (steps < 6) ({ steps++ }) else null,
                    decreaseLabel = stringResource(R.string.sample_tile_fewer),
                    increaseLabel = stringResource(R.string.sample_tile_more),
                    shape = shape,
                    modifier = m,
                )
            })
        }
        TileCard(stringResource(R.string.sample_tiles_destinations)) {
            line(
                { m, shape -> ActionTile(stringResource(R.string.sample_tile_general), {}, shape, m, Icons.Outlined.Tune, summary = stringResource(R.string.sample_tile_general_summary), centered = true) },
                { m, shape -> ActionTile(stringResource(R.string.sample_swipe_remove), {}, shape, m, Icons.Outlined.Delete, centered = true, destructive = true) },
            )
        }
    }
}

@Composable
private fun DemoHeading(@StringRes text: Int) {
    Text(
        stringResource(text),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
    )
}

/** An entry as an [ItemCard] that opens with a tap: check mark in place of its icon, its actions, "more" unfolding groups. */
@Composable
private fun ActionCardDemo() {
    var open by remember { mutableStateOf(false) }
    var more by remember { mutableStateOf(false) }
    ItemCard(
        title = stringResource(R.string.sample_action_entry),
        subtitle = stringResource(R.string.sample_action_entry_details),
        onClick = {
            open = !open
            more = false
        },
        leading = { EntryIcon(open, Icons.Outlined.Image) },
        selected = open,
        menu = if (open) sampleCardMenu(more) { more = !more } else null,
        expanded = more,
    )
}

/** An entry's icon, a check mark while its card is open. */
@Composable
private fun EntryIcon(selected: Boolean, icon: ImageVector) {
    if (selected) {
        SelectionMark(Modifier.size(36.dp))
    } else {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp).padding(4.dp))
    }
}

/** The actions of an opened entry; "more" shows [more] and runs [onMore]. */
@Composable
private fun sampleCardMenu(more: Boolean, onMore: () -> Unit) = CardMenu(
    actions = listOf(
        CardAction(Icons.Outlined.ContentCopy, stringResource(R.string.sample_copy)) {},
        CardAction(Icons.Outlined.Share, stringResource(R.string.sample_share)) {},
        CardAction(Icons.Outlined.Delete, stringResource(R.string.sample_delete), isDestructive = true) {},
        CardAction(Icons.Outlined.MoreHoriz, stringResource(R.string.sample_more), isActive = more, onClick = onMore),
    ),
    groups = sampleActionGroups(),
)

/** The further actions of the demo card and the selection card, one of them not available. */
@Composable
private fun sampleActionGroups() = listOf(
    CardActionGroup(
        stringResource(R.string.sample_group_organize),
        listOf(
            CardAction(Icons.Outlined.ContentCut, stringResource(R.string.sample_move)) {},
            CardAction(Icons.Outlined.Edit, stringResource(R.string.sample_edit)) {},
            CardAction(Icons.Outlined.FolderZip, stringResource(R.string.sample_compress)) {},
            CardAction(Icons.Outlined.StarOutline, stringResource(R.string.sample_favorite)) {},
        ),
    ),
    CardActionGroup(
        stringResource(R.string.sample_group_info),
        listOf(
            CardAction(Icons.Outlined.Info, stringResource(R.string.sample_details)) {},
            CardAction(Icons.Outlined.Apps, stringResource(R.string.sample_open_with), enabled = false) {},
        ),
    ),
)

/** A bar floating in place of the pill, e.g. for pasting: its main actions in [PillDefaults]' colors. */
@Composable
private fun PillBarDemo(modifier: Modifier) {
    FloatingSurface(RoundedCornerShape(24.dp), hazeState = null, modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(pluralStringResource(R.plurals.sample_pill_bar_title, 3, 3), style = MaterialTheme.typography.titleSmall, maxLines = 1)
                MarqueeText(
                    stringResource(R.string.sample_pill_bar_target),
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalDesignColors.current.onPillVariant,
                )
            }
            IconButton(onClick = {}) { Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.sample_clear)) }
            FilledTonalIconButton(onClick = {}, colors = PillDefaults.tonalIconButtonColors()) {
                Icon(Icons.Outlined.Refresh, contentDescription = stringResource(R.string.sample_retry))
            }
            FilledTonalButton(onClick = {}, colors = PillDefaults.tonalButtonColors()) {
                Icon(Icons.Outlined.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.sample_paste))
            }
        }
    }
}

@Composable
private fun ComponentsPage(
    padding: PaddingValues,
    query: String,
    entries: List<String>,
    modifier: Modifier,
    onOpenOnboarding: () -> Unit,
    onOpenViewer: () -> Unit,
) {
    val context = LocalContext.current
    var wifi by remember { mutableStateOf(true) }
    var volume by remember { mutableFloatStateOf(0.6f) }
    var sort by remember { mutableIntStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }
    var openEntry by rememberSaveable { mutableStateOf<String?>(null) }
    var more by rememberSaveable { mutableStateOf(false) }
    val sortOptions = listOf(R.string.sample_sort_name, R.string.sample_sort_date, R.string.sample_sort_size)

    val listState = rememberLazyListState()
    val listPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding())
    LazyColumn(modifier.fillMaxSize().scrollbar(listState, listPadding), state = listState, contentPadding = listPadding) {
        if (query.isBlank()) {
            item {
                SettingsGroup(stringResource(R.string.sample_group_rows)) {
                    row { ListRow(it, stringResource(R.string.sample_row_title), summary = stringResource(R.string.sample_row_summary), icon = Icons.Outlined.Info, onClick = {}) }
                    row { SwitchRow(it, stringResource(R.string.sample_switch), wifi, onCheckedChange = { value -> wifi = value }) }
                    row {
                        SliderRow(it, stringResource(R.string.sample_slider), volume, onValueChange = { value -> volume = value }, valueRange = 0f..1f,
                            valueText = stringResource(DesignR.string.nd_percent, (volume * 100).toInt()))
                    }
                    row {
                        SegmentedRow(it, stringResource(R.string.sample_sort), sortOptions.indices.toList(), sort, label = { index -> stringResource(sortOptions[index]) },
                            onSelect = { index -> sort = index })
                    }
                }
            }
            item {
                SettingsGroup(stringResource(R.string.sample_group_feedback)) {
                    row {
                        ListRow(it, stringResource(R.string.sample_toast), onClick = {
                            Toast.makeText(context, R.string.sample_toast_text, Toast.LENGTH_SHORT).show()
                        })
                    }
                    row { ListRow(it, stringResource(R.string.sample_dialog), onClick = { showDialog = true }) }
                    row { ListRow(it, stringResource(R.string.sample_viewer), summary = stringResource(R.string.sample_viewer_summary), onClick = onOpenViewer) }
                    row {
                        ListRow(it, stringResource(R.string.sample_onboarding), summary = stringResource(R.string.sample_onboarding_summary), onClick = onOpenOnboarding)
                    }
                    row {
                        SwipeActionRow(it, stringResource(R.string.sample_swipe_remove), onAction = {
                            Toast.makeText(context, R.string.sample_swipe_removed, Toast.LENGTH_SHORT).show()
                        }) {
                            ListRow(it, stringResource(R.string.sample_swipe_row), summary = stringResource(R.string.sample_swipe_row_summary), onClick = {})
                        }
                    }
                }
            }
            item { CardsDemo() }
            item { Text(stringResource(R.string.sample_group_list), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) }
        }
        if (entries.isEmpty()) {
            item { EmptyState(Icons.Outlined.SearchOff, stringResource(R.string.sample_no_results), message = stringResource(R.string.sample_no_results_message)) }
        }
        // each entry a card of its own; a long press opens it with its actions, as in the file manager
        items(entries, key = { it }) { entry ->
            val isOpen = entry == openEntry
            ItemCard(
                title = entry,
                subtitle = stringResource(R.string.sample_entry_details),
                onClick = { if (isOpen) openEntry = null else Toast.makeText(context, entry, Toast.LENGTH_SHORT).show() },
                onLongClick = {
                    openEntry = if (isOpen) null else entry
                    more = false
                },
                leading = { EntryIcon(isOpen, Icons.Outlined.Description) },
                selected = isOpen,
                menu = if (isOpen) sampleCardMenu(more) { more = !more } else null,
                expanded = more,
            )
        }
        item { BottomPillSpacer() }
    }

    if (showDialog) SampleDialog(onDismiss = { showDialog = false })
}

/**
 * A full screen view as for a picture: a [FloatingItemCard] with its actions, "info" unfolding facts as tiles and
 * "more" the groups; tapping hides header and card and turns the background black.
 */
@Composable
private fun ViewerDemo(onClose: () -> Unit) {
    var isFullScreen by rememberSaveable { mutableStateOf(false) }
    var isFavorite by rememberSaveable { mutableStateOf(false) }
    var showInfo by rememberSaveable { mutableStateOf(false) }
    var more by rememberSaveable { mutableStateOf(false) }
    BackHandler(onBack = onClose)
    AppScaffold(
        title = stringResource(R.string.sample_viewer),
        subtitle = stringResource(R.string.sample_viewer_position, 3, 12),
        navigationIcon = { IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(DesignR.string.nd_back)) } },
        headerVisible = !isFullScreen,
        backgroundColor = if (isFullScreen) Color.Black else null,
        bottomBar = {
            FloatingItemCard(
                title = stringResource(R.string.sample_action_entry),
                subtitle = stringResource(R.string.sample_action_entry_details),
                menu = CardMenu(
                    actions = listOf(
                        CardAction(Icons.Outlined.Edit, stringResource(R.string.sample_edit)) {},
                        CardAction(if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline, stringResource(R.string.sample_favorite)) { isFavorite = !isFavorite },
                        CardAction(Icons.Outlined.Delete, stringResource(R.string.sample_delete), isDestructive = true) {},
                        CardAction(Icons.Outlined.Info, stringResource(R.string.sample_group_info), isActive = showInfo) {
                            more = false
                            showInfo = !showInfo
                        },
                        CardAction(Icons.Outlined.MoreHoriz, stringResource(R.string.sample_more), isActive = more) {
                            showInfo = false
                            more = !more
                        },
                    ),
                    groups = sampleActionGroups(),
                ),
                expanded = more,
                onFold = {
                    more = false
                    showInfo = false
                },
                visible = !isFullScreen,
                modifier = Modifier.align(Alignment.BottomCenter),
                details = if (showInfo) ({ ViewerDetails() }) else null,
            )
        },
    ) {
        Box(
            Modifier.fillMaxSize().clickable(interactionSource = null, indication = null) { isFullScreen = !isFullScreen },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier.fillMaxWidth().aspectRatio(4f / 3f).background(
                    Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)),
                ),
            )
        }
    }
}

/** What is known about the picture, as tiles in the card's boxes. */
@Composable
private fun ViewerDetails() {
    CardTileGroups {
        label(stringResource(R.string.sample_viewer_capture))
        detailLines(listOf(Detail(stringResource(R.string.sample_detail_camera), "Pixel 6"), Detail(stringResource(R.string.sample_detail_taken), "17.10.26, 08:12")))
        // a fact that does something: tapped, it would open a map
        val context = LocalContext.current
        val place = Detail(stringResource(R.string.sample_detail_place), "52.516275, 13.377704")
        val showOnMap = stringResource(R.string.sample_show_on_map)
        line({ modifier, shape ->
            DetailTile(place, shape, modifier, onClick = {
                Toast.makeText(context, R.string.sample_show_on_map, Toast.LENGTH_SHORT).show()
            }, actionIcon = Icons.Outlined.Map, actionLabel = showOnMap)
        })
        label(stringResource(R.string.sample_detail_file))
        detailLines(listOf(Detail(stringResource(R.string.sample_detail_resolution), "4032 × 3024"), Detail(stringResource(R.string.sample_detail_path), "/Bilder/Urlaub/2026/foto.jpg")))
    }
}

/** [ContentDialog] with a card of tiles (state and fingerprint) and a [SecretTextField]. */
@Composable
private fun SampleDialog(onDismiss: () -> Unit) {
    var password by remember { mutableStateOf("") }
    ContentDialog(
        title = stringResource(R.string.sample_dialog),
        confirmLabel = stringResource(DesignR.string.nd_ok),
        onConfirm = onDismiss,
        onDismiss = onDismiss,
        confirmEnabled = password.isNotEmpty(),
        extraLabel = stringResource(R.string.sample_dialog_skip),
        onExtra = onDismiss,
        icon = Icons.Outlined.Key,
    ) {
        TileCard(stringResource(R.string.sample_dialog_key)) {
            line({ m, shape -> StatusTile(CardStatus(stringResource(R.string.sample_dialog_untrusted), Icons.Outlined.Key, StatusTone.ACCENT), shape, m) })
            line({ m, shape -> DetailTile(Detail("ssh-ed25519", "SHA256:3q2+7w8n4yVh0kQm1XcZp9bR", isCode = true), shape, m) })
        }
        Spacer(Modifier.height(12.dp))
        SecretTextField(password, { password = it }, stringResource(R.string.sample_password), onDone = { if (password.isNotEmpty()) onDismiss() })
    }
}

@Composable
private fun AboutPage(padding: PaddingValues) {
    val listState = rememberLazyListState()
    val listPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding())
    LazyColumn(Modifier.scrollbar(listState, listPadding), state = listState, contentPadding = listPadding) {
        item {
            SettingsGroup(stringResource(R.string.sample_page_about)) {
                row { ListRow(it, stringResource(R.string.sample_about_package), summary = stringResource(R.string.sample_about_license)) }
            }
        }
        item {
            EmptyState(Icons.Outlined.Palette, stringResource(R.string.sample_about_try), action = {
                SignalButton(onClick = {}) { Text(stringResource(R.string.sample_about_button)) }
            })
        }
    }
}

private const val SHEET_ADD = "add"
private const val SHEET_OPTIONS = "options"
private const val SAMPLE_SELECTION_BYTES = 337_920L
