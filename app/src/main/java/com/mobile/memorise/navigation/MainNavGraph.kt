package com.mobile.memorise.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Text
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
import com.mobile.memorise.ui.screen.cards.CardsScreen
import com.mobile.memorise.ui.screen.cards.StudyScreen
import com.mobile.memorise.ui.screen.cards.CardItemData
import kotlinx.serialization.json.Json
//import com.mobile.memorise.navigation.MainRoute


@Composable
fun NavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues,
    onLogout: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = MainRoute.Home.route,
        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
    ) {
        // 1. Halaman Home
        composable(route = MainRoute.Home.route) {
            HomeScreen(
                onFolderClick = { folderName ->
                    // Pindah ke halaman DeckDetail membawa nama folder
                    navController.navigate(MainRoute.DeckDetail.createRoute(folderName))
                },
                onDeckClick = { deckName ->
                    // Pindah ke halaman Cards membawa nama deck
                    navController.navigate(MainRoute.Cards.createRoute(deckName))
                }

            )
        }

        // 3. Halaman Account (Dummy)
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

        // 4. Halaman Detail Deck (Menerima Data JSON)
        composable(
            route = MainRoute.DeckDetail.route,
            arguments = listOf(navArgument("folderName") { type = NavType.StringType })
        ) { backStackEntry ->
            val folderName = backStackEntry.arguments?.getString("folderName") ?: "Unknown"
            DeckScreen(
                folderName = folderName,
                onBackClick = { navController.popBackStack() },
                onDeckClick = { deckName ->
                    // Kita navigasi ke rute Cards sambil menyelipkan nama deck-nya
                    navController.navigate(MainRoute.Cards.createRoute(deckName))
                }


            )
        }

        // 6. Halaman Cards (Detail Kartu dalam Deck)
        // --- TAMBAHAN BARU ---
        composable(
            route = MainRoute.Cards.route,
            arguments = listOf(navArgument("deckName") { type = NavType.StringType })
        ) { backStackEntry ->
            val deckName = backStackEntry.arguments?.getString("deckName") ?: "Unknown"

            CardsScreen(
                deckName = deckName,
                onBackClick = { navController.popBackStack() },
                // --- UPDATE INI ---
                onStudyClick = { encodedJson ->
                    // Navigasi ke Study Screen bawa data JSON
                    navController.navigate(MainRoute.Study.createRoute(deckName, encodedJson))
                },
                onAddCardClick = { /* Navigate ke Create Card (Nanti) */ }
            )
        }

        // 7. Halaman Study (Swipe Card)
        // --- TAMBAHAN BARU ---
        composable(
            route = MainRoute.Study.route, // "study/{deckName}/{cardList}"
            arguments = listOf(
                navArgument("deckName") { type = NavType.StringType },
                navArgument("cardList") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val deckName = backStackEntry.arguments?.getString("deckName") ?: "Unknown"
            val jsonString = backStackEntry.arguments?.getString("cardList") ?: "[]"

            // Deserialize JSON String kembali menjadi List<CardItemData>
            val cardList = try {
                Json.decodeFromString<List<CardItemData>>(jsonString)
            } catch (e: Exception) {
                emptyList()
            }

            StudyScreen(
                deckName = deckName,
                cardList = cardList,
                onBackClick = { navController.popBackStack() }
            )
        }

    }//NavGraph
}