package com.mobile.memorise.navigation

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.mobile.memorise.MainScreenContent
import com.mobile.memorise.ui.screen.landing.LandingScreen
import com.mobile.memorise.ui.screen.sigin.SignInScreen
import com.mobile.memorise.ui.screen.signup.SignUpScreen
import com.mobile.memorise.ui.screen.passwordd.ResetPwScreen
import com.mobile.memorise.ui.screen.onboarding.OnboardingScreen1
import com.mobile.memorise.ui.screen.onboarding.OnboardingScreen2
import com.mobile.memorise.ui.screen.onboarding.OnboardingScreen3
import com.mobile.memorise.ui.screen.onboarding.OnboardingScreen4
import com.mobile.memorise.ui.screen.onboarding.OnboardingScreen5


@Composable
fun AppNavGraph(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = "landing"
    ) {

        composable("landing") {
            LandingScreen(
                onNavigate = {
                    navController.navigate("Onboarding1")
                }
            )
        }

        composable("Onboarding1") {
            OnboardingScreen1(
                onNext = { navController.navigate("Onboarding2") },
                onSkip = { navController.navigate("Onboarding5") }
            )
        }

        composable("Onboarding2") {
            OnboardingScreen2(
                onNext = { navController.navigate("onboarding3") },
                onSkip = { navController.navigate("Onboarding5") }
            )
        }

        composable("onboarding3") {
            OnboardingScreen3(
                onNext = { navController.navigate("onboarding4") },
                onSkip = { navController.navigate("Onboarding5") }
            )
        }

        composable("onboarding4") {
            OnboardingScreen4(
                onNext = { navController.navigate("Onboarding5") },
                onSkip = { navController.navigate("Onboarding5") }
            )
        }

        composable("Onboarding5") {
            OnboardingScreen5(
                onSignUp = { navController.navigate("signup") },
                onLogin = { navController.navigate("signin") }
            )
        }

        composable("signin") {
            SignInScreen(
                onSignInSuccess = {
                    navController.navigate("main_entry") {
                        popUpTo("landing") { inclusive = true }
                    }
                },
                onSignUpClick = { navController.navigate("signup") },
                onForgotPasswordClick = { navController.navigate("resetpw") }
            )
        }

        composable("signup") {
            SignUpScreen(
                onLoginClick = { navController.navigate("signin") }
            )
        }

        composable("resetpw") {
            ResetPwScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // "main_entry" akan memuat MainScreenContent yang punya NavController sendiri
        composable("main_entry") {
            MainScreenContent()
        }
    }
}
