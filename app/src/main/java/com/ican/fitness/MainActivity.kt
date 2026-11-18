package com.ican.fitness

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ican.fitness.ui.IcanViewModel
import com.ican.fitness.ui.screens.*
import com.ican.fitness.ui.theme.*

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val app = application as IcanApplication

        setContent {
            IcanTheme {
                val vm: IcanViewModel = viewModel(factory = IcanViewModel.Factory(app.repository))
                val profileState by vm.userProfile.collectAsState()

                val profile = profileState
                if (profile == null) {
                    Surface(modifier = Modifier.fillMaxSize(), color = CharcoalBg) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = OliveAccent)
                        }
                    }
                    return@IcanTheme
                }

                if (!profile.onboardingCompleted) {
                    OnboardingScreen(
                        existingProfile = profile,
                        onComplete = { completedProfile -> vm.saveProfile(completedProfile) }
                    )
                    return@IcanTheme
                }

                var selectedTab by remember { mutableIntStateOf(0) }
                var activeWorkoutMode by remember { mutableStateOf(false) }

                val todayPlan by vm.todayWorkoutPlan.collectAsState()
                val todayMealPlan by vm.todayMealPlan.collectAsState()
                val recentMealPlans by vm.recentMealPlans.collectAsState()
                val goals by vm.allGoals.collectAsState()
                val workouts by vm.completedWorkouts.collectAsState()
                val records by vm.personalRecords.collectAsState()
                val measurements by vm.measurements.collectAsState()
                val interrupted by vm.interruptedSession.collectAsState()
                val isResearching by vm.isResearchingGoal.collectAsState()
                val researchError by vm.researchError.collectAsState()
                val isGeneratingDiet by vm.isGeneratingDiet.collectAsState()

                if (activeWorkoutMode && todayPlan != null) {
                    WorkoutSessionScreen(
                        plan = todayPlan!!,
                        beginnerMode = profile.beginnerMode,
                        onFinishWorkout = { session, recs, _ ->
                            vm.finishWorkout(session, recs)
                        },
                        onSaveInterrupted = { partial ->
                            vm.saveInterruptedSession(partial)
                        },
                        onCancelWorkout = {
                            activeWorkoutMode = false
                        }
                    )
                    return@IcanTheme
                }

                Scaffold(
                    containerColor = CharcoalBg,
                    bottomBar = {
                        NavigationBar(
                            containerColor = GraphiteSurface,
                            contentColor = WarmCream
                        ) {
                            val navItems = listOf(
                                Triple("Today", Icons.Default.Today, 0),
                                Triple("Goals", Icons.Default.TrackChanges, 1),
                                Triple("Diet", Icons.Default.Restaurant, 2),
                                Triple("Progress", Icons.Default.Insights, 3),
                                Triple("Profile", Icons.Default.Person, 4)
                            )
                            navItems.forEach { (label, icon, index) ->
                                NavigationBarItem(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    icon = { Icon(icon, contentDescription = label) },
                                    label = { Text(label) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = CharcoalBg,
                                        selectedTextColor = OliveAccent,
                                        indicatorColor = OliveAccent,
                                        unselectedIconColor = WarmCreamMuted,
                                        unselectedTextColor = WarmCreamMuted
                                    )
                                )
                            }
                        }
                    }
                ) { padding ->
                    Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                        when (selectedTab) {
                            0 -> {
                                if (todayPlan != null) {
                                    TodayScreen(
                                        profile = profile,
                                        todayPlan = todayPlan!!,
                                        todayMealPlan = todayMealPlan,
                                        activeGoalsCount = goals.count { it.status == "ACTIVE" },
                                        interruptedSession = interrupted,
                                        onStartWorkout = { activeWorkoutMode = true },
                                        onRefreshWorkout = { vm.refreshTodayWorkout() },
                                        onAddWater = { vm.addWater(it) },
                                        onNavigateToDiet = { selectedTab = 2 }
                                    )
                                }
                            }
                            1 -> GoalsScreen(
                                goals = goals,
                                isResearching = isResearching,
                                errorMessage = researchError,
                                onSubmitGoalResearch = { vm.researchGoal(it) },
                                onUpdateGoalStatus = { g, st -> vm.updateGoalStatus(g, st) },
                                onDeleteGoal = { vm.deleteGoal(it) }
                            )
                            2 -> DietScreen(
                                profile = profile,
                                activeMealPlan = todayMealPlan,
                                recentPlans = recentMealPlans,
                                isGenerating = isGeneratingDiet,
                                onGenerateMealPlan = { vm.generateMealPlan(it) },
                                onToggleMealCompleted = { plan, type -> vm.toggleMealCompleted(plan, type) },
                                onReplaceMeal = { plan, type, alt -> vm.replaceMeal(plan, type, alt) },
                                onAddWater = { vm.addWater(it) }
                            )
                            3 -> ProgressScreen(
                                workouts = workouts,
                                measurements = measurements,
                                personalRecords = records,
                                onSaveMeasurement = { vm.saveMeasurement(it) },
                                onDeleteMeasurement = { vm.deleteMeasurement(it) }
                            )
                            4 -> ProfileScreen(
                                profile = profile,
                                onSaveProfile = { vm.saveProfile(it) },
                                onExportDataJson = { vm.exportDataJson() },
                                onWipeAllData = { vm.wipeAllData() }
                            )
                        }
                    }
                }
            }
        }
    }
}
