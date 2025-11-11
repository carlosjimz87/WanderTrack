package com.carlosjimz87.wandertrack.fakes

import com.carlosjimz87.wandertrack.domain.auth.usecase.DeleteAccountUseCase
import com.carlosjimz87.wandertrack.domain.auth.usecase.EnsureUserDocumentUseCase
import com.carlosjimz87.wandertrack.domain.auth.usecase.LoginWithEmailUseCase
import com.carlosjimz87.wandertrack.domain.auth.usecase.LoginWithGoogleUseCase
import com.carlosjimz87.wandertrack.domain.auth.usecase.LogoutUseCase
import com.carlosjimz87.wandertrack.domain.auth.usecase.ResendVerificationEmailUseCase
import com.carlosjimz87.wandertrack.domain.auth.usecase.SignupUseCase

class FakeLoginWithEmailUseCase(private val authRepository: FakeAuthRepositoryImpl) :
    LoginWithEmailUseCase(authRepository) {
    override suspend fun execute(email: String, pass: String): Result<Unit> {
        return authRepository.loginWithEmail(email, pass)
    }
}

class FakeLoginWithGoogleUseCase(private val authRepository: FakeAuthRepositoryImpl) :
    LoginWithGoogleUseCase(authRepository) {
    override suspend fun execute(idToken: String): Result<Unit> {
        return authRepository.loginWithGoogle(idToken)
    }
}

class FakeSignupUseCase(private val authRepository: FakeAuthRepositoryImpl) : SignupUseCase(authRepository) {
    override suspend fun execute(email: String, pass: String): Result<String> {
        return authRepository.signup(email, pass)
    }
}

class FakeResendVerificationEmailUseCase(private val authRepository: FakeAuthRepositoryImpl) :
    ResendVerificationEmailUseCase(authRepository) {
    override suspend fun execute(): Result<String> {
        return authRepository.resendVerificationEmail()
    }
}

class FakeLogoutUseCase(private val authRepository: FakeAuthRepositoryImpl) : LogoutUseCase(authRepository) {
    override fun execute() {
        authRepository.logout()
    }
}

class FakeDeleteAccountUseCase(
    private val authRepository: FakeAuthRepositoryImpl,
    private val firestoreRepository: FakeFirestoreRepositoryImpl
) :
    DeleteAccountUseCase(authRepository, firestoreRepository) {
    override suspend fun execute() {
        val user = authRepository.currentUser ?: return
        firestoreRepository.deleteUserDocument(user.uid)
        authRepository.currentUser?.delete()
    }
}

class FakeEnsureUserDocumentUseCase(
    private val authRepository: FakeAuthRepositoryImpl,
    private val firestoreRepository: FakeFirestoreRepositoryImpl
) :
    EnsureUserDocumentUseCase(authRepository, firestoreRepository) {
    override suspend fun execute() {
        val userId = authRepository.currentUser?.uid ?: throw Exception("USER_NOT_FOUND")
        firestoreRepository.ensureUserDocument(userId)
    }
}