package com.play.golf.perf.tracker.core.common

data class ResourceState<T>(
    val data: T? = null,
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val errorMessage: String? = null,
    val isShowErrorDialog: Boolean = false,
    val fieldErrors: FieldErrors? = null,
    val isConnectivityError: Boolean = false
)

/**
 * Maps a [Resource] emission into a [ResourceState] ready for the UI layer.
 *
 * @param isShowErrorDialog  whether the error should trigger a dialog in the UI
 * @param mapper             optional transform from the raw API type [T] to the UI type [U]
 * @param onSuccess          optional side-effect invoked after a successful mapping
 */
suspend fun <T, U> Resource<T>.toResourceState(
    isShowErrorDialog: Boolean = false,
    mapper: ((T) -> U)? = null,
    onSuccess: (suspend (U?) -> Unit)? = null
): ResourceState<U> {
    return when (this) {
        is Resource.Loading -> ResourceState(
            isLoading = true
        )

        is Resource.Error -> ResourceState(
            hasError           = true,
            errorMessage       = this.message,
            isShowErrorDialog  = isShowErrorDialog,
            fieldErrors        = this.fieldErrors,
            isConnectivityError = this.isConnectivityError
        )

        is Resource.Success -> {
            val mappedData: U? = this.data?.let {
                if (mapper == null) {
                    // Safe cast fallback. Only valid when T and U are the same type
                    @Suppress("UNCHECKED_CAST")
                    it as? U
                } else {
                    mapper(it)
                }
            }
            onSuccess?.invoke(mappedData)
            ResourceState(data = mappedData)
        }
    }
}