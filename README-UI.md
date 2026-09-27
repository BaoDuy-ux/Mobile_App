# Nông Sản Xanh — Kotlin / Jetpack Compose

Giao diện Android dựa trên 18 màn hình trong ảnh tham chiếu. Mã giao diện nằm trong `app/src/main/java/com/example/agriculture_management/ui`.

## Chạy trong Android Studio

1. Mở thư mục **Mobile**, đợi Gradle Sync hoàn tất.
2. Chọn máy ảo/điện thoại **Android 16 (API 36) trở lên**, đúng cấu hình `minSdk` có sẵn của dự án.
3. Run cấu hình **app**.
4. Chọn **Bắt đầu → Bỏ qua → Trải nghiệm giao diện** để mở dữ liệu mẫu.

Để xem riêng 18 màn hình, mở **ScreenGallery.kt**, chọn **Split/Design** rồi **Build & Refresh**. `FarmScreenPreviewProvider` cung cấp từng màn hình cho Compose Preview.

## Màn hình và đường dẫn thao tác

| Ảnh mẫu | Màn hình | Cách mở |
|---|---|---|
| 01 | Splash | Khởi động app |
| 02 | Onboarding, 4 trang | Bắt đầu |
| 03 | Đăng nhập | Bỏ qua / hoàn thành giới thiệu |
| 04 | Tổng quan | Trải nghiệm giao diện |
| 05 | Thửa đất, lọc trạng thái | Thanh điều hướng |
| 06 | Chi tiết, 4 tab | Chọn một thửa đất |
| 07 | Thêm/chỉnh sửa thửa đất | Thửa đất → Thêm / Chi tiết → Chỉnh sửa |
| 08 | Thêm vụ trồng | Banner trang chủ / Chi tiết / Ghi nhanh |
| 09 | Ghi nhanh | Trang chủ → Ghi nhanh hoạt động |
| 10 | Nhật ký, lọc tháng/loại | Thanh điều hướng |
| 11 | Báo cáo, biểu đồ cột/vòng | Thanh điều hướng |
| 12 | Kho nông sản | Trang chủ / Ghi nhanh |
| 13 | Nhắc việc | Trang chủ / Ghi nhanh |
| 14 | Cài đặt | Biểu tượng bánh răng trên Trang chủ |
| 15 | Sao lưu | Cài đặt → Sao lưu dữ liệu |
| 16 | Hồ sơ | Avatar Trang chủ / Cài đặt |
| 17 | Thêm | Nút ba chấm trên Trang chủ |
| 18 | Chưa có dữ liệu | Thêm → Xem giao diện khi chưa có dữ liệu; các danh sách rỗng cũng có thông báo |

## Đã nối tương tác

- Điều hướng và nút Back Android; giữ trang/form khi xoay màn hình.
- Chọn ảnh từ thiết bị; thêm/sửa thửa đất; kiểm tra tên và diện tích.
- Chọn loại cây, diện tích và ngày bằng lịch Android; kiểm tra thời gian và phần đất còn lại.
- Thu hoạch tăng tồn kho; bán hàng giảm tồn kho và không cho bán quá số lượng hiện có.
- Thêm chi phí, ghi nhật ký; bộ lọc nhật ký, kho và nhắc việc.
- Thêm nhắc việc và đánh dấu hoàn thành; sửa tên hiển thị.
- Lưu dữ liệu giao diện cục bộ, tạo/khôi phục bản sao lưu tại chỗ, xuất JSON.
- Xuất nhật ký CSV/PDF qua trình chọn tệp của Android; chia sẻ qua bảng chia sẻ hệ thống.

## Các tệp chính

- `GreenFarmApp.kt`: điểm vào Compose và điều hướng.
- `FarmDesign.kt`: màu, chữ, thẻ, nút, form, logo lá và minh họa vector Canvas.
- `WelcomeScreens.kt`: mở đầu, giới thiệu, đăng nhập.
- `FarmScreens.kt`: tổng quan, thửa đất, vụ trồng, trạng thái trống.
- `ActivityScreens.kt`: ghi nhanh, nhật ký, báo cáo, kho, nhắc việc.
- `SettingsScreens.kt`: cài đặt, sao lưu, hồ sơ, tiện ích.
- `FarmState.kt`: dữ liệu mẫu và lưu cục bộ cho giao diện; độc lập với các lớp domain/recovery hiện có.
- `ScreenGallery.kt`: bộ Preview cho 18 màn hình.

## Phạm vi bản giao diện

Minh họa nông trại và rau/cà chua/ớt được dựng lại bằng vector, không phải ảnh gốc. Icon chức năng dùng Material; bố cục co giãn và cuộn để dùng trên điện thoại nhỏ.

Đăng nhập email/Google/Apple, đăng ký, đổi mật khẩu, thông báo hệ thống và đồng bộ đám mây **chưa kết nối dịch vụ thật**. Nút tương ứng giải thích rõ trạng thái. Báo cáo và hai chỉ số sản lượng/doanh thu trên Trang chủ dùng **số liệu minh họa**. Các danh sách và thao tác ghi nhanh lưu thật trên thiết bị.

Sao lưu tại chỗ giữ một phiên bản gần nhất; tệp JSON xuất ra giúp lưu nhiều phiên bản. Ảnh dùng URI thư viện của thiết bị, chưa được đóng gói trong sao lưu. Nút Khôi phục dùng bản tại chỗ, chưa nhập JSON từ thiết bị khác.

Chưa nối `FarmState` với repository/domain có sẵn vì các lớp này đang là khung. Khi triển khai nghiệp vụ thật, chuyển trạng thái màn hình sang ViewModel/StateFlow, nối repository/Room và dịch vụ xác thực/thông báo.

## Kiểm tra

```text
gradlew.bat :app:assembleDebug :app:testDebugUnitTest
gradlew.bat :app:connectedDebugAndroidTest
```

`ComposeFlowTest` kiểm tra luồng vào danh sách đất, kiểm tra form, giới hạn tồn kho, lưu/khôi phục và dựng đủ 18 màn hình. Dữ liệu kiểm thử sử dụng vùng lưu riêng. Ảnh kiểm tra được tạo trong thư mục `ui-review` thuộc external files của ứng dụng kiểm thử.

Kết quả kiểm tra trên máy này: tạo APK thành công, 3 unit test và 6 kiểm thử trên máy giả lập đều đạt; Android Lint không có lỗi. Các ảnh chụp riêng từng màn hình đã được sao chép vào **Mobile/ui-review**. Ảnh riêng không bao gồm thanh điều hướng chung; thanh điều hướng có trong app khi chạy bình thường. Máy giả lập sẵn có mang tên API 36 nhưng thực tế dùng system image Android 17.

Cấu hình Compose Compiler dùng cùng phiên bản Kotlin theo [hướng dẫn Android](https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler). Giữ nguyên cấu hình SDK của dự án.
