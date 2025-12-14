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

        // Таймер
        window.setInterval({
            if (game.gameState == GameState.PLAYING) {
                val timer = document.getElementById("timer")
                timer?.textContent = "⏱️: ${game.elapsedTime}"
            }
        }, 1000)
    }
}

fun setupUI(game: Game) {
    val app = document.getElementById("app") ?: document.body!!

    app.innerHTML = """
        <div style="text-align: center; max-width: 500px; margin: 0 auto;">
            <h1>🎮 Сапёр на Kotlin/JS</h1>
            
            <div id="status-panel" style="
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin: 20px 0;
                padding: 10px 20px;
                background: #f5f5f5;
                border-radius: 10px;
                font-family: monospace;
            ">
                <div id="mine-count" style="font-size: 24px; font-weight: bold;">
                    💣: <span id="mine-count-value">${game.getRemainingMines()}</span>
                </div>
                
                <button id="smiley-btn" style="
                    font-size: 28px;
                    width: 60px;
                    height: 60px;
                    border-radius: 50%;
                    border: 3px solid #999;
                    cursor: pointer;
                    background: #ffd700;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                ">😊</button>
                
                <div id="timer" style="font-size: 24px; font-weight: bold;">
                    ⏱️: <span id="timer-value">0</span>
                </div>
            </div>
            
            <div id="game-board" style="
                display: grid;
                grid-template-columns: repeat(${game.cols}, 35px);
                grid-gap: 3px;
                justify-content: center;
                background: #888;
                padding: 10px;
                border-radius: 8px;
                margin: 0 auto;
            "></div>
            
            <div style="margin-top: 25px; color: #555; font-size: 14px; text-align: left; padding: 15px; background: #f9f9f9; border-radius: 8px;">
                <p><strong>Как играть:</strong></p>
                <p>🖱️ <strong>Левый клик</strong> — открыть клетку</p>
                <p>🖱️ <strong>Правый клик</strong> — поставить/убрать флаг 🚩</p>
                <p>😊 <strong>Нажмите смайлик</strong> — перезапустить игру</p>
                <p style="margin-top: 10px; color: #777;">Цифра показывает сколько мин вокруг клетки</p>
            </div>
        </div>
    """

    // Обработчик для кнопки смайлика
    document.getElementById("smiley-btn")?.addEventListener("click", {
        game.restart()
        renderGame(game)
    })
}

fun renderGame(game: Game) {
    val board = document.getElementById("game-board") ?: return
    val mineCountValue = document.getElementById("mine-count-value") ?: return
    val timerValue = document.getElementById("timer-value") ?: return
    val smileyBtn = document.getElementById("smiley-btn") ?: return

    // Обновляем счетчики
    mineCountValue.textContent = game.getRemainingMines().toString()
    timerValue.textContent = game.elapsedTime.toString()

    // Обновляем смайлик
    smileyBtn.textContent = when (game.gameState) {
        GameState.PLAYING -> "😊"
        GameState.WON -> "😎"
        GameState.LOST -> "💀"
    }

    // Очищаем поле
    board.innerHTML = ""

    // Создаем кнопки для клеток
    for (row in 0 until game.rows) {
        for (col in 0 until game.cols) {
            val cell = game.field.cells[row][col]

            val button = document.createElement("button") as HTMLButtonElement

            // Базовые стили
            button.style.cssText = """
                width: 35px;
                height: 35px;
                border: 3px outset #ccc;
                font-weight: bold;
                cursor: pointer;
                font-size: 16px;
                margin: 0;
                padding: 0;
                display: flex;
                align-items: center;
                justify-content: center;
                transition: all 0.1s;
            """

            // Стили в зависимости от состояния клетки
            if (cell.isRevealed) {
                button.style.border = "1px solid #999"
                button.style.background = "#e0e0e0"

                if (cell.hasMine) {
                    button.style.background = "#ff4444"
                    button.textContent = "💣"
                } else if (cell.minesAround > 0) {
                    // Цвета для цифр
                    val color = when (cell.minesAround) {
                        1 -> "blue"
                        2 -> "green"
                        3 -> "red"
                        4 -> "darkblue"
                        5 -> "darkred"
                        6 -> "teal"
                        7 -> "black"
                        8 -> "gray"
                        else -> "#666"
                    }
                    button.style.color = color
                    button.textContent = cell.minesAround.toString()
                }
            } else {
                button.style.background = "#c0c0c0"
                if (cell.isFlagged) {
                    button.textContent = "🚩"
                }
            }

            // Обработчики событий
            button.onclick = {
                game.revealCell(row, col)
                renderGame(game)
            }

            button.oncontextmenu = { event ->
                event.preventDefault()
                game.toggleFlag(row, col)
                renderGame(game)
                false
            }

            // Эффект при наведении (только для закрытых клеток)
            if (!cell.isRevealed) {
                button.onmouseenter = {
                    if (!cell.isFlagged) {
                        button.style.background = "#d0d0d0"
                    }
                }
                button.onmouseleave = {
                    if (!cell.isFlagged) {
                        button.style.background = "#c0c0c0"
                    }
                }
            }

            board.appendChild(button)
        }
    }

    // Проверяем состояние игры для алертов
    window.setTimeout({
        when (game.gameState) {
            GameState.WON -> {
                window.alert("🎉 Поздравляем! Вы выиграли!\nВремя: ${game.elapsedTime} секунд")
            }
            GameState.LOST -> {
                window.alert("💥 Вы проиграли!\nНажмите смайлик, чтобы попробовать снова")
            }
            else -> {}
        }
    }, 100)
}