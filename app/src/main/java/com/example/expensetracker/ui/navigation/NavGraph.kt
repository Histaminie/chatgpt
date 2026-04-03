package com.example.expensetracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.expensetracker.ui.screens.add_edit.AddEditExpenseScreen
import com.example.expensetracker.ui.screens.dashboard.DashboardScreen
import com.example.expensetracker.ui.screens.history.HistoryScreen
import com.example.expensetracker.viewmodel.ExpenseViewModel

sealed class AppRoute(val route: String) {
    data object Dashboard : AppRoute("dashboard")
    data object AddExpense : AppRoute("add_expense")
    data object History : AppRoute("history")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.Dashboard.route,
        modifier = modifier
    ) {
        composable(AppRoute.Dashboard.route) {
            DashboardScreen(viewModel = viewModel)
        }
        composable(AppRoute.AddExpense.route) {
            AddEditExpenseScreen(viewModel = viewModel)
        }
        composable(AppRoute.History.route) {
            HistoryScreen(viewModel = viewModel)
        }
    }
}
