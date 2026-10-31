package de.needanapp.design.sample

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.needanapp.design.R as DesignR
import de.needanapp.design.components.AppScaffold
import de.needanapp.design.components.ItemCard
import de.needanapp.design.components.NavigationPill
import de.needanapp.design.components.OnboardingAppearance
import de.needanapp.design.components.OnboardingButton
import de.needanapp.design.components.OnboardingPoint
import de.needanapp.design.components.OnboardingScreen
import de.needanapp.design.components.OnboardingStatusRow
import de.needanapp.design.components.PillItem
import de.needanapp.design.theme.DesignStyle
import de.needanapp.design.theme.ThemeSettings

/**
 * An introduction in three steps: what the app does, a permission that may be granted later, and the look, each
 * below a preview of the app that takes on every choice at once.
 */
@Composable
internal fun OnboardingDemo(settings: ThemeSettings, onClose: () -> Unit) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var allowed by rememberSaveable { mutableStateOf(false) }
    BackHandler { if (step > 0) step-- else onClose() }
    val names = listOf(R.string.sample_onboarding_welcome, R.string.sample_onboarding_permission, DesignR.string.nd_appearance)
    OnboardingScreen(
        step = step,
        stepCount = names.size,
        stepName = stringResource(names[step]),
        title = stringResource(if (step == 0) R.string.sample_onboarding_title else names[step]),
        badge = if (step == 1) stringResource(R.string.sample_onboarding_optional) else null,
        onSkip = onClose,
        preview = { OnboardingSamplePreview() },
        actions = {
            when {
                step == 1 && !allowed -> {
                    OnboardingButton(stringResource(R.string.sample_onboarding_later), onClick = { step++ }, filled = false)
                    OnboardingButton(stringResource(R.string.sample_onboarding_allow), onClick = { allowed = true })
                }
                step == names.lastIndex -> OnboardingButton(stringResource(R.string.sample_onboarding_start), onClick = onClose)
                else -> OnboardingButton(stringResource(R.string.sample_onboarding_next), onClick = { step++ })
            }
        },
    ) {
        when (step) {
            0 -> {
                Text(stringResource(R.string.sample_onboarding_text), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 6.dp))
                OnboardingPoint(Icons.Outlined.Widgets, stringResource(R.string.sample_onboarding_point_parts))
                OnboardingPoint(Icons.Outlined.Palette, stringResource(R.string.sample_onboarding_point_look))
            }
            1 -> OnboardingStatusRow(
                Icons.Outlined.Notifications,
                stringResource(R.string.sample_onboarding_permission),
                stringResource(if (allowed) R.string.sample_onboarding_allowed else R.string.sample_onboarding_not_allowed),
                done = allowed,
            )
            else -> OnboardingAppearance(settings, styles = DesignStyle.entries)
        }
    }
}

/** What the preview frame shows: a header, a few cards and the pill. */
@Composable
private fun OnboardingSamplePreview() {
    val pages = listOf(
        PillItem(Icons.Outlined.Widgets, stringResource(R.string.sample_page_components)),
        PillItem(Icons.Outlined.Star, stringResource(R.string.sample_onboarding_favorites)),
        PillItem(Icons.Outlined.Share, stringResource(R.string.sample_share)),
    )
    AppScaffold(
        title = stringResource(R.string.sample_page_components),
        bottomBar = { hazeState ->
            NavigationPill(items = pages, selectedIndex = 0, onSelect = {}, hazeState = hazeState, modifier = Modifier.align(Alignment.BottomCenter))
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            repeat(PREVIEW_ENTRIES) { index ->
                ItemCard(
                    title = stringResource(R.string.sample_entry, index + 1),
                    subtitle = stringResource(R.string.sample_entry_details),
                    onClick = {},
                    leading = { Icon(Icons.Outlined.Description, contentDescription = null) },
                )
            }
        }
    }
}

private const val PREVIEW_ENTRIES = 6
