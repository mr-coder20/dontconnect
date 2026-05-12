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

class FakeVpnViewModel : ViewModel() {
    private val _state = MutableStateFlow(FakeVpnState())
    val state: StateFlow<FakeVpnState> = _state.asStateFlow()
    private var job: Job? = null

    fun connect() {
        job?.cancel()
        _state.update { it.copy(isConnecting = true, progress = 0, joke = "") }
        job = viewModelScope.launch {
            for (i in 1..100) {
                delay(80)
                _state.update { it.copy(progress = i) }
                if (i == 100) {
                    _state.update {
                        it.copy(
                            isConnecting = false,
                            joke = hilariousJokes.random()
                        )
                    }
                }
            }
        }
    }

    private val hilariousJokes = listOf(
        "😂 داداش، این وی‌پی‌ان همونقدر واقعیه که وعده‌های انتخاباتی!",
        "📡 وصل شد! (فقط یه لحظه... نه، نشد!)",
        "🔒 اتصال برقرار شد! 【برای کاربران مریخ】",
        "🎭 راستش ما اصلاً سروری نداریم، فقط دوس داریم یه کم باهات شوخی کنیم!",
        "⚡ سرعت اینترنت الآن به اندازه‌ی تلاش ما برای آزادیه: صفر مطلق!",
        "🍕 برنامه‌نویس گفت اگه این دکمه رو زدی، براش پیتزا بگیری شاید یه وی‌پی‌ان واقعی درست کنه!",
        "🚀 فیلترشکن وصل شد؟ نه بابا، فقط داری به یک سرور تو قلب تهران متصل میشی که خودش فیلتره!",
        "💸 پول اشتراک دادی؟ خب معلومه که وصل نمیشه! این یکی مجانیه!",
        "🧠 این وی‌پی‌ان با مغز برنامه‌نویس طراحی شده (اونی که نت نداره)",
        "📞 با ۱۸۰۰ تماس بگیر، شاید نت باز بشه! (البته نه)",
        "🤡 ما که بهت گفتیم «وصل نشو»! حالا خودت میدونی!",
        "🌐 اتصال به اینترنت بین‌الملل: مثل دیدن یونیکورن‌ها در اتوبان آزادگان!",
        "⏳ منتظر بمون تا تحریم‌ها بردارن... (شاید نوه‌هات ببینن)",
        "🍜 برنامه‌نویس از گشنگی دیگه کد الکی می‌نویسه، بهش رحم کن!"
    )
}

data class FakeVpnState(
    val progress: Int = 0,
    val isConnecting: Boolean = false,
    val joke: String = ""
)