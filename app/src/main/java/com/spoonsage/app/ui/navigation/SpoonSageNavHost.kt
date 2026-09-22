package com.spoonsage.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.spoonsage.app.data.UserPreferences
import com.spoonsage.app.ui.aicoach.AiCoachScreen
import com.spoonsage.app.ui.cooking.CookingModeScreen
import com.spoonsage.app.ui.dashboard.DashboardScreen
import com.spoonsage.app.ui.grocery.GroceryListScreen
import com.spoonsage.app.ui.home.HomeScreen
import com.spoonsage.app.ui.mealplan.MealPlanScreen
import com.spoonsage.app.ui.profile.ProfileScreen
import com.spoonsage.app.ui.profilesetup.ProfileSetupScreen
import com.spoonsage.app.ui.progress.ProgressScreen
import com.spoonsage.app.ui.quickadd.QuickAddDialog
import com.spoonsage.app.ui.recipedetail.RecipeDetailScreen
import com.spoonsage.app.ui.splash.SplashScreen
import com.spoonsage.app.ui.welcome.WelcomeScreen
import com.spoonsage.app.viewmodel.*
import kotlinx.coroutines.flow.first

private object Routes {
    const val SPLASH = "splash"
    const val WELCOME = "welcome"
    const val PROFILE_SETUP = "profile_setup"

    const val HOME = "home"
    const val MEAL_PLAN = "meal_plan"
    const val PROGRESS = "progress"
    const val PROFILE = "profile"

    const val RECIPES = "recipes"
    const val GROCERY = "grocery"
    const val AI_COACH = "ai_coach"
    const val RECIPE_DETAIL = "recipe_detail/{recipeId}"
    const val COOKING = "cooking/{recipeId}"

    fun recipeDetail(id: Int) = "recipe_detail/$id"
    fun cooking(id: Int) = "cooking/$id"
}

private data class BottomTab(val route: String?, val label: String, val icon: ImageVector, val isAction: Boolean = false)

private val bottomTabs = listOf(
    BottomTab(Routes.HOME, "Home", Icons.Default.Home),
    BottomTab(Routes.MEAL_PLAN, "Meal Plan", Icons.Default.CalendarMonth),
    BottomTab(null, "Add", Icons.Default.Add, isAction = true),
    BottomTab(Routes.PROGRESS, "Progress", Icons.Default.TrendingUp),
    BottomTab(Routes.PROFILE, "Profile", Icons.Default.Person)
)

@Composable
fun SpoonSageNavHost(
    factory: AppViewModelFactory,
    preferences: UserPreferences
) {
    val navController = rememberNavController()
    var showQuickAdd by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            if (bottomTabs.any { it.route == currentRoute }) {
                NavigationBar {
                    bottomTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = !tab.isAction && currentRoute == tab.route,
                            onClick = {
                                if (tab.isAction) {
                                    showQuickAdd = true
                                } else if (tab.route != null) {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = Routes.SPLASH,
                modifier = Modifier.padding(padding)
            ) {
                composable(Routes.SPLASH) {
                    SplashScreen(onFinished = {})
                    // Decide where to land based on saved local account state
                    // (no real Firebase session here - see UserPreferences).
                    LaunchedEffect(Unit) {
                        val isGuest = preferences.isGuest.first()
                        val hasAccount = preferences.hasAccount.first()
                        val setupDone = preferences.profileSetupDone.first()
                        val destination = when {
                            isGuest -> Routes.HOME
                            hasAccount && setupDone -> Routes.HOME
                            hasAccount -> Routes.PROFILE_SETUP
                            else -> Routes.WELCOME
                        }
                        kotlinx.coroutines.delay(1500)
                        navController.navigate(destination) { popUpTo(Routes.SPLASH) { inclusive = true } }
                    }
                }

                composable(Routes.WELCOME) {
                    val vm: AuthViewModel = viewModel(factory = factory)
                    WelcomeScreen(
                        viewModel = vm,
                        onLoggedIn = {
                            navController.navigate(Routes.HOME) { popUpTo(Routes.WELCOME) { inclusive = true } }
                        },
                        onSignedUp = {
                            navController.navigate(Routes.PROFILE_SETUP) { popUpTo(Routes.WELCOME) { inclusive = true } }
                        },
                        onGuest = {
                            navController.navigate(Routes.HOME) { popUpTo(Routes.WELCOME) { inclusive = true } }
                        }
                    )
                }

                composable(Routes.PROFILE_SETUP) {
                    val vm: ProfileSetupViewModel = viewModel(factory = factory)
                    ProfileSetupScreen(
                        viewModel = vm,
                        onFinished = {
                            navController.navigate(Routes.HOME) { popUpTo(Routes.PROFILE_SETUP) { inclusive = true } }
                        }
                    )
                }

                composable(Routes.HOME) {
                    val vm: HomeDashboardViewModel = viewModel(factory = factory)
                    DashboardScreen(
                        viewModel = vm,
                        onOpenMealPlan = { navController.navigate(Routes.MEAL_PLAN) },
                        onOpenRecipes = { navController.navigate(Routes.RECIPES) },
                        onOpenGrocery = { navController.navigate(Routes.GROCERY) },
                        onOpenAiCoach = { navController.navigate(Routes.AI_COACH) },
                        onRecipeClick = { id -> navController.navigate(Routes.recipeDetail(id)) }
                    )
                }

                composable(Routes.MEAL_PLAN) {
                    val vm: MealPlanViewModel = viewModel(factory = factory)
                    MealPlanScreen(
                        viewModel = vm,
                        onRecipeClick = { id -> navController.navigate(Routes.recipeDetail(id)) }
                    )
                }

                composable(Routes.PROGRESS) {
                    val vm: ProgressViewModel = viewModel(factory = factory)
                    ProgressScreen(viewModel = vm)
                }

                composable(Routes.PROFILE) {
                    val vm: ProfileViewModel = viewModel(factory = factory)
                    ProfileScreen(
                        viewModel = vm,
                        onLoggedOut = {
                            navController.navigate(Routes.WELCOME) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Routes.RECIPES) {
                    val vm: HomeViewModel = viewModel(factory = factory)
                    HomeScreen(
                        viewModel = vm,
                        onRecipeClick = { id -> navController.navigate(Routes.recipeDetail(id)) }
                    )
                }

                composable(Routes.GROCERY) {
                    val vm: GroceryViewModel = viewModel(factory = factory)
                    GroceryListScreen(viewModel = vm)
                }

                composable(Routes.AI_COACH) {
                    val vm: AiCoachViewModel = viewModel(factory = factory)
                    AiCoachScreen(
                        viewModel = vm,
                        onBack = { navController.popBackStack() },
                        onViewRecipe = { id -> navController.navigate(Routes.recipeDetail(id)) }
                    )
                }

                composable(
                    Routes.RECIPE_DETAIL,
                    arguments = listOf(navArgument("recipeId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getInt("recipeId") ?: return@composable
                    val vm: RecipeDetailViewModel = viewModel(factory = factory)
                    RecipeDetailScreen(
                        recipeId = id,
                        viewModel = vm,
                        onBack = { navController.popBackStack() },
                        onStartCooking = { recipeId -> navController.navigate(Routes.cooking(recipeId)) }
                    )
                }

                composable(
                    Routes.COOKING,
                    arguments = listOf(navArgument("recipeId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getInt("recipeId") ?: return@composable
                    val vm: RecipeDetailViewModel = viewModel(factory = factory)
                    CookingModeScreen(
                        recipeId = id,
                        viewModel = vm,
                        onBack = { navController.popBackStack() },
                        onFinished = { navController.popBackStack(Routes.HOME, inclusive = false) }
                    )
                }
            }

            if (showQuickAdd) {
                val progressVm: ProgressViewModel = viewModel(factory = factory)
                val groceryVm: GroceryViewModel = viewModel(factory = factory)
                QuickAddDialog(
                    progressViewModel = progressVm,
                    groceryViewModel = groceryVm,
                    onDismiss = { showQuickAdd = false }
                )
            }
        }
    }
}
