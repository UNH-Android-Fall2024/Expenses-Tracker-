package com.unh.expense_tracker.ui.activity

data class expensedata(
    var amount: String,
    var Transactiondate: String,
    var desc: String,
    var category: String
)

val expenselist: ArrayList<expensedata> = arrayListOf(
    expensedata(amount = "1000", Transactiondate = "20/10/2024", desc = "walmart", category = "Shopping"),
    expensedata(amount = "2000", Transactiondate = "20/10/2024", desc = "walmart", category = "Shopping"),
    expensedata(amount = "3000", Transactiondate = "20/10/2024", desc = "walmart", category = "Shopping"),
    expensedata(amount = "4000", Transactiondate = "20/10/2024", desc = "walmart", category = "Shopping"),
    expensedata(amount = "5000", Transactiondate = "20/10/2024", desc = "walmart", category = "Shopping")
)
