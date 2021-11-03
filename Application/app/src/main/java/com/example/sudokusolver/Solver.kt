// Source: https://github.com/TypicalDevStuff/sudoku-generator

package com.example.sudokusolver

internal object Solver {

    val GRID_SIZE_SQUARE_ROOT = 3
    val MIN_DIGIT_VALUE = 1
    val MAX_DIGIT_VALUE = 9
    val MIN_DIGIT_INDEX = 0
    val MAX_DIGIT_INDEX = 8
    val BOX_SIZE = 3
    val GRID_SIZE = 9

    lateinit var grid: Array<IntArray>
    /*fun solvable(grid: Array<IntArray>) : Boolean {

        this.grid = grid.copy()
        // can return grid here instead
        val solved = solve()

        return solved

    }
     */

    fun solvable(oldgrid: Array<IntArray>) : Pair<Boolean, Array<IntArray>> {

        this.grid = oldgrid.copy()
        // can return grid here instead
        val solved = solve()

        if(solved) {
            return Pair(solved, this.grid)
        } else {
            return Pair(solved, oldgrid)
        }


    }

    private fun Array<IntArray>.copy() = Array(size) { get(it).clone() }

    private fun solve() : Boolean{
        for (i in 0 until GRID_SIZE) {
            for (j in 0 until GRID_SIZE) {
                if (grid[i][j] == 0) {
                    val availableDigits = getAvailableDigits(i, j)
                    for (k in availableDigits) {
                        grid[i][j] = k
                        val temp = solve()
                        if (solve()) {
                            return true
                        }
                        grid[i][j] = 0
                    }
                    return false
                }
            }
        }
        return true
    }

    private fun getAvailableDigits(row: Int, column: Int) : Iterable<Int> {
        val digitsRange = MIN_DIGIT_VALUE..MAX_DIGIT_VALUE
        var availableDigits = mutableSetOf<Int>()
        availableDigits.addAll(digitsRange)

        truncateByDigitsAlreadyUsedInRow(grid, availableDigits, row)
        if (availableDigits.size > 1) {
            truncateByDigitsAlreadyUsedInColumn(grid, availableDigits, column)
        }
        if (availableDigits.size > 1) {
            truncateByDigitsAlreadyUsedInBox(grid, availableDigits, row, column)
        }

        return availableDigits.asIterable()
    }

    private fun truncateByDigitsAlreadyUsedInRow(board: Array<IntArray>, availableDigits: MutableSet<Int>, row: Int) {
        for (i in MIN_DIGIT_INDEX..MAX_DIGIT_INDEX) {
            if (board[row][i] != 0) {
                availableDigits.remove(grid[row][i])
            }
        }
    }

    private fun truncateByDigitsAlreadyUsedInColumn(board: Array<IntArray>, availableDigits: MutableSet<Int>, column: Int) {
        for (i in MIN_DIGIT_INDEX..MAX_DIGIT_INDEX) {
            if (board[i][column] != 0) {
                availableDigits.remove(grid[i][column])
            }
        }
    }

    private fun truncateByDigitsAlreadyUsedInBox(board: Array<IntArray>, availableDigits: MutableSet<Int>, row: Int, column: Int) {
        val rowStart = findBoxStart(row)
        val rowEnd = findBoxEnd(rowStart)
        val columnStart = findBoxStart(column)
        val columnEnd = findBoxEnd(columnStart)

        for (i in rowStart until rowEnd) {
            for (j in columnStart until columnEnd) {
                if (grid[i][j] != 0) {
                    availableDigits.remove(grid[i][j])
                }
            }
        }
    }

    private fun findBoxStart(index: Int) = index - index % GRID_SIZE_SQUARE_ROOT

    private fun findBoxEnd(index: Int) = index + BOX_SIZE - 1

    fun printGrid(board: Array<IntArray>) {
        for (i in 0 until GRID_SIZE) {
            for (j in 0 until GRID_SIZE) {
                print(board[i][j].toString().plus(" "))
            }
            println()
        }
        println()
    }
}