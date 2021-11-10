package com.example.sudokusolver

import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(JUnitParamsRunner::class)
class ATest {
    @Test
    @Parameters(method = "easyParameters")
    fun test_easyBoard_removeUsedValues(index: Int, expected: Array<Int>) {
        val easyBoard = arrayOf(
            arrayOf(0,0,0,2,6,0,7,0,1),
            arrayOf(6,8,0,0,7,0,0,9,0),
            arrayOf(1,9,0,0,0,4,5,0,0),
            arrayOf(8,2,0,1,0,0,0,4,0),
            arrayOf(0,0,4,6,0,2,9,0,0),
            arrayOf(0,5,0,0,0,3,0,2,8),
            arrayOf(0,0,9,3,0,0,0,7,4),
            arrayOf(0,4,0,0,5,0,0,3,6),
            arrayOf(7,0,3,0,1,8,0,0,0)
        )
        val result = SudokuSolver.removeUsedValues(index, easyBoard)

        assertEquals(expected.toList(), result)
    }

    fun easyParameters() = arrayOf(
            arrayOf(13, arrayOf(7)),
            arrayOf(29, arrayOf(6,7)),
            arrayOf(43, arrayOf(1,5)),
            arrayOf(55, arrayOf(1,6)),
            arrayOf(62, arrayOf(4)),
            arrayOf(73, arrayOf(6))
        )

    @Test
    @Parameters(method = "mediumParameters")
    fun test_mediumBoard_removeUsedValues(index: Int, expected: Array<Int>) {
        val mediumBoard = arrayOf(
            arrayOf(0,0,0,6,0,0,4,0,0),
            arrayOf(7,0,0,0,0,3,6,0,0),
            arrayOf(0,0,0,0,9,1,0,8,0),
            arrayOf(0,0,0,0,0,0,0,0,0),
            arrayOf(0,5,0,1,8,0,0,0,3),
            arrayOf(0,0,0,3,0,6,0,4,5),
            arrayOf(0,4,0,2,0,0,0,6,0),
            arrayOf(9,0,3,0,0,0,0,0,0),
            arrayOf(0,2,0,0,0,0,1,0,0)
        )
        val result = SudokuSolver.removeUsedValues(index, mediumBoard)

        assertEquals(expected.toList(), result)
    }

    fun mediumParameters() = arrayOf(
        arrayOf(13, arrayOf(2,4,5)),
        arrayOf(29, arrayOf(1,2,4,6,7,8,9)),
        arrayOf(43, arrayOf(2,7,9)),
        arrayOf(55, arrayOf(4)),
        arrayOf(62, arrayOf(7,8,9)),
        arrayOf(73, arrayOf(2))
    )

    @Test
    @Parameters(method = "hardParameters")
    fun test_hardBoard_removeUsedValues(index: Int, expected: Array<Int>) {
        val hardBoard = arrayOf(
            arrayOf(0,0,0,8,0,1,0,0,0),
            arrayOf(0,0,0,0,0,0,4,3,0),
            arrayOf(5,0,0,0,0,0,0,0,0),
            arrayOf(0,0,0,0,7,0,8,0,0),
            arrayOf(0,0,0,0,0,0,1,0,0),
            arrayOf(0,2,0,0,3,0,0,0,0),
            arrayOf(6,0,0,0,0,0,0,7,5),
            arrayOf(0,0,3,4,0,0,0,0,0),
            arrayOf(0,0,0,2,0,0,6,0,0)
        )
        val result = SudokuSolver.removeUsedValues(index, hardBoard)

        assertEquals(expected.toList(), result)
    }

    fun hardParameters() = arrayOf(
        arrayOf(13, arrayOf(2,5,6,9)),
        arrayOf(29, arrayOf(1,4,5,6,9)),
        arrayOf(43, arrayOf(2,4,5,6,9)),
        arrayOf(55, arrayOf(1,4,8,9)),
        arrayOf(62, arrayOf(5)),
        arrayOf(73, arrayOf(1,4,5,7,8,9))
    )
}
