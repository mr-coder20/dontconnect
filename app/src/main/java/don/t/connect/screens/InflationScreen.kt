package don.t.connect.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import don.t.connect.viewmodel.InflationViewModel
import don.t.connect.viewmodel.CalculationResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InflationScreen(viewModel: InflationViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val inflationRates = listOf(50, 100, 150, 200, 250, 300, 400, 500)
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("📉 حساب تورم مسخره", fontSize = 20.sp, fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "💰 چقدر می‌تونی پس‌انداز کنی؟",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // نام کالا
                        OutlinedTextField(
                            value = state.product,
                            onValueChange = { viewModel.updateField("product", it) },
                            label = { Text("نام کالا (مثل آیفون ۱۵)") },
                            leadingIcon = { Icon(Icons.Default.ShoppingCart, null) },
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // قیمت کالا با جداکننده هزارگان
                        OutlinedTextField(
                            value = state.price,
                            onValueChange = { newValue ->
                                val cleaned = newValue.replace(",", "").filter { it.isDigit() }
                                val formatted = cleaned.reversed().chunked(3).joinToString(",").reversed()
                                viewModel.updateField("price", formatted)
                            },
                            label = { Text("قیمت فعلی کالا (تومان)") },
                            leadingIcon = { Icon(Icons.Default.PriceCheck, null) },
                            trailingIcon = { Text("تومان", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        // Dropdown نرخ تورم
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            TextField(
                                value = "${state.selectedInflation}%",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("نرخ تورم سالانه") },
                                leadingIcon = { Icon(Icons.Default.TrendingUp, null) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true)
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                inflationRates.forEach { rate ->
                                    DropdownMenuItem(
                                        text = { Text("$rate%") },
                                        onClick = {
                                            viewModel.setInflation(rate)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // پس‌انداز ماهانه
                        OutlinedTextField(
                            value = state.saving,
                            onValueChange = { newValue ->
                                val cleaned = newValue.replace(",", "").filter { it.isDigit() }
                                val formatted = cleaned.reversed().chunked(3).joinToString(",").reversed()
                                viewModel.updateField("saving", formatted)
                            },
                            label = { Text("پس‌انداز ماهانه (تومان)") },
                            leadingIcon = { Icon(Icons.Default.Savings, null) },
                            trailingIcon = { Text("تومان", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        Button(
                            onClick = { viewModel.calculate() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(30.dp)
                        ) {
                            Icon(Icons.Default.Calculate, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("🚀 شروع محاسبه", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }

    // دیالوگ نتیجه (با اشتراک‌گذاری متن)
    if (state.showResultDialog && state.result != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissResultDialog() },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = if (state.result is CalculationResult.Success) "🎉 نتیجه محاسبه" else "😭 نتیجه محاسبه",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = state.result!!.message,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    if (!state.chartData.isNullOrEmpty()) {
                        SimpleLineChart(data = state.chartData!!, modifier = Modifier.height(150.dp))
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = { viewModel.dismissResultDialog() }) {
                        Text("بستن")
                    }
                    Button(
                        onClick = {
                            shareResultAsText(context, state.result!!.message)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Share, null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("اشتراک‌گذاری نتیجه")
                    }
                }
            }
        )
    }
}

// تابع اشتراک‌گذاری متن نتیجه
private fun shareResultAsText(context: Context, message: String) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "📊 نتیجه محاسبه تورم:\n\n$message\n\n📱 اپلیکیشن «وصل نشو»")
    }
    context.startActivity(Intent.createChooser(shareIntent, "اشتراک‌گذاری نتیجه با دوستان"))
}

// نمودار خطی
@Composable
fun SimpleLineChart(data: List<Pair<Int, Pair<Long, Long>>>, modifier: Modifier = Modifier) {
    if (data.isEmpty()) return
    val maxMonth = data.maxOf { it.first }
    val maxPrice = data.maxOf { it.second.first }
    val maxSaving = data.maxOf { it.second.second }
    val maxValue = maxOf(maxPrice, maxSaving).toFloat()

    Canvas(modifier = modifier.fillMaxWidth()) {
        val width = size.width
        val height = size.height
        val stepX = width / maxMonth
        fun valueToY(value: Long) = height * (1 - value.toFloat() / maxValue)

        // خط قیمت (قرمز)
        val pricePath = Path()
        var first = true
        for ((month, pair) in data) {
            val x = (month - 1) * stepX
            val y = valueToY(pair.first)
            if (first) { pricePath.moveTo(x, y); first = false }
            else pricePath.lineTo(x, y)
        }
        drawPath(pricePath, Color.Red, style = Stroke(4f))

        // خط پس‌انداز (سبز)
        val savingPath = Path()
        first = true
        for ((month, pair) in data) {
            val x = (month - 1) * stepX
            val y = valueToY(pair.second)
            if (first) { savingPath.moveTo(x, y); first = false }
            else savingPath.lineTo(x, y)
        }
        drawPath(savingPath, Color.Green, style = Stroke(4f))
    }
}