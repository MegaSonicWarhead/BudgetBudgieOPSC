package com.example.budget_budgie_opsc

import com.example.budget_budgie_opsc.R

object AppData{
    public var currentOutfit = R.raw.ivan_framed;

    val GlobalOutfitAvailable: MutableMap<String, Boolean> = mutableMapOf(
        "Plain Ivan" to false,
        "Glasses" to false,
        "Winky Glasses" to false,
        "Ball & Chain" to false,
        "Mother Russia" to false,
        "Cool Glasses" to false
    )

    val GlobalIvanOutfits: MutableMap<String, Int> = mutableMapOf(
        "Plain Ivan" to R.raw.ivan_framed,
        "Glasses" to R.raw.ivan_with_plain_glasses,
        "Winky Glasses" to R.raw.ivan_red_glasses,
        "Ball & Chain" to R.raw.ivan_ball_and_chain,
        "Mother Russia" to R.raw.ivan_mother_russia,
        "Cool Glasses" to R.raw.ivan_with_lightning_glasses
    )
}