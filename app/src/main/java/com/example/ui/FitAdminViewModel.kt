package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.FitAdminDatabase
import com.example.data.model.CheckIn
import com.example.data.model.GymPlan
import com.example.data.model.Member
import com.example.data.model.MemberStatus
import com.example.data.model.Payment
import com.example.data.repository.FitAdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

sealed class Screen {
    object Login : Screen()
    object Dashboard : Screen()
    object MembersList : Screen()
    data class AddEditMember(val memberId: Long? = null) : Screen()
    data class MemberProfile(val memberId: Long) : Screen()
    object PlansSetup : Screen()
    data class RecordPayment(val preselectedMemberId: Long? = null) : Screen()
    object CheckIn : Screen()
    object ExpiringSoon : Screen()
}

data class CheckInResult(
    val member: Member,
    val isAllowed: Boolean,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class DashboardStats(
    val activeMembersCount: Int = 0,
    val expiringThisWeekCount: Int = 0,
    val revenueThisMonth: Double = 0.0,
    val formattedRevenue: String = "$0.00",
    val checkInsTodayCount: Int = 0
)

class FitAdminViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FitAdminRepository

    init {
        val db = FitAdminDatabase.getDatabase(application, viewModelScope)
        repository = FitAdminRepository(db.fitAdminDao())
    }

    // Navigation Stack
    private val _navigationStack = MutableStateFlow<List<Screen>>(listOf(Screen.Dashboard))
    val currentScreen: StateFlow<Screen> = MutableStateFlow<Screen>(Screen.Dashboard).apply {
        viewModelScope.launch {
            _navigationStack.collect { stack ->
                value = stack.lastOrNull() ?: Screen.Dashboard
            }
        }
    }

    fun navigateTo(screen: Screen) {
        val currentList = _navigationStack.value.toMutableList()
        currentList.add(screen)
        _navigationStack.value = currentList
    }

    fun navigateBack(): Boolean {
        val currentList = _navigationStack.value.toMutableList()
        return if (currentList.size > 1) {
            currentList.removeAt(currentList.lastIndex)
            _navigationStack.value = currentList
            true
        } else {
            false
        }
    }

    fun resetToDashboard() {
        _navigationStack.value = listOf(Screen.Dashboard)
    }

    // Staff / Auth State
    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _staffName = MutableStateFlow("Alex Rivera (Front Desk Lead)")
    val staffName: StateFlow<String> = _staffName.asStateFlow()

    fun login(name: String) {
        _staffName.value = name
        _isLoggedIn.value = true
        resetToDashboard()
    }

    fun logout() {
        _isLoggedIn.value = false
        _navigationStack.value = listOf(Screen.Login)
    }

    // Repository Flows
    val allPlans: StateFlow<List<GymPlan>> = repository.allPlans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMembers: StateFlow<List<Member>> = repository.allMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPayments: StateFlow<List<Payment>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCheckIns: StateFlow<List<CheckIn>> = repository.allCheckIns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Members list filtering
    private val _memberSearchQuery = MutableStateFlow("")
    val memberSearchQuery: StateFlow<String> = _memberSearchQuery.asStateFlow()

    private val _memberStatusFilter = MutableStateFlow<MemberStatus?>(null)
    val memberStatusFilter: StateFlow<MemberStatus?> = _memberStatusFilter.asStateFlow()

    private val _memberPlanFilter = MutableStateFlow<Long?>(null)
    val memberPlanFilter: StateFlow<Long?> = _memberPlanFilter.asStateFlow()

    fun setMemberSearchQuery(query: String) {
        _memberSearchQuery.value = query
    }

    fun setMemberStatusFilter(status: MemberStatus?) {
        _memberStatusFilter.value = status
    }

    fun setMemberPlanFilter(planId: Long?) {
        _memberPlanFilter.value = planId
    }

    val filteredMembers: StateFlow<List<Member>> = combine(
        allMembers,
        _memberSearchQuery,
        _memberStatusFilter,
        _memberPlanFilter
    ) { members, query, status, planId ->
        val now = System.currentTimeMillis()
        members.filter { member ->
            val matchesQuery = query.isBlank() ||
                    member.fullName.contains(query, ignoreCase = true) ||
                    member.memberCode.contains(query, ignoreCase = true) ||
                    member.phone.contains(query, ignoreCase = true) ||
                    member.email.contains(query, ignoreCase = true)

            val matchesStatus = status == null || member.getStatus(now) == status
            val matchesPlan = planId == null || member.planId == planId

            matchesQuery && matchesStatus && matchesPlan
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Expiring within 7 days (or expired within 7 days)
    val expiringSoonMembers: StateFlow<List<Member>> = allMembers.combine(_memberSearchQuery) { members, query ->
        val now = System.currentTimeMillis()
        members.filter { member ->
            val status = member.getStatus(now)
            val isExpiringOrRecentlyExpired = status == MemberStatus.EXPIRING_SOON ||
                    (status == MemberStatus.EXPIRED && member.daysRemaining(now) >= -7)
            val matchesQuery = query.isBlank() ||
                    member.fullName.contains(query, ignoreCase = true) ||
                    member.memberCode.contains(query, ignoreCase = true)
            isExpiringOrRecentlyExpired && matchesQuery
        }.sortedBy { it.expiryDate }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard Statistics Calculation
    val dashboardStats: StateFlow<DashboardStats> = combine(
        allMembers,
        allPayments,
        allCheckIns
    ) { members, payments, checkIns ->
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        cal.timeInMillis = now
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfToday = cal.timeInMillis

        // Start of current month
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val startOfMonth = cal.timeInMillis

        val activeCount = members.count { it.getStatus(now) == MemberStatus.ACTIVE }
        val expiringCount = members.count { it.getStatus(now) == MemberStatus.EXPIRING_SOON }

        val monthlyRevenue = payments
            .filter { it.paymentDate >= startOfMonth }
            .sumOf { it.amount }

        val todayCheckInsCount = checkIns.count { it.timestamp >= startOfToday && it.isAllowed }

        val formattedRev = NumberFormat.getCurrencyInstance(Locale.US).format(monthlyRevenue)

        DashboardStats(
            activeMembersCount = activeCount,
            expiringThisWeekCount = expiringCount,
            revenueThisMonth = monthlyRevenue,
            formattedRevenue = formattedRev,
            checkInsTodayCount = todayCheckInsCount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    // Check-In Handling & Dialogs
    private val _checkInResult = MutableStateFlow<CheckInResult?>(null)
    val checkInResult: StateFlow<CheckInResult?> = _checkInResult.asStateFlow()

    fun clearCheckInResult() {
        _checkInResult.value = null
    }

    fun performCheckIn(member: Member) {
        viewModelScope.launch {
            val (allowed, msg) = repository.checkInMember(member.id)
            _checkInResult.value = CheckInResult(
                member = member,
                isAllowed = allowed,
                message = msg
            )
        }
    }

    // Member Profile Helper Flows
    fun getMemberById(id: Long) = repository.getMemberById(id)
    fun getPaymentsForMember(id: Long) = repository.getPaymentsForMember(id)
    fun getCheckInsForMember(id: Long) = repository.getCheckInsForMember(id)

    // Save/Update Member
    fun saveMember(
        id: Long,
        name: String,
        phone: String,
        email: String,
        plan: GymPlan,
        startDateMs: Long,
        trainer: String?,
        notes: String?,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            cal.timeInMillis = startDateMs
            cal.add(Calendar.MONTH, plan.durationMonths)
            val expiryMs = cal.timeInMillis

            val initials = name.split(" ")
                .filter { it.isNotBlank() }
                .take(2)
                .map { it.first().uppercase() }
                .joinToString("")
                .ifEmpty { "MB" }

            val code = if (id == 0L) {
                "FA-${(1000 + (allMembers.value.size + 1))}"
            } else {
                repository.getMemberByIdDirect(id)?.memberCode ?: "FA-1000"
            }

            val member = Member(
                id = id,
                memberCode = code,
                fullName = name.trim(),
                phone = phone.trim(),
                email = email.trim(),
                avatarInitials = initials,
                avatarColorHex = plan.colorHex,
                planId = plan.id,
                planName = plan.name,
                startDate = startDateMs,
                expiryDate = expiryMs,
                assignedTrainer = trainer?.takeIf { it.isNotBlank() && it != "None" },
                notes = notes?.trim()
            )

            val memberId = repository.saveMember(member)

            // If new member, record first payment automatically
            if (id == 0L) {
                repository.recordPayment(
                    memberId = memberId,
                    planId = plan.id,
                    amount = plan.price,
                    paymentMethod = "Cash/Initial",
                    notes = "Initial membership enrollment"
                )
            }

            onComplete()
        }
    }

    fun deleteMember(member: Member, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteMember(member)
            onComplete()
        }
    }

    // Record Payment
    fun recordPayment(
        memberId: Long,
        planId: Long,
        amount: Double,
        method: String,
        notes: String?,
        onSuccess: (Payment) -> Unit
    ) {
        viewModelScope.launch {
            val payment = repository.recordPayment(
                memberId = memberId,
                planId = planId,
                amount = amount,
                paymentMethod = method,
                notes = notes
            )
            if (payment != null) {
                onSuccess(payment)
            }
        }
    }

    // Plans Management
    fun savePlan(
        id: Long,
        name: String,
        durationMonths: Int,
        price: Double,
        description: String,
        colorHex: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val plan = GymPlan(
                id = id,
                name = name.trim(),
                durationMonths = durationMonths,
                price = price,
                description = description.trim(),
                colorHex = colorHex
            )
            repository.savePlan(plan)
            onComplete()
        }
    }

    fun deletePlan(plan: GymPlan, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deletePlan(plan)
            onComplete()
        }
    }
}
