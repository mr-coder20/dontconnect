package don.t.connect.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import don.t.connect.utils.AdiveryAdManager
import don.t.connect.utils.AdiveryBannerAd
import don.t.connect.viewmodel.InflationViewModel
import don.t.connect.viewmodel.CalculationResult
import don.t.connect.viewmodel.SettingsViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InflationScreen(
    viewModel: InflationViewModel,
    settingsViewModel: SettingsViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val settingsState by settingsViewModel.state.collectAsStateWithLifecycle()
    val inflationRates = listOf(50, 100, 150, 200, 250, 300, 400, 500)
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isEnglish = configuration.locales[0]?.language == "en"

    val pricePlaceholder = remember(isEnglish) { if (isEnglish) "Current price (Toman)" else "قیمت فعلی کالا (تومان)" }
    val savingPlaceholder = remember(isEnglish) { if (isEnglish) "Monthly savings (Toman)" else "پس‌انداز ماهانه (تومان)" }
    val currencyText = remember(isEnglish) { if (isEnglish) "Toman" else "تومان" }

    // آماده‌سازی تبلیغ جایزه‌دار برای دکمه محاسبه
    LaunchedEffect(Unit) {
        AdiveryAdManager.prepareRewarded(context)
    }

    Scaffold(
        topBar = {}
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // محتوای اصلی (اسکرول‌دار)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = if (isEnglish) "💰 Purchase Outlook with Inflation" else "💰 چشم انداز خرید کالا بر اساس تورم",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                            OutlinedTextField(
                                value = state.product,
                                onValueChange = { viewModel.updateField("product", it) },
                                label = { Text(if (isEnglish) "Product name (e.g. iPhone 15)" else "نام کالا (مثل آیفون ۱۵)", fontSize = 14.sp,)  },
                                leadingIcon = { Icon(Icons.Default.ShoppingCart, null) },
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = state.price,
                                onValueChange = { newValue ->
                                    val cleaned = newValue.replace(",", "").filter { it.isDigit() }
                                    val formatted = cleaned.reversed().chunked(3).joinToString(",").reversed()
                                    viewModel.updateField("price", formatted)
                                },
                                label = { Text(pricePlaceholder, fontSize = 14.sp) },
                                leadingIcon = { Icon(Icons.Default.PriceCheck, null) },
                                trailingIcon = { Text(currencyText, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded }
                            ) {
                                TextField(
                                    value = "${state.selectedInflation}%",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text(if (isEnglish) "Annual inflation rate" else "نرخ تورم سالانه") },
                                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.TrendingUp, null) },
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
                            OutlinedTextField(
                                value = state.saving,
                                onValueChange = { newValue ->
                                    val cleaned = newValue.replace(",", "").filter { it.isDigit() }
                                    val formatted = cleaned.reversed().chunked(3).joinToString(",").reversed()
                                    viewModel.updateField("saving", formatted)
                                },
                                label = { Text(savingPlaceholder, fontSize = 14.sp) },
                                leadingIcon = { Icon(Icons.Default.Savings, null) },
                                trailingIcon = { Text(currencyText, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            // don/t.connect/screens/InflationScreen.kt

// داخل @Composable InflationScreen، در بخش دکمه محاسبه:

                            Button(
                                onClick = {
                                    if (settingsState.isAdsRemoved) {
                                        viewModel.calculate()
                                    } else {


                                        // فقط تبلیغ میان صفحه‌ای (بدون جایزه‌دار)
                                        AdiveryAdManager.showInterstitialWithCallback(context as Activity) {
                                            viewModel.calculate()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(40.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Calculate, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isEnglish) "🚀 Calculate" else "🚀 شروع محاسبه", fontSize = 16.sp)
                            }
                        }
                    }
                }
            }

            // بنر تبلیغاتی در پایین صفحه (فقط در صورت عدم حذف تبلیغات)
            if (!settingsState.isAdsRemoved) {
                AdiveryBannerAd(
                    placementId = AdiveryAdManager.getBannerPlacementId(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }

    // Result Dialog (بدون تغییر)
    if (state.showResultDialog && state.result != null) {
        var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
        val resultMessage = state.result!!.message(isEnglish)

        AlertDialog(
            onDismissRequest = { viewModel.dismissResultDialog() },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = if (state.result is CalculationResult.Success) {
                        if (isEnglish) "🎉 Calculation Result" else "🎉 نتیجه محاسبه"
                    } else {
                        if (isEnglish) "😭 Result" else "😭 نتیجه محاسبه"
                    },
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                AndroidView(
                    factory = { ctx ->
                        FrameLayout(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                        }
                    },
                    update = { frameLayout ->
                        frameLayout.removeAllViews()
                        val composeView = androidx.compose.ui.platform.ComposeView(frameLayout.context).apply {
                            setContent {
                                MaterialTheme {
                                    ShareableContent(
                                        result = state.result!!,
                                        chartData = state.chartData,
                                        productName = state.product,
                                        isEnglish = isEnglish
                                    )
                                }
                            }
                        }
                        frameLayout.addView(composeView)
                        frameLayout.post {
                            capturedBitmap = composeView.toBitmap()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = { viewModel.dismissResultDialog() }) {
                        Text(if (isEnglish) "Close" else "بستن")
                    }
                    Button(
                        onClick = {
                            val fullCaption = buildFullCaption(context, resultMessage, isEnglish)
                            val bitmap = capturedBitmap
                            if (bitmap != null) shareBitmap(context, bitmap, fullCaption)
                            else shareText(context, fullCaption)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Share, null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isEnglish) "Share Image" else "اشتراک تصویر")
                    }
                }
            }
        )
    }
}


@Composable
fun ShareableContent(
    result: CalculationResult,
    chartData: List<Pair<Int, Pair<Long, Long>>>?,
    productName: String,
    isEnglish: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (isEnglish) "📊 Purchase Report: $productName" else "📊 گزارش خرید $productName",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            if (!chartData.isNullOrEmpty()) {
                EnhancedLineChart(data = chartData, modifier = Modifier.height(200.dp).fillMaxWidth())
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isEnglish) "🔴 Price   🟢 Your Savings" else "🔴 قیمت کالا      🟢 پس‌انداز شما",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = result.message(isEnglish),
                fontSize = 12.sp,        // کاهش سایز
                lineHeight = 16.sp,      // فاصله خطوط (اختیاری)
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text(
                text = "📅 ${java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.getDefault()).format(java.util.Date())}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun buildFullCaption(context: Context, originalMessage: String, isEnglish: Boolean): String {
    val packageName = context.packageName
    val appLink = "https://myket.ir/app/$packageName"
    val inviteText = if (isEnglish) {
        "If you like to calculate too, install this app:"
    } else {
        "اگه تو هم دوست داری محاسبه کنی، این اپلیکیشن رو نصب کن:"
    }
    return "$originalMessage\n\n$inviteText\n$appLink"
}

fun View.toBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    draw(canvas)
    return bitmap
}

private fun shareBitmap(context: Context, bitmap: Bitmap, fullCaption: String) {
    val cacheFolder = File(context.cacheDir, "share_images")
    if (!cacheFolder.exists()) cacheFolder.mkdirs()
    val file = File(cacheFolder, "result_${System.currentTimeMillis()}.png")
    FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, fullCaption)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Result"))
}

private fun shareText(context: Context, fullCaption: String) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, fullCaption)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Result"))
}

@Composable
fun EnhancedLineChart(data: List<Pair<Int, Pair<Long, Long>>>, modifier: Modifier = Modifier) {
    // ... (بدون تغییر)
    if (data.isEmpty()) return

    val maxMonth = data.maxOf { it.first }
    val maxPrice = data.maxOf { it.second.first }
    val maxSaving = data.maxOf { it.second.second }
    val maxValue = maxOf(maxPrice, maxSaving).toDouble()

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val paddingLeft = 60f
        val paddingRight = 40f
        val paddingTop = 40f
        val paddingBottom = 60f
        val graphWidth = width - paddingLeft - paddingRight
        val graphHeight = height - paddingTop - paddingBottom
        val startX = paddingLeft
        val startY = paddingTop
        val endX = width - paddingRight
        val endY = height - paddingBottom

        // Grid lines
        val ySteps = 5
        for (i in 0..ySteps) {
            val y = endY - (i * graphHeight / ySteps)
            val value = (maxValue * i / ySteps)
            drawLine(Color.Gray.copy(alpha = 0.3f), Offset(startX, y), Offset(endX, y), 1f)
            val formattedValue = formatDecimal(value)
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = Color.Gray.toArgb()
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
                drawText(formattedValue, startX - 8, y + 10, paint)
            }
        }

        val xSteps = 6
        for (i in 0..xSteps) {
            val month = (maxMonth * i / xSteps)
            val x = startX + (i * graphWidth / xSteps)
            drawLine(Color.Gray.copy(alpha = 0.3f), Offset(x, startY), Offset(x, endY), 1f)
            val text = if (month >= 12) {
                val years = month / 12
                val rem = month % 12
                if (rem == 0) "${years}y" else "${years}y ${rem}m"
            } else {
                "${month}m"
            }
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = Color.Gray.toArgb()
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText(text, x, endY + 30, paint)
            }
        }

        fun xValue(month: Int): Float = startX + (month.toFloat() / maxMonth) * graphWidth
        fun yValue(value: Long): Float = (endY - (value.toDouble() / maxValue) * graphHeight).toFloat()

        val pricePath = Path()
        var first = true
        for ((month, pair) in data) {
            val x = xValue(month)
            val y = yValue(pair.first)
            if (first) {
                pricePath.moveTo(x, y)
                first = false
            } else {
                pricePath.lineTo(x, y)
            }
        }
        drawPath(pricePath, Color.Red, style = Stroke(3f))
        for ((month, pair) in data) {
            val x = xValue(month)
            val y = yValue(pair.first)
            drawCircle(Color.Red, radius = 6f, center = Offset(x, y))
        }

        val savingPath = Path()
        first = true
        for ((month, pair) in data) {
            val x = xValue(month)
            val y = yValue(pair.second)
            if (first) {
                savingPath.moveTo(x, y)
                first = false
            } else {
                savingPath.lineTo(x, y)
            }
        }
        drawPath(savingPath, Color.Green, style = Stroke(3f))
        for ((month, pair) in data) {
            val x = xValue(month)
            val y = yValue(pair.second)
            drawCircle(Color.Green, radius = 6f, center = Offset(x, y))
        }
    }
}

private fun formatDecimal(value: Double): String {
    val rounded = kotlin.math.round(value * 100) / 100.0
    val integerPart = rounded.toLong()
    val fraction = rounded - integerPart
    return if (fraction == 0.0) {
        String.format("%,d", integerPart)
    } else {
        "%,.2f".format(rounded).replace(",", "٬")
    }
}