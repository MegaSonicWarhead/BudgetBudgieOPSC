package com.example.budget_budgie_opsc

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    val id: String = "",
    val username: String = "",
    val password: String = ""
)
