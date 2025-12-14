package com.minesweeper.model

import kotlin.random.Random

class Field(val rows: Int, val cols: Int, val totalMines: Int) {
    val cells: Array<Array<Cell>> = Array(rows) { Array(cols) { Cell() } }
    var minesGenerated = false
    var gameStarted = false

    fun generateMines(excludeRow: Int, excludeCol: Int) {
        val excludedCells = mutableSetOf<Pair<Int, Int>>()

        // Исключаем первую ячейку и её соседей
        for (i in -1..1) {
            for (j in -1..1) {
                val r = excludeRow + i
                val c = excludeCol + j
                if (r in 0 until rows && c in 0 until cols) {
                    excludedCells.add(Pair(r, c))
                }
            }
        }

        var minesPlaced = 0
        while (minesPlaced < totalMines) {
            val r = Random.nextInt(rows)
            val c = Random.nextInt(cols)

            if (excludedCells.contains(Pair(r, c)) || cells[r][c].hasMine) continue

            cells[r][c].hasMine = true
            minesPlaced++
        }

        minesGenerated = true
        calculateMinesAround()
        gameStarted = true
    }

    private fun calculateMinesAround() {
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                if (!cells[row][col].hasMine) {
                    cells[row][col].minesAround = countMinesAround(row, col)
                }
            }
        }
    }

    private fun countMinesAround(row: Int, col: Int): Int {
        var count = 0
        for (i in -1..1) {
            for (j in -1..1) {
                if (i == 0 && j == 0) continue
                val newRow = row + i
                val newCol = col + j
                if (newRow in 0 until rows && newCol in 0 until cols && cells[newRow][newCol].hasMine) {
                    count++
                }
            }
        }
        return count
    }

    fun revealCell(row: Int, col: Int): RevealResult {
        val cell = cells[row][col]

        if (cell.isRevealed || cell.isFlagged) {
            return RevealResult.ALREADY_REVEALED
        }

        cell.isRevealed = true

        if (cell.hasMine) {
            return RevealResult.MINE
        }

        if (!minesGenerated) {
            generateMines(row, col)
        }

        if (cell.minesAround == 0) {
            revealAdjacentCells(row, col)
        }

        return RevealResult.SAFE
    }

    private fun revealAdjacentCells(row: Int, col: Int) {
        for (i in -1..1) {
            for (j in -1..1) {
                if (i == 0 && j == 0) continue
                val newRow = row + i
                val newCol = col + j
                if (newRow in 0 until rows && newCol in 0 until cols) {
                    val adjacentCell = cells[newRow][newCol]
                    if (!adjacentCell.isRevealed && !adjacentCell.isFlagged && !adjacentCell.hasMine) {
                        adjacentCell.isRevealed = true
                        if (adjacentCell.minesAround == 0) {
                            revealAdjacentCells(newRow, newCol)
                        }
                    }
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

    fun checkWin(): Boolean {
        for (row in cells) {
            for (cell in row) {
                if (!cell.hasMine && !cell.isRevealed) {
                    return false
                }
            }
        }
        return true
    }

    fun revealAllMines() {
        for (row in cells) {
            for (cell in row) {
                if (cell.hasMine) {
                    cell.isRevealed = true
                }
            }
        }
    }

    fun getRemainingMines(): Int {
        var flaggedCount = 0
        for (row in cells) {
            for (cell in row) {
                if (cell.isFlagged) {
                    flaggedCount++
                }
            }
        }
        return totalMines - flaggedCount
    }

    fun restart() {
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val cell = cells[row][col]
                cell.hasMine = false
                cell.isRevealed = false
                cell.isFlagged = false
                cell.minesAround = 0
            }
        }
        minesGenerated = false
        gameStarted = false
    }
}

enum class RevealResult {
    SAFE, MINE, ALREADY_REVEALED
}