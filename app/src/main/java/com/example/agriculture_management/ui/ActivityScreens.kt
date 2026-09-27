package com.example.agriculture_management.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val activityColors=listOf(Forest,Coral,Violet,Color(0xFF63A28A),Color(0xFF5C9EA0))
private val activityIcons=listOf(Icons.Outlined.Eco,Icons.Outlined.ShoppingCart,Icons.Outlined.AccountBalanceWallet,Icons.Outlined.WaterDrop,Icons.Outlined.HealthAndSafety)

@Composable
fun LogCard(log: FarmLog) {
    FarmCard {
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            IconTile(activityIcons[log.kind.coerceIn(0,4)],activityColors[log.kind.coerceIn(0,4)])
            Column(Modifier.weight(1f)) { Text(log.title,fontWeight=FontWeight.Medium,fontSize=13.sp);Text(log.detail,fontSize=11.sp,color=Muted);Text(log.date,fontSize=10.sp,color=Muted) }
            if(log.amount!=0) Text(when(log.kind) {0 -> "+${number(log.amount)} kg";1 -> "-${number(log.amount)} kg";else -> "${number(log.amount)} đ"},
                color=when(log.kind) {0->Forest;1->Coral;else->Ink},fontWeight=FontWeight.SemiBold,fontSize=12.sp)
        }
    }
}

@Composable
fun QuickActionScreen(back:()->Unit,go:(Screen)->Unit,record:(Int)->Unit) {
    Page("Ghi nhanh",back) {
        val names=listOf("Thu hoạch","Bán hàng","Chi phí","Nhật ký")
        listOf(0..1,2..3).forEach { indices ->
            Row(Modifier.fillMaxWidth().padding(bottom=14.dp),horizontalArrangement=Arrangement.spacedBy(14.dp)) {
                indices.forEach { index -> Column(Modifier.weight(1f).clip(RoundedCornerShape(18.dp)).background(Color.White).clickable { record(index) }.padding(vertical=30.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                    IconTile(if(index==3) Icons.Outlined.EditNote else activityIcons[index],if(index==3) Violet else activityColors[index],54)
                    Text(names[index],fontWeight=FontWeight.Medium,modifier=Modifier.padding(top=15.dp))
                } }
            }
        }
        Section("Thao tác khác")
        FarmCard(Modifier) {
            MenuRow(Icons.Outlined.AddBusiness,"Thêm thửa đất") { go(Screen.AddLand) };HorizontalDivider(color=Line)
            MenuRow(Icons.Outlined.Spa,"Thêm vụ trồng") { go(Screen.AddPlanting) };HorizontalDivider(color=Line)
            MenuRow(Icons.Outlined.Inventory2,"Quản lý kho") { go(Screen.Inventory) };HorizontalDivider(color=Line)
            MenuRow(Icons.Outlined.NotificationsNone,"Nhắc việc") { go(Screen.Reminders) }
        }
    }
}

@Composable
fun LogsScreen(state: FarmState,back:()->Unit,add:()->Unit) {
    var filter by rememberSaveable { mutableStateOf("Tất cả") }
    var month by rememberSaveable { mutableStateOf("09/2026") }
    Box(Modifier.fillMaxSize()) {
        Page("Nhật ký",back) {
            Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                Box(Modifier.weight(1f)) { ChooseField("Hoạt động",listOf("Tất cả","Thu hoạch","Bán hàng","Chi phí","Chăm sóc"),filter) { filter=it } }
                Box(Modifier.weight(1f)) { ChooseField("Thời gian",(listOf(today().takeLast(7))+listOf("09/2026","08/2026","07/2026")).distinct(),month) { month=it } }
            }
            val rows=state.logs.filter { it.date.endsWith(month) && (filter=="Tất cả" || when(filter) {"Thu hoạch"->it.kind==0;"Bán hàng"->it.kind==1;"Chi phí"->it.kind==2;else->it.kind>=3}) }
            rows.forEach { LogCard(it);Spacer(Modifier.height(12.dp)) }
            if(rows.isEmpty()) Text("Chưa có hoạt động trong thời gian này.",color=Muted,modifier=Modifier.padding(vertical=40.dp))
            Spacer(Modifier.height(60.dp))
        }
        FloatingActionButton(onClick=add,containerColor=Forest,contentColor=Color.White,shape=androidx.compose.foundation.shape.CircleShape,
            modifier=Modifier.align(Alignment.BottomEnd).padding(24.dp)) { Icon(Icons.Outlined.Add,"Thêm hoạt động") }
    }
}

@Composable
fun RecordScreen(kind: Int,state: FarmState,back:()->Unit,saved:()->Unit) {
    var land by rememberSaveable { mutableStateOf(state.lands.firstOrNull()?.name ?: "") }
    var crop by rememberSaveable { mutableStateOf("Rau cải") }
    var title by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf(today()) }
    var error by rememberSaveable { mutableStateOf("") }
    Page(listOf("Ghi nhận thu hoạch","Ghi nhận bán hàng","Thêm chi phí","Ghi nhật ký")[kind],back) {
        ChooseField("Thửa đất",state.lands.map { it.name },land) { land=it }
        if(kind<2) ChooseField("Nông sản",cropNames,crop) { crop=it }
        if(kind>=2) Field("Nội dung",title,{title=it},if(kind==2) "Ví dụ: Mua phân hữu cơ" else "Ví dụ: Tưới nước")
        if(kind<3) Field(if(kind==2) "Số tiền (đ)" else "Khối lượng (kg)",amount,{amount=it},"Nhập số lượng",keyboardType=KeyboardType.Number)
        DateField("Ngày thực hiện",date) { date=it }
        if(error.isNotBlank()) Text(error,color=Coral,modifier=Modifier.padding(bottom=16.dp))
        PrimaryButton("Lưu hoạt động") {
            val quantity=amount.toIntOrNull() ?: 0
            val stock=state.products.find { it.name==crop }?.quantity ?: 0
            when {
                land.isBlank() -> error="Vui lòng thêm và chọn thửa đất."
                kind<3 && quantity<=0 -> error="Nhập giá trị nguyên lớn hơn 0."
                kind>=2 && title.isBlank() -> error="Vui lòng nhập nội dung."
                kind==1 && quantity>stock -> error="Kho chỉ còn $stock kg $crop."
                kind==0 && stock.toLong()+quantity>Int.MAX_VALUE -> error="Khối lượng vượt giới hạn cho phép."
                else -> {
                    state.logs.add(0,FarmLog(when(kind) {0->"Thu hoạch ${crop.lowercase()}";1->"Bán ${crop.lowercase()}";else->title.trim()},land,date,kind,quantity))
                    if(kind<2) {
                        val index=state.products.indexOfFirst { it.name==crop }
                        val updated=Product(crop,stock+if(kind==0) quantity else -quantity)
                        if(index<0) state.products.add(updated) else state.products[index]=updated
                    }
                    saved()
                }
            }
        }
    }
}

@Composable
fun ReportScreen(back:()->Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var month by rememberSaveable { mutableStateOf("Tháng 9/2026") }
    val current=month=="Tháng 9/2026"
    val chart=when(tab) {0->if(current) listOf(16,14,16,14,24,18,14,14) else listOf(10,13,14,11,18,15,16,19)
        1->if(current) listOf(100,85,110,95,150,120,110,130) else listOf(85,75,100,80,140,110,100,110)
        else->if(current) listOf(30,20,35,15,50,30,30,40) else listOf(40,30,35,20,50,35,30,40)}
    val maximum=((chart.max()+9)/10)*10
    val segments=when(tab) {0->if(current) listOf(70,40,20) else listOf(60,36,20)
        1->if(current) listOf(485,277,138) else listOf(430,246,124)
        else->if(current) listOf(135,77,38) else listOf(151,86,43)}
    Page("Báo cáo",back) {
        ChooseField("Thời gian",listOf("Tháng 9/2026","Tháng 8/2026"),month) { month=it }
        Tabs(listOf("Sản lượng","Doanh thu","Chi phí"),tab) { tab=it };Spacer(Modifier.height(17.dp))
        FarmCard {
            Text(listOf("Tổng sản lượng","Tổng doanh thu","Tổng chi phí")[tab],color=Muted)
            Text(when(tab) {0->if(current) "130 kg" else "116 kg";1->if(current) "900.000 đ" else "800.000 đ";else->if(current) "250.000 đ" else "280.000 đ"},style=MaterialTheme.typography.headlineLarge,modifier=Modifier.padding(vertical=6.dp))
            Text(if(!current) "Dữ liệu minh họa trong tháng" else if(tab==2) "↘ 10,7% so với tháng trước" else if(tab==1) "↗ 12,5% so với tháng trước" else "↗ 12% so với tháng trước",color=Forest,fontSize=12.sp)
            Text(if(tab==0) "Đơn vị: kg" else "Đơn vị: nghìn đồng",fontSize=10.sp,color=Muted,modifier=Modifier.padding(top=10.dp))
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth().height(165.dp)) {
                Column(Modifier.fillMaxHeight().width(26.dp),verticalArrangement=Arrangement.SpaceBetween) { (3 downTo 0).forEach { Text("${maximum*it/3}",fontSize=9.sp,color=Muted) } }
                Canvas(Modifier.weight(1f).fillMaxHeight().semantics { contentDescription="Biểu đồ mẫu theo tuần, ${chart.joinToString()}" }) {
                    repeat(4) { i -> val y=size.height*i/3;drawLine(Line,Offset(0f,y),Offset(size.width,y),1.dp.toPx()) }
                    val slot=size.width/chart.size
                    chart.forEachIndexed { i,v ->
                        val height=size.height*v/maximum.toFloat()
                        drawRoundRect(if(i%2==0) Forest else Leaf,Offset(slot*i+slot*.24f,size.height-height),Size(slot*.40f,height),androidx.compose.ui.geometry.CornerRadius(7f))
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(start=26.dp,top=10.dp),horizontalArrangement=Arrangement.SpaceAround) { (1..4).forEach { Text("Tuần $it",fontSize=10.sp,color=Muted) } }
        }
        Spacer(Modifier.height(16.dp))
        FarmCard {
            Text(if(tab==0) "Sản lượng theo loại cây" else if(tab==1) "Doanh thu theo loại cây" else "Chi phí theo hạng mục",fontWeight=FontWeight.SemiBold)
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(20.dp)) {
                val colors=listOf(Forest,Leaf,Color(0xFF6AAF9D))
                Canvas(Modifier.size(115.dp).semantics { contentDescription="Phân bổ dữ liệu mẫu: ${segments.joinToString()} ${if(tab==0) "kg" else "nghìn đồng"}" }) {
                    var start=-90f
                    segments.forEachIndexed { i,v ->
                        val sweep=v.toFloat()/segments.sum()*360
                        drawArc(colors[i],start,sweep-3,false,Offset(12.dp.toPx(),12.dp.toPx()),Size(size.width-24.dp.toPx(),size.height-24.dp.toPx()),style=Stroke(19.dp.toPx(),cap=StrokeCap.Butt));start+=sweep
                    }
                }
                Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                    val labels=if(tab==2) listOf("Phân bón","Hạt giống","Vật tư") else listOf("Rau cải","Rau muống","Cà chua")
                    labels.forEachIndexed { i,label ->
                        Row(verticalAlignment=Alignment.CenterVertically) {
                            Box(Modifier.size(7.dp).background(colors[i],androidx.compose.foundation.shape.CircleShape));Spacer(Modifier.width(6.dp))
                            Text(label,fontSize=11.sp,modifier=Modifier.weight(1f))
                            Text("${segments[i]}${if(tab==0) " kg" else "k"}",fontSize=11.sp,fontWeight=FontWeight.Medium)
                        }
                    }
                }
            }
        }
        Text("Báo cáo minh họa • Dữ liệu mẫu tháng 8–9/2026",color=Muted,fontSize=11.sp,modifier=Modifier.padding(top=16.dp))
    }
}

@Composable
fun InventoryScreen(state: FarmState,back:()->Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var dialog by rememberSaveable { mutableStateOf(false) }
    var editing by rememberSaveable { mutableStateOf<String?>(null) }
    var name by rememberSaveable { mutableStateOf("") };var quantity by rememberSaveable { mutableStateOf("") };var error by remember { mutableStateOf("") }
    Page("Kho nông sản",back) {
        Tabs(listOf("Tất cả","Còn hàng","Sắp hết"),tab) { tab=it };Spacer(Modifier.height(20.dp))
        val rows=state.products.filter { tab==0 || (tab==1 && it.quantity>0) || (tab==2 && it.quantity<=10) }
        rows.forEach { product ->
            Row(Modifier.fillMaxWidth().padding(bottom=12.dp).clip(RoundedCornerShape(16.dp)).background(Color.White).clickable {
                editing=product.name;name=product.name;quantity=product.quantity.toString();error="";dialog=true
            }.padding(15.dp),verticalAlignment=Alignment.CenterVertically) {
                CropIcon(product.name);Spacer(Modifier.width(15.dp));Column(Modifier.weight(1f)) { Text(product.name,fontWeight=FontWeight.Medium);Text("Còn ${number(product.quantity)} kg",color=Muted,fontSize=12.sp) }
                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight,null,Modifier.size(18.dp),tint=Muted)
            }
        }
        if(rows.isEmpty()) Text("Không có nông sản trong nhóm này.",color=Muted,modifier=Modifier.padding(vertical=25.dp))
        Spacer(Modifier.height(14.dp));PrimaryButton("＋  Thêm sản phẩm",pale=true) { editing=null;name="";quantity="";error="";dialog=true }
    }
    if(dialog) AlertDialog(onDismissRequest={dialog=false},title={Text(if(editing==null) "Thêm sản phẩm" else "Cập nhật tồn kho")},text={Column {
        Field("Tên nông sản",name,{name=it},"Ví dụ: Rau cải");Field("Khối lượng (kg)",quantity,{quantity=it},"0",keyboardType=KeyboardType.Number)
        if(error.isNotBlank()) Text(error,color=Coral)
    }},confirmButton={TextButton(onClick={
        val parsed=quantity.toIntOrNull()
        if(name.isBlank() || parsed==null || parsed<0) error="Nhập tên và khối lượng hợp lệ."
        else if(state.products.any { it.name.equals(name.trim(),true) && it.name!=editing }) error="Nông sản này đã có trong kho."
        else { val index=state.products.indexOfFirst { it.name==editing };val product=Product(name.trim(),parsed);if(index<0) state.products.add(product) else state.products[index]=product;state.save();dialog=false }
    }) { Text("Lưu") }},dismissButton={TextButton(onClick={dialog=false}) { Text("Hủy") }})
}

@Composable
fun RemindersScreen(state: FarmState,back:()->Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) };var dialog by rememberSaveable { mutableStateOf(false) }
    var title by rememberSaveable { mutableStateOf("") };var land by rememberSaveable { mutableStateOf(state.lands.firstOrNull()?.name ?: "") }
    var date by rememberSaveable { mutableStateOf(today()) };var error by remember { mutableStateOf("") }
    Box(Modifier.fillMaxSize()) {
        Page("Nhắc việc",back) {
            Tabs(listOf("Tất cả","Sắp đến hạn","Đã hoàn thành"),tab) { tab=it };Spacer(Modifier.height(18.dp))
            val format=DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val rows=state.reminders.withIndex().filter { (_,r) -> tab==0 || (tab==2 && r.done) || (tab==1 && !r.done && runCatching { LocalDate.parse(r.date,format)<=LocalDate.now().plusDays(7) }.getOrDefault(false)) }
            rows.forEach { (index,r) ->
                FarmCard {
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        IconTile(when { r.title.contains("Tưới")->Icons.Outlined.WaterDrop;r.title.contains("Thu")->Icons.Outlined.Eco;else->Icons.Outlined.NotificationsNone },if(r.done) Muted else Forest)
                        Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)) { Text(r.title,fontWeight=FontWeight.Medium);Text(r.land,color=Muted,fontSize=12.sp);Text(r.date,color=Muted,fontSize=11.sp) }
                        Checkbox(checked=r.done,onCheckedChange={state.reminders[index]=r.copy(done=it);state.save()},modifier=Modifier.semantics { contentDescription="Hoàn thành ${r.title}" })
                    }
                };Spacer(Modifier.height(12.dp))
            }
            if(rows.isEmpty()) Text("Chưa có công việc trong nhóm này.",color=Muted,modifier=Modifier.padding(vertical=24.dp))
            Text("Danh sách nhắc việc lưu trên máy. Chưa lên lịch thông báo hệ thống.",color=Muted,fontSize=11.sp,modifier=Modifier.padding(top=15.dp))
            Spacer(Modifier.height(70.dp))
        }
        FloatingActionButton(onClick={title="";error="";dialog=true},modifier=Modifier.align(Alignment.BottomEnd).padding(24.dp),containerColor=Forest,contentColor=Color.White,shape=androidx.compose.foundation.shape.CircleShape) { Icon(Icons.Outlined.Add,"Thêm nhắc việc") }
    }
    if(dialog) AlertDialog(onDismissRequest={dialog=false},title={Text("Thêm nhắc việc")},text={Column {
        Field("Công việc",title,{title=it},"Ví dụ: Tưới nước");ChooseField("Thửa đất",state.lands.map { it.name },land) { land=it };DateField("Ngày thực hiện",date) { date=it }
        if(error.isNotBlank()) Text(error,color=Coral)
    }},confirmButton={TextButton(onClick={if(title.isBlank() || land.isBlank()) error="Vui lòng nhập công việc và chọn thửa đất." else {state.reminders.add(Reminder(title.trim(),land,date));state.save();dialog=false}}) { Text("Lưu") }},dismissButton={TextButton(onClick={dialog=false}) { Text("Hủy") }})
}
