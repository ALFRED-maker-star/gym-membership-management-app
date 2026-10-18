package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.CheckInResult
import com.example.ui.FitAdminViewModel
import com.example.ui.Screen
import com.example.ui.components.CheckInResultDialog
import com.example.ui.components.FitAdminBottomNav
import com.example.ui.components.FitAdminTopBar
import com.example.ui.screens.AddEditMemberScreen
import com.example.ui.screens.CheckInScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExpiringSoonScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MemberProfileScreen
import com.example.ui.screens.MembersListScreen
import com.example.ui.screens.PlansSetupScreen
import com.example.ui.screens.RecordPaymentScreen
import com.example.ui.theme.FitAdminTheme

class MainActivity : ComponentActivity() {

    private val viewModel: FitAdminViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitAdminTheme {
                FitAdminApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun FitAdminApp(viewModel: FitAdminViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val staffName by viewModel.staffName.collectAsState()
    val stats by viewModel.dashboardStats.collectAsState()

    val allMembers by viewModel.allMembers.collectAsState()
    val filteredMembers by viewModel.filteredMembers.collectAsState()
    val allPlans by viewModel.allPlans.collectAsState()
    val expiringMembers by viewModel.expiringSoonMembers.collectAsState()
    val allPayments by viewModel.allPayments.collectAsState()
    val allCheckIns by viewModel.allCheckIns.collectAsState()

    val searchQuery by viewModel.memberSearchQuery.collectAsState()
    val statusFilter by viewModel.memberStatusFilter.collectAsState()
    val planFilter by viewModel.memberPlanFilter.collectAsState()

    val checkInResult by viewModel.checkInResult.collectAsState()

    // Handle back press
    BackHandler(enabled = currentScreen !is Screen.Dashboard && currentScreen !is Screen.Login) {
        viewModel.navigateBack()
    }

    // Check-in modal dialog
    CheckInResultDialog(
        result = checkInResult,
        onDismiss = { viewModel.clearCheckInResult() },
        onRenewClick = { memberId ->
            viewModel.clearCheckInResult()
            viewModel.navigateTo(Screen.RecordPayment(preselectedMemberId = memberId))
        }
    )

    if (!isLoggedIn || currentScreen is Screen.Login) {
        LoginScreen(
            onLoginSuccess = { name ->
                viewModel.login(name)
            }
        )
    } else {
        val isRootScreen = currentScreen is Screen.Dashboard ||
                currentScreen is Screen.MembersList ||
                currentScreen is Screen.CheckIn ||
                currentScreen is Screen.ExpiringSoon ||
                currentScreen is Screen.PlansSetup

        Scaffold(
            topBar = {
                val title = when (currentScreen) {
                    is Screen.Dashboard -> "FitAdmin"
                    is Screen.MembersList -> "Member Directory"
                    is Screen.AddEditMember -> if ((currentScreen as Screen.AddEditMember).memberId == null) "New Member" else "Edit Member"
                    is Screen.MemberProfile -> "Member Profile"
                    is Screen.PlansSetup -> "Membership Plans"
                    is Screen.RecordPayment -> "Record Payment"
                    is Screen.CheckIn -> "Fast Check-In"
                    is Screen.ExpiringSoon -> "Expiring Members"
                    is Screen.Login -> "Sign In"
                }

                FitAdminTopBar(
                    title = title,
                    subtitle = if (currentScreen is Screen.Dashboard) "EVOLVE CLUB DESK" else null,
                    canNavigateBack = !isRootScreen,
                    onNavigateBack = { viewModel.navigateBack() },
                    staffName = if (isRootScreen) staffName else null,
                    onLogout = { viewModel.logout() }
                )
            },
            bottomBar = {
                if (isRootScreen) {
                    FitAdminBottomNav(
                        currentScreen = currentScreen,
                        onNavigate = { screen -> viewModel.navigateTo(screen) }
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (val screen = currentScreen) {
                    is Screen.Dashboard -> {
                        DashboardScreen(
                            stats = stats,
                            recentCheckIns = allCheckIns,
                            onNavigate = { dest -> viewModel.navigateTo(dest) }
                        )
                    }

                    is Screen.MembersList -> {
                        MembersListScreen(
                            members = filteredMembers,
                            plans = allPlans,
                            searchQuery = searchQuery,
                            onSearchChange = { viewModel.setMemberSearchQuery(it) },
                            statusFilter = statusFilter,
                            onStatusFilterChange = { viewModel.setMemberStatusFilter(it) },
                            planFilter = planFilter,
                            onPlanFilterChange = { viewModel.setMemberPlanFilter(it) },
                            onMemberClick = { memberId ->
                                viewModel.navigateTo(Screen.MemberProfile(memberId))
                            },
                            onAddMemberClick = {
                                viewModel.navigateTo(Screen.AddEditMember())
                            }
                        )
                    }

                    is Screen.AddEditMember -> {
                        val member = screen.memberId?.let { id ->
                            allMembers.find { it.id == id }
                        }
                        AddEditMemberScreen(
                            existingMember = member,
                            plans = allPlans,
                            onSave = { id, name, phone, email, plan, start, trainer, notes ->
                                viewModel.saveMember(id, name, phone, email, plan, start, trainer, notes) {
                                    viewModel.navigateBack()
                                }
                            },
                            onCancel = { viewModel.navigateBack() }
                        )
                    }

                    is Screen.MemberProfile -> {
                        val member = allMembers.find { it.id == screen.memberId }
                        if (member != null) {
                            val memberPayments = allPayments.filter { it.memberId == member.id }
                            val memberCheckIns = allCheckIns.filter { it.memberId == member.id }

                            MemberProfileScreen(
                                member = member,
                                payments = memberPayments,
                                checkIns = memberCheckIns,
                                onRenewClick = { id ->
                                    viewModel.navigateTo(Screen.RecordPayment(preselectedMemberId = id))
                                },
                                onEditClick = { id ->
                                    viewModel.navigateTo(Screen.AddEditMember(memberId = id))
                                },
                                onDeleteClick = { mem ->
                                    viewModel.deleteMember(mem) {
                                        viewModel.navigateBack()
                                    }
                                },
                                onCheckInClick = { mem ->
                                    viewModel.performCheckIn(mem)
                                }
                            )
                        } else {
                            viewModel.navigateBack()
                        }
                    }

                    is Screen.PlansSetup -> {
                        PlansSetupScreen(
                            plans = allPlans,
                            members = allMembers,
                            onSavePlan = { id, name, duration, price, desc, color ->
                                viewModel.savePlan(id, name, duration, price, desc, color) {}
                            },
                            onDeletePlan = { plan ->
                                viewModel.deletePlan(plan) {}
                            }
                        )
                    }

                    is Screen.RecordPayment -> {
                        RecordPaymentScreen(
                            members = allMembers,
                            plans = allPlans,
                            preselectedMemberId = screen.preselectedMemberId,
                            onSavePayment = { memberId, planId, amount, method, notes, onSuccess ->
                                viewModel.recordPayment(memberId, planId, amount, method, notes, onSuccess)
                            },
                            onDone = {
                                viewModel.resetToDashboard()
                            }
                        )
                    }

                    is Screen.CheckIn -> {
                        CheckInScreen(
                            members = allMembers,
                            recentCheckIns = allCheckIns,
                            onCheckInMember = { mem ->
                                viewModel.performCheckIn(mem)
                            },
                            onRenewMember = { id ->
                                viewModel.navigateTo(Screen.RecordPayment(preselectedMemberId = id))
                            }
                        )
                    }

                    is Screen.ExpiringSoon -> {
                        ExpiringSoonScreen(
                            expiringMembers = expiringMembers,
                            onMemberClick = { memberId ->
                                viewModel.navigateTo(Screen.MemberProfile(memberId))
                            },
                            onRenewClick = { memberId ->
                                viewModel.navigateTo(Screen.RecordPayment(preselectedMemberId = memberId))
                            }
                        )
                    }

                    is Screen.Login -> {
                        // Handled above
                    }
                }
            }
        }
    }
}
