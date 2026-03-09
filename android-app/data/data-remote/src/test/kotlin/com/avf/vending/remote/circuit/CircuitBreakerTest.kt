package com.avf.vending.remote.circuit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CircuitBreakerTest {
    @Test
    fun `opens after threshold and transitions through half open`() {
        val breaker = CircuitBreaker(
            config = CircuitBreakerConfig(
                failureThreshold = 2,
                resetTimeMs = 5L,
            )
        )

        breaker.recordFailure()
        assertTrue(breaker.isAllowed())

        breaker.recordFailure()
        assertEquals(CircuitBreaker.State.OPEN, breaker.currentState())
        assertFalse(breaker.isAllowed())

        Thread.sleep(10L)

        assertTrue(breaker.isAllowed())
        assertEquals(CircuitBreaker.State.HALF_OPEN, breaker.currentState())

        breaker.recordSuccess()
        assertEquals(CircuitBreaker.State.CLOSED, breaker.currentState())
        assertTrue(breaker.isAllowed())
    }
}
