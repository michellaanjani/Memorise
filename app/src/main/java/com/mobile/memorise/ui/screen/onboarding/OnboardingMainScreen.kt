package com.mobile.memorise.ui.screen.onboarding

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

@Composable
fun OnboardingMainScreen(
    onFinished: () -> Unit,      // Callback saat user klik Login/Signup di hal 5
    onLoginClick: () -> Unit,    // Callback khusus Login
    onSignUpClick: () -> Unit    // Callback khusus Signup
) {
    // 1. Inisialisasi State Pager (Jumlah halaman = 5)
    val pagerState = rememberPagerState(pageCount = { 5 })
    val scope = rememberCoroutineScope()

    // 2. Komponen Pager
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        // 'page' adalah index halaman saat ini (0 sampai 4)

        when (page) {
            0 -> OnboardingScreen1(
                onNext = {
                    // Geser ke halaman berikutnya (index 1)
                    scope.launch { pagerState.animateScrollToPage(1) }
                },
                onSkip = {
                    // Langsung lompat ke halaman terakhir (index 4)
                    scope.launch { pagerState.animateScrollToPage(4) }
                }
            )

            1 -> OnboardingScreen2(
                onNext = { scope.launch { pagerState.animateScrollToPage(2) } },
                onSkip = { scope.launch { pagerState.animateScrollToPage(4) } }
            )

            2 -> OnboardingScreen3(
                onNext = { scope.launch { pagerState.animateScrollToPage(3) } },
                onSkip = { scope.launch { pagerState.animateScrollToPage(4) } }
            )

            3 -> OnboardingScreen4(
                onNext = { scope.launch { pagerState.animateScrollToPage(4) } },
                onSkip = { scope.launch { pagerState.animateScrollToPage(4) } }
            )

            4 -> OnboardingScreen5(
                onSignUp = onSignUpClick,
                onLogin = onLoginClick
            )
        }
    }
}