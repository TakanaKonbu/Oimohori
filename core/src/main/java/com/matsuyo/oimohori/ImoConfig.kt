package com.matsuyo.oimohori

data class ImoType(
    val name: String,
    val textureName: String,
    val points: Int,
    val minImos: Int = 0,
    val probability: Float = 1.0f
)

object ImoConfig {
    val imoTypes = listOf(
        ImoType("通常芋", "normal_imo.png", 5),
        ImoType("シルバー芋", "silver_imo.png", 7, 30, 0.2f),
        ImoType("ゴールド芋", "gold_imo.png", 10, 30, 0.2f),
        ImoType("メラメラ芋", "fire_imo.png", 12, 150, 0.2f),
        ImoType("ヒエヒエ芋", "ice_imo.png", 12, 150, 0.2f),
        ImoType("キラキラ芋", "star_imo.png", 12, 150, 0.2f),
        ImoType("キリン芋", "giraffe_imo.png", 15, 370, 0.2f),
        ImoType("シマウマ芋", "zebra_imo.png", 15, 370, 0.2f),
        ImoType("ウシ芋", "cow_imo.png", 15, 370, 0.2f),
        ImoType("DJ芋", "dj_imo.png", 17, 580, 0.2f),
        ImoType("虹芋", "rainbow_imo.png", 17, 580, 0.2f),
        ImoType("迷彩芋", "meisai_imo.png", 17, 580, 0.2f),
        ImoType("日本芋", "japan_imo.png", 20, 800, 0.2f),
        ImoType("アメリカ芋", "usa_imo.png", 20, 800, 0.2f),
        ImoType("ドイツ芋", "germany_imo.png", 20, 800, 0.2f),
        ImoType("ジャマイカ芋", "jamaica_imo.png", 20, 800, 0.2f),
        ImoType("ロシア芋", "russia_imo.png", 20, 800, 0.2f),
        ImoType("野球芋", "baseball_imo.png", 25, 1120, 0.2f),
        ImoType("ラグビーボール", "rugbyball.png", 25, 1120, 0.2f),
        ImoType("サッカー芋", "soccer_imo.png", 25, 1120, 0.2f),
        ImoType("バスケ芋", "basketball_imo.png", 25, 1120, 0.2f),
        ImoType("虫食い芋", "musikui_imo.png", 1, probability = 0.05f),
        ImoType("小石", "koisi.png", 1, probability = 0.05f),
        ImoType("ミミズ", "mimizu.png", 1, probability = 0.05f),
        ImoType("ジャガイモ", "poteto.png", 1, probability = 0.05f)
    )
}
