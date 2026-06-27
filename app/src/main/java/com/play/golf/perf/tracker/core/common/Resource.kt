package com.play.golf.perf.tracker.core.common



data class FieldErrors(
    val errors: Map<String, List<String>> = emptyMap()
)

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null,
    val exception: Exception? = null,
    val errorCode: Int? = null,
    val isKickOutUser: Boolean = false,
    val fieldErrors: FieldErrors? = null,
    val isConnectivityError: Boolean = false
) {
    class Success<T>(data: T?) : Resource<T>(data)

    class Error<T>(
        message: String,
        data: T? = null,
        exception: Exception? = null,
        errorCode: Int? = null,
        isKickOutUser: Boolean = false,
        fieldErrors: FieldErrors? = null,
        isConnectivityError: Boolean = false
    ) : Resource<T>(
        data,
        message,
        exception,
        errorCode,
        isKickOutUser,
        fieldErrors,
        isConnectivityError
    )

    class Loading<T>(data: T? = null) : Resource<T>(data)
}