package com.minesweeper.model

class Cell(
    var hasMine: Boolean = false,
    var isRevealed: Boolean = false,
    var isFlagged: Boolean = false,
    var minesAround: Int = 0
)