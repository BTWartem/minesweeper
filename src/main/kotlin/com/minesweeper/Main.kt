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
        <div style="text-align:center; max-width:520px; margin:auto;">
            <h1>🎮 Сапёр на Kotlin/JS</h1>

            <div style="
                display:flex;
                justify-content:space-between;
                align-items:center;
                padding:10px 20px;
                background:#f0f0f0;
                border-radius:10px;
                font-family:monospace;
                font-size:22px;
            ">
                💣 <span id="mine-count-value">${game.getRemainingMines()}</span>

                <button id="smiley-btn"
                    style="font-size:28px;width:60px;height:60px;border-radius:50%;">
                    😊
                </button>

                ⏱️ <span id="timer-value">0</span>
            </div>

            <div id="game-board"
                style="
                display:grid;
                grid-template-columns:repeat(${game.cols}, 36px);
                gap:4px;
                background:#888;
                padding:10px;
                border-radius:8px;
                margin-top:15px;
                ">
            </div>

            <div style="
                margin-top:15px;
                text-align:left;
                background:#fafafa;
                padding:12px;
                border-radius:8px;
                font-size:14px;
            ">
                <b>Как играть:</b><br>
                🖱 ЛКМ — открыть клетку<br>
                🖱 ПКМ — поставить / убрать флаг<br>
                😊 — перезапуск игры<br>
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
                width:36px;height:36px;
                font-size:16px;
                font-weight:bold;
                user-select:none;
                border:3px outset #ccc;
                cursor:pointer;
            """

            if (cell.isRevealed) {
                btn.style.border = "1px solid #999"
                btn.style.background = "#e0e0e0"

                when {
                    cell.hasMine -> btn.textContent = "💣"
                    cell.minesAround > 0 -> btn.textContent = cell.minesAround.toString()
                }
            } else {
                btn.style.background = "#c0c0c0"
                if (cell.isFlagged) btn.textContent = "🚩"
            }

            // ЛКМ
            btn.addEventListener("click", {
                game.revealCell(row, col)
                renderGame(game)
            })

            // ПКМ
            btn.addEventListener("contextmenu", { e ->
                e.preventDefault()
                game.toggleFlag(row, col)
                renderGame(game)
            })

            board.appendChild(btn)
        }
    }

    if (game.gameState == GameState.WON) {
        window.setTimeout({window.alert("🎉 Победа!\nВремя: ${game.elapsedTime} сек")
        }, 100)
    }

    if (game.gameState == GameState.LOST) {
        window.setTimeout({
            window.alert("💥 Поражение!\nНажмите 😊 для новой игры")
        }, 100)
    }
}