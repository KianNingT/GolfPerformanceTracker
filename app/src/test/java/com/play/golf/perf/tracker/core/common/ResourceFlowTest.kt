package com.play.golf.perf.tracker.core.common

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.Response
import java.net.UnknownHostException

class ResourceFlowTest {

    // Helpers

    private fun <T> successResponse(body: T): Response<T> = Response.success(body)

    private fun <T> errorResponse(code: Int = 500): Response<T> =
        Response.error(code, "error".toResponseBody())

    // Success

    @Test
    fun `emits Loading then Success on successful response`() = runTest {
        val requestAction = mockk<suspend () -> Response<String>>()
        coEvery { requestAction() } returns successResponse("ok")

        ResourceFlow.dispatchWithRetry(
            requestAction      = requestAction,
            requestDescription = "test request",
        ).test {
            val loading = awaitItem()
            assertThat(loading).isInstanceOf(Resource.Loading::class.java)

            val success = awaitItem()
            assertThat(success).isInstanceOf(Resource.Success::class.java)
            assertThat((success as Resource.Success).data).isEqualTo("ok")

            awaitComplete()
        }
    }

    @Test
    fun `Success data matches response body`() = runTest {
        val requestAction = mockk<suspend () -> Response<Int>>()
        coEvery { requestAction() } returns successResponse(42)

        ResourceFlow.dispatchWithRetry(requestAction = requestAction).test {
            awaitItem() // Loading
            val success = awaitItem() as Resource.Success
            assertThat(success.data).isEqualTo(42)
            awaitComplete()
        }
    }

    // HTTP Error

    @Test
    fun `emits Loading then Error on non-successful HTTP response`() = runTest {
        val requestAction = mockk<suspend () -> Response<String>>()
        coEvery { requestAction() } returns errorResponse(404)

        ResourceFlow.dispatchWithRetry(requestAction = requestAction).test {
            awaitItem() // Loading
            val error = awaitItem()
            assertThat(error).isInstanceOf(Resource.Error::class.java)
            awaitComplete()
        }
    }

    @Test
    fun `Error resource carries HTTP error code`() = runTest {
        val requestAction = mockk<suspend () -> Response<String>>()
        coEvery { requestAction() } returns errorResponse(503)

        ResourceFlow.dispatchWithRetry(requestAction = requestAction).test {
            awaitItem() // Loading
            val error = awaitItem() as Resource.Error
            assertThat(error.errorCode).isEqualTo(503)
            awaitComplete()
        }
    }

    // Connectivity Error

    @Test
    fun `emits Error with isConnectivityError true on UnknownHostException`() = runTest {
        val requestAction = mockk<suspend () -> Response<String>>()
        coEvery { requestAction() } throws UnknownHostException("no network")

        ResourceFlow.dispatchWithRetry(
            requestAction = requestAction,
            retryCount    = 0,
        ).test {
            awaitItem() // Loading
            val error = awaitItem() as Resource.Error
            assertThat(error.isConnectivityError).isTrue()
            awaitComplete()
        }
    }

    @Test
    fun `emits Error with isConnectivityError true on ConnectException`() = runTest {
        val requestAction = mockk<suspend () -> Response<String>>()
        coEvery { requestAction() } throws java.net.ConnectException("refused")

        ResourceFlow.dispatchWithRetry(
            requestAction = requestAction,
            retryCount    = 0,
        ).test {
            awaitItem() // Loading
            val error = awaitItem() as Resource.Error
            assertThat(error.isConnectivityError).isTrue()
            awaitComplete()
        }
    }

    // Retry Logic

    @Test
    fun `retries specified number of times on UnknownHostException`() = runTest {
        val requestAction = mockk<suspend () -> Response<String>>()
        coEvery { requestAction() } throws UnknownHostException("no network")

        ResourceFlow.dispatchWithRetry(
            requestAction = requestAction,
            retryCount    = 2,
        ).test {
            awaitItem() // Loading
            awaitItem() // eventual Error after retries
            awaitComplete()
        }

        // initial attempt + 2 retries = 3 total calls
        coVerify(exactly = 3) { requestAction() }
    }

    @Test
    fun `succeeds on second attempt after initial failure`() = runTest {
        val requestAction = mockk<suspend () -> Response<String>>()
        coEvery { requestAction() }
            .throwsMany(listOf(UnknownHostException("no network")))
            .andThen(successResponse("recovered"))

        ResourceFlow.dispatchWithRetry(
            requestAction = requestAction,
            retryCount    = 2,
        ).test {
            awaitItem() // Loading
            val result = awaitItem()
            assertThat(result).isInstanceOf(Resource.Success::class.java)
            assertThat((result as Resource.Success).data).isEqualTo("recovered")
            awaitComplete()
        }

        coVerify(exactly = 2) { requestAction() }
    }

    @Test
    fun `does not retry when retryCount is 0`() = runTest {
        val requestAction = mockk<suspend () -> Response<String>>()
        coEvery { requestAction() } throws UnknownHostException("no network")

        ResourceFlow.dispatchWithRetry(
            requestAction = requestAction,
            retryCount    = 0,
        ).test {
            awaitItem() // Loading
            awaitItem() // Error
            awaitComplete()
        }

        coVerify(exactly = 1) { requestAction() }
    }

    // Custom Success Predicate

    @Test
    fun `uses custom isSuccessResponse predicate`() = runTest {
        val requestAction = mockk<suspend () -> Response<String>>()
        // HTTP 200 but we treat it as error based on custom logic
        coEvery { requestAction() } returns successResponse("INVALID")

        ResourceFlow.dispatchWithRetry(
            requestAction      = requestAction,
            isSuccessResponse  = { response ->
                response.isSuccessful && response.body() != "INVALID"
            },
        ).test {
            awaitItem() // Loading
            val result = awaitItem()
            assertThat(result).isInstanceOf(Resource.Error::class.java)
            awaitComplete()
        }
    }

    // Flow Completion

    @Test
    fun `flow completes after emitting success`() = runTest {
        val requestAction = mockk<suspend () -> Response<String>>()
        coEvery { requestAction() } returns successResponse("done")

        var completed = false
        ResourceFlow.dispatchWithRetry(requestAction = requestAction).test {
            awaitItem() // Loading
            awaitItem() // Success
            awaitComplete()
            completed = true
        }

        assertThat(completed).isTrue()
    }

    @Test
    fun `flow completes after emitting error`() = runTest {
        val requestAction = mockk<suspend () -> Response<String>>()
        coEvery { requestAction() } returns errorResponse(400)

        var completed = false
        ResourceFlow.dispatchWithRetry(requestAction = requestAction).test {
            awaitItem() // Loading
            awaitItem() // Error
            awaitComplete()
            completed = true
        }

        assertThat(completed).isTrue()
    }
}