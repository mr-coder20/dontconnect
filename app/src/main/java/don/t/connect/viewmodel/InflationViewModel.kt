package don.t.connect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import don.t.connect.data.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.pow

class InflationViewModel(
    private val dataStore: DataStoreManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(InflationUiState())
    val uiState: StateFlow<InflationUiState> = _uiState.asStateFlow()

    fun updateField(field: String, value: String) {
        when (field) {
            "product" -> _uiState.update { it.copy(product = value) }
            "price" -> _uiState.update { it.copy(price = value) }
            "saving" -> _uiState.update { it.copy(saving = value) }
        }
    }

    fun setInflation(rate: Int) {
        _uiState.update { it.copy(selectedInflation = rate) }
    }

    fun calculate() {
        val s = _uiState.value
        val price = s.price.replace(",", "").toLongOrNull() ?: 0
        val saving = s.saving.replace(",", "").toLongOrNull() ?: 0
        if (price <= 0 || saving <= 0 || s.product.isBlank()) {
            _uiState.update {
                it.copy(
                    resultMsg = "🌚 داداش همه فیلدها رو پر کن! قیمت و پس‌انداز رو درست وارد کن.",
                    result = null,
                    chartData = null,
                    showResultDialog = false
                )
            }
            return
        }
        val result = calculateGoal(s.product, price, s.selectedInflation, saving)
        _uiState.update {
            it.copy(
                result = result,
                resultMsg = result.message,
                chartData = result.chartData,
                showResultDialog = true
            )
        }
        viewModelScope.launch {
            dataStore.saveGoal(s.product, price, s.selectedInflation, saving)
        }
    }

    fun dismissResultDialog() {
        _uiState.update { it.copy(showResultDialog = false) }
    }

    private fun calculateGoal(
        product: String,
        price: Long,
        inflationPercent: Int,
        saving: Long
    ): CalculationResult {
        val monthlyRate = (1 + inflationPercent / 100.0).pow(1.0 / 12) - 1
        var currentPrice = price.toDouble()
        var savings = 0.0
        val chart = mutableListOf<Pair<Int, Pair<Long, Long>>>()
        for (month in 1..120) {
            currentPrice *= (1 + monthlyRate)
            savings += saving
            chart.add(month to (currentPrice.toLong() to savings.toLong()))
            if (savings >= currentPrice) {
                return CalculationResult.Success(
                    months = month,
                    finalPrice = currentPrice.toLong(),
                    finalSaving = savings.toLong(),
                    data = chart,
                    productName = product
                )
            }
        }
        return CalculationResult.Unreachable(product)
    }
}

data class InflationUiState(
    val product: String = "",
    val price: String = "",
    val selectedInflation: Int = 100,
    val saving: String = "",
    val result: CalculationResult? = null,
    val resultMsg: String = "",
    val chartData: List<Pair<Int, Pair<Long, Long>>>? = null,
    val showResultDialog: Boolean = false
)

sealed class CalculationResult {
    data class Success(
        val months: Int,
        val finalPrice: Long,
        val finalSaving: Long,
        val data: List<Pair<Int, Pair<Long, Long>>>,
        val productName: String
    ) : CalculationResult()

    data class Unreachable(val productName: String) : CalculationResult()

    val message: String
        get() = when (this) {
            is Success -> {
                val years = months / 12
                val remainingMonths = months % 12
                val timeStr = if (years > 0) "$years سال و $remainingMonths ماه" else "$months ماه"
                "🎉 آفرین! بعد از $timeStr می‌تونی $productName رو بخری!\n" +
                        "💰 قیمت نهایی: ${finalPrice.formatNumber()} تومان\n" +
                        "💸 پس‌انداز نهایی: ${finalSaving.formatNumber()} تومان\n" +
                        "😅 راستی اگه تورم بیشتر بشه، این بازم کمه!"
            }
            is Unreachable -> {
                "😭 حتی بعد ۱۰ سال نمی‌تونی $productName رو بخری! تورم باخت.\n" +
                        "🍕 بهترین راهش اینه که بیای به برنامه‌نویس پیتزا بدی تا یه ربات پولساز برات بنویسه!"
            }
        }

    val chartData: List<Pair<Int, Pair<Long, Long>>>?
        get() = (this as? Success)?.data
}

private fun Long.formatNumber(): String = String.format("%,d", this)