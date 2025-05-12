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
import kotlin.math.floor

class ReinforcementScreen(private val game: GameMain) : ScreenAdapter() {
    private val worldCamera = OrthographicCamera()
    private val viewport = ExtendViewport(1080f, 1920f, worldCamera)
    private val pixelCamera = OrthographicCamera()
    private val shapeRenderer = ShapeRenderer()
    private val font = BitmapFont()
    private val upgradeButtonTexture = Texture(Gdx.files.internal("upgrade_btn.png"))
    private val moguraTexture = Texture(Gdx.files.internal("mogura1.png"))
    private val turuhasiTexture = Texture(Gdx.files.internal("turuhasi.png"))
    private val imoTexture = Texture(Gdx.files.internal("normal_imo.png"))
    private val buttonScale = 0.8f
    private val imageScale = 0.6f
    private val imoScale = 0.5f
    private val buttonWidth = upgradeButtonTexture.width * buttonScale
    private val buttonHeight = upgradeButtonTexture.height * buttonScale
    private val moguraUpgradeButton = Rectangle(590f, 1600f, buttonWidth, buttonHeight)
    private val turuhasiValueButton = Rectangle(590f, 1150f, buttonWidth, buttonHeight) // ツルハシ強化ボタン
    private val turuhasiUnlockButton = Rectangle(590f, 700f, buttonWidth, buttonHeight) // ツルハシ解放ボタン
    private val backButton = Rectangle(50f, 1900f - 50f, 300f, 50f) // Back to Top ボタン

    init {
        worldCamera.position.set(1080f / 2f, 1920f / 2f, 0f)
        worldCamera.update()
        pixelCamera.setToOrtho(false, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        pixelCamera.position.set(Gdx.graphics.width / 2f, Gdx.graphics.height / 2f, 0f)
        pixelCamera.update()
        font.data.setScale(3.8f) // フォントサイズを3.8倍
        upgradeButtonTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        moguraTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        turuhasiTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        imoTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)

        // ボタン領域のデバッグログ
        Gdx.app.log("ReinforcementScreen", "Button size: width=$buttonWidth, height=$buttonHeight")
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
        val midY = screenHeight / 2f
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
        shapeRenderer.setColor(0.5451f, 0.3412f, 0.2157f, 1f) // 茶色（下部 y=0～300）
        shapeRenderer.rect(0f, 0f, 1080f, 300f)
        shapeRenderer.end()

        game.batch.projectionMatrix = worldCamera.combined
        game.batch.begin()

        // Back to Top ボタン
        font.draw(game.batch, "< Back to Top", 50f, 1900f, 0f, Align.left, false)

        // 強化項目（Mogura）
        // 左側：mogura1.png と Bonus
        game.batch.draw(moguraTexture, 50f, 1600f, moguraTexture.width * imageScale, moguraTexture.height * imageScale)
        font.draw(game.batch, "Bonus", 120f, 1550f, 0f, Align.left, false)
        // 右側：upgrade_btn.png と Next ポイント
        font.draw(game.batch, "Next ${game.moguraCost}pt", 590f, 1700f + buttonHeight + 50f, 0f, Align.left, false)
        game.batch.draw(upgradeButtonTexture, moguraUpgradeButton.x, moguraUpgradeButton.y, moguraUpgradeButton.width, moguraUpgradeButton.height)

        // 強化項目（Tsuruhasi レベル強化）
        // 左側：turuhasi.png と Bonus
        game.batch.draw(turuhasiTexture, 50f, 1150f, turuhasiTexture.width * imageScale, turuhasiTexture.height * imageScale)
        font.draw(game.batch, "Bonus", 120f, 1100f, 0f, Align.left, false)
        // 右側：upgrade_btn.png と Next ポイント
        font.draw(game.batch, "Next ${game.turuhasiValueCost}pt", 590f, 1250f + buttonHeight + 50f, 0f, Align.left, false)
        game.batch.draw(upgradeButtonTexture, turuhasiValueButton.x, turuhasiValueButton.y, turuhasiValueButton.width, turuhasiValueButton.height)

        // 強化項目（Tsuruhasi 解放）
        // 左側：turuhasi.png と Quantity
        game.batch.draw(turuhasiTexture, 50f, 700f, turuhasiTexture.width * imageScale, turuhasiTexture.height * imageScale)
        font.draw(game.batch, "Quantity", 120f, 650f, 0f, Align.left, false)
        // 右側：upgrade_btn.png と Next ポイント（解放上限に達していない場合のみ）
        if (game.turuhasiUnlockedCount < 2) {
            font.draw(game.batch, "Next ${game.turuhasiUnlockCost}pt", 590f, 800f + buttonHeight + 50f, 0f, Align.left, false)
        }
        game.batch.draw(upgradeButtonTexture, turuhasiUnlockButton.x, turuhasiUnlockButton.y, turuhasiUnlockButton.width, turuhasiUnlockButton.height)

        // さつまいもとスコア
        game.batch.draw(imoTexture, 300f, 10f, imoTexture.width * imoScale, imoTexture.height * imoScale)
        font.draw(game.batch, "${game.score}", 500f, 150f, 0f, Align.left, false)

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
            Gdx.app.log("ReinforcementScreen", "Touch: x=$touchX, y=$touchY")

            // Back to Top ボタン
            if (backButton.contains(touchX, touchY)) {
                Gdx.app.log("ReinforcementScreen", "Back to Top button tapped")
                game.setScreen(TitleScreen(game))
            }

            if (moguraUpgradeButton.contains(touchX, touchY) && game.score >= game.moguraCost) {
                Gdx.app.log("ReinforcementScreen", "Mogura upgrade button tapped")
                game.score -= game.moguraCost
                game.moguraHarvest = floor(game.moguraHarvest * 1.5f).toInt()
                game.moguraCost = floor(game.moguraCost * 1.25f).toInt()
            }

            if (turuhasiValueButton.contains(touchX, touchY) && game.score >= game.turuhasiValueCost) {
                Gdx.app.log("ReinforcementScreen", "Tsuruhasi value button tapped")
                game.score -= game.turuhasiValueCost
                game.turuhasiLevel = floor(game.turuhasiLevel * 1.5f).toInt()
                game.turuhasiValueCost = floor(game.turuhasiValueCost * 1.25f).toInt()
                game.updateTuruhasiValue()
            }

            if (turuhasiUnlockButton.contains(touchX, touchY) && game.score >= game.turuhasiUnlockCost && game.turuhasiUnlockedCount < 2) {
                Gdx.app.log("ReinforcementScreen", "Tsuruhasi unlock button tapped")
                game.score -= game.turuhasiUnlockCost
                game.turuhasiUnlockedCount += 1
                if (game.turuhasiUnlockedCount == 1) {
                    game.turuhasiUnlockCost = 700 // ツルハシ3の解放コスト
                } else if (game.turuhasiUnlockedCount == 2) {
                    game.turuhasiUnlockCost = 0 // 上限到達
                }
                game.updateTuruhasiValue()
            }
        }
    }

    override fun dispose() {
        font.dispose()
        upgradeButtonTexture.dispose()
        moguraTexture.dispose()
        turuhasiTexture.dispose()
        imoTexture.dispose()
        shapeRenderer.dispose()
    }
}
