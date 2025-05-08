package com.matsuyo.oimohori

import com.badlogic.gdx.Game
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class GameMain : Game() {
    lateinit var batch: SpriteBatch
    var score = 500
    var moguraHarvest = 1 // モグラの収穫数（デフォルト1）
    var turuhasiLevel = 3 // ツルハシの共通レベル（デフォルト：3）
    var turuhasiValue = 3 // ツルハシの合計値（初期：3）
    var moguraCost = 50 // モグラ強化コスト
    var turuhasiValueCost = 50 // ツルハシの値強化コスト（共通）
    var turuhasiUnlockedCount = 0 // 解放済みツルハシ数（0: ツルハシ1のみ, 1: ツルハシ1,2, 2: ツルハシ1,2,3）
    var turuhasiUnlockCost = 350 // 現在の解放コスト（ツルハシ2: 350, ツルハシ3: 700）

    override fun create() {
        batch = SpriteBatch()
        setScreen(TitleScreen(this))
    }

    fun updateTuruhasiValue() {
        // ツルハシの合計値 = レベル * ツルハシ本数（解放済み数 + 1）
        turuhasiValue = turuhasiLevel * (turuhasiUnlockedCount + 1)
    }

    override fun dispose() {
        batch.dispose()
    }
}
