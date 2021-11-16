// Inspiration: https://github.com/TypicalDevStuff/sudoku-generator
package com.example.sudokusolver

import android.content.Context
import android.util.Log
import java.lang.Math.sqrt

class SudokuSolver constructor(private val context: Context) {
    // solved?
    var status = false
    var error: String? = null
    var rows = 9
    var columns = rows
    var squareSides = sqrt(rows.toDouble()).toInt()
    var indexList = mutableListOf<Pair<Int, Int>>()

    var maxIndex = 0

    var grid = arrayOf<Array<Int>>()
    var tempGrid = arrayOf<Array<Int>>()

    fun fill(array: Array<Int>): Pair<Array<Int>, String?> {
        grid = parse1Dto2D(array)
        return (solve())
    }

    fun printBoard(board: Array<Array<Int>>) {
        for (i in board.indices) {
            Log.i("Board: ", board[i].contentToString())
        }
    }

    private fun setError(key: Int) {
        error = context.getString(key)
    }

    fun solve(): Pair<Array<Int>, String?> {
        getIndex(grid)
        var finalBoard = traverse(0, grid)
        status = finalBoard.second
        if(!status){
            setError(R.string.algorithm_error_correct_board)
        }
        if (error != null) {
            Log.e("SudokuSolver", error!!)
            return Pair(parse2Dto1D(grid), error)
        } else {
            return Pair(parse2Dto1D(finalBoard.first), error)
        }
    }

    fun getIndex(board: Array<Array<Int>>) {
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
        // Stops working here...
        // Both sortBy and sortByDescending get stuck in loops
        // But same logic works as long as they're sorted by boardindex...weird
        // indexList.sortByDescending { it.second }
        Log.i("size: ", indexList.count().toString())
    }

    // gets pretty far but still not working, let's fix later!
    fun traverse(index: Int, board: Array<Array<Int>>): Pair<Array<Array<Int>>, Boolean> {
        val range = removeUsedValues(indexList.elementAt(index).first, board)
        // check if we got a good board
        var result = Pair(board, status)
        // the index we want to check is stored in indexList
        var realI = indexList.elementAt(index).first
        var newBoard = copyArray(board)

        for (candidateValue in range) {
            // check if realI or I
            newBoard[rowI(realI)][colI(realI)] = candidateValue
            if (index == indexList.count() - 1) {
                status = true
                return Pair(newBoard, status)
            }
            var temp = traverse(index + 1, newBoard)
            if (temp.second == true) {
                return temp
            }
            // clean up traversed indexes in wrong solutions - not working, think about more
            for(i in (indexList.elementAt(index+1).first until indexList.count()-1)) {
                // when we reach unfilled indexes we can stop
                if (tempGrid[rowI(indexList.elementAt(i).first)][colI(indexList.elementAt(i).first)] == 0) {
                    break
                } else if(tempGrid[rowI(indexList.elementAt(i).first)][colI(indexList.elementAt(i).first)] != grid[rowI(indexList.elementAt(i).first)][colI(indexList.elementAt(i).first)]) {
                    tempGrid[rowI(indexList.elementAt(i).first)][colI(indexList.elementAt(i).first)] = 0
                }
            }
        }
        //printBoard(tempGrid)
        //Log.i("BREAK", "           ")
        // does not update if board is unsolvable!!
        return result
    }

    private fun copyArray(old: Array<Array<Int>>): Array<Array<Int>> {
        val newArray: Array<Array<Int>> = Array<Array<Int>>(9) { Array<Int>(9) { 0 } }
        for (i in (0..8)) {
            newArray[i] = old[i].copyOf()
        }
        return newArray
    }

    fun removeUsedValues(index: Int, board: Array<Array<Int>>): List<Int> {
        var rowIndex = index / rows
        var colIndex = index % columns
        // If square is prefilled, return only that value
        if (board[rowIndex][colIndex] != 0) {
            return listOf(board[rowIndex][colIndex])
        }

        var noRow = removeRow(index, board)
        var noCol = removeCol(noRow, index, board)
        var noSquare = removeSquare(noCol, index, board)

        return noSquare
    }

    fun rowI(index: Int): Int {
        return index / rows
    }
    fun colI(index: Int): Int {
        return index % columns
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

    fun removeCol(range: MutableList<Int>, index: Int, board: Array<Array<Int>>): MutableList<Int> {
        var colIndex = index % columns
        var newRange: MutableList<Int> = range

        board.forEach {
            if (it[colIndex] != 0) {
                newRange.remove(it[colIndex])
            }
        }

        return newRange
    }

    fun removeSquare(
        range: MutableList<Int>,
        index: Int,
        board: Array<Array<Int>>
    ): MutableList<Int> {
        val rowStart = findBoxStart(index / rows)
        val rowEnd = findBoxEnd(rowStart)
        val columnStart = findBoxStart(index % columns)
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

    fun parse1Dto2D(oneD: Array<Int>): Array<Array<Int>> {
        var newgrid = Array(9) {
            Array(9, { 0 })
        }
        for (i in oneD.indices) {
            var rowIndex = i / rows
            var colIndex = i % columns
            newgrid[rowIndex][colIndex] = oneD[i]
        }
        return newgrid
    }

    fun parse2Dto1D(twoD: Array<Array<Int>>): Array<Int> {
        var newgrid = Array(81) { 0 }
        for (i in (0 until rows)) {
            for (j in (0 until columns)) {
                newgrid[i * (rows) + j] = twoD[i][j]
            }
        }
        return newgrid
    }
}
