package com.vinn.vhike.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vinn.vhike.ui.screens.AddHikeScreen
import com.vinn.vhike.ui.screens.HikeDetailScreen
import com.vinn.vhike.ui.screens.HikeListScreen
import com.vinn.vhike.ui.screens.MapPickerScreen
import com.vinn.vhike.ui.screens.SearchHikeScreen

object AppDestinations {
    const val HIKE_LIST = "hike_list"
    const val ADD_HIKE = "add_hike"
    const val SEARCH_HIKES = "search_hikes"
    const val HIKE_DETAIL = "hike_detail"
    const val HIKE_ID_ARG = "hikeId"
    const val MAP_PICKER = "map_picker"
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.HIKE_LIST
    ) {
        composable(AppDestinations.HIKE_LIST) {
            HikeListScreen(
                onAddHike = { navController.navigate(AppDestinations.ADD_HIKE) },
                onSearchClick = { navController.navigate(AppDestinations.SEARCH_HIKES) },
                onHikeClick = { hikeId ->
                    navController.navigate("${AppDestinations.HIKE_DETAIL}/$hikeId")
                }
            )
        }

        composable(AppDestinations.ADD_HIKE) { navBackStackEntry ->
            AddHikeScreen(
                navBackStackEntry = navBackStackEntry,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMap = { navController.navigate(AppDestinations.MAP_PICKER) }
            )
        }

        composable(AppDestinations.ADD_HIKE) { navBackStackEntry ->
            AddHikeScreen(
                navBackStackEntry = navBackStackEntry,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMap = { navController.navigate(AppDestinations.MAP_PICKER) }
            )
        }

        composable(AppDestinations.SEARCH_HIKES) {
            SearchHikeScreen(
                onNavigateBack = { navController.popBackStack() },
                onHikeClick = { hikeId ->
                    navController.navigate("${AppDestinations.HIKE_DETAIL}/$hikeId")
                }
            )
        }

        composable(
            route = "${AppDestinations.HIKE_DETAIL}/{${AppDestinations.HIKE_ID_ARG}}",
            arguments = listOf(navArgument(AppDestinations.HIKE_ID_ARG) {
                type = NavType.LongType
            })
        ) { backStackEntry ->
            val hikeId = backStackEntry.arguments?.getLong(AppDestinations.HIKE_ID_ARG)
            if (hikeId != null) {
                HikeDetailScreen(
                    hikeId = hikeId,
                    onNavigateBack = { navController.popBackStack() },
                    onEditHike = { /* You could navigate to AddHike with an ID */ }
                )
            }
        }
        composable(AppDestinations.MAP_PICKER) {
            MapPickerScreen (
                onNavigateBack = { navController.popBackStack() },
                onLocationSelected = { latLng ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("pickedLocation", latLng)
                    navController.popBackStack()
                }
            )
        }
    }
}