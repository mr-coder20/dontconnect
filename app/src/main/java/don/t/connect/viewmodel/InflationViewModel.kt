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
                    resultMsg = "🌚 همه فیلدها رو پر کن!",
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
                resultMsg = "",  // خالی (پیام موفقیت در UI از طریق result.message(isEnglish) گرفته می‌شود)
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
        val maxMonths = 1200 // 100 سال
        val chart = mutableListOf<Pair<Int, Pair<Long, Long>>>()

        for (month in 1..maxMonths) {
            currentPrice *= (1 + monthlyRate)
            savings += saving

            if (month % 12 == 0 || month == 1) {
                chart.add(month to (currentPrice.toLong() to savings.toLong()))
            }

            if (savings >= currentPrice) {
                if (month % 12 != 0 && month != 1) {
                    chart.add(month to (currentPrice.toLong() to savings.toLong()))
                }
                return CalculationResult.Success(
                    months = month,
                    finalPrice = currentPrice.toLong(),
                    finalSaving = savings.toLong(),
                    data = chart,
                    productName = product
                )
            }
        }
        return CalculationResult.Unreachable(
            productName = product,
            chart = chart
        )
    }
}

data class InflationUiState(
    val product: String = "",
    val price: String = "",
    val selectedInflation: Int = 100,
    val saving: String = "",
    val result: CalculationResult? = null,
    val resultMsg: String = "",   // فقط برای خطاها
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

    data class Unreachable(
        val productName: String,
        val chart: List<Pair<Int, Pair<Long, Long>>>
    ) : CalculationResult()

    fun message(isEnglish: Boolean): String {
        return when (this) {
            is Success -> {
                val years = months / 12
                val remainingMonths = months % 12
                val timeStr = if (years > 0) {
                    if (remainingMonths > 0) "$years سال و $remainingMonths ماه" else "$years سال"
                } else "$months ماه"

                if (isEnglish) {
                    val timeEn = if (years > 0) {
                        if (remainingMonths > 0) "$years years and $remainingMonths months"
                        else "$years years"
                    } else "$months months"
                    "🎉 Damn! After $timeEn you finally got your $productName!\n" +
                            "💰 Final price: ${finalPrice.formatNumber()} Tomans (say goodbye to your savings)\n" +
                            "💸 Total savings: ${finalSaving.formatNumber()} Tomans (you sacrificed a lot)\n" +
                            "📈 But guess what? Inflation just went up again. Good luck next time!"
                } else {
                    "🎉 به زور جیب خالی! بعد از $timeStr بالاخره $productName رو مال خودت کردی!\n" +
                            "💰 قیمت نهایی: ${finalPrice.formatNumber()} تومن (حالا باید چایی خشک بخوری)\n" +
                            "💸 کل پس‌اندازت: ${finalSaving.formatNumber()} تومن (همونایی که واسه روز مبادا گذاشته بودی)\n" +
                            "😂 تورم اما یه نفس راحت نمیده! همین الان دوباره گرون شد.\n" +
                            "📉 بیا یه سری به برنامه‌نویس بزن شاید واست یه معجزه کنه (بعید)!"
                }
            }
            is Unreachable -> {
                if (isEnglish) {
                    "😭 Bro, even after 100 YEARS you can't afford $productName!\n" +
                            "📉 Inflation laughed at your savings: 'Nice try, loser!'\n" +
                            "💸 Your money vs. price → a mouse vs. an elephant.\n" +
                            "😂 Maybe start a OnlyFans? Or find a financial advisor (good luck!).\n" +
                            "💸 Or just accept being broke forever. Welcome to the squad!"
                } else {
                    "😭 داداش، حتی بعد ۱۰۰ سال نمی‌تونی $productName رو بخری! تورم خندید به پولت.\n" +
                            "📉 تورم مثل سایه دنبالته، هرچی پس‌انداز میکنی می‌پره هوا.\n" +
                            "💸 پولت در برابر قیمت کالا مثل موش در برابر فیل شده!\n" +
                            "😂 برو یه مشاور مالی پیدا کن (هرچی زودتر) شاید یه راه حلی داشته باشه!\n" +
                            "💸 یا بیخیال شو و برو یه چیز دیگه بخر (مثلاً یه پاکت تخمه)."
                }
            }
        }
    }

    val chartData: List<Pair<Int, Pair<Long, Long>>>?
        get() = when (this) {
            is Success -> data
            is Unreachable -> chart
        }
}

private fun Long.formatNumber(): String = String.format("%,d", this)