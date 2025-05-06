package com.matsuyo.oimohori

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.viewport.ExtendViewport

/** [com.badlogic.gdx.ApplicationListener] implementation shared by all platforms. */
class GameMain : ApplicationAdapter() {
    private lateinit var batch: SpriteBatch
    private lateinit var image: Texture
    private lateinit var camera: OrthographicCamera
    private lateinit var viewport: ExtendViewport

    override fun create() {
        batch = SpriteBatch()
        image = Texture("libgdx.png")

        // カメラを作成
        camera = OrthographicCamera()
        // ワールド座標のサイズを設定
        viewport = ExtendViewport(1080f, 1920f, camera)
        // カメラの位置をワールドの中心に設定
        camera.position.set(viewport.worldWidth / 2, viewport.worldHeight / 2, 0f)
        // ビューポートを適用
        viewport.apply()
    }

    override fun render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f)

        // カメラを更新
        camera.update()
        // SpriteBatchにカメラのビューポートを設定
        batch.projectionMatrix = camera.combined

        batch.begin()
        // 描画位置はワールド座標に基づきます
        batch.draw(image, 140f, 210f)
        batch.end()
    }

    override fun resize(width: Int, height: Int) {
        // 画面サイズが変更されたときにビューポートを更新
        viewport.update(width, height, true)
        // カメラの位置を再調整（必要であれば）
        camera.position.set(viewport.worldWidth / 2, viewport.worldHeight / 2, 0f)
    }

    override fun dispose() {
        batch.dispose()
        image.dispose()
    }
}
