package com.matsuyo.oimohori

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.ExtendViewport

class TitleScreen(private val game: GameMain) : ScreenAdapter() {
    private val worldCamera = OrthographicCamera() // ワールド座標用カメラ
    private val viewport = ExtendViewport(1080f, 1920f, worldCamera)
    private val pixelCamera = OrthographicCamera() // ピクセル座標用カメラ
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

        // ピクセル座標用カメラを設定
        pixelCamera.setToOrtho(false, width.toFloat(), height.toFloat())
        pixelCamera.position.set(width / 2f, height / 2f, 0f)
        pixelCamera.update()

        Gdx.app.log("TitleScreen", "Viewport: width=${viewport.worldWidth}, height=${viewport.worldHeight}")
        Gdx.app.log("TitleScreen", "Screen: width=$width, height=$height")
    }

    override fun render(delta: Float) {
        // 画面をクリア（デフォルトは黒）
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // デバイス画面全体をShapeRendererで塗る（ピクセル座標）
        shapeRenderer.projectionMatrix = pixelCamera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        val screenHeight = Gdx.graphics.height.toFloat()
        val screenWidth = Gdx.graphics.width.toFloat()
        val midY = screenHeight / 2f

        // 下半分（#8b5737: RGB 139, 87, 55）
        shapeRenderer.setColor(0.5451f, 0.3412f, 0.2157f, 1f)
        shapeRenderer.rect(0f, 0f, screenWidth, midY)

        // 上半分（#5ce1e6: RGB 92, 225, 230）
        shapeRenderer.setColor(0.3608f, 0.8824f, 0.9020f, 1f)
        shapeRenderer.rect(0f, midY, screenWidth, screenHeight - midY)
        shapeRenderer.end()

        // ビューポートを適用してワールド座標で描画
        viewport.apply()
        game.batch.projectionMatrix = worldCamera.combined

        // title.pngをワールド座標の中央に描画
        game.batch.begin()
        val x = 1080f / 2f - titleTexture.width / 2f
        val y = 1920f / 2f - titleTexture.height / 2f
        game.batch.draw(titleTexture, x, y)
        game.batch.end()

        // 描画位置をログ出力（デバッグ用）
        Gdx.app.log("TitleScreen", "Drawing title.png at x=$x, y=$y, width=${titleTexture.width}, height=${titleTexture.height}")

        // タップ検知
        if (Gdx.input.isTouched && !isTapped) {
            isTapped = true
            Gdx.app.log("TitleScreen", "GameScreen")
        } else if (!Gdx.input.isTouched) {
            isTapped = false
        }
    }

    override fun dispose() {
        titleTexture.dispose()
        shapeRenderer.dispose()
    }
}
