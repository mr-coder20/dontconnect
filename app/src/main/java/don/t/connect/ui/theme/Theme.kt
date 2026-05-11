package don.t.connect.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColorDark,
    primaryContainer = PrimaryVariantColorDark,
    onPrimary = OnPrimaryColorDark,
    secondary = SecondaryColorDark,
    secondaryContainer = SecondaryVariantColorDark,
    onSecondary = OnSecondaryColorDark,
    background = PrimaryColorDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    primaryContainer = PrimaryVariantColor,
    onPrimary = OnPrimaryColor,
    secondary = SecondaryColor,
    secondaryContainer = SecondaryVariantColor,
    onSecondary = OnSecondaryColor,
    background = PrimaryColor
)

@Composable
fun DontConnectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }


    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}