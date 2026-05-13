package don.t.connect.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import don.t.connect.MainActivity
import don.t.connect.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val state by settingsViewModel.state.collectAsState()
    var languageMenuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        // بدون topBar
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 20.dp)
        ) {
            // کارت تم (حالت شب)
            item {
                SettingsCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (state.currentLanguage == "fa") "حالت شب (چشماتو نجات بده)" else "Dark mode (save your eyes)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Switch(
                            checked = state.isDarkTheme,
                            onCheckedChange = { settingsViewModel.setTheme(it) },
                            thumbContent = {
                                Icon(
                                    if (state.isDarkTheme) Icons.Default.Nightlight else Icons.Default.WbSunny,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }

            // کارت زبان
            item {
                SettingsCard {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (state.currentLanguage == "fa") "زبان (به کدوم زبون حرف بزنیم؟)" else "Language",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (state.currentLanguage == "fa") "متن برنامه به چه زبونی باشه؟" else "Choose app language",
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = { languageMenuExpanded = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Text(if (state.currentLanguage == "fa") "فارسی 🇮🇷" else "English 🇬🇧")
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                            DropdownMenu(
                                expanded = languageMenuExpanded,
                                onDismissRequest = { languageMenuExpanded = false },
                                modifier = Modifier.clip(RoundedCornerShape(16.dp))
                            ) {
                                DropdownMenuItem(
                                    text = { Text("فارسی (مادری)") },
                                    onClick = {
                                        settingsViewModel.setLanguage("fa")
                                        restartApp(context)
                                    },
                                    leadingIcon = { if (state.currentLanguage == "fa") Icon(Icons.Default.Check, null) else null }
                                )
                                DropdownMenuItem(
                                    text = { Text("English (just for fun)") },
                                    onClick = {
                                        settingsViewModel.setLanguage("en")
                                        restartApp(context)
                                    },
                                    leadingIcon = { if (state.currentLanguage == "en") Icon(Icons.Default.Check, null) else null }
                                )
                            }
                        }
                    }
                }
            }

            // کارت حمایت (نظرسنجی)
            item {
                SettingsCard {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (state.currentLanguage == "fa") "🤝 حمایت الکی (برای حال خودت)" else "🤝 Fake support (for your own sake)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Button(
                            onClick = { settingsViewModel.openRatePage() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(Icons.Default.Star, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (state.currentLanguage == "fa") "⭐ ۵ ستاره بده (رایگانه!)" else "⭐ Rate 5 stars (free!)",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 📢 کارت دعوت دوستان (جایگزین کارت حذف تبلیغات)
            item {
                SettingsCard {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (state.currentLanguage == "fa") "📢 دعوت دوستان" else "📢 Invite Friends",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (state.currentLanguage == "fa") "این اپ رو با دوستانت به اشتراک بذار تا اونا هم بتونن محاسبات تورمی انجام بدن!" else "Share this app with friends so they can also do inflation calculations!",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = { shareApp(context, state.currentLanguage == "en") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(32.dp)
                        ) {
                            Icon(Icons.Default.Share, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (state.currentLanguage == "fa") "📤 اشتراک گذاری" else "📤 Share",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // یک اسپیس کوچک در انتها
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun SettingsCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            content = content
        )
    }
}

private fun restartApp(context: Context) {
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    context.startActivity(intent)
    (context as? Activity)?.finish()
}

private fun shareApp(context: Context, isEnglish: Boolean) {
    val packageName = context.packageName
    val appLink = "https://myket.ir/app/$packageName"
    val message = if (isEnglish) {
        "🔥 Wondering when you can afford your dream product with current inflation?\n" +
                "📊 \"Don't Connect\" app is **completely free** and calculates exactly how many months you need to save to reach the inflated price.\n" +
                "😂 It also has a **funny-message VPN feature** (no subscription, totally free) that brings you some laughs!\n" +
                "🎁 Useful & entertaining.\n\n" +
                "Install from Myket: $appLink"
    } else {
        "🔥 می‌خوای بدونی با تورم فعلی، کی می‌تونی اون کالای رویاییت رو بخری؟\n" +
                "📊 اپ «وصل نشو» **کاملاً رایگان**، دقیقاً بهت می‌گه چند ماه باید پس‌انداز کنی تا به قیمت تورمی برسی.\n" +
                "😂 یه ویژگی خاص دیگه هم داره: **فیلترشکن با پیام‌های بامزه** (بدون اشتراک، کاملاً رایگان) که لحظات مفرحی برات میسازه!\n" +
                "🎁 هم مفیده، هم سرگرم‌کننده.\n\n" +
                "نصب کن از مایکت: $appLink"
    }
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
    }
    context.startActivity(Intent.createChooser(shareIntent, if (isEnglish) "Share the app" else "اشتراک برنامه"))
}