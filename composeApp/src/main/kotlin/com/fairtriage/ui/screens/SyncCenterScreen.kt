package com.fairtriage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.fairtriage.core.LocalTriageCache
import com.fairtriage.repository.KtorLogRepository
import com.fairtriage.repository.KtorPatientRepository
import com.fairtriage.repository.KtorQueueRepository
import com.fairtriage.ui.components.DisclaimerText
import com.fairtriage.ui.components.FairColors
import com.fairtriage.ui.components.FairTriageTopBar
import com.fairtriage.ui.components.FairTypography
import com.fairtriage.ui.components.SectionCardTitle
import com.fairtriage.ui.components.StandardCard
import kotlinx.coroutines.launch

class SyncCenterScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        var syncing by remember { mutableStateOf(false) }
        var message by remember { mutableStateOf("Ready") }
        var refreshKey by remember { mutableStateOf(0) }

        Scaffold(
            topBar = { FairTriageTopBar(title = "Sync center", subtitle = "Offline queue and cache", onBack = { navigator.pop() }) },
            containerColor = FairColors.ScreenBg
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    StandardCard {
                        SectionCardTitle("Local-first status", Icons.Default.CloudSync)
                        SyncRow("Pending patient creates", LocalTriageCache.pendingCreateCount().toString())
                        SyncRow("Pending overrides", LocalTriageCache.pendingOverrides().size.toString())
                        SyncRow("Pending completions", LocalTriageCache.pendingCompletions().size.toString())
                        SyncRow("Cached queue records", LocalTriageCache.cachedQueue().size.toString())
                        SyncRow("Cached log records", LocalTriageCache.cachedLogs().size.toString())
                        SyncRow("Last cache update", lastSyncText(LocalTriageCache.lastSyncMillis()))
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    syncing = true
                                    message = "Syncing..."
                                    val result = runCatching {
                                        KtorPatientRepository().getPatients()
                                        KtorQueueRepository().getQueue()
                                        KtorLogRepository().getLogs()
                                    }
                                    message = if (result.isSuccess) "Sync completed" else "Sync failed: ${result.exceptionOrNull()?.message ?: "network unavailable"}"
                                    syncing = false
                                    refreshKey += 1
                                }
                            },
                            enabled = !syncing,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FairColors.NavyDark, contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(Modifier.padding(4.dp))
                            Text(if (syncing) "Syncing" else "Sync now")
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(FairColors.InfoBlueBg, RoundedCornerShape(12.dp))
                            .border(1.dp, FairColors.InfoBlueBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(message, style = FairTypography.BodyMedium.copy(fontWeight = FontWeight.Medium), color = FairColors.InfoBlueDark)
                    }
                }
                item { DisclaimerText() }
            }
        }
    }
}

@Composable
private fun SyncRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = FairTypography.BodyMedium, color = FairColors.TextSecondary)
        Text(value, style = FairTypography.BodyMedium.copy(fontWeight = FontWeight.Medium), color = FairColors.TextPrimary)
    }
}

private fun lastSyncText(value: Long): String {
    if (value == 0L) return "Never"
    val ageSeconds = ((System.currentTimeMillis() - value) / 1000).coerceAtLeast(0)
    return when {
        ageSeconds < 60 -> "${ageSeconds}s ago"
        ageSeconds < 3600 -> "${ageSeconds / 60}m ago"
        else -> "${ageSeconds / 3600}h ago"
    }
}
