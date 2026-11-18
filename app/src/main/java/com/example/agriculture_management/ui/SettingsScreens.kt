package com.example.agriculture_management.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agriculture_management.feature.operations.FarmOperationsViewModel

@Composable
fun SettingsScreen(state: FarmState,back:()->Unit,go:(Screen)->Unit,message:(String)->Unit,operationsViewModel: FarmOperationsViewModel? = null) {
    var info by remember { mutableStateOf("") };var restore by remember { mutableStateOf(false) }
    Page("Cài đặt",back) {
        FarmCard {
            MenuRow(Icons.Outlined.AccountCircle,"Hồ sơ cá nhân") { go(Screen.Profile) };HorizontalDivider(color=Line)
            MenuRow(Icons.Outlined.Straighten,"Đơn vị hiển thị","kg, m², VNĐ") { info="Ứng dụng hiện sử dụng kilôgam (kg), mét vuông (m²) và Việt Nam đồng (VNĐ)." };HorizontalDivider(color=Line)
            MenuRow(Icons.Outlined.Language,"Ngôn ngữ","Tiếng Việt") { info="Giao diện hiện hỗ trợ Tiếng Việt." };HorizontalDivider(color=Line)
            Row(Modifier.fillMaxWidth().padding(start=12.dp),verticalAlignment=Alignment.CenterVertically) {
                Icon(Icons.Outlined.NotificationsNone,null,tint=Forest,modifier=Modifier.size(21.dp));Text("Thông báo",Modifier.weight(1f).padding(start=13.dp),fontSize=14.sp)
                Switch(checked=state.notifications,onCheckedChange={state.notifications=it;state.save();message("Đã lưu tùy chọn. Bản mẫu chưa gửi thông báo hệ thống.")})
            };HorizontalDivider(color=Line)
            MenuRow(Icons.Outlined.CloudUpload,"Sao lưu dữ liệu") { go(Screen.Backup) };HorizontalDivider(color=Line)
            MenuRow(Icons.Outlined.Restore,"Khôi phục dữ liệu") { restore=true };HorizontalDivider(color=Line)
            MenuRow(Icons.Outlined.Info,"Về ứng dụng") { info="Nông Sản Xanh 1.0\n\nGiao diện quản lý nông trại bằng Kotlin và Jetpack Compose. Dữ liệu trải nghiệm được lưu trên thiết bị. Tài khoản, thông báo hệ thống và đồng bộ đám mây chưa được kết nối." }
        }
        Text("Nông Sản Xanh · Phiên bản giao diện 1.0",color=Muted,fontSize=11.sp,modifier=Modifier.align(Alignment.CenterHorizontally).padding(top=30.dp))
    }
    InfoDialog("Thông tin",info) { info="" }
    if(restore) AlertDialog(onDismissRequest={restore=false},title={Text("Khôi phục bản sao lưu?")},text={Text("Dữ liệu thửa đất, kho, nhật ký và nhắc việc hiện tại sẽ được thay bằng bản sao lưu gần nhất trên thiết bị.")},
        confirmButton={TextButton(onClick={val json = operationsViewModel?.uiState?.value?.lastBackupJson; if (json != null) operationsViewModel?.restoreBackupJson(json); message(if(state.restore()) "Đã khôi phục dữ liệu" else "Chưa có bản sao lưu hợp lệ"); restore=false}) { Text("Khôi phục") }},dismissButton={TextButton(onClick={restore=false}) { Text("Hủy") }})
}

@Composable
fun BackupScreen(state: FarmState,back:()->Unit,message:(String)->Unit,operationsViewModel: FarmOperationsViewModel? = null) {
    val context=LocalContext.current
    val export=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if(uri!=null) message(if(runCatching { checkNotNull(context.contentResolver.openOutputStream(uri)).bufferedWriter(Charsets.UTF_8).use { it.write(operationsViewModel?.uiState?.value?.lastBackupJson ?: state.snapshot()) } }.isSuccess) "Đã xuất bản sao lưu" else "Không thể ghi tệp sao lưu")
    }
    Page("Sao lưu dữ liệu",back) {
        Column(Modifier.fillMaxWidth().padding(vertical=18.dp),horizontalAlignment=Alignment.CenterHorizontally) {
            IconTile(Icons.Outlined.CloudUpload,Forest,90)
            Text("Bảo vệ dữ liệu nông trại của bạn",fontWeight=FontWeight.SemiBold,modifier=Modifier.padding(top=22.dp,bottom=8.dp))
            Text("Sao lưu thường xuyên để tránh mất dữ liệu.",color=Muted,fontSize=13.sp)
        }
        PrimaryButton("Sao lưu ngay") { state.backup();operationsViewModel?.exportBackupJson();message("Đã tạo bản sao lưu trên thiết bị") }
        Spacer(Modifier.height(10.dp));PrimaryButton("Xuất bản sao lưu ra tệp",pale=true) { export.launch("nong-san-xanh-${today().replace('/','-')}.json") }
        Section("Sao lưu gần đây")
        if(state.backupDate.isBlank()) FarmCard { Text("Chưa có bản sao lưu. Hãy tạo bản đầu tiên.",color=Muted) }
        else FarmCard {
            Row(verticalAlignment=Alignment.CenterVertically) { IconTile(Icons.Outlined.TaskAlt);Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)) { Text(state.backupDate,fontWeight=FontWeight.Medium);Text("Bản gần nhất trên thiết bị",color=Muted,fontSize=12.sp) };Badge("Đã lưu") }
        }
        Text("Bản sao lưu tại chỗ được thay thế mỗi lần lưu. Xuất ra tệp để giữ nhiều phiên bản. Ảnh đã chọn vẫn tham chiếu đến thư viện ảnh của thiết bị.",color=Muted,fontSize=12.sp,modifier=Modifier.padding(top=20.dp))
    }
}

@Composable
fun ProfileScreen(state: FarmState,back:()->Unit,operationsViewModel: FarmOperationsViewModel? = null,logout:()->Unit) {
    var edit by rememberSaveable { mutableStateOf(false) };var name by rememberSaveable { mutableStateOf(state.name) }
    var info by remember { mutableStateOf("") };var confirmLogout by remember { mutableStateOf(false) }
    Page("Hồ sơ cá nhân",back) {
        Box(Modifier.fillMaxWidth().padding(bottom=30.dp),contentAlignment=Alignment.Center) {
            Box(Modifier.size(105.dp).clip(CircleShape).background(Pale),contentAlignment=Alignment.Center) { LeafMark(Modifier.size(68.dp)) }
        }
        FarmCard {
            MenuRow(Icons.Outlined.PersonOutline,state.name) { name=state.name;edit=true };HorizontalDivider(color=Line)
            MenuRow(Icons.Outlined.MailOutline,"nguyenvanan@email.com") { info="Địa chỉ email minh họa. Bản mẫu chưa kết nối tài khoản." };HorizontalDivider(color=Line)
            MenuRow(Icons.Outlined.Phone,"+84 912 345 678") { info="Số điện thoại minh họa. Bản mẫu chưa kết nối tài khoản." };HorizontalDivider(color=Line)
            MenuRow(Icons.Outlined.Lock,"Đổi mật khẩu") { info="Đổi mật khẩu sẽ khả dụng khi kết nối dịch vụ xác thực." }
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick={confirmLogout=true},modifier=Modifier.fillMaxWidth().height(50.dp),shape=androidx.compose.foundation.shape.RoundedCornerShape(12.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFFF9E3E0),contentColor=Coral)) { Text("Đăng xuất") }
        Text("Hồ sơ trải nghiệm trên thiết bị",color=Muted,fontSize=11.sp,modifier=Modifier.align(Alignment.CenterHorizontally).padding(top=16.dp))
    }
    InfoDialog("Tài khoản",info) { info="" }
    if(edit) AlertDialog(onDismissRequest={edit=false},title={Text("Tên hiển thị")},text={Field("Họ và tên",name,{name=it})},confirmButton={TextButton(onClick={if(name.isNotBlank()) {val newName=name.trim();state.name=newName;state.save();operationsViewModel?.updateProfile(displayName = newName, email = "nguyenvanan@email.com");edit=false}},enabled=name.isNotBlank()) { Text("Lưu") }},dismissButton={TextButton(onClick={edit=false}) { Text("Hủy") }})
    if(confirmLogout) AlertDialog(onDismissRequest={confirmLogout=false},title={Text("Thoát trải nghiệm?")},text={Text("Dữ liệu trên thiết bị vẫn được giữ lại.")},confirmButton={TextButton(onClick=logout) { Text("Đăng xuất") }},dismissButton={TextButton(onClick={confirmLogout=false}) { Text("Ở lại") }})
}

@Composable
fun MoreScreen(state: FarmState,back:()->Unit,go:(Screen)->Unit,message:(String)->Unit) {
    val context=LocalContext.current
    var info by remember { mutableStateOf("") };var exportDialog by remember { mutableStateOf(false) }
    val csv=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if(uri!=null) message(if(runCatching {
            fun cell(value: String): String { val safe=if(value.startsWith('=') || value.startsWith('+') || value.startsWith('-') || value.startsWith('@')) "'$value" else value;return "\"${safe.replace("\"","\"\"")}\"" }
            val content="\uFEFFNgày,Hoạt động,Chi tiết,Giá trị,Đơn vị\r\n"+state.logs.joinToString("\r\n") { listOf(it.date,it.title,it.detail,it.amount.toString(),if(it.kind<2) "kg" else if(it.kind==2) "VND" else "").joinToString(",",transform=::cell) }
            checkNotNull(context.contentResolver.openOutputStream(uri)).bufferedWriter(Charsets.UTF_8).use { it.write(content) }
        }.isSuccess) "Đã xuất nhật ký CSV" else "Không thể xuất tệp")
    }
    val pdf=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if(uri!=null) message(if(runCatching {
            val document=android.graphics.pdf.PdfDocument()
            try {
                val paint=android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { textSize=12f;color=android.graphics.Color.rgb(40,60,35) }
                val rows=listOf("NÔNG SẢN XANH — NHẬT KÝ NÔNG TRẠI","Ngày xuất: ${today()}","")+state.logs.flatMap { log ->
                    listOf("${log.date} • ${log.title}","${log.detail} • ${log.amount} ${if(log.kind<2) "kg" else if(log.kind==2) "VND" else ""}","")
                }
                rows.chunked(32).forEachIndexed { index,lines ->
                    val page=document.startPage(android.graphics.pdf.PdfDocument.PageInfo.Builder(595,842,index+1).create())
                    lines.forEachIndexed { line,text -> page.canvas.drawText(text.take(80),36f,48f+line*23f,paint) };document.finishPage(page)
                }
                checkNotNull(context.contentResolver.openOutputStream(uri)).use { document.writeTo(it) }
            } finally { document.close() }
        }.isSuccess) "Đã xuất nhật ký PDF" else "Không thể xuất tệp")
    }
    Page("Thêm",back) {
        FarmCard {
            MenuRow(Icons.Outlined.FileDownload,"Xuất dữ liệu (CSV/PDF)") { exportDialog=true }
            MenuRow(Icons.Outlined.Share,"Chia sẻ ứng dụng") { context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type="text/plain";putExtra(Intent.EXTRA_TEXT,"Nông Sản Xanh — Quản lý thửa đất, mùa vụ, nhật ký và kho nông sản trong một ứng dụng.") },"Chia sẻ Nông Sản Xanh")) }
            MenuRow(Icons.Outlined.ChatBubbleOutline,"Góp ý / Báo lỗi") { info="Đây là bản giao diện mẫu. Hãy ghi lại màn hình, thao tác và lỗi gặp phải để gửi cho nhóm phát triển của bạn." }
            MenuRow(Icons.Outlined.HelpOutline,"Trung tâm trợ giúp") { info="1. Vào Thửa đất → Thêm để tạo thửa đất.\n2. Chọn thửa đất → Thêm vụ trồng.\n3. Dùng Ghi nhanh để ghi thu hoạch, bán hàng hoặc chi phí.\n4. Quản lý tồn kho và nhắc việc từ Trang chủ.\n5. Vào Cài đặt → Sao lưu để giữ dữ liệu." }
            MenuRow(Icons.Outlined.Description,"Điều khoản sử dụng") { info="Bản mẫu phục vụ thiết kế và thử nghiệm giao diện. Điều khoản sử dụng chính thức cần được bổ sung trước khi phát hành ứng dụng." }
            MenuRow(Icons.Outlined.VerifiedUser,"Chính sách bảo mật") { info="Dữ liệu giao diện được lưu cục bộ. Bản mẫu không gửi tài khoản hay dữ liệu nông trại lên máy chủ. Tệp xuất được ghi vào vị trí bạn chọn. Cần hoàn thiện chính sách trước khi phát hành." }
        }
        Spacer(Modifier.height(16.dp));PrimaryButton("Cài đặt",pale=true) { go(Screen.Settings) }
        TextButton(onClick={go(Screen.Empty)},modifier=Modifier.align(Alignment.CenterHorizontally)) { Text("Xem giao diện khi chưa có dữ liệu",fontSize=12.sp) }
    }
    InfoDialog("Thông tin",info) { info="" }
    if(exportDialog) AlertDialog(onDismissRequest={exportDialog=false},title={Text("Xuất nhật ký")},text={Text("Chọn định dạng để lưu danh sách hoạt động nông trại.")},confirmButton={TextButton(onClick={exportDialog=false;csv.launch("nhat-ky-nong-trai.csv")}) { Text("CSV") }},dismissButton={TextButton(onClick={exportDialog=false;pdf.launch("nhat-ky-nong-trai.pdf")}) { Text("PDF") }})
}

@Composable
private fun InfoDialog(title: String,body: String,dismiss:()->Unit) {
    if(body.isNotBlank()) AlertDialog(onDismissRequest=dismiss,title={Text(title)},text={Text(body)},confirmButton={TextButton(onClick=dismiss) { Text("Đã hiểu") }})
}
