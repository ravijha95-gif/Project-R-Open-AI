package com.projectr.launcher

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject

private val Bg = Color(0xFF0B0D12)
private val Panel = Color(0xFF151923)
private val Panel2 = Color(0xFF1C2230)
private val Accent = Color(0xFFB7F34A)
private val Muted = Color(0xFF929BAA)
private val White = Color(0xFFF4F6F8)

class MainActivity : ComponentActivity() {
    private val prefs by lazy { getSharedPreferences("project_r", MODE_PRIVATE) }
    private var onFolderPicked: ((Uri, String) -> Unit)? = null
    private val folderPicker = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            try {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            } catch (_: Exception) { }
            val name = uri.lastPathSegment?.substringAfterLast(':')?.ifBlank { "Imported game folder" } ?: "Imported game folder"
            onFolderPicked?.invoke(uri, name)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var games by remember { mutableStateOf(loadGames()) }
            var selectedTab by remember { mutableStateOf(AppTab.HOME) }
            var selectedGame by remember { mutableStateOf<GameEntry?>(null) }
            var showImportInfo by remember { mutableStateOf(false) }

            fun persist(updated: List<GameEntry>) {
                games = updated
                val array = JSONArray()
                updated.forEach { game ->
                    array.put(JSONObject().put("name", game.name).put("uri", game.uri).put("added", game.added))
                }
                prefs.edit().putString("games", array.toString()).apply()
            }

            onFolderPicked = { uri, folderName ->
                val duplicate = games.any { it.uri == uri.toString() }
                if (!duplicate) persist(games + GameEntry(folderName, uri.toString(), "Imported just now"))
                selectedTab = AppTab.LIBRARY
            }

            MaterialTheme(colorScheme = darkColorScheme(
                background = Bg, surface = Panel, primary = Accent, onPrimary = Color(0xFF11150A),
                onBackground = White, onSurface = White, secondary = Muted
            )) {
                Surface(modifier = Modifier.fillMaxSize(), color = Bg) {
                    when {
                        selectedGame != null -> GameDetailsScreen(
                            game = selectedGame!!,
                            onBack = { selectedGame = null },
                            onConfigure = { selectedTab = AppTab.SETTINGS; selectedGame = null }
                        )
                        else -> Scaffold(
                            containerColor = Bg,
                            bottomBar = { BottomBar(selectedTab) { selectedTab = it } }
                        ) { padding ->
                            Box(Modifier.fillMaxSize().padding(padding)) {
                                when (selectedTab) {
                                    AppTab.HOME -> HomeScreen(
                                        games = games,
                                        onImport = { folderPicker.launch(null) },
                                        onLibrary = { selectedTab = AppTab.LIBRARY },
                                        onGame = { selectedGame = it }
                                    )
                                    AppTab.LIBRARY -> LibraryScreen(
                                        games = games,
                                        onImport = { folderPicker.launch(null) },
                                        onGame = { selectedGame = it }
                                    )
                                    AppTab.SETTINGS -> SettingsScreen()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadGames(): List<GameEntry> {
        val raw = prefs.getString("games", null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                GameEntry(obj.optString("name", "Imported game"), obj.optString("uri"), obj.optString("added", "Imported"))
            }
        } catch (_: Exception) { emptyList() }
    }
}

data class GameEntry(val name: String, val uri: String, val added: String)
enum class AppTab { HOME, LIBRARY, SETTINGS }

@Composable
private fun BrandHeader(subtitle: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("PROJECT", color = White, fontSize = 21.sp, fontWeight = FontWeight.Black, letterSpacing = 1.8.sp)
                Spacer(Modifier.width(7.dp))
                Text("R", color = Accent, fontSize = 25.sp, fontWeight = FontWeight.Black)
            }
            Text(subtitle.uppercase(), color = Muted, fontSize = 9.sp, letterSpacing = 2.sp, fontWeight = FontWeight.SemiBold)
        }
        Box(Modifier.size(38.dp).clip(CircleShape).background(Panel2), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.SportsEsports, null, tint = Accent, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun HomeScreen(games: List<GameEntry>, onImport: () -> Unit, onLibrary: () -> Unit, onGame: (GameEntry) -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp).padding(top = 24.dp), verticalArrangement = Arrangement.spacedBy(22.dp)) {
        BrandHeader("Windows gaming environment")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("YOUR PC GAMES.\nON ANDROID.", color = White, fontSize = 29.sp, lineHeight = 34.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.6).sp)
            Text("A personal hub for your classic Windows game collection.", color = Muted, fontSize = 13.sp)
        }
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Panel).padding(18.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(Accent.copy(alpha = .13f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.FolderOpen, null, tint = Accent)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("ADD YOUR FIRST GAME", color = White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Choose a Windows game folder", color = Muted, fontSize = 11.sp)
                    }
                }
                Button(onClick = onImport, modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Color(0xFF11150A)), shape = RoundedCornerShape(13.dp)) {
                    Icon(Icons.Default.Add, null); Spacer(Modifier.width(7.dp)); Text("IMPORT GAME FOLDER", fontWeight = FontWeight.ExtraBold, letterSpacing = .6.sp)
                }
                Text("Phase 1 stores your selected folder. Windows execution arrives in Phase 2.", color = Muted, fontSize = 10.sp, lineHeight = 14.sp)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            SectionTitle("RECENT GAMES")
            Text("VIEW ALL", color = Accent, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onLibrary() })
        }
        if (games.isEmpty()) EmptyState() else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
            items(games.take(3)) { GameRow(it, onClick = { onGame(it) }) }
        }
    }
}

@Composable
private fun LibraryScreen(games: List<GameEntry>, onImport: () -> Unit, onGame: (GameEntry) -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp).padding(top = 24.dp)) {
        BrandHeader("Your collection")
        Spacer(Modifier.height(25.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("GAME LIBRARY", color = White, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
                Text("${games.size} imported folder${if (games.size == 1) "" else "s"}", color = Muted, fontSize = 12.sp)
            }
            FilledTonalButton(onClick = onImport, colors = ButtonDefaults.filledTonalButtonColors(containerColor = Accent, contentColor = Color(0xFF11150A))) {
                Icon(Icons.Default.Add, null); Text(" ADD", fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(18.dp))
        if (games.isEmpty()) EmptyState() else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
            items(games) { GameRow(it, onClick = { onGame(it) }) }
        }
    }
}

@Composable
private fun GameRow(game: GameEntry, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Panel).clickable(onClick = onClick).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(52.dp).clip(RoundedCornerShape(13.dp)).background(Panel2), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.SportsEsports, null, tint = Accent, modifier = Modifier.size(25.dp))
        }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(game.name, color = White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(game.added, color = Muted, fontSize = 11.sp)
            Text("FOLDER IMPORTED · NOT YET RUNNABLE", color = Accent, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = .5.sp)
        }
        Icon(Icons.Default.ChevronRight, null, tint = Muted)
    }
}

@Composable
private fun EmptyState() {
    Column(Modifier.fillMaxWidth().padding(top = 28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Default.FolderOff, null, tint = Muted, modifier = Modifier.size(36.dp))
        Text("No games imported yet", color = White, fontWeight = FontWeight.SemiBold)
        Text("Import a folder containing your game files to add it here.", color = Muted, fontSize = 11.sp)
    }
}

@Composable
private fun GameDetailsScreen(game: GameEntry, onBack: () -> Unit, onConfigure: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = White) }
            Text("GAME DETAILS", color = White, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
        Box(Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(20.dp)).background(Panel), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.SportsEsports, null, tint = Accent, modifier = Modifier.size(58.dp))
        }
        Text(game.name, color = White, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
        InfoLine("STATUS", "Imported · engine not integrated")
        InfoLine("FOLDER URI", game.uri)
        InfoLine("ADDED", game.added)
        Spacer(Modifier.weight(1f))
        Button(onClick = onConfigure, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Color(0xFF11150A))) {
            Icon(Icons.Default.Tune, null); Spacer(Modifier.width(8.dp)); Text("GAME CONFIGURATION", fontWeight = FontWeight.Bold)
        }
        Text("Launch will be enabled after the Windows compatibility engine is integrated.", color = Muted, fontSize = 11.sp)
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = Accent, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Text(value, color = White, fontSize = 12.sp)
    }
}

@Composable
private fun SettingsScreen() {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        BrandHeader("Application preferences")
        Spacer(Modifier.height(8.dp))
        Text("SETTINGS", color = White, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
        SettingCard("Compatibility engine", "Not installed · planned for Phase 2", Icons.Default.Memory)
        SettingCard("Graphics configuration", "Per-game graphics profiles · planned", Icons.Default.Tune)
        SettingCard("Touch controls", "Custom keyboard, mouse and gamepad · planned", Icons.Default.Gamepad)
        SettingCard("Storage access", "Folders are selected using Android's document picker", Icons.Default.FolderOpen)
        SettingCard("App version", "PROJECT R · Phase 1 · 0.1.0", Icons.Default.Info)
        Spacer(Modifier.weight(1f))
        Text("PROJECT R IS A PERSONAL PROJECT. WINDOWS GAME FILES AND LICENCES ARE NOT INCLUDED.", color = Muted, fontSize = 9.sp, lineHeight = 14.sp, letterSpacing = .5.sp)
    }
}

@Composable
private fun SettingCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(Panel).padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Accent, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(13.dp))
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, color = White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(subtitle, color = Muted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun SectionTitle(text: String) { Text(text, color = White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.3.sp) }

@Composable
private fun BottomBar(selected: AppTab, onSelect: (AppTab) -> Unit) {
    NavigationBar(containerColor = Panel, contentColor = White, tonalElevation = 0.dp) {
        NavigationBarItem(selected == AppTab.HOME, { onSelect(AppTab.HOME) }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("HOME", fontSize = 9.sp, letterSpacing = .5.sp) }, colors = navColors())
        NavigationBarItem(selected == AppTab.LIBRARY, { onSelect(AppTab.LIBRARY) }, icon = { Icon(Icons.Default.ViewModule, null) }, label = { Text("LIBRARY", fontSize = 9.sp, letterSpacing = .5.sp) }, colors = navColors())
        NavigationBarItem(selected == AppTab.SETTINGS, { onSelect(AppTab.SETTINGS) }, icon = { Icon(Icons.Default.Settings, null) }, label = { Text("SETTINGS", fontSize = 9.sp, letterSpacing = .5.sp) }, colors = navColors())
    }
}

@Composable
private fun navColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = Accent, selectedTextColor = Accent,
    indicatorColor = Accent.copy(alpha = .12f), unselectedIconColor = Muted, unselectedTextColor = Muted
)
