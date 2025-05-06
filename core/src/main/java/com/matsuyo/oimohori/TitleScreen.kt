package com.matsuyo.oimohori

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.ExtendViewport

class TitleScreen(private val game: GameMain) : ScreenAdapter() {
    private val worldCamera = OrthographicCamera()
    private val viewport = ExtendViewport(1080f, 1920f, worldCamera)
    private val pixelCamera = OrthographicCamera()
    private val titleTexture: Texture = Texture(Gdx.files.internal("title.png"))
    private val shapeRenderer = ShapeRenderer()
    private var isTapped = false

    init {
        titleTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        worldCamera.position.set(1080f / 2f, 1920f / 2f, 0f)
        worldCamera.update()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
        worldCamera.position.set(1080f / 2f, 1920f / 2f, 0f)
        worldCamera.update()

        pixelCamera.setToOrtho(false, width.toFloat(), height.toFloat())
        pixelCamera.position.set(width / 2f, height / 2f, 0f)
        pixelCamera.update()

        Gdx.app.log("TitleScreen", "Viewport: width=${viewport.worldWidth}, height=${viewport.worldHeight}")
        Gdx.app.log("TitleScreen", "Screen: width=$width, height=$height")
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        shapeRenderer.projectionMatrix = pixelCamera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        val screenHeight = Gdx.graphics.height.toFloat()
        val screenWidth = Gdx.graphics.width.toFloat()
        val midY = screenHeight / 2f

        shapeRenderer.setColor(0.5451f, 0.3412f, 0.2157f, 1f)
        shapeRenderer.rect(0f, 0f, screenWidth, midY)
        shapeRenderer.setColor(0.3608f, 0.8824f, 0.9020f, 1f)
        shapeRenderer.rect(0f, midY, screenWidth, screenHeight - midY)
        shapeRenderer.end()

        viewport.apply()
        game.batch.projectionMatrix = worldCamera.combined

        game.batch.begin()
        val x = 1080f / 2f - titleTexture.width / 2f
        val y = 1920f / 2f - titleTexture.height / 2f
        game.batch.draw(titleTexture, x, y)
        game.batch.end()

        Gdx.app.log("TitleScreen", "Drawing title.png at x=$x, y=$y, width=${titleTexture.width}, height=${titleTexture.height}")

        if (Gdx.input.isTouched && !isTapped) {
            isTapped = true
            game.setScreen(GameScreen(game)) // GameScreenに遷移
        } else if (!Gdx.input.isTouched) {
            isTapped = false
        }
    }

    override fun dispose() {
        titleTexture.dispose()
        shapeRenderer.dispose()
    }
}
