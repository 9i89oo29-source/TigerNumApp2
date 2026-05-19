package com.tigernum.app.ui.instructions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructionsScreen() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("التعليمات") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "كيفية استخدام التطبيق",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            InstructionStep(
                number = "1",
                title = "اختيار المزود والخدمة",
                description = "من الشاشة الرئيسية، اختر مزود الخدمة، ثم الدولة، ثم الخدمة المطلوبة (مثل واتساب، تيليجرام...)."
            )
            InstructionStep(
                number = "2",
                title = "شراء رقم افتراضي",
                description = "اضغط على زر 'شراء رقم'. سيقوم التطبيق بالتواصل مع الخادم وشراء رقم جديد تلقائياً."
            )
            InstructionStep(
                number = "3",
                title = "انتظار الرسالة",
                description = "بعد الشراء، سيظهر الرقم الذي تم شراؤه. انتظر وصول رمز التحقق إلى هذا الرقم."
            )
            InstructionStep(
                number = "4",
                title = "استلام الرمز",
                description = "بمجرد وصول رسالة SMS تحتوي على رمز التحقق، سيظهر الرمز تلقائياً في الشاشة ويمكنك نسخه."
            )
            InstructionStep(
                number = "5",
                title = "مراجعة الطلبات",
                description = "يمكنك الاطلاع على جميع الطلبات السابقة والرموز المستلمة من خلال تبويب 'الطلبات'."
            )
        }
    }
}

@Composable
private fun InstructionStep(number: String, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Column {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
