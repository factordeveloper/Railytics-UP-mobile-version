package com.autonovations.railytics_up_mobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autonovations.railytics_up_mobile.R
import com.autonovations.railytics_up_mobile.ui.viewmodel.RailyticsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: RailyticsViewModel,
    modifier: Modifier = Modifier
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val isOfflineMode by viewModel.isOfflineMode.collectAsState()
    val apiUrl by viewModel.apiUrl.collectAsState()

    val streams by viewModel.streams.collectAsState()
    val events by viewModel.events.collectAsState()
    val frames by viewModel.frames.collectAsState()
    val framesTotal by viewModel.framesTotal.collectAsState()
    val activeSessions by viewModel.activeSessions.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val menuItems = listOf(
        "STREAM SELECTION",
        "REAL-TIME ANALYSIS",
        "DASHBOARD",
        "FRAME GALLERY",
        "SETTINGS"
    )

    val menuIcons = listOf(
        Icons.Default.PlayArrow,
        Icons.Default.Info,
        Icons.Default.Home,
        Icons.Default.Photo,
        Icons.Default.Settings
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF1A1A1A),
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
                    .border(width = 1.dp, color = Color(0xFFFFC107).copy(alpha = 0.2f))
            ) {
                // USA flag header
                Image(
                    painter = painterResource(id = R.drawable.usa_flag),
                    contentDescription = "USA Flag",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .border(1.dp, Color(0xFFFFC107).copy(alpha = 0.2f))
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Menu items
                menuItems.forEachIndexed { index, label ->
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = menuIcons[index],
                                contentDescription = label,
                                tint = if (selectedTab == index) Color(0xFFFFC107) else Color.White
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                color = if (selectedTab == index) Color(0xFFFFC107) else Color.White,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        },
                        selected = selectedTab == index,
                        onClick = {
                            viewModel.setSelectedTab(index)
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Color(0xFFFFC107).copy(alpha = 0.15f),
                            unselectedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Footer branding
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Divider(color = Color.Gray.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Railytics UP Mobile v1.0",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.up_logo),
                                contentDescription = "UP Logo",
                                modifier = Modifier
                                    .height(45.dp)
                                    .padding(end = 8.dp)
                            )
                            Text(
                                text = "RAILYTICS",
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                fontSize = 16.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Navigation Menu",
                                tint = Color.Black,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    },
                    actions = {
                        Image(
                            painter = painterResource(id = R.drawable.an_logo),
                            contentDescription = "AN Logo",
                            modifier = Modifier
                                .height(40.dp)
                                .padding(end = 12.dp)
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFFFFAD01) // Gold/Yellow brand header
                    ),
                    modifier = Modifier.height(64.dp)
                )
            },
            modifier = modifier
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFF0A0A05))
            ) {
                when (selectedTab) {
                    0 -> StreamSelectionScreen(
                        streams = streams.map { it.name },
                        streamObjects = streams,
                        activeStreamIds = activeSessions.map { it.streamId },
                        onStartAnalysis = { viewModel.startStream(it) },
                        onStopAnalysis = { viewModel.stopStream(it) },
                        onStartAll = { viewModel.startAllAnalysis() },
                        onStopAll = { viewModel.stopAllAnalysis() }
                    )
                    1 -> RealTimeAnalysisScreen(
                        viewModel = viewModel
                    )
                    2 -> DashboardScreen(
                        events = events,
                        onRefresh = { viewModel.loadInitialData() }
                    )
                    3 -> FrameGalleryScreen(
                        frames = frames,
                        totalFrames = framesTotal,
                        streams = streams,
                        onRefresh = { viewModel.loadInitialData() },
                        onPageChanged = { streamId, limit, skip ->
                            viewModel.loadFramesPage(streamId, limit, skip)
                        }
                    )
                    4 -> SettingsScreen(
                        isOfflineMode = isOfflineMode,
                        onToggleOfflineMode = { viewModel.toggleOfflineMode(it) },
                        apiUrl = apiUrl,
                        onUpdateApiUrl = { viewModel.updateApiUrl(it) }
                    )
                }
            }
        }
    }
}
