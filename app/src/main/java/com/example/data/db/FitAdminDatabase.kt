package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CheckIn
import com.example.data.model.GymPlan
import com.example.data.model.Member
import com.example.data.model.Payment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@Database(
    entities = [GymPlan::class, Member::class, Payment::class, CheckIn::class],
    version = 1,
    exportSchema = false
)
abstract class FitAdminDatabase : RoomDatabase() {

    abstract fun fitAdminDao(): FitAdminDao

    companion object {
        @Volatile
        private var INSTANCE: FitAdminDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): FitAdminDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FitAdminDatabase::class.java,
                    "fitadmin_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(FitAdminDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class FitAdminDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.fitAdminDao())
                }
            }
        }

        private suspend fun populateInitialData(dao: FitAdminDao) {
            val now = System.currentTimeMillis()
            val dayMs = TimeUnit.DAYS.toMillis(1)

            // Seed Plans
            val plans = listOf(
                GymPlan(
                    id = 1,
                    name = "Monthly Basic",
                    durationMonths = 1,
                    price = 49.00,
                    description = "Full gym floor access, locker room & standard cardio zone.",
                    colorHex = "#3B82F6"
                ),
                GymPlan(
                    id = 2,
                    name = "Quarterly Pro",
                    durationMonths = 3,
                    price = 129.00,
                    description = "Gym floor, steam/sauna, free fitness assessment & 2 guest passes.",
                    colorHex = "#84CC16"
                ),
                GymPlan(
                    id = 3,
                    name = "Half-Year Elite",
                    durationMonths = 6,
                    price = 239.00,
                    description = "All-access + 1 monthly PT session, recovery lounge & towel service.",
                    colorHex = "#F59E0B"
                ),
                GymPlan(
                    id = 4,
                    name = "Annual VIP All-Access",
                    durationMonths = 12,
                    price = 449.00,
                    description = "Unlimited 24/7 access, VIP locker, personalized diet & trainer.",
                    colorHex = "#8B5CF6"
                ),
                GymPlan(
                    id = 5,
                    name = "Student Pass",
                    durationMonths = 1,
                    price = 35.00,
                    description = "Valid student ID required. Off-peak hours & open gym floor.",
                    colorHex = "#06B6D4"
                )
            )
            dao.insertPlans(plans)

            // Seed Members
            val members = listOf(
                Member(
                    id = 1,
                    memberCode = "FA-1001",
                    fullName = "Liam Walker",
                    phone = "+1 (555) 234-8901",
                    email = "liam.walker@example.com",
                    avatarInitials = "LW",
                    avatarColorHex = "#84CC16",
                    planId = 2,
                    planName = "Quarterly Pro",
                    startDate = now - (35 * dayMs),
                    expiryDate = now + (55 * dayMs),
                    assignedTrainer = "Coach Marcus",
                    emergencyContact = "Sarah Walker - +1 (555) 234-8909",
                    notes = "Prefers heavy lifting and power rack morning slots."
                ),
                Member(
                    id = 2,
                    memberCode = "FA-1002",
                    fullName = "Elena Rostova",
                    phone = "+1 (555) 891-2345",
                    email = "elena.r@example.com",
                    avatarInitials = "ER",
                    avatarColorHex = "#3B82F6",
                    planId = 4,
                    planName = "Annual VIP All-Access",
                    startDate = now - (90 * dayMs),
                    expiryDate = now + (275 * dayMs),
                    assignedTrainer = "Coach Elena",
                    emergencyContact = "Dmitri - +1 (555) 891-9999",
                    notes = "Attends HIIT bootcamp and pilates."
                ),
                Member(
                    id = 3,
                    memberCode = "FA-1003",
                    fullName = "Marcus Vance",
                    phone = "+1 (555) 432-1098",
                    email = "m.vance@example.com",
                    avatarInitials = "MV",
                    avatarColorHex = "#10B981",
                    planId = 3,
                    planName = "Half-Year Elite",
                    startDate = now - (60 * dayMs),
                    expiryDate = now + (120 * dayMs),
                    assignedTrainer = "Coach David",
                    notes = "Focusing on hypertrophy & shoulder rehab."
                ),
                Member(
                    id = 4,
                    memberCode = "FA-1004",
                    fullName = "Chloe Davis",
                    phone = "+1 (555) 765-4321",
                    email = "chloe.d@example.com",
                    avatarInitials = "CD",
                    avatarColorHex = "#EC4899",
                    planId = 1,
                    planName = "Monthly Basic",
                    startDate = now - (10 * dayMs),
                    expiryDate = now + (20 * dayMs),
                    assignedTrainer = null,
                    notes = "Cardio and functional training."
                ),
                // EXPIRING SOON (< 7 days)
                Member(
                    id = 5,
                    memberCode = "FA-1005",
                    fullName = "David Morales",
                    phone = "+1 (555) 321-7654",
                    email = "david.morales@example.com",
                    avatarInitials = "DM",
                    avatarColorHex = "#F59E0B",
                    planId = 1,
                    planName = "Monthly Basic",
                    startDate = now - (29 * dayMs),
                    expiryDate = now + (1 * dayMs), // Expires tomorrow!
                    assignedTrainer = "Coach Marcus",
                    notes = "Interested in renewing with Quarterly Pro discount."
                ),
                Member(
                    id = 6,
                    memberCode = "FA-1006",
                    fullName = "Rachel Kim",
                    phone = "+1 (555) 987-6543",
                    email = "rachel.kim@example.com",
                    avatarInitials = "RK",
                    avatarColorHex = "#F97316",
                    planId = 2,
                    planName = "Quarterly Pro",
                    startDate = now - (86 * dayMs),
                    expiryDate = now + (4 * dayMs), // Expires in 4 days
                    assignedTrainer = "Coach Elena",
                    notes = "Remind about locker clearance if not renewing."
                ),
                Member(
                    id = 7,
                    memberCode = "FA-1007",
                    fullName = "Jordan Hayes",
                    phone = "+1 (555) 654-9870",
                    email = "jordan.hayes@example.com",
                    avatarInitials = "JH",
                    avatarColorHex = "#EAB308",
                    planId = 5,
                    planName = "Student Pass",
                    startDate = now - (28 * dayMs),
                    expiryDate = now + (2 * dayMs), // Expires in 2 days
                    assignedTrainer = null,
                    notes = "Evening gym goer."
                ),
                // EXPIRED
                Member(
                    id = 8,
                    memberCode = "FA-1008",
                    fullName = "Samira Khan",
                    phone = "+1 (555) 456-7890",
                    email = "samira.khan@example.com",
                    avatarInitials = "SK",
                    avatarColorHex = "#EF4444",
                    planId = 1,
                    planName = "Monthly Basic",
                    startDate = now - (35 * dayMs),
                    expiryDate = now - (5 * dayMs), // Expired 5 days ago
                    assignedTrainer = "Coach Marcus",
                    notes = "Traveling for work last week, wants to renew."
                ),
                Member(
                    id = 9,
                    memberCode = "FA-1009",
                    fullName = "Tyler Brooks",
                    phone = "+1 (555) 345-6789",
                    email = "tyler.b@example.com",
                    avatarInitials = "TB",
                    avatarColorHex = "#DC2626",
                    planId = 1,
                    planName = "Monthly Basic",
                    startDate = now - (42 * dayMs),
                    expiryDate = now - (12 * dayMs), // Expired 12 days ago
                    assignedTrainer = null,
                    notes = "Missed renewal notice."
                )
            )
            dao.insertMembers(members)

            // Seed Payments
            val payments = listOf(
                Payment(
                    id = 1,
                    memberId = 1,
                    memberName = "Liam Walker",
                    planId = 2,
                    planName = "Quarterly Pro",
                    amount = 129.00,
                    paymentDate = now - (35 * dayMs),
                    paymentMethod = "Credit/Debit Card",
                    extendedUntilDate = now + (55 * dayMs),
                    receiptNumber = "REC-8041",
                    notes = "Paid via POS Terminal #1"
                ),
                Payment(
                    id = 2,
                    memberId = 2,
                    memberName = "Elena Rostova",
                    planId = 4,
                    planName = "Annual VIP All-Access",
                    amount = 449.00,
                    paymentDate = now - (90 * dayMs),
                    paymentMethod = "UPI/Bank Transfer",
                    extendedUntilDate = now + (275 * dayMs),
                    receiptNumber = "REC-7920",
                    notes = "Annual VIP advance"
                ),
                Payment(
                    id = 3,
                    memberId = 3,
                    memberName = "Marcus Vance",
                    planId = 3,
                    planName = "Half-Year Elite",
                    amount = 239.00,
                    paymentDate = now - (60 * dayMs),
                    paymentMethod = "Credit/Debit Card",
                    extendedUntilDate = now + (120 * dayMs),
                    receiptNumber = "REC-8115",
                    notes = "Card on file"
                ),
                Payment(
                    id = 4,
                    memberId = 4,
                    memberName = "Chloe Davis",
                    planId = 1,
                    planName = "Monthly Basic",
                    amount = 49.00,
                    paymentDate = now - (10 * dayMs),
                    paymentMethod = "Cash",
                    extendedUntilDate = now + (20 * dayMs),
                    receiptNumber = "REC-8240",
                    notes = "Cash desk receipt"
                ),
                Payment(
                    id = 5,
                    memberId = 5,
                    memberName = "David Morales",
                    planId = 1,
                    planName = "Monthly Basic",
                    amount = 49.00,
                    paymentDate = now - (29 * dayMs),
                    paymentMethod = "Credit/Debit Card",
                    extendedUntilDate = now + (1 * dayMs),
                    receiptNumber = "REC-8092"
                )
            )
            dao.insertPayments(payments)

            // Seed Check-Ins (some today, some yesterday)
            val checkIns = listOf(
                CheckIn(
                    id = 1,
                    memberId = 1,
                    memberCode = "FA-1001",
                    memberName = "Liam Walker",
                    planName = "Quarterly Pro",
                    timestamp = now - TimeUnit.MINUTES.toMillis(42),
                    isAllowed = true,
                    statusNote = "Check-In Approved • 55 days remaining"
                ),
                CheckIn(
                    id = 2,
                    memberId = 2,
                    memberCode = "FA-1002",
                    memberName = "Elena Rostova",
                    planName = "Annual VIP All-Access",
                    timestamp = now - TimeUnit.MINUTES.toMillis(85),
                    isAllowed = true,
                    statusNote = "Check-In Approved • VIP Access"
                ),
                CheckIn(
                    id = 3,
                    memberId = 4,
                    memberCode = "FA-1004",
                    memberName = "Chloe Davis",
                    planName = "Monthly Basic",
                    timestamp = now - TimeUnit.HOURS.toMillis(3),
                    isAllowed = true,
                    statusNote = "Check-In Approved • 20 days remaining"
                ),
                CheckIn(
                    id = 4,
                    memberId = 8,
                    memberCode = "FA-1008",
                    memberName = "Samira Khan",
                    planName = "Monthly Basic",
                    timestamp = now - TimeUnit.HOURS.toMillis(5),
                    isAllowed = false,
                    statusNote = "Denied: Membership expired 5 days ago"
                )
            )
            dao.insertCheckIns(checkIns)
        }
    }
}
