package com.example.sudokusolver

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
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
    }

    private fun getOriginalImage(): Mat {
        return this.originalImage.clone()
    }

    private fun convertToGrayscale(matrix: Mat) {
        val bufferMatrix = generateBuffer(matrix)
        Imgproc.cvtColor(bufferMatrix, matrix, Imgproc.COLOR_BGR2GRAY)
        bufferMatrix.release()
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
