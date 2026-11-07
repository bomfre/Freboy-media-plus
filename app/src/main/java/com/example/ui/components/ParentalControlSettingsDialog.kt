package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ParentalControlConfig
import com.example.ui.theme.CyanBright
import com.example.ui.theme.GreenActive
import com.example.ui.theme.RedLiveBadge
import com.example.ui.theme.SlateDark700
import com.example.ui.theme.SlateDark800
import com.example.ui.theme.SlateDark900
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.TextPrimary

@Composable
fun ParentalControlSettingsDialog(
    config: ParentalControlConfig,
    availableCategories: List<String>,
    onToggleEnabled: (Boolean) -> Unit,
    onChangePin: (String) -> Unit,
    onToggleCategoryLock: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var showChangePinSection by remember { mutableStateOf(false) }
    var newPinInput by remember { mutableStateOf("") }
    var pinChangeSuccessMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("parental_control_settings_dialog"),
        containerColor = SlateDark800,
        titleContentColor = TextPrimary,
        textContentColor = SlateMuted,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Parental Controls",
                    tint = CyanBright,
                    modifier = Modifier.size(26.dp)
                )
                Text(
                    text = "Parental Controls",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .padding(vertical = 4.dp)
            ) {
                // Enable switch card
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateDark900),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Protection Status",
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (config.isEnabled) "PIN required for locked content" else "All channels and categories open",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (config.isEnabled) GreenActive else SlateMuted
                            )
                        }

                        Switch(
                            checked = config.isEnabled,
                            onCheckedChange = { onToggleEnabled(it) },
                            modifier = Modifier.testTag("parental_enable_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CyanBright,
                                checkedTrackColor = CyanBright.copy(alpha = 0.3f),
                                uncheckedThumbColor = SlateMuted,
                                uncheckedTrackColor = SlateDark700
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Change PIN section
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateDark900),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showChangePinSection = !showChangePinSection },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    tint = CyanBright,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Security PIN",
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = if (showChangePinSection) "Hide" else "Change",
                                style = MaterialTheme.typography.labelMedium,
                                color = CyanBright
                            )
                        }

                        if (showChangePinSection) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newPinInput,
                                onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) newPinInput = it },
                                label = { Text("New 4-digit PIN") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("new_pin_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanBright,
                                    unfocusedBorderColor = SlateDark700,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = {
                                    if (newPinInput.length == 4) {
                                        onChangePin(newPinInput)
                                        pinChangeSuccessMsg = "PIN successfully changed to $newPinInput"
                                        newPinInput = ""
                                    }
                                },
                                enabled = newPinInput.length == 4,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("save_pin_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanBright)
                            ) {
                                Text("Save New PIN", color = SlateDark900, fontWeight = FontWeight.Bold)
                            }

                            pinChangeSuccessMsg?.let {
                                Text(
                                    text = it,
                                    color = GreenActive,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Locked Categories heading
                Text(
                    text = "Lock Content Categories (${config.lockedCategories.size} locked)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "Checking a category will lock all of its channels behind PIN",
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateMuted
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Categories list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateDark900)
                        .padding(horizontal = 8.dp)
                ) {
                    items(availableCategories) { category ->
                        val isLocked = config.lockedCategories.contains(category)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleCategoryLock(category) }
                                .padding(vertical = 6.dp, horizontal = 4.dp)
                                .testTag("cat_lock_$category"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isLocked,
                                onCheckedChange = { onToggleCategoryLock(category) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = CyanBright,
                                    uncheckedColor = SlateMuted,
                                    checkmarkColor = SlateDark900
                                )
                            )
                            Text(
                                text = category,
                                color = if (isLocked) CyanBright else TextPrimary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isLocked) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            if (isLocked) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = CyanBright,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.testTag("parental_done_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CyanBright)
            ) {
                Text("Done", color = SlateDark900, fontWeight = FontWeight.Bold)
            }
        }
    )
}
