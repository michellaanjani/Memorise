package com.mobile.memorise.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.mobile.memorise.ui.screen.profile.ProfileViewModel
import com.mobile.memorise.ui.screen.cards.CardsScreen
import com.mobile.memorise.ui.screen.cards.StudyScreen
import com.mobile.memorise.ui.screen.cards.QuizScreen
import com.mobile.memorise.ui.screen.cards.CardItemData
import com.mobile.memorise.ui.screen.cards.DetailCardScreen
import com.mobile.memorise.ui.screen.create.ai.AiGenerationScreen
import com.mobile.memorise.ui.screen.create.ai.CameraCaptureScreen
import com.mobile.memorise.ui.screen.createnew.folder.CreateFolderScreen
import com.mobile.memorise.ui.screen.createnew.folder.EditFolderScreen
import com.mobile.memorise.ui.screen.createnew.folder.FolderViewModel
import com.mobile.memorise.ui.screen.createnew.deck.EditDeckScreen
import com.mobile.memorise.ui.screen.createnew.deck.CreateDeckScreen
import com.mobile.memorise.ui.screen.createnew.deck.DeckViewModel
import kotlinx.serialization.json.Json
import android.net.Uri


@Composable
fun NavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues,
    onLogout: () -> Unit
) {

    val deckViewModel: DeckViewModel = viewModel()

    val folderViewModel: FolderViewModel = viewModel()  // ⭐ NEW

    // ---------------------------------------------------------
    // Existing ProfileViewModel (tetap)
    // ---------------------------------------------------------
    val profileViewModel: ProfileViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = MainRoute.Home.route,
        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
    ) {

        // ============================================================
        // 1. Home Screen — sekarang menerima folderViewModel
        composable(route = MainRoute.Home.route) {
            HomeScreen(
                onFolderClick = { folderName ->
                    navController.navigate(MainRoute.DeckDetail.createRoute(folderName))
                },
                onDeckClick = { deckName ->
                    navController.navigate(MainRoute.Cards.createRoute(deckName))
                },
                onEditFolder = { folderName, color ->
                    navController.navigate(MainRoute.EditFolder.createRoute(folderName, color))
                },
                onEditDeck = { deckName ->
                    navController.navigate(MainRoute.EditDeck.createRoute(deckName))
                },
                folderViewModel = folderViewModel,
                deckViewModel = deckViewModel
            )
        }


        // ============================================================
        // 2. Profile Screen
        // ============================================================
        composable(route = MainRoute.Account.route) {
            ProfileScreen(
                navController = navController,
                onLogout = onLogout,
                viewModel = profileViewModel
            )
        }

        // ============================================================
        // 3. Edit Profile
        // ============================================================
        composable(route = MainRoute.EditProfile.route) {
            EditProfileScreen(
                navController = navController,
                viewModel = profileViewModel
            )
        }

        // ============================================================
        // 4. Update Password
        // ============================================================
        composable(route = MainRoute.EditPassword.route) {
            UpdatePasswordScreen(navController = navController)
        }

        // ============================================================
        // 5. Deck Detail
        // ============================================================
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
                },
                { route ->
                    navController.navigate(route)
                }
            )
        }

        // ============================================================
        // 6. Cards
        // ============================================================
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

        // ============================================================
        // 7. Card Detail
        // ============================================================
        composable(
            route = MainRoute.CardDetail.route,
            arguments = listOf(
                navArgument("cardList") { type = NavType.StringType },
                navArgument("index") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val jsonString = backStackEntry.arguments?.getString("cardList") ?: "[]"
            val index = backStackEntry.arguments?.getInt("index") ?: 0

            val cardList = try { Json.decodeFromString<List<CardItemData>>(jsonString) }
            catch (e: Exception) { emptyList() }

            DetailCardScreen(
                cards = cardList,
                initialIndex = index,
                onClose = { navController.popBackStack() }
            )
        }

        // ============================================================
        // 8. Study
        // ============================================================
        composable(
            route = MainRoute.Study.route,
            arguments = listOf(
                navArgument("deckName") { type = NavType.StringType },
                navArgument("cardList") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val deckName = backStackEntry.arguments?.getString("deckName") ?: "Unknown"
            val jsonString = backStackEntry.arguments?.getString("cardList") ?: "[]"

            val cardList = try { Json.decodeFromString<List<CardItemData>>(jsonString) }
            catch (e: Exception) { emptyList() }

            StudyScreen(
                deckName = deckName,
                cardList = cardList,
                onBackClick = { navController.popBackStack() }
            )
        }

        // ============================================================
        // 9. Quiz
        // ============================================================
        composable(
            route = MainRoute.Quiz.route,
            arguments = listOf(
                navArgument("deckName") { type = NavType.StringType },
                navArgument("cardList") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val deckName = backStackEntry.arguments?.getString("deckName") ?: "Unknown"
            val jsonString = backStackEntry.arguments?.getString("cardList") ?: "[]"

            val cardList = try { Json.decodeFromString<List<CardItemData>>(jsonString) }
            catch (e: Exception) { emptyList() }

            QuizScreen(
                deckName = deckName,
                cardList = cardList,
                onBackClick = { navController.popBackStack() }
            )
        }

        // ============================================================
        // 10. AI Generation
        // ============================================================
        composable(route = MainRoute.AiGeneration.route) {
            AiGenerationScreen(
                navController = navController,
                onBackClick = { navController.popBackStack() },
                onGenerateClick = {}
            )
        }

        // ============================================================
        // 11. Camera Screen
        // ============================================================
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

        // ============================================================
        // 12. Create Folder Screen — sekarang menerima FolderViewModel
        // ============================================================
        composable(route = MainRoute.CreateFolder.route) {
            CreateFolderScreen(
                navController = navController,
                folderViewModel = folderViewModel,    // ⭐ EDIT: Dikirim
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(route = MainRoute.CreateDeck.route) {
            CreateDeckScreen(
                navController = navController,
                deckViewModel = deckViewModel,  // ⭐ Nanti kita bikin
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "edit_folder/{oldName}/{color}",
            arguments = listOf(
                navArgument("oldName") { type = NavType.StringType },
                navArgument("color") { type = NavType.StringType },
            )
        ) { backStackEntry ->

            val oldName = Uri.decode(backStackEntry.arguments?.getString("oldName") ?: "")
            val color = Uri.decode(backStackEntry.arguments?.getString("color") ?: "#FFFFFF")

            EditFolderScreen(
                oldName = oldName,
                initialColor = color,

                folderViewModel = folderViewModel,       // WAJIB DITAMBAH
                onBackClick = { navController.popBackStack() } // WAJIB DITAMBAH
            )
        }

        composable(
            route = MainRoute.EditDeck.route,
            arguments = listOf(navArgument("oldName") { type = NavType.StringType })
        ) { backStackEntry ->
            val oldName = backStackEntry.arguments?.getString("oldName") ?: ""

            EditDeckScreen(
                oldName = oldName,
                deckViewModel = deckViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }



    }
}
