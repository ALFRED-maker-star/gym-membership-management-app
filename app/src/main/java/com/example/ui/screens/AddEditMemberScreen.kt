package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SportsGymnastics
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GymPlan
import com.example.data.model.Member
import com.example.ui.components.MemberAvatar
import com.example.ui.theme.Charcoal800
import com.example.ui.theme.Charcoal900
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.ElectricLimeDark
import com.example.ui.theme.ElectricLimeSoft
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddEditMemberScreen(
    existingMember: Member?,
    plans: List<GymPlan>,
    onSave: (
        id: Long,
        name: String,
        phone: String,
        email: String,
        plan: GymPlan,
        startDate: Long,
        trainer: String?,
        notes: String?
    ) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(existingMember?.fullName ?: "") }
    var phone by remember { mutableStateOf(existingMember?.phone ?: "") }
    var email by remember { mutableStateOf(existingMember?.email ?: "") }
    var selectedPlanId by remember {
        mutableLongStateOf(
            existingMember?.planId ?: plans.firstOrNull()?.id ?: 1L
        )
    }
    var startDateMs by remember {
        mutableLongStateOf(existingMember?.startDate ?: System.currentTimeMillis())
    }
    var assignedTrainer by remember {
        mutableStateOf(existingMember?.assignedTrainer ?: "None")
    }
    var notes by remember { mutableStateOf(existingMember?.notes ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val trainerOptions = listOf("None", "Coach Marcus", "Coach Elena", "Coach David")

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("add_edit_member_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Avatar Preview Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Charcoal900),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val initials = name.split(" ")
                    .filter { it.isNotBlank() }
                    .take(2)
                    .map { it.first().uppercase() }
                    .joinToString("")
                    .ifEmpty { "MB" }

                MemberAvatar(
                    initials = initials,
                    colorHex = plans.find { it.id == selectedPlanId }?.colorHex ?: "#84CC16",
                    size = 56
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = if (existingMember == null) "New Member Profile" else "Edit Member Profile",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = PureWhite
                    )
                    Text(
                        text = if (existingMember == null) "Fill details to issue membership" else "ID: ${existingMember.memberCode}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ElectricLime
                    )
                }
            }
        }

        // Section: Personal Information
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "MEMBER INFORMATION",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    ),
                    color = Slate500
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("Full Name *") },
                    placeholder = { Text("e.g. Jordan Hayes") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Slate500) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Charcoal900,
                        unfocusedBorderColor = Slate200
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_member_name")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number *") },
                    placeholder = { Text("+1 (555) 000-0000") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Slate500) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Charcoal900,
                        unfocusedBorderColor = Slate200
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_member_phone")
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    placeholder = { Text("jordan@example.com") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Slate500) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Charcoal900,
                        unfocusedBorderColor = Slate200
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_member_email")
                )
            }
        }

        // Section: Plan Selection
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
                    text = "MEMBERSHIP PLAN *",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    ),
                    color = Slate500
                )

                plans.forEach { plan ->
                    val isSelected = selectedPlanId == plan.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPlanId = plan.id }
                            .testTag("plan_select_${plan.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) ElectricLimeSoft else Slate100
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedPlanId = plan.id },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Charcoal900,
                                        unselectedColor = Slate400
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = plan.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Charcoal900
                                    )
                                    Text(
                                        text = "${plan.durationMonths} month(s) • ${plan.description}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate700
                                    )
                                }
                            }

                            Text(
                                text = plan.formattedPrice,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = Charcoal900
                            )
                        }
                    }
                }
            }
        }

        // Section: Start Date & Trainer
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "START DATE & COACHING",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    ),
                    color = Slate500
                )

                // Date indicator
                val formattedStart = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(startDateMs))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate100)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Charcoal900)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "Start Date", style = MaterialTheme.typography.labelSmall, color = Slate500)
                            Text(text = formattedStart, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Charcoal900)
                        }
                    }

                    OutlinedButton(
                        onClick = { startDateMs = System.currentTimeMillis() },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Set Today", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }

                // Trainer Selector
                Text(
                    text = "Assigned Trainer (Optional)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Charcoal900
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    trainerOptions.forEach { trainer ->
                        val isSelected = assignedTrainer == trainer
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Charcoal900 else Slate100)
                                .border(1.dp, if (isSelected) Charcoal900 else Slate200, RoundedCornerShape(10.dp))
                                .clickable { assignedTrainer = trainer }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = trainer.replace("Coach ", ""),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) ElectricLime else Slate700
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Desk Notes (Medical, fitness goals, etc.)") },
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null, tint = Slate500) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Charcoal900,
                        unfocusedBorderColor = Slate200
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_member_notes"),
                    minLines = 2
                )
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFEF4444)
            )
        }

        // Action Buttons: Save & Cancel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("btn_cancel_member"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel", fontWeight = FontWeight.Bold, color = Slate700)
            }

            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Please enter member's full name"
                        return@Button
                    }
                    if (phone.isBlank()) {
                        errorMessage = "Please enter member's phone number"
                        return@Button
                    }
                    val plan = plans.find { it.id == selectedPlanId } ?: plans.first()
                    onSave(
                        existingMember?.id ?: 0L,
                        name,
                        phone,
                        email,
                        plan,
                        startDateMs,
                        assignedTrainer,
                        notes
                    )
                },
                modifier = Modifier
                    .weight(1.5f)
                    .height(52.dp)
                    .testTag("btn_save_member"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricLime,
                    contentColor = Charcoal900
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (existingMember == null) "SAVE & ENROLL" else "UPDATE MEMBER",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
