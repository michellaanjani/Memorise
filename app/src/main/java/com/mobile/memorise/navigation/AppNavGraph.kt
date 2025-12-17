package com.mobile.memorise.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

// MAIN
import com.mobile.memorise.ui.screen.main.MainScreenContent

// LANDING & AUTH
import com.mobile.memorise.ui.screen.landing.LandingScreen
import com.mobile.memorise.ui.screen.sigin.SignInScreen
import com.mobile.memorise.ui.screen.signup.SignUpScreen

// SIGNUP VERIFICATION
import com.mobile.memorise.ui.screen.signup.successverif.VerificationSuccessPopup
import com.mobile.memorise.ui.screen.signup.linksentverif.VerivicationLinkSentScreen

// PASSWORD RESET FLOW
import com.mobile.memorise.ui.screen.password.sent.ResetOtpScreen
import com.mobile.memorise.ui.screen.password.forgot.ResetPwScreen
// Pastikan import ini mengarah ke file NewPasswordScreen yang benar
import com.mobile.memorise.ui.screen.password.newpassword.NewPasswordScreen
import com.mobile.memorise.ui.screen.password.successupdatepassword.PasswordUpdateSuccessPopup

// ONBOARDING
import com.mobile.memorise.ui.screen.onboarding.*

@Composable
fun AppNavGraph(
    navController: NavHostController,
    onLogout: () -> Unit
) {

    NavHost(
        navController = navController,
        startDestination = AppRoute.Landing.route
    ) {

        // =========================================================
        // LANDING
        // =========================================================
        composable(AppRoute.Landing.route) {
            LandingScreen(
                onNavigate = { navController.navigate(AppRoute.OnboardingFlow.route) }
            )
        }

        // =========================================================
        // ONBOARDING
        // =========================================================
        composable(AppRoute.OnboardingFlow.route) {
            OnboardingMainScreen(
                onFinished = { },
                onSignUpClick = { navController.navigate(AppRoute.SignUp.route) },
                onLoginClick = { navController.navigate(AppRoute.SignIn.route) }
            )
        }

        // =========================================================
        // SIGN IN
        // =========================================================
        composable(AppRoute.SignIn.route) {
            SignInScreen(
                onSignInSuccess = {
                    navController.navigate(AppRoute.MainEntry.route) {
                        popUpTo(AppRoute.Landing.route) { inclusive = true }
                    }
                },
                onSignUpClick = { navController.navigate(AppRoute.SignUp.route) },
                onForgotPasswordClick = {
                    navController.navigate(AppRoute.ResetPassword.route)
                }
            )
        }

        // =========================================================
        // SIGN UP FLOW
        // =========================================================
        composable(AppRoute.SignUp.route) {
            SignUpScreen(
                onLoginClick = { navController.navigate(AppRoute.SignIn.route) },
                onSignUpSuccess = {
                    navController.navigate(AppRoute.VerificationLinkSent.route)
                }
            )
        }

        composable(AppRoute.VerificationLinkSent.route) {
            VerivicationLinkSentScreen(
                onContinue = {
                    navController.navigate(AppRoute.VerificationSuccess.route)
                }
            )
        }

        composable(AppRoute.VerificationSuccess.route) {
            VerificationSuccessPopup(
                onDone = {
                    navController.navigate(AppRoute.MainEntry.route) {
                        popUpTo(AppRoute.SignUp.route) { inclusive = true }
                    }
                }
            )
        }

        // =========================================================
        // PASSWORD RESET FLOW
        // =========================================================

        // 1. INPUT EMAIL
        composable(AppRoute.ResetPassword.route) {
            ResetPwScreen(
                onBackClick = { navController.popBackStack() },
                onEmailSent = { emailInput ->
                    navController.navigate("${AppRoute.ResetPasswordOtp.route}/$emailInput")
                }
            )
        }

        // 2. INPUT OTP (Menerima parameter {email})
        composable(
            route = "${AppRoute.ResetPasswordOtp.route}/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val emailArg = backStackEntry.arguments?.getString("email") ?: ""

            ResetOtpScreen(
                email = emailArg,
                onBack = { navController.popBackStack() },
                onVerified = { otpCode ->
                    // Kirim OTP Code ke layar New Password
                    navController.navigate("${AppRoute.ResetPasswordNew.route}/$otpCode")
                }
            )
        }

        // 3. NEW PASSWORD (Menerima parameter {otpToken})
        composable(
            route = "${AppRoute.ResetPasswordNew.route}/{otpToken}",
            arguments = listOf(navArgument("otpToken") { type = NavType.StringType })
        ) { backStackEntry ->
            val tokenArg = backStackEntry.arguments?.getString("otpToken") ?: ""

            // 🔥 PERBAIKAN DI SINI: Gunakan NewPasswordScreen, BUKAN ResetOtpScreen 🔥
            NewPasswordScreen(
                otpToken = tokenArg,
                onSuccessReset = {
                    navController.navigate(AppRoute.PasswordUpdateSuccess.route) {
                        popUpTo(AppRoute.ResetPassword.route) { inclusive = true }
                    }
                }
            )
        }

        // 4. SUCCESS POPUP
        composable(AppRoute.PasswordUpdateSuccess.route) {
            PasswordUpdateSuccessPopup(
                onDone = {
                    navController.navigate(AppRoute.SignIn.route) {
                        popUpTo(AppRoute.Landing.route) { inclusive = false }
                    }
                }
            )
        }

        // =========================================================
        // MAIN AREA
        // =========================================================
        composable(AppRoute.MainEntry.route) {
            val mainNavController = rememberNavController()
            MainScreenContent(
                navController = mainNavController,
                onLogout = {
                    navController.navigate(AppRoute.Landing.route) {
                        popUpTo(AppRoute.Landing.route) { inclusive = true }
                    }
                }
            )
        }
    }
}