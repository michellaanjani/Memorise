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
import androidx.navigation.NavHostController
import com.mobile.memorise.R
import com.mobile.memorise.ui.theme.*
import com.mobile.memorise.navigation.AppNavGraph
import com.mobile.memorise.navigation.MainRoute
import com.mobile.memorise.navigation.NavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MemoriseTheme {
                val navController = rememberNavController()

                AppNavGraph(
                    navController = navController,
                    onLogout = {
                        navController.navigate("landing") {
                            popUpTo("main_entry") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

/** Composable utama yang menampilkan bottom nav dan container nav (Main flow).
 *  Ini menerima navController yang dibuat di dalamnya, dan meneruskan onLogout ke NavGraph.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    val items = listOf(MainRoute.Home, MainRoute.Create, MainRoute.Account)

    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        containerColor = AppBackgroundColor,
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().background(AppBackgroundColor)) {
                NavigationBar(
                    containerColor = NavbarBgColor,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .graphicsLayer {
                            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
                            clip = true
                        }
                        .height(80.dp)
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    items.forEach { screen ->
                        val isSelected =
                            currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        val isCreateButton = screen == MainRoute.Create

                        NavigationBarItem(
                            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent),
                            selected = isSelected,
                            onClick = {
                                if (isCreateButton) {
                                    showBottomSheet = true
                                } else {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
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
                                            Spacer(modifier = Modifier.height(8.dp))
                                        } else {
                                            Spacer(modifier = Modifier.height(11.dp))
                                        }
                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = screen.title,
                                            modifier = Modifier.size(28.dp),
                                            tint = if (isSelected) BrightBlue else InactiveIconColor
                                        )
                                    }
                                }
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 12.sp,
                                    color = if (isSelected) BrightBlue else InactiveIconColor,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            },
                            alwaysShowLabel = true
                        )
                    }
                }
            }
        }
    ) { innerPadding ->

        // PENTING: NavGraph sekarang menerima onLogout agar ProfileScreen bisa memanggilnya
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
    ) {
        Text(
            text = "Create New",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextGray,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        CreateOptionItem(
            painter = painterResource(id = R.drawable.cfolder),
            title = "Create Folder",
            subtitle = "Create Folder to organize decks",
            onClick = { }
        )

        Spacer(modifier = Modifier.height(16.dp))

        CreateOptionItem(
            painter = painterResource(id = R.drawable.cdeck),
            title = "Create Deck",
            subtitle = "Organize flashcard into decks",
            onClick = { }
        )

        Spacer(modifier = Modifier.height(16.dp))

        CreateOptionItem(
            painter = painterResource(id = R.drawable.cai),
            title = "Generate With AI",
            subtitle = "Create cards with AI",
            onClick = { }
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
