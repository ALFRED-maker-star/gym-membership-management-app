package com.example.ui.screens

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GymPlan
import com.example.data.model.Member
import com.example.ui.theme.Charcoal900
import com.example.ui.theme.DangerRed
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
fun PlansSetupScreen(
    plans: List<GymPlan>,
    members: List<Member>,
    onSavePlan: (id: Long, name: String, durationMonths: Int, price: Double, description: String, colorHex: String) -> Unit,
    onDeletePlan: (GymPlan) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingPlan by remember { mutableStateOf<GymPlan?>(null) }
    var planToDelete by remember { mutableStateOf<GymPlan?>(null) }

    // Dialog for adding or editing a plan
    if (showAddEditDialog) {
        var name by remember { mutableStateOf(editingPlan?.name ?: "") }
        var durationStr by remember { mutableStateOf(editingPlan?.durationMonths?.toString() ?: "1") }
        var priceStr by remember { mutableStateOf(editingPlan?.price?.toString() ?: "49.00") }
        var description by remember { mutableStateOf(editingPlan?.description ?: "") }
        var selectedColor by remember { mutableStateOf(editingPlan?.colorHex ?: "#84CC16") }
        var dialogError by remember { mutableStateOf<String?>(null) }

        val colorPresets = listOf("#84CC16", "#3B82F6", "#F59E0B", "#8B5CF6", "#EC4899", "#10B981")

        AlertDialog(
            onDismissRequest = { showAddEditDialog = false },
            title = {
                Text(
                    text = if (editingPlan == null) "Create Membership Plan" else "Edit Plan",
                    fontWeight = FontWeight.Bold,
                    color = Charcoal900
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Plan Name *") },
                        placeholder = { Text("e.g. VIP All-Access") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Charcoal900,
                            unfocusedBorderColor = Slate200
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("input_plan_name")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = durationStr,
                            onValueChange = { durationStr = it },
                            label = { Text("Duration (Months) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Charcoal900,
                                unfocusedBorderColor = Slate200
                            ),
                            modifier = Modifier.weight(1f).testTag("input_plan_duration")
                        )

                        OutlinedTextField(
                            value = priceStr,
                            onValueChange = { priceStr = it },
                            label = { Text("Price ($) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Charcoal900,
                                unfocusedBorderColor = Slate200
                            ),
                            modifier = Modifier.weight(1f).testTag("input_plan_price")
                        )
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Plan Benefits / Description") },
                        placeholder = { Text("e.g. Full floor access, sauna, locker...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Charcoal900,
                            unfocusedBorderColor = Slate200
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("input_plan_description"),
                        minLines = 2
                    )

                    Text(
                        text = "Accent Color",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = Slate700
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        colorPresets.forEach { hex ->
                            val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { ElectricLime }
                            val isSelected = selectedColor == hex
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) Charcoal900 else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = hex }
                            )
                        }
                    }

                    if (dialogError != null) {
                        Text(text = dialogError ?: "", color = DangerRed, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val duration = durationStr.toIntOrNull()
                        val price = priceStr.toDoubleOrNull()
                        if (name.isBlank()) {
                            dialogError = "Plan name is required"
                            return@Button
                        }
                        if (duration == null || duration <= 0) {
                            dialogError = "Enter valid positive months duration"
                            return@Button
                        }
                        if (price == null || price < 0) {
                            dialogError = "Enter valid price amount"
                            return@Button
                        }
                        onSavePlan(
                            editingPlan?.id ?: 0L,
                            name,
                            duration,
                            price,
                            description,
                            selectedColor
                        )
                        showAddEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = Charcoal900),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_save_plan_dialog")
                ) {
                    Text("Save Plan", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showAddEditDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = PureWhite
        )
    }

    if (planToDelete != null) {
        val plan = planToDelete!!
        val activeCount = members.count { it.planId == plan.id }
        AlertDialog(
            onDismissRequest = { planToDelete = null },
            title = { Text("Delete Plan?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    if (activeCount > 0)
                        "Warning: There are currently $activeCount member(s) enrolled in ${plan.name}. Are you sure you want to delete this plan?"
                    else
                        "Delete ${plan.name} from gym offerings?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeletePlan(plan)
                        planToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { planToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingPlan = null
                    showAddEditDialog = true
                },
                containerColor = Charcoal900,
                contentColor = ElectricLime,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_plan")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Plan")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "NEW PLAN",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black)
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("plans_setup_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "MEMBERSHIP TIERS & PACKAGES",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    ),
                    color = Slate500
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Configure pricing, billing durations, and package perks",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate700
                )
            }

            items(plans, key = { it.id }) { plan ->
                val activeMembersOnPlan = members.count { it.planId == plan.id }
                val planAccentColor = try {
                    Color(android.graphics.Color.parseColor(plan.colorHex))
                } catch (_: Exception) {
                    ElectricLime
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("plan_card_${plan.id}"),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(planAccentColor)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = plan.name,
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                        color = Charcoal900
                                    )
                                    Text(
                                        text = "${plan.durationMonths} Month(s) Duration",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Slate500
                                    )
                                }
                            }

                            Text(
                                text = plan.formattedPrice,
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                                color = Charcoal900
                            )
                        }

                        if (plan.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = plan.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate700
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Slate100)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$activeMembersOnPlan Active Member(s) Enrolled",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = Charcoal900
                            )

                            Row {
                                IconButton(
                                    onClick = {
                                        editingPlan = plan
                                        showAddEditDialog = true
                                    },
                                    modifier = Modifier.size(32.dp).testTag("btn_edit_plan_${plan.id}")
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Plan", tint = Slate700, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { planToDelete = plan },
                                    modifier = Modifier.size(32.dp).testTag("btn_delete_plan_${plan.id}")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Plan", tint = DangerRed, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
