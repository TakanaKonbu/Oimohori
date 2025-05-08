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
    private val buttonScale = 0.7f
    private val reinforcementButton = Rectangle(
        50f,
        1870f - reinforcementButtonTexture.height * buttonScale,
        reinforcementButtonTexture.width * buttonScale,
        reinforcementButtonTexture.height * buttonScale
    )

    init {
        worldCamera.position.set(1080f / 2f, 1920f / 2f, 0f)
        worldCamera.update()
        titleTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        reinforcementButtonTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)

        // ボタン領域のデバッグログ
        Gdx.app.log("TitleScreen", "Button: x=${reinforcementButton.x}, y=${reinforcementButton.y}, width=${reinforcementButton.width}, height=${reinforcementButton.height}")
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

        // デバイス画面全体をピクセル座標で塗る（黒帯対策）
        shapeRenderer.projectionMatrix = pixelCamera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        val screenHeight = Gdx.graphics.height.toFloat()
        val screenWidth = Gdx.graphics.width.toFloat()
        val midY = screenHeight / 2f
        shapeRenderer.setColor(0.5451f, 0.3412f, 0.2157f, 1f) // 土の色
        shapeRenderer.rect(0f, 0f, screenWidth, midY)
        shapeRenderer.setColor(0.3608f, 0.8824f, 0.9020f, 1f) // 空の色
        shapeRenderer.rect(0f, midY, screenWidth, screenHeight - midY)
        shapeRenderer.end()

        viewport.apply()

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
                game.setScreen(ReinforcementScreen(game))
                return
            }

            // ボタン外の画面タッチで GameScreen に遷移
            if (touchX >= 0f && touchX <= 1080f && touchY >= 0f && touchY <= 1920f) {
                Gdx.app.log("TitleScreen", "Title tapped")
                game.setScreen(GameScreen(game))
            }
        }
    }

    override fun dispose() {
        titleTexture.dispose()
        reinforcementButtonTexture.dispose()
        shapeRenderer.dispose()
    }
}
