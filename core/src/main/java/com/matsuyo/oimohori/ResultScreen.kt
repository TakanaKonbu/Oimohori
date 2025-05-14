package com.matsuyo.oimohori

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ExtendViewport

class ResultScreen(
    private val game: GameMain,
    private val totalPoints: Int,
    private val collectedImos: Int,
    private val imoCounts: Map<GameScreen.ImoType, Int>
) : ScreenAdapter() {
    private val worldCamera = OrthographicCamera()
    private val viewport = ExtendViewport(1080f, 1920f, worldCamera)
    private val pixelCamera = OrthographicCamera()
    private val shapeRenderer = ShapeRenderer()
    private val font = BitmapFont()
    private val fontWhite = BitmapFont(Gdx.files.internal("font_white.fnt")) // 白色フォントをロード
    private val mogura2Texture = Texture(Gdx.files.internal("mogura2.png"))
    private val mogura3Texture = Texture(Gdx.files.internal("mogura3.png"))
    private val imoTexture = Texture(Gdx.files.internal("normal_imo.png"))
    private val retryButtonTexture = Texture(Gdx.files.internal("retry_btn.png"))
    private val doubleButtonTexture = Texture(Gdx.files.internal("double_btn.png"))
    private val breakdownButtonTexture = Texture(Gdx.files.internal("breakdown_btn.png"))
    private val buttonScale = 0.8f
    private val imageScale = 1.0f
    private val imoScale = 0.5f
    private val buttonWidth = retryButtonTexture.width * buttonScale
    private val buttonHeight = retryButtonTexture.height * buttonScale
    private val retryButton = Rectangle((1080f - 3 * buttonWidth - 100f) / 2, 1620f, buttonWidth, buttonHeight)
    private val doubleButton = Rectangle((1080f - 3 * buttonWidth - 100f) / 2 + buttonWidth + 50f, 1620f, buttonWidth, buttonHeight)
    private val breakdownButton = Rectangle((1080f - 3 * buttonWidth - 100f) / 2 + 2 * (buttonWidth + 50f), 1620f, buttonWidth, buttonHeight)
    private var animationTimer = 0f
    private val animationInterval = 0.5f // 0.5秒ごとに切り替え
    private var isMogura2 = true

    init {
        worldCamera.position.set(1080f / 2f, 1920f / 2f, 0f)
        worldCamera.update()
        pixelCamera.setToOrtho(false, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        pixelCamera.position.set(Gdx.graphics.width / 2f, Gdx.graphics.height / 2f, 0f)
        pixelCamera.update()
        font.data.setScale(4.8f)
        fontWhite.data.setScale(1.0f) // 白色フォントのスケールを1.0に設定
        fontWhite.region.texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear) // 滑らかに表示
        mogura2Texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        mogura3Texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        imoTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        retryButtonTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        doubleButtonTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        breakdownButtonTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)

        // ResultScreen表示時に効果音を再生
        if (game.isResultSoundInitialized()) {
            game.resultSound.play()
        }
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
        // アニメーションタイマーを更新
        animationTimer += delta
        if (animationTimer >= animationInterval) {
            isMogura2 = !isMogura2
            animationTimer = 0f
            Gdx.app.log("ResultScreen", "Switched to ${if (isMogura2) "mogura2.png" else "mogura3.png"}")
        }

        // 黒帯対策：ピクセル座標で画面クリア
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // ピクセル座標で背景を上下分割
        shapeRenderer.projectionMatrix = pixelCamera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        val screenHeight = Gdx.graphics.height.toFloat()
        val screenWidth = Gdx.graphics.width.toFloat()
        val midY = screenHeight * 0.3385417f
        shapeRenderer.setColor(0.5451f, 0.3412f, 0.2157f, 1f) // 茶色（下半分）
        shapeRenderer.rect(0f, 0f, screenWidth, midY)
        shapeRenderer.setColor(0.3608f, 0.8824f, 0.9020f, 1f) // 水色（上半分）
        shapeRenderer.rect(0f, midY, screenWidth, screenHeight - midY)
        shapeRenderer.end()

        viewport.apply()

        // ワールド座標で背景を塗りつぶし
        shapeRenderer.projectionMatrix = worldCamera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.setColor(0.3608f, 0.8824f, 0.9020f, 1f) // 水色（全体）
        shapeRenderer.rect(0f, 0f, 1080f, 1920f)
        shapeRenderer.setColor(0.5451f, 0.3412f, 0.2157f, 1f) // 茶色（下部 y=0～650）
        shapeRenderer.rect(0f, 0f, 1080f, 650f)
        shapeRenderer.end()

        game.batch.projectionMatrix = worldCamera.combined
        game.batch.begin()

        // モグラ（y=600、中央、アニメーション）
        val currentMoguraTexture = if (isMogura2) mogura2Texture else mogura3Texture
        game.batch.draw(
            currentMoguraTexture,
            (1080f - currentMoguraTexture.width * imageScale) / 2,
            600f,
            currentMoguraTexture.width * imageScale,
            currentMoguraTexture.height * imageScale
        )

        // さつまいも（y=300, x=300）とスコア（y=300, x=550）
        game.batch.draw(imoTexture, 200f, 150f, imoTexture.width * imoScale, imoTexture.height * imoScale)
        fontWhite.draw(game.batch, "${totalPoints}pt GET", 450f, 300f, 0f, Align.left, false) // font_whiteを使用

        // ボタン（y=1620、中央）
        game.batch.draw(retryButtonTexture, retryButton.x, retryButton.y, retryButton.width, retryButton.height)
        game.batch.draw(doubleButtonTexture, doubleButton.x, doubleButton.y, doubleButton.width, doubleButton.height)
        game.batch.draw(breakdownButtonTexture, breakdownButton.x, breakdownButton.y, breakdownButton.width, breakdownButton.height)

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
            Gdx.app.log("ResultScreen", "Touch: x=$touchX, y=$touchY")

            if (retryButton.contains(touchX, touchY)) {
                Gdx.app.log("ResultScreen", "Retry button tapped")
                if (game.isPushSoundInitialized()) {
                    game.pushSound.play()
                }
                game.setScreen(TitleScreen(game))
            } else if (doubleButton.contains(touchX, touchY)) {
                Gdx.app.log("ResultScreen", "Double button tapped")
                if (game.isPushSoundInitialized()) {
                    game.pushSound.play()
                }
                // ダブルボタンの処理（未実装の場合、ログのみ）
            } else if (breakdownButton.contains(touchX, touchY)) {
                Gdx.app.log("ResultScreen", "Breakdown button tapped")
                if (game.isPushSoundInitialized()) {
                    game.pushSound.play()
                }
                game.setScreen(BreakdownScreen(game, collectedImos, imoCounts, this))
            }
        }
    }

    override fun dispose() {
        font.dispose()
        fontWhite.dispose() // font_whiteのリソース解放を追加
        mogura2Texture.dispose()
        mogura3Texture.dispose()
        imoTexture.dispose()
        retryButtonTexture.dispose()
        doubleButtonTexture.dispose()
        breakdownButtonTexture.dispose()
        shapeRenderer.dispose()
    }
}
