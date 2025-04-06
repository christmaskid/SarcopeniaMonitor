package com.example.sarcopeniamonitor
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Url

interface ApiService {
    @GET("app/user-records/")
    fun getUserRecords(): Call<List<UserRecord>>  // Ensure this fetches meal_images as part of UserRecord

    @POST("app/user-records/")
    fun postUserRecord(@Body userRecord: UserRecord): Call<UserRecord>

    @PUT("app/user-records/{id}/")
    fun updateUserRecord(@Path("id") id: Int, @Body record: UserRecord): Call<UserRecord>

    @POST("app/questionnaires/")
    fun submitQuestionnaireData(@Body questionnaireData: QuestionnaireData): Call<QuestionnaireResponse>

    @GET("app/questionnaires/latest/")
    fun getLatestQuestionnaireData(): Call<QuestionnaireResponse>

    @GET("app/predictions/latest")
    fun getLatestPrediction(): Call<Prediction>

    @Multipart
    @POST("app/user-records/upload-meal-image/")
    fun uploadImage(@Part image: MultipartBody.Part): Call<ResponseBody>

    @GET
    fun fetchImage(@Url imageUrl: String): Call<ResponseBody>  // Fetch image content by URL
}