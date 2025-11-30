package com.mobile.memorise.navigation

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.mobile.memorise.ui.screen.main.MainScreenContent
import com.mobile.memorise.ui.screen.landing.LandingScreen
import com.mobile.memorise.ui.screen.sigin.SignInScreen
import com.mobile.memorise.ui.screen.signup.SignUpScreen
import com.mobile.memorise.ui.screen.signup.VerificationInfoScreen
import com.mobile.memorise.ui.screen.signup.VerificationSuccessPopup
import com.mobile.memorise.ui.screen.password.forgot.ResetPwScreen
import com.mobile.memorise.ui.screen.password.sent.ResetLinkSentScreen
import com.mobile.memorise.ui.screen.password.newpassword.NewPasswordScreen
import com.mobile.memorise.ui.screen.onboarding.*
import com.mobile.memorise.ui.screen.splash.SplashScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    onLogout: () -> Unit
) {

    NavHost(
        navController = navController,
        startDestination = "splash"                      //  Splash jadi start
    ) {

        // =============== SPLASH ==================
        composable("splash") {
            SplashScreen(
                onFinish = {
                    navController.navigate("landing") {
                        popUpTo("splash") { inclusive = true }   //  tidak bisa back ke splash
                    }
                }
            )
        }

        // =============== LANDING ==================
        composable("landing") {
            LandingScreen(
                onNavigate = {
                    navController.navigate("onboarding1")
                }
            )
        }

        // =============== ONBOARDING ==================
        composable("onboarding1") {
            OnboardingScreen1(
                onNext = { navController.navigate("onboarding2") },
                onSkip = { navController.navigate("onboarding5") }
            )
        }

        composable("onboarding2") {
            OnboardingScreen2(
                onNext = { navController.navigate("onboarding3") },
                onSkip = { navController.navigate("onboarding5") }
            )
        }

        composable("onboarding3") {
            OnboardingScreen3(
                onNext = { navController.navigate("onboarding4") },
                onSkip = { navController.navigate("onboarding5") }
            )
        }

        composable("onboarding4") {
            OnboardingScreen4(
                onNext = { navController.navigate("onboarding5") },
                onSkip = { navController.navigate("onboarding5") }
            )
        }

        composable("onboarding5") {
            OnboardingScreen5(
                onSignUp = { navController.navigate("signup") },
                onLogin = { navController.navigate("signin") }
            )
        }

        // =============== SIGN IN ==================
        composable("signin") {
            SignInScreen(
                onSignInSuccess = {
                    navController.navigate("main_entry") {
                        popUpTo("landing") { inclusive = true }
                    }
                },
                onSignUpClick = { navController.navigate("signup") },
                onForgotPasswordClick = {
                    navController.navigate(AppRoute.ResetPassword.route)
                }
            )
        }

        // =============== SIGN UP ==================
        composable("signup") {
            SignUpScreen(
                onLoginClick = { navController.navigate("signin") },
                onSignUpSuccess = { navController.navigate("verification_info") }
            )
        }

        // =============== RESET PASSWORD ==================
        composable(AppRoute.ResetPassword.route) {
            ResetPwScreen(
                onBackClick = { navController.popBackStack() },
                onEmailSent = {
                    navController.navigate(AppRoute.ResetPasswordSent.route)
                }
            )
        }

        composable(AppRoute.ResetPasswordSent.route) {
            ResetLinkSentScreen(
                onContinue = {
                    navController.navigate(AppRoute.ResetPasswordNew.route)
                }
            )
        }

        composable(AppRoute.ResetPasswordNew.route) {
            NewPasswordScreen(
                onSuccess = {
                    navController.navigate(AppRoute.SignIn.route + "?updated=true") {
                        popUpTo(AppRoute.Landing.route)
                    }
                },
                onBackToResetPw = {
                    navController.navigate(AppRoute.ResetPassword.route) {
                        popUpTo(AppRoute.ResetPassword.route)
                    }
                }
            )
        }

        // =============== VERIFICATION ==================
        composable("verification_info") {
            VerificationInfoScreen(
                onContinue = { navController.navigate("verification_success") }
            )
        }

        composable("verification_success") {
            VerificationSuccessPopup(
                onDone = {
                    navController.navigate("signin") {
                        popUpTo("signup") { inclusive = true }
                    }
                }
            )
        }

        // =============== MAIN ==================
        composable("main_entry") {

            // NavController khusus main
            val mainNavController = rememberNavController()

            MainScreenContent(
                navController = mainNavController,
                onLogout = {
                    navController.navigate("landing") {
                        popUpTo("landing") { inclusive = true }
                    }
                }
            )
        }
    }
}
