package com.example.sudokusolver

import android.util.Log
import java.lang.Math.sqrt

object GridModel {

    var status = false

    val solver = MySolver()
    // generates the entire Sudoku grid of GridCells with value 0
    val grid =  (0..2).asSequence().flatMap { parentX -> (0..2).asSequence().map { parentY -> parentX to parentY } }
        .flatMap { (parentX,parentY) ->
            (0..2).asSequence().flatMap { x -> (0..2).asSequence().map { y -> x to y } }
                .map { (x,y) -> GridCell(parentX,parentY,x,y,0) }
        }.toList()

    // retrieves a GridCell
    fun cellFor(parentX: Int, parentY: Int, x: Int, y: Int) = grid.first {
        it.squareX == parentX &&
                it.squareY == parentY  &&
                it.x == x &&
                it.y == y
    }

    fun Solve() {
        status = false
        solver.solve()
    }

    fun PrintBoard() {
        grid.forEach() {
            Log.i("Board: ", it.value.toString())
        }
    }

    // Own function - fills grid from list we pass in
    fun fill(list: List<Int>) {
        grid.zip(list).forEach  {
            it.first.value = it.second
        }
    }
}


class MySolver   {
    inner class GridBranch(
        val selectedValue: Int,     // new value we are testing on during traversal
        val gridCell: GridCell,
        val previous: GridBranch? = null
    ) {
        val x = gridCell.x
        val y = gridCell.y
        val squareX = gridCell.squareX
        val squareY = gridCell.squareY

        val traverseBackwards = generateSequence(this) {it.previous}.toList()

        // Be able to retrieve a given row, column, or square of assigned values from this branch
        val allRow  = traverseBackwards.filter { it.y == y && it.squareY == squareY }
        val allColumn = traverseBackwards.filter { it.x == x && it.squareX == squareX }
        val allSquare = traverseBackwards.filter { it.squareY == squareY && it.squareX== squareX }

        // Determines whether our current branch does not break any Sudoku rules
        val constraintsMet = allRow.filter { it.selectedValue == selectedValue }.count() <= 1
                && allColumn.filter { it.selectedValue == selectedValue }.count() <= 1
                && allSquare.filter { it.selectedValue == selectedValue }.count() <= 1

        // Determines whether this branch should continue to be traversed
        val isContinuable =  constraintsMet && traverseBackwards.count() < 81

        // Determines if this branch is a full solution
        val isSolution = traverseBackwards.count() == 81 && constraintsMet

        fun applyToCell() {
            gridCell.value = selectedValue      // puts in value passed to it
        }

        init {
            if (isContinuable) applyToCell()    // always called
        }
    }
    fun solve() {

        Log.i("Got here: ", "solve()")

        // Order Sudoku cells by count of how many candidate values they have left
        // Starting with the most constrained cells (with fewest possible values left) will greatly reduce the search space
        // Fixed cells will have only 1 candidate and will be processed first
        val sortedByCandidateCount = GridModel.grid.asSequence()
            .sortedBy { it.candidatesLeft.count() }
            .toList()

        // hold onto fixed values snapshot as they are going to mutate during animation
        val fixedCellValues =  GridModel.grid.asSequence().map { it to it.value }
            .filter { it.second != null }
            .toMap()

        // this is a recursive function for exploring nodes in a branch-and-bound tree
        // for loop goes through one number and if a workable number is found, it called traverse on
        // the next index
        fun traverse(index: Int, currentBranch: GridBranch): GridBranch? {

            val nextCell = sortedByCandidateCount[index+1]

            val fixedValue = fixedCellValues[nextCell]

            // we want to explore possible values 1..9 unless this cell is fixed already
            // infeasible values should terminate the branch
            val range = if (fixedValue == null) (1..9) else (fixedValue..fixedValue)

            for (candidateValue in range) {
                Log.i("Got into range loop: ", "solve() -> traverse()")

                val nextBranch = GridBranch(candidateValue, nextCell, currentBranch)

                if (nextBranch.isSolution)
                    return nextBranch

                if (nextBranch.isContinuable) {
                    val terminalBranch = traverse(index + 1, nextBranch)
                    if (terminalBranch?.isSolution == true) {
                        return terminalBranch
                    }
                }
            }
            return null
        }

        // start with the first sorted Sudoku cell and set it as the seed
        val seed = sortedByCandidateCount.first()
            .let { GridBranch(it.value?:1, it) }

        // recursively traverse from the seed and get a solution
        val solution = traverse(0, seed)

        // apply solution back to game board
        // solution is the same as original board here...not working
        if(solution != null) {
            solution?.traverseBackwards?.forEach { it.applyToCell(); Log.i("Board: ", it.gridCell.value.toString()) }

        }

        GridModel.status = solution != null
    }
}

data class GridCell(val squareX: Int, val squareY: Int, val x: Int, val y: Int, var value: Int) {
    //fun valueProperty() = getProperty(GridCell::value)

    val allRow by lazy { GridModel.grid.filter { it.y == y && it.squareY == squareY }.toSet() }
    val allColumn by lazy { GridModel.grid.filter { it.x == x && it.squareX== squareX }.toSet() }
    val allSquare by lazy { GridModel.grid.filter { it.squareY == squareY && it.squareX== squareX }.toSet() }

    val nextValidValue get() = ((value)..8).asSequence().map { it + 1 }.firstOrNull { candidate ->
        allRow.all { it.value != candidate }
                && allColumn.all { it.value != candidate }
                && allSquare.all { it.value != candidate }
    }

    val candidatesLeft get() = if (value != 0)
        setOf()
    else
        allRow.asSequence()
            .plus(allColumn.asSequence())
            .plus(allSquare.asSequence())
            .map { it.value }
            .filterNotNull()
            .distinct()
            .toSet().let { taken -> (1..9).asSequence().filter { it !in taken } }.toSet()

    fun increment() {
        nextValidValue?.let {
            value = nextValidValue as Int
        }
    }
}

object dumbVer {
    // solved?
    var status = false
    var cells = 81
    var rows = 9
    var columns = rows
    var squareSides = sqrt(rows.toDouble()).toInt()

    var grid = mutableListOf<Int>()
    // Own function - fills grid from list we pass in
    fun fill(list: List<Int>) {
        if (list.count() == 81) {
            grid = list.toMutableList()
        }
    }
    fun PrintBoard() {
        grid.forEach() {
            Log.i("Board: ", it.toString())
        }
    }

    fun solve(index: Int, board: MutableList<Int>): Pair<MutableList<Int>, Boolean> {
        // Look at one cell
        // exclude all numbers that are on the row/column/square
        // create a new solve() for the next cell for every remaining viable number
        // therefore we need cellindex and board
                //remove unusable values
        val range = removeUsedValues(index, board)
        // check if we got a good board
        var result = Pair<MutableList<Int>, Boolean>(board, false)
        var newBoard = board
        for (candidateValue in range) {
            newBoard.set(index, candidateValue)
            var temp = solve(index+1, newBoard)
            if(temp.second == true) {
                result = temp
            }
        }

        // does not update if board is unsolvable!!
        return result
    }
    fun removeUsedValues(index: Int, board: List<Int>): List<Int> {
        var noRow = removeRow(index, board)
        var noCol = removeCol(noRow, index, board)
        var noSquare = removeSquare(noCol, index, board)

        return noSquare
    }

    // should work
    fun removeRow(index: Int, board: List<Int>): MutableList<Int> {
        var indexes: MutableList<Int>
        indexes = getRowIndex(index)

        var newRange: MutableList<Int> = (1..9).toMutableList()

        indexes.forEach {
            if (board.indexOf(it) != 0) {
                newRange.remove(board.indexOf(it))
            }
        }
        return newRange
    }

    fun removeCol (range: MutableList<Int>, index: Int, board: List<Int>): MutableList<Int> {
        var indexes: MutableList<Int>
        indexes = getColIndex(index)

        var newRange: MutableList<Int> = range

        indexes.forEach {
            if (board.indexOf(it) != 0) {
                newRange.remove(board.indexOf(it))
            }
        }
        return newRange
    }

    fun removeSquare (range: MutableList<Int>, index: Int, board: List<Int>): MutableList<Int> {
        var indexes: MutableList<Int>
        indexes = getSquareIndex(index)

        var newRange: MutableList<Int> = range

        indexes.forEach {
            if (board.indexOf(it) != 0) {
                newRange.remove(board.indexOf(it))
            }
        }
        return newRange
    }


    // works!
    fun getRowIndex(index: Int): MutableList<Int> {
        var row = mutableListOf<Int>()
        var start = index / rows

        for (i in 0..(rows-1)) {
            row.add(i + 1 + (rows * start))
        }
        return row
    }
    // works!
    fun getColIndex(index: Int): MutableList<Int> {
        var col = mutableListOf<Int>()
        var remainder = index%rows
        for (i in 0..(columns-1)) {
            col.add(remainder + columns*i)
        }
        return col
    }
    fun getSquareIndex(index: Int): MutableList<Int> {
        var square = mutableListOf<Int>()
        var squareCol = 1 + (index % columns) / squareSides // 1 - 3
        var squareRow = 1 + (index / rows) / squareSides // 1 - 3
        for(i in 1..(squareSides)) {
            for (j in 1..(squareSides)) {
                // it's breakin mah brain!
            }
        }
        return square
    }
}