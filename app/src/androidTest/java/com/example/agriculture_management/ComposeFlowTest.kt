package com.example.agriculture_management

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.*
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.example.agriculture_management.ui.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

class ComposeFlowTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var state: FarmState
    @Before fun setup() {
        context.getSharedPreferences("green_farm_test",Context.MODE_PRIVATE).edit().clear().commit()
        state=FarmState(context,"green_farm_test")
    }
    @After fun cleanup() { context.getSharedPreferences("green_farm_test",Context.MODE_PRIVATE).edit().clear().commit() }

    @Test fun welcomeFlowReachesLandList() {
        compose.setContent { GreenFarmApp(state) }
        compose.onNodeWithText("Bắt đầu").performClick()
        compose.onNodeWithText("Bỏ qua").performClick()
        compose.onNodeWithText("Trải nghiệm giao diện").performScrollTo().performClick()
        compose.onNodeWithText("Thửa đất",useUnmergedTree=true).performClick()
        compose.onNodeWithText("Thửa bờ Tây").assertIsDisplayed()
    }

    @Test fun landFormRejectsInvalidAreaAndSavesValidInput() {
        var result: Land?=null
        compose.setContent { FarmTheme { AddLandScreen(null,{},{ result=it }) } }
        compose.onNodeWithText("Lưu").performScrollTo().performClick()
        compose.onNodeWithText("Nhập tên thửa đất và diện tích lớn hơn 0.").assertExists()
        compose.onNodeWithText("Ví dụ: Vườn sau nhà").performScrollTo().performTextInput("Vườn mới")
        compose.onNodeWithText("Nhập diện tích").performScrollTo().performTextInput("450")
        compose.onNodeWithText("Lưu").performScrollTo().performClick()
        compose.runOnIdle { assertEquals("Vườn mới",result?.name);assertEquals(450,result?.area) }
    }

    @Test fun saleCannotExceedAvailableStock() {
        var saved=false
        compose.setContent { FarmTheme { RecordScreen(1,state,{},{saved=true}) } }
        compose.onNodeWithText("Nhập số lượng").performTextInput("100")
        compose.onNodeWithText("Lưu hoạt động").performScrollTo().performClick()
        compose.onNodeWithText("Kho chỉ còn 35 kg Rau cải.").assertExists()
        compose.runOnIdle { assertFalse(saved);assertEquals(35,state.products.first().quantity) }
    }

    @Test fun localStorePersistsAndBackupRestores() {
        state.lands.add(Land("test","Vườn kiểm tra",450));state.save();state.backup()
        state.lands.removeAt(state.lands.lastIndex);state.save()
        assertTrue(state.restore())
        assertEquals("Vườn kiểm tra",FarmState(context,"green_farm_test").lands.last().name)
    }

    @Test fun allReferenceScreensRender() {
        var current by mutableStateOf(Screen.Splash)
        compose.setContent { FarmTheme { Surface(color=Cream) { key(current) { ScreenGallery(current,state) } } } }
        val directory=File(context.getExternalFilesDir(null),"ui-review").apply { mkdirs() }
        Screen.entries.filter { it!=Screen.Record }.forEach { screen ->
            compose.runOnIdle { current=screen }
            compose.waitForIdle()
            val bitmap=compose.onRoot().captureToImage().asAndroidBitmap()
            File(directory,"${screen.ordinal.toString().padStart(2,'0')}-${screen.name}.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
        }
    }
}
