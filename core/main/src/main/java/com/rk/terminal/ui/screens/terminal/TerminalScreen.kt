package com.rk.terminal.ui.screens.terminal

import android.graphics.Typeface
import android.util.TypedValue
import android.view.View
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.rk.libcommons.dpToPx
import com.rk.libcommons.pendingCommand
import com.rk.terminal.ui.activities.terminal.MainActivity
import com.rk.terminal.ui.routes.MainActivityRoutes
import com.rk.terminal.ui.screens.terminal.virtualkeys.VirtualKeysConstants
import com.rk.terminal.ui.screens.terminal.virtualkeys.VirtualKeysInfo
import com.rk.terminal.ui.screens.terminal.virtualkeys.VirtualKeysListener
import com.rk.terminal.ui.screens.terminal.virtualkeys.VirtualKeysView
import com.termux.view.TerminalView
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

var terminalView = WeakReference<TerminalView?>(null)
var virtualKeysView = WeakReference<VirtualKeysView?>(null)
var virtualKeysId = View.generateViewId()


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(modifier: Modifier = Modifier, mainActivityActivity: MainActivity,navController: NavController) {
    val context = LocalContext.current

    Box(modifier = Modifier.imePadding()) {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val configuration = LocalConfiguration.current
        val screenWidthDp = configuration.screenWidthDp
        val drawerWidth = (screenWidthDp * 0.84).dp

        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = drawerState.isOpen,
            drawerContent = {
                ModalDrawerSheet(modifier = Modifier.width(drawerWidth)) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Session",
                                style = MaterialTheme.typography.titleLarge
                            )

                            Row {
                                IconButton(onClick = {
                                    navController.navigate(MainActivityRoutes.Settings.route)
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = null
                                    )
                                }

                                IconButton(onClick = {
                                    fun generateUniqueString(existingStrings: List<String>): String {
                                        var index = 1
                                        var newString: String

                                        do {
                                            newString = "main$index"
                                            index++
                                        } while (newString in existingStrings)

                                        return newString
                                    }
                                    terminalView.get()
                                        ?.let {
                                            val client = TerminalBackEnd(it, mainActivityActivity)
                                            mainActivityActivity.sessionBinder!!.createSession(
                                                generateUniqueString(mainActivityActivity.sessionBinder!!.getService().sessionList),
                                                client,
                                                mainActivityActivity
                                            )
                                        }

                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null
                                    )
                                }

                            }


                        }

                        mainActivityActivity.sessionBinder?.getService()?.sessionList?.let{
                            LazyColumn {
                                items(it){ session_id ->
                                    SelectableCard(
                                        selected = session_id == mainActivityActivity.sessionBinder?.getService()?.currentSession?.value,
                                        onSelect = { changeSession(mainActivityActivity, session_id) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = session_id,
                                                style = MaterialTheme.typography.bodyLarge
                                            )

                                            if (session_id != mainActivityActivity.sessionBinder?.getService()?.currentSession?.value) {
                                                Spacer(modifier = Modifier.weight(1f))

                                                IconButton(
                                                    onClick = {
                                                        println(session_id)
                                                        mainActivityActivity.sessionBinder?.terminateSession(
                                                            session_id, isDeleteButton = true
                                                        )
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {

                                                    //todo make the icon outlined
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(20.dp)
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
                Scaffold(topBar = {
                    TopAppBar(
                        title = { Text(text = "ReTerminal") },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch { drawerState.open() }
                            }) {
                                Icon(Icons.Default.Menu, null)
                            }
                        })
                }) { paddingValues ->
                    Column(modifier = Modifier.padding(paddingValues)) {
                        AndroidView(
                            factory = { context ->
                                TerminalView(context, null).apply {
                                    terminalView = WeakReference(this)
                                    setTextSize(dpToPx(13f, context))
                                    val client = TerminalBackEnd(this, mainActivityActivity)

                                    val session = if (pendingCommand != null){
                                        mainActivityActivity.sessionBinder!!.getService().currentSession.value = pendingCommand!!.id
                                        mainActivityActivity.sessionBinder!!.getSession(pendingCommand!!.id)
                                            ?: mainActivityActivity.sessionBinder!!.createSession(
                                                pendingCommand!!.id,
                                                client,
                                                mainActivityActivity
                                            )
                                    }else{
                                        mainActivityActivity.sessionBinder!!.getSession(mainActivityActivity.sessionBinder!!.getService().currentSession.value)
                                            ?: mainActivityActivity.sessionBinder!!.createSession(
                                                mainActivityActivity.sessionBinder!!.getService().currentSession.value,
                                                client,
                                                mainActivityActivity
                                            )
                                    }

                                    session.updateTerminalSessionClient(client)
                                    attachSession(session)
                                    setTerminalViewClient(client)
                                    setTypeface(Typeface.MONOSPACE)

                                    post {
                                        val typedValue = TypedValue()

                                        context.theme.resolveAttribute(
                                            com.google.android.material.R.attr.colorOnSurface,
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
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            update = { terminalView ->
                                terminalView.onScreenUpdated();

                                val typedValue = TypedValue()

                                context.theme.resolveAttribute(
                                    com.google.android.material.R.attr.colorOnSurface,
                                    typedValue,
                                    true
                                )

                                terminalView.mEmulator?.mColors?.mCurrentColors?.apply {
                                    set(256, typedValue.data)
                                    set(258, typedValue.data)
                                } },
                        )

                        AndroidView(
                            factory = { context ->
                                VirtualKeysView(context, null).apply {
                                    virtualKeysView = WeakReference(this)
                                    id = virtualKeysId
                                    val typedValue = TypedValue()
                                    context.theme.resolveAttribute(
                                        com.google.android.material.R.attr.colorOnSurface,
                                        typedValue,
                                        true
                                    )


                                    virtualKeysViewClient =
                                        terminalView.get()?.mTermSession?.let {
                                            VirtualKeysListener(
                                                it
                                            )
                                        }

                                    buttonTextColor = typedValue.data

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
                    }
                }
            })
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


fun changeSession(mainActivityActivity: MainActivity, session_id:String){
    terminalView.get()?.apply {
        val client = TerminalBackEnd(this, mainActivityActivity)
        val session =
            mainActivityActivity.sessionBinder!!.getSession(session_id)
                ?: mainActivityActivity.sessionBinder!!.createSession(
                    session_id,
                    client,
                    mainActivityActivity
                )
        session.updateTerminalSessionClient(client)
        attachSession(session)
        setTerminalViewClient(client)
        post {
            val typedValue = TypedValue()

            context.theme.resolveAttribute(
                com.google.android.material.R.attr.colorOnSurface,
                typedValue,
                true
            )
            keepScreenOn = true
            requestFocus()
            setFocusableInTouchMode(true)

            mEmulator?.mColors?.mCurrentColors?.apply {
                set(256, typedValue.data)
                set(258, typedValue.data)
            }
        }
        virtualKeysView.get()?.apply {
            virtualKeysViewClient = terminalView.get()?.mTermSession?.let { VirtualKeysListener(it) }
        }

    }
    mainActivityActivity.sessionBinder!!.getService().currentSession.value = session_id

}



const val VIRTUAL_KEYS =
    ("[" + "\n  [" + "\n    \"ESC\"," + "\n    {" + "\n      \"key\": \"/\"," + "\n      \"popup\": \"\\\\\"" + "\n    }," + "\n    {" + "\n      \"key\": \"-\"," + "\n      \"popup\": \"|\"" + "\n    }," + "\n    \"HOME\"," + "\n    \"UP\"," + "\n    \"END\"," + "\n    \"PGUP\"" + "\n  ]," + "\n  [" + "\n    \"TAB\"," + "\n    \"CTRL\"," + "\n    \"ALT\"," + "\n    \"LEFT\"," + "\n    \"DOWN\"," + "\n    \"RIGHT\"," + "\n    \"PGDN\"" + "\n  ]" + "\n]")