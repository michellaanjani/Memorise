package com.mobile.memorise.ui.screen.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels // Untuk inisialisasi ViewModel
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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mobile.memorise.R
import com.mobile.memorise.navigation.AppNavGraph
import com.mobile.memorise.navigation.MainRoute
import com.mobile.memorise.navigation.NavGraph
import com.mobile.memorise.ui.theme.*
import dagger.hilt.android.AndroidEntryPoint
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
//import javax.inject.Inject
//import com.mobile.memorise.domain.repository.AuthRepository

// --- 2. MAIN ACTIVITY ---
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Inisialisasi ViewModel dengan delegate
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle Splash Screen
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Tahan Splash Screen sampai status login (true/false) didapatkan (tidak null)
        // Ini mencegah layar berkedip saat mengecek DataStore
        splashScreen.setKeepOnScreenCondition {
            mainViewModel.isLoggedIn.value == null
        }

        setContent {
            MemoriseTheme {
                // Ambil state login dari ViewModel
                val isLoggedIn by mainViewModel.isLoggedIn.collectAsState()

                // Gunakan Surface agar background sesuai tema
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AppBackgroundColor
                ) {
                    // --- LOGIKA PERPINDAHAN ROOT SCREEN ---
                    when (isLoggedIn) {
                        true -> {
                            // SUDAH LOGIN -> Masuk ke Main Screen (Home)
                            // Kita buat NavController baru khusus untuk sesi Main
                            val mainNavController = rememberNavController()

                            MainScreenContent(
                                navController = mainNavController,
                                onLogout = {
                                    // Panggil fungsi logout di ViewModel
                                    // State akan berubah jadi false, dan UI otomatis pindah ke AppNavGraph
                                    mainViewModel.logout()
                                }
                            )
                        }
                        false -> {
                            // BELUM LOGIN -> Masuk ke AppNavGraph (Landing/SignIn/SignUp)
                            val authNavController = rememberNavController()

                            AppNavGraph(
                                navController = authNavController,
                                onLogout = {
                                    // Callback ini mungkin tidak terpakai di sini karena AppNavGraph
                                    // biasanya flow masuk, tapi disiapkan saja.
                                }
                            )
                        }
                        null -> {
                            // Loading State (Biasanya tertutup Splash Screen,
                            // tapi bisa tampilkan Loading Indicator jika perlu)
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 3. MAIN SCREEN CONTENT (Kode UI Kamu) ---
// Tidak ada logika yang diubah, hanya memastikan parameter navController benar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val items = listOf(MainRoute.Home, MainRoute.Create, MainRoute.Account)
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val scope = rememberCoroutineScope()

    val closeSheetAndNavigate: (String) -> Unit = { route ->
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                showBottomSheet = false
                navController.navigate(route)
            }
        }
    }

    Scaffold(
        containerColor = AppBackgroundColor,
        bottomBar = {
            val showBottomBar = currentRoute == MainRoute.Home.route || currentRoute == MainRoute.Account.route

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
        NavGraph(
            navController = navController,
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
                CreateBottomSheetContent(
                    onNavigate = closeSheetAndNavigate
                )
            }
        }
    }
}

@Composable
fun CreateBottomSheetContent(
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 48.dp)
            .navigationBarsPadding()
    ) {
        Text(
            text = "Create New",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextGray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        CreateOptionItem(
            painter = painterResource(id = R.drawable.cfolder),
            title = "Create Folder",
            subtitle = "Create Folder to organize your decks",
            onClick = { onNavigate(MainRoute.CreateFolder.route) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        CreateOptionItem(
            painter = painterResource(id = R.drawable.cdeck),
            title = "Create Deck",
            subtitle = "Organize flashcard into decks",
            onClick = { onNavigate(MainRoute.CreateDeck.route) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        CreateOptionItem(
            painter = painterResource(id = R.drawable.cai),
            title = "Generate With AI",
            subtitle = "Create cards with AI",
            onClick = {
                onNavigate(MainRoute.AiGeneration.route)
            }
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