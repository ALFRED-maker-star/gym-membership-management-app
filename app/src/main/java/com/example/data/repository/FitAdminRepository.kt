package com.example.data.repository

import com.example.data.db.FitAdminDao
import com.example.data.model.CheckIn
import com.example.data.model.GymPlan
import com.example.data.model.Member
import com.example.data.model.MemberStatus
import com.example.data.model.Payment
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class FitAdminRepository(private val dao: FitAdminDao) {

    val allPlans: Flow<List<GymPlan>> = dao.getAllPlans()
    val allMembers: Flow<List<Member>> = dao.getAllMembers()
    val allPayments: Flow<List<Payment>> = dao.getAllPayments()
    val allCheckIns: Flow<List<CheckIn>> = dao.getAllCheckIns()

    fun getMemberById(id: Long): Flow<Member?> = dao.getMemberById(id)

    fun getPaymentsForMember(memberId: Long): Flow<List<Payment>> =
        dao.getPaymentsForMember(memberId)

    fun getCheckInsForMember(memberId: Long): Flow<List<CheckIn>> =
        dao.getCheckInsForMember(memberId)

    suspend fun getMemberByIdDirect(id: Long): Member? = dao.getMemberByIdDirect(id)

    suspend fun getMemberByCode(code: String): Member? = dao.getMemberByCode(code)

    suspend fun saveMember(member: Member): Long {
        return if (member.id == 0L) {
            dao.insertMember(member)
        } else {
            dao.updateMember(member)
            member.id
        }
    }

    suspend fun deleteMember(member: Member) {
        dao.deleteMember(member)
    }

    suspend fun savePlan(plan: GymPlan): Long {
        return if (plan.id == 0L) {
            dao.insertPlan(plan)
        } else {
            dao.updatePlan(plan)
            plan.id
        }
    }

    suspend fun deletePlan(plan: GymPlan) {
        dao.deletePlan(plan)
    }

    /**
     * Record payment and auto-extend member expiry date.
     * If currently expired, new expiry = today + plan duration.
     * If currently active, new expiry = current expiry + plan duration.
     */
    suspend fun recordPayment(
        memberId: Long,
        planId: Long,
        amount: Double,
        paymentMethod: String,
        notes: String? = null
    ): Payment? {
        val member = dao.getMemberByIdDirect(memberId) ?: return null
        val plan = dao.getPlanById(planId) ?: return null

        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()

        // Base date: if already expired, extend from now; if still active, extend from current expiry
        val baseDate = if (member.expiryDate > now) member.expiryDate else now
        cal.timeInMillis = baseDate
        cal.add(Calendar.MONTH, plan.durationMonths)
        val newExpiryDate = cal.timeInMillis

        val receiptNumber = "REC-${Random.nextInt(1000, 9999)}"

        val payment = Payment(
            memberId = member.id,
            memberName = member.fullName,
            planId = plan.id,
            planName = plan.name,
            amount = amount,
            paymentDate = now,
            paymentMethod = paymentMethod,
            extendedUntilDate = newExpiryDate,
            receiptNumber = receiptNumber,
            notes = notes
        )
        val paymentId = dao.insertPayment(payment)

        // Update member's plan and expiry date
        val updatedMember = member.copy(
            planId = plan.id,
            planName = plan.name,
            expiryDate = newExpiryDate
        )
        dao.updateMember(updatedMember)

        return payment.copy(id = paymentId)
    }

    /**
     * Check-in member:
     * - Returns Pair(Boolean, String): (isAllowed, message)
     * - Blocks if membership is expired!
     */
    suspend fun checkInMember(memberId: Long): Pair<Boolean, String> {
        val member = dao.getMemberByIdDirect(memberId)
            ?: return Pair(false, "Member not found")

        val now = System.currentTimeMillis()
        val status = member.getStatus(now)

        return if (status == MemberStatus.EXPIRED) {
            val daysAgo = -member.daysRemaining(now)
            val denialReason = "Membership expired $daysAgo day(s) ago on ${member.formattedExpiry}."
            val checkIn = CheckIn(
                memberId = member.id,
                memberCode = member.memberCode,
                memberName = member.fullName,
                planName = member.planName,
                timestamp = now,
                isAllowed = false,
                statusNote = "Access Denied: $denialReason"
            )
            dao.insertCheckIn(checkIn)
            Pair(false, denialReason)
        } else {
            val daysLeft = member.daysRemaining(now)
            val note = if (status == MemberStatus.EXPIRING_SOON) {
                "Approved • Expires in $daysLeft day(s)!"
            } else {
                "Approved • $daysLeft days remaining"
            }
            val checkIn = CheckIn(
                memberId = member.id,
                memberCode = member.memberCode,
                memberName = member.fullName,
                planName = member.planName,
                timestamp = now,
                isAllowed = true,
                statusNote = note
            )
            dao.insertCheckIn(checkIn)
            Pair(true, note)
        }
    }
}
