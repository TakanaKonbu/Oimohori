package com.matsuyo.oimohori

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ExtendViewport

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
    private var score = 0
    private var isTuruhasiDragging = false
    private var turuhasiDragStart = Vector2()
    private var turuhasiDragEnd = Vector2()
    private var turuhasiX = 1080f / 2f - turuhasiTexture.width * 0.5f / 2f
    private var turuhasiY = 1870f - turuhasiTexture.height * 0.5f
    private var isMoguraDragging = false
    private var moguraDragStart = Vector2()
    private var moguraDragEnd = Vector2()
    private var lastSwipeTime = 0f
    private var swipeSpeed = 0f
    private var moguraState = MoguraState.IDLE
    private var moguraY = 580f
    private var collectedImos = 0
    private val imoScale = 0.5f
    private val moguraX = 1080f / 2f - mogura1Texture.width / 2f
    private val particles = mutableListOf<Particle>()
    private var showTuruhasi = true
    private var digTimer = 0f
    private val DIG_DURATION = 0.5f

    // 芋の種類を定義
    private data class ImoType(
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

    // テクスチャのキャッシュ
    private val textureCache = mutableMapOf<String, Texture>()

    // 芋の位置と種類を保持
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

        // テクスチャキャッシュの初期化
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
        when (moguraState) {
            MoguraState.IDLE, MoguraState.DIGGING -> {}
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
        if (showTuruhasi) {
            val turuhasiScale = 0.5f
            val turuhasiWidth = turuhasiTexture.width * turuhasiScale
            val turuhasiHeight = turuhasiTexture.height * turuhasiScale
            game.batch.draw(turuhasiTexture, turuhasiX, turuhasiY, turuhasiWidth, turuhasiHeight)
        }
        font.draw(game.batch, "Score: $score", 50f, 1900f, 0f, Align.left, false)
        game.batch.end()

        if (moguraState == MoguraState.MOVING) {
            moguraY += 500f * delta
            imoInstances.forEach { imo ->
                imo.position.y += 500f * delta
            }
            if (collectedImos > 0) {
                val lowestImoY = imoInstances.last().position.y
                if (lowestImoY > 1920f) {
                    moguraState = MoguraState.IDLE
                    moguraY = 580f
                    imoInstances.clear()
                    showTuruhasi = true
                    game.setScreen(ResultScreen(game))
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

    private fun handleInput(delta: Float) {
        if (Gdx.input.isTouched) {
            val touchX = Gdx.input.x.toFloat() * (1080f / Gdx.graphics.width)
            val touchY = (Gdx.graphics.height - Gdx.input.y) * (1920f / Gdx.graphics.height)

            if (moguraState == MoguraState.IDLE) {
                if (!isTuruhasiDragging) {
                    isTuruhasiDragging = true
                    turuhasiDragStart.set(touchX, touchY)
                    lastSwipeTime = 0f
                } else {
                    turuhasiDragEnd.set(touchX, touchY)
                    lastSwipeTime += delta
                    turuhasiY = touchY - (turuhasiTexture.height * 0.5f) / 2f
                    turuhasiX = touchX - (turuhasiTexture.width * 0.5f) / 2f

                    if (turuhasiDragEnd.y < turuhasiDragStart.y && turuhasiY <= 650f) {
                        swipeSpeed = turuhasiDragStart.dst(turuhasiDragEnd) / lastSwipeTime
                        val particleCount = when {
                            swipeSpeed > 2000f -> 50
                            swipeSpeed > 1500f -> 30
                            swipeSpeed > 1000f -> 20
                            else -> 10
                        }
                        spawnParticles(particleCount, turuhasiX + (turuhasiTexture.width * 0.5f) / 2f, 650f, swipeSpeed)
                        moguraState = MoguraState.DIGGING
                        resetTuruhasi()
                    }
                }
            }
            else if (moguraState == MoguraState.WAITING) {
                if (!isMoguraDragging) {
                    isMoguraDragging = true
                    moguraDragStart.set(touchX, touchY)
                    lastSwipeTime = 0f
                } else {
                    moguraDragEnd.set(touchX, touchY)
                    lastSwipeTime += delta
                    if (moguraDragEnd.y > moguraDragStart.y) {
                        swipeSpeed = moguraDragStart.dst(moguraDragEnd) / lastSwipeTime
                        val bonus = if (swipeSpeed > 1000f) 2 else 1
                        collectedImos = 50 * bonus

                        imoInstances.clear()
                        var totalPoints = 0
                        val tsutaY = moguraY - tsutaTexture.height
                        val tsutaCenterX = moguraX + (mogura2Texture.width - tsutaTexture.width) / 2f + tsutaTexture.width / 2f
                        for (i in 0 until collectedImos) {
                            val selectedImo = selectImoType(collectedImos)
                            totalPoints += selectedImo.points
                            val imoY = tsutaY - (i + 1) * (textureCache[selectedImo.textureName]?.height?.times(imoScale)?.times(0.3f) ?: 50f)
                            val spreadFactor = (i + 1) * 20f
                            val offsetX = MathUtils.random(-spreadFactor, spreadFactor)
                            val imoX = tsutaCenterX - (textureCache[selectedImo.textureName]?.width?.times(imoScale)?.div(2f) ?: 50f) + offsetX
                            imoInstances.add(ImoInstance(Vector2(imoX, imoY), selectedImo))
                        }
                        score += totalPoints
                        moguraState = MoguraState.MOVING
                        showTuruhasi = false
                    }
                }
            }
        } else {
            isTuruhasiDragging = false
            isMoguraDragging = false
            if (moguraState == MoguraState.IDLE) {
                resetTuruhasi()
            }
        }
    }

    private fun selectImoType(collectedImos: Int): ImoType {
        // 常に5%の確率で出現する芋をチェック
        val alwaysAvailable = imoTypes.filter { it.probability == 0.05f }
        if (MathUtils.random() < 0.05f) {
            return alwaysAvailable.random()
        }

        // 収穫数に応じて可能な芋をフィルタリング
        val availableImos = imoTypes.filter {
            it.minImos <= collectedImos && it.probability > 0.05f
        }

        // 20%の確率で特殊芋を選択
        if (availableImos.isNotEmpty() && MathUtils.random() < 0.2f) {
            return availableImos.random()
        }

        // デフォルトは通常芋
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

    private fun resetTuruhasi() {
        turuhasiX = 1080f / 2f - turuhasiTexture.width * 0.5f / 2f
        turuhasiY = 1870f - turuhasiTexture.height * 0.5f
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
