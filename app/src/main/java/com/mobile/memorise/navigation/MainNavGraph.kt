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
import com.mobile.memorise.navigation.MainRoute.EditCard
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
import com.mobile.memorise.ui.screen.createnew.card.AddCardScreen
import com.mobile.memorise.ui.screen.createnew.card.EditCardScreen
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
                onFolderClick = { folderId, folderName ->
                    navController.navigate(MainRoute.DeckDetail.createRoute(folderId, folderName))
                },
                onDeckClick = { deckId, deckName ->
                    navController.navigate(MainRoute.Cards.createRoute(deckId, deckName))
                },
                onEditFolder = { id, name, color ->
                    // Navigasi dengan format: edit_folder/{id}/{name}/{color}
                    navController.navigate("edit_folder/$id/$name/$color")
                },
                onEditDeck = { deckId, deckName ->
                    navController.navigate(MainRoute.EditDeck.createRoute(deckId, deckName))
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
            arguments = listOf(
                navArgument("folderId") { type = NavType.StringType },
                navArgument("folderName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val folderId = backStackEntry.arguments?.getString("folderId") ?: ""
            val folderName = backStackEntry.arguments?.getString("folderName") ?: "Unknown"
            DeckScreen(
                folderId = folderId,
                folderName = folderName,
                onBackClick = { navController.popBackStack() },
                onDeckClick = { deckId, deckName ->
                    navController.navigate(MainRoute.Cards.createRoute(deckId, deckName))
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
            arguments = listOf(
                navArgument("deckId") { type = NavType.StringType },
                navArgument("deckName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId") ?: ""
            val deckName = backStackEntry.arguments?.getString("deckName") ?: "Unknown"

            CardsScreen(
                deckId = deckId,
                deckName = deckName,
                onBackClick = { navController.popBackStack() },
                onStudyClick = {
                    navController.navigate(MainRoute.Study.createRoute(deckId, deckName))
                },
                onQuizClick = {
                    navController.navigate(MainRoute.Quiz.createRoute(deckId, deckName))
                },
                onAddCardClick = {
                    // ⬅️ INI YANG PENTING
                    navController.navigate(MainRoute.AddCard.createRoute(deckId, deckName))

                },
//                onCardClick = { encodedJson, index ->
//                    navController.navigate(MainRoute.CardDetail.createRoute(encodedJson, index))
//                },
                onCardClick = { encodedJson, index ->
                    navController.navigate(MainRoute.CardDetail.createRoute(deckId, deckName, encodedJson, index))
                },
                        onEditCardClick = { encodedJson, index ->
                    navController.navigate(EditCard.createRoute(deckId, deckName, index, encodedJson))
                }
            )
        }

        // ============================================================
        // 7. Card Detail
        // ============================================================
        // ===================== 7. Card Detail =====================
        composable(
            route = MainRoute.CardDetail.route,
            arguments = listOf(
                navArgument("deckId") { type = NavType.StringType },
                navArgument("deckName") { type = NavType.StringType },
                navArgument("cardList") { type = NavType.StringType },
                navArgument("index") { type = NavType.IntType }
            )
        ) { backStackEntry ->

            val deckId = backStackEntry.arguments?.getString("deckId") ?: ""
            val deckName = backStackEntry.arguments?.getString("deckName") ?: "Unknown"
            val jsonString = backStackEntry.arguments?.getString("cardList") ?: "[]"
            val index = backStackEntry.arguments?.getInt("index") ?: 0
            val deckRemoteViewModel: com.mobile.memorise.ui.viewmodel.DeckRemoteViewModel = androidx.hilt.navigation.compose.hiltViewModel()

            val cards = try {
                Json.decodeFromString<List<CardItemData>>(Uri.decode(jsonString))
            } catch (e: Exception) {
                emptyList()
            }

            DetailCardScreen(
                deckId = deckId,
                deckName = deckName,
                cards = cards,
                initialIndex = index,
                onClose = { navController.popBackStack() },
                onEditCard = { idx, encodedJson ->
                    val encoded = Uri.encode(encodedJson)
                    navController.navigate(MainRoute.EditCard.createRoute(deckId, deckName, idx, encoded))
                },
                onDeleteCard = { idx ->
                    val targetId = cards.getOrNull(idx)?.id
                    if (targetId != null) {
                        deckRemoteViewModel.deleteCard(targetId, deckId)
                    }
                }
            )

        }



        // ============================================================
        // 8. Study
        // ============================================================
        composable(
            route = MainRoute.Study.route,
            arguments = listOf(
                navArgument("deckId") { type = NavType.StringType },
                navArgument("deckName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId") ?: ""
            val deckName = backStackEntry.arguments?.getString("deckName") ?: "Unknown"
            StudyScreen(
                deckName = deckName,
                cardList = emptyList(),
                deckId = deckId,
                onBackClick = { navController.popBackStack() }
            )
        }

        // ============================================================
        // 9. Quiz
        // ============================================================
        composable(
            route = MainRoute.Quiz.route,
            arguments = listOf(
                navArgument("deckId") { type = NavType.StringType },
                navArgument("deckName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId") ?: ""
            val deckName = backStackEntry.arguments?.getString("deckName") ?: "Unknown"

            QuizScreen(
                deckId = deckId,
                deckName = deckName,
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

        composable(
            route = MainRoute.CreateDeck.route,
            arguments = listOf(
                navArgument("folderId") { type = NavType.StringType; defaultValue = "" },
                navArgument("folderName") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val folderIdArg = backStackEntry.arguments?.getString("folderId").orEmpty()
            CreateDeckScreen(
                navController = navController,
                folderId = folderIdArg.ifBlank { null },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "edit_folder/{folderId}/{folderName}/{folderColor}",
            arguments = listOf(
                navArgument("folderId") { type = NavType.StringType },
                navArgument("folderName") { type = NavType.StringType },
                navArgument("folderColor") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            // 1. Ambil data dari argument
            val folderId = backStackEntry.arguments?.getString("folderId") ?: ""
            val oldName = backStackEntry.arguments?.getString("folderName") ?: ""
            val rawColor  = backStackEntry.arguments?.getString("folderColor") ?: "#FFFFFF"

            val initialColor = "#$rawColor"

            // 2. Panggil Screen dengan folderId
            EditFolderScreen(
                folderId = folderId, // <--- TAMBAHKAN INI
                oldName = oldName,
                initialColor = initialColor,
                folderViewModel = folderViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = MainRoute.EditDeck.route,
            arguments = listOf(
                navArgument("deckId") { type = NavType.StringType },
                navArgument("deckName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId") ?: ""
            val oldName = backStackEntry.arguments?.getString("deckName") ?: ""

            EditDeckScreen(
                deckId = deckId,
                oldName = oldName,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = MainRoute.AddCard.route,
            arguments = listOf(
                navArgument("deckId") { type = NavType.StringType },
                navArgument("deckName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId") ?: ""
            val deckName = backStackEntry.arguments?.getString("deckName") ?: "Unknown"

            AddCardScreen(
                deckId = deckId,
                deckName = deckName,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = MainRoute.EditCard.route,
            arguments = listOf(
                navArgument("deckId") { type = NavType.StringType },
                navArgument("deckName") { type = NavType.StringType },
                navArgument("index") { type = NavType.IntType },
                navArgument("json") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            val deckId = backStackEntry.arguments?.getString("deckId") ?: ""
            val deckName = backStackEntry.arguments?.getString("deckName") ?: ""
            val index = backStackEntry.arguments?.getInt("index") ?: 0
            val raw = backStackEntry.arguments?.getString("json") ?: ""
            val decoded = Uri.decode(raw)
            val cards: List<CardItemData> = Json.decodeFromString(decoded)


            // card yg mau diedit
            val card = cards[index]

            EditCardScreen(
                deckId = deckId,
                deckName = deckName,
                card = card,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
