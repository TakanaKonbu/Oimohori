package com.matsuyo.oimohori

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ExtendViewport

class ResultScreen(private val game: GameMain) : ScreenAdapter() {
    private val worldCamera = OrthographicCamera()
    private val viewport = ExtendViewport(1080f, 1920f, worldCamera)
    private val font = BitmapFont()

    init {
        worldCamera.position.set(1080f / 2f, 1920f / 2f, 0f)
        worldCamera.update()
        font.data.setScale(3f) // フォントサイズを大きく
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
        worldCamera.position.set(1080f / 2f, 1920f / 2f, 0f)
        worldCamera.update()
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.3608f, 0.8824f, 0.9020f, 1f) // GameScreen の空の色に合わせる
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        viewport.apply()

        game.batch.projectionMatrix = worldCamera.combined
        game.batch.begin()
        font.draw(game.batch, "Result", 540f, 960f, 0f, Align.center, false)
        game.batch.end()
    }

    override fun dispose() {
        font.dispose()
    }
}
