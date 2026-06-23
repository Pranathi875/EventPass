package com.example.eventpass.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.eventpass.ui.addattendee.AddAttendeeScreen
import com.example.eventpass.ui.attendees.AttendeeListScreen
import com.example.eventpass.ui.dashboard.DashboardScreen
import com.example.eventpass.ui.login.LoginScreen
import com.example.eventpass.ui.scanner.ScannerScreen
import com.example.eventpass.ui.stats.StatsScreen
import com.example.eventpass.util.AppViewModelFactory

/**
 * Type-safe-ish list of navigation destinations. Keeping the route strings in
 * one place avoids typos when navigating.
 */
object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val SCANNER = "scanner"
    const val ATTENDEES = "attendees"
    const val ADD_ATTENDEE = "add_attendee"
    const val STATS = "stats"
}

/**
 * Hosts the whole navigation graph. The login screen is the start destination;
 * after a successful login we pop it off the back stack so the back button
 * doesn't return to it.
 *
 * @param factory shared ViewModel factory so every screen gets the repository.
 */
@Composable
fun EventPassNavHost(
    factory: AppViewModelFactory,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Routes.LOGIN) {

        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = viewModel(factory = factory),
                onLoginSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                viewModel = viewModel(factory = factory),
                onStartScanning = { navController.navigate(Routes.SCANNER) },
                onViewAttendees = { navController.navigate(Routes.ATTENDEES) },
                onAddAttendee = { navController.navigate(Routes.ADD_ATTENDEE) },
                onViewStats = { navController.navigate(Routes.STATS) }
            )
        }

        composable(Routes.SCANNER) {
            ScannerScreen(
                viewModel = viewModel(factory = factory),
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ATTENDEES) {
            AttendeeListScreen(
                viewModel = viewModel(factory = factory),
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ADD_ATTENDEE) {
            AddAttendeeScreen(
                viewModel = viewModel(factory = factory),
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.STATS) {
            StatsScreen(
                viewModel = viewModel(factory = factory),
                onBack = { navController.popBackStack() }
            )
        }
    }
}
