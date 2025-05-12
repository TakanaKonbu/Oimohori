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
    private val syuukakuTexture = Texture(Gdx.files.internal("syuukaku.png"))
    private val honsuuTexture = Texture(Gdx.files.internal("honsuu.png"))
    private val backToTopTexture = Texture(Gdx.files.internal("back_to_top.png"))
    private val usePointTexture = Texture(Gdx.files.internal("use_point.png"))
    private val buttonScale = 0.8f
    private val imageScale = 0.6f
    private val imoScale = 0.5f
    private val labelScale = 0.6f
    private val labelImageScale = 1.0f
    private val buttonWidth = upgradeButtonTexture.width * buttonScale
    private val buttonHeight = upgradeButtonTexture.height * buttonScale
    // 変更: ボタンの座標を中央に移動
    private val moguraUpgradeButton = Rectangle(640f, 1350f, buttonWidth, buttonHeight)
    private val turuhasiValueButton = Rectangle(640f, 900f, buttonWidth, buttonHeight)
    private val turuhasiUnlockButton = Rectangle(640f, 450f, buttonWidth, buttonHeight)
    private val backButton = Rectangle(50f, 1900f - 50f, 300f, 50f)

    init {
        worldCamera.position.set(1080f / 2f, 1920f / 2f, 0f)
        worldCamera.update()
        pixelCamera.setToOrtho(false, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        pixelCamera.position.set(Gdx.graphics.width / 2f, Gdx.graphics.height / 2f, 0f)
        pixelCamera.update()
        font.data.setScale(3.8f)
        upgradeButtonTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        moguraTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        turuhasiTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        imoTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        syuukakuTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        honsuuTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        backToTopTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        usePointTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)

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

        shapeRenderer.projectionMatrix = worldCamera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.setColor(0.3608f, 0.8824f, 0.9020f, 1f)
        shapeRenderer.rect(0f, 0f, 1080f, 1920f)
        shapeRenderer.setColor(0.5451f, 0.3412f, 0.2157f, 1f)
        shapeRenderer.rect(0f, 0f, 1080f, 300f)
        shapeRenderer.end()

        game.batch.projectionMatrix = worldCamera.combined
        game.batch.begin()

        // Back to Top
        game.batch.draw(backToTopTexture, 50f, 1900f - backToTopTexture.height * labelScale, backToTopTexture.width * labelImageScale, backToTopTexture.height * labelImageScale)

        // 強化項目（Mogura）
        // 変更: X座標を100f、Y座標を250f下げる
        game.batch.draw(moguraTexture, 100f, 1350f, moguraTexture.width * imageScale, moguraTexture.height * imageScale)
        game.batch.draw(syuukakuTexture, 170f, 1300f - syuukakuTexture.height * labelScale / 2, syuukakuTexture.width * labelScale, syuukakuTexture.height * labelScale)
        game.batch.draw(usePointTexture, 640f, 1450f + buttonHeight + 50f - usePointTexture.height * labelScale, usePointTexture.width * labelImageScale, usePointTexture.height * labelImageScale)
        font.draw(game.batch, "${game.moguraCost}pt", 530f + usePointTexture.width * labelScale + 10f, 1400f + buttonHeight + 50f, 0f, Align.left, false)
        game.batch.draw(upgradeButtonTexture, moguraUpgradeButton.x, moguraUpgradeButton.y, moguraUpgradeButton.width, moguraUpgradeButton.height)

        // 強化項目（Tsuruhasi レベル強化）
        game.batch.draw(turuhasiTexture, 100f, 900f, turuhasiTexture.width * imageScale, turuhasiTexture.height * imageScale)
        game.batch.draw(syuukakuTexture, 170f, 850f - syuukakuTexture.height * labelScale / 2, syuukakuTexture.width * labelScale, syuukakuTexture.height * labelScale)
        game.batch.draw(usePointTexture, 640f, 1000f + buttonHeight + 50f - usePointTexture.height * labelScale, usePointTexture.width * labelImageScale, usePointTexture.height * labelImageScale)
        font.draw(game.batch, "${game.turuhasiValueCost}pt", 530f + usePointTexture.width * labelScale + 10f, 950f + buttonHeight + 50f, 0f, Align.left, false)
        game.batch.draw(upgradeButtonTexture, turuhasiValueButton.x, turuhasiValueButton.y, turuhasiValueButton.width, turuhasiValueButton.height)

        // 強化項目（Tsuruhasi 解放）
        game.batch.draw(turuhasiTexture, 100f, 450f, turuhasiTexture.width * imageScale, turuhasiTexture.height * imageScale)
        game.batch.draw(honsuuTexture, 170f, 400f - honsuuTexture.height * labelScale / 2, honsuuTexture.width * labelScale, honsuuTexture.height * labelScale)
        if (game.turuhasiUnlockedCount < 2) {
            game.batch.draw(usePointTexture, 640f, 550f + buttonHeight + 50f - usePointTexture.height * labelScale, usePointTexture.width * labelImageScale, usePointTexture.height * labelImageScale)
            font.draw(game.batch, "${game.turuhasiUnlockCost}pt", 530f + usePointTexture.width * labelScale + 10f, 500f + buttonHeight + 50f, 0f, Align.left, false)
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
            val touchPos = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
            viewport.unproject(touchPos)
            val touchX = touchPos.x
            val touchY = touchPos.y

            Gdx.app.log("ReinforcementScreen", "Touch: x=$touchX, y=$touchY")

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
                    game.turuhasiUnlockCost = 700
                } else if (game.turuhasiUnlockedCount == 2) {
                    game.turuhasiUnlockCost = 0
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
        syuukakuTexture.dispose()
        honsuuTexture.dispose()
        backToTopTexture.dispose()
        usePointTexture.dispose()
        shapeRenderer.dispose()
    }
}
