package don.t.connect.utils



import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object Formatter {

    // فرمت‌کننده اعداد با جدا کننده هزارگان (مخصوص ایران)
    private val numberFormat: DecimalFormat by lazy {
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ','   // جدا کننده هزارگان با کاما (مثال: ۱,۲۵۰,۰۰۰)
        }
        DecimalFormat("#,###", symbols)
    }

    /**
     * یک عدد [Long] را به صورت ۳,۲۰۰,۰۰۰ برمی‌گرداند.
     * مثال: formatNumber(12500000) -> "12,500,000"
     */
    fun formatNumber(number: Long): String {
        return numberFormat.format(number)
    }

    /**
     * عدد را به صورت تومان نمایش می‌دهد، مثلاً "۱۲,۵۰۰,۰۰۰ تومان"
     */
    fun formatToman(number: Long): String {
        return "${formatNumber(number)} تومان"
    }

    /**
     * برای اعداد بسیار بزرگ (میلیون، میلیارد) نسخه کوتاه شده برمی‌گرداند.
     * مثال: 12,500,000 -> "۱۲.۵ میلیون"
     * مثال: 1,250,000,000 -> "۱.۲۵ میلیارد"
     */
    fun formatShort(number: Long): String {
        return when {
            number >= 1_000_000_000 -> {
                val billions = number / 1_000_000_000.0
                "%.2f میلیارد".format(billions).replace(".", "/") // جایگزین . با / برای نمایش فارسی? بعداً
            }
            number >= 1_000_000 -> {
                val millions = number / 1_000_000.0
                "%.2f میلیون".format(millions)
            }
            else -> formatNumber(number)
        }
    }

    /**
     * نمایش قیمت با واحد تومان و نسخه کوتاه، مثلاً "۱۲.۵ میلیون تومان"
     */
    fun formatShortToman(number: Long): String {
        return when {
            number >= 1_000_000_000 -> "%.2f میلیارد تومان".format(number / 1_000_000_000.0)
            number >= 1_000_000 -> "%.1f میلیون تومان".format(number / 1_000_000.0)
            else -> formatToman(number)
        }
    }
}