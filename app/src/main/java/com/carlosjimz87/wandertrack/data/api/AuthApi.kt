package com.carlosjimz87.wandertrack.data.api

import com.carlosjimz87.wandertrack.domain.models.AuthData
import com.carlosjimz87.wandertrack.domain.models.AuthProvider

interface AuthApi {
    fun login(provider: AuthProvider, data: AuthData, onResult: (Boolean, String?) -> Unit)
    fun logout()
}