package com.carlosjimz87.wandertrack.di

import com.carlosjimz87.wandertrack.domain.auth.usecase.DeleteAccountUseCase
import com.carlosjimz87.wandertrack.domain.auth.usecase.EnsureUserDocumentUseCase
import com.carlosjimz87.wandertrack.domain.auth.usecase.LoginWithEmailUseCase
import com.carlosjimz87.wandertrack.domain.auth.usecase.LoginWithGoogleUseCase
import com.carlosjimz87.wandertrack.domain.auth.usecase.LogoutUseCase
import com.carlosjimz87.wandertrack.domain.auth.usecase.ResendVerificationEmailUseCase
import com.carlosjimz87.wandertrack.domain.auth.usecase.SignupUseCase
import org.koin.dsl.module

val authUseCaseModule = module {
    single { LoginWithEmailUseCase(get()) }
    single { LoginWithGoogleUseCase(get()) }
    single { SignupUseCase(get()) }
    single { ResendVerificationEmailUseCase(get()) }
    single { LogoutUseCase(get()) }
    single { DeleteAccountUseCase(get(), get()) }
    single { EnsureUserDocumentUseCase(get(), get()) }
}
