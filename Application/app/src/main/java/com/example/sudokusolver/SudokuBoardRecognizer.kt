package com.example.sudokusolver

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.sudokusolver.ml.Model
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import kotlin.math.pow
import kotlin.math.sqrt

class SudokuBoardRecognizer constructor(private val context: Context) {
    private var dependenciesLoaded = loadOpenCV()
    private lateinit var model: Model
    private var originalImage = Mat()
    private var boardMatrix = Mat()

    fun execute() {
        //check if dependencies has been loaded
        if (!dependenciesLoaded) {
            Log.e("OpenCV", "OpenCV failed to load and is preventing image recognition")
            return
        }
        //initialize Tensorflow Lite model
        model = Model.newInstance(context)
        //perform preprocessing
        extractBoard()
        val cellPositions = getCellPositionsByContours()
        computePredictionsOnCells(cellPositions)
        //clean up
        model.close()
    }

    private fun loadOpenCV(): Boolean {
        return OpenCVLoader.initDebug()
    }

    private fun extractBoard() {
        val fullImage = getOriginalImage()
        //perform preprocessing of image
        convertToGrayscale(fullImage)
        performGaussianBlur(fullImage)
        performAdaptiveThresholding(fullImage, 11, 2.0)
        performBitwiseNot(fullImage)
        performDilation(fullImage)
        //get board area in the image (finds largest rectangle in the image)
        val boardCoordinates = findBoardCoordinates(fullImage)
        //crop and warp the sudoku board
        val boardImage = getOriginalImage()
        focusOnBoard(boardImage, boardCoordinates)
        setBoardMatrix(boardImage)
        fullImage.release()
        boardImage.release()
    }

    private fun getOriginalImage(): Mat {
        return this.originalImage.clone()
    }

    private fun getBoardMatrix(): Mat {
        return this.boardMatrix.clone()
    }

    private fun convertToGrayscale(matrix: Mat) {
        val bufferMatrix = generateBuffer(matrix)
        Imgproc.cvtColor(bufferMatrix, matrix, Imgproc.COLOR_BGR2GRAY)
        bufferMatrix.release()
    }

    private fun performGaussianBlur(matrix: Mat) {
        val bufferMatrix = generateBuffer(matrix)
        Imgproc.GaussianBlur(bufferMatrix, matrix, Size(9.0, 9.0), 0.0)
        bufferMatrix.release()
    }

    private fun performAdaptiveThresholding(matrix: Mat, blockSize: Int, constSubtraction: Double) {
        val bufferMatrix = generateBuffer(matrix)
        Imgproc.adaptiveThreshold(
            bufferMatrix,
            matrix,
            255.0,
            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
            Imgproc.THRESH_BINARY,
            blockSize,
            constSubtraction
        )
        bufferMatrix.release()
    }

    private fun performBitwiseNot(matrix: Mat) {
        val bufferMatrix = generateBuffer(matrix)
        Core.bitwise_not(bufferMatrix, matrix)
        bufferMatrix.release()
    }

    private fun performDilation(matrix: Mat) {
        val kernel = Mat.zeros(3, 3, CvType.CV_8U)
        kernel.put(0, 0, byteArrayOf(0.toByte(), 1.toByte(), 0.toByte()))
        kernel.put(1, 0, byteArrayOf(1.toByte(), 1.toByte(), 1.toByte()))
        kernel.put(2, 0, byteArrayOf(0.toByte(), 1.toByte(), 0.toByte()))
        val bufferMatrix = generateBuffer(matrix)
        Imgproc.dilate(bufferMatrix, matrix, kernel)
        bufferMatrix.release()
    }

    private fun findBoardCoordinates(matrix: Mat): BoardCoordinates {
        //find contours
        val contours = getContours(matrix, Imgproc.RETR_EXTERNAL)
        //get largest contour area (presumably the sudoku board)
        var largestContourArea = 0.0
        var largestContourIndex = 0
        for (i in contours.indices) {
            val contourArea = Imgproc.contourArea(contours[i])
            if (largestContourArea < contourArea) {
                largestContourArea = contourArea
                largestContourIndex = i
            }
        }
        //calculate contour perimeter and approximate a polygon for the area
        val bufferCurve = MatOfPoint2f()
        contours[largestContourIndex].convertTo(bufferCurve, CvType.CV_32FC2)
        val perimeter = Imgproc.arcLength(bufferCurve, true)
        val approx = MatOfPoint2f()
        Imgproc.approxPolyDP(bufferCurve, approx, 0.015 * perimeter, true)
        //get each corners
        val corners = approx.toList()
        val boardCoordinates = BoardCoordinates()
        boardCoordinates.topLeft = corners[2]
        boardCoordinates.bottomLeft = corners[1]
        boardCoordinates.bottomRight = corners[0]
        boardCoordinates.topRight = corners[3]
        return boardCoordinates
    }

    private fun getContours(matrix: Mat, mode: Int): List<MatOfPoint> {
        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(matrix, contours, hierarchy, mode, Imgproc.CHAIN_APPROX_SIMPLE)
        hierarchy.release()
        return contours
    }

    private fun focusOnBoard(matrix: Mat, boardCoordinates: BoardCoordinates) {
        //get width of board
        val widthBottom = sqrt((boardCoordinates.bottomRight.x - boardCoordinates.bottomLeft.x).pow(2) + (boardCoordinates.bottomRight.y - boardCoordinates.bottomLeft.y).pow(2))
        val widthTop = sqrt((boardCoordinates.topRight.x - boardCoordinates.topLeft.x).pow(2) + (boardCoordinates.topRight.y - boardCoordinates.topLeft.y).pow(2))
        val width = kotlin.math.max(widthBottom.toInt(), widthTop.toInt())
        //get height of board
        val heightRight = sqrt((boardCoordinates.topRight.x - boardCoordinates.bottomRight.x).pow(2) + (boardCoordinates.topRight.y - boardCoordinates.bottomRight.y).pow(2))
        val heightLeft = sqrt((boardCoordinates.topLeft.x - boardCoordinates.bottomLeft.x).pow(2) + (boardCoordinates.topLeft.y - boardCoordinates.bottomLeft.y).pow(2))
        val height = kotlin.math.max(heightRight.toInt(), heightLeft.toInt())
        //get dimensions of board
        val dimensions = Mat.zeros(4, 2, CvType.CV_32F)
        dimensions.put(0, 0, floatArrayOf(0.0F, 0.0F))
        dimensions.put(1, 0, floatArrayOf(width - 1.0F, 0.0F))
        dimensions.put(2, 0, floatArrayOf(width - 1.0F, height - 1.0F))
        dimensions.put(3, 0, floatArrayOf(0.0F, height - 1.0F))
        //get coordinates of board
        val orderedCorners = Mat.zeros(4, 2, CvType.CV_32F)
        orderedCorners.put(0, 0, floatArrayOf(boardCoordinates.bottomLeft.x.toFloat(), boardCoordinates.bottomLeft.y.toFloat()))
        orderedCorners.put(1, 0, floatArrayOf(boardCoordinates.bottomRight.x.toFloat(), boardCoordinates.bottomRight.y.toFloat()))
        orderedCorners.put(2, 0, floatArrayOf(boardCoordinates.topRight.x.toFloat(), boardCoordinates.topRight.y.toFloat()))
        orderedCorners.put(3, 0, floatArrayOf(boardCoordinates.topLeft.x.toFloat(), boardCoordinates.topLeft.y.toFloat()))
        //crop and warp to board
        val grid = Imgproc.getPerspectiveTransform(orderedCorners, dimensions)
        val bufferMatrix = generateBuffer(matrix)
        Imgproc.warpPerspective(
            bufferMatrix,
            matrix,
            grid,
            Size(width.toDouble(), height.toDouble())
        )
        bufferMatrix.release()
    }

    private fun getCellPositionsByContours(): List<Rect> {
        //perform preprocessing of image
        val boardImage = getBoardMatrix()
        convertToGrayscale(boardImage)
        performAdaptiveThresholding(boardImage, 11, 2.0)
        performBitwiseNot(boardImage)
        performDilation(boardImage)
        //iterate through contours and store their positions if they are within acceptable cell area
        val cellWidth = boardImage.width() / 9
        val cellHeight = boardImage.height() / 9
        val threshold = 1.10
        val contours = getContours(boardImage, Imgproc.RETR_LIST)
        val cellAreas = mutableListOf<Rect>()
        for (i in contours.indices) {
            val cellArea = Imgproc.boundingRect(contours[i])
            if (cellArea.width <= cellWidth * threshold && cellArea.height <= cellHeight * threshold) {
                cellAreas.add(cellArea)
            }
        }
        //only keep the contours with the largest areas (presumably the cells)
        cellAreas.sortByDescending { it.area() }
        cellAreas.subList(81, cellAreas.size).clear()
        //sort cell areas by y position and iterate through list to sort them by their respected position on the board
        cellAreas.sortBy { it.y }
        for (row in 0..8) {
            val cellAreasBuffer = mutableListOf<Rect>()
            for (col in 0..8) {
                cellAreasBuffer.add(cellAreas[row * 9 + col])
            }
            cellAreasBuffer.sortBy { it.x }
            for (col in 0..8) {
                cellAreas[row * 9 + col] = cellAreasBuffer[col]
            }
        }
        boardImage.release()
        return cellAreas
    }

    private fun getCellPositionsByGrid(): List<Rect> {
        //iterate through each cell in the board and crop the matrix
        val boardHeight = this.boardMatrix.height()
        val boardWidth = this.boardMatrix.width()
        val cellHeight = boardHeight / 9
        val cellWidth = boardWidth / 9
        val cells = mutableListOf<Rect>()
        for (y in 0 until (boardHeight - cellHeight) step cellHeight) {
            for (x in 0 until (boardWidth - cellWidth) step cellWidth) {
                cells.add(Rect(Point(x.toDouble(), y.toDouble()), Point((x + cellWidth).toDouble(),(y + cellHeight).toDouble())))
            }
        }
        return cells
    }

    private fun computePredictionsOnCells(cellPositions: List<Rect>) {
        //perform preprocessing of image
        val matrix = getBoardMatrix()
        convertToGrayscale(matrix)
        performBitwiseNot(matrix)
        performThresholding(matrix, 130.0)
        //iterate through each cell and predict if there is a digit inside
        for (cellIndex in cellPositions.indices) {
            val cell = extractAreaFromMatrix(matrix, cellPositions[cellIndex])
        }
    }

    private fun performThresholding(matrix: Mat, thresh: Double) {
        val bufferMatrix = generateBuffer(matrix)
        Imgproc.threshold(bufferMatrix, matrix, thresh, 255.0, Imgproc.THRESH_BINARY)
        bufferMatrix.release()
    }

    private fun extractAreaFromMatrix(matrix: Mat, cellCoordinates: Rect): Mat {
        //create a sub matrix by area
        val cellX = cellCoordinates.x.toDouble()
        val cellY = cellCoordinates.y.toDouble()
        val cellWidth = cellCoordinates.width.toDouble()
        val cellHeight = cellCoordinates.height.toDouble()
        return matrix.submat(Rect(Point(cellX, cellY), Point(cellX + cellWidth, cellY + cellHeight)))
    }

    private fun setBoardMatrix(matrix: Mat) {
        this.boardMatrix.release()
        matrix.copyTo(this.boardMatrix)
    }

    private fun generateBuffer(matrix: Mat): Mat {
        //because of objects being references in Kotlin and OpenCV semantics, I have
        //added a quick switch and release function this allows me to keep the original
        //matrix instead of creating a lot of new matrices after each Opencv function
        val bufferMatrix = Mat()
        matrix.copyTo(bufferMatrix)
        matrix.release()
        return bufferMatrix
    }

    fun setImageFromResource(resource: Int) {
        this.originalImage.release()
        this.originalImage = Utils.loadResource(context, resource, Imgcodecs.IMREAD_COLOR + Imgcodecs.IMREAD_IGNORE_ORIENTATION)
    }

    fun setImageFromBitmap(bitmap: Bitmap) {
        this.originalImage.release()
        Utils.bitmapToMat(bitmap, originalImage)
    }
}
