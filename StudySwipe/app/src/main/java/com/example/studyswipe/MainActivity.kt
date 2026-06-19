package com.example.studyswipe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.studyswipe.ui.pages.HomePage
import com.example.studyswipe.ui.pages.LoginPage
import com.example.studyswipe.ui.pages.ProfileSetupPage
import com.example.studyswipe.ui.pages.RegisterPage
import com.example.studyswipe.ui.pages.UsersListPage
import com.example.studyswipe.ui.pages.MatchesListPage
import com.example.studyswipe.ui.pages.ChatPage
import com.example.studyswipe.ui.pages.SwipePage
import com.example.studyswipe.ui.pages.MatchCelebrationPage
import com.example.studyswipe.ui.pages.SettingsPage
import com.example.studyswipe.ui.pages.AdminHomePage
import com.example.studyswipe.ui.pages.AdminUserListPage
import com.example.studyswipe.model.UserRole
import com.example.studyswipe.ui.theme.StudySwipeTheme
import com.example.studyswipe.viewmodel.AuthResult
import com.example.studyswipe.viewmodel.AuthViewModel

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val PROFILE_SETUP = "profile_setup"
    const val HOME = "home"
    const val USERS_LIST = "users_list"
    const val MATCHES = "matches"
    const val CHAT = "chat/{matchId}"
    const val SWIPE = "swipe"
    const val MATCH_CELEBRATION = "match_celebration/{matchId}"
    const val SETTINGS = "settings"
    const val ADMIN_HOME = "admin_home"
    const val ADMIN_USER_LIST = "admin_user_list/{role}"
}

class MainActivity : ComponentActivity() {
    // by viewModels() creeaza ViewModel-ul in contextul Activity-ului
    // Activity stie automat sa furnizeze Application pentru AndroidViewModel
    // Asta e mai sigur decat viewModel() in composable pentru AndroidViewModel
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudySwipeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    StudySwipeApp(
                        modifier = Modifier.padding(innerPadding),
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun StudySwipeApp(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel
) {
    val navController: NavHostController = rememberNavController()

    val loginState by authViewModel.loginState.collectAsState()
    val registerState by authViewModel.registerState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val allUsers by authViewModel.allUsers.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LaunchedEffect(currentUser) {
                val user = currentUser
                if (user != null) {
                    val destination = if (user.role == UserRole.ADMIN) {
                        Routes.ADMIN_HOME
                    } else if (user.isProfileComplete) {
                        Routes.HOME
                    } else {
                        Routes.PROFILE_SETUP
                    }
                    navController.navigate(destination) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            }

            LaunchedEffect(loginState) {
                if (loginState is AuthResult.Success) {
                    // După login, verificăm dacă profilul e completat sau dacă este admin.
                    val destination = if (currentUser?.role == UserRole.ADMIN) {
                        Routes.ADMIN_HOME
                    } else if (currentUser?.isProfileComplete == true) {
                        Routes.HOME
                    } else {
                        Routes.PROFILE_SETUP
                    }
                    navController.navigate(destination) {
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
                    // După Register mergem la ProfileSetup, nu direct la Home.
                    navController.navigate(Routes.PROFILE_SETUP) {
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

        composable(Routes.PROFILE_SETUP) {
            ProfileSetupPage(
                modifier = modifier,
                userName = currentUser?.name ?: "",
                onSaveClick = { subjects, bio ->
                    authViewModel.saveProfile(subjects, bio)
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.PROFILE_SETUP) { inclusive = true }
                    }
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
                },
                onViewMatchesClick = {
                    navController.navigate(Routes.MATCHES)
                },
                onViewSwipeClick = {
                    navController.navigate(Routes.SWIPE)
                },
                onSettingsClick = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }

        composable(Routes.USERS_LIST) {
            UsersListPage(
                users = allUsers.filter { it.id != currentUser?.id }, // Filter out current user
                onBackClick = { navController.popBackStack() },
                onChatClick = { otherUserId ->
                    authViewModel.startChat(otherUserId) { matchId ->
                        navController.navigate("chat/$matchId")
                    }
                }
            )
        }

        composable(Routes.MATCHES) {
            MatchesListPage(
                authViewModel = authViewModel,
                onMatchClick = { matchId ->
                    navController.navigate("chat/$matchId")
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.CHAT) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
            ChatPage(
                matchId = matchId,
                authViewModel = authViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.SWIPE) {
            SwipePage(
                authViewModel = authViewModel,
                onBackClick = { navController.popBackStack() },
                onMatchCreated = { matchId ->
                    navController.navigate("match_celebration/$matchId") {
                        popUpTo(Routes.SWIPE) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MATCH_CELEBRATION) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
            MatchCelebrationPage(
                matchId = matchId,
                authViewModel = authViewModel,
                onStartChatClick = {
                    navController.navigate("chat/$matchId") {
                        popUpTo(Routes.MATCH_CELEBRATION) { inclusive = true }
                    }
                },
                onKeepSwipingClick = {
                    navController.navigate(Routes.SWIPE) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsPage(
                authViewModel = authViewModel,
                onBackClick = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }

        composable(Routes.ADMIN_HOME) {
            AdminHomePage(
                authViewModel = authViewModel,
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.ADMIN_HOME) { inclusive = true }
                    }
                },
                onNavigateToUserList = { role ->
                    navController.navigate("admin_user_list/${role.name}")
                }
            )
        }

        composable(Routes.ADMIN_USER_LIST) { backStackEntry ->
            val roleStr = backStackEntry.arguments?.getString("role") ?: ""
            val role = try {
                UserRole.valueOf(roleStr)
            } catch (e: Exception) {
                UserRole.STUDENT
            }
            AdminUserListPage(
                authViewModel = authViewModel,
                role = role,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}