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
import kotlin.math.log10
import kotlin.math.max

class BreakdownScreen(
    private val game: GameMain,
    private val collectedImos: Int,
    private val imoCounts: Map<GameScreen.ImoType, Int>,
    private val resultScreen: ResultScreen
) : ScreenAdapter() {
    private val worldCamera = OrthographicCamera()
    private val viewport = ExtendViewport(1080f, 1920f, worldCamera)
    private val pixelCamera = OrthographicCamera()
    private val shapeRenderer = ShapeRenderer()
    private val font = BitmapFont(Gdx.files.internal("font.fnt")) // 黒色フォント
    private val fontWhite = BitmapFont(Gdx.files.internal("font_white.fnt")) // 白色フォント
    private val backButtonTexture = Texture(Gdx.files.internal("back_btn.png"))
    private var syuukakuTexture: Texture? = null
    private val textureCache = mutableMapOf<String, Texture>()
    private val buttonScale = 0.8f
    private val imoScale = 0.2f
    private val buttonWidth = backButtonTexture.width * buttonScale
    private val buttonHeight = backButtonTexture.height * buttonScale
    private val backButton = Rectangle(50f, 1820f - buttonHeight, buttonWidth, buttonHeight)

    init {
        worldCamera.position.set(1080f / 2f, 1920f / 2f, 0f)
        worldCamera.update()
        pixelCamera.setToOrtho(false, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        pixelCamera.position.set(Gdx.graphics.width / 2f, Gdx.graphics.height / 2f, 0f)
        pixelCamera.update()

        // フォントの初期設定
        font.region.texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear) // 滑らかに表示
        fontWhite.data.setScale(0.7f) // 白色フォントのスケールを0.7に設定
        fontWhite.region.texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear) // 滑らかに表示

        backButtonTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)

        // syuukakusuu.png の読み込み
        try {
            syuukakuTexture = Texture(Gdx.files.internal("syuukakusuu.png")).apply {
                setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            }
        } catch (e: Exception) {
            Gdx.app.error("BreakdownScreen", "Failed to load syuukakusuu.png: ${e.message}")
        }

        // 芋のテクスチャをキャッシュ
        imoCounts.keys.forEach { imoType ->
            try {
                val texture = Texture(Gdx.files.internal(imoType.textureName)).apply {
                    setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
                }
                textureCache[imoType.textureName] = texture
            } catch (e: Exception) {
                Gdx.app.error("BreakdownScreen", "Failed to load ${imoType.textureName}: ${e.message}")
            }
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

        // 戻るボタン（左上）
        game.batch.draw(backButtonTexture, backButton.x, backButton.y, backButton.width, backButton.height)

        // syuukakusuu.png と収穫数（戻るボタンと同じ高さ、画面中央）
        val yPosHeader = 1820f - buttonHeight
        if (syuukakuTexture != null) {
            val syuukakuWidth = syuukakuTexture!!.width * buttonScale
            val syuukakuHeight = syuukakuTexture!!.height * buttonScale
            val xPosSyuukaku = 1080f / 2 - syuukakuWidth / 2
            game.batch.draw(
                syuukakuTexture,
                xPosSyuukaku + 5,
                yPosHeader - 25,
                syuukakuWidth,
                syuukakuHeight
            )
            font.data.setScale(1.0f) // スケールを1.0に設定
            font.setColor(0f, 0f, 0f, 1f) // 黒色（デフォルト）
            // 収穫数の桁数に応じて間隔を調整
            val digitCount = max(1, log10(collectedImos.toFloat()).toInt() + 1)
            val offset = 50f + digitCount * 10f
            font.draw(
                game.batch,
                "$collectedImos",
                xPosSyuukaku + syuukakuWidth + offset,
                yPosHeader + syuukakuHeight / 2,
                0f,
                Align.center,
                false
            )
        } else {
            // フォールバック：収穫数を中央に表示
            font.data.setScale(1.0f) // スケールを1.0に設定
            font.setColor(0f, 0f, 0f, 1f) // 黒色（デフォルト）
            font.draw(
                game.batch,
                "収穫: $collectedImos",
                540f,
                yPosHeader + buttonHeight / 2,
                0f,
                Align.center,
                false
            )
        }

        // 芋ごとの内訳（y=1600から開始、3列表示）
        imoCounts.entries.filter { it.value > 0 }.forEachIndexed { index, (imoType, count) ->
            val column = index % 3 // 0: 左列, 1: 中央列, 2: 右列
            val row = index / 3 // 行番号
            val xPos = when (column) {
                0 -> 100f // 左列
                1 -> 400f // 中央列
                else -> 700f // 右列
            }
            val yPos = 1600f - row * 180f // 行ごとに180ピクセル下

            val texture = textureCache[imoType.textureName] ?: textureCache["normal_imo.png"]
            if (texture != null) {
                game.batch.draw(
                    texture,
                    xPos,
                    yPos - texture.height * imoScale / 2,
                    texture.width * imoScale,
                    texture.height * imoScale
                )
                fontWhite.draw(
                    game.batch,
                    "$count",
                    xPos + texture.width * imoScale + 20f,
                    yPos,
                    0f,
                    Align.center,
                    false
                )
            }
        }

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
            Gdx.app.log("BreakdownScreen", "Touch: x=$touchX, y=$touchY")

            if (backButton.contains(touchX, touchY)) {
                Gdx.app.log("BreakdownScreen", "Back button tapped")
                game.setScreen(resultScreen)
            }
        }
    }

    override fun dispose() {
        font.dispose()
        fontWhite.dispose() // font_whiteのリソース解放を追加
        backButtonTexture.dispose()
        syuukakuTexture?.dispose()
        textureCache.values.forEach { it.dispose() }
        shapeRenderer.dispose()
    }
}
