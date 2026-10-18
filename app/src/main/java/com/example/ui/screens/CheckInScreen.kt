package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

@Composable
fun CheckInScreen(
    members: List<Member>,
    recentCheckIns: List<CheckIn>,
    onCheckInMember: (Member) -> Unit,
    onRenewMember: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedMember by remember { mutableStateOf<Member?>(null) }
    var checkInBannerState by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    fun triggerVibration(isSuccess: Boolean) {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (isSuccess) {
                        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        // Double buzz for expired denial
                        val pattern = longArrayOf(0, 150, 100, 250)
                        vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                    }
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(if (isSuccess) 100 else 300)
                }
            }
        } catch (_: Exception) {
            // Ignore vibration if unavailable in runtime
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("check_in_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Hero: Check-In Scanner Simulation & Search
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Charcoal900),
                shape = RoundedCornerShape(20.dp)
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
                        Column {
                            Text(
                                text = "FITADMIN CHECK-IN",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                ),
                                color = ElectricLime
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Turnstile & Desk Entry",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                color = PureWhite
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ElectricLime),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Scanner",
                                tint = Charcoal900,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            val exactMatch = members.find { m ->
                                m.memberCode.equals(it.trim(), ignoreCase = true) ||
                                        m.phone.replace(Regex("[^0-9]"), "").contains(it.trim())
                            }
                            if (exactMatch != null) {
                                selectedMember = exactMatch
                            }
                        },
                        placeholder = { Text("Scan Barcode or Search (FA-1001, name, phone)...", color = Slate400) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ElectricLime) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricLime,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("checkin_search_field")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Tip: Tap any member below to inspect and register instant entry.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                }
            }
        }

        // Live Banner Notification (Animated feedback)
        item {
            AnimatedVisibility(
                visible = checkInBannerState != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                if (checkInBannerState != null) {
                    val (allowed, message) = checkInBannerState!!
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (allowed) ElectricLimeSoft else DangerRedSoft
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (allowed) ElectricLimeDark else DangerRed
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (allowed) Icons.Default.CheckCircle else Icons.Default.Block,
                                contentDescription = null,
                                tint = if (allowed) ElectricLimeDark else DangerRed,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = if (allowed) "ENTRY APPROVED" else "ENTRY DENIED: EXPIRED",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                    color = if (allowed) Charcoal900 else DangerRedDark
                                )
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (allowed) Slate700 else DangerRedDark
                                )
                            }
                        }
                    }
                }
            }
        }

        // Selected Member Verification Card & Action
        if (selectedMember != null) {
            val member = selectedMember!!
            val now = System.currentTimeMillis()
            val status = member.getStatus(now)
            val isExpired = status == MemberStatus.EXPIRED

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("checkin_selected_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isExpired) Color(0xFFFFF1F2) else PureWhite
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 2.dp,
                        color = if (isExpired) DangerRed else ElectricLime
                    ),
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                MemberAvatar(
                                    initials = member.avatarInitials,
                                    colorHex = member.avatarColorHex,
                                    size = 54
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = member.fullName,
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                        color = Charcoal900
                                    )
                                    Text(
                                        text = "${member.memberCode} • ${member.planName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Slate500
                                    )
                                }
                            }

                            StatusBadge(status = status)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Details
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isExpired) DangerRedSoft else Slate100)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "EXPIRY DATE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Slate500
                                )
                                Text(
                                    text = member.formattedExpiry,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isExpired) DangerRed else Charcoal900
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "STATUS REMAINING",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Slate500
                                )
                                val days = member.daysRemaining(now)
                                Text(
                                    text = if (days < 0) "Expired ${-days}d ago" else "$days days left",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                    color = if (isExpired) DangerRed else ElectricLimeDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // If EXPIRED -> Warning Block + RENEW SHORTCUT
                        if (isExpired) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DangerRedSoft)
                                    .border(1.dp, DangerRed, RoundedCornerShape(10.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = DangerRed)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "WARNING: Access blocked! Membership expired. Collect payment before allowing gym entry.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = DangerRedDark
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        triggerVibration(false)
                                        checkInBannerState = Pair(false, "Access Blocked: ${member.fullName}'s membership expired on ${member.formattedExpiry}")
                                        onCheckInMember(member)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = PureWhite),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).height(48.dp).testTag("btn_log_denied_checkin")
                                ) {
                                    Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Log Blocked", fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { onRenewMember(member.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = Charcoal900),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1.3f).height(48.dp).testTag("btn_renew_from_checkin")
                                ) {
                                    Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("RENEW NOW", fontWeight = FontWeight.Black)
                                }
                            }
                        } else {
                            // Active / Expiring Soon: One-Tap High-Energy Check In
                            Button(
                                onClick = {
                                    triggerVibration(true)
                                    val days = member.daysRemaining(now)
                                    checkInBannerState = Pair(true, "${member.fullName} checked in! $days days remaining on ${member.planName}.")
                                    onCheckInMember(member)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("btn_one_tap_checkin"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricLime,
                                    contentColor = Charcoal900
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Charcoal900, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "CONFIRM ENTRY (ONE-TAP)",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Member Search Results & Rapid-Tap Roster
        item {
            Text(
                text = if (searchQuery.isNotBlank()) "SEARCH RESULTS" else "SELECT MEMBER TO CHECK IN",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                ),
                color = Slate500
            )
        }

        val filteredRoster = members.filter {
            searchQuery.isBlank() ||
                    it.fullName.contains(searchQuery, ignoreCase = true) ||
                    it.memberCode.contains(searchQuery, ignoreCase = true) ||
                    it.phone.contains(searchQuery)
        }

        items(filteredRoster, key = { it.id }) { member ->
            val now = System.currentTimeMillis()
            val status = member.getStatus(now)
            val isSelected = selectedMember?.id == member.id

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedMember = member }
                    .testTag("checkin_roster_${member.id}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) ElectricLimeSoft else PureWhite
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) ElectricLimeDark else Slate200
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
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
                            size = 42
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = member.fullName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Charcoal900
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = member.memberCode,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate500
                                )
                            }
                            Text(
                                text = "${member.planName} • Exp: ${member.formattedExpiry}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate700
                            )
                        }
                    }

                    StatusBadge(status = status)
                }
            }
        }
    }
}
