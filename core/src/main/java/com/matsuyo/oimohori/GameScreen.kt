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
    private var totalPoints = 0
    private val speed = 1000f
    private val MAX_IMO_DISPLAY = 100

    private data class TuruhasiInstance(
        var x: Float,
        var y: Float,
        var isDragging: Boolean = false,
        var isActive: Boolean = true,
        var dragStart: Vector2 = Vector2(),
        var dragEnd: Vector2 = Vector2(),
        var lastSwipeTime: Float = 0f
    )

    private val turuhasiInstances = mutableListOf<TuruhasiInstance>()

    private val turuhasiScale = 0.5f
    private val turuhasiWidth = turuhasiTexture.width * turuhasiScale
    private val turuhasiHeight = turuhasiTexture.height * turuhasiScale

    private val mainTuruhasiX = 1080f / 2f - turuhasiWidth / 2f
    private val mainTuruhasiY = 1870f - turuhasiHeight

    private val turuhasiOffsetX = 300f

    private val textureCache = mutableMapOf<String, Texture>()

    private data class ImoInstance(
        val position: Vector2,
        val imoType: ImoType
    )

    private val imoCounts = mutableMapOf<ImoType, Int>()

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

        ImoConfig.imoTypes.forEach { imoType ->
            try {
                val texture = Texture(Gdx.files.internal(imoType.textureName)).apply {
                    setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
                }
                textureCache[imoType.textureName] = texture
            } catch (e: Exception) {
                Gdx.app.error("GameScreen", "Failed to load ${imoType.textureName}: ${e.message}")
            }
        }

        initTuruhasi()
    }

    private fun initTuruhasi() {
        turuhasiInstances.clear()
        turuhasiInstances.add(TuruhasiInstance(mainTuruhasiX, mainTuruhasiY))
        if (game.turuhasiUnlockedCount >= 1) {
            turuhasiInstances.add(TuruhasiInstance(mainTuruhasiX - turuhasiOffsetX, mainTuruhasiY))
        }
        if (game.turuhasiUnlockedCount >= 2) {
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
            MoguraState.IDLE -> {
                drawTuruhasi()
            }
            MoguraState.DIGGING -> {
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
                    initTuruhasi()
                    // ResultScreen遷移前にsyuukakujiSoundを停止
                    game.stopSyuukakujiSound()
                    game.setScreen(ResultScreen(game, totalPoints, collectedImos, imoCounts))
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
        turuhasiInstances.filter { it.isActive }.forEach { turuhasi ->
            game.batch.draw(turuhasiTexture, turuhasi.x, turuhasi.y, turuhasiWidth, turuhasiHeight)
        }
    }

    private fun handleInput(delta: Float) {
        if (Gdx.input.isTouched) {
            val touchPos = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
            viewport.unproject(touchPos)
            val touchX = touchPos.x
            val touchY = touchPos.y

            Gdx.app.log("GameScreen", "Raw touch: x=${Gdx.input.x}, y=${Gdx.input.y}, World touch: x=$touchX, y=$touchY")

            if (moguraState == MoguraState.IDLE) {
                turuhasiInstances.filter { it.isActive }.forEach { turuhasi ->
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
                            // ツルハシの効果音を再生
                            if (game.isTsuruhasiSoundInitialized()) {
                                game.tsuruhasiSound.play()
                            }

                            if (turuhasiInstances.none { it.isActive }) {
                                Gdx.app.log("GameScreen", "All turuhasis swiped, transitioning to DIGGING")
                                moguraState = MoguraState.DIGGING
                            }
                        }
                    }
                }
            } else if (moguraState == MoguraState.WAITING) {
                val moguraBounds = Rectangle(moguraX, moguraY, mogura1Texture.width.toFloat(), mogura1Texture.height.toFloat())

                if (moguraBounds.contains(touchX, touchY)) {
                    val swipeDir = Vector2(touchX, touchY).sub(Vector2(moguraX, moguraY)).nor()

                    if (swipeDir.y > 0.5f) {
                        val swipeSpeed = 1500f
                        val bonus = if (swipeSpeed > 1000f) 2 else 1
                        collectedImos = minOf(game.moguraHarvest * game.turuhasiValue * bonus, 3000)

                        // 収穫時の効果音を再生
                        if (game.isSyuukakujiSoundInitialized()) {
                            game.syuukakujiSound.play()
                        }

                        imoInstances.clear()
                        totalPoints = 0
                        imoCounts.clear()
                        val tsutaY = moguraY - tsutaTexture.height
                        val tsutaCenterX = moguraX + (mogura2Texture.width - tsutaTexture.width) / 2f + tsutaTexture.width / 2f

                        val displayImos = min(collectedImos, MAX_IMO_DISPLAY)
                        Gdx.app.log("GameScreen", "Total imos: $collectedImos, Displaying: $displayImos")

                        for (i in 0 until displayImos) {
                            val selectedImo = selectImoType(collectedImos)
                            totalPoints += selectedImo.points
                            imoCounts[selectedImo] = imoCounts.getOrDefault(selectedImo, 0) + 1
                            game.unlockImo(selectedImo) // 芋をアンロックして保存
                            val imoY = tsutaY - (i + 1) * (textureCache[selectedImo.textureName]?.height?.times(imoScale)?.times(0.1f) ?: 50f)
                            val spreadFactor = i * 2f
                            val angle = 0.5f
                            val offsetX = spreadFactor * sin(i * angle)
                            val imoX = tsutaCenterX - (textureCache[selectedImo.textureName]?.width?.times(imoScale)?.div(2f) ?: 50f) + offsetX
                            imoInstances.add(ImoInstance(Vector2(imoX, imoY), selectedImo))
                        }

                        for (i in displayImos until collectedImos) {
                            val selectedImo = selectImoType(collectedImos)
                            totalPoints += selectedImo.points
                            imoCounts[selectedImo] = imoCounts.getOrDefault(selectedImo, 0) + 1
                            game.unlockImo(selectedImo) // 芋をアンロックして保存
                        }

                        game.updateScore(game.score + totalPoints) // スコアを更新して保存
                        moguraState = MoguraState.MOVING
                    }
                }
            }
        } else {
            turuhasiInstances.forEach {
                if (it.isDragging && it.isActive) {
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
        val alwaysAvailable = ImoConfig.imoTypes.filter { it.probability == 0.05f }
        if (MathUtils.random() < 0.05f) {
            return alwaysAvailable.random()
        }

        val availableImos = ImoConfig.imoTypes.filter {
            it.minImos <= collectedImos && it.probability > 0.05f
        }

        if (availableImos.isNotEmpty() && MathUtils.random() < 0.2f) {
            return availableImos.random()
        }

        return ImoConfig.imoTypes[0]
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
