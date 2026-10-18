package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.SportsGymnastics
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CheckIn
import com.example.data.model.Member
import com.example.data.model.MemberStatus
import com.example.data.model.Payment
import com.example.ui.components.MemberAvatar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.Charcoal800
import com.example.ui.theme.Charcoal900
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedDark
import com.example.ui.theme.DangerRedSoft
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.ElectricLimeDark
import com.example.ui.theme.ElectricLimeSoft
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberSoft
import kotlin.math.max

@Composable
fun MemberProfileScreen(
    member: Member,
    payments: List<Payment>,
    checkIns: List<CheckIn>,
    onRenewClick: (Long) -> Unit,
    onEditClick: (Long) -> Unit,
    onDeleteClick: (Member) -> Unit,
    onCheckInClick: (Member) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val now = System.currentTimeMillis()
    val status = member.getStatus(now)
    val daysRemaining = member.daysRemaining(now)

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Member?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to remove ${member.fullName} (${member.memberCode})? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick(member)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("member_profile_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Photo Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Charcoal900),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            MemberAvatar(
                                initials = member.avatarInitials,
                                colorHex = member.avatarColorHex,
                                size = 64
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = member.fullName,
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                                    color = PureWhite
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = member.memberCode,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = ElectricLime
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    StatusBadge(status = status)
                                }
                            }
                        }

                        // Edit / Delete quick buttons
                        Row {
                            IconButton(
                                onClick = { onEditClick(member.id) },
                                modifier = Modifier.testTag("btn_profile_edit")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Member", tint = Slate400)
                            }
                            IconButton(
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier.testTag("btn_profile_delete")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Member", tint = DangerRed)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Contact action chips (Call, SMS, Email)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${member.phone}"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Charcoal800,
                                contentColor = PureWhite
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("btn_call_member")
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp), tint = ElectricLime)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Call", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:${member.phone}")).apply {
                                    putExtra("sms_body", "Hi ${member.fullName}, greetings from FitAdmin Evolve Gym! Your membership is active until ${member.formattedExpiry}.")
                                }
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Charcoal800,
                                contentColor = PureWhite
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("btn_sms_member")
                        ) {
                            Icon(Icons.Default.Message, contentDescription = null, modifier = Modifier.size(16.dp), tint = ElectricLime)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SMS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        if (member.email.isNotBlank()) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${member.email}"))
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Charcoal800,
                                    contentColor = PureWhite
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("btn_email_member")
                            ) {
                                Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp), tint = ElectricLime)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Email", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Expiry Countdown Card & Quick Actions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "MEMBERSHIP STATUS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.8.sp
                                ),
                                color = Slate500
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = when (status) {
                                    MemberStatus.ACTIVE -> "$daysRemaining Days Remaining"
                                    MemberStatus.EXPIRING_SOON -> "Expiring Soon ($daysRemaining days left!)"
                                    MemberStatus.EXPIRED -> "Expired ${-daysRemaining} Days Ago"
                                },
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black
                                ),
                                color = when (status) {
                                    MemberStatus.ACTIVE -> Charcoal900
                                    MemberStatus.EXPIRING_SOON -> Color(0xFFB45309)
                                    MemberStatus.EXPIRED -> DangerRed
                                }
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when (status) {
                                        MemberStatus.ACTIVE -> ElectricLimeSoft
                                        MemberStatus.EXPIRING_SOON -> WarningAmberSoft
                                        MemberStatus.EXPIRED -> DangerRedSoft
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (status) {
                                    MemberStatus.ACTIVE -> Icons.Default.CheckCircle
                                    MemberStatus.EXPIRING_SOON -> Icons.Default.HourglassTop
                                    MemberStatus.EXPIRED -> Icons.Default.Warning
                                },
                                contentDescription = null,
                                tint = when (status) {
                                    MemberStatus.ACTIVE -> ElectricLimeDark
                                    MemberStatus.EXPIRING_SOON -> Color(0xFFB45309)
                                    MemberStatus.EXPIRED -> DangerRed
                                },
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress bar
                    val totalDuration = max(1L, member.expiryDate - member.startDate)
                    val elapsed = max(0L, now - member.startDate)
                    val progress = (elapsed.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = when (status) {
                            MemberStatus.ACTIVE -> ElectricLime
                            MemberStatus.EXPIRING_SOON -> WarningAmber
                            MemberStatus.EXPIRED -> DangerRed
                        },
                        trackColor = Slate200
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Started: ${member.formattedStartDate}", style = MaterialTheme.typography.bodySmall, color = Slate500)
                        Text(text = "Expires: ${member.formattedExpiry}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Charcoal900)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons: Check In & Renew Membership
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onCheckInClick(member) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_profile_checkin"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Fast Check-In", fontWeight = FontWeight.Bold, color = Charcoal900)
                        }

                        Button(
                            onClick = { onRenewClick(member.id) },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(48.dp)
                                .testTag("btn_profile_renew"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricLime,
                                contentColor = Charcoal900
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("RENEW MEMBERSHIP", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black))
                        }
                    }
                }
            }
        }

        // Plan & Coach Meta Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "PLAN & DESK DETAILS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp
                        ),
                        color = Slate500
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Enrolled Plan", style = MaterialTheme.typography.bodyMedium, color = Slate500)
                        Text(text = member.planName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Charcoal900)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Assigned Trainer", style = MaterialTheme.typography.bodyMedium, color = Slate500)
                        Text(text = member.assignedTrainer ?: "None", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Charcoal900)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Phone Number", style = MaterialTheme.typography.bodyMedium, color = Slate500)
                        Text(text = member.phone, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Charcoal900)
                    }

                    if (!member.notes.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Notes: ${member.notes}", style = MaterialTheme.typography.bodySmall, color = Slate700)
                    }
                }
            }
        }

        // Tabs: Payment History vs Check-In History
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Slate100,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Charcoal900
                    )
                },
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "Payments (${payments.size})",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) Charcoal900 else Slate500
                        )
                    },
                    modifier = Modifier.testTag("tab_payments")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "Check-Ins (${checkIns.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) Charcoal900 else Slate500
                        )
                    },
                    modifier = Modifier.testTag("tab_checkins")
                )
            }
        }

        // History Content
        if (selectedTab == 0) {
            if (payments.isEmpty()) {
                item {
                    Text(
                        text = "No payment records yet for this member.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate500,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            } else {
                items(payments) { payment ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "${payment.planName} • ${payment.paymentMethod}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Charcoal900
                                )
                                Text(
                                    text = "${payment.receiptNumber} • ${payment.formattedDate}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate500
                                )
                                if (!payment.notes.isNullOrBlank()) {
                                    Text(
                                        text = payment.notes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate700
                                    )
                                }
                            }

                            Text(
                                text = payment.formattedAmount,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = Charcoal900
                            )
                        }
                    }
                }
            }
        } else {
            if (checkIns.isEmpty()) {
                item {
                    Text(
                        text = "No check-in history found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate500,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            } else {
                items(checkIns) { checkIn ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(if (checkIn.isAllowed) ElectricLimeSoft else DangerRedSoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (checkIn.isAllowed) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (checkIn.isAllowed) ElectricLimeDark else DangerRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = checkIn.statusNote,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = Charcoal900
                                    )
                                    Text(
                                        text = "${checkIn.formattedDate} at ${checkIn.formattedTime}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate500
                                    )
                                }
                            }

                            Text(
                                text = if (checkIn.isAllowed) "ENTRY OK" else "BLOCKED",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = if (checkIn.isAllowed) ElectricLimeDark else DangerRed
                            )
                        }
                    }
                }
            }
        }
    }
}
