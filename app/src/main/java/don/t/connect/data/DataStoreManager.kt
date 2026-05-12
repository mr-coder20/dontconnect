package don.t.connect.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "economy_prefs")

class DataStoreManager(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        private val LAST_PRODUCT = stringPreferencesKey("product")
        private val LAST_PRICE = longPreferencesKey("price")
        private val LAST_INFLATION = intPreferencesKey("inflation")
        private val LAST_SAVING = longPreferencesKey("saving")
    }

    suspend fun saveGoal(product: String, price: Long, inflation: Int, saving: Long) {
        dataStore.edit { prefs ->
            prefs[LAST_PRODUCT] = product
            prefs[LAST_PRICE] = price
            prefs[LAST_INFLATION] = inflation
            prefs[LAST_SAVING] = saving
        }
    }

    fun getGoal(): Flow<GoalData?> = dataStore.data.map { prefs ->
        val product = prefs[LAST_PRODUCT] ?: return@map null
        GoalData(
            product = product,
            price = prefs[LAST_PRICE] ?: 0,
            inflation = prefs[LAST_INFLATION] ?: 100,
            saving = prefs[LAST_SAVING] ?: 0
        )
    }
}

data class GoalData(
    val product: String,
    val price: Long,
    val inflation: Int,
    val saving: Long
)