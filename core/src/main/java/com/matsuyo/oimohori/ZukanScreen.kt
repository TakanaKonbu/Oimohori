package com.matsuyo.oimohori

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.Align

class ZukanScreen(private val game: GameMain) : ScreenAdapter() {
    private val worldCamera = OrthographicCamera()
    private val viewport = ExtendViewport(1080f, 1920f, worldCamera)
    private val stage = Stage(viewport, game.batch)
    private val backToTopTexture = Texture(Gdx.files.internal("back_to_top.png"))
    private val completedTexture = Texture(Gdx.files.internal("completed.png"))
    private val font = BitmapFont(Gdx.files.internal("font.fnt"))
    private val unknownImoTexture = Texture(Gdx.files.internal("unkown_imo_text.png"))
    private val textureCache = mutableMapOf<String, Texture>()
    private val buttonScale = 1.0f
    private val backButton = Rectangle(
        50f,
        1870f - backToTopTexture.height * buttonScale,
        backToTopTexture.width * buttonScale,
        backToTopTexture.height * buttonScale
    )

    private val imoTypes = listOf(
        GameScreen.ImoType("通常芋", "normal_imo.png", 5),
        GameScreen.ImoType("シルバー芋", "silver_imo.png", 7, 100, 0.2f),
        GameScreen.ImoType("ゴールド芋", "gold_imo.png", 10, 100, 0.2f),
        GameScreen.ImoType("メラメラ芋", "fire_imo.png", 12, 250, 0.2f),
        GameScreen.ImoType("ヒエヒエ芋", "ice_imo.png", 12, 250, 0.2f),
        GameScreen.ImoType("キラキラ芋", "star_imo.png", 12, 250, 0.2f),
        GameScreen.ImoType("キリン芋", "giraffe_imo.png", 15, 370, 0.2f),
        GameScreen.ImoType("シマウマ芋", "zebra_imo.png", 15, 370, 0.2f),
        GameScreen.ImoType("ウシ芋", "cow_imo.png", 15, 370, 0.2f),
        GameScreen.ImoType("DJ芋", "dj_imo.png", 17, 480, 0.2f),
        GameScreen.ImoType("虹芋", "rainbow_imo.png", 17, 480, 0.2f),
        GameScreen.ImoType("迷彩芋", "meisai_imo.png", 17, 480, 0.2f),
        GameScreen.ImoType("日本芋", "japan_imo.png", 20, 600, 0.2f),
        GameScreen.ImoType("アメリカ芋", "usa_imo.png", 20, 600, 0.2f),
        GameScreen.ImoType("ドイツ芋", "germany_imo.png", 20, 600, 0.2f),
        GameScreen.ImoType("ジャマイカ芋", "jamaica_imo.png", 20, 600, 0.2f),
        GameScreen.ImoType("ロシア芋", "russia_imo.png", 20, 600, 0.2f),
        GameScreen.ImoType("野球芋", "baseball_imo.png", 25, 720, 0.2f),
        GameScreen.ImoType("ラグビーボール", "rugbyball.png", 25, 720, 0.2f),
        GameScreen.ImoType("サッカー芋", "soccer_imo.png", 25, 720, 0.2f),
        GameScreen.ImoType("バスケ芋", "basketball_imo.png", 25, 720, 0.2f),
        GameScreen.ImoType("虫食い芋", "musikui_imo.png", 1, probability = 0.05f),
        GameScreen.ImoType("小石", "koisi.png", 1, probability = 0.05f),
        GameScreen.ImoType("ミミズ", "mimizu.png", 1, probability = 0.05f),
        GameScreen.ImoType("ジャガイモ", "poteto.png", 1, probability = 0.05f)
    )

    init {
        worldCamera.position.set(1080f / 2f, 1920f / 2f, 0f)
        worldCamera.update()
        backToTopTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        completedTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        unknownImoTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        font.data.setScale(1.0f) // フォントサイズ（必要に応じて調整）
        font.region.texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)

        // 芋のテキストテクスチャをキャッシュ
        imoTypes.forEach { imoType ->
            try {
                val textTextureName = imoType.textureName.replace(".png", "_text.png")
                val texture = Texture(Gdx.files.internal(textTextureName)).apply {
                    setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
                }
                textureCache[textTextureName] = texture
            } catch (e: Exception) {
                Gdx.app.error("ZukanScreen", "Failed to load ${imoType.textureName.replace(".png", "_text.png")}: ${e.message}")
            }
        }

        // ボタン領域のデバッグログ
        Gdx.app.log("ZukanScreen", "Back Button: x=${backButton.x}, y=${backButton.y}, width=${backButton.width}, height=${backButton.height}")

        // 入力処理をStageに設定
        Gdx.input.inputProcessor = stage

        // スクロール可能なテーブルを作成
        val table = Table()
        table.align(Align.center)

        // 25個の芋をimoTypesの順番で表示
        imoTypes.forEach { imoType ->
            val textTextureName = if (game.unlockedImos.contains(imoType)) {
                imoType.textureName.replace(".png", "_text.png")
            } else {
                "unkown_imo_text.png"
            }
            val texture = textureCache[textTextureName] ?: unknownImoTexture
            val image = Image(texture)
            table.add(image).width(texture.width.toFloat() * 2.0f).height(texture.height.toFloat() * 2.0f).pad(20f)
            table.row()
        }

        // ScrollPaneを作成
        val scrollPane = ScrollPane(table)
        scrollPane.setSize(1080f, 1820f - backToTopTexture.height * buttonScale - 50f)
        scrollPane.setPosition(0f, 0f)
        stage.addActor(scrollPane)

        // 戻るボタンはStageではなく従来通り描画
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
        worldCamera.position.set(1080f / 2f, 1920f / 2f, 0f)
        worldCamera.update()
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.3608f, 0.8824f, 0.9020f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        viewport.apply()

        // Stageを描画
        stage.act(delta)
        stage.draw()

        // 戻るボタン、completed.png、収集率を描写
        game.batch.projectionMatrix = worldCamera.combined
        game.batch.begin()
        // 戻るボタン
        game.batch.draw(
            backToTopTexture,
            backButton.x,
            backButton.y,
            backButton.width,
            backButton.height
        )
        // completed.png（戻るボタンの右、20fの間隔、中央軸を揃える）
        val centerY = backButton.y + backButton.height / 2 // 戻るボタンの中央Y
        val completedX = backButton.x + backButton.width + 20f
        val completedY = centerY - (completedTexture.height * buttonScale / 2)
        game.batch.draw(
            completedTexture,
            completedX,
            completedY,
            completedTexture.width * buttonScale,
            completedTexture.height * buttonScale
        )
        // 収集率（completed.pngの右、20fの間隔、中央軸を揃える）
        val completionRate = (game.unlockedImos.size.toFloat() / imoTypes.size * 100).toInt()
        font.draw(
            game.batch,
            "$completionRate%",
            completedX + completedTexture.width * buttonScale + 20f,
            centerY + font.capHeight / 2, // フォントのベースラインを中央に
            0f,
            Align.left,
            false
        )
        game.batch.end()

        handleInput()
    }

    private fun handleInput() {
        if (Gdx.input.justTouched()) {
            val touchPos = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
            viewport.unproject(touchPos)
            val touchX = touchPos.x
            val touchY = touchPos.y

            Gdx.app.log("ZukanScreen", "Touch: x=$touchX, y=$touchY")

            if (backButton.contains(touchX, touchY)) {
                Gdx.app.log("ZukanScreen", "Back to Top button tapped")
                if (game.isPushSoundInitialized()) {
                    game.pushSound.play()
                }
                game.setScreen(TitleScreen(game))
            }
        }
    }

    override fun dispose() {
        backToTopTexture.dispose()
        completedTexture.dispose()
        unknownImoTexture.dispose()
        font.dispose()
        textureCache.values.forEach { it.dispose() }
        stage.dispose()
    }
}
