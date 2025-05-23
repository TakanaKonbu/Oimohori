package com.matsuyo.oimohori

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class GameMain : Game() {
    lateinit var batch: SpriteBatch
    var score = 0
    var moguraHarvest = 1
    var turuhasiLevel = 3
    var turuhasiValue = 3
    var moguraCost = 50
    var turuhasiValueCost = 50
    var turuhasiUnlockedCount = 0
    var turuhasiUnlockCost = 350
    val unlockedImos = mutableSetOf<ImoType>()
    var adHandler: AdHandler? = null
    var currentPlayPoints = 0
    var pointsAdded = false

    lateinit var bgm: Music
    lateinit var resultSound: Sound
    lateinit var tsuruhasiSound: Sound
    lateinit var syuukakujiSound: Sound
    lateinit var pushSound: Sound

    private val prefs: Preferences by lazy { Gdx.app.getPreferences("OimohoriPrefs") }

    override fun create() {
        batch = SpriteBatch()
        loadGameState()
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

        // ここからresetPlayPoints()の呼び出しを削除
        setScreen(TitleScreen(this))
    }

    fun resetPlayPoints() {
        currentPlayPoints = 0
        pointsAdded = false
        Gdx.app.log("GameMain", "Play points reset: currentPlayPoints=$currentPlayPoints, pointsAdded=$pointsAdded")
    }

    fun saveGameState() {
        prefs.putInteger("score", score)
        prefs.putInteger("moguraHarvest", moguraHarvest)
        prefs.putInteger("turuhasiLevel", turuhasiLevel)
        prefs.putInteger("turuhasiValue", turuhasiValue)
        prefs.putInteger("moguraCost", moguraCost)
        prefs.putInteger("turuhasiValueCost", turuhasiValueCost)
        prefs.putInteger("turuhasiUnlockedCount", turuhasiUnlockedCount)
        prefs.putInteger("turuhasiUnlockCost", turuhasiUnlockCost)

        val imoNames = unlockedImos.map { it.textureName }.joinToString(",")
        prefs.putString("unlockedImos", imoNames)

        prefs.flush()
        Gdx.app.log("GameMain", "Game state saved: score=$score")
    }

    private fun loadGameState() {
        score = prefs.getInteger("score", 0)
        moguraHarvest = prefs.getInteger("moguraHarvest", 1)
        turuhasiLevel = prefs.getInteger("turuhasiLevel", 3)
        turuhasiValue = prefs.getInteger("turuhasiValue", 3)
        moguraCost = prefs.getInteger("moguraCost", 50)
        turuhasiValueCost = prefs.getInteger("turuhasiValueCost", 50)
        turuhasiUnlockedCount = prefs.getInteger("turuhasiUnlockedCount", 0)
        turuhasiUnlockCost = prefs.getInteger("turuhasiUnlockCost", 500)

        val imoNames = prefs.getString("unlockedImos", "").split(",").filter { it.isNotEmpty() }
        unlockedImos.clear()
        imoNames.forEach { textureName ->
            ImoConfig.imoTypes.find { it.textureName == textureName }?.let { unlockedImos.add(it) }
        }

        Gdx.app.log("GameMain", "Game state loaded: score=$score, unlockedImos=${unlockedImos.size}")
    }

    fun updateTuruhasiValue() {
        turuhasiValue = turuhasiLevel * (turuhasiUnlockedCount + 1)
        saveGameState()
    }

    fun updateScore(newScore: Int) {
        score = newScore
        saveGameState()
        Gdx.app.log("GameMain", "Score updated: score=$score")
    }

    fun unlockImo(imoType: ImoType) {
        unlockedImos.add(imoType)
        saveGameState()
    }

    fun showAdDialog(message: String, onRewardGranted: Runnable) {
        adHandler?.showAdDialog(message, onRewardGranted)
    }

    fun showMessageDialog(message: String) {
        adHandler?.showMessageDialog(message)
    }

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

    fun stopSyuukakujiSound() {
        if (::syuukakujiSound.isInitialized) {
            syuukakujiSound.stop()
        }
    }

    override fun dispose() {
        saveGameState()
        batch.dispose()
        if (::bgm.isInitialized) bgm.dispose()
        if (::resultSound.isInitialized) resultSound.dispose()
        if (::tsuruhasiSound.isInitialized) tsuruhasiSound.dispose()
        if (::syuukakujiSound.isInitialized) syuukakujiSound.dispose()
        if (::pushSound.isInitialized) pushSound.dispose()
    }
}
