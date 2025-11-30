package com.mobile.memorise.navigation

sealed class AppRoute(val route: String) {

    data object Landing : AppRoute("landing")

    data object Onboarding1 : AppRoute("onboarding1")
    data object Onboarding2 : AppRoute("onboarding2")
    data object Onboarding3 : AppRoute("onboarding3")
    data object Onboarding4 : AppRoute("onboarding4")
    data object Onboarding5 : AppRoute("onboarding5")

    data object SignIn : AppRoute("signin")
    data object SignUp : AppRoute("signup")

    // PASSWORD FLOW
    data object ResetPassword : AppRoute("resetpw")          // halaman input email
    data object ResetPasswordSent : AppRoute("resetpw_sent") // halaman “link sent”
    data object ResetPasswordNew : AppRoute("resetpw_new")   // halaman new password

    data object VerificationInfo : AppRoute("verification_info")
    data object VerificationSuccess : AppRoute("verification_success")
}
