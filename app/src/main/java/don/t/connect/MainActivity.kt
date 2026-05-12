package don.t.connect

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import don.t.connect.data.DataStoreManager
import don.t.connect.navigation.AppNavigation
import don.t.connect.ui.theme.DontConnectTheme
import don.t.connect.viewmodel.FakeVpnViewModel
import don.t.connect.viewmodel.InflationViewModel
import don.t.connect.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Locale

class MainActivity : ComponentActivity() {

    private var isRestarting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val dataStore = DataStoreManager(applicationContext)
        val inflationViewModel = InflationViewModel(dataStore)
        val fakeVpnViewModel = FakeVpnViewModel()
        val settingsViewModel = SettingsViewModel(application)

        // 🔥 1. اعمال زبان ذخیره‌شده به صورت همزمان (قبل از هر چیز دیگر)
        val savedLang = runBlocking {
            settingsViewModel.getSavedLanguage()
        }
        setAppLocale(savedLang)

        // 🔥 2. گوش دادن به تغییرات زبان (با flag جلوگیری از ریستارت مجدد)
        lifecycleScope.launch {
            settingsViewModel.languageFlow
                .distinctUntilChanged()
                .collect { newLang ->
                    val currentLang = resources.configuration.locales[0]?.language ?: "fa"
                    if (currentLang != newLang && !isRestarting) {
                        isRestarting = true
                        restartApp()
                    }
                }
        }

        setContent {
            val isDarkTheme by settingsViewModel.isDarkThemeFlow.collectAsStateWithLifecycle(initialValue = false)
            DontConnectTheme(darkTheme = isDarkTheme, dynamicColor = false) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        inflationViewModel = inflationViewModel,
                        fakeVpnViewModel = fakeVpnViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }

        // تنظیم نوارها با مقدار اولیه تم
        lifecycleScope.launch {
            val initialTheme = settingsViewModel.getInitialTheme()
            setupSystemBars(initialTheme)
        }
    }

    private fun setAppLocale(languageCode: String) {
        val locale = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Locale.Builder().setLanguage(languageCode).build()
        } else {
            @Suppress("DEPRECATION")
            Locale(languageCode)
        }
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun restartApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun setupSystemBars(isDarkTheme: Boolean) {
        val window = window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = !isDarkTheme
        insetsController.isAppearanceLightNavigationBars = !isDarkTheme
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyPreview() {
    DontConnectTheme { }
}