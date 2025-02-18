package ie.setu.project.api.weather

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val BASE_URL = "https://api.weatherapi.com/v1/"

    val api: WeatherApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)  // Base URL for WeatherAPI
            .addConverterFactory(GsonConverterFactory.create())  // Using Gson to parse JSON
            .build()

        retrofit.create(WeatherApiService::class.java)  // Create the API service
    }
}