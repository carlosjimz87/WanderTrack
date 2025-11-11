import com.carlosjimz87.wandertrack.fakes.FakeAuthRepositoryImpl
import com.carlosjimz87.wandertrack.fakes.FakeDeleteAccountUseCase
import com.carlosjimz87.wandertrack.fakes.FakeEnsureUserDocumentUseCase
import com.carlosjimz87.wandertrack.fakes.FakeFirestoreRepositoryImpl
import com.carlosjimz87.wandertrack.fakes.FakeLoginWithEmailUseCase
import com.carlosjimz87.wandertrack.fakes.FakeLoginWithGoogleUseCase
import com.carlosjimz87.wandertrack.fakes.FakeLogoutUseCase
import com.carlosjimz87.wandertrack.fakes.FakeResendVerificationEmailUseCase
import com.carlosjimz87.wandertrack.fakes.FakeSessionManagerImpl
import com.carlosjimz87.wandertrack.fakes.FakeSignupUseCase
import com.carlosjimz87.wandertrack.ui.screens.auth.viewmodel.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private lateinit var authRepo: FakeAuthRepositoryImpl
    private lateinit var firestoreRepo: FakeFirestoreRepositoryImpl
    private lateinit var sessionManager: FakeSessionManagerImpl
    private lateinit var vm: AuthViewModel
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        authRepo = FakeAuthRepositoryImpl()
        firestoreRepo = FakeFirestoreRepositoryImpl()
        sessionManager = FakeSessionManagerImpl(authRepo)
        vm = AuthViewModel(
            loginWithEmailUseCase = FakeLoginWithEmailUseCase(authRepo),
            loginWithGoogleUseCase = FakeLoginWithGoogleUseCase(authRepo),
            signupUseCase = FakeSignupUseCase(authRepo),
            resendVerificationEmailUseCase = FakeResendVerificationEmailUseCase(authRepo),
            logoutUseCase = FakeLogoutUseCase(authRepo),
            deleteAccountUseCase = FakeDeleteAccountUseCase(authRepo, firestoreRepo),
            ensureUserDocumentUseCase = FakeEnsureUserDocumentUseCase(authRepo, firestoreRepo),
            sessionManager = sessionManager,
            authRepository = authRepo
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `signup success sets verification flags and successMessage`() = runTest {
        authRepo.nextSignup = FakeAuthRepositoryImpl.Outcome.Success

        vm.signup("test@test.com", "password")
        advanceUntilIdle()

        val state = vm.authUiState.value
        assertTrue(state.isSignupSuccessful)
        assertTrue(state.verificationEmailSent)
        assertNull(state.errorMessage)
    }

    @Test
    fun `email login success updates flags and session`() = runTest {
        authRepo.isEmailVerified = true
        authRepo.nextEmailLogin = FakeAuthRepositoryImpl.Outcome.Success

        vm.loginWithEmail("test@test.com", "password")
        advanceUntilIdle()

        val state = vm.authUiState.value
        assertTrue(state.isLoginSuccessful)
        assertNull(state.errorMessage)
        assertNotNull(vm.authState.value)
        assertTrue(vm.validSession.value == true)
    }

    @Test
    fun `email login blocked when email not verified`() = runTest {
        authRepo.isEmailVerified = false
        authRepo.nextEmailLogin = FakeAuthRepositoryImpl.Outcome.Success

        vm.loginWithEmail("test@test.com", "password")
        advanceUntilIdle()

        val state = vm.authUiState.value
        assertFalse(state.isLoginSuccessful)
        assertEquals("EMAIL_NOT_VERIFIED", state.errorMessage)
        assertTrue(state.showResendButton)
        assertTrue(state.blockNavigation)
        assertNull(vm.authState.value)
    }

    @Test
    fun `email login maps common errors`() = runTest {
        val errorCodes = listOf("WRONG_PASSWORD", "USER_NOT_FOUND", "TOO_MANY_REQUESTS")
        for (code in errorCodes) {
            authRepo.reset()
            authRepo.nextEmailLogin = FakeAuthRepositoryImpl.Outcome.Error(code)

            vm.loginWithEmail("fail@test.com", "x")
            advanceUntilIdle()

            val state = vm.authUiState.value
            assertFalse(state.isLoginSuccessful)
            assertEquals(code, state.errorMessage)
            assertFalse(state.showResendButton)
            assertNull(vm.authState.value)
        }
    }

    @Test
    fun `authUiState emits Loading then success on email login`() = runTest {
        authRepo.isEmailVerified = true
        authRepo.nextEmailLogin = FakeAuthRepositoryImpl.Outcome.Success

        vm.loginWithEmail("test@test.com", "password")
        advanceUntilIdle()

        val state = vm.authUiState.value
        assertTrue(state.isLoginSuccessful)
        assertNull(state.errorMessage)
    }

    @Test
    fun `logout clears authState and resets flags`() = runTest {
        authRepo.isEmailVerified = true
        vm.loginWithEmail("test@test.com", "password")
        advanceUntilIdle()

        assertNotNull(vm.authState.value)
        vm.logout()

        assertNull(vm.authState.value)
        val state = vm.authUiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isLoginSuccessful)
        assertNull(state.errorMessage)
        assertNull(state.successMessage)
        assertFalse(state.verificationEmailSent)
        assertFalse(state.blockNavigation)
    }

    @Test
    fun `google login success sets loginSuccessful`() = runTest {
        authRepo.nextGoogleLogin = FakeAuthRepositoryImpl.Outcome.Success

        vm.loginWithGoogle("token")
        advanceUntilIdle()

        val state = vm.authUiState.value
        assertTrue(state.isLoginSuccessful)
        assertNull(state.errorMessage)
        assertNotNull(vm.authState.value)
    }

    @Test
    fun `google login failure sets errorMessage`() = runTest {
        authRepo.nextGoogleLogin = FakeAuthRepositoryImpl.Outcome.Error("INVALID_IDP_RESPONSE")

        vm.loginWithGoogle("token")
        advanceUntilIdle()

        val state = vm.authUiState.value
        assertFalse(state.isLoginSuccessful)
        assertEquals("USER_NOT_FOUND", state.errorMessage)
        assertNull(vm.authState.value)
        assertFalse(state.blockNavigation)
    }

    @Test
    fun `resendVerificationEmail updates state with error when no user`() = runTest {
        vm.resendVerificationEmail()
        advanceUntilIdle()

        val state = vm.authUiState.value
        assertFalse(state.verificationEmailSent)
        assertEquals("USER_NOT_FOUND", state.errorMessage)
    }

    @Test
    fun `resendVerificationEmail updates state with success when user exists`() = runTest {
        authRepo.seedLoggedUser(email = "a@b.com")
        authRepo.isEmailVerified = false
        authRepo.nextResendVerification = FakeAuthRepositoryImpl.Outcome.Success

        vm.resendVerificationEmail()
        advanceUntilIdle()

        val state = vm.authUiState.value
        assertTrue(state.verificationEmailSent)
        assertEquals("VERIFICATION_EMAIL_SENT", state.errorMessage)
    }
}