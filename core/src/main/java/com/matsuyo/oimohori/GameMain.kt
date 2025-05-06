package com.matsuyo.oimohori

import com.badlogic.gdx.Game
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class GameMain : Game() {
    lateinit var batch: SpriteBatch

    override fun create() {
        batch = SpriteBatch()
        setScreen(TitleScreen(this))
    }

    override fun dispose() {
        batch.dispose()
        super.dispose()
    }
}
