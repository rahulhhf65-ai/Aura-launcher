package net.kdt.pojavlaunch.aerix.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.aerix.components.AerixSkinPreview3D
import net.kdt.pojavlaunch.aerix.bridge.AerixInstance
import net.kdt.pojavlaunch.aerix.bridge.PojavAccountBridge
import net.kdt.pojavlaunch.aerix.bridge.PojavInstanceBridge
import net.kdt.pojavlaunch.aerix.bridge.PojavLaunchBridge
import net.kdt.pojavlaunch.aerix.servers.SavedServers
import net.kdt.pojavlaunch.aerix.theme.AerixRadii
import net.kdt.pojavlaunch.aerix.theme.AerixSpacing
import net.kdt.pojavlaunch.aerix.theme.AerixSurface
import net.kdt.pojavlaunch.aerix.theme.AerixThemeManager
import net.kdt.pojavlaunch.aerix.ui.LauncherSection

private val AuraPink = Color(0xFFFF5DB1)
private val AuraPurple = Color(0xFF9B6DFF)
private val AuraCanvas = Color(0xFF090711)
private val AuraPanel = Color(0xC9141021)
private val AuraLine = Color(0x5CBE8EFF)

/** Aura home presentation, backed by the launcher's existing account, instance and launch bridges. */
@Composable
fun AuraHomeScreen(activity: FragmentActivity, navigate: (LauncherSection) -> Unit) {
    val context = LocalContext.current
    val instances = PojavInstanceBridge.instances
    val selected = PojavInstanceBridge.selected
    val account = PojavAccountBridge.current
    val accent = AuraPink
    val servers = remember(context) { SavedServers.load(context) }
    var blossom by remember { mutableStateOf<ImageBitmap?>(null) }
    var instanceMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        PojavAccountBridge.ensureLoaded()
        blossom = withContext(Dispatchers.IO) {
            runCatching {
                context.assets.open("wallpapers/wp_02_cherry_blossom.webp").use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            }.getOrNull()
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(AuraCanvas)) {
        val compact = maxWidth < 690.dp || maxHeight < 360.dp
        Box(Modifier.fillMaxSize()) {
            blossom?.let {
                Image(
                    bitmap = it,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        0f to Color(0xCC0A0713),
                        0.46f to Color(0x8F10091B),
                        1f to Color(0xF2090711)
                    )
                )
            )
            Box(
                Modifier.fillMaxSize().drawWithCache {
                    val pinkGlow = Brush.radialGradient(
                        listOf(AuraPink.copy(alpha = 0.19f), Color.Transparent),
                        center = Offset(size.width * 0.54f, size.height * 0.62f),
                        radius = size.minDimension * 0.62f
                    )
                    val purpleGlow = Brush.radialGradient(
                        listOf(AuraPurple.copy(alpha = 0.13f), Color.Transparent),
                        center = Offset(size.width * 0.88f, size.height * 0.13f),
                        radius = size.minDimension * 0.52f
                    )
                    onDrawBehind {
                        drawRect(pinkGlow)
                        drawRect(purpleGlow)
                    }
                }
            )

            if (!compact) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AuraShortcuts(navigate, Modifier.width(146.dp).fillMaxHeight())
                    AuraCharacterStage(account?.username, account, Modifier.weight(1f).fillMaxHeight())
                    AuraLaunchPanel(
                        selected = selected,
                        instances = instances,
                        expanded = instanceMenu,
                        onExpand = { instanceMenu = true },
                        onDismiss = { instanceMenu = false },
                        onSelect = { PojavInstanceBridge.select(it); instanceMenu = false },
                        accountName = account?.username,
                        serverCount = servers.size,
                        serverAddress = servers.firstOrNull()?.address,
                        accent = accent,
                        onAccount = { navigate(LauncherSection.ACCOUNTS) },
                        onInstances = { navigate(LauncherSection.LIBRARY) },
                        onServers = { navigate(LauncherSection.SERVERS) },
                        onPlay = {
                            if (selected == null) navigate(LauncherSection.LIBRARY)
                            else PojavLaunchBridge.play(activity)
                        },
                        onJoinServer = {
                            val server = servers.firstOrNull()
                            if (server == null) navigate(LauncherSection.SERVERS)
                            else if (selected == null) navigate(LauncherSection.LIBRARY)
                            else PojavLaunchBridge.joinSocialSession(
                                activity = activity,
                                mcVersion = null,
                                loaderName = null,
                                serverAddress = server.address
                            )
                        },
                        modifier = Modifier.width(190.dp).fillMaxHeight()
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AuraBrandHeader(
                        modifier = Modifier.fillMaxWidth(),
                        onAccount = { navigate(LauncherSection.ACCOUNTS) },
                        accountName = account?.username
                    )
                    Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AuraShortcuts(navigate, Modifier.width(56.dp).fillMaxHeight(), iconOnly = true)
                        AuraCharacterStage(account?.username, account, Modifier.weight(1f).fillMaxHeight())
                    }
                    AuraCompactControls(
                        selected = selected,
                        instances = instances,
                        expanded = instanceMenu,
                        onExpand = { instanceMenu = true },
                        onDismiss = { instanceMenu = false },
                        onSelect = { PojavInstanceBridge.select(it); instanceMenu = false },
                        serverCount = servers.size,
                        accent = accent,
                        onInstances = { navigate(LauncherSection.LIBRARY) },
                        onServers = { navigate(LauncherSection.SERVERS) },
                        onPlay = {
                            if (selected == null) navigate(LauncherSection.LIBRARY)
                            else PojavLaunchBridge.play(activity)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun AuraBrandHeader(
    modifier: Modifier = Modifier,
    onAccount: () -> Unit,
    accountName: String?
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Box(
                Modifier.size(34.dp).clip(RoundedCornerShape(11.dp))
                    .background(Brush.linearGradient(listOf(AuraPink, AuraPurple))),
                contentAlignment = Alignment.Center
            ) {
                Text("A", color = Color.White, fontWeight = FontWeight.Black, fontSize = 23.sp)
            }
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text("AURA", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 3.sp)
                Text("MINECRAFT JAVA · ANDROID", color = Color(0xFFCFC2DA), fontSize = 8.sp, letterSpacing = 1.15.sp)
            }
        }
        Surface(
            onClick = onAccount,
            shape = CircleShape,
            color = Color(0xB91A1325),
            border = BorderStroke(1.dp, AuraLine)
        ) {
            Row(Modifier.padding(horizontal = 11.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(painterResource(R.drawable.aerix_ic_person_outlined), contentDescription = null,
                    tint = AuraPink, modifier = Modifier.size(15.dp))
                Text(accountName ?: "ADD ACCOUNT", color = Color.White, fontSize = 9.sp,
                    fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun AuraShortcuts(
    navigate: (LauncherSection) -> Unit,
    modifier: Modifier = Modifier,
    iconOnly: Boolean = false
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(9.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!iconOnly) AuraBrandHeader(
            modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp),
            onAccount = { navigate(LauncherSection.ACCOUNTS) },
            accountName = PojavAccountBridge.current?.username
        )
        if (!iconOnly) AuraSectionLabel("YOUR SPACE", "Everything ready for your next world")
        AuraShortcutTile("Home", "Return to your launcher", R.drawable.aerix_ic_home_filled,
            selected = true, iconOnly = iconOnly) { navigate(LauncherSection.HOME) }
        AuraShortcutTile("Library", "Instances & versions", R.drawable.aerix_ic_dashboard_filled,
            iconOnly = iconOnly) { navigate(LauncherSection.LIBRARY) }
        AuraShortcutTile("Discover", "Mods, packs & more", R.drawable.aerix_ic_public,
            iconOnly = iconOnly) { navigate(LauncherSection.DISCOVER) }
        AuraShortcutTile("Servers", "Saved Java servers", R.drawable.aerix_ic_sports_esports_outlined,
            iconOnly = iconOnly) { navigate(LauncherSection.SERVERS) }
        AuraShortcutTile("Settings", "Renderer & controls", R.drawable.aerix_ic_settings_filled,
            iconOnly = iconOnly) { navigate(LauncherSection.SETTINGS) }
        Spacer(Modifier.weight(1f, fill = !iconOnly))
        if (!iconOnly) Text("PLAY YOUR WAY", color = Color(0xFFB4A7C2), fontSize = 8.sp,
            letterSpacing = 1.3.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AuraSectionLabel(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 5.dp, vertical = 3.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, color = Color.White, fontSize = 10.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.ExtraBold)
        Text(subtitle, color = Color(0xFFB4A7C2), fontSize = 9.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun AuraShortcutTile(
    title: String,
    subtitle: String,
    icon: Int,
    selected: Boolean = false,
    iconOnly: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(15.dp),
        color = if (selected) Color(0xB629172F) else Color(0x83110E1A),
        border = BorderStroke(1.dp, if (selected) AuraPink.copy(alpha = 0.65f) else Color(0x32FFFFFF))
    ) {
        Row(
            Modifier.padding(horizontal = if (iconOnly) 0.dp else 10.dp, vertical = if (iconOnly) 10.dp else 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(painterResource(icon), contentDescription = title,
                tint = if (selected) AuraPink else Color(0xFFD8CFE2), modifier = Modifier.size(19.dp))
            if (!iconOnly) Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color(0xFFB4A7C2), fontSize = 8.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun AuraCharacterStage(
    accountName: String?,
    account: net.kdt.pojavlaunch.aerix.bridge.AerixAccount?,
    modifier: Modifier = Modifier
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (modifier == Modifier) { /* brand header is provided by the surrounding responsive layout */ }
        if (accountName != null) {
            Surface(shape = CircleShape, color = Color(0xA9181123), border = BorderStroke(1.dp, Color(0x56FFFFFF))) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF70F0B0)))
                    Text("${accountName.uppercase()} · READY", color = Color.White, fontSize = 9.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        } else {
            Text("YOUR NEXT ADVENTURE", color = Color(0xFFE7D8F4), fontSize = 9.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        }
        Box(
            Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(26.dp))
                .drawWithCache {
                    val glow = Brush.radialGradient(
                        listOf(AuraPurple.copy(alpha = 0.28f), AuraPink.copy(alpha = 0.10f), Color.Transparent),
                        center = Offset(size.width / 2f, size.height * 0.63f), radius = size.minDimension * 0.54f
                    )
                    onDrawBehind { drawRect(glow) }
                },
            contentAlignment = Alignment.Center
        ) {
            // Reuse the live skin/cape renderer; this remains a real, draggable character preview.
            AerixSkinPreview3D(
                account = account,
                azimuth = 24,
                interactionEnabled = true,
                useClientCape = true,
                showOfficialCape = true,
                animationName = "NewIdle",
                cameraDistance = 48,
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
        Text("A WORLD OF YOUR OWN", color = Color(0xFFD7CADF), fontSize = 8.sp,
            letterSpacing = 2.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AuraLaunchPanel(
    selected: AerixInstance?,
    instances: List<AerixInstance>,
    expanded: Boolean,
    onExpand: () -> Unit,
    onDismiss: () -> Unit,
    onSelect: (AerixInstance) -> Unit,
    accountName: String?,
    serverCount: Int,
    serverAddress: String?,
    accent: Color,
    onAccount: () -> Unit,
    onInstances: () -> Unit,
    onServers: () -> Unit,
    onPlay: () -> Unit,
    onJoinServer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
            color = AuraPanel, border = BorderStroke(1.dp, AuraLine)
        ) {
            Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AuraEyebrow("ACTIVE INSTANCE")
                Box {
                    Surface(
                        modifier = Modifier.fillMaxWidth(), onClick = onExpand,
                        shape = RoundedCornerShape(13.dp), color = Color(0x99110D1A),
                        border = BorderStroke(1.dp, Color(0x56FFFFFF))
                    ) {
                        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(painterResource(R.drawable.aerix_ic_videogame_asset_outlined), contentDescription = null,
                                tint = AuraPink, modifier = Modifier.size(20.dp))
                            Column(Modifier.weight(1f)) {
                                Text(selected?.name ?: "Choose a world", color = Color.White,
                                    fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(selected?.versionId ?: "Select an instance", color = Color(0xFFC8B9D2),
                                    fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Text("⌄", color = AuraPink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
                        instances.forEach { instance ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(instance.name, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                        Text(instance.versionId, fontSize = 11.sp, color = AerixSurface.textSecondary, maxLines = 1)
                                    }
                                },
                                onClick = { onSelect(instance) }
                            )
                        }
                        DropdownMenuItem(text = { Text("Manage instances…") }, onClick = { onDismiss(); onInstances() })
                    }
                }
                Text("Choose a profile to select its Minecraft version and loader.", color = Color(0xFFB9ADBF),
                    fontSize = 9.sp, lineHeight = 13.sp)
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
            color = AuraPanel, border = BorderStroke(1.dp, Color(0x557A5DC1)),
            onClick = onServers
        ) {
            Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    AuraEyebrow("SERVER INFO")
                    Icon(painterResource(R.drawable.aerix_ic_chevron_right), contentDescription = "Open servers",
                        tint = AuraPurple, modifier = Modifier.size(16.dp))
                }
                Text(if (serverCount == 0) "No saved servers yet" else "$serverCount saved Java server${if (serverCount == 1) "" else "s"}",
                    color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(serverAddress ?: "Add a server to see it here", color = Color(0xFFC8B9D2),
                    fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (serverAddress != null) {
                    Surface(onClick = onJoinServer, shape = CircleShape,
                        color = AuraPurple.copy(alpha = 0.18f), border = BorderStroke(1.dp, AuraPurple.copy(alpha = 0.55f))) {
                        Text("QUICK JOIN", color = Color(0xFFE8DAFF), fontSize = 8.sp, fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(), onClick = onAccount,
            shape = RoundedCornerShape(17.dp), color = Color(0xAA171020),
            border = BorderStroke(1.dp, Color(0x42FFFFFF))
        ) {
            Row(Modifier.padding(11.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(painterResource(R.drawable.aerix_ic_person_outlined), contentDescription = null,
                    tint = accent, modifier = Modifier.size(19.dp))
                Column(Modifier.weight(1f)) {
                    AuraEyebrow("MINECRAFT ACCOUNT")
                    Text(accountName ?: "Sign in or play offline", color = Color.White, fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text("›", color = accent, fontSize = 20.sp)
            }
        }

        Spacer(Modifier.weight(1f, fill = true))
        AuraPlayButton(onClick = onPlay, enabled = selected != null, modifier = Modifier.fillMaxWidth())
        Text("MINECRAFT: JAVA EDITION", color = Color(0xFFB9ADBF), fontSize = 8.sp,
            letterSpacing = 1.1.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
private fun AuraCompactControls(
    selected: AerixInstance?,
    instances: List<AerixInstance>,
    expanded: Boolean,
    onExpand: () -> Unit,
    onDismiss: () -> Unit,
    onSelect: (AerixInstance) -> Unit,
    serverCount: Int,
    accent: Color,
    onInstances: () -> Unit,
    onServers: () -> Unit,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) {
                Surface(onClick = onExpand, shape = RoundedCornerShape(14.dp), color = AuraPanel,
                    border = BorderStroke(1.dp, AuraLine), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(horizontal = 11.dp, vertical = 8.dp)) {
                        AuraEyebrow("INSTANCE · MINECRAFT VERSION")
                        Text(selected?.let { "${it.name}  ·  ${it.versionId}" } ?: "Choose an instance",
                            color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
                    instances.forEach { instance -> DropdownMenuItem(
                        text = { Text("${instance.name} · ${instance.versionId}", maxLines = 1) },
                        onClick = { onSelect(instance) }
                    ) }
                    DropdownMenuItem(text = { Text("Manage instances…") }, onClick = { onDismiss(); onInstances() })
                }
            }
            AuraPlayButton(onClick = onPlay, enabled = selected != null, modifier = Modifier.width(120.dp))
        }
        Surface(onClick = onServers, shape = RoundedCornerShape(12.dp), color = Color(0x99140F20),
            border = BorderStroke(1.dp, Color(0x447A5DC1))) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 11.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Icon(painterResource(R.drawable.aerix_ic_sports_esports_outlined), contentDescription = null,
                    tint = AuraPurple, modifier = Modifier.size(16.dp))
                Text("SERVER HUB", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text("$serverCount saved", color = Color(0xFFC8B9D2), fontSize = 9.sp, modifier = Modifier.weight(1f))
                Text("OPEN  ›", color = AuraPink, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun AuraPlayButton(onClick: () -> Unit, enabled: Boolean, modifier: Modifier = Modifier) {
    val buttonShape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier.height(52.dp).drawWithCache {
            val glow = Brush.radialGradient(
                listOf(AuraPink.copy(alpha = 0.48f), AuraPurple.copy(alpha = 0.12f), Color.Transparent),
                center = Offset(size.width / 2f, size.height / 2f), radius = size.maxDimension * 1.18f
            )
            onDrawBehind { drawRect(glow) }
        }.padding(3.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize().border(1.dp, Color(0xFFFFC1E5), buttonShape),
            onClick = onClick,
            enabled = enabled,
            shape = buttonShape,
            color = Color.Transparent,
            contentColor = Color.White
        ) {
            Box(Modifier.fillMaxSize().background(
                Brush.horizontalGradient(listOf(Color(0xFFFF4DA6), Color(0xFFB84CFF), Color(0xFF8658FF)))
            ), contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(painterResource(R.drawable.aerix_ic_play_arrow_filled), contentDescription = null,
                        tint = Color.White, modifier = Modifier.size(21.dp))
                    Text("PLAY", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black,
                        letterSpacing = 2.2.sp)
                }
            }
        }
    }
}

@Composable
private fun AuraEyebrow(text: String) {
    Text(text, color = Color(0xFFBDAEC8), fontSize = 8.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.ExtraBold)
}
