package com.mobile.memorise.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel // ✅ DITAMBAHKAN
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
import com.mobile.memorise.ui.screen.profile.ProfileViewModel // ✅ DITAMBAHKAN
import com.mobile.memorise.ui.screen.cards.CardsScreen
import com.mobile.memorise.ui.screen.cards.StudyScreen
import com.mobile.memorise.ui.screen.cards.QuizScreen
import com.mobile.memorise.ui.screen.cards.CardItemData
import com.mobile.memorise.ui.screen.cards.DetailCardScreen
import com.mobile.memorise.ui.screen.create.ai.AiGenerationScreen
import com.mobile.memorise.ui.screen.create.ai.CameraCaptureScreen
import kotlinx.serialization.json.Json

@Composable
fun NavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues,
    onLogout: () -> Unit
) {

    // ---------------------------------------------------------
    // ✅ FIX PALING PENTING
    // BUAT SATU VIEWMODEL UNTUK PROFILE & EDIT PROFILE
    // (MENCEGAH VIEWMODEL TERBENTUK ULANG)
    // ---------------------------------------------------------
    val profileViewModel: ProfileViewModel = viewModel() // 🔥 WAJIB

    NavHost(
        navController = navController,
        startDestination = MainRoute.Home.route,
        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
    ) {

        // 1. Home
        composable(route = MainRoute.Home.route) {
            HomeScreen(
                onFolderClick = { folderName ->
                    navController.navigate(MainRoute.DeckDetail.createRoute(folderName))
                },
                onDeckClick = { deckName ->
                    navController.navigate(MainRoute.Cards.createRoute(deckName))
                }
            )
        }

        // 2. Profile Screen
        composable(route = MainRoute.Account.route) {
            ProfileScreen(
                navController = navController,
                onLogout = onLogout,
                viewModel = profileViewModel // ✅ PENTING
            )
        }

        // 3. Edit Profile
        composable(route = MainRoute.EditProfile.route) {
            EditProfileScreen(
                navController = navController,
                viewModel = profileViewModel // 🔥 AGAR DATA TETAP SAMA
            )
        }

        // 4. Update Password
        composable(route = MainRoute.EditPassword.route) {
            UpdatePasswordScreen(
                navController = navController
            )
        }

        // 👇 Sisanya tidak ada hubungannya dengan profile
        // Jadi tidak diubah
        composable(
            route = MainRoute.DeckDetail.route,
            arguments = listOf(navArgument("folderName") { type = NavType.StringType })
        ) { backStackEntry ->
            val folderName = backStackEntry.arguments?.getString("folderName") ?: "Unknown"
            DeckScreen(
                folderName = folderName,
                onBackClick = { navController.popBackStack() },
                onDeckClick = { deckName ->
                    navController.navigate(MainRoute.Cards.createRoute(deckName))
                }
            )
        }

        composable(
            route = MainRoute.Cards.route,
            arguments = listOf(navArgument("deckName") { type = NavType.StringType })
        ) { backStackEntry ->
            val deckName = backStackEntry.arguments?.getString("deckName") ?: "Unknown"

            CardsScreen(
                deckName = deckName,
                onBackClick = { navController.popBackStack() },
                onStudyClick = { encodedJson ->
                    navController.navigate(MainRoute.Study.createRoute(deckName, encodedJson))
                },
                onQuizClick = { encodedJson ->
                    navController.navigate(MainRoute.Quiz.createRoute(deckName, encodedJson))
                },
                onAddCardClick = {},
                onCardClick = { encodedJson, index ->
                    navController.navigate(MainRoute.CardDetail.createRoute(encodedJson, index))
                }
            )
        }

        composable(
            route = MainRoute.CardDetail.route,
            arguments = listOf(
                navArgument("cardList") { type = NavType.StringType },
                navArgument("index") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val jsonString = backStackEntry.arguments?.getString("cardList") ?: "[]"
            val index = backStackEntry.arguments?.getInt("index") ?: 0

            val cardList = try {
                Json.decodeFromString<List<CardItemData>>(jsonString)
            } catch (e: Exception) {
                emptyList()
            }

            DetailCardScreen(
                cards = cardList,
                initialIndex = index,
                onClose = { navController.popBackStack() }
            )
        }

        composable(
            route = MainRoute.Study.route,
            arguments = listOf(
                navArgument("deckName") { type = NavType.StringType },
                navArgument("cardList") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val deckName = backStackEntry.arguments?.getString("deckName") ?: "Unknown"
            val jsonString = backStackEntry.arguments?.getString("cardList") ?: "[]"

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

        composable(
            route = MainRoute.Quiz.route,
            arguments = listOf(
                navArgument("deckName") { type = NavType.StringType },
                navArgument("cardList") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val deckName = backStackEntry.arguments?.getString("deckName") ?: "Unknown"
            val jsonString = backStackEntry.arguments?.getString("cardList") ?: "[]"

            val cardList = try {
                Json.decodeFromString<List<CardItemData>>(jsonString)
            } catch (e: Exception) {
                emptyList()
            }

            QuizScreen(
                deckName = deckName,
                cardList = cardList,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(route = MainRoute.AiGeneration.route) {
            AiGenerationScreen(
                navController = navController,
                onBackClick = { navController.popBackStack() },
                onGenerateClick = {}
            )
        }

        composable("camera_screen") {
            CameraCaptureScreen(
                onImageCaptured = { uri ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("captured_image_uri", uri.toString())
                    navController.popBackStack()
                },
                onClose = { navController.popBackStack() }
            )
        }
    }
}
