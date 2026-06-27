package com.play.golf.perf.tracker.core.common

import com.play.golf.perf.tracker.core.network.isConnectivityError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.retryWhen
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import java.net.UnknownHostException
import kotlin.math.pow
import kotlin.math.roundToInt

object ResourceFlow {

    /**
     * Wraps a Retrofit [Response] call in a [Flow] of [Resource], with optional
     * exponential-backoff retries on transient network errors.
     *
     * Retry delay schedule (500ms base):
     *   attempt 0 → 500ms, attempt 1 → 1000ms, attempt 2 → 2000ms, …
     *
     * @param requestAction        suspend lambda that performs the actual API call
     * @param requestDescription   human-readable label used in log / error messages
     * @param isSuccessResponse    custom success predicate (default: Response.isSuccessful)
     * @param retryCount           maximum number of retries (0 = no retry)
     */
    fun <T> dispatchWithRetry(
        requestAction: suspend () -> Response<T>,
        requestDescription: String? = null,
        isSuccessResponse: (Response<T>) -> Boolean = { it.isSuccessful },
        retryCount: Int = 0,
    ): Flow<Resource<T>> {
        require(retryCount >= 0) { "retryCount must be >= 0" }

        val requestIdentifier = if (requestDescription.isNullOrBlank()) "a request"
        else requestDescription

        val requestExceptionMsg = "Error when dispatching $requestIdentifier"

        // Mutable retry tracking. Scoped to this flow instance
        var retried: Int? = null
        var nextRetryAfter: Int?

        return flow {
            emit(Resource.Loading())

            flow {
                emit(requestAction())
            }
                .flowOn(Dispatchers.IO)
                .retryWhen { cause, attempt ->
                    val isRetryable = cause is HttpException
                            || cause is UnknownHostException
                            || cause is IOException

                    if (isRetryable && attempt < retryCount) {
                        retried = retried?.inc() ?: 0
                        // Exponential backoff: 500ms, 1000ms, 2000ms, 4000ms …
                        nextRetryAfter = (500 * (2.0.pow(retried!!.toDouble()))).roundToInt()
                        Timber.w(
                            "%snull", "ResourceFlow: retrying $requestIdentifier " +
                                    "(attempt ${attempt + 1}/$retryCount) "
                        )
                        delay(nextRetryAfter!!.toLong())
                        true
                    } else {
                        false
                    }
                }
                .collect { response ->
                    if (isSuccessResponse(response)) {
                        Timber.d("ResourceFlow: $requestIdentifier succeeded")
                        emit(Resource.Success(response.body()))
                    } else {
                        Timber.w(
                            "ResourceFlow: $requestIdentifier failed " +
                                    "— HTTP ${response.code()} ${response.message()}"
                        )
                        emit(
                            Resource.Error(
                                message   = response.message(),
                                data      = response.body(),
                                errorCode = response.code(),
                            )
                        )
                    }
                }
        }.catch { throwable ->
            Timber.e(throwable, "ResourceFlow: $requestExceptionMsg")
            emit(
                Resource.Error(
                    message              = requestExceptionMsg,
                    isConnectivityError  = throwable.isConnectivityError()
                )
            )
        }
    }
}