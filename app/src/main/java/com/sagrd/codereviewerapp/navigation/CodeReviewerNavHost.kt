package com.sagrd.codereviewerapp.navigation


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sagrd.codereviewerapp.CodeReviewViewModel
import com.sagrd.codereviewerapp.*

@Composable
fun CodeReviewerNavHost() {
    val navController = rememberNavController()
    val viewModel: CodeReviewViewModel = viewModel()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(navController = navController, startDestination = Destinations.Selection) {
            composable<Destinations.Selection> {
                SelectionScreen(
                    viewModel = viewModel,
                    onNavigateToReview = {
                        navController.navigate(Destinations.Review)
                    },
                    onNavigateToHistory = {
                        navController.navigate(Destinations.History)
                    }
                )
            }
            composable<Destinations.Review> {
                ReviewScreen(
                    viewModel = viewModel,
                    onNavigateToSummary = {
                        navController.navigate(Destinations.Summary)
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable<Destinations.Summary> {
                SummaryScreen(
                    viewModel = viewModel,
                    onNavigateToSelection = {
                        navController.navigate(Destinations.Selection) {
                            popUpTo(Destinations.Selection) { inclusive = true }
                        }
                    },
                    onNavigateToHistory = {
                        navController.navigate(Destinations.History) {
                             popUpTo(Destinations.Selection) { inclusive = false }
                        }
                    }
                )
            }
            composable<Destinations.History> {
                HistoryScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToReview = {
                        navController.navigate(Destinations.Review)
                    }
                )
            }
        }
    }
}
