# Firebase Integration - Quick Reference

## How to Use Firebase Services

All Firebase operations use the `FirebaseServiceManager` singleton:

```kotlin
// Example: Get all accounts for a user
lifecycleScope.launch {
    val accounts = withContext(Dispatchers.IO) {
        FirebaseServiceManager.accountService.getAllAccountsForUser(userId)
    }
    // Update UI with accounts
}
```

## Available Services

### UserService
```kotlin
FirebaseServiceManager.userService.insert(user: User) // Returns String (user ID)
FirebaseServiceManager.userService.login(username: String, password: String): User?
```

### AccountService
```kotlin
FirebaseServiceManager.accountService.insert(account: Account) // Returns String (account ID)
FirebaseServiceManager.accountService.getAllAccountsForUser(userId: String): List<Account>
FirebaseServiceManager.accountService.getAccountById(accountId: String): Account?
FirebaseServiceManager.accountService.updateAccount(account: Account)
FirebaseServiceManager.accountService.deleteAccount(accountId: String)
```

### CategoryService
```kotlin
FirebaseServiceManager.categoryService.insert(category: Category) // Returns String (category ID)
FirebaseServiceManager.categoryService.getCategoriesForAccount(accountId: String): List<Category>
FirebaseServiceManager.categoryService.getCategoriesForAccountAndUser(accountId: String, userId: String): List<Category>
FirebaseServiceManager.categoryService.updateCategory(category: Category)
FirebaseServiceManager.categoryService.deleteCategory(categoryId: String)
```

### ExpenseService
```kotlin
FirebaseServiceManager.expenseService.insertExpense(expense: Expense) // Returns String (expense ID)
FirebaseServiceManager.expenseService.getExpensesForUserAccount(userId: String, accountId: String): List<Expense>
FirebaseServiceManager.expenseService.updateExpense(expense: Expense)
FirebaseServiceManager.expenseService.deleteExpense(expenseId: String)
```

## Important Notes

### All IDs are Strings
Firebase requires String IDs, so all entities use `String` instead of `Int` for IDs.

### Async Operations
All Firebase operations are suspend functions and should be called from coroutines:

```kotlin
lifecycleScope.launch {
    val result = withContext(Dispatchers.IO) {
        FirebaseServiceManager.accountService.getAllAccountsForUser(userId)
    }
    // Update UI on main thread
}
```

### Error Handling
Firebase operations can throw exceptions. Wrap in try-catch:

```kotlin
try {
    FirebaseServiceManager.accountService.insert(account)
} catch (e: Exception) {
    Log.e("Firebase", "Error inserting account", e)
    Toast.makeText(this, "Error saving account", Toast.LENGTH_SHORT).show()
}
```

## Firebase Database Structure

Data is stored in Firebase Realtime Database as JSON:

```json
{
  "users": {
    "abc123": {
      "id": "abc123",
      "username": "john",
      "password": "hashed_password"
    }
  },
  "accounts": {
    "def456": {
      "id": "def456",
      "userId": "abc123",
      "name": "Savings",
      "balance": 1500.0
    }
  },
  "categories": {
    "ghi789": {
      "id": "ghi789",
      "userId": "abc123",
      "accountId": "def456",
      "name": "Groceries",
      "allocatedAmount": 500.0
    }
  },
  "expenses": {
    "jkl012": {
      "id": "jkl012",
      "userId": "abc123",
      "accountId": "def456",
      "categoryId": "ghi789",
      "description": "Weekly shopping",
      "amount": 75.50,
      "date": 1704067200000,
      "receiptUri": null
    }
  }
}
```

## See Also
- `MIGRATION_COMPLETE.md` - Full migration details
- Firebase Documentation: https://firebase.google.com/docs/database/android/start
