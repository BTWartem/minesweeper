package com.minesweeper.model

class Game(
    val rows: Int = 9,
    val cols: Int = 9,
    val mines: Int = 10
) {
    val field = Field(rows, cols, mines)
    var gameState: GameState = GameState.PLAYING
    private var startTime: Double? = null
    private var endTime: Double? = null

    val elapsedTime: Int
        get() = if (startTime != null) {
            val end = endTime ?: kotlin.js.Date.now()
            ((end - startTime!!) / 1000).toInt()
        } else 0

    fun revealCell(row: Int, col: Int): RevealResult {
        if (gameState != GameState.PLAYING) return RevealResult.ALREADY_REVEALED

        if (startTime == null) {
            startTime = kotlin.js.Date.now()
        }

        val result = field.revealCell(row, col)

        if (result == RevealResult.MINE) {
            gameState = GameState.LOST
            endTime = kotlin.js.Date.now()
            field.revealAllMines()
        } else if (result == RevealResult.SAFE && field.checkWin()) {
            gameState = GameState.WON
            endTime = kotlin.js.Date.now()
        }

        return result
    }

    fun toggleFlag(row: Int, col: Int): Boolean {
        if (gameState != GameState.PLAYING) return false
        if (!field.minesGenerated) return false   // ❗ КЛЮЧЕВО
        return field.toggleFlag(row, col)
    }

    fun restart() {
        field.restart()
        gameState = GameState.PLAYING
        startTime = null
        endTime = null
    }

    fun getRemainingMines(): Int {
        return field.getRemainingMines()
    }
}

enum class GameState {
    PLAYING, WON, LOST
}