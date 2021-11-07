package com.example.sudokusolver

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc

class SudokuBoardRecognizer constructor(private val context: Context) {
    private var dependenciesLoaded = loadOpenCV()
    private var originalImage = Mat()

    fun execute() {
        //check if dependencies has been loaded
        if (!dependenciesLoaded) {
            Log.e("OpenCV", "OpenCV failed to load and is preventing image recognition")
            return
        }
        //perform preprocessing
        extractBoard()
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
    }

    private fun getOriginalImage(): Mat {
        return this.originalImage.clone()
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

    private fun getContours(matrix: Mat, mode: Int): List<MatOfPoint> {
        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(matrix, contours, hierarchy, mode, Imgproc.CHAIN_APPROX_SIMPLE)
        hierarchy.release()
        return contours
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
