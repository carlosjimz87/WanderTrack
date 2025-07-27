package com.carlosjimz87.wandertrack.domain.models

interface NavController<T> {
    val current: T
    fun navigate(screen: T)
    fun replace(screen: T)
    fun pop()
    val backStack: List<T>
    fun setNewRoot(screen: T)
}