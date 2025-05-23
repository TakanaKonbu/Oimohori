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
import com.badlogic.gdx.Application

class ResultScreen(
    private val game: GameMain,
    private val totalPoints: Int,
    private val collectedImos: Int,
    private val imoCounts: Map<ImoType, Int>
) : ScreenAdapter() {
    private val worldCamera = OrthographicCamera()
    private val viewport = ExtendViewport(1080f, 1920f, worldCamera)
    private val pixelCamera = OrthographicCamera()
    private val shapeRenderer = ShapeRenderer()
    private val font = BitmapFont()
    private val fontWhite = BitmapFont(Gdx.files.internal("font_white.fnt"))
    private val mogura2TexturePath = "mogura2.png"
    private val mogura3TexturePath = "mogura3.png"
    private val imoTexturePath = "normal_imo.png"
    private val retryButtonTexturePath = "retry_btn.png"
    private val doubleButtonTexturePath = "double_btn.png"
    private val breakdownButtonTexturePath = "breakdown_btn.png"
    private val mogura2Texture = Texture(Gdx.files.internal(mogura2TexturePath))
    private val mogura3Texture = Texture(Gdx.files.internal(mogura3TexturePath))
    private val imoTexture = Texture(Gdx.files.internal(imoTexturePath))
    private val retryButtonTexture = Texture(Gdx.files.internal(retryButtonTexturePath))
    private val doubleButtonTexture = Texture(Gdx.files.internal(doubleButtonTexturePath))
    private val breakdownButtonTexture = Texture(Gdx.files.internal(breakdownButtonTexturePath))
    private val buttonScale = 0.8f
    private val imageScale = 1.0f
    private val imoScale = 0.5f
    private val buttonWidth = retryButtonTexture.width * buttonScale
    private val buttonHeight = retryButtonTexture.height * buttonScale
    private val retryButton = Rectangle((1080f - 3 * buttonWidth - 100f) / 2, 1620f, buttonWidth, buttonHeight)
    private val doubleButton = Rectangle((1080f - 3 * buttonWidth - 100f) / 2 + buttonWidth + 50f, 1620f, buttonWidth, buttonHeight)
    private val breakdownButton = Rectangle((1080f - 3 * buttonWidth - 100f) / 2 + 2 * (buttonWidth + 50f), 1620f, buttonWidth, buttonHeight)
    private var animationTimer = 0f
    private val animationInterval = 0.5f
    private var isMogura2 = true
    private var lastTouchTime = 0f
    private val touchCooldown = 0.2f // タッチ間隔（秒）

    init {
        worldCamera.position.set(1080f / 2f, 1920f / 2f, 0f)
        worldCamera.update()
        pixelCamera.setToOrtho(false, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        pixelCamera.position.set(Gdx.graphics.width / 2f, Gdx.graphics.height / 2f, 0f)
        pixelCamera.update()
        font.data.setScale(4.8f)
        fontWhite.data.setScale(1.0f)
        fontWhite.region.texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        mogura2Texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        mogura3Texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        imoTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        retryButtonTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        doubleButtonTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        breakdownButtonTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)

        if (game.isResultSoundInitialized()) {
            game.resultSound.play()
        }

        // ResultScreenが表示された際に、今回獲得したポイントをセット
        game.currentPlayPoints = totalPoints
        Gdx.app.log("ResultScreen", "Initialized: currentPlayPoints=$totalPoints, score=${game.score}, pointsAdded=${game.pointsAdded}")
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
        animationTimer += delta
        lastTouchTime += delta
        if (animationTimer >= animationInterval) {
            isMogura2 = !isMogura2
            animationTimer = 0f
            Gdx.app.log("ResultScreen", "Switched to ${if (isMogura2) mogura2TexturePath else mogura3TexturePath}")
        }

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

        val currentMoguraTexture = if (isMogura2) mogura2Texture else mogura3Texture
        game.batch.draw(
            currentMoguraTexture,
            (1080f - currentMoguraTexture.width * imageScale) / 2,
            600f,
            currentMoguraTexture.width * imageScale,
            currentMoguraTexture.height * imageScale
        )

        game.batch.draw(imoTexture, 200f, 150f, imoTexture.width * imoScale, imoTexture.height * imoScale)
        fontWhite.draw(game.batch, "${game.currentPlayPoints}pt GET", 450f, 300f, 0f, Align.left, false)

        game.batch.draw(retryButtonTexture, retryButton.x, retryButton.y, retryButton.width, retryButton.height)
        game.batch.draw(doubleButtonTexture, doubleButton.x, doubleButton.y, doubleButton.width, doubleButton.height)
        game.batch.draw(breakdownButtonTexture, breakdownButton.x, breakdownButton.y, breakdownButton.width, breakdownButton.height)

        game.batch.end()

        handleInput()
    }

    private fun handleInput() {
        if (Gdx.input.justTouched() && lastTouchTime >= touchCooldown) {
            lastTouchTime = 0f
            val touchPos = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
            viewport.unproject(touchPos)
            val touchX = touchPos.x
            val touchY = touchPos.y

            Gdx.app.log("ResultScreen", "Touch: x=$touchX, y=$touchY")

            if (retryButton.contains(touchX, touchY)) {
                Gdx.app.log("ResultScreen", "Retry button tapped")
                if (game.isPushSoundInitialized()) {
                    game.pushSound.play()
                }
                // 初回のみポイントを加算
                if (!game.pointsAdded) {
                    game.updateScore(game.score + game.currentPlayPoints)
                    game.pointsAdded = true
                    Gdx.app.log("ResultScreen", "Points added on Retry: currentPlayPoints=${game.currentPlayPoints}, score=${game.score}")
                } else {
                    Gdx.app.log("ResultScreen", "Points already added, skipping on Retry.")
                }
                game.setScreen(TitleScreen(game))
            } else if (doubleButton.contains(touchX, touchY)) {
                Gdx.app.log("ResultScreen", "Double button tapped")
                if (game.isPushSoundInitialized()) {
                    game.pushSound.play()
                }
                if (Gdx.app.type == Application.ApplicationType.Android) {
                    if (game.pointsAdded) {
                        game.showMessageDialog("次のプレイ後に有効になります。")
                        Gdx.app.log("ResultScreen", "Message dialog shown: Ad already used")
                    } else {
                        game.showAdDialog("広告を視聴してポイントを2倍にしますか？", Runnable {
                            game.currentPlayPoints *= 2
                            game.updateScore(game.score + game.currentPlayPoints)
                            game.pointsAdded = true
                            Gdx.app.log("ResultScreen", "Points doubled and added: currentPlayPoints=${game.currentPlayPoints}, score=${game.score}")
                        })
                    }
                }
            } else if (breakdownButton.contains(touchX, touchY)) {
                Gdx.app.log("ResultScreen", "Breakdown button tapped")
                if (game.isPushSoundInitialized()) {
                    game.pushSound.play()
                }
                // 初回のみポイントを加算
                if (!game.pointsAdded) {
                    game.updateScore(game.score + game.currentPlayPoints)
                    game.pointsAdded = true
                    Gdx.app.log("ResultScreen", "Points added on Breakdown: currentPlayPoints=${game.currentPlayPoints}, score=${game.score}")
                } else {
                    Gdx.app.log("ResultScreen", "Points already added, skipping on Breakdown.")
                }
                game.setScreen(BreakdownScreen(game, collectedImos, imoCounts, this))
            }
        }
    }

    override fun dispose() {
        font.dispose()
        fontWhite.dispose()
        mogura2Texture.dispose()
        mogura3Texture.dispose()
        imoTexture.dispose()
        retryButtonTexture.dispose()
        doubleButtonTexture.dispose()
        breakdownButtonTexture.dispose()
        shapeRenderer.dispose()
    }
}
