package com.example.agriculture_management.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.graphics.ImageDecoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CropIcon(name: String, modifier: Modifier = Modifier) {
    val color=if(name=="Cà chua" || name=="Ớt") Coral else Forest
    Box(modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(color.copy(alpha=.09f)),contentAlignment=Alignment.Center) {
        if(name=="Khác") Icon(Icons.Outlined.MoreHoriz,null,Modifier.size(30.dp),tint=Muted)
        else Canvas(Modifier.size(34.dp)) {
            val w=size.width;val h=size.height
            fun point(x: Float,y: Float)=androidx.compose.ui.geometry.Offset(w*x,h*y)
            fun leaf(x: Float,y: Float,tipX: Float,tipY: Float,shade: Color) {
                drawPath(androidx.compose.ui.graphics.Path().apply {
                    moveTo(w*x,h*y);quadraticTo(w*(tipX-.35f),h*tipY,w*tipX,h*tipY)
                    quadraticTo(w*(tipX+.27f),h*(y-.13f),w*x,h*y);close()
                },shade)
                drawLine(Color(0xFFD6E9AE),point(x,y),point(tipX,tipY),1.dp.toPx())
            }
            when(name) {
                "Cà chua" -> {
                    drawCircle(Color(0xFFB92E26),w*.36f,point(.5f,.59f))
                    drawCircle(Color(0xFFE14E35),w*.32f,point(.47f,.55f))
                    drawOval(Color(0xFFFF9870),point(.27f,.38f),androidx.compose.ui.geometry.Size(w*.14f,h*.24f))
                    drawPath(androidx.compose.ui.graphics.Path().apply {
                        moveTo(w*.49f,h*.36f);lineTo(w*.24f,h*.24f);lineTo(w*.42f,h*.25f)
                        lineTo(w*.4f,h*.12f);lineTo(w*.53f,h*.25f);lineTo(w*.75f,h*.19f)
                        lineTo(w*.62f,h*.32f);lineTo(w*.71f,h*.4f);lineTo(w*.51f,h*.35f);close()
                    },Forest)
                    drawLine(Forest,point(.49f,.3f),point(.56f,.08f),2.dp.toPx())
                }
                "Ớt" -> {
                    drawPath(androidx.compose.ui.graphics.Path().apply {
                        moveTo(w*.58f,h*.25f);cubicTo(w*.99f,h*.48f,w*.51f,h*.87f,w*.12f,h*.94f)
                        cubicTo(w*.43f,h*.62f,w*.39f,h*.35f,w*.58f,h*.25f);close()
                    },Color(0xFFD83F34))
                    drawPath(androidx.compose.ui.graphics.Path().apply { moveTo(w*.6f,h*.37f);quadraticTo(w*.62f,h*.58f,w*.32f,h*.79f) },
                        Color(0xFFFF9D78),style=androidx.compose.ui.graphics.drawscope.Stroke(1.5.dp.toPx()))
                    drawPath(androidx.compose.ui.graphics.Path().apply {moveTo(w*.53f,h*.3f);quadraticTo(w*.7f,h*.19f,w*.74f,h*.06f)},Forest,style=androidx.compose.ui.graphics.drawscope.Stroke(3.dp.toPx()))
                }
                "Rau muống" -> {
                    drawLine(Forest,point(.32f,.97f),point(.66f,.10f),1.5.dp.toPx())
                    leaf(.36f,.84f,.17f,.38f,Leaf);leaf(.45f,.65f,.75f,.13f,Forest);leaf(.52f,.87f,.90f,.4f,Leaf)
                }
                else -> {
                    leaf(.46f,.94f,.24f,.2f,Forest);leaf(.52f,.91f,.7f,.12f,Leaf)
                    leaf(.49f,.9f,.44f,.05f,Color(0xFF6E9A47));leaf(.42f,.87f,.12f,.40f,Leaf)
                    leaf(.53f,.93f,.86f,.38f,Color(0xFF629846))
                }
            }
        }
    }
}

@Composable
fun LandImage(land: Land, modifier: Modifier) {
    val context=LocalContext.current
    val bitmap by produceState<androidx.compose.ui.graphics.ImageBitmap?>(null,land.photo) {
        value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            if(land.photo.isBlank()) null else runCatching {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver,Uri.parse(land.photo))) { decoder, info, _ ->
                    val scale = minOf(1f, 1000f/info.size.width)
                    decoder.setTargetSize((info.size.width*scale).toInt().coerceAtLeast(1),(info.size.height*scale).toInt().coerceAtLeast(1))
                }.asImageBitmap()
            }.getOrNull()
        }
    }
    if(bitmap!=null) Image(bitmap!!,land.name,modifier.clip(RoundedCornerShape(14.dp)),contentScale=ContentScale.Crop)
    else FarmArt(modifier)
}

@Composable
fun HomeScreen(state: FarmState, go: (Screen)->Unit, detail: (String)->Unit, lands: List<Land> = state.lands) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp)) {
        Row(verticalAlignment=Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text("Xin chào,",color=Muted);Text("${state.name} 👋",style=MaterialTheme.typography.titleLarge) }
            IconButton(onClick={go(Screen.Profile)},modifier=Modifier.clip(CircleShape).background(Pale)) { LeafMark(Modifier.size(30.dp)) }
        }
        Row(Modifier.fillMaxWidth().padding(top=24.dp).clip(RoundedCornerShape(16.dp)).background(Pale).clickable { go(Screen.AddPlanting) }.padding(18.dp),verticalAlignment=Alignment.CenterVertically) {
            LeafMark(Modifier.size(43.dp));Spacer(Modifier.width(16.dp))
            Column { Text("Mùa vụ mới đang chờ!",fontWeight=FontWeight.SemiBold);Text("Bắt đầu ghi nhật ký hôm nay",fontSize=12.sp,color=Forest) }
        }
        Section("Tổng quan hôm nay")
        Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            Metric("${lands.count { it.crop.isNotBlank() }}","Thửa đất đang trồng",Icons.Outlined.Assignment,Forest,Modifier.weight(1f)) { go(Screen.Lands) }
            Metric("${lands.flatMap { it.crop.split(", ") }.filter { it.isNotBlank() }.distinct().size}","Loại cây trồng",Icons.Outlined.Eco,Leaf,Modifier.weight(1f)) { go(Screen.Lands) }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            Metric("130 kg","Sản lượng mẫu",Icons.Outlined.LocalFlorist,Color(0xFFD5A04D),Modifier.weight(1f)) { go(Screen.Report) }
            Metric("900.000 đ","Doanh thu mẫu",Icons.Outlined.AccountBalanceWallet,Forest,Modifier.weight(1f)) { go(Screen.Report) }
        }
        Section("Hoạt động gần đây","Xem tất cả") { go(Screen.Logs) }
        state.logs.take(2).forEach { LogCard(it);Spacer(Modifier.height(9.dp)) }
        PrimaryButton("＋  Ghi nhanh hoạt động",pale=true) { go(Screen.QuickAction) }
        Section("Nông trại của bạn","Quản lý") { go(Screen.Lands) }
        lands.take(1).forEach { LandRow(it) { detail(it.id) } }
        Row(Modifier.padding(top=18.dp).fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly) {
            TextButton(onClick={go(Screen.Inventory)}) { Text("Kho nông sản",fontSize=12.sp) }
            TextButton(onClick={go(Screen.Reminders)}) { Text("Nhắc việc",fontSize=12.sp) }
            IconButton(onClick={go(Screen.Settings)}) { Icon(Icons.Outlined.Settings,"Cài đặt",tint=Forest) }
            IconButton(onClick={go(Screen.More)}) { Icon(Icons.Outlined.MoreHoriz,"Thêm",tint=Forest) }
        }
    }
}

@Composable
private fun Metric(value: String,label: String,icon: androidx.compose.ui.graphics.vector.ImageVector,color: Color,modifier: Modifier,onClick:()->Unit) {
    FarmCard(modifier.clickable(onClick=onClick)) {
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(9.dp)) {
            IconTile(icon,color,30)
            Column(Modifier.weight(1f)) { Text(value,fontSize=17.sp,fontWeight=FontWeight.Bold);Text(label,fontSize=10.sp,color=Muted) }
        }
    }
}

@Composable
fun LandRow(land: Land, onClick:()->Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White).clickable(onClick=onClick).padding(12.dp),verticalAlignment=Alignment.CenterVertically) {
        LandImage(land,Modifier.size(85.dp));Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(5.dp)) {
            Text(land.name,fontWeight=FontWeight.SemiBold,fontSize=15.sp)
            Badge(if(land.crop.isBlank()) "Để trống" else "Đang trồng",if(land.crop.isBlank()) Muted else Forest)
            Text("${number(land.area)} m² | ${land.crop.ifBlank { "Chưa trồng" }}",fontSize=11.sp,color=Muted)
        }
        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight,null,Modifier.size(19.dp),tint=Muted)
    }
}

@Composable
fun LandListScreen(state: FarmState,back:()->Unit,add:()->Unit,detail:(String)->Unit, lands: List<Land> = state.lands) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    Page("Thửa đất",back,action={TextButton(onClick=add) { Icon(Icons.Outlined.Add,null,Modifier.size(18.dp));Text("Thêm") }}) {
        Tabs(listOf("Tất cả (${lands.size})","Đang trồng (${lands.count { it.crop.isNotBlank() }})","Để trống (${lands.count { it.crop.isBlank() }})"),selected) { selected=it }
        Spacer(Modifier.height(20.dp))
        val filtered=lands.filter { selected==0 || (selected==1 && it.crop.isNotBlank()) || (selected==2 && it.crop.isBlank()) }
        filtered.forEach { land -> LandRow(land) { detail(land.id) };Spacer(Modifier.height(13.dp)) }
        if(filtered.isEmpty()) EmptyContent(add)
    }
}

@Composable
fun LandDetailScreen(land: Land,state: FarmState,back:()->Unit,edit:()->Unit,plant:()->Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    Page(land.name,back,action={IconButton(onClick=edit) { Icon(Icons.Outlined.Edit,"Chỉnh sửa") }}) {
        LandImage(land,Modifier.fillMaxWidth().height(210.dp))
        Row(Modifier.fillMaxWidth().padding(vertical=18.dp),horizontalArrangement=Arrangement.SpaceBetween) {
            Text("◉  ${number(land.area)} m²",color=Muted,fontSize=13.sp);Badge(if(land.crop.isBlank()) "Để trống" else "Đang trồng")
        }
        Tabs(listOf("Tổng quan","Cây trồng","Nhật ký","Chi phí"),tab) { tab=it }
        Spacer(Modifier.height(20.dp))
        when(tab) {
            0,1 -> {
                if(land.crop.isBlank()) { EmptyContent(plant) }
                else {
                    land.crop.split(", ").forEach { crop ->
                        FarmCard { Row(verticalAlignment=Alignment.CenterVertically) { CropIcon(crop);Spacer(Modifier.width(12.dp));Column { Text(crop,fontWeight=FontWeight.Medium);Text("Diện tích ${number(land.cropAreas[crop] ?: (land.area/land.crop.split(", ").size))} m²",fontSize=12.sp,color=Muted) } } }
                        Spacer(Modifier.height(10.dp))
                    }
                    FarmCard {
                        Row(Modifier.fillMaxWidth()) {
                            Column(Modifier.weight(1f)) { Text("Ngày trồng",fontSize=11.sp,color=Muted);Text(land.planted,fontWeight=FontWeight.SemiBold,modifier=Modifier.padding(top=7.dp)) }
                            Column(Modifier.weight(1f)) { Text("Ngày thu hoạch dự kiến",fontSize=11.sp,color=Muted);Text(land.harvest,fontWeight=FontWeight.SemiBold,modifier=Modifier.padding(top=7.dp)) }
                        }
                    }
                    Section("Ghi chú");Text(land.note.ifBlank { "Chưa có ghi chú" },color=Muted)
                    if(land.location.isNotBlank()) { Section("Vị trí");Text(land.location,color=Muted) }
                    Spacer(Modifier.height(26.dp));PrimaryButton("Chỉnh sửa",pale=true,onClick=edit)
                    TextButton(onClick=plant,modifier=Modifier.align(Alignment.CenterHorizontally)) { Text("＋ Thêm vụ trồng") }
                }
            }
            2 -> { val logs=state.logs.filter { it.detail==land.name };if(logs.isEmpty()) Text("Chưa có hoạt động cho thửa đất này.",color=Muted);logs.forEach { LogCard(it);Spacer(Modifier.height(10.dp)) } }
            3 -> { val costs=state.logs.filter { it.detail==land.name && it.kind==2 };FarmCard { Text("Tổng chi phí",color=Muted);Text("${number(costs.sumOf { it.amount })} đ",style=MaterialTheme.typography.headlineSmall) };Spacer(Modifier.height(12.dp));costs.forEach { LogCard(it) } }
        }
    }
}

@Composable
fun DateField(label: String, value: String, onChange: (String)->Unit) {
    val context=LocalContext.current
    val format=DateTimeFormatter.ofPattern("dd/MM/yyyy")
    Text(label,fontSize=13.sp,fontWeight=FontWeight.Medium,modifier=Modifier.padding(bottom=8.dp))
    OutlinedButton(onClick={
        val initial=runCatching { LocalDate.parse(value,format) }.getOrDefault(LocalDate.now())
        DatePickerDialog(context,{_,y,m,d -> onChange(LocalDate.of(y,m+1,d).format(format)) },initial.year,initial.monthValue-1,initial.dayOfMonth).show()
    },modifier=Modifier.fillMaxWidth().padding(bottom=17.dp).height(54.dp),shape=RoundedCornerShape(12.dp),border=androidx.compose.foundation.BorderStroke(1.dp,Line),colors=ButtonDefaults.outlinedButtonColors(containerColor=Color.White)) {
        Text(value.ifBlank { "Chọn ngày" },Modifier.weight(1f),color=if(value.isBlank()) Muted else Ink);Icon(Icons.Outlined.CalendarMonth,null,Modifier.size(20.dp))
    }
}

@Composable
fun AddLandScreen(existing: Land?,back:()->Unit,save:(Land)->Unit) {
    var name by rememberSaveable { mutableStateOf(existing?.name ?: "") };var area by rememberSaveable { mutableStateOf(existing?.area?.toString() ?: "") }
    var location by rememberSaveable { mutableStateOf(existing?.location ?: "") };var note by rememberSaveable { mutableStateOf(existing?.note ?: "") }
    var photo by rememberSaveable { mutableStateOf(existing?.photo ?: "") };var error by rememberSaveable { mutableStateOf("") }
    val context=LocalContext.current
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if(uri!=null) {
        runCatching { context.contentResolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION) };photo=uri.toString()
    } }
    Page(if(existing==null) "Thêm thửa đất" else "Chỉnh sửa thửa đất",back) {
        Box(Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(16.dp)).background(Pale.copy(alpha=.6f)).clickable { picker.launch(arrayOf("image/*")) },contentAlignment=Alignment.Center) {
            if(photo.isNotBlank()) LandImage(Land("","",0,photo=photo),Modifier.fillMaxSize())
            else Column(horizontalAlignment=Alignment.CenterHorizontally) { IconTile(Icons.Outlined.PhotoCamera);Text("Thêm ảnh (tùy chọn)",fontSize=12.sp,modifier=Modifier.padding(top=10.dp)) }
        }
        Spacer(Modifier.height(23.dp))
        Field("Tên thửa đất",name,{name=it},"Ví dụ: Vườn sau nhà")
        Field("Diện tích (m²)",area,{area=it},"Nhập diện tích",keyboardType=KeyboardType.Number)
        Field("Vị trí",location,{location=it},"Ví dụ: Xã, Huyện, Tỉnh",trailing={Icon(Icons.Outlined.LocationOn,null,Modifier.size(18.dp))})
        Field("Ghi chú",note,{note=it},"Ghi chú thêm (tùy chọn)",lines=3)
        if(error.isNotBlank()) Text(error,color=Coral,modifier=Modifier.padding(bottom=12.dp))
        PrimaryButton("Lưu") {
            val parsed=area.toIntOrNull()
            if(name.isBlank() || parsed==null || parsed<=0) error="Nhập tên thửa đất và diện tích lớn hơn 0."
            else if(existing!=null && parsed<existing.cropAreas.values.sum()) error="Diện tích không thể nhỏ hơn phần đang trồng."
            else save((existing ?: Land(System.currentTimeMillis().toString(),"",0)).copy(name=name.trim(),area=parsed,location=location.trim(),note=note.trim(),photo=photo))
        }
    }
}

@Composable
fun ChooseField(label: String, options: List<String>, value: String, choose: (String)->Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
    Text(label,fontSize=13.sp,fontWeight=FontWeight.Medium,modifier=Modifier.padding(bottom=8.dp))
    Box {
        OutlinedButton(onClick={expanded=true},modifier=Modifier.fillMaxWidth().padding(bottom=17.dp).heightIn(min=52.dp),shape=RoundedCornerShape(12.dp),
            border=androidx.compose.foundation.BorderStroke(1.dp,Line),colors=ButtonDefaults.outlinedButtonColors(containerColor=Color.White)) {
            Text(value.ifBlank { "Chọn" },Modifier.weight(1f),color=Ink);Icon(Icons.Outlined.ExpandMore,null)
        }
        DropdownMenu(expanded,{expanded=false}) { options.forEach { item -> DropdownMenuItem(text={Text(item)},onClick={choose(item);expanded=false}) } }
    }
    }
}

@Composable
fun AddPlantingScreen(state: FarmState,selectedId: String,back:()->Unit,save:(Land)->Unit) {
    var landId by rememberSaveable { mutableStateOf(state.lands.find { it.id==selectedId }?.id ?: state.lands.firstOrNull()?.id ?: "") }
    var crop by rememberSaveable { mutableStateOf("Rau cải") };var area by rememberSaveable { mutableStateOf("") }
    var planted by rememberSaveable { mutableStateOf(today()) };var harvest by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf("") }
    Page("Thêm vụ trồng",back) {
        val land=state.lands.find { it.id==landId }
        ChooseField("Chọn thửa đất",state.lands.map { "${it.name} · ${it.id.takeLast(4)}" },land?.let { "${it.name} · ${it.id.takeLast(4)}" } ?: "") { choice ->
            landId=state.lands.first { "${it.name} · ${it.id.takeLast(4)}"==choice }.id
        }
        Text("Chọn cây trồng",fontSize=13.sp,fontWeight=FontWeight.Medium)
        cropNames.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth().padding(top=10.dp),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                row.forEach { name -> Column(Modifier.weight(1f).clip(RoundedCornerShape(13.dp)).background(if(crop==name) Pale else Color.White).clickable { crop=name }.padding(13.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                    CropIcon(name);Text(name,fontSize=11.sp,modifier=Modifier.padding(top=8.dp))
                } }
                repeat(3-row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        Spacer(Modifier.height(22.dp));Field("Diện tích (m²)",area,{area=it},"Nhập diện tích",keyboardType=KeyboardType.Number)
        DateField("Ngày trồng",planted) { planted=it };DateField("Dự kiến thu hoạch",harvest) { harvest=it }
        if(error.isNotBlank()) Text(error,color=Coral,modifier=Modifier.padding(bottom=12.dp))
        PrimaryButton("Lưu") {
            val parsed=area.toIntOrNull();val format=DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val allocations=land?.crop?.split(", ")?.filter { it.isNotBlank() }?.associateWith { land.cropAreas[it] ?: land.area/land.crop.split(", ").size } ?: emptyMap()
            val available=(land?.area ?: 0)-allocations.filterKeys { it!=crop }.values.sum()
            when {
                land==null -> error="Hãy thêm một thửa đất trước."
                parsed==null || parsed<=0 || parsed>available -> error="Diện tích phải lớn hơn 0 và không vượt quá phần còn lại ($available m²)."
                harvest.isBlank() -> error="Vui lòng chọn ngày thu hoạch."
                LocalDate.parse(harvest,format)<LocalDate.parse(planted,format) -> error="Ngày thu hoạch phải từ ngày trồng trở đi."
                else -> { state.logs.add(0,FarmLog("Trồng $crop ($parsed m²)",land.name,today(),3));save(land.copy(crop=(land.crop.split(", ").filter { it.isNotBlank() }+crop).distinct().joinToString(", "),planted=planted,harvest=harvest,cropAreas=allocations+(crop to parsed))) }
            }
        }
    }
}

@Composable
fun EmptyContent(add:()->Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical=40.dp),horizontalAlignment=Alignment.CenterHorizontally) {
        Box(Modifier.size(160.dp).clip(CircleShape).background(Pale.copy(alpha=.6f)),contentAlignment=Alignment.Center) { LeafMark(Modifier.size(100.dp)) }
        Text("Chưa có dữ liệu",style=MaterialTheme.typography.titleLarge,modifier=Modifier.padding(top=28.dp,bottom=10.dp))
        Text("Hãy thêm thửa đất hoặc bắt đầu trồng\nvụ mới để quản lý nông trại của bạn.",color=Muted,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(26.dp));PrimaryButton("Bắt đầu thêm",Modifier.widthIn(max=240.dp),onClick=add)
    }
}
@Composable
fun EmptyScreen(back:()->Unit,add:()->Unit) { Page("",back) { Spacer(Modifier.height(45.dp));EmptyContent(add) } }
