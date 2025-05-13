package com.matsuyo.oimohori

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.ExtendViewport
import kotlin.math.min
import kotlin.math.sin

class GameScreen(private val game: GameMain) : ScreenAdapter() {
    private val worldCamera = OrthographicCamera()
    private val viewport = ExtendViewport(1080f, 1920f, worldCamera)
    private val pixelCamera = OrthographicCamera()
    private val shapeRenderer = ShapeRenderer()
    private val mogura1Texture: Texture = Texture(Gdx.files.internal("mogura1.png"))
    private val mogura2Texture: Texture = Texture(Gdx.files.internal("mogura2.png"))
    private val turuhasiTexture: Texture = Texture(Gdx.files.internal("turuhasi.png"))
    private val tsutaTexture: Texture = Texture(Gdx.files.internal("tsuta.png"))
    private val font = BitmapFont()
    private var moguraState = MoguraState.IDLE
    private var moguraY = 580f
    private var collectedImos = 0
    private val imoScale = 0.5f
    private val moguraX = 1080f / 2f - mogura1Texture.width / 2f
    private val particles = mutableListOf<Particle>()
    private var digTimer = 0f
    private val DIG_DURATION = 0.5f
    private var totalPoints = 0 // 今回のプレイで獲得したスコア
    private val speed = 1000f
    private val MAX_IMO_DISPLAY = 100 // 表示する芋の最大数

    // ツルハシ管理用のデータクラス
    private data class TuruhasiInstance(
        var x: Float,
        var y: Float,
        var isDragging: Boolean = false,
        var isActive: Boolean = true,
        var dragStart: Vector2 = Vector2(),
        var dragEnd: Vector2 = Vector2(),
        var lastSwipeTime: Float = 0f
    )

    // 複数ツルハシのリスト
    private val turuhasiInstances = mutableListOf<TuruhasiInstance>()

    // ツルハシのスケールとサイズ
    private val turuhasiScale = 0.5f
    private val turuhasiWidth = turuhasiTexture.width * turuhasiScale
    private val turuhasiHeight = turuhasiTexture.height * turuhasiScale

    // メインツルハシの位置（中央）- 常に存在する
    private val mainTuruhasiX = 1080f / 2f - turuhasiWidth / 2f
    private val mainTuruhasiY = 1870f - turuhasiHeight

    // 左右のツルハシの位置オフセット
    private val turuhasiOffsetX = 300f

    data class ImoType(
        val name: String,
        val textureName: String,
        val points: Int,
        val minImos: Int = 0,
        val probability: Float = 1.0f
    )

    private val imoTypes = listOf(
        ImoType("通常芋", "normal_imo.png", 5),
        ImoType("シルバー芋", "silver_imo.png", 7, 30, 0.2f),
        ImoType("ゴールド芋", "gold_imo.png", 10, 40, 0.2f),
        ImoType("メラメラ芋", "fire_imo.png", 12, 50, 0.2f),
        ImoType("ヒエヒエ芋", "ice_imo.png", 12, 50, 0.2f),
        ImoType("キラキラ芋", "star_imo.png", 12, 50, 0.2f),
        ImoType("キリン芋", "giraffe_imo.png", 15, 70, 0.2f),
        ImoType("シマウマ芋", "zebra_imo.png", 12, 70, 0.2f),
        ImoType("ウシ芋", "cow_imo.png", 12, 70, 0.2f),
        ImoType("DJ芋", "dj_imo.png", 15, 80, 0.2f),
        ImoType("虹芋", "rainbow_imo.png", 15, 80, 0.2f),
        ImoType("迷彩芋", "meisai_imo.png", 15, 80, 0.2f),
        ImoType("日本芋", "japan_imo.png", 20, 100, 0.2f),
        ImoType("アメリカ芋", "usa_imo.png", 20, 100, 0.2f),
        ImoType("ドイツ芋", "Germany_imo.png", 20, 100, 0.2f),
        ImoType("ジャマイカ芋", "jamaica_imo.png", 20, 100, 0.2f),
        ImoType("ロシア芋", "russia_imo.png", 20, 100, 0.2f),
        ImoType("野球芋", "baseball_imo.png", 25, 120, 0.2f),
        ImoType("ラグビーボール", "rugbyball.png", 25, 120, 0.2f),
        ImoType("サッカー芋", "soccer_imo.png", 25, 120, 0.2f),
        ImoType("バスケ芋", "basketball_imo.png", 25, 120, 0.2f),
        ImoType("虫食い芋", "musikui_imo.png", 1, probability = 0.05f),
        ImoType("小石", "koisi.png", 1, probability = 0.05f),
        ImoType("ミミズ", "mimizu.png", 1, probability = 0.05f),
        ImoType("ジャガイモ", "poteto.png", 1, probability = 0.05f)
    )

    private val textureCache = mutableMapOf<String, Texture>()

    private data class ImoInstance(
        val position: Vector2,
        val imoType: ImoType
    )

    private val imoInstances = mutableListOf<ImoInstance>()

    enum class MoguraState {
        IDLE, DIGGING, WAITING, MOVING
    }

    private data class Particle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var lifetime: Float
    )

    init {
        worldCamera.position.set(1080f / 2f, 1920f / 2f, 0f)
        worldCamera.update()
        mogura1Texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        mogura2Texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        turuhasiTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        tsutaTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        font.data.setScale(2f)

        imoTypes.forEach { imoType ->
            try {
                val texture = Texture(Gdx.files.internal(imoType.textureName)).apply {
                    setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
                }
                textureCache[imoType.textureName] = texture
            } catch (e: Exception) {
                Gdx.app.error("GameScreen", "Failed to load ${imoType.textureName}: ${e.message}")
            }
        }

        // ツルハシの初期化
        initTuruhasi()
    }

    private fun initTuruhasi() {
        turuhasiInstances.clear()

        // 中央のツルハシ（常に表示）
        turuhasiInstances.add(TuruhasiInstance(mainTuruhasiX, mainTuruhasiY))

        // 解放されたツルハシを追加
        if (game.turuhasiUnlockedCount >= 1) {
            // 左側に1本目のツルハシを追加
            turuhasiInstances.add(TuruhasiInstance(mainTuruhasiX - turuhasiOffsetX, mainTuruhasiY))
        }

        if (game.turuhasiUnlockedCount >= 2) {
            // 右側に2本目のツルハシを追加
            turuhasiInstances.add(TuruhasiInstance(mainTuruhasiX + turuhasiOffsetX, mainTuruhasiY))
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
        shapeRenderer.rect(0f, 0f, 1080f, 650f)
        shapeRenderer.end()

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.setColor(0.5451f, 0.3412f, 0.2157f, 1f)
        particles.forEach { particle ->
            shapeRenderer.circle(particle.x, particle.y, 5f)
        }
        shapeRenderer.end()

        updateParticles(delta)

        game.batch.projectionMatrix = worldCamera.combined
        game.batch.begin()

        // モグラの状態に応じた描画
        when (moguraState) {
            MoguraState.IDLE -> {
                // ツルハシの描画
                drawTuruhasi()
            }
            MoguraState.DIGGING -> {
                // ツルハシの描画
                drawTuruhasi()
            }
            MoguraState.WAITING -> {
                game.batch.draw(mogura1Texture, moguraX, moguraY)
            }
            MoguraState.MOVING -> {
                game.batch.draw(mogura2Texture, moguraX, moguraY)
                val tsutaY = moguraY - tsutaTexture.height
                game.batch.draw(tsutaTexture, moguraX + (mogura2Texture.width - tsutaTexture.width) / 2f, tsutaY)
                imoInstances.forEach { imo ->
                    val texture = textureCache[imo.imoType.textureName] ?: textureCache["normal_imo.png"]!!
                    game.batch.draw(
                        texture,
                        imo.position.x,
                        imo.position.y,
                        texture.width * imoScale,
                        texture.height * imoScale
                    )
                }
            }
        }

        game.batch.end()
        // アニメーション速度
        if (moguraState == MoguraState.MOVING) {
            moguraY += speed * delta
            imoInstances.forEach { imo ->
                imo.position.y += speed * delta
            }
            if (collectedImos > 0) {
                val lowestImoY = imoInstances.last().position.y
                if (lowestImoY > 1920f) {
                    moguraState = MoguraState.IDLE
                    moguraY = 580f
                    imoInstances.clear()
                    initTuruhasi() // ツルハシを再初期化
                    game.setScreen(ResultScreen(game, totalPoints, collectedImos, imoCounts)) // 収穫数と内訳を渡す
                }
            }
        }
        if (moguraState == MoguraState.DIGGING) {
            digTimer += delta
            if (digTimer >= DIG_DURATION) {
                moguraState = MoguraState.WAITING
                digTimer = 0f
            }
        }

        handleInput(delta)
    }

    private fun drawTuruhasi() {
        // アクティブなツルハシだけを描画
        turuhasiInstances.filter { it.isActive }.forEach { turuhasi ->
            game.batch.draw(turuhasiTexture, turuhasi.x, turuhasi.y, turuhasiWidth, turuhasiHeight)
        }
    }

    private val imoCounts = mutableMapOf<ImoType, Int>() // 芋ごとの収穫数

    private fun handleInput(delta: Float) {
        if (Gdx.input.isTouched) {
            // タッチ座標をワールド座標に変換
            val touchPos = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
            viewport.unproject(touchPos)
            val touchX = touchPos.x
            val touchY = touchPos.y

            Gdx.app.log("GameScreen", "Raw touch: x=${Gdx.input.x}, y=${Gdx.input.y}, World touch: x=$touchX, y=$touchY")

            if (moguraState == MoguraState.IDLE) {
                // 各ツルハシのドラッグ処理
                turuhasiInstances.filter { it.isActive }.forEach { turuhasi ->
                    // ツルハシの当たり判定（スケール後のサイズを使用）
                    val turuhasiBounds = Rectangle(turuhasi.x, turuhasi.y, turuhasiWidth, turuhasiHeight)
                    Gdx.app.log("GameScreen", "Turuhasi bounds: x=${turuhasiBounds.x}, y=${turuhasiBounds.y}, width=${turuhasiBounds.width}, height=${turuhasiBounds.height}")

                    if (!turuhasi.isDragging && turuhasiBounds.contains(touchX, touchY)) {
                        Gdx.app.log("GameScreen", "Turuhasi touched at x=${turuhasi.x}, y=${turuhasi.y}")
                        turuhasi.isDragging = true
                        turuhasi.dragStart.set(touchX, touchY)
                        turuhasi.lastSwipeTime = 0f
                    } else if (!turuhasiBounds.contains(touchX, touchY)) {
                        Gdx.app.log("GameScreen", "Touch missed turuhasi at x=${turuhasi.x}, y=${turuhasi.y}")
                    }

                    if (turuhasi.isDragging) {
                        turuhasi.dragEnd.set(touchX, touchY)
                        turuhasi.lastSwipeTime += delta
                        turuhasi.y = touchY - turuhasiHeight / 2f
                        turuhasi.x = touchX - turuhasiWidth / 2f
                        Gdx.app.log("GameScreen", "Turuhasi dragging to x=${turuhasi.x}, y=${turuhasi.y}")

                        // 下にスワイプして地面に到達したら
                        if (turuhasi.dragEnd.y < turuhasi.dragStart.y && turuhasi.y <= 650f) {
                            val swipeSpeed = turuhasi.dragStart.dst(turuhasi.dragEnd) / turuhasi.lastSwipeTime
                            val particleCount = when {
                                swipeSpeed > 2000f -> 50
                                swipeSpeed > 1500f -> 30
                                swipeSpeed > 1000f -> 20
                                else -> 10
                            }
                            spawnParticles(particleCount, turuhasi.x + turuhasiWidth / 2f, 650f, swipeSpeed)
                            turuhasi.isActive = false
                            turuhasi.isDragging = false
                            Gdx.app.log("GameScreen", "Turuhasi swiped at x=${turuhasi.x}, y=${turuhasi.y}")

                            // すべてのツルハシがスワイプされたかチェック
                            if (turuhasiInstances.none { it.isActive }) {
                                Gdx.app.log("GameScreen", "All turuhasis swiped, transitioning to DIGGING")
                                moguraState = MoguraState.DIGGING
                            }
                        }
                    }
                }
            } else if (moguraState == MoguraState.WAITING) {
                // モグラのスワイプ処理
                val moguraBounds = Rectangle(moguraX, moguraY, mogura1Texture.width.toFloat(), mogura1Texture.height.toFloat())

                if (moguraBounds.contains(touchX, touchY)) {
                    val swipeDir = Vector2(touchX, touchY).sub(Vector2(moguraX, moguraY)).nor()

                    // 上向きのスワイプ
                    if (swipeDir.y > 0.5f) {
                        val swipeSpeed = 1500f // 固定値または適切な計算方法を使用
                        val bonus = if (swipeSpeed > 1000f) 2 else 1
                        collectedImos = game.moguraHarvest * game.turuhasiValue * bonus

                        imoInstances.clear()
                        totalPoints = 0 // 今回のプレイのスコアをリセット
                        imoCounts.clear() // 内訳をリセット
                        val tsutaY = moguraY - tsutaTexture.height
                        val tsutaCenterX = moguraX + (mogura2Texture.width - tsutaTexture.width) / 2f + tsutaTexture.width / 2f

                        // 表示する芋を最大100個に制限
                        val displayImos = min(collectedImos, MAX_IMO_DISPLAY)
                        Gdx.app.log("GameScreen", "Total imos: $collectedImos, Displaying: $displayImos")

                        // 表示する芋の生成と内訳記録
                        for (i in 0 until displayImos) {
                            val selectedImo = selectImoType(collectedImos)
                            totalPoints += selectedImo.points
                            imoCounts[selectedImo] = imoCounts.getOrDefault(selectedImo, 0) + 1
                            val imoY = tsutaY - (i + 1) * (textureCache[selectedImo.textureName]?.height?.times(imoScale)?.times(0.1f) ?: 50f)
                            val spreadFactor = i * 2f
                            val angle = 0.5f
                            val offsetX = spreadFactor * sin(i * angle)
                            val imoX = tsutaCenterX - (textureCache[selectedImo.textureName]?.width?.times(imoScale)?.div(2f) ?: 50f) + offsetX
                            imoInstances.add(ImoInstance(Vector2(imoX, imoY), selectedImo))
                        }

                        // 表示しない芋のスコアと内訳を計算
                        for (i in displayImos until collectedImos) {
                            val selectedImo = selectImoType(collectedImos)
                            totalPoints += selectedImo.points
                            imoCounts[selectedImo] = imoCounts.getOrDefault(selectedImo, 0) + 1
                        }

                        game.score += totalPoints
                        moguraState = MoguraState.MOVING
                    }
                }
            }
        } else {
            // タッチが終了したらフラグをリセット
            turuhasiInstances.forEach {
                if (it.isDragging && it.isActive) {
                    // スワイプが中断された場合、元の位置に戻す
                    it.isDragging = false
                    when (turuhasiInstances.indexOf(it)) {
                        0 -> it.x = mainTuruhasiX
                        1 -> it.x = mainTuruhasiX - turuhasiOffsetX
                        2 -> it.x = mainTuruhasiX + turuhasiOffsetX
                    }
                    it.y = mainTuruhasiY
                }
            }
        }
    }

    private fun selectImoType(collectedImos: Int): ImoType {
        val alwaysAvailable = imoTypes.filter { it.probability == 0.05f }
        if (MathUtils.random() < 0.05f) {
            return alwaysAvailable.random()
        }

        val availableImos = imoTypes.filter {
            it.minImos <= collectedImos && it.probability > 0.05f
        }

        if (availableImos.isNotEmpty() && MathUtils.random() < 0.2f) {
            return availableImos.random()
        }

        return imoTypes[0]
    }

    private fun spawnParticles(count: Int, x: Float, y: Float, swipeSpeed: Float) {
        val speedFactor = swipeSpeed / 1000f
        for (i in 0 until count) {
            val vx = MathUtils.random(-150f * speedFactor, 150f * speedFactor)
            val vy = MathUtils.random(200f * speedFactor, 400f * speedFactor)
            particles.add(Particle(x, y, vx, vy, 1.5f))
        }
    }

    private fun updateParticles(delta: Float) {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val particle = iterator.next()
            particle.x += particle.vx * delta
            particle.y += particle.vy * delta
            particle.vy -= 300f * delta
            particle.lifetime -= delta
            if (particle.lifetime <= 0 || particle.y < 650f) {
                iterator.remove()
            }
        }
    }

    override fun dispose() {
        mogura1Texture.dispose()
        mogura2Texture.dispose()
        turuhasiTexture.dispose()
        tsutaTexture.dispose()
        textureCache.values.forEach { it.dispose() }
        shapeRenderer.dispose()
        font.dispose()
    }
}
