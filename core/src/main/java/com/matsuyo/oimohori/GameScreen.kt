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
    private var imoTexture: Texture? = null
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
    private val imoPositions = mutableListOf<Vector2>()
    private val moguraX = 1080f / 2f - mogura1Texture.width / 2f
    private val particles = mutableListOf<Particle>()
    private var showTuruhasi = true
    private var digTimer = 0f
    private val DIG_DURATION = 0.5f

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

        try {
            imoTexture = Texture(Gdx.files.internal("normal_imo.png")).apply {
                setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            }
        } catch (e: Exception) {
            Gdx.app.error("GameScreen", "Failed to load normal_imo.png: ${e.message}")
            imoTexture = null
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
                if (imoTexture != null) {
                    for (i in 0 until collectedImos) {
                        val pos = imoPositions[i]
                        game.batch.draw(
                            imoTexture,
                            pos.x,
                            pos.y,
                            imoTexture!!.width * imoScale,
                            imoTexture!!.height * imoScale
                        )
                    }
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
            for (i in 0 until collectedImos) {
                imoPositions[i].y += 500f * delta
            }
            if (collectedImos > 0) {
                val lowestImoY = imoPositions.last().y
                if (lowestImoY > 1920f) {
                    moguraState = MoguraState.IDLE
                    moguraY = 580f
                    imoPositions.clear()
                    showTuruhasi = true
                    game.setScreen(ResultScreen(game)) // ResultScreen に遷移
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
                        score += collectedImos
                        moguraState = MoguraState.MOVING
                        showTuruhasi = false

                        imoPositions.clear()
                        val tsutaY = moguraY - tsutaTexture.height
                        val tsutaCenterX = moguraX + (mogura2Texture.width - tsutaTexture.width) / 2f + tsutaTexture.width / 2f
                        for (i in 0 until collectedImos) {
                            val imoY = tsutaY - (i + 1) * (imoTexture!!.height * imoScale * 0.3f)
                            val spreadFactor = (i + 1) * 20f
                            val offsetX = MathUtils.random(-spreadFactor, spreadFactor)
                            val imoX = tsutaCenterX - (imoTexture!!.width * imoScale) / 2f + offsetX
                            imoPositions.add(Vector2(imoX, imoY))
                        }
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
        imoTexture?.dispose()
        shapeRenderer.dispose()
        font.dispose()
    }
}
