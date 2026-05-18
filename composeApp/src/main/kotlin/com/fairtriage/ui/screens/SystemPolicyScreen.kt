package com.fairtriage.ui.screens

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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.fairtriage.core.ScreenState
import com.fairtriage.model.ProductPolicy
import com.fairtriage.repository.KtorProductRepository
import com.fairtriage.repository.ProductRepository
import com.fairtriage.ui.components.DisclaimerText
import com.fairtriage.ui.components.ErrorState
import com.fairtriage.ui.components.FairColors
import com.fairtriage.ui.components.FairTriageTopBar
import com.fairtriage.ui.components.FairTypography
import com.fairtriage.ui.components.LoadingState
import com.fairtriage.ui.components.SectionCardTitle
import com.fairtriage.ui.components.StandardCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SystemPolicyScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { SystemPolicyScreenModel() }
        val state by screenModel.state.collectAsState()

        LaunchedEffect(Unit) {
            screenModel.load()
        }

        Scaffold(
            topBar = { FairTriageTopBar(title = "System policy", subtitle = "Safety, fairness, privacy", onBack = { navigator.pop() }) },
            containerColor = FairColors.ScreenBg
        ) { padding ->
            when (val current = state) {
                is ScreenState.Loading -> LoadingState(modifier = Modifier.padding(padding))
                is ScreenState.Error -> ErrorState(current.message, onRetry = screenModel::load, modifier = Modifier.padding(padding))
                is ScreenState.Success -> PolicyContent(policy = current.data, modifier = Modifier.padding(padding))
            }
        }
    }
}

@Composable
private fun PolicyContent(policy: ProductPolicy, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            StandardCard {
                SectionCardTitle(policy.product_name, Icons.Default.Policy)
                Text(policy.prototype_disclaimer, style = FairTypography.BodyMedium, color = FairColors.DangerRed)
                Spacer(Modifier.height(8.dp))
                PolicyRow("Clinical control", policy.clinical_control_policy)
                PolicyRow("Scoring", policy.scoring_formula)
                PolicyRow("Fairness", policy.fairness_policy)
                PolicyRow("Privacy", policy.privacy_policy)
                PolicyRow("Offline", policy.offline_policy)
            }
        }
        item {
            StandardCard {
                SectionCardTitle("Safety rules", Icons.Default.Info)
                policy.safety_rules.forEachIndexed { index, rule ->
                    Text("${index + 1}. $rule", style = FairTypography.BodyMedium, color = FairColors.TextPrimary)
                    if (index != policy.safety_rules.lastIndex) HorizontalDivider(color = FairColors.Divider, modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
        item {
            StandardCard {
                SectionCardTitle("Operational thresholds")
                policy.max_waiting_minutes.forEach { (level, minutes) ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(level, style = FairTypography.BodyMedium.copy(fontWeight = FontWeight.Medium), color = FairColors.TextPrimary)
                        Text("$minutes min", style = FairTypography.BodyMedium, color = FairColors.TextSecondary)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text("Audit actions: ${policy.audit_log_actions.joinToString(", ")}", style = FairTypography.LabelSmall, color = FairColors.TextSecondary)
            }
        }
        item { DisclaimerText() }
    }
}

@Composable
private fun PolicyRow(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(label, style = FairTypography.LabelSmall.copy(fontWeight = FontWeight.Medium), color = FairColors.TextHint)
            Spacer(Modifier.height(3.dp))
            Text(value, style = FairTypography.BodyMedium, color = FairColors.TextPrimary)
        }
    }
}

class SystemPolicyScreenModel(
    private val repository: ProductRepository = KtorProductRepository()
) : ScreenModel {
    private val _state = MutableStateFlow<ScreenState<ProductPolicy>>(ScreenState.Loading)
    val state: StateFlow<ScreenState<ProductPolicy>> = _state.asStateFlow()

    fun load() {
        screenModelScope.launch {
            _state.value = ScreenState.Loading
            try {
                _state.value = ScreenState.Success(repository.getPolicy())
            } catch (e: Throwable) {
                _state.value = ScreenState.Error(e.message ?: "Unable to load system policy.")
            }
        }
    }
}
