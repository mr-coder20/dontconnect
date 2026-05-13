package don.t.connect.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import don.t.connect.utils.AdiveryAdManager
import don.t.connect.utils.AdiveryBannerAd
import don.t.connect.viewmodel.FakeVpnViewModel
import don.t.connect.viewmodel.SettingsViewModel
import don.t.connect.viewmodel.VpnStatus
import kotlinx.coroutines.delay

private const val TAG = "FakeVpnScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FakeVpnScreen(
    viewModel: FakeVpnViewModel,
    settingsViewModel: SettingsViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val settingsState by settingsViewModel.state.collectAsStateWithLifecycle()
    var rotation by remember { mutableStateOf(0f) }
    var isRotating by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val isEnglish = configuration.locales[0]?.language == "en"

    // آماده‌سازی تبلیغ جایزه‌دار و لاگ
    LaunchedEffect(Unit) {
        Log.d(TAG, "Preparing rewarded ad...")
        AdiveryAdManager.prepareRewarded(context)
    }

    LaunchedEffect(state.status) {
        isRotating = (state.status == VpnStatus.CONNECTING)
        if (isRotating) {
            rotation = 0f
            while (isRotating) {
                delay(16)
                rotation = (rotation + 10) % 360
            }
        } else {
            rotation = 0f
        }
    }

    val connectingText = remember(state.progress) {
        when (state.progress) {
            in 0..25 -> if (isEnglish) "Testing internet..." else "تست اینترنت..."
            in 26..50 -> if (isEnglish) "Checking server..." else "بررسی سرور..."
            in 51..75 -> if (isEnglish) "Connecting to server..." else "اتصال به سرور..."
            else -> if (isEnglish) "Authenticating..." else "احراز هویت..."
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Fastfood,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (settingsState.isAdsRemoved) {
                                if (isEnglish) "Ads removed - Thanks!" else "تبلیغات حذف شد - مرسی!"
                            } else {
                                if (isEnglish) "Dev is hungry! ❤️" else "برنامه‌نویس گرسنه است! ❤️"
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        if (!settingsState.isAdsRemoved) {
                            Button(
                                onClick = { settingsViewModel.purchaseRemoveAds({}, {}) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(32.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(Icons.Default.Payment, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(if (isEnglish) "Support" else "حمایت", fontSize = 12.sp)
                            }
                        } else {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.Green,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // محتوای اصلی (اسکرول‌دار)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(40.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                    ),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Icon(
                            imageVector = when (state.status) {
                                VpnStatus.CONNECTING -> Icons.Default.VpnKey
                                else -> Icons.Default.Lock
                            },
                            contentDescription = null,
                            modifier = Modifier
                                .size(90.dp)
                                .rotate(rotation),
                            tint = if (state.status == VpnStatus.CONNECTING) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        when (state.status) {
                            VpnStatus.CONNECTING -> {
                                Text(
                                    text = connectingText,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                                LinearProgressIndicator(
                                    progress = { state.progress / 100f },
                                    modifier = Modifier.fillMaxWidth(),
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text("${state.progress}%", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }

                            VpnStatus.CONNECTED -> {
                                Text(
                                    text = if (isEnglish) "⚡ Connected!" else "⚡ متصل شد!",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    SpeedCard(
                                        label = if (isEnglish) "Download" else "دانلود",
                                        speed = state.downloadSpeed,
                                        unit = "Mbps",
                                        icon = Icons.Default.ArrowDownward
                                    )
                                    SpeedCard(
                                        label = if (isEnglish) "Upload" else "آپلود",
                                        speed = state.uploadSpeed,
                                        unit = "Mbps",
                                        icon = Icons.Default.ArrowUpward
                                    )
                                }
                                Text(
                                    text = state.funnyMessage,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                Button(
                                    onClick = { viewModel.disconnect() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFD32F2F),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(40.dp),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                                ) {
                                    Icon(Icons.Default.PowerSettingsNew, null, modifier = Modifier.size(22.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = if (isEnglish) "DISCONNECT" else "قطع اتصال",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            VpnStatus.IDLE, VpnStatus.DISCONNECTED -> {
                                Text(
                                    text = if (isEnglish) "🔌 Disconnected" else "🔌 قطع است",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = {
                                        if (settingsState.isAdsRemoved) {
                                            Log.d(TAG, "Ads removed, connecting directly")
                                            viewModel.connect()
                                        } else {
                                            Log.d(TAG, "Attempting to show rewarded ad")
                                            AdiveryAdManager.showRewardedOrInterstitialWithCallback(context as Activity) {
                                                viewModel.connect()
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(40.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(4.dp)
                                ) {
                                    Icon(Icons.Default.VpnKey, null, modifier = Modifier.size(22.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        if (isEnglish) "Connect" else "اتصال",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ✅ بنر در پایین صفحه – وسط‌چین و چسبیده
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
}

@Composable
fun SpeedCard(label: String, speed: Double, unit: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier
            .width(110.dp)
            .height(80.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = String.format("%.2f", speed),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(text = unit, fontSize = 11.sp)
            Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}