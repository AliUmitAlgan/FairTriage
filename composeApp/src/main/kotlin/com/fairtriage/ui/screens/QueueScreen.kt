package com.fairtriage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.fairtriage.core.ScreenState
import com.fairtriage.model.Patient
import com.fairtriage.screenmodel.QueueScreenModel
import com.fairtriage.ui.components.AppBarStyle
import com.fairtriage.ui.components.EmptyState
import com.fairtriage.ui.components.ErrorState
import com.fairtriage.ui.components.FairColors
import com.fairtriage.ui.components.LoadingState
import com.fairtriage.ui.components.PatientQueueCard
import com.fairtriage.ui.components.ScreenScaffold

class QueueScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { QueueScreenModel() }
        val state by screenModel.state.collectAsState()
        val count = (state as? ScreenState.Success)?.data?.size ?: 0

        ScreenScaffold(
            title = "Patient Queue",
            subtitle = "$count patients waiting",
            showBack = true,
            appBarStyle = AppBarStyle.Blue
        ) { paddingValues ->
            when (val currentState = state) {
                ScreenState.Loading -> LoadingState(modifier = Modifier.padding(paddingValues))
                is ScreenState.Error -> ErrorState(
                    errorMessage = currentState.message,
                    onRetry = screenModel::refresh,
                    modifier = Modifier.padding(paddingValues)
                )
                is ScreenState.Success -> QueueContent(
                    patients = currentState.data,
                    onRefresh = screenModel::refresh,
                    onPatientClick = { patientId -> navigator.push(PatientDetailScreen(patientId)) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun QueueContent(
    patients: List<Patient>,
    onRefresh: () -> Unit,
    onPatientClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (patients.isEmpty()) {
        EmptyState(
            icon = "+",
            title = "No patients in queue",
            hint = "Add a patient or seed demo data.",
            modifier = modifier
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FairColors.Background),
        contentPadding = PaddingValues(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            QueueBanner(patients = patients)
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onRefresh,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("R  Refresh Queue", color = FairColors.PrimaryBlue, fontSize = 13.sp)
                }
            }
        }
        items(patients, key = { patient -> patient.id }) { patient ->
            PatientQueueCard(
                patient = patient,
                onClick = { onPatientClick(patient.id) }
            )
        }
    }
}

@Composable
private fun QueueBanner(patients: List<Patient>) {
    val hasCritical = patients.any { it.triage_level.equals("Critical", ignoreCase = true) }
    val hasUrgent = patients.any { it.triage_level.equals("Urgent", ignoreCase = true) }
    val color = when {
        hasCritical -> FairColors.CriticalRed
        hasUrgent -> FairColors.UrgentOrange
        else -> FairColors.StableGreen
    }
    val text = when {
        hasCritical -> "! CRITICAL PATIENTS PRESENT"
        hasUrgent -> "! URGENT PATIENTS WAITING"
        else -> "All patients stable"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
