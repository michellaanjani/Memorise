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
import com.mobile.memorise.ui.screen.cards.QuizScreen
import com.mobile.memorise.ui.screen.cards.CardItemData
import com.mobile.memorise.ui.screen.cards.DetailCardScreen
import com.mobile.memorise.ui.screen.create.ai.AiGenerationScreen
import com.mobile.memorise.ui.screen.create.ai.CameraCaptureScreen

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
                onQuizClick = { encodedJson ->
                    // Navigasi ke Study Screen bawa data JSON
                    navController.navigate(MainRoute.Quiz.createRoute(deckName, encodedJson))
                },
                onAddCardClick = { /* Navigate ke Create Card (Nanti) */ },
                onCardClick = { encodedJson, index ->
                    // Navigasi ke Detail Card bawa data JSON dan Index
                    navController.navigate(MainRoute.CardDetail.createRoute(encodedJson, index))
                }
            )
        }
        // 9. Halaman Detail Card (TAMPILAN DETAIL KARTU)
        // --- INI YANG KURANG ---
        composable(
            route = MainRoute.CardDetail.route, // Pastikan MainRoute.CardDetail sudah dibuat
            arguments = listOf(
                navArgument("cardList") { type = NavType.StringType },
                navArgument("index") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            // 1. Ambil argumen dari URL
            val jsonString = backStackEntry.arguments?.getString("cardList") ?: "[]"
            val index = backStackEntry.arguments?.getInt("index") ?: 0

            // 2. Decode JSON kembali menjadi List Data
            val cardList = try {
                Json.decodeFromString<List<CardItemData>>(jsonString)
            } catch (e: Exception) {
                emptyList()
            }

            // 3. Panggil UI Screen
            DetailCardScreen(
                cards = cardList,
                initialIndex = index,
                onClose = { navController.popBackStack() }
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
        // 8. Halaman Quiz
        // --- TAMBAHAN BARU ---
        composable(
            route = MainRoute.Quiz.route, // "study/{deckName}/{cardList}"
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

            QuizScreen(
                deckName = deckName,
                cardList = cardList,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(route = MainRoute.AiGeneration.route) {
            AiGenerationScreen(
                navController = navController,
                onBackClick = {
                    navController.popBackStack()
                },
                onGenerateClick = {
                    // TODO: Logika saat tombol Generate ditekan
                    // Misalnya validasi input atau kirim data ke server
                    // Untuk sementara, bisa navigasi kembali atau tampilkan pesan
                }
            )
        }

        // Di NavGraph
        composable("camera_screen") {
            CameraCaptureScreen(
                onImageCaptured = { uri ->
                    // Simpan URI ke NavBackStack agar bisa diambil Form Screen
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("captured_image_uri", uri.toString())

                    navController.popBackStack() // Kembali ke Form
                },
                onClose = {
                    navController.popBackStack() // Batal
                }
            )
        }

    }//NavGraph
}