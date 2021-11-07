package com.example.sudokusolver

import android.util.Log
import org.opencv.android.OpenCVLoader

class SudokuBoardRecognizer {
    private var dependenciesLoaded = loadOpenCV()

    fun execute() {
        //check if dependencies has been loaded
        if (!dependenciesLoaded) {
            Log.e("OpenCV", "OpenCV failed to load and is preventing image recognition")
            return
        }
    }

    private fun loadOpenCV(): Boolean {
        return OpenCVLoader.initDebug()
    }
}