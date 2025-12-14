package com.minesweeper

import com.minesweeper.model.*
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLButtonElement

fun main() {
    window.onload = {
        val game = Game()
        setupUI(game)
        renderGame(game)

        window.setInterval({
            if (game.gameState == GameState.PLAYING) {
                document.getElementById("timer-value")!!.textContent =
                    game.elapsedTime.toString()
            }
        }, 1000)
    }
}

fun setupUI(game: Game) {
    val app = document.getElementById("app") ?: document.body!!

    app.innerHTML = """
        <div style="max-width:540px;margin:auto;font-family:Arial;">
            <h1 style="text-align:center;">🎮 Сапёр на Kotlin/JS</h1>

            <div style="
                display:flex;
                justify-content:space-between;
                align-items:center;
                padding:12px 20px;
                background:#f2f2f2;
                border-radius:12px;
                font-size:22px;
                margin-bottom:15px;
            ">
                💣 <span id="mine-count-value">${game.getRemainingMines()}</span>

                <button id="smiley-btn"
                    style="font-size:28px;width:60px;height:60px;border-radius:50%;cursor:pointer;">
                    😊
                </button>

                ⏱ <span id="timer-value">0</span>
            </div>

            <div id="game-board"
                style="
                display:grid;
                grid-template-columns:repeat(${game.cols}, 36px);
                gap:4px;
                padding:6px;
                background:#888;
                border-radius:8px;
                width:fit-content;
                margin:auto;
                ">
            </div>

            <div style="
                margin-top:15px;
                padding:12px;
                background:#fafafa;
                border-radius:8px;
                font-size:14px;
            ">
                <b>Как играть:</b><br>
                🖱 ЛКМ — открыть клетку<br>
                🖱 ПКМ — флаг<br>
                😊 — перезапуск<br>
                <br>
                ⚠ Флаги можно ставить только после первого хода
            </div>
        </div>
    """

    document.getElementById("smiley-btn")!!
        .addEventListener("click", {
            game.restart()
            renderGame(game)
        })
}

fun renderGame(game: Game) {
    val board = document.getElementById("game-board")!!
    document.getElementById("mine-count-value")!!.textContent =
        game.getRemainingMines().toString()

    board.innerHTML = ""

    for (row in 0 until game.rows) {
        for (col in 0 until game.cols) {
            val cell = game.field.cells[row][col]
            val btn = document.createElement("button") as HTMLButtonElement

            btn.style.cssText = """
                width:36px;
                height:36px;
                font-size:16px;
                font-weight:bold;
                border-radius:4px;
                border:2px solid #aaa;
                background:#cfcfcf;
                cursor:pointer;
                user-select:none;
            """

            if (cell.isRevealed) {
                btn.style.background = "#e8e8e8"
                btn.style.border = "1px solid #999"
                when {
                    cell.hasMine -> btn.textContent = "💣"
                    cell.minesAround > 0 -> btn.textContent = cell.minesAround.toString()
                }
            } else if (cell.isFlagged) {
                btn.textContent = "🚩"
            }

            btn.addEventListener("click", {
                game.revealCell(row, col)
                renderGame(game)
            })

            btn.addEventListener("contextmenu", { e ->
                e.preventDefault()
                game.toggleFlag(row, col)
                renderGame(game)
            })

            board.appendChild(btn)
        }
    }

    if (game.gameState == GameState.WON) {
        window.setTimeout({window.alert("🎉 Победа! Время: ${game.elapsedTime} сек")
        }, 100)
    }

    if (game.gameState == GameState.LOST) {
        window.setTimeout({
            window.alert("💥 Вы проиграли")
        }, 100)
    }
}