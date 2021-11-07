package com.example.sudokusolver

import org.opencv.core.Point

data class BoardCoordinates(var topLeft: Point = Point(), var bottomLeft: Point = Point(), var bottomRight: Point = Point(), var topRight: Point = Point())
