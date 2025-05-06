package com.matsuyo.oimohori

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.ExtendViewport

class GameScreen(private val game: GameMain) : ScreenAdapter() {
    private val worldCamera = OrthographicCamera()
    private val viewport = ExtendViewport(1080f, 1920f, worldCamera)
    private val pixelCamera = OrthographicCamera()
    private val shapeRenderer = ShapeRenderer()
    private val moguraTexture: Texture = Texture(Gdx.files.internal("mogura1.png"))
    private val turuhasiTexture: Texture = Texture(Gdx.files.internal("turuhasi.png"))

    init {
        worldCamera.position.set(1080f / 2f, 1920f / 2f, 0f)
        worldCamera.update()
        moguraTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        turuhasiTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
        worldCamera.position.set(1080f / 2f, 1920f / 2f, 0f)
        worldCamera.update()

        pixelCamera.setToOrtho(false, width.toFloat(), height.toFloat())
        pixelCamera.position.set(width / 2f, height / 2f, 0f)
        pixelCamera.update()

        Gdx.app.log("GameScreen", "Viewport: width=${viewport.worldWidth}, height=${viewport.worldHeight}")
        Gdx.app.log("GameScreen", "Screen: width=$width, height=$height")
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

        shapeRenderer.setColor(0.5451f, 0.3412f, 0.2157f, 1f) // 下半分: #8b5737
        shapeRenderer.rect(0f, 0f, screenWidth, midY)
        shapeRenderer.setColor(0.3608f, 0.8824f, 0.9020f, 1f) // 上半分: #5ce1e6
        shapeRenderer.rect(0f, midY, screenWidth, screenHeight - midY)
        shapeRenderer.end()

        // ビューポートを適用してワールド座標で描画
        viewport.apply()

        // ワールド座標全体を#5ce1e6で塗る
        shapeRenderer.projectionMatrix = worldCamera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.setColor(0.3608f, 0.8824f, 0.9020f, 1f) // #5ce1e6
        shapeRenderer.rect(0f, 0f, 1080f, 1920f)
        // ワールド座標下部（Y=0～650）を#8b5737で塗る
        shapeRenderer.setColor(0.5451f, 0.3412f, 0.2157f, 1f) // #8b5737
        shapeRenderer.rect(0f, 0f, 1080f, 650f)
        shapeRenderer.end()

        // モグラとつるはしを描画
        game.batch.projectionMatrix = worldCamera.combined
        game.batch.begin()
        // モグラ
        val moguraX = 1080f / 2f - moguraTexture.width / 2f
        val moguraY = 580f
        game.batch.draw(moguraTexture, moguraX, moguraY)
        // つるはし（1本、中央）
        val turuhasiScale = 0.5f // スケールファクター（50%）
        val turuhasiWidth = turuhasiTexture.width * turuhasiScale
        val turuhasiHeight = turuhasiTexture.height * turuhasiScale
        val turuhasiX = 1080f / 2f - turuhasiWidth / 2f // スケール後の幅で中央揃え
        val turuhasiY = 1870f - turuhasiHeight // スケール後の高さで下端がY=1870
        game.batch.draw(turuhasiTexture, turuhasiX, turuhasiY, turuhasiWidth, turuhasiHeight)
        game.batch.end()

        // デバッグ用ログ
        Gdx.app.log("GameScreen", "Drawing mogura1.png at x=$moguraX, y=$moguraY, width=${moguraTexture.width}, height=${moguraTexture.height}")
        Gdx.app.log("GameScreen", "Drawing turuhasi.png at x=$turuhasiX, y=$turuhasiY, width=${turuhasiTexture.width}, height=${turuhasiTexture.height}")
    }

    override fun dispose() {
        moguraTexture.dispose()
        turuhasiTexture.dispose()
        shapeRenderer.dispose()
    }
}
