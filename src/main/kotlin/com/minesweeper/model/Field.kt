package com.minesweeper.model

import kotlin.random.Random

class Field(val rows: Int, val cols: Int, val totalMines: Int) {
    val cells: Array<Array<Cell>> = Array(rows) { Array(cols) { Cell() } }
    var minesGenerated = false

    fun generateMines(excludeRow: Int, excludeCol: Int) {
        val excluded = mutableSetOf<Pair<Int, Int>>()

        for (i in -1..1)
            for (j in -1..1) {
                val r = excludeRow + i
                val c = excludeCol + j
                if (r in 0 until rows && c in 0 until cols)
                    excluded.add(r to c)
            }

        var placed = 0
        while (placed < totalMines) {
            val r = Random.nextInt(rows)
            val c = Random.nextInt(cols)
            if ((r to c) in excluded || cells[r][c].hasMine) continue
            cells[r][c].hasMine = true
            placed++
        }

        minesGenerated = true
        calculateMinesAround()
    }

    private fun calculateMinesAround() {
        for (r in 0 until rows)
            for (c in 0 until cols)
                if (!cells[r][c].hasMine)
                    cells[r][c].minesAround = countMinesAround(r, c)
    }

    private fun countMinesAround(row: Int, col: Int): Int {
        var count = 0
        for (i in -1..1)
            for (j in -1..1) {
                if (i == 0 && j == 0) continue
                val r = row + i
                val c = col + j
                if (r in 0 until rows && c in 0 until cols && cells[r][c].hasMine)
                    count++
            }
        return count
    }

    fun revealCell(row: Int, col: Int): RevealResult {
        val cell = cells[row][col]
        if (cell.isRevealed || cell.isFlagged) return RevealResult.ALREADY_REVEALED

        if (!minesGenerated) generateMines(row, col)

        cell.isRevealed = true
        if (cell.hasMine) return RevealResult.MINE

        if (cell.minesAround == 0) revealAdjacent(row, col)
        return RevealResult.SAFE
    }

    private fun revealAdjacent(row: Int, col: Int) {
        for (i in -1..1)
            for (j in -1..1) {
                val r = row + i
                val c = col + j
                if (r in 0 until rows && c in 0 until cols) {
                    val cell = cells[r][c]
                    if (!cell.isRevealed && !cell.isFlagged && !cell.hasMine) {
                        cell.isRevealed = true
                        if (cell.minesAround == 0)
                            revealAdjacent(r, c)
                    }
                }
            }
    }

    fun toggleFlag(row: Int, col: Int): Boolean {
        val cell = cells[row][col]
        if (!cell.isRevealed) {
            cell.isFlagged = !cell.isFlagged
            return true
        }
        return false
    }

    fun checkWin(): Boolean =
        cells.all { row -> row.all { it.hasMine || it.isRevealed } }

    fun revealAllMines() {
        cells.flatten().filter { it.hasMine }.forEach { it.isRevealed = true }
    }

    fun getRemainingMines(): Int =
        totalMines - cells.flatten().count { it.isFlagged }

    fun restart() {
        cells.flatten().forEach {
            it.hasMine = false
            it.isRevealed = false
            it.isFlagged = false
            it.minesAround = 0
        }
        minesGenerated = false
    }
}

enum class RevealResult {
    SAFE, MINE, ALREADY_REVEALED
}