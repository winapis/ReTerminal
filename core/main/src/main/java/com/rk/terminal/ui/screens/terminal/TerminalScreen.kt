package com.rk.terminal.ui.screens.terminal

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.Window
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import androidx.palette.graphics.Palette
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.material.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rk.components.compose.preferences.base.PreferenceGroup
import com.rk.libcommons.application
import com.rk.libcommons.child
import com.rk.libcommons.dpToPx
import com.rk.libcommons.localDir
import com.rk.libcommons.pendingCommand
import com.rk.resources.strings
import com.rk.settings.SettingsManager
import com.rk.terminal.ui.activities.terminal.MainActivity
import com.rk.terminal.ui.components.SettingsToggle
import com.rk.terminal.ui.routes.MainActivityRoutes
import com.rk.terminal.ui.screens.settings.SettingsCard
import com.rk.terminal.ui.screens.settings.WorkingMode
import com.rk.terminal.ui.screens.terminal.virtualkeys.VirtualKeysConstants
import com.rk.terminal.ui.screens.terminal.virtualkeys.VirtualKeysInfo
import com.rk.terminal.ui.screens.terminal.virtualkeys.VirtualKeysListener
import com.rk.terminal.ui.screens.terminal.virtualkeys.VirtualKeysView
import com.rk.terminal.ui.theme.KarbonTheme
import com.termux.terminal.TerminalColors
import com.termux.view.TerminalView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.lang.ref.WeakReference
import java.util.Properties

var terminalView = WeakReference<TerminalView?>(null)
var virtualKeysView = WeakReference<VirtualKeysView?>(null)


var darkText = mutableStateOf(SettingsManager.Terminal.blackTextColor)
var bitmap = mutableStateOf<ImageBitmap?>(null)

private val file = application!!.filesDir.child("font.ttf")
private var font = (if (file.exists() && file.canRead()){
    Typeface.createFromFile(file)
}else{
    Typeface.MONOSPACE
})

suspend fun setFont(typeface: Typeface) = withContext(Dispatchers.Main){
    font = typeface
    terminalView.get()?.apply {
        setTypeface(typeface)
        onScreenUpdated()
    }
}

fun getViewColor(): Int{
    return if (darkText.value){
        Color.BLACK
    }else{
        Color.WHITE
    }
}

fun getComposeColor():androidx.compose.ui.graphics.Color{
    return if (darkText.value){
        androidx.compose.ui.graphics.Color.Black
    }else{
        androidx.compose.ui.graphics.Color.White
    }
}

var showToolbar = mutableStateOf(SettingsManager.Interface.toolbar)
var showVirtualKeys = mutableStateOf(SettingsManager.Interface.virtualKeys)
var showHorizontalToolbar = mutableStateOf(SettingsManager.Interface.toolbar)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SessionTabBar(
    sessions: List<String>,
    currentSession: String?,
    onSessionSelect: (String) -> Unit,
    onSessionClose: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (sessions.size > 1) {
        LazyRow(
            modifier = modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(sessions) { sessionId ->
                val isActive = sessionId == currentSession
                
                Card(
                    modifier = Modifier
                        .wrapContentWidth()
                        .combinedClickable(
                            onClick = { onSessionSelect(sessionId) }
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isActive) 4.dp else 1.dp
                    ),
                    border = if (isActive) {
                        BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    } else null
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = sessionId.take(8) + if (sessionId.length > 8) "..." else "",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isActive) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                        
                        if (sessions.size > 1) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close tab",
                                modifier = Modifier
                                    .size(16.dp)
                                    .combinedClickable(
                                        onClick = { onSessionClose(sessionId) }
                                    ),
                                tint = if (isActive) {
                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TerminalScreen(
    modifier: Modifier = Modifier,
    mainActivityActivity: MainActivity,
    navController: NavController
) {
    val context = LocalContext.current
    val isDarkMode = isSystemInDarkTheme()
    val scope = rememberCoroutineScope()


    LaunchedEffect(Unit){
        withContext(Dispatchers.IO){
            if (context.filesDir.child("background").exists().not()){
                darkText.value = !isDarkMode
            }else if (bitmap.value == null){
                val fullBitmap = BitmapFactory.decodeFile(context.filesDir.child("background").absolutePath)?.asImageBitmap()
                if (fullBitmap != null) bitmap.value = fullBitmap
            }
        }


        scope.launch(Dispatchers.Main){
            virtualKeysView.get()?.apply {
                virtualKeysViewClient =
                    terminalView.get()?.mTermSession?.let {
                        VirtualKeysListener(
                            it
                        )
                    }

                buttonTextColor = getViewColor()


                reload(
                    VirtualKeysInfo(
                        VIRTUAL_KEYS,
                        "",
                        VirtualKeysConstants.CONTROL_CHARS_ALIASES
                    )
                )
            }

            terminalView.get()?.apply {
                onScreenUpdated()


                mEmulator?.mColors?.mCurrentColors?.apply {
                    set(256, getViewColor())
                    set(258, getViewColor())
                }
            }
        }


    }

    Box {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val configuration = LocalConfiguration.current
        val screenWidthDp = configuration.screenWidthDp
        val drawerWidth = (screenWidthDp * 0.84).dp
        var showAddDialog by remember { mutableStateOf(false) }

        // Function to create a new terminal session
        fun createSession(workingMode: Int = SettingsManager.System.workingMode) {
            fun generateUniqueString(existingStrings: List<String>): String {
                var index = 1
                var newString: String

                do {
                    newString = "main$index"
                    index++
                } while (newString in existingStrings)

                return newString
            }

            val sessionId = generateUniqueString(mainActivityActivity.sessionBinder!!.getService().sessionList.keys.toList())

            terminalView.get()?.let {
                val client = TerminalBackEnd(it, mainActivityActivity)
                mainActivityActivity.sessionBinder!!.createSession(
                    sessionId,
                    client,
                    mainActivityActivity, 
                    workingMode = workingMode
                )
            }

            changeSession(mainActivityActivity, session_id = sessionId)
            
            // Close drawer after creating session
            scope.launch {
                drawerState.close()
            }
        }

        BackHandler(enabled = drawerState.isOpen) {
            scope.launch {
                drawerState.close()
            }
        }

        if (drawerState.isClosed){
            SetStatusBarTextColor(isDarkIcons = darkText.value)
        }else{
            SetStatusBarTextColor(isDarkIcons = !isDarkMode)
        }

        if (showAddDialog){
            BasicAlertDialog(
                onDismissRequest = {
                    showAddDialog = false
                }
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Dialog Header
                        Text(
                            text = "Create New Session",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        SettingsCard(
                            title = { Text("Alpine", fontWeight = FontWeight.SemiBold) },
                            description = {Text("Alpine Linux - Lightweight and secure")},
                            onClick = {
                               createSession(workingMode = WorkingMode.ALPINE)
                                showAddDialog = false
                            })

                        SettingsCard(
                            title = { Text("Ubuntu", fontWeight = FontWeight.SemiBold) },
                            description = {Text("Ubuntu Linux ARM64 - Popular and user-friendly")},
                            onClick = {
                               createSession(workingMode = WorkingMode.UBUNTU)
                                showAddDialog = false
                            })

                        SettingsCard(
                            title = { Text("Debian", fontWeight = FontWeight.SemiBold) },
                            description = {Text("Debian Linux ARM64 - Stable and reliable")},
                            onClick = {
                               createSession(workingMode = WorkingMode.DEBIAN)
                                showAddDialog = false
                            })

                        SettingsCard(
                            title = { Text("Arch Linux", fontWeight = FontWeight.SemiBold) },
                            description = {Text("Arch Linux ARM64 - Bleeding edge and minimal")},
                            onClick = {
                               createSession(workingMode = WorkingMode.ARCH)
                                showAddDialog = false
                            })

                        SettingsCard(
                            title = { Text("Kali Linux", fontWeight = FontWeight.SemiBold) },
                            description = {Text("Kali Linux ARM64 - Security and penetration testing")},
                            onClick = {
                               createSession(workingMode = WorkingMode.KALI)
                                showAddDialog = false
                            })

                        SettingsCard(
                            title = { Text("Android", fontWeight = FontWeight.SemiBold) },
                            description = {Text("Android Shell - Native Android environment")},
                            onClick = {
                                createSession(workingMode = WorkingMode.ANDROID)
                                showAddDialog = false
                            })
                    }
                }
            }
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = drawerState.isOpen || !(showToolbar.value && (LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE || showHorizontalToolbar.value)),
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.width(drawerWidth),
                    drawerContainerColor = MaterialTheme.colorScheme.surface,
                    drawerContentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Enhanced Header
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Terminal Sessions",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "${mainActivityActivity.sessionBinder?.getService()?.sessionList?.size ?: 0} sessions active",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    val keyboardController = LocalSoftwareKeyboardController.current
                                    IconButton(
                                        onClick = {
                                            navController.navigate(MainActivityRoutes.Settings.route)
                                            keyboardController?.hide()
                                        },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                RoundedCornerShape(16.dp)
                                            )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Settings,
                                            contentDescription = "Settings",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                RoundedCornerShape(16.dp)
                                            )
                                            .combinedClickable(
                                                onClick = { createSession() },
                                                onLongClick = { showAddDialog = true } // Long-press for distribution selection
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "New session (long-press for distribution selection)",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }

                        mainActivityActivity.sessionBinder?.getService()?.sessionList?.keys?.toList()?.let { sessions ->
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(sessions) { session_id ->
                                    val isActive = session_id == mainActivityActivity.sessionBinder?.getService()?.currentSession?.value?.first
                                    val sessionWorkingMode = mainActivityActivity.sessionBinder?.getService()?.sessionList?.get(session_id)
                                    
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isActive) {
                                                MaterialTheme.colorScheme.primaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.surfaceContainer
                                            }
                                        ),
                                        elevation = CardDefaults.cardElevation(
                                            defaultElevation = if (isActive) 6.dp else 2.dp
                                        ),
                                        onClick = {
                                            if (!isActive) {
                                                changeSession(mainActivityActivity, session_id)
                                                scope.launch { drawerState.close() }
                                            }
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = session_id,
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                                                        color = if (isActive) {
                                                            MaterialTheme.colorScheme.onPrimaryContainer
                                                        } else {
                                                            MaterialTheme.colorScheme.onSurface
                                                        }
                                                    )
                                                    if (isActive) {
                                                        Text(
                                                            text = " ●",
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }
                                                
                                                Text(
                                                    text = when(sessionWorkingMode) {
                                                        0 -> "Alpine Linux"
                                                        1 -> "Android Shell"
                                                        2 -> "Ubuntu ARM64"
                                                        3 -> "Debian ARM64"
                                                        4 -> "Arch Linux ARM64"
                                                        5 -> "Kali Linux ARM64"
                                                        else -> "Unknown"
                                                    },
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (isActive) {
                                                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                                    } else {
                                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                    }
                                                )
                                            }

                                            // Always show close button for enhanced UX
                                            if (sessions.size > 1) { // Only allow closing if there's more than one session
                                                IconButton(
                                                    onClick = {
                                                        if (isActive && sessions.size > 1) {
                                                            // Switch to another session before closing
                                                            val otherSession = sessions.firstOrNull { it != session_id }
                                                            otherSession?.let { changeSession(mainActivityActivity, it) }
                                                        }
                                                        mainActivityActivity.sessionBinder?.terminateSession(session_id)
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.Delete,
                                                        contentDescription = "Close session",
                                                        modifier = Modifier.size(18.dp),
                                                        tint = if (isActive) {
                                                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                                        } else {
                                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }
                }

            },
            content = {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        BackgroundImage()
                        val color = getComposeColor()
                        Column {

                            fun getNameOfWorkingMode(workingMode:Int?):String{
                                return when(workingMode){
                                    0 -> "alpine"
                                    1 -> "android"
                                    2 -> "ubuntu"
                                    3 -> "debian"
                                    4 -> "arch"
                                    5 -> "kali"
                                    null -> "null"
                                    else -> "unknown"
                                }
                            }


                            if (showToolbar.value && (LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE || showHorizontalToolbar.value)){
                                TopAppBar(
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                                        scrolledContainerColor = androidx.compose.ui.graphics.Color.Transparent
                                    ),
                                    title = {
                                        Column {
                                            Text(text = "ReTerminal",color = color)
                                            Text(style = MaterialTheme.typography.bodySmall,text = mainActivityActivity.sessionBinder?.getService()?.currentSession?.value?.first + " (${getNameOfWorkingMode(mainActivityActivity.sessionBinder?.getService()?.currentSession?.value?.second)})",color = color)
                                        }
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = {
                                            scope.launch { drawerState.open() }
                                        }) {
                                            Icon(Icons.Default.Menu, null, tint = color)
                                        }
                                    },
                                    actions = {
                                        IconButton(onClick = {
                                            createSession() // Create session with default/selected distribution
                                        }) {
                                            Icon(Icons.Default.Add,null, tint = color)
                                        }
                                    }
                                )
                            }

                            // Add horizontal tab bar for session switching
                            mainActivityActivity.sessionBinder?.getService()?.sessionList?.keys?.toList()?.let { sessions ->
                                SessionTabBar(
                                    sessions = sessions,
                                    currentSession = mainActivityActivity.sessionBinder?.getService()?.currentSession?.value?.first,
                                    onSessionSelect = { sessionId ->
                                        changeSession(mainActivityActivity, sessionId)
                                    },
                                    onSessionClose = { sessionId ->
                                        val isActive = sessionId == mainActivityActivity.sessionBinder?.getService()?.currentSession?.value?.first
                                        if (isActive && sessions.size > 1) {
                                            // Switch to another session before closing the active one
                                            val otherSession = sessions.firstOrNull { it != sessionId }
                                            otherSession?.let { changeSession(mainActivityActivity, it) }
                                        }
                                        mainActivityActivity.sessionBinder?.terminateSession(sessionId)
                                    }
                                )
                            }

                            val density = LocalDensity.current
                            Column(modifier = Modifier.imePadding().navigationBarsPadding().padding(top = if (showToolbar.value){0.dp}else{
                                with(density){
                                    TopAppBarDefaults.windowInsets.getTop(density).toDp()
                                }
                            })) {
                                AndroidView(
                                    factory = { context ->
                                        TerminalView(context, null).apply {
                                            terminalView = WeakReference(this)
                                            setTextSize(
                                                dpToPx(
                                                    SettingsManager.Terminal.fontSize.toFloat(),
                                                    context
                                                )
                                            )
                                            val client = TerminalBackEnd(this, mainActivityActivity)

                                            val session = if (pendingCommand != null) {
                                                mainActivityActivity.sessionBinder!!.getService().currentSession.value = Pair(
                                                    pendingCommand!!.id, pendingCommand!!.workingMode)
                                                mainActivityActivity.sessionBinder!!.getSession(
                                                    pendingCommand!!.id
                                                )
                                                    ?: mainActivityActivity.sessionBinder!!.createSession(
                                                        pendingCommand!!.id,
                                                        client,
                                                        mainActivityActivity, workingMode = SettingsManager.System.workingMode
                                                    )
                                            } else {
                                                mainActivityActivity.sessionBinder!!.getSession(
                                                    mainActivityActivity.sessionBinder!!.getService().currentSession.value.first
                                                )
                                                    ?: mainActivityActivity.sessionBinder!!.createSession(
                                                        mainActivityActivity.sessionBinder!!.getService().currentSession.value.first,
                                                        client,
                                                        mainActivityActivity,workingMode = SettingsManager.System.workingMode
                                                    )
                                            }

                                            session.updateTerminalSessionClient(client)
                                            attachSession(session)
                                            setTerminalViewClient(client)
                                            setTypeface(font)

                                            post {
                                                val color = getViewColor()

                                                keepScreenOn = true
                                                requestFocus()
                                                isFocusableInTouchMode = true

                                                mEmulator?.mColors?.mCurrentColors?.apply {
                                                    set(256, color)
                                                    set(258, color)
                                                }

                                                val colorsFile = localDir().child("colors.properties")
                                                if (colorsFile.exists() && colorsFile.isFile){
                                                    val props = Properties()
                                                    FileInputStream(colorsFile).use { input ->
                                                        props.load(input)
                                                    }
                                                    TerminalColors.COLOR_SCHEME.updateWith(props)
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    update = { terminalView ->
                                        terminalView.onScreenUpdated()
                                       val color = getViewColor()

                                        terminalView.mEmulator?.mColors?.mCurrentColors?.apply {
                                            set(256, color)
                                            set(258, color)
                                        }
                                    },
                                )

                                if (showVirtualKeys.value){
                                    AndroidView(update = {
                                        it.apply {
                                            virtualKeysViewClient =
                                                terminalView.get()?.mTermSession?.let {
                                                    VirtualKeysListener(
                                                        it
                                                    )
                                                }


                                            buttonTextColor = getViewColor()


                                            reload(
                                                VirtualKeysInfo(
                                                    VIRTUAL_KEYS,
                                                    "",
                                                    VirtualKeysConstants.CONTROL_CHARS_ALIASES
                                                )
                                            )
                                        }
                                    },
                                        factory = { context ->
                                            VirtualKeysView(context, null).apply {
                                                virtualKeysView = WeakReference(this)

                                                virtualKeysViewClient =
                                                    terminalView.get()?.mTermSession?.let {
                                                        VirtualKeysListener(
                                                            it
                                                        )
                                                    }


                                                buttonTextColor = getViewColor()


                                                reload(
                                                    VirtualKeysInfo(
                                                        VIRTUAL_KEYS,
                                                        "",
                                                        VirtualKeysConstants.CONTROL_CHARS_ALIASES
                                                    )
                                                )
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(75.dp)
                                    )
                                }else{
                                    virtualKeysView = WeakReference(null)
                                }

                            }
                        }



                }

            })
    }
}

@Composable
fun BackgroundImage() {
    bitmap.value?.let {
        Image(
            bitmap = it,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .zIndex(-1f)
        )
    }
}

@Composable
fun SetStatusBarTextColor(isDarkIcons: Boolean) {
    val view = LocalView.current
    val window = (view.context as? Activity)?.window ?: return

    SideEffect {
        WindowCompat.getInsetsController(window, view)?.isAppearanceLightStatusBars = isDarkIcons
    }
}



@Composable
fun SelectableCard(
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = when {
            selected -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surface
        },
        label = "containerColor"
    )

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 8.dp else 2.dp
        ),
        enabled = enabled,
        onClick = onSelect
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}


fun changeSession(mainActivityActivity: MainActivity, session_id: String) {
    terminalView.get()?.apply {
        val client = TerminalBackEnd(this, mainActivityActivity)
        val session =
            mainActivityActivity.sessionBinder!!.getSession(session_id)
                ?: mainActivityActivity.sessionBinder!!.createSession(
                    session_id,
                    client,
                    mainActivityActivity,workingMode = SettingsManager.System.workingMode
                )
        session.updateTerminalSessionClient(client)
        attachSession(session)
        setTerminalViewClient(client)
        post {
            val typedValue = TypedValue()

            context.theme.resolveAttribute(
                R.attr.colorOnSurface,
                typedValue,
                true
            )
            keepScreenOn = true
            requestFocus()
            isFocusableInTouchMode = true

            mEmulator?.mColors?.mCurrentColors?.apply {
                set(256, typedValue.data)
                set(258, typedValue.data)
            }
        }
        virtualKeysView.get()?.apply {
            virtualKeysViewClient =
                terminalView.get()?.mTermSession?.let { VirtualKeysListener(it) }
        }

    }
    mainActivityActivity.sessionBinder!!.getService().currentSession.value = Pair(session_id,mainActivityActivity.sessionBinder!!.getService().sessionList[session_id]!!)

}


const val VIRTUAL_KEYS =
    ("[" + "\n  [" + "\n    \"ESC\"," + "\n    \"F1\"," + "\n    \"F2\"," + "\n    \"F3\"," + "\n    \"F4\"," + "\n    \"F5\"," + "\n    \"F6\"" + "\n  ]," + "\n  [" + "\n    \"TAB\"," + "\n    \"F7\"," + "\n    \"F8\"," + "\n    \"F9\"," + "\n    \"F10\"," + "\n    \"F11\"," + "\n    \"F12\"" + "\n  ]," + "\n  [" + "\n    \"CTRL\"," + "\n    \"ALT\"," + "\n    \"SHIFT\"," + "\n    {" + "\n      \"key\": \"/\"," + "\n      \"popup\": \"\\\\\"" + "\n    }," + "\n    {" + "\n      \"key\": \"-\"," + "\n      \"popup\": \"|\"" + "\n    }," + "\n    \"~\"," + "\n    \"`\"" + "\n  ]," + "\n  [" + "\n    \"HOME\"," + "\n    \"UP\"," + "\n    \"END\"," + "\n    \"PGUP\"," + "\n    \"PGDN\"," + "\n    \"INS\"," + "\n    \"DEL\"" + "\n  ]," + "\n  [" + "\n    \"LEFT\"," + "\n    \"DOWN\"," + "\n    \"RIGHT\"," + "\n    \"BKSP\"," + "\n    \"ENTER\"," + "\n    \"(\"," + "\n    \")\"" + "\n  ]" + "\n]")