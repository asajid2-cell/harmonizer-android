package cc.harmonizerlabs.app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.harmonizerlabs.app.model.DiscothequeApp
import cc.harmonizerlabs.app.model.DiscothequeApps
import cc.harmonizerlabs.app.model.HarmonizerApp
import cc.harmonizerlabs.app.ui.components.GridBackground
import cc.harmonizerlabs.app.ui.components.neonGlow
import cc.harmonizerlabs.app.ui.theme.*
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val DiscoBlue = Color(0xFF1D55FF)
private val IconBg = Color(0xFF0D0D0D)
private val TaskbarBg = Color(0xFF0D0D0D)

/**
 * Internet Discotheque — native home. Faithful translation of the desktop icon grid in
 * index.html: glowing blue serif title, beveled neon app icons (real mascot art), credits,
 * and a Win98 taskbar — relaid out for a single thumb-scrollable mobile column with Harmonizer
 * featured first.
 */
@Composable
fun DiscothequeHomeScreen(
    onOpenHarmonizer: () -> Unit,
    onOpenUrl: (String) -> Unit,
) {
    Scaffold(
        containerColor = Black,
        bottomBar = { Taskbar(onOpenUrl = onOpenUrl, onOpenHarmonizer = onOpenHarmonizer) },
    ) { pad ->
        Box(Modifier.fillMaxSize().background(Black)) {
            GridBackground(bright = true)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(20.dp))

                // ── Title ─────────────────────────────────────────────────────
                // Web desktop title: one string "My Very Own Internet Discotheque", #1d55ff,
                // serif, letter-spacing .32em, glow. On mobile it's the same treatment, wrapped.
                Text(
                    "MY VERY OWN INTERNET DISCOTHEQUE",
                    color = DiscoBlue,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    lineHeight = 34.sp,
                    letterSpacing = 6.sp,
                    textAlign = TextAlign.Center,
                    style = TextStyle(shadow = Shadow(Color(0xFF3D80FF), Offset.Zero, 26f)),
                )

                Spacer(Modifier.height(24.dp))

                // ── Featured: Harmonizer (the native app) ─────────────────────
                HarmonizerHero(onClick = onOpenHarmonizer)

                Spacer(Modifier.height(28.dp))

                // ── Programs label ────────────────────────────────────────────
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "PROGRAMS",
                        color = Color(0xFF8AB4FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp,
                    )
                    Spacer(Modifier.width(10.dp))
                    HorizontalDivider(
                        color = Color(0xFF8AB4FF).copy(alpha = 0.25f),
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ── App grid (3 columns, non-lazy to avoid nested scroll) ─────
                val cols = 3
                DiscothequeApps.chunked(cols).forEach { rowApps ->
                    Row(Modifier.fillMaxWidth()) {
                        rowApps.forEach { app ->
                            Box(Modifier.weight(1f)) {
                                IconTile(app = app, onClick = { app.url?.let(onOpenUrl) })
                            }
                        }
                        // pad short final row so tiles stay left-aligned in their column
                        repeat(cols - rowApps.size) { Spacer(Modifier.weight(1f)) }
                    }
                    Spacer(Modifier.height(18.dp))
                }

                Spacer(Modifier.height(14.dp))

                // ── Credits ───────────────────────────────────────────────────
                Text("ID Chief", color = NeonOrange, fontFamily = PlexMonoFamily,
                    fontSize = 12.sp, letterSpacing = 1.sp)
                Text("コンシャスTHOUGHTS", color = Color(0xFFC38AFF), fontFamily = PlexMonoFamily,
                    fontSize = 12.sp, letterSpacing = 1.sp)
                Text("Aloe Island Posse", color = Color(0xFF58FF7A), fontFamily = PlexMonoFamily,
                    fontSize = 12.sp, letterSpacing = 1.sp)

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun HarmonizerHero(onClick: () -> Unit) {
    val app = HarmonizerApp
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .neonGlow(app.primary, glowRadius = 12.dp, intensity = 0.5f)
            .border(2.dp, app.primary)
            .background(Black)
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(IconBg)
                .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(6.dp)),
        ) {
            app.iconRes?.let {
                Image(painterResource(it), contentDescription = null, modifier = Modifier.size(58.dp))
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "HARMONIZER",
                color = app.primary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                letterSpacing = 2.sp,
                style = TextStyle(shadow = Shadow(app.primary, Offset.Zero, 18f)),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Tap to enter the lab — hashtag infinite loops",
                color = TextMuted,
                fontSize = 12.sp,
                lineHeight = 16.sp,
            )
        }
        Text("▶", color = app.primary, fontSize = 20.sp, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun IconTile(app: DiscothequeApp, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
    ) {
        // Web `.icon-frame`: 64px black tile, white .18 hairline, inset bevel — NOT a colored aura.
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(IconBg)
                .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(6.dp)),
        ) {
            when {
                app.iconRes != null ->
                    Image(painterResource(app.iconRes), contentDescription = null,
                        modifier = Modifier.size(58.dp))
                app.glyph != null ->
                    Text(app.glyph, color = app.primary, fontSize = 34.sp,
                        fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(7.dp))
        Text(
            app.label.uppercase(),
            color = Color(0xFFF8F8F8),
            fontSize = 10.sp,
            letterSpacing = 1.sp,
            lineHeight = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun Taskbar(onOpenUrl: (String) -> Unit, onOpenHarmonizer: () -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }
    var clock by remember { mutableStateOf(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))) }
    LaunchedEffect(Unit) {
        while (true) {
            clock = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            delay(30_000)
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(TaskbarBg)
            .drawBehind {
                drawLine(
                    color = Color(0xFF2B2B2B),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 2f,
                )
            }
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Box {
            Text(
                "START",
                color = Color(0xFFCFE4FF),
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF151515))
                    .border(1.dp, Color(0xFF3A3A3A), RoundedCornerShape(2.dp))
                    .clickable { menuOpen = true }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            )
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
                modifier = Modifier.background(Color(0xFF0A0E18)),
            ) {
                DropdownMenuItem(
                    text = { Text("Harmonizer", color = HarmonizerApp.primary, fontSize = 13.sp) },
                    onClick = { menuOpen = false; onOpenHarmonizer() },
                )
                DiscothequeApps.forEach { a ->
                    DropdownMenuItem(
                        text = { Text(a.label, color = Color(0xFFCFE4FF), fontSize = 13.sp) },
                        onClick = { menuOpen = false; a.url?.let(onOpenUrl) },
                    )
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Text("AN ODE TO YOU", color = Color(0xFF8AB4FF), fontSize = 10.sp, letterSpacing = 3.sp)
        Spacer(Modifier.weight(1f))
        Text(
            clock,
            color = Color(0xFFA3B6D6),
            fontFamily = PlexMonoFamily,
            fontSize = 11.sp,
            modifier = Modifier
                .border(1.dp, Color(0xFF94B4FF).copy(alpha = 0.2f))
                .padding(horizontal = 6.dp, vertical = 4.dp),
        )
    }
}
