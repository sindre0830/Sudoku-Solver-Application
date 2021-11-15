package com.example.sudokusolver

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.Float
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt
import com.example.sudokusolver.ml.ModelMnist as Model

/**
 * Handler for converting image of sudoku board to parsable list of sudoku board.
 */
class SudokuBoardRecognizer constructor(private val context: Context) {
    private var dependenciesLoaded = loadOpenCV()
    private lateinit var model: Model
    private var originalImage = Mat()
    private var boardMatrix = Mat()
    private val imageSize = Size(32.0, 32.0)
    private val inputBuffer = ByteBuffer.allocateDirect(
        Float.Companion.SIZE_BYTES * imageSize.width.toInt() * imageSize.height.toInt()
    ).apply { order(ByteOrder.nativeOrder()) }
    // generate list of 81 zeros representing an empty board
    var predictionOutput = emptyBoard()
    lateinit var debugImage: Bitmap
    var flagDebugActivity = false
    private val margin = 2
    var error: String? = null

    /**
     * Performs the pipeline required to get predictions.
     */
    fun execute() {
        // check if dependencies has been loaded
        if (!dependenciesLoaded) {
            Log.e("OpenCV", "OpenCV failed to load and is preventing image recognition")
            return
        }
        // set list to be empty
        predictionOutput = emptyBoard()
        // extract board and branch if an error occurred
        extractBoard()
        if (error != null) {
            Log.e("SudokuBoardRecognizer", error!!)
            originalImage.release()
            return
        }
        // get cell positions and branch if an error occurred
        val cellPositions = getCellPositionsByContours()
        if (error != null) {
            Log.e("SudokuBoardRecognizer", error!!)
            originalImage.release()
            boardMatrix.release()
            return
        }
        // initialize Tensorflow Lite model
        model = Model.newInstance(context)
        computePredictionsOnCells(cellPositions)
        // clean up
        model.close()
        originalImage.release()
        boardMatrix.release()
    }

    /**
     * Returns an array filled with 0 imitating an empty board.
     */
    private fun emptyBoard(): MutableList<Int> {
        return MutableList(81) { 0 }
    }

    /**
     * Initialize OpenCV library.
     */
    private fun loadOpenCV(): Boolean {
        return OpenCVLoader.initDebug()
    }

    /**
     * Warps and crops to sudoku board and saves it to the global variable, boardMatrix.
     */
    private fun extractBoard() {
        var image = getOriginalImage()
        /* perform preprocessing of image */
        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY)
        Imgproc.GaussianBlur(image, image, Size(11.0, 11.0), 0.0)
        Imgproc.adaptiveThreshold(
            image,
            image,
            255.0,
            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
            Imgproc.THRESH_BINARY,
            5,
            2.0
        )
        Core.bitwise_not(image, image)
        // perform dilation with a plus shaped kernel
        val kernel = Mat.zeros(3, 3, CvType.CV_8U)
        kernel.put(0, 0, byteArrayOf(0.toByte(), 1.toByte(), 0.toByte()))
        kernel.put(1, 0, byteArrayOf(1.toByte(), 1.toByte(), 1.toByte()))
        kernel.put(2, 0, byteArrayOf(0.toByte(), 1.toByte(), 0.toByte()))
        Imgproc.dilate(image, image, kernel)
        kernel.release()
        // get board position in the image (finds largest rectangle in the image)
        // and branch if an error occurred
        val boardCoordinates = findBoardCoordinates(image)
        image.release()
        if (error != null) return
        // crop and warp the sudoku board
        image = getOriginalImage()
        focusOnBoard(image, boardCoordinates)
        // update global variable
        setBoardMatrix(image)
        image.release()
    }

    /**
     * Updates global variable error.
     */
    private fun setError(key: Int) {
        error = context.getString(key)
    }

    /**
     * Returns copy of global variable originalImage.
     */
    private fun getOriginalImage(): Mat {
        return this.originalImage.clone()
    }

    /**
     * Returns copy of global variable boardMatrix.
     */
    private fun getBoardMatrix(): Mat {
        return this.boardMatrix.clone()
    }

    /**
     * Find coordinates of board corners using contours.
     */
    private fun findBoardCoordinates(image: Mat): BoardCoordinates {
        val boardCoordinates = BoardCoordinates()
        // find contours and branch if an error occurred
        val contours = getContours(image, Imgproc.RETR_EXTERNAL)
        if (contours.isEmpty()) {
            setError(R.string.recognizer_error_extract_board)
            return boardCoordinates
        }
        // get largest contour area (presumably the sudoku board)
        var largestContourArea = 0.0
        var largestContourIndex = 0
        for (i in contours.indices) {
            val contourArea = Imgproc.contourArea(contours[i])
            if (largestContourArea < contourArea) {
                largestContourArea = contourArea
                largestContourIndex = i
            }
        }
        // calculate contour perimeter and approximate a polygon for the area
        val bufferCurve = MatOfPoint2f()
        contours[largestContourIndex].convertTo(bufferCurve, CvType.CV_32FC2)
        val perimeter = Imgproc.arcLength(bufferCurve, true)
        val approx = MatOfPoint2f()
        Imgproc.approxPolyDP(bufferCurve, approx, 0.015 * perimeter, true)
        // get each corners
        val corners = approx.toList()
        // find top left, bottom left, bottom right, top right
        corners.sortBy { it.x + it.y }
        boardCoordinates.bottomLeft = corners[0]
        boardCoordinates.topRight = corners[3]
        if (corners[1].x > corners[2].x) {
            boardCoordinates.topLeft = corners[2]
            boardCoordinates.bottomRight = corners[1]
        } else {
            boardCoordinates.topLeft = corners[1]
            boardCoordinates.bottomRight = corners[2]
        }
        contours.forEach { it.release() }
        return boardCoordinates
    }

    /**
     * Finds contours in matrix based on mode.
     */
    private fun getContours(image: Mat, mode: Int): List<MatOfPoint> {
        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(image, contours, hierarchy, mode, Imgproc.CHAIN_APPROX_SIMPLE)
        hierarchy.release()
        return contours
    }

    /**
     * Warp and crop matrix based on coordinates.
     */
    private fun focusOnBoard(image: Mat, boardCoordinates: BoardCoordinates) {
        // get width of board
        var widthX = (boardCoordinates.bottomRight.x - boardCoordinates.bottomLeft.x).pow(2)
        var widthY = (boardCoordinates.bottomRight.y - boardCoordinates.bottomLeft.y).pow(2)
        val widthBottom = sqrt(widthX + widthY)
        widthX = (boardCoordinates.topRight.x - boardCoordinates.topLeft.x).pow(2)
        widthY = (boardCoordinates.topRight.y - boardCoordinates.topLeft.y).pow(2)
        val widthTop = sqrt(widthX + widthY)
        val width = max(widthBottom.toInt(), widthTop.toInt())
        // get height of board
        var heightX = (boardCoordinates.topRight.x - boardCoordinates.bottomRight.x).pow(2)
        var heightY = (boardCoordinates.topRight.y - boardCoordinates.bottomRight.y).pow(2)
        val heightRight = sqrt(heightX + heightY)
        heightX = (boardCoordinates.topLeft.x - boardCoordinates.bottomLeft.x).pow(2)
        heightY = (boardCoordinates.topLeft.y - boardCoordinates.bottomLeft.y).pow(2)
        val heightLeft = sqrt(heightX + heightY)
        val height = max(heightRight.toInt(), heightLeft.toInt())
        // get dimensions of board
        val dimensions = Mat.zeros(4, 2, CvType.CV_32F)
        dimensions.put(0, 0, floatArrayOf(0.0F, 0.0F))
        dimensions.put(1, 0, floatArrayOf(width - 1.0F, 0.0F))
        dimensions.put(2, 0, floatArrayOf(width - 1.0F, height - 1.0F))
        dimensions.put(3, 0, floatArrayOf(0.0F, height - 1.0F))
        // get coordinates of board
        val orderedCorners = Mat.zeros(4, 2, CvType.CV_32F)
        orderedCorners.put(
            0,
            0,
            floatArrayOf(
                boardCoordinates.bottomLeft.x.toFloat(),
                boardCoordinates.bottomLeft.y.toFloat()
            )
        )
        orderedCorners.put(
            1,
            0,
            floatArrayOf(
                boardCoordinates.bottomRight.x.toFloat(),
                boardCoordinates.bottomRight.y.toFloat()
            )
        )
        orderedCorners.put(
            2,
            0,
            floatArrayOf(
                boardCoordinates.topRight.x.toFloat(),
                boardCoordinates.topRight.y.toFloat()
            )
        )
        orderedCorners.put(
            3,
            0,
            floatArrayOf(
                boardCoordinates.topLeft.x.toFloat(),
                boardCoordinates.topLeft.y.toFloat()
            )
        )
        // crop and warp to board
        val grid = Imgproc.getPerspectiveTransform(orderedCorners, dimensions)
        Imgproc.warpPerspective(image, image, grid, Size(width.toDouble(), height.toDouble()))
    }

    /**
     * Get every cell position on the board through the use of contours.
     */
    private fun getCellPositionsByContours(): List<Rect> {
        /* perform preprocessing of image */
        var image = getBoardMatrix()
        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY)
        Imgproc.Canny(image, image, 30.0, 90.0, 3, true)
        // perform dilation with a plus shaped kernel
        val kernel = Mat.zeros(3, 3, CvType.CV_8U)
        kernel.put(0, 0, byteArrayOf(0.toByte(), 1.toByte(), 0.toByte()))
        kernel.put(1, 0, byteArrayOf(1.toByte(), 1.toByte(), 1.toByte()))
        kernel.put(2, 0, byteArrayOf(0.toByte(), 1.toByte(), 0.toByte()))
        Imgproc.dilate(image, image, kernel)
        kernel.release()
        /* set matrix data */
        val cellWidth = image.width() / 9
        val cellHeight = image.height() / 9
        val thresholdUp = 1.20
        val thresholdDown = abs(thresholdUp - 2)
        val contours = getContours(image, Imgproc.RETR_TREE)
        val cellPositions = mutableListOf<Rect>()
        image.release()
        // iterate through contours and store their positions if they are within acceptable area
        for (i in contours.indices) {
            val cellArea = Imgproc.boundingRect(contours[i])
            // branch if area is within cell limits
            if (cellArea.width <= cellWidth * thresholdUp && cellArea.height <= cellHeight * thresholdUp && cellArea.width >= cellWidth * thresholdDown && cellArea.height >= cellHeight * thresholdDown) {
                // branch if first cell isn't set yet
                if (cellPositions.isEmpty()) {
                    cellPositions.add(cellArea)
                } else {
                    // branch if area is not too close to another cell
                    if (!cellPositions.any { cellArea.x <= it.x + (cellWidth / 4) && cellArea.x >= it.x - (cellWidth / 4) && cellArea.y <= it.y + (cellHeight / 4) && cellArea.y >= it.y - (cellHeight / 4) }) {
                        cellPositions.add(cellArea)
                    }
                }
            }
        }
        contours.forEach { it.release() }
        // branch if cells found isn't 81
        if (cellPositions.size != 81) {
            setError(R.string.recognizer_error_extract_cells)
            return cellPositions
        }
        // sort cell areas by y position and iterate through list to sort them by their respected
        // position on the board
        cellPositions.sortBy { it.y }
        for (row in 0..8) {
            val cellAreasBuffer = mutableListOf<Rect>()
            for (col in 0..8) {
                cellAreasBuffer.add(cellPositions[row * 9 + col])
            }
            cellAreasBuffer.sortBy { it.x }
            for (col in 0..8) {
                cellPositions[row * 9 + col] = cellAreasBuffer[col]
            }
        }
        return cellPositions
    }

    /**
     * Get cell positions by bruteforce. This will simply divide the image width and height by 9
     * and save positions by each step. This can give bad results if the cells aren't the same
     * size or aligned.
     */
    private fun getCellPositionsByGrid(): List<Rect> {
        val boardHeight = this.boardMatrix.height()
        val boardWidth = this.boardMatrix.width()
        val cellHeight = boardHeight / 9
        val cellWidth = boardWidth / 9
        val cells = mutableListOf<Rect>()
        // iterate through each cell in the board and crop the matrix
        for (y in 0 until (boardHeight - cellHeight) step cellHeight) {
            for (x in 0 until (boardWidth - cellWidth) step cellWidth) {
                cells.add(
                    Rect(
                        Point(x.toDouble(), y.toDouble()),
                        Point((x + cellWidth).toDouble(), (y + cellHeight).toDouble())
                    )
                )
            }
        }
        return cells
    }

    /**
     * Extract each cell with a digit and perform prediction.
     */
    private fun computePredictionsOnCells(cellPositions: List<Rect>) {
        /* perform preprocessing of image */
        var image = getBoardMatrix()
        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY)
        Imgproc.adaptiveThreshold(
            image,
            image,
            255.0,
            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
            Imgproc.THRESH_BINARY,
            9,
            7.0
        )
        Core.bitwise_not(image, image)
        // iterate through each cell and predict if there is a digit inside
        for (cellIndex in cellPositions.indices) {
            val cell = extractAreaFromMatrix(image, cellPositions[cellIndex])
            val contours = getContours(cell, Imgproc.RETR_EXTERNAL)
            // get largest contour area that isn't the grid
            var largestContourArea = 0.0
            var index = -1
            for (i in contours.indices) {
                val rect = Imgproc.boundingRect(contours[i])
                val contourArea = Imgproc.contourArea(contours[i])
                if (!(rect.x < 4 || rect.y < 4 || rect.height < 4 || rect.width < 4) && largestContourArea < contourArea) {
                    index = i
                    largestContourArea = contourArea
                }
            }
            // branch if digit was found in cell and predict
            if (index >= 0) {
                // get digit and adjust ROI
                var digit = extractAreaFromMatrix(cell, Imgproc.boundingRect(contours[index]))
                performAdjustROI(digit)
                /* perform preprocessing of image */
                var method = Imgproc.INTER_CUBIC.toDouble()
                // branch if image is larger than output size and set method for downscaling
                if (digit.width() > imageSize.width || digit.height() > imageSize.height) {
                    method = Imgproc.INTER_AREA.toDouble()
                }
                Imgproc.resize(digit, digit, Size(imageSize.width, imageSize.height), method)
                Imgproc.threshold(digit, digit, 160.0, 255.0, Imgproc.THRESH_BINARY)
                Core.normalize(digit, digit, 1.0, 0.0, Core.NORM_MINMAX)
                // predict on digit
                predictionOutput[cellIndex] = predictCell(digit, model)
                digit.release()
            }
            cell.release()
            contours.forEach { it.release() }
        }
        image.release()
    }

    /**
     * Extract sub-matrix from matrix based on coordinates.
     */
    private fun extractAreaFromMatrix(matrix: Mat, cellCoordinates: Rect): Mat {
        // create a sub matrix by area
        val x = cellCoordinates.x.toDouble()
        val y = cellCoordinates.y.toDouble()
        val width = cellCoordinates.width.toDouble()
        val height = cellCoordinates.height.toDouble()
        return matrix.submat(Rect(Point(x, y), Point(x + width, y + height)))
    }

    /**
     * Resize sub-matrix by adding a margin and columns and rows to convert the matrix from
     * rectangle to square. This gives better results when resizing later.
     */
    private fun performAdjustROI(matrix: Mat) {
        var top = margin
        var bottom = margin
        var left = margin
        var right = margin
        // branch if width needs to be adjusted and set difference
        if (matrix.width() < matrix.height()) {
            var diff = matrix.height() - matrix.width()
            // branch if difference isn't divisible by 2, add one, and apply difference
            if (diff.mod(2) != 0) {
                diff = (diff + 1) / 2
                left += diff - 1
                right += diff
            } else {
                diff /= 2
                left += diff
                right += diff
            }
        } else if (matrix.width() > matrix.height()) {
            var diff = matrix.width() - matrix.height()
            if (diff.mod(2) != 0) {
                diff = (diff + 1) / 2
                top += diff - 1
                bottom += diff
            } else {
                diff /= 2
                top += diff
                bottom += diff
            }
        }
        matrix.adjustROI(top, bottom, left, right)
    }

    /**
     * Perform prediction on an image and return the label with the highest accuracy.
     */
    private fun predictCell(cell: Mat, model: Model): Int {
        // convert matrix to bytebuffer
        inputBuffer.rewind()
        for (i in 0 until imageSize.height.toInt()) {
            for (j in 0 until imageSize.width.toInt()) {
                inputBuffer.putFloat(cell.get(i, j)[0].toFloat())
            }
        }
        // input buffer to model
        val input = TensorBuffer.createFixedSize(
            intArrayOf(1, imageSize.width.toInt(), imageSize.height.toInt(), 1),
            DataType.FLOAT32
        )
        input.loadBuffer(inputBuffer)
        // output new buffer from model
        val outputBuffer = model.process(input)
        val output = outputBuffer.outputFeature0AsTensorBuffer
        // get the index (label) of the highest accuracy
        val predictions = output.floatArray
        var highestAccuracy = 0.0F
        var prediction = 0
        for (i in predictions.indices) {
            if (i != 0 && predictions[i] > highestAccuracy) {
                highestAccuracy = predictions[i]
                prediction = i
            }
        }
        return prediction
    }

    /**
     * Updates global variable boardMatrix.
     */
    private fun setBoardMatrix(matrix: Mat) {
        this.boardMatrix.release()
        matrix.copyTo(this.boardMatrix)
    }

    /**
     * Convert matrix to bitmap and update global variable debugImage. Used for displaying images
     * after preprocessing to debug.
     */
    private fun setDebugImage(matrix: Mat) {
        this.debugImage = Bitmap.createBitmap(
            matrix.width(),
            matrix.height(),
            Bitmap.Config.ARGB_8888
        )
        Utils.matToBitmap(matrix, this.debugImage)
        flagDebugActivity = true
    }

    /**
     * Convert bitmap to matrix and update global variable originalImage.
     */
    fun setImageFromBitmap(bitmap: Bitmap) {
        this.originalImage.release()
        Utils.bitmapToMat(bitmap, originalImage)
    }
}
