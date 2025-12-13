//package com.mobile.memorise.data.remote.api
//
//import com.mobile.memorise.domain.model.quiz.QuizStartResponse
//import com.mobile.memorise.domain.model.quiz.QuizSubmitRequest
//import com.mobile.memorise.domain.model.quiz.QuizSubmitResponse
//import retrofit2.Response
//import retrofit2.http.Body
//import retrofit2.http.GET
//import retrofit2.http.POST
//import retrofit2.http.Path
//
//interface QuizApi {
//    @GET("quiz/start/{deckId}")
//    suspend fun startQuiz(@Path("deckId") deckId: String): Response<QuizStartResponse>
//
//    @POST("quiz/submit")
//    suspend fun submitQuiz(@Body request: QuizSubmitRequest): Response<QuizSubmitResponse>
//}
