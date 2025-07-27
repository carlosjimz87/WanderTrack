package com.carlosjimz87.wandertrack.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import com.carlosjimz87.wandertrack.domain.models.NavController
import com.carlosjimz87.wandertrack.domain.models.Screens

@Composable
fun rememberMyNavController(
    initial: Screens = Screens.Splash
): NavController<Screens> {
    val stack = remember {
        mutableStateListOf(initial)
    }

    return remember(stack) {
        object : NavController<Screens> {
            override val current: Screens
                get() = stack.last()

            override fun navigate(screen: Screens) {
                stack.add(screen)
            }

            override fun replace(screen: Screens) {
                stack.clear()
                stack.add(screen)
            }

            override fun pop() {
                if (stack.size > 1) stack.removeAt(stack.lastIndex)
            }

            override fun setNewRoot(screen: Screens) {
                stack.clear()
                stack.add(screen)
            }

            override val backStack: List<Screens>
                get() = stack
        }
    }
}