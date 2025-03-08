package com.chicohan.currencyexchange.data.network

import android.util.Log
import com.chicohan.currencyexchange.domain.model.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

interface SafeApiCall {

    companion object {
        private const val SAFE_API_CALL = "SafeApiCall"
    }

    suspend fun <T> safeApiCall(
        apiCall: suspend () -> T
    ): Resource<T> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiCall.invoke()
                Log.d(SAFE_API_CALL, "API call successful: $response")
                Resource.Success(response)
            } catch (e: Exception) {
                e.printStackTrace()
                when (e) {
                    is HttpException -> {
                        Resource.Error(e.response()?.errorBody().toString(), null)
                    }

                    is IOException -> {
                        Log.e(SAFE_API_CALL, "Network error: ${e.message}")
                        Resource.Error("Network error: ${e.message}", null)
                    }

                    else -> {
                        Log.d(SAFE_API_CALL, e.message.toString())
                        Resource.Error(e.message.toString(), null)
                    }
                }
            }
        }
    }
}


