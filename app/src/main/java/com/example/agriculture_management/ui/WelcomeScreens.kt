package com.example.agriculture_management.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SplashScreen(next: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Box(Modifier.offset((-80).dp,(-90).dp).size(245.dp).clip(CircleShape).background(Pale.copy(alpha=.5f)))
        FarmArt(Modifier.fillMaxWidth().fillMaxHeight(.34f).align(Alignment.BottomCenter))
        Column(Modifier.fillMaxWidth().fillMaxHeight(.72f).padding(horizontal=48.dp), horizontalAlignment=Alignment.CenterHorizontally,
            verticalArrangement=Arrangement.Center) {
            LeafMark(Modifier.size(112.dp))
            Spacer(Modifier.height(20.dp))
            Text("Nông Sản Xanh",style=MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(15.dp))
            Text("Quản lý nông nghiệp\nDễ dàng hơn mỗi ngày",color=Muted,style=MaterialTheme.typography.bodyLarge,
                textAlign=androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(Modifier.height(44.dp))
            PrimaryButton("Bắt đầu",Modifier.widthIn(max=230.dp),onClick=next)
        }
    }
}

@Composable
fun OnboardingScreen(next: () -> Unit) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    val titles=listOf("Quản lý toàn bộ\ntrong tầm tay","Chăm sóc cây trồng\nđúng thời điểm","Hiểu mùa vụ\nqua từng con số","Nông trại của bạn,\nluôn ở bên bạn")
    val descriptions=listOf("Theo dõi thửa đất, cây trồng, chi phí, doanh thu và năng suất một cách dễ dàng.",
        "Ghi lại hoạt động mỗi ngày và lên lịch tưới nước, bón phân, thu hoạch.",
        "Theo dõi sản lượng và kết quả mùa vụ qua báo cáo trực quan.","Bắt đầu với thửa đất đầu tiên và cùng vun trồng một mùa vụ xanh.")
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(26.dp)) {
        TextButton(onClick=next,modifier=Modifier.align(Alignment.End)) { Text("Bỏ qua",color=Ink) }
        Spacer(Modifier.height(28.dp))
        FarmArt(Modifier.fillMaxWidth().aspectRatio(1.15f).clip(CircleShape),farmer=true)
        Spacer(Modifier.height(36.dp))
        Text(titles[page],style=MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))
        Text(descriptions[page],color=Muted,style=MaterialTheme.typography.bodyLarge)
        Row(Modifier.fillMaxWidth().padding(vertical=30.dp),horizontalArrangement=Arrangement.Center) {
            repeat(4) { Box(Modifier.padding(5.dp).size(if(it==page) 9.dp else 7.dp).clip(CircleShape).background(if(it==page) Forest else Line)) }
        }
        Spacer(Modifier.weight(1f))
        PrimaryButton(if(page==3) "Bắt đầu ngay" else "Tiếp tục") { if(page<3) page++ else next() }
    }
}

@Composable
fun LoginScreen(enter: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var visible by rememberSaveable { mutableStateOf(false) }
    var dialog by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=26.dp),horizontalAlignment=Alignment.CenterHorizontally) {
        Spacer(Modifier.height(55.dp));LeafMark(Modifier.size(60.dp));Spacer(Modifier.height(28.dp))
        Text("Chào mừng trở lại",style=MaterialTheme.typography.headlineLarge,modifier=Modifier.fillMaxWidth())
        Text("Đăng nhập để tiếp tục quản lý nông trại\ncủa bạn",color=Muted,modifier=Modifier.fillMaxWidth().padding(top=10.dp,bottom=32.dp))
        OutlinedTextField(email,{email=it;error=false},Modifier.fillMaxWidth(),singleLine=true,placeholder={Text("Email hoặc số điện thoại",fontSize=14.sp)},
            leadingIcon={Icon(Icons.Outlined.MailOutline,null)},shape=androidx.compose.foundation.shape.RoundedCornerShape(12.dp),isError=error)
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(password,{password=it;error=false},Modifier.fillMaxWidth(),singleLine=true,placeholder={Text("Mật khẩu",fontSize=14.sp)},
            leadingIcon={Icon(Icons.Outlined.Lock,null)},trailingIcon={IconButton(onClick={visible=!visible}) { Icon(if(visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,if(visible) "Ẩn mật khẩu" else "Hiện mật khẩu") }},
            visualTransformation=if(visible) VisualTransformation.None else PasswordVisualTransformation(),shape=androidx.compose.foundation.shape.RoundedCornerShape(12.dp),isError=error)
        if(error) Text("Vui lòng nhập email/số điện thoại và mật khẩu.",color=Coral,fontSize=12.sp)
        TextButton(onClick={dialog="Khôi phục mật khẩu cần được kết nối với hệ thống tài khoản. Bạn có thể dùng bản trải nghiệm bên dưới."},modifier=Modifier.align(Alignment.End)) { Text("Quên mật khẩu?",fontSize=12.sp) }
        Spacer(Modifier.height(15.dp))
        PrimaryButton("Đăng nhập") { if(email.isBlank() || password.isBlank()) error=true else dialog="Đây là giao diện mẫu, chưa xác thực tài khoản. Chọn Trải nghiệm giao diện để khám phá ứng dụng." }
        Text("Hoặc đăng nhập với",color=Muted,fontSize=12.sp,modifier=Modifier.padding(top=26.dp,bottom=15.dp))
        Row(horizontalArrangement=Arrangement.spacedBy(20.dp)) {
            OutlinedButton(onClick={dialog="Đăng nhập Google chưa được kết nối trong bản giao diện mẫu."},shape=androidx.compose.foundation.shape.RoundedCornerShape(12.dp)) { Text("G",color=Color(0xFF4285F4),fontSize=22.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.width(8.dp));Text("Google",color=Ink) }
            OutlinedButton(onClick={dialog="Đăng nhập Apple chưa được kết nối trong bản giao diện mẫu."},shape=androidx.compose.foundation.shape.RoundedCornerShape(12.dp)) { Icon(Icons.Outlined.PhoneIphone,null);Text("Apple",color=Ink) }
        }
        Spacer(Modifier.height(28.dp))
        TextButton(onClick=enter) { Text("Trải nghiệm giao diện",fontWeight=FontWeight.Bold) }
        Text("Dữ liệu mẫu • Lưu trên thiết bị",color=Muted,fontSize=11.sp)
        Spacer(Modifier.weight(1f));Spacer(Modifier.height(28.dp))
        TextButton(onClick={dialog="Tạo tài khoản sẽ được bổ sung khi kết nối dịch vụ xác thực."}) { Text("Chưa có tài khoản? Đăng ký") }
    }
    if(dialog.isNotEmpty()) AlertDialog(onDismissRequest={dialog=""},title={Text("Tài khoản")},text={Text(dialog)},confirmButton={TextButton(onClick={dialog=""}) { Text("Đã hiểu") }})
}

@Preview(showBackground=true,device="spec:width=393dp,height=852dp,dpi=440")
@Composable
private fun SplashPreview() { FarmTheme { SplashScreen {} } }
