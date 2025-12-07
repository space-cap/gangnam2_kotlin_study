package com.ezlevup.my.day251205.exercise

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test


class Ex1NumbersTest {

    @Test
    fun `짝수에 10 곱하기`(): Unit = runBlocking {
        // when
        val result = mutableListOf<Int>()
        ex1Numbers().collect { value ->
            result.add(value)
        }

        // then
        assertEquals(listOf(20, 40), result)
    }
}