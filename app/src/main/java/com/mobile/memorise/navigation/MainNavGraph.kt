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
import com.mobile.memorise.navigation.MainRoute.*
import com.mobile.memorise.ui.screen.deck.DeckScreen
import com.mobile.memorise.ui.screen.home.HomeScreen
import com.mobile.memorise.ui.screen.profile.*
import com.mobile.memorise.ui.screen.create.ai.*
import com.mobile.memorise.ui.screen.cards.*
import com.mobile.memorise.ui.screen.createnew.folder.*
import com.mobile.memorise.ui.screen.createnew.card.*
import com.mobile.memorise.ui.screen.createnew.deck.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import android.net.Uri

@Composable
fun NavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues,
    onLogout: () -> Unit
) {
    // ============================================================
    // ViewModel Initialization
    // ============================================================
    val deckViewModel: DeckViewModel = viewModel()
    val folderViewModel: FolderViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()

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
                onFolderClick = { folderName ->
                    navController.navigate(MainRoute.DeckDetail.createRoute(folderName))
                },
                onDeckClick = { deckName ->
                    navController.navigate(MainRoute.Cards.createRoute(deckName))
                },
                onEditFolder = { id, name, color ->
                    navController.navigate("edit_folder/$id/$name/$color")
                },
                onEditDeck = { deckName ->
                    navController.navigate(MainRoute.EditDeck.createRoute(deckName))
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
        // 5. DECK DETAIL
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
                onAddCardClick = {
                    navController.navigate(MainRoute.AddCard.createRoute(deckName))
                },
                onCardClick = { encodedJson, index ->
                    navController.navigate(
                        MainRoute.CardDetail.createRoute(
                            deckName,
                            encodedJson,
                            index
                        )
                    )
                },
                onEditCardClick = { encodedJson, index ->
                    navController.navigate(
                        EditCard.createRoute(deckName, index, encodedJson)
                    )
                }
            )
        }

        // ============================================================
        // 7. CARD DETAIL
        // ============================================================
        composable(
            route = MainRoute.CardDetail.route,
            arguments = listOf(
                navArgument("deckName") { type = NavType.StringType },
                navArgument("cardList") { type = NavType.StringType },
                navArgument("index") { type = NavType.IntType }
            )
        ) { backStackEntry ->

            val deckName = backStackEntry.arguments?.getString("deckName") ?: "Unknown"
            val jsonString = backStackEntry.arguments?.getString("cardList") ?: "[]"
            val index = backStackEntry.arguments?.getInt("index") ?: 0

            val cards = try {
                Json.decodeFromString<List<CardItemData>>(Uri.decode(jsonString))
            } catch (_: Exception) {
                emptyList()
            }

            DetailCardScreen(
                deckName = deckName,
                cards = cards,
                initialIndex = index,
                onClose = { navController.popBackStack() },
                onEditCard = { idx, encodedJson ->
                    val encoded = Uri.encode(encodedJson)
                    navController.navigate("edit_card/$deckName/$idx/$encoded")
                },
                onDeleteCard = { _ -> }
            )
        }

        // ============================================================
        // 8. STUDY MODE
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

            val cardList = try {
                Json.decodeFromString<List<CardItemData>>(jsonString)
            } catch (_: Exception) {
                emptyList()
            }

            StudyScreen(
                deckName = deckName,
                cardList = cardList,
                onBackClick = { navController.popBackStack() }
            )
        }

        // ============================================================
        // 9. QUIZ
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

            val cardList = try {
                Json.decodeFromString<List<CardItemData>>(jsonString)
            } catch (_: Exception) {
                emptyList()
            }

            QuizScreen(
                deckName = deckName,
                cardList = cardList,
                onBackClick = { navController.popBackStack() }
            )
        }

        // ============================================================
        // 10. CAMERA SCREEN
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
        // 11. CREATE FOLDER
        // ============================================================
        composable(route = MainRoute.CreateFolder.route) {
            CreateFolderScreen(
                navController = navController,
                folderViewModel = folderViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // ============================================================
        // 12. CREATE DECK
        // ============================================================
        composable(route = MainRoute.CreateDeck.route) {
            CreateDeckScreen(
                navController = navController,
                deckViewModel = deckViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // ============================================================
        // 13. EDIT FOLDER
        // ============================================================
        composable(
            route = "edit_folder/{folderId}/{folderName}/{folderColor}",
            arguments = listOf(
                navArgument("folderId") { type = NavType.StringType },
                navArgument("folderName") { type = NavType.StringType },
                navArgument("folderColor") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            val folderId = backStackEntry.arguments?.getString("folderId") ?: ""
            val oldName = backStackEntry.arguments?.getString("folderName") ?: ""
            val rawColor = backStackEntry.arguments?.getString("folderColor") ?: "#FFFFFF"

            val initialColor = "#$rawColor"

            EditFolderScreen(
                folderId = folderId,
                oldName = oldName,
                initialColor = initialColor,
                folderViewModel = folderViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // ============================================================
        // 14. EDIT DECK
        // ============================================================
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

        // ============================================================
        // 15. ADD CARD
        // ============================================================
        composable(
            route = MainRoute.AddCard.route,
            arguments = listOf(navArgument("deckName") { type = NavType.StringType })
        ) { backStackEntry ->

            val deckName = backStackEntry.arguments?.getString("deckName") ?: "Unknown"

            AddCardScreen(
                deckName = deckName,
                onBackClick = { navController.popBackStack() },
                onCardSaved = { /* TODO */ }
            )
        }

        // ============================================================
        // 16. EDIT CARD
        // ============================================================
        composable(
            route = "edit_card/{deckName}/{index}/{json}",
            arguments = listOf(
                navArgument("deckName") { type = NavType.StringType },
                navArgument("index") { type = NavType.IntType },
                navArgument("json") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            val deckName = backStackEntry.arguments?.getString("deckName") ?: ""
            val index = backStackEntry.arguments?.getInt("index") ?: 0
            val decoded = Uri.decode(backStackEntry.arguments?.getString("json") ?: "")

            val cards: List<CardItemData> =
                Json.decodeFromString(decoded)

            val card = cards[index]

            EditCardScreen(
                deckName = deckName,
                card = card,
                onBackClick = { navController.popBackStack() },
                onCardUpdated = { navController.popBackStack() }
            )
        }

        // ============================================================
        // 17. AI GENERATION SCREEN
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

        // ============================================================
        // 18. AI GENERATED DRAFT
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
                        popUpTo(0)   // clear all backstack
                    }
                },
                onCardClick = { index ->
                    navController.navigate(
                        AiCardDetail.createRoute(index, encodedJson)
                    )
                },
                onEditCardClick = { index ->
                    navController.navigate(
                        AiEditCard.createRoute(index, encodedJson)
                    )
                }
            )
        }

        // ============================================================
        // 19. AI DETAIL CARD
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
                        AiEditCard.createRoute(
                            index = editIndex,
                            cardsJson = updatedJson // FIX: remove double encode
                        )
                    )
                },
                onReturnUpdatedList = { updatedList ->
                    val encoded =
                        Uri.encode(Json.encodeToString(updatedList))

                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("updated_ai_cards", encoded)

                    navController.popBackStack()
                }
            )
        }

        // ============================================================
        // 20. AI EDIT CARD
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

                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("updated_ai_cards", encoded)

                    navController.popBackStack()
                }
            )
        }
    }
}
