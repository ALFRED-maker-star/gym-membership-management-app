package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SpaceDashboard
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MemberStatus
import com.example.ui.CheckInResult
import com.example.ui.Screen
import com.example.ui.theme.Charcoal800
import com.example.ui.theme.Charcoal900
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedDark
import com.example.ui.theme.DangerRedSoft
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.ElectricLimeDark
import com.example.ui.theme.ElectricLimeSoft
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberSoft

@Composable
fun FitAdminTopBar(
    title: String,
    subtitle: String? = null,
    canNavigateBack: Boolean = false,
    onNavigateBack: () -> Unit = {},
    staffName: String? = null,
    onLogout: () -> Unit = {},
    actions: @Composable () -> Unit = {}
) {
    Surface(
        color = Charcoal900,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (canNavigateBack) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .testTag("top_bar_back_button")
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PureWhite
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElectricLime),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = "FitAdmin Logo",
                            tint = Charcoal900,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.2).sp
                        ),
                        color = PureWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricLime,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (staffName != null) {
                        Text(
                            text = staffName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate400,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                actions()
                if (staffName != null && !canNavigateBack) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .testTag("top_bar_logout_button")
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Log Out Staff",
                            tint = Slate400
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(
    status: MemberStatus,
    modifier: Modifier = Modifier,
    customText: String? = null
) {
    val (bgColor, textColor, label) = when (status) {
        MemberStatus.ACTIVE -> Triple(
            ElectricLimeSoft,
            ElectricLimeDark,
            customText ?: "ACTIVE"
        )
        MemberStatus.EXPIRING_SOON -> Triple(
            WarningAmberSoft,
            Color(0xFFB45309),
            customText ?: "EXPIRING SOON"
        )
        MemberStatus.EXPIRED -> Triple(
            DangerRedSoft,
            DangerRedDark,
            customText ?: "EXPIRED"
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            ),
            color = textColor
        )
    }
}

@Composable
fun MemberAvatar(
    initials: String,
    colorHex: String,
    size: Int = 44,
    modifier: Modifier = Modifier
) {
    val parsedColor = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (_: Exception) {
        ElectricLime
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Charcoal900)
            .border(2.dp, parsedColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials.take(2).uppercase(),
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = (size * 0.38).sp,
                fontWeight = FontWeight.Black
            ),
            color = PureWhite
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color = ElectricLimeSoft,
    iconTint: Color = ElectricLimeDark,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            ),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp
                    ),
                    color = Slate500
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black
                    ),
                    color = Charcoal900
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate700
                )
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
fun FitAdminBottomNav(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = Charcoal900,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentScreen is Screen.Dashboard,
            onClick = { onNavigate(Screen.Dashboard) },
            icon = {
                Icon(
                    imageVector = Icons.Default.SpaceDashboard,
                    contentDescription = "Dashboard"
                )
            },
            label = { Text("Dashboard", style = MaterialTheme.typography.labelSmall) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Charcoal900,
                selectedTextColor = ElectricLime,
                indicatorColor = ElectricLime,
                unselectedIconColor = Slate400,
                unselectedTextColor = Slate400
            ),
            modifier = Modifier.testTag("nav_dashboard")
        )

        NavigationBarItem(
            selected = currentScreen is Screen.MembersList,
            onClick = { onNavigate(Screen.MembersList) },
            icon = {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = "Members"
                )
            },
            label = { Text("Members", style = MaterialTheme.typography.labelSmall) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Charcoal900,
                selectedTextColor = ElectricLime,
                indicatorColor = ElectricLime,
                unselectedIconColor = Slate400,
                unselectedTextColor = Slate400
            ),
            modifier = Modifier.testTag("nav_members")
        )

        // Prominent Central Check-In Tab
        NavigationBarItem(
            selected = currentScreen is Screen.CheckIn,
            onClick = { onNavigate(Screen.CheckIn) },
            icon = {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (currentScreen is Screen.CheckIn) ElectricLimeDark else ElectricLime),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Check-In",
                        tint = Charcoal900,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            label = {
                Text(
                    "Check-In",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Charcoal900,
                selectedTextColor = ElectricLime,
                indicatorColor = Color.Transparent,
                unselectedIconColor = Charcoal900,
                unselectedTextColor = ElectricLime
            ),
            modifier = Modifier.testTag("nav_checkin")
        )

        NavigationBarItem(
            selected = currentScreen is Screen.ExpiringSoon,
            onClick = { onNavigate(Screen.ExpiringSoon) },
            icon = {
                Icon(
                    imageVector = Icons.Default.HourglassTop,
                    contentDescription = "Expiring"
                )
            },
            label = { Text("Expiring", style = MaterialTheme.typography.labelSmall) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Charcoal900,
                selectedTextColor = ElectricLime,
                indicatorColor = ElectricLime,
                unselectedIconColor = Slate400,
                unselectedTextColor = Slate400
            ),
            modifier = Modifier.testTag("nav_expiring")
        )

        NavigationBarItem(
            selected = currentScreen is Screen.PlansSetup,
            onClick = { onNavigate(Screen.PlansSetup) },
            icon = {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = "Plans"
                )
            },
            label = { Text("Plans", style = MaterialTheme.typography.labelSmall) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Charcoal900,
                selectedTextColor = ElectricLime,
                indicatorColor = ElectricLime,
                unselectedIconColor = Slate400,
                unselectedTextColor = Slate400
            ),
            modifier = Modifier.testTag("nav_plans")
        )
    }
}

/**
 * High-visibility Alert & Animation Dialog for Check-Ins
 * Shows SUCCESS checkmark or EXPIRED warning block with instant renewal flow.
 */
@Composable
fun CheckInResultDialog(
    result: CheckInResult?,
    onDismiss: () -> Unit,
    onRenewClick: (memberId: Long) -> Unit
) {
    if (result == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            if (!result.isAllowed) {
                Button(
                    onClick = {
                        onDismiss()
                        onRenewClick(result.member.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = Charcoal900),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("dialog_renew_membership_button")
                ) {
                    Icon(imageVector = Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Renew Membership Now", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Charcoal900, contentColor = PureWhite),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("dialog_dismiss_button")
                ) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (!result.isAllowed) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Dismiss", color = Slate700)
                }
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (result.isAllowed) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ElectricLimeSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Allowed",
                            tint = ElectricLimeDark,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Check-In Approved!",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = Charcoal900
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(DangerRedSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = "Denied",
                            tint = DangerRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "ACCESS BLOCKED",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = DangerRed
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                // Member Card Preview
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.isAllowed) ElectricLimeSoft.copy(alpha = 0.4f) else DangerRedSoft.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MemberAvatar(
                            initials = result.member.avatarInitials,
                            colorHex = result.member.avatarColorHex,
                            size = 46
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = result.member.fullName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Charcoal900
                            )
                            Text(
                                text = "${result.member.memberCode} • ${result.member.planName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate700
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = result.message,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (!result.isAllowed) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (!result.isAllowed) DangerRed else Charcoal900
                )

                if (!result.isAllowed) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Member cannot access the gym floor until a renewal payment is recorded.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate500
                    )
                }
            }
        },
        containerColor = PureWhite,
        shape = RoundedCornerShape(20.dp)
    )
}
