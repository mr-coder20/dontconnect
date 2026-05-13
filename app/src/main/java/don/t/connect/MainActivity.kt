package don.t.connect

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adivery.sdk.Adivery
import don.t.connect.data.DataStoreManager
import don.t.connect.navigation.AppNavigation
import don.t.connect.ui.theme.DontConnectTheme
import don.t.connect.utils.AdiveryAdManager
import don.t.connect.utils.AdiveryBannerAd
import don.t.connect.utils.MyketPurchaseManager
import don.t.connect.viewmodel.FakeVpnViewModel
import don.t.connect.viewmodel.InflationViewModel
import don.t.connect.viewmodel.SettingsViewModel
import kotlinx.coroutines.runBlocking
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)


        AdiveryAdManager.initialize(application)
        MyketPurchaseManager.initialize(applicationContext)

        Adivery.setLoggingEnabled(true)

        val dataStore = DataStoreManager(applicationContext)
        val inflationViewModel = InflationViewModel(dataStore)
        val fakeVpnViewModel = FakeVpnViewModel()
        val settingsViewModel = SettingsViewModel(application)

        // یک بار زبان ذخیره‌شده را اعمال کن
        val savedLang = runBlocking {
            settingsViewModel.getSavedLanguage()
        }
        setAppLocale(savedLang)

        setContent {
            val isDarkTheme by settingsViewModel.isDarkThemeFlow.collectAsStateWithLifecycle(
                initialValue = false
            )

            LaunchedEffect(isDarkTheme) {
                setupSystemBars(isDarkTheme)
            }

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
    }

    private fun setAppLocale(languageCode: String) {
        val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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

    @Suppress("DEPRECATION")
    private fun setupSystemBars(isDarkTheme: Boolean) {
        val window = window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)

        if (isDarkTheme) {
            window.statusBarColor = android.graphics.Color.parseColor("#2C2C2C")
            insetsController.isAppearanceLightStatusBars = false
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            insetsController.isAppearanceLightNavigationBars = false
        } else {
            window.statusBarColor = android.graphics.Color.WHITE
            insetsController.isAppearanceLightStatusBars = true
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            insetsController.isAppearanceLightNavigationBars = true
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        MyketPurchaseManager.dispose()
    }

}

@Preview(showBackground = true)
@Composable
fun EmptyPreview() {
    DontConnectTheme { }
}
