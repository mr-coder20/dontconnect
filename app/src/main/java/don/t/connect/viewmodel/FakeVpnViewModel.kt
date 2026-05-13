package don.t.connect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class FakeVpnViewModel : ViewModel() {
    private val _state = MutableStateFlow(FakeVpnState())
    val state: StateFlow<FakeVpnState> = _state.asStateFlow()
    private var connectJob: Job? = null
    private var speedUpdateJob: Job? = null
    private var funnyMessageJob: Job? = null

    fun connect() {
        connectJob?.cancel()
        speedUpdateJob?.cancel()
        funnyMessageJob?.cancel()
        _state.update {
            it.copy(
                status = VpnStatus.CONNECTING,
                progress = 0,
                downloadSpeed = 0.0,
                uploadSpeed = 0.0,
                funnyMessage = "",
                errorMessage = null
            )
        }
        connectJob = viewModelScope.launch {
            for (i in 1..100) {
                delay(80)
                _state.update { it.copy(progress = i) }
                if (i == 100) {
                    _state.update {
                        it.copy(
                            status = VpnStatus.CONNECTED,
                            funnyMessage = getRandomFunnyMessage(isEnglish = false) // مقدار اولیه
                        )
                    }
                    startSpeedSimulation()
                    startFunnyMessageRotation()
                }
            }
        }
    }

    fun disconnect() {
        speedUpdateJob?.cancel()
        connectJob?.cancel()
        funnyMessageJob?.cancel()
        _state.update {
            it.copy(
                status = VpnStatus.DISCONNECTED,
                progress = 0,
                downloadSpeed = 0.0,
                uploadSpeed = 0.0,
                funnyMessage = ""
            )
        }
    }

    private fun startSpeedSimulation() {
        speedUpdateJob?.cancel()
        speedUpdateJob = viewModelScope.launch {
            while (_state.value.status == VpnStatus.CONNECTED) {
                val download = Random.nextDouble(5.0, 10.0)
                val upload = Random.nextDouble(5.0, 10.0)
                _state.update {
                    it.copy(
                        downloadSpeed = download,
                        uploadSpeed = upload
                    )
                }
                delay(2000) // سرعت هر ۲ ثانیه عوض می‌شود
            }
        }
    }

    private fun startFunnyMessageRotation() {
        funnyMessageJob?.cancel()
        funnyMessageJob = viewModelScope.launch {
            while (_state.value.status == VpnStatus.CONNECTED) {
                val isEnglish = (state.value.currentLanguage == "en") // باید currentLanguage را در State نگه داریم
                val newMessage = getRandomFunnyMessage(isEnglish)
                _state.update { it.copy(funnyMessage = newMessage) }
                delay(4000) // هر ۴ ثانیه یک جمله طنز جدید
            }
        }
    }

    private fun getRandomFunnyMessage(isEnglish: Boolean): String {
        val messages = if (isEnglish) {
            listOf(
                "😂 This VPN is faster than Iran's fiber optic! (0.001 Mbps)",
                "📡 Starlink? No, this is 'Star-lag' – one packet per year!",
                "🔒 Connected to a server in... your imagination!",
                "⚡ Speed of light in molasses? Still faster than this!",
                "💸 Our dev spent all budget on pizza, no real servers left!",
                "🚀 This config is so secret, even we can't find it!",
                "🎭 Real VPN costs money. This one? Just laughs (and ads maybe)",
                "🧠 Built by a broke developer who can't afford internet himself!",
                "🔥 Speed test result: your patience is faster than our VPN!",
                "🍕 Buy us a pizza, maybe we'll upgrade from fake to real!",
                "📞 Our support line: 1800-GET-REAL (just kidding, we have no support)",
                "🌐 'International connection' – connecting to your neighbor's WiFi!"
            )
        } else {
            listOf(
                "😂 این وی‌پی‌ان از فیبر نوری ایرانم تنده! (۰.۰۰۱ مگابیت)",
                "📡 استارلینک؟ نه بابا، این «استارلگه» – سالی یه پکت!",
                "🔒 متصل شدی به سروری تو... تخیلات خودت!",
                "⚡ سرعت نور؟ بازم از این سریع‌تره!",
                "💸 برنامه‌نویس کل بودجه رو داده پیتزا، دیگه سرور واقعی نمونده!",
                "🚀 این کانفیگ اونقدر محرمانه‌ست که خودمونم پیدا نمی‌کنیم!",
                "🎭 وی‌پی‌ان واقعی پول میخواد. این یکی فقط خنده (و شاید تبلیغ)",
                "🧠 ساخته شده توسط برنامه‌نویسی که خودش نت نداره! (حالا بفهم)",
                "🔥 تست سرعت: حوصله‌ی تو از وی‌پی‌ان ما بیشتره!",
                "🍕 پیتزا به برنامه‌نویس بدی شاید یه روز واقعی بشه!",
                "📞 پشتیبانی: ۱۸۰۰-بگیر-ببر (شوخی کردیم، هیچ پشتیبانی نداریم)",
                "🌐 «اتصال بین‌الملل»: داری به وای‌فای همسایه وصل میشی!"
            )
        }
        return messages.random()
    }

    fun updateLanguage(isEnglish: Boolean) {
        // اگر زبان عوض شد، جمله طنز را به‌روز کن فقط در حالت Connected
        if (_state.value.status == VpnStatus.CONNECTED) {
            _state.update { it.copy(funnyMessage = getRandomFunnyMessage(isEnglish)) }
        }
        // ذخیره زبان جاری در state (اختیاری)
        _state.update { it.copy(currentLanguage = if (isEnglish) "en" else "fa") }
    }

    override fun onCleared() {
        super.onCleared()
        connectJob?.cancel()
        speedUpdateJob?.cancel()
        funnyMessageJob?.cancel()
    }
}

enum class VpnStatus {
    IDLE, CONNECTING, CONNECTED, DISCONNECTED
}

data class FakeVpnState(
    val status: VpnStatus = VpnStatus.IDLE,
    val progress: Int = 0,
    val downloadSpeed: Double = 0.0,
    val uploadSpeed: Double = 0.0,
    val funnyMessage: String = "",
    val errorMessage: String? = null,
    val currentLanguage: String = "fa"  // برای تعیین زبان جاری
)