package com.matsuyo.oimohori

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class GameMain : Game() {
    lateinit var batch: SpriteBatch
    var score = 5000
    var moguraHarvest = 1
    var turuhasiLevel = 3
    var turuhasiValue = 3
    var moguraCost = 50
    var turuhasiValueCost = 50
    var turuhasiUnlockedCount = 0
    var turuhasiUnlockCost = 350

    // オーディオリソース
    lateinit var bgm: Music
    lateinit var resultSound: Sound
    lateinit var tsuruhasiSound: Sound
    lateinit var syuukakujiSound: Sound
    lateinit var pushSound: Sound

    override fun create() {
        batch = SpriteBatch()
        try {
            bgm = Gdx.audio.newMusic(Gdx.files.internal("bgm.mp3"))
            bgm.isLooping = true
            bgm.volume = 0.5f
            bgm.play()
        } catch (e: Exception) {
            Gdx.app.error("GameMain", "Failed to load bgm.mp3: ${e.message}")
        }

        try {
            resultSound = Gdx.audio.newSound(Gdx.files.internal("result.mp3"))
            tsuruhasiSound = Gdx.audio.newSound(Gdx.files.internal("tsuruhasi.mp3"))
            syuukakujiSound = Gdx.audio.newSound(Gdx.files.internal("syuukakuji.mp3"))
            pushSound = Gdx.audio.newSound(Gdx.files.internal("push.mp3"))
        } catch (e: Exception) {
            Gdx.app.error("GameMain", "Failed to load sound effects: ${e.message}")
        }

        setScreen(TitleScreen(this))
    }

    fun updateTuruhasiValue() {
        turuhasiValue = turuhasiLevel * (turuhasiUnlockedCount + 1)
    }

    // 初期化チェック用のメソッド
    fun isTsuruhasiSoundInitialized(): Boolean {
        return ::tsuruhasiSound.isInitialized
    }

    fun isSyuukakujiSoundInitialized(): Boolean {
        return ::syuukakujiSound.isInitialized
    }

    fun isResultSoundInitialized(): Boolean {
        return ::resultSound.isInitialized
    }

    fun isPushSoundInitialized(): Boolean {
        return ::pushSound.isInitialized
    }

    // サウンド停止用のメソッド
    fun stopSyuukakujiSound() {
        if (::syuukakujiSound.isInitialized) {
            syuukakujiSound.stop()
        }
    }

    override fun dispose() {
        batch.dispose()
        if (::bgm.isInitialized) bgm.dispose()
        if (::resultSound.isInitialized) resultSound.dispose()
        if (::tsuruhasiSound.isInitialized) tsuruhasiSound.dispose()
        if (::syuukakujiSound.isInitialized) syuukakujiSound.dispose()
        if (::pushSound.isInitialized) pushSound.dispose()
    }
}
