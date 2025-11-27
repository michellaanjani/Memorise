package com.mobile.memorise.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mobile.memorise.navigation.MainRote
import com.mobile.memorise.ui.screen.deck.DeckScreen
import com.mobile.memorise.ui.screen.home.HomeScreen
import com.mobile.memorise.ui.screen.profile.ProfileScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = MainRote.Home.route,
        modifier = Modifier.padding(innerPadding)
    ) {
        // 1. Halaman Home
        composable(route = MainRote.Home.route) {
            HomeScreen(
                onFolderClick = { folderName ->
                    // Pindah ke halaman DeckDetail membawa nama folder
                    navController.navigate(MainRote.DeckDetail.createRoute(folderName))
                }
            )
        }

        // 2. Halaman Create (Dummy)
        composable(route = MainRote.Create.route) {
            Text("Halaman Create New")
        }

        // 3. Halaman Account (Dummy)
        composable(route = MainRote.Account.route) {
            ProfileScreen()
        }

        // 4. Halaman Detail Deck (Menerima Data JSON)
        composable(
            route = MainRote.DeckDetail.route,
            arguments = listOf(navArgument("folderName") { type = NavType.StringType })
        ) { backStackEntry ->
            val folderName = backStackEntry.arguments?.getString("folderName") ?: "Unknown"
            DeckScreen(
                folderName = folderName,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}