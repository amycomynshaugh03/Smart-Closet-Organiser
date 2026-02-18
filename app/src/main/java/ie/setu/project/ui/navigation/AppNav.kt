package ie.setu.project.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ie.setu.project.ui.auth.AuthStateViewModel
import ie.setu.project.ui.auth.LoginScreen
import ie.setu.project.ui.auth.RegisterScreen

object Routes {
    const val Login = "login"
    const val Register = "register"
    const val Home = "home"
}

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val authStateVm: AuthStateViewModel = hiltViewModel()
    val user by authStateVm.user.collectAsState()

    val startDestination = if (user == null) Routes.Login else Routes.Home

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.Login) {
            LoginScreen(
                onGoToRegister = { navController.navigate(Routes.Register) },
                onSignedIn = {
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Login) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Register) {
            RegisterScreen(
                onGoToLogin = { navController.popBackStack() },
                onRegistered = {
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Login) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Home) {
            HomeScreen(
                onSignOut = {
                    // after signOut, AuthStateViewModel will update and user==null
                    navController.navigate(Routes.Login) {
                        popUpTo(Routes.Home) { inclusive = true }
                    }
                }
            )
        }
    }
}
