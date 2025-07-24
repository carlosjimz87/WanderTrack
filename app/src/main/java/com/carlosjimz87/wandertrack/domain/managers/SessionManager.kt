package com.carlosjimz87.wandertrack.domain.managers

import kotlinx.coroutines.flow.StateFlow

interface SessionManager {
    val validSession: StateFlow<Boolean?>
    fun refreshSession()
}