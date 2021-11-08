package com.example.sudokusolver

import android.util.Log
import java.lang.Math.sqrt

object dumbVer {
    // solved?
    var status = false
    var cells = 81
    var rows = 9
    var columns = rows
    var squareSides = sqrt(rows.toDouble()).toInt()
    var maxIndex = 0

    var grid = arrayOf<Array<Int>>()
    // Own function - fills grid from list we pass in
    fun fill(array: Array<Array<Int>>) {
        grid = array
    }
    fun printBoard(board: Array<Array<Int>>) {
        for (i in board.indices) {
            Log.i("Board: ", board[i].contentToString())
        }
    }

    // gets pretty far but still not working, let's fix later!
    fun solve(index: Int,  board: Array<Array<Int>>): Pair<Array<Array<Int>>, Boolean> {
        // for getting best board
        /*
        if (index > maxIndex && index > 50) {
            Log.i("Highest index: ", index.toString())
            maxIndex = index
        }

         */
        val range = removeUsedValues(index, board)
        // check if we got a good board
        var result = Pair(board, status)
        var rowIndex = index / rows
        var colIndex = index % columns
        var newBoard = copyArray(board)

        for (candidateValue in range) {
            newBoard[rowIndex][colIndex] = candidateValue
            if(index == 80) {
                status = true
                return Pair(newBoard, status)
            }
            var temp = solve(index + 1, newBoard)
            if(temp.second == true) {
                return temp
            }
        }

        // does not update if board is unsolvable!!
        return result
    }

    private fun copyArray(old: Array<Array<Int>>): Array<Array<Int>> {
        //var newArray = arrayOf<Array<Int>>()
        val newArray: Array<Array<Int>> = Array<Array<Int>>(9){ Array<Int>(9) {0} }
        for (i in (0..8)) {
            newArray[i] = old[i].copyOf()
        }
        return newArray
    }

    fun removeUsedValues(index: Int, board: Array<Array<Int>>): List<Int> {
        var rowIndex = index / rows
        var colIndex = index % columns
        // If square is prefilled, return only that value
        if(board[rowIndex][colIndex] != 0) {
            return listOf(board[rowIndex][colIndex])
        }

        var noRow = removeRow(index, board)
        var noCol = removeCol(noRow, index, board)
        var noSquare = removeSquare(noCol, index, board)

        return noSquare
    }

    // WORKS
    fun removeRow(index: Int, board: Array<Array<Int>>): MutableList<Int> {
        var rowIndex = index / rows

        var newRange: MutableList<Int> = (1..9).toMutableList()

        board[rowIndex].forEach {
            if (it != 0) {
                newRange.remove(it)
            }
        }

        return newRange
    }

    fun removeCol (range: MutableList<Int>, index: Int, board: Array<Array<Int>>): MutableList<Int> {
        var colIndex = index % columns
        var newRange: MutableList<Int> = range

        board.forEach {
            if (it[colIndex] != 0) {
                newRange.remove(it[colIndex])
            }
        }

        return newRange
    }

    fun removeSquare (range: MutableList<Int>, index: Int, board: Array<Array<Int>>): MutableList<Int> {
        val rowStart = findBoxStart(index/rows)
        val rowEnd = findBoxEnd(rowStart)
        val columnStart = findBoxStart(index%columns)
        val columnEnd = findBoxEnd(columnStart)

        var newRange: MutableList<Int> = range

        for (i in rowStart until rowEnd) {
            for (j in columnStart until columnEnd) {
                if (board[i][j] != 0) {
                    newRange.remove(board[i][j])
                }
            }
        }

        return newRange
    }

    private fun findBoxStart(index: Int) = index - index % squareSides

    private fun findBoxEnd(index: Int) = index + squareSides

}