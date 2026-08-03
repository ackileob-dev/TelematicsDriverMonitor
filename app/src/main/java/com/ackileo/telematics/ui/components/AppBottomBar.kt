package com.ackileo.telematics.ui.components



import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.ackileo.telematics.screens.bottomNavItems



/**
 * A reusable Bottom Navigation Bar component for the Telematics app.
 *
 * @param navController The controller used to handle navigation between tabs.
 * @param currentRoute The current active route string to handle highlighting.
 */
@Composable
fun AppBottomBar(
    navController: NavController,
    currentRoute: String?
) {
    NavigationBar {
        bottomNavItems.forEach { screen ->
            val isSelected = currentRoute == screen.route

            NavigationBarItem(
                label = {
                    screen.title?.let { Text(text = it) }
                },
                icon = {
                    screen.icon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = screen.title
                        )
                    }
                },
                selected = isSelected,
                onClick = {
                    // Avoid navigating if already on the selected tab
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            // on the back stack as users select items
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when

                            launchSingleTop = true

                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}