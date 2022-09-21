package com.rickclephas.kmp.nativecoroutines

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class NativeFlowTests {

    @Test
    fun ensureCompletionCallbackIsInvoked() = runTest {
        val flow = flow<RandomValue> { }
        val nativeFlow = flow.asNativeFlow(this)
        val completionCount = atomic(0)
        nativeFlow({ _, _ -> }, { error, _ ->
            assertNull(error, "Flow should complete without an error")
            completionCount.incrementAndGet()
        })
        advanceUntilIdle()
        assertEquals(1, completionCount.value, "Completion callback should be called once")
    }

    @Test
    fun ensureExceptionsAreReceivedAsErrors() = runTest {
        val exception = RandomException()
        val flow = flow<RandomValue> { throw exception }
        val nativeFlow = flow.asNativeFlow(this)
        val completionCount = atomic(0)
        nativeFlow({ _, _ -> }, { error, _ ->
            assertNotNull(error, "Flow should complete with an error")
            val kotlinException = error.kotlinCause
            assertSame(exception, kotlinException, "Kotlin exception should be the same exception")
            completionCount.incrementAndGet()
        })
        advanceUntilIdle()
        assertEquals(1, completionCount.value, "Completion callback should be called once")
    }

    @Test
    fun ensureValuesAreReceived() = runTest {
        val values = listOf(RandomValue(), RandomValue(), RandomValue(), RandomValue())
        val flow = flow { values.forEach { emit(it) } }
        val nativeFlow = flow.asNativeFlow(this)
        val receivedValueCount = atomic(0)
        nativeFlow({ value, _ ->
            assertSame(values[receivedValueCount.value], value, "Received incorrect value")
            receivedValueCount.incrementAndGet()
        }, { _, _ -> })
        advanceUntilIdle()
        assertEquals(
            values.size,
            receivedValueCount.value,
            "Item callback should be called for every value"
        )
    }

    @Test
    fun ensureCollectionIsCancelled() = runTest {
        val flow = MutableSharedFlow<RandomValue>()
        val nativeFlow = flow.asNativeFlow(this)
        val completionCount = atomic(0)
        val cancel = nativeFlow({ _, _ -> }, { error, _ ->
            assertNotNull(error, "Flow should complete with an error")
            val exception = error.kotlinCause
            assertIs<CancellationException>(exception, "Error should contain CancellationException")
            completionCount.incrementAndGet()
        })
        delay(100) // Gives the collection some time to start
        cancel()
        advanceUntilIdle()
        assertEquals(1, completionCount.value, "Completion callback should be called once")
    }
}
