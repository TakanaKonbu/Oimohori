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

class HowToPlayScreen(private val game: GameMain) : ScreenAdapter() {
    private val worldCamera = OrthographicCamera()
    private val pixelCamera = OrthographicCamera()
    private val viewport = ExtendViewport(1080f, 1920f, worldCamera)
    private val shapeRenderer = ShapeRenderer()
    private val howToPlayTexture = Texture(Gdx.files.internal("how_to_play.png"))
    private val backToTopTexture = Texture(Gdx.files.internal("back_to_top.png"))
    private val mogura2Texture = Texture(Gdx.files.internal("mogura2.png"))
    private val mogura3Texture = Texture(Gdx.files.internal("mogura3.png"))
    private var elapsedTime = 0f
    private var showMogura2 = true
    private val backButtonScale = 1.0f
    private val backButton = Rectangle(
        50f,
        1800f - backToTopTexture.height * backButtonScale,
        backToTopTexture.width * backButtonScale,
        backToTopTexture.height * backButtonScale
    )

    init {
        worldCamera.position.set(1080f / 2f, 1920f / 2f, 0f)
        worldCamera.update()
        howToPlayTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        backToTopTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        mogura2Texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        mogura3Texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)

        // ボタン領域のデバッグログ
        Gdx.app.log("HowToPlayScreen", "Back Button: x=${backButton.x}, y=${backButton.y}, width=${backButton.width}, height=${backButton.height}")
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
        // Update elapsed time and toggle between mogura2 and mogura3 every second
        elapsedTime += delta
        if (elapsedTime >= 1f) {
            showMogura2 = !showMogura2
            elapsedTime -= 1f
        }

        Gdx.gl.glClearColor(0.3608f, 0.8824f, 0.9020f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        viewport.apply()

        game.batch.projectionMatrix = worldCamera.combined
        game.batch.begin()
        // how_to_play.pngを画面中央に配置
        game.batch.draw(
            howToPlayTexture,
            (1080f - howToPlayTexture.width) / 2f,
            (1820f - howToPlayTexture.height) / 2f,
            howToPlayTexture.width.toFloat(),
            howToPlayTexture.height.toFloat()
        )
        // back_to_top.pngを左上に配置
        game.batch.draw(
            backToTopTexture,
            backButton.x,
            backButton.y,
            backButton.width,
            backButton.height
        )
        // mogura2.png or mogura3.pngを画面下部中央に配置
        val moguraTexture = if (showMogura2) mogura2Texture else mogura3Texture
        game.batch.draw(
            moguraTexture,
            (1080f - moguraTexture.width) / 2f, // 中央揃え
            50f, // 画面下部（y=50で底から少し浮かせる）
            moguraTexture.width.toFloat(),
            moguraTexture.height.toFloat()
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
            Gdx.app.log("HowToPlayScreen", "Touch: x=$touchX, y=$touchY")

            // Back to Topボタン
            if (backButton.contains(touchX, touchY)) {
                Gdx.app.log("HowToPlayScreen", "Back to Top button tapped")
                game.setScreen(TitleScreen(game))
            }
        }
    }

    override fun dispose() {
        howToPlayTexture.dispose()
        backToTopTexture.dispose()
        mogura2Texture.dispose()
        mogura3Texture.dispose()
        shapeRenderer.dispose()
    }
}
