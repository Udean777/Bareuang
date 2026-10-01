package com.ssajudn.bareuang.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ssajudn.bareuang.ui.components.AppNavigationBar

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val showNavigationBar = currentRoute in TopLevelRoutes
    val destinations = rememberTopLevelDestinations()

    Box(Modifier.fillMaxSize()) {
        TourHost(currentRoute, navController) {
        Scaffold(
            bottomBar = {
                AnimatedVisibility(
                    visible = showNavigationBar,
                    enter = fadeIn(tween(350)),
                    exit = fadeOut(tween(350)),
                ) {
                    AppNavigationBar(
                        items = destinations,
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            if (currentRoute != route) navController.navigate(route) {
                                popUpTo(Screen.Dashboard.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            },
            floatingActionButton = {
                AppFabHost(showNavigationBar, currentRoute, navController)
            },
        ) { innerPadding -> AppNavGraph(navController, innerPadding) }
        }
    }
}
