package don.t.connect.screens

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import don.t.connect.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val state by settingsViewModel.state.collectAsState()
    var languageMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(

    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 20.dp)
        ) {
            // 🤖 کارت تم (حالت شب)
            item {
                SettingsCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (state.currentLanguage == "fa") "حالت شب (چشماتو سیو کن)" else "Dark mode (save your eyes)",
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

            // 🗣️ کارت زبان
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
                                        languageMenuExpanded = false
                                    },
                                    leadingIcon = { if (state.currentLanguage == "fa") Icon(Icons.Default.Check, null) else null }
                                )
                                DropdownMenuItem(
                                    text = { Text("English (just for fun)") },
                                    onClick = {
                                        settingsViewModel.setLanguage("en")
                                        languageMenuExpanded = false
                                    },
                                    leadingIcon = { if (state.currentLanguage == "en") Icon(Icons.Default.Check, null) else null }
                                )
                            }
                        }
                        Text(
                            text = if (state.currentLanguage == "fa") "(اگه زبون رو عوض کردی، برنامه یه چشم به هم زدن خودشو جمع وجور می‌کنه 😉)" else "(If you change language, the app will restart – don't panic 😄)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ⭐ کارت حمایت (فقط نظرسنجی)
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

            // 🍔 کارت نجات برنامه‌نویس از گرسنگی (حذف تبلیغات)
            item {
                SettingsCard {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (state.currentLanguage == "fa") "🍔 نجات برنامه‌نویس از گرسنگی" else "🍔 Save the dev from hunger",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (state.isAdsRemoved) {
                                if (state.currentLanguage == "fa") "🙏 آفرین! برنامه‌نویس رفت نون بخره. چشمت روشن!" else "🙏 Awesome! Dev bought bread. You're a hero!"
                            } else {
                                if (state.currentLanguage == "fa") "🍜 برنامه‌نویس دو هفته‌ست ساندویچ نخورده! با حذف تبلیغات یه لقمه بهش برسون." else "🍜 Dev hasn't eaten a sandwich for 2 weeks. Help by removing ads."
                            },
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = { if (!state.isAdsRemoved) settingsViewModel.purchaseRemoveAds({}, {}) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isAdsRemoved,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.isAdsRemoved) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                                contentColor = if (state.isAdsRemoved) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(32.dp)
                        ) {
                            Icon(
                                if (state.isAdsRemoved) Icons.Default.CheckCircle else Icons.Default.Fastfood,
                                null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (state.isAdsRemoved) {
                                    if (state.currentLanguage == "fa") "✓ تبلیغات حذف شد (مرسی!)" else "✓ Ads removed (thanks!)"
                                } else {
                                    if (state.currentLanguage == "fa") "🍕 حذف تبلیغات + پیتزا برای برنامه‌نویس" else "🍕 Remove ads + pizza for dev"
                                },
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