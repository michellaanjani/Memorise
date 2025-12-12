package com.mobile.memorise.navigation

import android.net.Uri
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

// Imports from your project structure
import com.mobile.memorise.navigation.MainRoute.*
import com.mobile.memorise.ui.screen.home.HomeScreen
import com.mobile.memorise.ui.screen.profile.*
import com.mobile.memorise.ui.screen.cards.*
import com.mobile.memorise.ui.screen.create.ai.*
import com.mobile.memorise.ui.screen.createnew.folder.*
import com.mobile.memorise.ui.screen.createnew.card.*
import com.mobile.memorise.ui.screen.createnew.deck.*

@Composable
fun NavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues,
    onLogout: () -> Unit
) {
    // ============================================================
    // ViewModel Initialization (Scoped to NavGraph)
    // Menggunakan hiltViewModel sesuai perubahan lokal Anda
    // ============================================================
    val deckViewModel: DeckViewModel = hiltViewModel()
    val folderViewModel: FolderViewModel = hiltViewModel() // Asumsi ini juga sudah Hilt
    val profileViewModel: ProfileViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = MainRoute.Home.route,
        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
    ) {

        // ============================================================
        // 1. HOME SCREEN
        // ============================================================
        composable(route = MainRoute.Home.route) {
            HomeScreen(
                onFolderClick = { folder ->
                    navController.navigate(MainRoute.DeckDetail.createRoute(folder.name, folder.id))
                },
                onDeckClick = { deck ->
                    navController.navigate(MainRoute.Cards.createRoute(deck.id, deck.deckName))
                },
                onEditFolder = { idFolder, name, color ->
                    navController.navigate(MainRoute.EditFolder.createRoute(idFolder, name, color))
                },
                onEditDeck = { deckId ->
                    navController.navigate(MainRoute.EditDeck.createRoute(deckId))
                },
                onMoveDeck = { deckId ->
                    navController.navigate(MainRoute.MoveDeck.createRoute(deckId))
                },
                folderViewModel = folderViewModel,
                deckViewModel = deckViewModel
            )
        }

        // ============================================================
        // 2. PROFILE
        // ============================================================
        composable(route = MainRoute.Account.route) {
            ProfileScreen(
                navController = navController,
                onLogout = onLogout,
                viewModel = profileViewModel
            )
        }

        // ============================================================
        // 3. EDIT PROFILE
        // ============================================================
        composable(route = MainRoute.EditProfile.route) {
            EditProfileScreen(
                navController = navController,
                viewModel = profileViewModel
            )
        }

        // ============================================================
        // 4. UPDATE PASSWORD
        // ============================================================
        composable(route = MainRoute.EditPassword.route) {
            UpdatePasswordScreen(navController = navController)
        }

        // ============================================================
        // 5. DECK DETAIL (Inside Folder)
        // ============================================================
        composable(
            route = MainRoute.DeckDetail.route,
            arguments = listOf(
                navArgument("folderName") { type = NavType.StringType },
                navArgument("folderId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val folderName = backStackEntry.arguments?.getString("folderName") ?: "Unknown"
            val folderId = backStackEntry.arguments?.getString("folderId") ?: ""

            DeckScreen(
                folderName = folderName,
                folderId = folderId,
                onBackClick = { navController.popBackStack() },
                onDeckClick = { deck ->
                    navController.navigate(MainRoute.Cards.createRoute(deck.id, deck.name))
                },
                onNavigate = { route ->
                    navController.navigate(route)
                }
            )
        }

        // ============================================================
        // 6. CARDS LIST
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
                deckViewModel = deckViewModel,
                onBackClick = { navController.popBackStack() },
                onStudyClick = {
                    navController.navigate(MainRoute.Study.createRoute(deckId, deckName))
                },
                onQuizClick = {
                    navController.navigate(MainRoute.Quiz.createRoute(deckId, deckName))
                },
                onAddCardClick = {
                    navController.navigate(MainRoute.AddCard.createRoute(deckId, deckName))
                },
                onCardClick = { encodedJson, index ->
                    navController.navigate(MainRoute.CardDetail.createRoute(deckId, deckName, encodedJson, index))
                },
                onEditCardClick = { encodedJson, index ->
                    navController.navigate(MainRoute.EditCard.createRoute(deckId, deckName, index, encodedJson))
                }
            )
        }

        // ============================================================
        // 7. CARD DETAIL
        // ============================================================
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
                        deckViewModel.deleteCard(targetId)
                        navController.popBackStack()
                    }
                }
            )
        }

        // ============================================================
        // 8. STUDY MODE
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
                cardList = emptyList(), // ViewModel handles data
                deckId = deckId,
                onBackClick = { navController.popBackStack() },
                deckViewModel = deckViewModel
            )
        }

        // ============================================================
        // 9. QUIZ
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
        // 10. AI GENERATION & CAMERA (Merged Upstream Features)
        // ============================================================
        composable(MainRoute.AiGeneration.route) {
            AiGenerationScreen(
                navController = navController,
                onBackClick = { navController.popBackStack() },
                onGenerateClick = {
                    val deckName = "AI Deck"
                    val sampleCards = listOf(
                        AiDraftCard("What is AI?", "AI means artificial intelligence."),
                        AiDraftCard("Define Machine Learning", "ML is a subset of AI.")
                    )
                    val encodedJson = Uri.encode(Json.encodeToString(sampleCards))

                    navController.navigate(
                        AiDraft.createRoute(deckName, encodedJson)
                    )
                }
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

        // ============================================================
        // 11. AI GENERATED DRAFT (Upstream Feature)
        // ============================================================
        composable(
            route = AiDraft.route,
            arguments = listOf(
                navArgument("deckName") { type = NavType.StringType },
                navArgument("cardsJson") { type = NavType.StringType }
            )
        ) { backStack ->
            val deckName = backStack.arguments?.getString("deckName") ?: ""
            val encodedJson = backStack.arguments?.getString("cardsJson") ?: ""
            val decodedJson = Uri.decode(encodedJson)

            AiGeneratedDraftScreen(
                navController = navController,
                deckName = deckName,
                cardsJson = decodedJson,
                onBackClick = { navController.popBackStack() },
                onSaveClick = { _, _ ->
                    navController.navigate(MainRoute.Home.route) {
                        popUpTo(0)
                    }
                },
                onCardClick = { index ->
                    navController.navigate(AiCardDetail.createRoute(index, encodedJson))
                },
                onEditCardClick = { index ->
                    navController.navigate(AiEditCard.createRoute(index, encodedJson))
                }
            )
        }

        // ============================================================
        // 12. AI DETAIL CARD (Upstream Feature)
        // ============================================================
        composable(
            route = AiCardDetail.route,
            arguments = listOf(
                navArgument("index") { type = NavType.IntType },
                navArgument("cardsJson") { type = NavType.StringType }
            )
        ) { backstack ->
            val index = backstack.arguments?.getInt("index") ?: 0
            val json = backstack.arguments?.getString("cardsJson") ?: ""

            AiDetailCardScreen(
                jsonCards = json,
                initialIndex = index,
                onClose = { navController.popBackStack() },
                onEditAiCard = { editIndex, updatedJson ->
                    navController.navigate(
                        AiEditCard.createRoute(index = editIndex, cardsJson = updatedJson)
                    )
                },
                onReturnUpdatedList = { updatedList ->
                    val encoded = Uri.encode(Json.encodeToString(updatedList))
                    navController.previousBackStackEntry?.savedStateHandle?.set("updated_ai_cards", encoded)
                    navController.popBackStack()
                }
            )
        }

        // ============================================================
        // 13. AI EDIT CARD (Upstream Feature)
        // ============================================================
        composable(
            route = AiEditCard.route,
            arguments = listOf(
                navArgument("index") { type = NavType.IntType },
                navArgument("cardsJson") { type = NavType.StringType }
            )
        ) { backstack ->
            val index = backstack.arguments?.getInt("index") ?: 0
            val json = Uri.decode(backstack.arguments?.getString("cardsJson") ?: "")

            val list = try {
                Json.decodeFromString<List<AiDraftCard>>(json)
            } catch (_: Exception) {
                emptyList()
            }
            val card = list.getOrNull(index) ?: run {
                navController.popBackStack()
                return@composable
            }

            EditAiCardScreen(
                card = card,
                onBackClick = { navController.popBackStack() },
                onCardUpdated = { updated ->
                    val newList = list.toMutableList()
                    newList[index] = updated
                    val encoded = Uri.encode(Json.encodeToString(newList))
                    navController.previousBackStackEntry?.savedStateHandle?.set("updated_ai_cards", encoded)
                    navController.popBackStack()
                }
            )
        }

        // ============================================================
        // 14. CREATE FOLDER
        // ============================================================
        composable(route = MainRoute.CreateFolder.route) {
            CreateFolderScreen(
                navController = navController,
                folderViewModel = folderViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // ============================================================
        // 15. CREATE DECK
        // ============================================================
        composable(
            route = MainRoute.CreateDeck.route,
            arguments = listOf(
                navArgument("folderId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val folderId = backStackEntry.arguments?.getString("folderId")

            CreateDeckScreen(
                navController = navController,
                deckViewModel = deckViewModel,
                folderId = folderId,
                onBackClick = { navController.popBackStack() }
            )
        }

        // ============================================================
        // 16. EDIT FOLDER
        // ============================================================
        composable(
            route = MainRoute.EditFolder.route,
            arguments = listOf(
                navArgument("idFolder") { type = NavType.StringType },
                navArgument("oldName") { type = NavType.StringType },
                navArgument("color") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val folderId = backStackEntry.arguments?.getString("idFolder") ?: ""
            val oldName = backStackEntry.arguments?.getString("oldName") ?: ""
            val rawColor  = backStackEntry.arguments?.getString("color") ?: "#FFFFFF"
            val initialColor = if (rawColor.startsWith("#")) rawColor else "#$rawColor"

            EditFolderScreen(
                folderId = folderId,
                oldName = oldName,
                initialColor = initialColor,
                folderViewModel = folderViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // ============================================================
        // 17. EDIT DECK
        // ============================================================
        composable(
            route = MainRoute.EditDeck.route,
            arguments = listOf(
                navArgument("deckId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId") ?: ""

            EditDeckScreen(
                deckId = deckId,
                deckViewModel = deckViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // ============================================================
        // 18. MOVE DECK
        // ============================================================
        composable(
            route = MainRoute.MoveDeck.route,
            arguments = listOf(navArgument("deckId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId") ?: ""

            MoveDeckScreen(
                deckId = deckId,
                onBackClick = { navController.popBackStack() }
            )
        }

        // ============================================================
        // 19. ADD CARD
        // ============================================================
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
                onBackClick = { navController.popBackStack() },
                deckViewModel = deckViewModel
            )
        }

        // ============================================================
        // 20. EDIT CARD (Using Local/Stashed logic for now)
        // ============================================================
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
            val deckName = backStackEntry.arguments?.getString("deckName") ?: "Unknown"

            // Menggunakan AddCardScreen sementara (sesuai kode stashed Anda)
            // sampai EditCardScreen mendukung parameter deckId/ViewModel
            AddCardScreen(
                deckId = deckId,
                deckName = deckName,
                onBackClick = { navController.popBackStack() },
                deckViewModel = deckViewModel
            )
        }
    }
}