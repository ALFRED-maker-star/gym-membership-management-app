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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.Payment
import com.example.ui.components.MemberAvatar
import com.example.ui.theme.Charcoal800
import com.example.ui.theme.Charcoal900
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.ElectricLimeDark
import com.example.ui.theme.ElectricLimeSoft
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun RecordPaymentScreen(
    members: List<Member>,
    plans: List<GymPlan>,
    preselectedMemberId: Long?,
    onSavePayment: (
        memberId: Long,
        planId: Long,
        amount: Double,
        method: String,
        notes: String?,
        onSuccess: (Payment) -> Unit
    ) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMemberId by remember {
        mutableLongStateOf(preselectedMemberId ?: members.firstOrNull()?.id ?: 0L)
    }
    var memberSearch by remember { mutableStateOf("") }
    var selectedPlanId by remember {
        val initialMember = members.find { it.id == selectedMemberId }
        mutableLongStateOf(initialMember?.planId ?: plans.firstOrNull()?.id ?: 1L)
    }

    val selectedMember = members.find { it.id == selectedMemberId }
    val selectedPlan = plans.find { it.id == selectedPlanId } ?: plans.firstOrNull()

    var amountStr by remember(selectedPlan) {
        mutableStateOf(selectedPlan?.price?.toString() ?: "49.00")
    }
    var selectedMethod by remember { mutableStateOf("Credit/Debit Card") }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var recordedReceipt by remember { mutableStateOf<Payment?>(null) }

    val paymentMethods = listOf("Credit/Debit Card", "Cash", "UPI/Bank Transfer", "Check")

    // Auto-calculate new extended expiry date preview
    val now = System.currentTimeMillis()
    val baseDate = if (selectedMember != null && selectedMember.expiryDate > now) {
        selectedMember.expiryDate
    } else {
        now
    }
    val cal = Calendar.getInstance().apply {
        timeInMillis = baseDate
        add(Calendar.MONTH, selectedPlan?.durationMonths ?: 1)
    }
    val newCalculatedExpiryMs = cal.timeInMillis
    val formattedNewExpiry = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(newCalculatedExpiryMs))

    // Receipt Confirmation Dialog
    if (recordedReceipt != null) {
        val receipt = recordedReceipt!!
        AlertDialog(
            onDismissRequest = onDone,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ElectricLimeSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ElectricLimeDark)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Payment Recorded!", fontWeight = FontWeight.Black, color = Charcoal900)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "Receipt Number: ${receipt.receiptNumber}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Slate500)
                    Text(text = "Member: ${receipt.memberName}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Charcoal900)
                    Text(text = "Plan: ${receipt.planName}", style = MaterialTheme.typography.bodyMedium, color = Slate700)
                    Text(text = "Amount Paid: ${receipt.formattedAmount} (${receipt.paymentMethod})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = ElectricLimeDark)
                    Text(
                        text = "Membership Expiry Auto-Extended to: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(receipt.extendedUntilDate))}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Charcoal900
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onDone,
                    colors = ButtonDefaults.buttonColors(containerColor = Charcoal900, contentColor = PureWhite),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_receipt_done")
                ) {
                    Text("Back to Dashboard", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = PureWhite
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("record_payment_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Charcoal900),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "RECORD MEMBERSHIP PAYMENT",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    ),
                    color = ElectricLime
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Collect dues & instantly renew member access",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate300
                )
            }
        }

        // Section 1: Member Selector
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
                    text = "1. SELECT MEMBER *",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    ),
                    color = Slate500
                )

                // Search field to filter member dropdown
                OutlinedTextField(
                    value = memberSearch,
                    onValueChange = { memberSearch = it },
                    placeholder = { Text("Search member by name or code...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate400) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Charcoal900,
                        unfocusedBorderColor = Slate200
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("payment_member_search")
                )

                val matchingMembers = members.filter {
                    memberSearch.isBlank() ||
                            it.fullName.contains(memberSearch, ignoreCase = true) ||
                            it.memberCode.contains(memberSearch, ignoreCase = true)
                }.take(4)

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    matchingMembers.forEach { member ->
                        val isSelected = selectedMemberId == member.id
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) ElectricLimeSoft else Slate100)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) ElectricLimeDark else Slate200,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    selectedMemberId = member.id
                                    selectedPlanId = member.planId
                                }
                                .padding(10.dp)
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
                                        size = 36
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = member.fullName,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Charcoal900
                                        )
                                        Text(
                                            text = "${member.memberCode} • Current: ${member.formattedExpiry}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate500
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = "Selected", tint = ElectricLimeDark)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Membership Plan & Auto-Expiry Preview
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
                    text = "2. PLAN & EXTENSION PREVIEW *",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    ),
                    color = Slate500
                )

                // Plan cards
                plans.forEach { plan ->
                    val isSelected = selectedPlanId == plan.id
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Slate100 else PureWhite)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Charcoal900 else Slate200,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                selectedPlanId = plan.id
                                amountStr = plan.price.toString()
                            }
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${plan.name} (${plan.durationMonths} mo)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                                ),
                                color = Charcoal900
                            )
                            Text(
                                text = plan.formattedPrice,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = Charcoal900
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Auto-extension Preview Callout
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ElectricLimeSoft)
                        .border(1.dp, ElectricLimeDark, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = ElectricLimeDark, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AUTOMATIC EXPIRY CALCULATION",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                ),
                                color = ElectricLimeDark
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "New Membership Expiry: $formattedNewExpiry",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = Charcoal900
                        )
                        Text(
                            text = if (selectedMember != null && selectedMember.expiryDate > now) {
                                "Extends by +${selectedPlan?.durationMonths ?: 1} month(s) from current active expiry (${selectedMember.formattedExpiry})"
                            } else {
                                "Membership was expired. Extends by +${selectedPlan?.durationMonths ?: 1} month(s) starting today."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate700
                        )
                    }
                }
            }
        }

        // Section 3: Amount, Payment Method, Notes
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
                    text = "3. AMOUNT & METHOD",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    ),
                    color = Slate500
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount Due / Collected ($) *") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null, tint = Slate500) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Charcoal900,
                        unfocusedBorderColor = Slate200
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("payment_amount_input")
                )

                Text(
                    text = "Payment Method",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Charcoal900
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    paymentMethods.forEach { method ->
                        val isSelected = selectedMethod == method
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Charcoal900 else Slate100)
                                .border(1.dp, if (isSelected) Charcoal900 else Slate200, RoundedCornerShape(8.dp))
                                .clickable { selectedMethod = method }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = method.replace("Credit/Debit ", "").replace("/Bank Transfer", ""),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) ElectricLime else Slate700,
                                maxLines = 1
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("POS Receipt / Reference Notes (Optional)") },
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null, tint = Slate500) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Charcoal900,
                        unfocusedBorderColor = Slate200
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("payment_notes_input")
                )
            }
        }

        if (errorMessage != null) {
            Text(text = errorMessage ?: "", color = DangerRed, fontWeight = FontWeight.Bold)
        }

        // Save Button
        Button(
            onClick = {
                val amount = amountStr.toDoubleOrNull()
                if (selectedMemberId == 0L) {
                    errorMessage = "Please select a member"
                    return@Button
                }
                if (amount == null || amount <= 0) {
                    errorMessage = "Please enter a valid payment amount"
                    return@Button
                }

                onSavePayment(
                    selectedMemberId,
                    selectedPlanId,
                    amount,
                    selectedMethod,
                    notes.ifBlank { null }
                ) { receipt ->
                    recordedReceipt = receipt
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("btn_save_payment"),
            colors = ButtonDefaults.buttonColors(
                containerColor = ElectricLime,
                contentColor = Charcoal900
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.CreditCard, contentDescription = null, tint = Charcoal900)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "CONFIRM & SAVE PAYMENT",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
