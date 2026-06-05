package com.example.studyswipe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.studyswipe.ui.pages.HomePage
import com.example.studyswipe.ui.pages.LoginPage
import com.example.studyswipe.ui.pages.RegisterPage
import com.example.studyswipe.ui.theme.StudySwipeTheme
import com.example.studyswipe.viewmodel.AuthResult
import com.example.studyswipe.viewmodel.AuthViewModel

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudySwipeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    StudySwipeApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun StudySwipeApp(modifier: Modifier = Modifier) {
    val navController: NavHostController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    val loginState by authViewModel.loginState.collectAsState()
    val registerState by authViewModel.registerState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LaunchedEffect(loginState) {
                if (loginState is AuthResult.Success) {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                    authViewModel.resetLoginState()
                }
            }

            LoginPage(
                modifier = modifier,
                isLoading = loginState is AuthResult.Loading,
                errorMessage = (loginState as? AuthResult.Error)?.message,
                onRegisterClick = {
                    authViewModel.resetLoginState()
                    navController.navigate(Routes.REGISTER)
                },
                onLoginClickFirebase = { email, password, _ ->
                    authViewModel.login(email, password)
                },
                onLoginClickApi = { email, password, _ ->
                    authViewModel.login(email, password)
                },
                onLoginSuccess = {}
            )
        }

        composable(Routes.REGISTER) {
            LaunchedEffect(registerState) {
                if (registerState is AuthResult.Success) {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                    authViewModel.resetRegisterState()
                }
            }

            RegisterPage(
                modifier = modifier,
                isLoading = registerState is AuthResult.Loading,
                errorMessage = (registerState as? AuthResult.Error)?.message,
                onRegisterClick = { name, email, password, role ->
                    authViewModel.register(name, email, password, role)
                },
                onLoginClick = {
                    authViewModel.resetRegisterState()
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.HOME) {
            HomePage(
                modifier = modifier,
                user = currentUser,
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}