// Inspiration: https://github.com/TypicalDevStuff/sudoku-generator
package com.example.sudokusolver

import android.content.Context

class SudokuSolver constructor(private val context: Context) {
    var status = false
    var error: String? = null
    var rows = 9
    var columns = rows
    var squareSides = kotlin.math.sqrt(rows.toDouble()).toInt()
    var indexList = mutableListOf<Pair<Int, Int>>()

    var grid = arrayOf<Array<Int>>()
    var finishedBoard = arrayOf<Array<Int>>()

    // initializes our SudokuSolver
    fun init(array: Array<Int>): Pair<Array<Int>, String?> {
        grid = parse1Dto2D(array)
        // Check that the board isn't already full
        if (!array.contains(0)) {
            setError(R.string.algorithm_error_board_full)
            return Pair(array, error)
        }
        return (solve())
    }

    private fun setError(key: Int) {
        error = context.getString(key)
    }

    // Our handler function that performs transformations and runs traverse
    fun solve(): Pair<Array<Int>, String?> {
        getIndex(grid)
        traverse(0, grid)
        if (!status) {
            setError(R.string.algorithm_error_correct_board)
        }
        if (error != null) {
            return Pair(parse2Dto1D(grid), error)
        } else {
            return Pair(parse2Dto1D(finishedBoard), error)
        }
    }

    // Gets the indices of our 1D array that are empty, and therefore need to be filled
    // This way we don't waste resources/memory on traversing through already filled
    // indices
    private fun getIndex(board: Array<Array<Int>>) {
        var rowI = 0
        var colI = 0
        for (i in (0..80)) {
            rowI = i / rows
            colI = i % columns
            // only add it empty cell
            if (board[rowI][colI] == 0) {
                indexList.add(Pair(i, removeUsedValues(i, board).count()))
            }
        }
        // Worked on sorting by size, but both starting lowest and highest failed/were slow
        // indexList.sortByDescending { it.second }
    }

    // gets pretty far but still not working, let's fix later!
    private fun traverse(index: Int, board: Array<Array<Int>>): Boolean {
        // the index we want to check is stored in indexList, so we extract it
        var realI = indexList.elementAt(index).first
        val range = removeUsedValues(realI, board)
        // the index we want to check is stored in indexList, so we extract it
        var newBoard = copyArray(board)

        for (candidateValue in range) {
            // check if realI or I
            newBoard[rowI(realI)][colI(realI)] = candidateValue
            if (index == indexList.count() - 1) {
                finishedBoard = newBoard
                status = true
                return status
            }
            traverse(index + 1, newBoard)
            if (status == true) {
                return status
            }
        }
        // does not update if board is unsolvable!!
        return status
    }

    // Performs copying (rather than simply referencing) board
    private fun copyArray(old: Array<Array<Int>>): Array<Array<Int>> {
        val newArray: Array<Array<Int>> = Array(9) { Array(9) { 0 } }
        for (i in (0..8)) {
            newArray[i] = old[i].copyOf()
        }
        return newArray
    }

    private fun removeUsedValues(index: Int, board: Array<Array<Int>>): List<Int> {
        // If square is prefilled, return only that value
        if (board[rowI(index)][colI(index)] != 0) {
            return listOf(board[rowI(index)][colI(index)])
        }

        var noRow = removeRow(index, board)
        var noCol = removeCol(noRow, index, board)
        var noSquare = removeSquare(noCol, index, board)

        return noSquare
    }

    // Removes numbers in the same row
    private fun removeRow(index: Int, board: Array<Array<Int>>): MutableList<Int> {
        var newRange: MutableList<Int> = (1..9).toMutableList()

        board[rowI(index)].forEach {
            if (it != 0) {
                newRange.remove(it)
            }
        }

        return newRange
    }

    // Removes numbers in the same column
    private fun removeCol(range: MutableList<Int>, index: Int, board: Array<Array<Int>>): MutableList<Int> {
        var newRange: MutableList<Int> = range

        board.forEach {
            if (it[colI(index)] != 0) {
                newRange.remove(it[colI(index)])
            }
        }
        return newRange
    }

    // Removes numbers in the same 3x3 square
    private fun removeSquare(
        range: MutableList<Int>,
        index: Int,
        board: Array<Array<Int>>
    ): MutableList<Int> {
        val rowStart = findBoxStart(rowI(index))
        val rowEnd = findBoxEnd(rowStart)
        val columnStart = findBoxStart(colI(index))
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

    // Functions to get row and col index from 1D
    private fun rowI(index: Int): Int {
        return index / rows
    }
    private fun colI(index: Int): Int {
        return index % columns
    }

    // helper functions for removeSquare
    private fun findBoxStart(index: Int) = index - index % squareSides
    private fun findBoxEnd(index: Int) = index + squareSides

    // Parser function from array to 2D array
    private fun parse1Dto2D(oneD: Array<Int>): Array<Array<Int>> {
        var newgrid = Array(9) {
            Array(9, { 0 })
        }
        for (i in oneD.indices) {
            newgrid[rowI(i)][colI(i)] = oneD[i]
        }
        return newgrid
    }

    // Parser function from 2D array to array
    private fun parse2Dto1D(twoD: Array<Array<Int>>): Array<Int> {
        var newgrid = Array(81) { 0 }
        for (i in (0 until rows)) {
            for (j in (0 until columns)) {
                newgrid[i * (rows) + j] = twoD[i][j]
            }
        }
        return newgrid
    }

    // Checks that each box sees every number in row, column and square
    private fun checkValid(board: Array<Array<Int>>): Boolean {
        for (i in (0 until rows)) {
            for (j in (0 until columns)) {
                if (removeRow(board[i][j], board).isNotEmpty() ||
                    removeCol((1..9).toMutableList(), board[i][j], board).isNotEmpty() ||
                    removeSquare((1..9).toMutableList(), board[i][j], board).isNotEmpty()
                    ) {
                    return false
                }
            }
        }
        return true
    }
}
