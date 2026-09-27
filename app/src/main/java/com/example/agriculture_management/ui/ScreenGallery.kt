package com.example.agriculture_management.ui

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class FarmScreenPreviewProvider : PreviewParameterProvider<Screen> {
    override val values = Screen.entries.filter { it != Screen.Record }.asSequence()
}

/** Open this file in Android Studio's Split/Design mode for all 18 reference screens. */
@Preview(name="Nông Sản Xanh · 18 màn hình",showBackground=true,widthDp=393,heightDp=852)
@Composable
fun FarmScreenPreview(@PreviewParameter(FarmScreenPreviewProvider::class) screen: Screen) {
    val context=LocalContext.current
    val state=remember { FarmState(context,"green_farm_preview") }
    FarmTheme { Surface(color=Cream) { ScreenGallery(screen,state) } }
}

@Composable
fun ScreenGallery(screen: Screen,state: FarmState) {
    when(screen) {
        Screen.Splash -> SplashScreen {}
        Screen.Onboarding -> OnboardingScreen {}
        Screen.Login -> LoginScreen {}
        Screen.Home -> HomeScreen(state,{},{})
        Screen.Lands -> LandListScreen(state,{},{},{})
        Screen.LandDetail -> LandDetailScreen(state.lands.first(),state,{},{},{})
        Screen.AddLand -> AddLandScreen(null,{},{})
        Screen.AddPlanting -> AddPlantingScreen(state,"1",{},{})
        Screen.QuickAction -> QuickActionScreen({},{},{})
        Screen.Logs -> LogsScreen(state,{},{})
        Screen.Report -> ReportScreen {}
        Screen.Inventory -> InventoryScreen(state,{})
        Screen.Reminders -> RemindersScreen(state,{})
        Screen.Settings -> SettingsScreen(state,{},{},{})
        Screen.Backup -> BackupScreen(state,{},{})
        Screen.Profile -> ProfileScreen(state,{},{})
        Screen.More -> MoreScreen(state,{},{},{})
        Screen.Empty -> EmptyScreen({},{})
        Screen.Record -> RecordScreen(0,state,{},{})
    }
}
