package com.play.golf.perf.tracker.core.common

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ResourceStateTest {

    // Loading

    @Test
    fun `Resource Loading maps to ResourceState with isLoading true`() = runTest {
        val resource: Resource<String> = Resource.Loading()
        val state = resource.toResourceState<String, String>()

        assertThat(state.isLoading).isTrue()
        assertThat(state.data).isNull()
        assertThat(state.hasError).isFalse()
    }

    // Success

    @Test
    fun `Resource Success maps to ResourceState with correct data`() = runTest {
        val resource: Resource<String> = Resource.Success("hello")
        val state = resource.toResourceState<String, String>()

        assertThat(state.isLoading).isFalse()
        assertThat(state.hasError).isFalse()
        assertThat(state.data).isEqualTo("hello")
    }

    @Test
    fun `Resource Success applies mapper function to transform data`() = runTest {
        val resource: Resource<Int> = Resource.Success(5)
        val state = resource.toResourceState<Int, String>(
            mapper = { number -> "value is $number" }
        )

        assertThat(state.data).isEqualTo("value is 5")
    }

    @Test
    fun `Resource Success with null body maps to ResourceState with null data`() = runTest {
        val resource: Resource<String> = Resource.Success(null)
        val state = resource.toResourceState<String, String>()

        assertThat(state.data).isNull()
        assertThat(state.hasError).isFalse()
        assertThat(state.isLoading).isFalse()
    }

    @Test
    fun `Resource Success invokes onSuccess callback with mapped data`() = runTest {
        var callbackData: String? = null
        val resource: Resource<Int> = Resource.Success(10)

        resource.toResourceState<Int, String>(
            mapper    = { it.toString() },
            onSuccess = { mapped -> callbackData = mapped }
        )

        assertThat(callbackData).isEqualTo("10")
    }

    // Error

    @Test
    fun `Resource Error maps to ResourceState with hasError true`() = runTest {
        val resource: Resource<String> = Resource.Error(message = "something went wrong")
        val state = resource.toResourceState<String, String>()

        assertThat(state.hasError).isTrue()
        assertThat(state.isLoading).isFalse()
        assertThat(state.data).isNull()
    }

    @Test
    fun `Resource Error carries error message`() = runTest {
        val resource: Resource<String> = Resource.Error(message = "network failed")
        val state = resource.toResourceState<String, String>()

        assertThat(state.errorMessage).isEqualTo("network failed")
    }

    @Test
    fun `Resource Error with isConnectivityError propagates flag`() = runTest {
        val resource: Resource<String> = Resource.Error(
            message             = "no network",
            isConnectivityError = true,
        )
        val state = resource.toResourceState<String, String>()

        assertThat(state.isConnectivityError).isTrue()
    }

    @Test
    fun `Resource Error with isShowErrorDialog propagates flag`() = runTest {
        val resource: Resource<String> = Resource.Error(message = "error")
        val state = resource.toResourceState<String, String>(isShowErrorDialog = true)

        assertThat(state.isShowErrorDialog).isTrue()
    }

    // Flow sequence test (uses Turbine)

    @Test
    fun `flow emitting Loading then Success produces correct ResourceState sequence`() = runTest {
        flow {
            emit(Resource.Loading<String>())
            emit(Resource.Success("final"))
        }.test {
            val loadingResource = awaitItem()
            assertThat(loadingResource).isInstanceOf(Resource.Loading::class.java)

            val successResource = awaitItem()
            assertThat(successResource).isInstanceOf(Resource.Success::class.java)
            assertThat((successResource as Resource.Success).data).isEqualTo("final")

            awaitComplete()
        }
    }

    @Test
    fun `flow emitting Loading then Error produces correct ResourceState sequence`() = runTest {
        flow {
            emit(Resource.Loading<String>())
            emit(Resource.Error<String>("oops"))
        }.test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)
            val error = awaitItem() as Resource.Error
            assertThat(error.message).isEqualTo("oops")
            awaitComplete()
        }
    }
}