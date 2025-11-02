package com.example.budget_budgie_opsc

object FirebaseServiceManager {
    val userService = FirebaseUserService()
    val accountService = FirebaseAccountService()
    val categoryService = FirebaseCategoryService()
    val expenseService = FirebaseExpenseService()
}
