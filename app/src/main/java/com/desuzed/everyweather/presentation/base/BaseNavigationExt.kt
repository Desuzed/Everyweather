package com.desuzed.everyweather.presentation.base

import androidx.compose.material.navigation.bottomSheet
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

fun <S, A, I, VM, Screen : BaseComposeScreen<S, A, I, VM>> NavGraphBuilder.composeScreenDestination(
    screen: Screen,
    navController: NavHostController,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
) {
    composable(
        route = screen.destination.route,
        arguments = arguments,
        deepLinks = deepLinks,
//TODO animations
        content = { navEntry ->//TODO scope, arguments?
            screen.ProvideScreenForNavGraph(navController, navEntry)
        }
    )
}

fun <S, A, I, VM,
        Screen : BaseComposeScreen<S, A, I, VM>> NavGraphBuilder.composeBottomSheetDestination(
    screen: Screen,
    navController: NavHostController,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
) {
    bottomSheet(
        route = screen.destination.route,
        arguments = arguments,
        deepLinks = deepLinks,
        content = { navEntry ->
            screen.ProvideScreenForNavGraph(navController, navEntry)
        }
    )
}

fun <S, A, I, VM, Screen : BaseComposeScreen<S, A, I, VM>> NavController.navigate(
    screen: Screen,
) {
    this.navigate(screen.destination.route)
}

fun <S, A, I, VM, Screen : BaseComposeScreen<S, A, I, VM>> NavController.navigate(
    screen: Screen,
    argName: String,
    argValue: String,
) {
    this.currentBackStackEntry?.arguments?.putString(argName, argValue)
    this.navigate(screen.destination.route)
    //this.navigate("${screen.destination.route}?$argName=$argValue")
}