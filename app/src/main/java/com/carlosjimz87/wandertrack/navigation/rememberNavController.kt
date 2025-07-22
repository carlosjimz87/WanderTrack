package com.carlosjimz87.wandertrack.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember

@Composable
fun rememberMyNavController(
    initial: Screen = Screen.Splash
): NavController<Screen> {
    val stack = remember {
        mutableStateListOf(initial)
    }

    return remember(stack) {
        object : NavController<Screen> {
            override val current: Screen
                get() = stack.last()

            override fun navigate(screen: Screen) {
                stack.add(screen)
            }

            override fun replace(screen: Screen) {
                stack.clear()
                stack.add(screen)
            }

            override fun pop() {
                if (stack.size > 1) stack.removeAt(stack.lastIndex)
            }

            override val backStack: List<Screen>
                get() = stack
        }
    }
}