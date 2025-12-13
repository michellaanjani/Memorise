package com.mobile.memorise.navigation

import android.net.Uri
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

// --- IMPORT SCREENS ---
import com.mobile.memorise.ui.screen.home.HomeScreen
import com.mobile.memorise.ui.screen.profile.*
import com.mobile.memorise.ui.screen.cards.*
import com.mobile.memorise.ui.screen.create.ai.*
import com.mobile.memorise.ui.screen.createnew.folder.*
import com.mobile.memorise.ui.screen.createnew.card.*
import com.mobile.memorise.ui.screen.createnew.deck.*

// --- HELPER: Shared ViewModel (untuk Nested Graph seperti AI) ---
@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedViewModel(navController: NavController): T {
    val navGraphRoute = destination.parent?.route ?: return hiltViewModel()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return hiltViewModel(parentEntry)
}

@Composable
fun NavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues,
    onLogout: () -> Unit
) {
    // --- GLOBAL VIEWMODELS ---
    // Di-instantiate di sini agar datanya bisa dishare antar screen (misal: List Cards update setelah Add Card)
    val deckViewModel: DeckViewModel = hiltViewModel()
    val folderViewModel: FolderViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = MainRoute.Home.route,
        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
    ) {

        // ============================================================
        // 1. HOME & DASHBOARD
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
        // 2. FEATURE: AI GENERATION (Nested Graph)
        // ============================================================
        navigation(
            startDestination = MainRoute.AiGeneration.route,
            route = "ai_graph"
        ) {
            composable(MainRoute.AiGeneration.route) { entry ->
                val sharedAiViewModel = entry.sharedViewModel<AiViewModel>(navController)
                AiGenerationScreen(
                    navController = navController,
                    onBackClick = { navController.popBackStack() },
                    viewModel = sharedAiViewModel
                )
            }

            composable(MainRoute.CameraScreen.route) {
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

            composable(
                route = MainRoute.AiDraft.route,
                arguments = listOf(navArgument("deckId") { type = NavType.StringType })
            ) { entry ->
                val sharedAiViewModel = entry.sharedViewModel<AiViewModel>(navController)
                val deckId = entry.arguments?.getString("deckId") ?: ""
                AiGeneratedDraftScreen(
                    navController = navController,
                    deckId = deckId,
                    onBackClick = { navController.popBackStack() },
                    viewModel = sharedAiViewModel
                )
            }

            composable(
                route = MainRoute.AiCardDetail.route,
                arguments = listOf(navArgument("index") { type = NavType.IntType })
            ) { entry ->
                val sharedAiViewModel = entry.sharedViewModel<AiViewModel>(navController)
                val index = entry.arguments?.getInt("index") ?: 0
                AiDetailCardScreen(
                    navController = navController,
                    initialIndex = index,
                    viewModel = sharedAiViewModel
                )
            }

            composable(
                route = MainRoute.AiEditCard.route,
                arguments = listOf(navArgument("cardId") { type = NavType.StringType })
            ) { entry ->
                val sharedAiViewModel = entry.sharedViewModel<AiViewModel>(navController)
                val cardId = entry.arguments?.getString("cardId") ?: ""
                EditAiCardScreen(
                    cardId = cardId,
                    onBackClick = { navController.popBackStack() },
                    viewModel = sharedAiViewModel
                )
            }
        }

        // ============================================================
        // 3. PROFILE & ACCOUNT
        // ============================================================
        composable(route = MainRoute.Account.route) {
            ProfileScreen(
                navController = navController,
                onLogout = onLogout,
                viewModel = profileViewModel
            )
        }

        composable(route = MainRoute.EditProfile.route) {
            EditProfileScreen(
                navController = navController,
                viewModel = profileViewModel
            )
        }

        composable(route = MainRoute.EditPassword.route) {
            UpdatePasswordScreen(navController = navController)
        }

        // ============================================================
        // 4. DECK & CARDS MANAGEMENT
        // ============================================================

        // List Deck dalam Folder
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
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        // List Kartu dalam Deck
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
                deckViewModel = deckViewModel, // Shared Instance
                onBackClick = { navController.popBackStack() },
                onStudyClick = { navController.navigate(MainRoute.Study.createRoute(deckId, deckName)) },
                onQuizClick = { navController.navigate(MainRoute.Quiz.createRoute(deckId, deckName)) },
                onAddCardClick = { navController.navigate(MainRoute.AddCard.createRoute(deckId, deckName)) },
                onCardClick = { encodedJson, index ->
                    navController.navigate(MainRoute.CardDetail.createRoute(deckId, deckName, encodedJson, index))
                },
                onEditCardClick = { encodedJson, index ->
                    navController.navigate(MainRoute.EditCard.createRoute(deckId, deckName, index, encodedJson))
                }
            )
        }

        // Detail Kartu (Pager View)
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

            // Decode JSON
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
                // deckViewModel = deckViewModel, // HAPUS INI jika menggunakan hiltViewModel() default di definisi fungsi, ATAU biarkan jika ingin inject manual.
                onClose = { navController.popBackStack() },

                // PERBAIKAN DI SINI:
                // DetailCardScreen mengirim (cardId, jsonString), pastikan route menerimanya dengan benar.
                onEditCard = { cardId, rawJsonString ->
                    // Jika createRoute butuh index (Int), Anda harus mengubah DetailCardScreen.
                    // Jika createRoute butuh ID (String), gunakan cardId.

                    // Asumsi: Anda mengirim ID kartu ke layar edit
                    navController.navigate(
                        MainRoute.EditCard.createRoute(deckId, deckName, cardId, rawJsonString)
                    )
                }

                // HAPUS parameter onDeleteCard di sini,
                // karena logika delete sudah ditangani di dalam DetailCardScreen.
            )
        }

        // Study Mode
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
                cardList = emptyList(), // Load fresh from API via ViewModel
                deckId = deckId,
                onBackClick = { navController.popBackStack() },
                deckViewModel = deckViewModel
            )
        }

        // Quiz Mode
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
        // 5. CREATE / EDIT FORMS
        // ============================================================

        composable(route = MainRoute.CreateFolder.route) {
            CreateFolderScreen(
                navController = navController,
                folderViewModel = folderViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

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
            val rawColor = backStackEntry.arguments?.getString("color") ?: "#FFFFFF"
            val initialColor = if (rawColor.startsWith("#")) rawColor else "#$rawColor"

            EditFolderScreen(
                folderId = folderId,
                oldName = oldName,
                initialColor = initialColor,
                folderViewModel = folderViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = MainRoute.EditDeck.route,
            arguments = listOf(navArgument("deckId") { type = NavType.StringType })
        ) { backStackEntry ->
            EditDeckScreen(
                deckId = backStackEntry.arguments?.getString("deckId") ?: "",
                deckViewModel = deckViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = MainRoute.MoveDeck.route,
            arguments = listOf(navArgument("deckId") { type = NavType.StringType })
        ) { backStackEntry ->
            MoveDeckScreen(
                deckId = backStackEntry.arguments?.getString("deckId") ?: "",
                onBackClick = { navController.popBackStack() }
            )
        }

        // ADD CARD
        composable(
            route = MainRoute.AddCard.route,
            arguments = listOf(
                navArgument("deckId") { type = NavType.StringType },
                navArgument("deckName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            AddCardScreen(
                deckId = backStackEntry.arguments?.getString("deckId") ?: "",
                deckName = backStackEntry.arguments?.getString("deckName") ?: "",
                onBackClick = { navController.popBackStack() },
                deckViewModel = deckViewModel // Gunakan ViewModel yang sama
            )
        }

        // EDIT CARD (Handling JSON Arguments)
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
            val index = backStackEntry.arguments?.getInt("index") ?: 0
            val jsonString = backStackEntry.arguments?.getString("json") ?: "[]"

            // 1. Decode JSON
            val cards = try {
                Json.decodeFromString<List<CardItemData>>(Uri.decode(jsonString))
            } catch (e: Exception) {
                emptyList()
            }

            // 2. Ambil object kartu
            val cardToEdit = cards.getOrNull(index)

            // 3. Tampilkan Screen jika data valid
            if (cardToEdit != null) {
                EditCardScreen(
                    deckId = deckId,
                    deckName = deckName,
                    card = cardToEdit,
                    onBackClick = { navController.popBackStack() },
                    deckViewModel = deckViewModel
                )
            } else {
                // Fallback jika data error
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }
    }
}