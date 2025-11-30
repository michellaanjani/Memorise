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
import com.mobile.memorise.ui.screen.deck.DeckScreen
import com.mobile.memorise.ui.screen.home.HomeScreen
import com.mobile.memorise.ui.screen.profile.UpdatePasswordScreen
import com.mobile.memorise.ui.screen.profile.EditProfileScreen
import com.mobile.memorise.ui.screen.profile.ProfileScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues,
    onLogout: () -> Unit            // <-- tambahkan parameter ini
) {

    NavHost(
        navController = navController,
        startDestination = MainRoute.Home.route,
        modifier = Modifier.padding(innerPadding)
    ) {

        /** 1. Home */
        composable(route = MainRoute.Home.route) {
            HomeScreen(
                onFolderClick = { folderName ->
                    navController.navigate(MainRoute.DeckDetail.createRoute(folderName))
                }
            )
        }

        /** 2. Create */
        composable(route = MainRoute.Create.route) {
            Text("Halaman Create New")
        }

        /** 3. Account */
        composable(route = MainRoute.Account.route) {
            ProfileScreen(
                navController = navController,
                onLogout = onLogout
            )
        }

        /** 4. Edit Profile */
        composable(route = MainRoute.EditProfile.route) {
            EditProfileScreen(
                navController = navController
            )
        }

        /** 5. Update Password */
        composable(route = MainRoute.EditPassword.route) {
            UpdatePasswordScreen(
                navController = navController
            )
        }

        /** 6. Deck Detail */
        composable(
            route = MainRoute.DeckDetail.route,
            arguments = listOf(navArgument("folderName") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val folderName = backStackEntry.arguments?.getString("folderName") ?: "Unknown"

            DeckScreen(
                folderName = folderName,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
