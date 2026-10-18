package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CheckIn
import com.example.data.model.GymPlan
import com.example.data.model.Member
import com.example.data.model.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface FitAdminDao {

    // Gym Plans
    @Query("SELECT * FROM gym_plans ORDER BY price ASC")
    fun getAllPlans(): Flow<List<GymPlan>>

    @Query("SELECT * FROM gym_plans WHERE id = :id LIMIT 1")
    suspend fun getPlanById(id: Long): GymPlan?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: GymPlan): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlans(plans: List<GymPlan>)

    @Update
    suspend fun updatePlan(plan: GymPlan)

    @Delete
    suspend fun deletePlan(plan: GymPlan)

    // Members
    @Query("SELECT * FROM members WHERE isArchived = 0 ORDER BY fullName ASC")
    fun getAllMembers(): Flow<List<Member>>

    @Query("SELECT * FROM members WHERE id = :id LIMIT 1")
    fun getMemberById(id: Long): Flow<Member?>

    @Query("SELECT * FROM members WHERE id = :id LIMIT 1")
    suspend fun getMemberByIdDirect(id: Long): Member?

    @Query("SELECT * FROM members WHERE memberCode = :code LIMIT 1")
    suspend fun getMemberByCode(code: String): Member?

    @Query("SELECT COUNT(*) FROM members WHERE isArchived = 0")
    suspend fun countMembers(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: Member): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<Member>)

    @Update
    suspend fun updateMember(member: Member)

    @Delete
    suspend fun deleteMember(member: Member)

    // Payments
    @Query("SELECT * FROM payments ORDER BY paymentDate DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE memberId = :memberId ORDER BY paymentDate DESC")
    fun getPaymentsForMember(memberId: Long): Flow<List<Payment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<Payment>)

    // Check-ins
    @Query("SELECT * FROM check_ins ORDER BY timestamp DESC")
    fun getAllCheckIns(): Flow<List<CheckIn>>

    @Query("SELECT * FROM check_ins WHERE memberId = :memberId ORDER BY timestamp DESC")
    fun getCheckInsForMember(memberId: Long): Flow<List<CheckIn>>

    @Query("SELECT * FROM check_ins WHERE timestamp >= :startOfDay ORDER BY timestamp DESC")
    fun getTodayCheckIns(startOfDay: Long): Flow<List<CheckIn>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: CheckIn): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIns(checkIns: List<CheckIn>)
}
