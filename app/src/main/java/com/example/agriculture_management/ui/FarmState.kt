package com.example.agriculture_management.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class Land(val id: String, val name: String, val area: Int, val location: String = "",
    val note: String = "", val crop: String = "", val planted: String = "25/08/2026", val harvest: String = "15/10/2026",
    val photo: String = "", val cropAreas: Map<String, Int> = emptyMap())
data class FarmLog(val title: String, val detail: String, val date: String, val kind: Int, val amount: Int = 0)
data class Product(val name: String, val quantity: Int)
data class Reminder(val title: String, val land: String, val date: String, val done: Boolean = false)

val cropNames = listOf("Rau cải", "Rau muống", "Cà chua", "Ớt", "Khác")
fun number(value: Int): String = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN")).format(value)
fun today(): String = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

/** Local UI demo store, separate from the existing domain/recovery templates. */
class FarmState(context: Context, preferenceName: String = "green_farm_ui") {
    private val preferences = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
    val lands = mutableStateListOf<Land>()
    val logs = mutableStateListOf<FarmLog>()
    val products = mutableStateListOf<Product>()
    val reminders = mutableStateListOf<Reminder>()
    var name by mutableStateOf(preferences.getString("name", "Nguyễn Văn An") ?: "Nguyễn Văn An")
    var notifications by mutableStateOf(preferences.getBoolean("notifications", true))
    var backupDate by mutableStateOf(preferences.getString("backup_date", "") ?: "")

    init {
        val saved = preferences.getString("data", null)
        if (saved == null || runCatching { load(saved) }.isFailure) seed()
    }

    private fun seed() {
        lands.clear(); logs.clear(); products.clear(); reminders.clear()
        lands.addAll(listOf(
            Land("1", "Vườn sau nhà", 1200, "Đà Lạt, Lâm Đồng", "Đất tơi xốp, thoát nước tốt. Bón phân hữu cơ định kỳ.", "Rau cải, Rau muống", cropAreas=mapOf("Rau cải" to 600,"Rau muống" to 400)),
            Land("2", "Thửa bờ Đông", 850, "Đà Lạt, Lâm Đồng", "Tưới nước vào sáng sớm.", "Cà chua, Ớt"),
            Land("3", "Thửa bờ Tây", 600, "Đà Lạt, Lâm Đồng")))
        logs.addAll(listOf(FarmLog("Thu hoạch rau cải", "Vườn sau nhà", "25/09/2026", 0, 50),
            FarmLog("Bán cho thương lái", "Rau cải", "24/09/2026", 1, 90),
            FarmLog("Mua phân hữu cơ", "Vườn sau nhà", "22/09/2026", 2, 250000),
            FarmLog("Tưới nước", "Vườn sau nhà", "22/09/2026", 3),
            FarmLog("Phun thuốc sinh học", "Vườn sau nhà", "21/09/2026", 4)))
        products.addAll(listOf(Product("Rau cải",35),Product("Rau muống",20),Product("Cà chua",50),Product("Ớt",10)))
        reminders.addAll(listOf(Reminder("Tưới nước","Vườn sau nhà","27/09/2026"),Reminder("Bón phân","Thửa bờ Đông","28/09/2026"),
            Reminder("Phun thuốc","Vườn sau nhà","30/09/2026"),Reminder("Thu hoạch","Rau cải","15/10/2026")))
    }

    fun snapshot(): String = JSONObject().apply {
        put("lands", JSONArray().apply { lands.forEach { l -> put(JSONObject().apply {
            put("id",l.id);put("name",l.name);put("area",l.area);put("location",l.location);put("note",l.note)
            put("crop",l.crop);put("planted",l.planted);put("harvest",l.harvest);put("photo",l.photo);put("cropAreas",JSONObject(l.cropAreas))
        }) } })
        put("logs", JSONArray().apply { logs.forEach { l -> put(JSONObject().apply {
            put("title",l.title);put("detail",l.detail);put("date",l.date);put("kind",l.kind);put("amount",l.amount)
        }) } })
        put("products", JSONArray().apply { products.forEach { put(JSONObject().put("name",it.name).put("quantity",it.quantity)) } })
        put("reminders", JSONArray().apply { reminders.forEach { put(JSONObject().put("title",it.title).put("land",it.land).put("date",it.date).put("done",it.done)) } })
    }.toString(2)

    private fun load(raw: String) {
        val data = JSONObject(raw)
        fun rows(key: String): List<JSONObject> = data.getJSONArray(key).let { a -> (0 until a.length()).map { a.getJSONObject(it) } }
        val loadedLands = rows("lands").map { row ->
            val areas=row.optJSONObject("cropAreas")
            Land(row.getString("id"),row.getString("name"),row.getInt("area"),row.optString("location"),row.optString("note"),row.optString("crop"),row.optString("planted"),row.optString("harvest"),row.optString("photo"),areas?.keys()?.asSequence()?.associateWith { areas.getInt(it) } ?: emptyMap())
        }
        val loadedLogs = rows("logs").map { FarmLog(it.getString("title"),it.getString("detail"),it.getString("date"),it.getInt("kind"),it.getInt("amount")) }
        val loadedProducts = rows("products").map { Product(it.getString("name"),it.getInt("quantity")) }
        val loadedReminders = rows("reminders").map { Reminder(it.getString("title"),it.getString("land"),it.getString("date"),it.getBoolean("done")) }
        lands.clear();lands.addAll(loadedLands);logs.clear();logs.addAll(loadedLogs)
        products.clear();products.addAll(loadedProducts);reminders.clear();reminders.addAll(loadedReminders)
    }
    fun save() { preferences.edit().putString("data",snapshot()).putString("name",name).putBoolean("notifications",notifications).apply() }
    fun backup() { backupDate = today(); preferences.edit().putString("backup",snapshot()).putString("backup_date",backupDate).apply() }
    fun restore(): Boolean {
        val raw = preferences.getString("backup",null) ?: return false
        return runCatching { load(raw);save() }.isSuccess
    }
}
