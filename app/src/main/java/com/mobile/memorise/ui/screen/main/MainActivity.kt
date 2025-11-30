package com.mobile.memorise.ui.screen.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mobile.memorise.ui.theme.*
import com.mobile.memorise.navigation.AppNavGraph
// Import yang diperlukan
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.navigation.NavHostController
import com.mobile.memorise.navigation.NavGraph
import com.mobile.memorise.R
import com.mobile.memorise.navigation.MainRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()// 1. Wajib untuk HP modern agar status bar & nav bar transparan/responsif
        super.onCreate(savedInstanceState)

        setContent {
            MemoriseTheme {
                val navController = rememberNavController()
                // Tambahkan Modifier.fillMaxSize() agar background pas
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AppBackgroundColor
                ) {
                    AppNavGraph(
                        navController = navController,
                        onLogout = {
                            navController.navigate("landing") {
                                popUpTo("main_entry") { inclusive = true }
                            }
                        }
                    )
                    // MainScreenContent() jjj
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()

    // 1. PINDAHKAN INI KE ATAS (Agar bisa dibaca oleh Scaffold)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val items = listOf(MainRoute.Home, MainRoute.Create, MainRoute.Account)
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        containerColor = AppBackgroundColor,
        bottomBar = {
            // 2. LOGIKA PENENTUAN (Hanya muncul di Home dan Account)
            // Screen.Create tidak perlu dimasukkan ke sini karena dia hanya tombol (bukan halaman pindah)
            // Tapi jika kamu punya halaman lain, Bottom Bar akan hilang.
            val showBottomBar = currentRoute == MainRoute.Home.route || currentRoute == MainRoute.Account.route

            // 3. BUNGKUS DENGAN IF
            if (showBottomBar) {
                NavigationBar(
                    containerColor = NavbarBgColor,
                    tonalElevation = 0.dp,
                    windowInsets = NavigationBarDefaults.windowInsets,
                    modifier = Modifier
                        .graphicsLayer {
                            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
                            clip = true
                        }
                ) {
                    // Loop items tetap sama
                    items.forEach { screen ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        val isCreateButton = screen == MainRoute.Create

                        NavigationBarItem(
                            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent),
                            selected = isSelected,
                            onClick = {
                                if (isCreateButton) {
                                    showBottomSheet = true
                                } else {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                if (isCreateButton) {
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .shadow(4.dp, CircleShape)
                                            .background(White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = "Create",
                                            tint = BrightBlue,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .width(24.dp)
                                                    .height(3.dp)
                                                    .background(BrightBlue, RoundedCornerShape(2.dp))
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                        } else {
                                            Spacer(modifier = Modifier.height(7.dp))
                                        }

                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = screen.title,
                                            modifier = Modifier.size(26.dp),
                                            tint = if (isSelected) BrightBlue else InactiveIconColor
                                        )
                                    }
                                }
                            },
                            label = {
                                if (!isCreateButton) {
                                    Text(
                                        text = screen.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) BrightBlue else InactiveIconColor,
                                        modifier = Modifier.padding(top = 0.dp)
                                    )
                                }
                            },
                            alwaysShowLabel = true
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavGraph(navController = navController,
            innerPadding = innerPadding,
            onLogout = onLogout
        )

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = White,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            ) {
                CreateBottomSheetContent()
            }
        }
    }
}

@Composable
fun CreateBottomSheetContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 48.dp)
            .navigationBarsPadding()
    ) {
        // --- HEADER BARU (HANYA JUDUL) ---
        Text(
            text = "Create New",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextGray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp), // Sedikit jarak vertikal
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- MENU ITEM ---
        CreateOptionItem(
            painter = painterResource(id = R.drawable.cfolder),
            title = "Create Folder",
            subtitle = "Create Folder to organize your decks",
            onClick = { /* Aksi buat folder */ }
        )

        Spacer(modifier = Modifier.height(16.dp))

        CreateOptionItem(
            painter = painterResource(id = R.drawable.cdeck),
            title = "Create Deck",
            subtitle = "Organize flashcard into decks",
            onClick = { /* Aksi buat deck */ }
        )

        Spacer(modifier = Modifier.height(16.dp))

        CreateOptionItem(
            painter = painterResource(id = R.drawable.cai),
            title = "Generate With AI",
            subtitle = "Create cards with AI",
            onClick = { /* Aksi AI */ }
        )
    }
}

@Composable
fun CreateOptionItem(
    painter: Painter,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF5F5F5))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painter,
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextBlack
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextGray
            )
        }

        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = TextBlack
        )
    }
}