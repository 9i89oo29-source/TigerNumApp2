package com.tigernum.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("الإعدادات") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // بطاقة معرّف الجهاز
            SettingsCard(title = "معرّف الجهاز") {
                Text(
                    text = uiState.deviceId,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // تبديل الوضع الداكن
            SettingsCard(title = "المظهر") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = uiState.isDarkMode,
                            onClick = { viewModel.toggleDarkMode(!uiState.isDarkMode) },
                            role = Role.Switch
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("الوضع الداكن", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = uiState.isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode(it) }
                    )
                }
            }

            // زر إعادة تعيين البيانات
            SettingsCard(title = "البيانات") {
                OutlinedButton(
                    onClick = { viewModel.resetAllData() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("مسح جميع البيانات المحلية")
                }
            }

            // نسخة التطبيق
            SettingsCard(title = "حول التطبيق") {
                Text(
                    text = "TigerNumApp v${uiState.appVersion}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "واجهة أمامية للبوت الخلفي – جميع الحقوق محفوظة.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
