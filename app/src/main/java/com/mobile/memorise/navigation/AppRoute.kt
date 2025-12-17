sealed class AppRoute(val route: String) {

    // Landing
    data object Landing : AppRoute("landing")

    // Onboarding
//    data object Onboarding1 : AppRoute("onboarding1")
//    data object Onboarding2 : AppRoute("onboarding2")
//    data object Onboarding3 : AppRoute("onboarding3")
//    data object Onboarding4 : AppRoute("onboarding4")
//    data object Onboarding5 : AppRoute("onboarding5")
    // Onboarding (GABUNGAN)
    data object OnboardingFlow : AppRoute("onboarding_flow")

    // Auth
    data object SignIn : AppRoute("signin")
    data object SignUp : AppRoute("signup")

    // Signup Verification
    data object VerificationLinkSent : AppRoute("verification_link_sent")
    data object VerificationSuccess : AppRoute("verification_success")

    // Reset Password
    data object ResetPassword : AppRoute("reset_password")
    data object ResetPasswordOtp : AppRoute("reset_password_otp")
    data object ResetPasswordNew : AppRoute("reset_password_new")
    data object PasswordUpdateSuccess : AppRoute("password_update_success")

    // Main
    data object MainEntry : AppRoute("main_entry")
}
