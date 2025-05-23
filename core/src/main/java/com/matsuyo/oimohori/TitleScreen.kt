package com.matsuyo.oimohori

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.ExtendViewport

class TitleScreen(private val game: GameMain) : ScreenAdapter() {
    private val worldCamera = OrthographicCamera()
    private val pixelCamera = OrthographicCamera()
    private val viewport = ExtendViewport(1080f, 1920f, worldCamera)
    private val shapeRenderer = ShapeRenderer()
    private val titleTexture = Texture(Gdx.files.internal("title.png"))
    private val reinforcementButtonTexture = Texture(Gdx.files.internal("reinforcement_btn.png"))
    private val howToPlayButtonTexture = Texture(Gdx.files.internal("how_to_play_btn.png"))
    private val zukanButtonTexture = Texture(Gdx.files.internal("zukan_btn.png"))
    private val buttonScale = 0.7f
    private val reinforcementButton = Rectangle(
        50f,
        1870f - reinforcementButtonTexture.height * buttonScale,
        reinforcementButtonTexture.width * buttonScale,
        reinforcementButtonTexture.height * buttonScale
    )
    private val howToPlayButton = Rectangle(
        50f + reinforcementButton.width + 20f,
        1870f - howToPlayButtonTexture.height * buttonScale,
        howToPlayButtonTexture.width * buttonScale,
        howToPlayButtonTexture.height * buttonScale
    )
    private val zukanButton = Rectangle(
        50f + reinforcementButton.width + howToPlayButton.width + 40f,
        1870f - zukanButtonTexture.height * buttonScale,
        zukanButtonTexture.width * buttonScale,
        zukanButtonTexture.height * buttonScale
    )

    init {
        worldCamera.position.set(1080f / 2f, 1920f / 2f, 0f)
        worldCamera.update()
        titleTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        reinforcementButtonTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        howToPlayButtonTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        zukanButtonTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)

        // ボタン領域のデバッグログ
        Gdx.app.log("TitleScreen", "Reinforcement Button: x=${reinforcementButton.x}, y=${reinforcementButton.y}, width=${reinforcementButton.width}, height=${reinforcementButton.height}")
        Gdx.app.log("TitleScreen", "HowToPlay Button: x=${howToPlayButton.x}, y=${howToPlayButton.y}, width=${howToPlayButton.width}, height=${howToPlayButton.height}")
        Gdx.app.log("TitleScreen", "Zukan Button: x=${zukanButton.x}, y=${zukanButton.y}, width=${zukanButton.width}, height=${zukanButton.height}")
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
        worldCamera.position.set(1080f / 2f, 1920f / 2f, 0f)
        worldCamera.update()

        pixelCamera.setToOrtho(false, width.toFloat(), height.toFloat())
        pixelCamera.position.set(width / 2f, height / 2f, 0f)
        pixelCamera.update()
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        shapeRenderer.projectionMatrix = pixelCamera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        val screenHeight = Gdx.graphics.height.toFloat()
        val screenWidth = Gdx.graphics.width.toFloat()
        val midY = screenHeight * 0.3385417f
        shapeRenderer.setColor(0.5451f, 0.3412f, 0.2157f, 1f)
        shapeRenderer.rect(0f, 0f, screenWidth, midY)
        shapeRenderer.setColor(0.3608f, 0.8824f, 0.9020f, 1f)
        shapeRenderer.rect(0f, midY, screenWidth, screenHeight - midY)
        shapeRenderer.end()

        viewport.apply()

        shapeRenderer.projectionMatrix = worldCamera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.setColor(0.3608f, 0.8824f, 0.9020f, 1f)
        shapeRenderer.rect(0f, 0f, 1080f, 1920f)
        shapeRenderer.setColor(0.5451f, 0.3412f, 0.2157f, 1f)
        shapeRenderer.rect(0f, 0f, 1080f, 650f)
        shapeRenderer.end()

        game.batch.projectionMatrix = worldCamera.combined
        game.batch.begin()
        game.batch.draw(titleTexture, 0f, 0f, 1080f, 1920f)
        game.batch.draw(
            reinforcementButtonTexture,
            reinforcementButton.x,
            reinforcementButton.y,
            reinforcementButton.width,
            reinforcementButton.height
        )
        game.batch.draw(
            howToPlayButtonTexture,
            howToPlayButton.x,
            howToPlayButton.y,
            howToPlayButton.width,
            howToPlayButton.height
        )
        game.batch.draw(
            zukanButtonTexture,
            zukanButton.x,
            zukanButton.y,
            zukanButton.width,
            zukanButton.height
        )
        game.batch.end()

        handleInput()
    }

    private fun handleInput() {
        if (Gdx.input.justTouched()) {
            // ビューポートを使用してタッチ座標をワールド座標に変換
            val touchPos = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
            viewport.unproject(touchPos)
            val touchX = touchPos.x
            val touchY = touchPos.y

            // タッチ座標のデバッグログ
            Gdx.app.log("TitleScreen", "Touch: x=$touchX, y=$touchY")

            // ボタンタッチを最優先
            if (reinforcementButton.contains(touchX, touchY)) {
                Gdx.app.log("TitleScreen", "Reinforcement button tapped")
                if (game.isPushSoundInitialized()) {
                    game.pushSound.play()
                }
                game.setScreen(ReinforcementScreen(game))
                return
            }
            if (howToPlayButton.contains(touchX, touchY)) {
                Gdx.app.log("TitleScreen", "How to Play button tapped")
                if (game.isPushSoundInitialized()) {
                    game.pushSound.play()
                }
                game.setScreen(HowToPlayScreen(game))
                return
            }
            if (zukanButton.contains(touchX, touchY)) {
                Gdx.app.log("TitleScreen", "Zukan button tapped")
                if (game.isPushSoundInitialized()) {
                    game.pushSound.play()
                }
                game.setScreen(ZukanScreen(game))
                return
            }

            // ボタン外の画面タッチで GameScreen に遷移
            if (touchX in 0f..1080f && touchY >= 0f && touchY <= 1920f) {
                Gdx.app.log("TitleScreen", "Title tapped")
                game.resetPlayPoints() // ここでresetPlayPoints()を呼び出す
                game.setScreen(GameScreen(game))
            }
        }
    }

    override fun dispose() {
        titleTexture.dispose()
        reinforcementButtonTexture.dispose()
        howToPlayButtonTexture.dispose()
        zukanButtonTexture.dispose()
        shapeRenderer.dispose()
    }
}
