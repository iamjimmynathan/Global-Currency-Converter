package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class OpenErApiResponse(
    @field:Json(name = "result") val result: String?,
    @field:Json(name = "base_code") val baseCode: String?,
    @field:Json(name = "time_last_update_unix") val timeLastUpdateUnix: Long?,
    @field:Json(name = "rates") val rates: Map<String, Double>?
)

interface OpenErApiService {
    @GET("v6/latest/{base}")
    suspend fun getLatestRates(@Path("base") base: String = "USD"): OpenErApiResponse
}

/**
 * Interface abstraction for exchange rate providers.
 * Makes it trivial to swap or fallback between different rate sources.
 */
interface ExchangeRateProvider {
    val providerName: String
    suspend fun getRates(baseCurrency: String = "USD"): Result<Map<String, Double>>
}

class OpenErApiProvider : ExchangeRateProvider {
    override val providerName: String = "Open Exchange Rates API"

    private val api: OpenErApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("https://open.er-api.com/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(OpenErApiService::class.java)
    }

    override suspend fun getRates(baseCurrency: String): Result<Map<String, Double>> {
        return try {
            val response = api.getLatestRates(baseCurrency)
            val rates = response.rates
            if (!rates.isNullOrEmpty()) {
                Result.success(rates)
            } else {
                Result.failure(IllegalStateException("No rates returned from API"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
