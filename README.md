# Sudoku Solver Application
Project for PROG2007 2021 Autmn course

## Introduction
This project involves making an app that will solve a Sudoku puzzle for the user. The puzzle can be added in three ways: Manually entering numbers onto a board, loading in an existing photo from the gallery, or taking a new photo. The solver will attempt to solve the puzzle passed to it, and if successful will display the finished board to the user in the same activity. If no sudoku board is detected, the user will get a message about this. If a board that has errors is passed in and the app cannot solve it, it will return a message about this and reset the board.

## Motivation
This project was inspired by the group’s enjoyment of various puzzle games. Our consideration was that making an app that related to puzzles would be fun, and the group quickly came up with the idea of solving sudoku puzzles. The overall challenge of implementing parsing images, using machine learning in android studio, implementing a solving algorithm in Kotlin, and designing and creating a UI with Jetpack Compose seemed both very fun and a great chance to learn a lot about the future of mobile application development.

## Scope
The project goal is to create a minimum viable product (MVP) of the application. Compared to other sudoku-solving applications on the market, we will not support constant feedback to the player when adding numbers to the board. Adding/removing numbers is primarily there for correcting any possible mistakes the recognizer has done.
Similarly, once a well-performing model for parsing images was in place, no further finetuning was performed - as that would be a lot of extra work for marginal gain. For the solving algorithm, a more complex model than the backtracking algorithm was considered but discarded as out of scope as well.

