package don.t.connect.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import don.t.connect.utils.MyketPurchaseManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private val Application.dataStore by preferencesDataStore(name = "settings_prefs")

data class SettingsState(
    val isDarkTheme: Boolean = false,
    val currentLanguage: String = "fa",
    val isAdsRemoved: Boolean = false
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val dataStore = application.dataStore

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    // تم به صورت زنده (برای MainActivity)
    val isDarkThemeFlow: Flow<Boolean> = dataStore.data
        .map { prefs -> (prefs[THEME_KEY] ?: 1) == 2 }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // زبان به صورت زنده (برای MainActivity)
    val languageFlow: Flow<String> = dataStore.data
        .map { prefs -> prefs[LANGUAGE_KEY] ?: "fa" }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "fa"
        )

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            dataStore.data
                .map { prefs ->
                    SettingsState(
                        isDarkTheme = (prefs[THEME_KEY] ?: 1) == 2,
                        currentLanguage = prefs[LANGUAGE_KEY] ?: "fa",
                        isAdsRemoved = prefs[ADS_REMOVED_KEY] ?: false
                    )
                }
                .distinctUntilChanged()
                .collect { newState ->
                    _state.value = newState
                }
        }
    }

    fun setTheme(isDark: Boolean) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[THEME_KEY] = if (isDark) 2 else 1
            }
        }
    }

    fun setLanguage(lang: String) {
        if (lang == _state.value.currentLanguage) return
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[LANGUAGE_KEY] = lang
            }
        }
    }

    fun purchaseRemoveAds(activity: Activity, onSuccess: () -> Unit) {
        MyketPurchaseManager.launchPurchaseFlow(activity) { success ->
            if (success) {
                viewModelScope.launch {
                    dataStore.edit { prefs ->
                        prefs[ADS_REMOVED_KEY] = true
                    }
                    _state.update { it.copy(isAdsRemoved = true) }
                    onSuccess()
                }
            }
        }
    }

    fun openRatePage() {
        val packageName = context.packageName
        val uri = Uri.parse("myket://comment?id=$packageName")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("ir.mservices.market")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // اگر مایکت نصب نبود، به لینک وب مایکت هدایت شود
            openUrl("https://myket.ir/app/$packageName")
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // توابع یکباره برای خواندن اولیه در MainActivity
    suspend fun getInitialTheme(): Boolean {
        return dataStore.data.map { prefs -> (prefs[THEME_KEY] ?: 1) == 2 }.first()
    }

    suspend fun getSavedLanguage(): String {
        return dataStore.data.map { prefs -> prefs[LANGUAGE_KEY] ?: "fa" }.first()
    }

    companion object {
        private val THEME_KEY = intPreferencesKey("theme")
        private val LANGUAGE_KEY = stringPreferencesKey("language")
        private val ADS_REMOVED_KEY = booleanPreferencesKey("ads_removed")
    }
}