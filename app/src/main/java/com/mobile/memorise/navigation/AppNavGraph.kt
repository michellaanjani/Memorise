package com.mobile.memorise.navigation

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

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
        startDestination = "landing"
    ) {

        // =========================================================
        // LANDING
        // =========================================================
        composable("landing") {
            LandingScreen(
                onNavigate = { navController.navigate("onboarding1") }
            )
        }

        // =========================================================
        // ONBOARDING (1–5)
        // =========================================================
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

        // =========================================================
        // SIGN IN
        // =========================================================
        composable("signin") {
            SignInScreen(
                onSignInSuccess = {
                    navController.navigate("main_entry") {
                        popUpTo("landing") { inclusive = true }
                    }
                },
                onSignUpClick = { navController.navigate("signup") },
                onForgotPasswordClick = {
                    navController.navigate("reset_password")
                }
            )
        }

        // =========================================================
        // SIGN UP FLOW
        // =========================================================
        composable("signup") {
            SignUpScreen(
                onLoginClick = { navController.navigate("signin") },
                onSignUpSuccess = {
                    navController.navigate("verification_link_sent")
                }
            )
        }

        // EMAIL VERIFICATION LINK SENT
        composable("verification_link_sent") {
            VerivicationLinkSentScreen(
                onContinue = {
                    // ⬅️ FIX: sebelumnya langsung ke signin, ini salah
                    navController.navigate("verification_success")
                }
            )
        }

        // VERIFICATION SUCCESS PAGE
        composable("verification_success") {
            VerificationSuccessPopup(
                onDone = {
                    navController.navigate("signin") {
                        popUpTo("signup") { inclusive = true }
                    }
                }
            )
        }

        // =========================================================
        // PASSWORD RESET FLOW
        // =========================================================
        composable("reset_password") {
            ResetPwScreen(
                onBackClick = { navController.popBackStack() },
                onEmailSent = {
                    navController.navigate("reset_password_otp")
                }
            )
        }

        composable("reset_password_otp") {
            ResetOtpScreen(
                onBack = { navController.popBackStack() },
                onVerified = {
                    navController.navigate("reset_password_new")
                }
            )
        }

        composable("reset_password_new") {
            NewPasswordScreen(
                onSuccess = {
                    navController.navigate("password_update_success") {
                        popUpTo("reset_password") { inclusive = true }
                    }
                },
                onBackToResetPw = {
                    navController.navigate("reset_password") {
                        popUpTo("reset_password")
                    }
                }
            )
        }

        composable("password_update_success") {
            PasswordUpdateSuccessPopup(
                onDone = { navController.navigate("signin") }
            )
        }

        // =========================================================
        // MAIN AREA (BOTTOM NAV)
        // =========================================================
        composable("main_entry") {
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
