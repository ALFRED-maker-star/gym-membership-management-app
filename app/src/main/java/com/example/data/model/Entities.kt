package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class MemberStatus {
    ACTIVE,
    EXPIRING_SOON,
    EXPIRED
}

@Entity(tableName = "gym_plans")
data class GymPlan(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val durationMonths: Int,
    val price: Double,
    val description: String,
    val isActive: Boolean = true,
    val colorHex: String = "#84CC16"
) {
    val formattedPrice: String
        get() = NumberFormat.getCurrencyInstance(Locale.US).format(price)
}

@Entity(tableName = "members")
data class Member(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val memberCode: String,
    val fullName: String,
    val phone: String,
    val email: String,
    val avatarInitials: String = "",
    val avatarColorHex: String = "#111827",
    val planId: Long,
    val planName: String,
    val startDate: Long,
    val expiryDate: Long,
    val assignedTrainer: String? = null,
    val emergencyContact: String? = null,
    val notes: String? = null,
    val isArchived: Boolean = false
) {
    fun getStatus(currentTimeMs: Long = System.currentTimeMillis()): MemberStatus {
        val diffMs = expiryDate - currentTimeMs
        return when {
            diffMs < 0 -> MemberStatus.EXPIRED
            diffMs <= TimeUnit.DAYS.toMillis(7) -> MemberStatus.EXPIRING_SOON
            else -> MemberStatus.ACTIVE
        }
    }

    fun daysRemaining(currentTimeMs: Long = System.currentTimeMillis()): Int {
        val diffMs = expiryDate - currentTimeMs
        val days = TimeUnit.MILLISECONDS.toDays(diffMs).toInt()
        return if (diffMs < 0) {
            -TimeUnit.MILLISECONDS.toDays(-diffMs).toInt()
        } else {
            days
        }
    }

    val formattedExpiry: String
        get() = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(expiryDate))

    val formattedStartDate: String
        get() = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(startDate))
}

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val memberId: Long,
    val memberName: String,
    val planId: Long,
    val planName: String,
    val amount: Double,
    val paymentDate: Long,
    val paymentMethod: String, // "Credit/Debit Card", "Cash", "UPI/Bank Transfer", "Check"
    val extendedUntilDate: Long,
    val receiptNumber: String,
    val notes: String? = null
) {
    val formattedAmount: String
        get() = NumberFormat.getCurrencyInstance(Locale.US).format(amount)

    val formattedDate: String
        get() = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(paymentDate))
}

@Entity(tableName = "check_ins")
data class CheckIn(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val memberId: Long,
    val memberCode: String,
    val memberName: String,
    val planName: String,
    val timestamp: Long,
    val isAllowed: Boolean,
    val statusNote: String
) {
    val formattedTime: String
        get() = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))

    val formattedDate: String
        get() = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
}
