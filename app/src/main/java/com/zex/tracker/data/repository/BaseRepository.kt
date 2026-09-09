package com.zex.tracker.data.repository

import com.zex.tracker.core.logging.ZexLogger
import com.zex.tracker.data.remote.ApiResult
import retrofit2.Response

abstract class BaseRepository {
    protected suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): ApiResult<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    ApiResult.Success(body)
                } else {
                    ApiResult.Error("Empty response body")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("ZEX_API", "Error Body: " + errorBody)
                ZexLogger.w("BaseRepository", "API Error: ${response.code()} - $errorBody")
                val msg = parseError(response.code(), errorBody)
                ApiResult.Error(msg, response.code())
            }
        } catch (e: Exception) {
            ZexLogger.e("BaseRepository", "API Exception", e)
            ApiResult.Error(e.message ?: "Unknown error")
        }
    }

    private fun parseError(code: Int, errorBody: String?): String {
        return when (code) {
            401 -> "Unauthorized. Please login again."
            422 -> "Validation failed. Please check your inputs."
            500 -> "Server error. Please try again later."
            else -> "Error: $code. $errorBody"
        }
    }
}
