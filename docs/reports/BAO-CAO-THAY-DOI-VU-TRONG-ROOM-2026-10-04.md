# Báo cáo thay đổi code: kết nối Vụ trồng với Room

**Ngày lập:** 04/10/2026  
**Project:** Agriculture_management / Nông Sản Xanh  
**Nhánh:** Update27thang9  
**Mốc so sánh:** commit `d8ab452` với mã hiện tại trong thư mục làm việc.

## 1. Kết luận và phạm vi

Thay đổi chính là chuyển thao tác **tạo và đọc vụ trồng** từ `FarmState` sang Room/SQLite, đồng thời nối dữ liệu vụ trồng với thửa đất bằng ID.

Trong đợt triển khai có **17 file liên quan: 7 file mới và 10 file chỉnh sửa**. Các con số này không tính chính báo cáo đang đọc. Thay đổi hiện chưa được commit hoặc push trong đợt triển khai này.

Đã build APK thành công và chạy qua 3 unit test hiện có. Đã viết thêm 8 bài kiểm thử Android và biên dịch được mã test; chưa chạy thành công các bài này do máy ảo không khởi động ổn định. Vì vậy, cần phân biệt **đường xử lý đã được viết** với **hành vi đã kiểm chứng trên thiết bị**.

**Phát hiện khi lập báo cáo:** `LandPlotRepository.updatePlot()` có một khối `withTransaction` và các kiểm tra bị lặp trong chính khối transaction bên ngoài. Đây là lỗi lặp code trong lần sửa trước. Báo cáo ghi rõ ở mục 9; lượt lập báo cáo không chỉnh sửa thêm mã ứng dụng.

## 2. Vấn đề trước khi sửa

Trước đây, app có hai danh sách thửa đất khác nhau:

| Nơi giữ dữ liệu | Được dùng ở đâu? | Ví dụ ID |
|---|---|---|
| Room / bảng `plots` | Danh sách, thêm và sửa thửa đất | `plot-1`, `plot-2` hoặc UUID |
| `FarmState.lands` | Form thêm vụ trồng mẫu | `1`, `2`, `3` |

Hàm `AddPlantingScreen` cũ nằm trong `FarmScreens.kt`. Khi người dùng lưu, code sửa `Land.crop`, `cropAreas`, ngày trồng và ngày thu hoạch trong `FarmState`, sau đó ghi JSON vào SharedPreferences.

Điều này dẫn đến ba vấn đề:

1. Thửa đất mới tạo trong Room không xuất hiện trong danh sách chọn thửa đất của form vụ trồng.
2. ID được truyền từ thửa đất Room không khớp bộ dữ liệu mẫu; form có thể chọn thửa đầu tiên trong `FarmState`.
3. Lưu vụ trồng không tạo bản ghi trong bảng `plantings`; chi tiết thửa đất Room không nhận được thông tin cây trồng đó.

## 3. Luồng mới sau khi sửa

### 3.1. Chiều ghi dữ liệu

```text
AddPlantingScreen
  Tạo yêu cầu CreatePlanting từ thông tin người dùng nhập
        ↓
GreenFarmApp
  Chuyển yêu cầu đến ViewModel
        ↓
LandPlotViewModel.createPlanting()
  Kiểm soát trạng thái đang lưu, gọi Repository và nhận kết quả
        ↓
PlantingRepository.create()
  Kiểm tra ngày, diện tích, ID và quan hệ nông trại
        ↓
PlantingDao.insert()
  Thêm PlantingEntity vào bảng plantings
        ↓
SQLite: nong_san_xanh.db
```

### 3.2. Chiều đọc dữ liệu

```text
Room quan sát plots, crops và plantings
        ↓
Các DAO phát Flow
        ↓
ViewModel dùng combine() lấy giá trị mới nhất từ ba luồng
        ↓
LandPlotUiState chứa plots, crops và plantings
        ↓
GreenFarmApp ghép các vụ theo plotId
        ↓
Danh sách và chi tiết thửa đất hiển thị dữ liệu mới
```

`combine()` gom giá trị mới nhất của các Flow. Nó không biến ba truy vấn thành một truy vấn hay bảo đảm chúng cùng là một ảnh chụp database tại một thời điểm. Quy tắc diện tích vẫn được kiểm tra trực tiếp trong transaction ở Repository, không dựa vào trạng thái đang hiển thị.

### 3.3. Ví dụ cụ thể

Thửa đất có 100 m², đang dùng 60 m². Người dùng tạo thêm vụ có diện tích 20,50 m²:

- Form chuyển `20,50` thành `2050`, là số đơn vị 1/100 m².
- Repository quy đổi diện tích đất: `100 × 100 = 10000`.
- DAO trả tổng diện tích đang giữ: `6000`.
- Phần còn lại: `10000 - 6000 = 4000`, tương đương 40 m².
- `2050 <= 4000`, nên có thể tạo vụ nếu các điều kiện khác hợp lệ.
- Bản ghi mới có ID riêng, `plotId`, `cropId`, các ngày theo ISO và trạng thái `ACTIVE`.

Nếu yêu cầu là 40,01 m² thì phải bị từ chối, vì `4001 > 4000`.

## 4. Danh sách toàn bộ file mới

Các đường dẫn ngắn dưới đây tính từ `app/src/main/java/com/example/agriculture_management/`, trừ hai file test.

| File | Mục đích |
|---|---|
| `domain/model/Planting.kt` | Định nghĩa yêu cầu tạo vụ và dữ liệu trả về để hiển thị |
| `data/local/dao/CropDao.kt` | Đọc cây theo nông trại, tìm cây và thêm danh mục mặc định |
| `data/local/dao/PlantingDao.kt` | Quan sát vụ, tính diện tích đang dùng và thêm bản ghi |
| `data/repository/PlantingRepository.kt` | Kiểm tra nghiệp vụ, khởi tạo cây mặc định và lưu vụ trong transaction |
| `ui/AddPlantingScreen.kt` | Form nhập mới dùng dữ liệu Room; định dạng diện tích và ngày |
| `app/src/androidTest/java/com/example/agriculture_management/PlantingRepositoryTest.kt` | 7 bài kiểm thử database và Repository |
| `app/src/androidTest/java/com/example/agriculture_management/PlantingFlowTest.kt` | 1 bài kiểm thử form lưu vụ và chi tiết cập nhật qua Flow |

## 5. Giải thích các model mới

Mở [Planting.kt](G:/Documents/2026/my-project/Mobile/app/src/main/java/com/example/agriculture_management/domain/model/Planting.kt:3).

File này có ba `data class`:

### `CropOption`

Gồm `id`, `farmId`, `name`. Nó là dữ liệu gọn để chọn một loại cây trên form. Cây được chọn bằng ID; tên chỉ phục vụ hiển thị.

### `PlantingSummary`

Gồm ID vụ, ID thửa đất, ID cây, tên cây, diện tích, ngày trồng, ngày thu hoạch dự kiến và trạng thái. Đây là kết quả được lấy từ truy vấn ghép các bảng.

Tên cây nằm trong bảng `crops`, nên `PlantingDao` dùng JOIN để đưa `cropName` vào kết quả này. Nó giúp màn hình không phải tự tìm tên cây theo từng ID.

### `CreatePlanting`

Là yêu cầu tạo một vụ mới, gồm:

| Trường | Ý nghĩa |
|---|---|
| `plotId` | Trồng trên thửa đất nào |
| `cropId` | Trồng cây gì |
| `areaM2x100` | Diện tích theo đơn vị 1/100 m² |
| `plantedOn` | Ngày trồng, chuỗi ISO `yyyy-MM-dd` |
| `expectedHarvestOn` | Ngày thu hoạch dự kiến, cùng định dạng ISO |

Ba model này không có `@Entity`, nên không tạo thêm bảng. `CropEntity` và `PlantingEntity` đã tồn tại từ trước và vẫn là các lớp mô tả bảng.

## 6. Giải thích hai DAO mới

### 6.1. CropDao

Mở [CropDao.kt](G:/Documents/2026/my-project/Mobile/app/src/main/java/com/example/agriculture_management/data/local/dao/CropDao.kt:9).

| Hàm | Cách hoạt động |
|---|---|
| `observeCrops(farmId)` | Chọn cây thuộc nông trại, chưa bị ẩn; sắp theo tên; trả `Flow<List<CropOption>>` |
| `findById(id)` | Lấy một cây chưa bị ẩn để kiểm tra trước khi tạo vụ |
| `insertDefaults(crops)` | Thêm danh mục cây mặc định; dùng `IGNORE` khi có xung đột ràng buộc |

`IGNORE` kết hợp với ID ổn định và chỉ mục unique `(farmId, name)` đã có giúp tránh tạo thêm dòng trùng mỗi lần mở app. Nó không có nghĩa dữ liệu cũ sẽ được cập nhật hoặc cây đã ẩn sẽ tự được bật lại.

### 6.2. PlantingDao

Mở [PlantingDao.kt](G:/Documents/2026/my-project/Mobile/app/src/main/java/com/example/agriculture_management/data/local/dao/PlantingDao.kt:9).

**`observeCurrent(farmId)`** ghép ba bảng:

- `plantings`: lấy thông tin vụ.
- `crops`: lấy tên cây.
- `plots`: xác định nông trại và loại thửa đất đã ẩn.

Chỉ các vụ có trạng thái `ACTIVE` hoặc `PLANNED` và `endedOn IS NULL` được trả về. Các vụ được sắp theo ngày trồng giảm dần, rồi thời gian tạo và ID.

**`occupiedArea(plotId)`** cộng `areaM2x100` của các vụ chưa kết thúc với hai trạng thái trên. `COALESCE(..., 0)` bảo đảm chưa có vụ thì kết quả là 0.

**`insert(planting)`** thêm một `PlantingEntity`; `ABORT` báo lỗi nếu vi phạm ràng buộc.

Quy tắc hiện tại giữ diện tích cho mọi vụ chưa kết thúc, kể cả vụ dự kiến. Code chưa tính khả năng dùng lại cùng diện tích cho các vụ ở những khoảng ngày không trùng nhau.

## 7. PlantingRepository: nơi xử lý nghiệp vụ mới

Mở [PlantingRepository.kt](G:/Documents/2026/my-project/Mobile/app/src/main/java/com/example/agriculture_management/data/repository/PlantingRepository.kt:13).

### 7.1. Chuẩn bị danh mục cây

`ensureDefaultCrops(farmId)` kiểm tra nông trại tồn tại rồi thêm năm cây: Rau cải, Rau muống, Cà chua, Ớt, Khác. ID có cấu trúc chứa `farmId` và chỉ số cây. Việc khởi tạo nằm trong transaction.

### 7.2. Tạo vụ trồng

Hàm `create()` bắt đầu tại dòng 27. Các bước được viết theo thứ tự:

1. Chặn diện tích không dương.
2. Đọc hai chuỗi ngày bằng `LocalDate.parse()`.
3. Chặn ngày thu hoạch trước ngày trồng.
4. Mở transaction.
5. Đọc thửa đất; từ chối nếu không tồn tại hoặc đã ẩn.
6. Đọc cây; từ chối nếu không tồn tại hoặc đã ẩn.
7. Kiểm tra cả cây và đất thuộc đúng `farmId` được truyền vào.
8. Đổi diện tích đất sang đơn vị 1/100 m² bằng `Math.multiplyExact()`.
9. Đọc tổng diện tích đang giữ từ database.
10. Từ chối nếu diện tích vụ mới vượt phần còn lại.
11. Tạo UUID và thời gian, rồi ghi `PlantingEntity`.
12. Trả ID vụ mới bằng `Result.success()`.

### 7.3. Vì sao kiểm tra và ghi phải trong cùng transaction?

Nếu hai lần lưu cùng đọc thấy còn 100 m² rồi mỗi lần thêm 60 m², tổng có thể thành 120 m² nếu bước kiểm tra và bước ghi bị tách rời.

Code đặt việc đọc phần còn lại và thêm vụ trong cùng transaction để ngăn tình huống này trong luồng ghi hiện tại. Đã viết bài test hai yêu cầu đồng thời để xác minh; bài đó chưa chạy thành công trên Android trong môi trường hiện tại.

### 7.4. Trả lỗi

Các lỗi kiểm tra hoặc lỗi database được chuyển thành `Result.failure()`. Riêng `CancellationException` được ném lại để việc hủy coroutine vẫn hoạt động bình thường, thay vì bị coi như một lỗi nghiệp vụ.

Kiểm tra cùng nông trại là kiểm tra tính nhất quán dữ liệu. Nó chưa phải cơ chế phân quyền tài khoản hoặc xác thực server.

## 8. Các file được chỉnh sửa và lý do

### 8.1. FarmDatabase.kt

Mở [FarmDatabase.kt](G:/Documents/2026/my-project/Mobile/app/src/main/java/com/example/agriculture_management/data/local/database/FarmDatabase.kt:32).

Bổ sung hai hàm `cropDao()` và `plantingDao()` cùng các import. Repository từ đó có thể lấy implementation DAO do Room sinh.

**Không thêm bảng hoặc đổi cột trong lần này.** Hai bảng `crops` và `plantings` đã được khai báo trước, nên schema vẫn là version 2. Điều này không giải quyết migration từ các phiên bản cũ hơn nếu thiết bị đang có database cũ.

### 8.2. LandPlotViewModel.kt

Mở [LandPlotViewModel.kt](G:/Documents/2026/my-project/Mobile/app/src/main/java/com/example/agriculture_management/feature/landplot/LandPlotViewModel.kt:13).

| Trước | Sau |
|---|---|
| Chỉ nhận LandPlotRepository | Nhận thêm PlantingRepository |
| UiState chỉ có plots/loading/error | Có thêm crops, plantings và saving |
| Chỉ quan sát danh sách thửa đất | `combine()` ba luồng thửa đất, cây và vụ |
| Các hàm lưu chưa có bộ xử lý chung | Hàm `save()` gom xử lý trạng thái, lỗi và hủy |
| Chưa tạo vụ | Có `createPlanting()` |

`load()` khởi tạo nông trại, cây mặc định rồi collect Flow. Khi có dữ liệu mới, nó dùng `uiState.copy(...)` để cập nhật các danh sách và tắt loading.

`save()` bỏ qua yêu cầu mới nếu đang tải hoặc đang lưu, đặt `saving = true`, thực hiện công việc rồi trả `saving = false` trong `finally`. Lỗi được đưa vào `uiState.error`. `createPlanting()` gọi Repository, dùng `getOrThrow()` và chỉ gọi callback thành công nếu không có lỗi.

Giới hạn: nếu luồng tải gặp lỗi, hiện chỉ hiện lỗi và kết thúc công việc tải; chưa có cơ chế retry riêng. Cờ saving ngăn thao tác trùng trong cùng ViewModel, không phải cơ chế idempotency giữa nhiều thiết bị.

### 8.3. GreenFarmApp.kt: điểm nối dữ liệu với màn hình

Mở [GreenFarmApp.kt](G:/Documents/2026/my-project/Mobile/app/src/main/java/com/example/agriculture_management/ui/GreenFarmApp.kt:33).

**Khởi tạo ViewModel:** thay `remember { LandPlotViewModel(...) }` bằng `viewModel(factory = ...)`. Android quản lý ViewModel theo ViewModelStoreOwner, giúp giữ instance qua việc tái tạo phù hợp và clear khi owner kết thúc. Factory truyền hai Repository vào constructor.

**Ghép dữ liệu:** mỗi thửa đất lấy các `PlantingSummary` có cùng `plotId`. Hàm `toLand()` tạo danh sách tên cây duy nhất và nhóm diện tích theo tên cây. Nhờ đó danh sách thửa đất và số loại cây nhận được dữ liệu từ Room.

**Khi lưu vụ:** nhánh `Screen.AddPlanting`, tại dòng 115, truyền `roomLands`, `plotState.crops`, `plotState.plantings`, loading và saving cho form. Callback gọi `plotViewModel.createPlanting(input)`. Sau thành công, cập nhật ID thửa được chọn, quay lại và báo đã lưu.

Đã bỏ thao tác sửa `state.lands` và `state.save()` khỏi nhánh tạo vụ. Như vậy, vụ mới được lưu vào Room thay vì ghi vào JSON.

`toLand()` vẫn phục vụ model giao diện cũ có diện tích nguyên. Diện tích nhóm cây ở model này được làm tròn lên theo m²; chi tiết vụ và kiểm tra diện tích ở Repository dùng số 1/100 m² gốc. Không dùng số đã làm tròn để kiểm tra nghiệp vụ.

### 8.4. AddPlantingScreen.kt: form mới

Mở [AddPlantingScreen.kt](G:/Documents/2026/my-project/Mobile/app/src/main/java/com/example/agriculture_management/ui/AddPlantingScreen.kt:21).

Form được tách thành file riêng và không nhận `FarmState`. Nó nhận danh sách đất, cây, vụ cùng callback `save(CreatePlanting)`.

Các thay đổi xử lý chính:

- Chọn theo `landId` và `cropId` thực tế từ Room.
- `LaunchedEffect` chọn giá trị mặc định khi dữ liệu đã tải và giá trị hiện tại không còn hợp lệ.
- Hiện trạng thái đang tải, trường hợp chưa có đất và nút thêm đất.
- Tính phần còn lại để báo sớm cho người dùng; Repository vẫn kiểm tra lại trước khi ghi.
- Nhận số thập phân bằng `BigDecimal`, chấp nhận dấu phẩy hoặc dấu chấm, đổi sang Long 1/100 m² bằng `longValueExact()`.
- Ngày nhập được đọc bằng định dạng strict `dd/MM/uuuu`, sau đó đổi thành ISO trước khi gửi Repository.
- Tắt nút Lưu khi đang lưu hoặc thiếu cây/đất hợp lệ.

Ví dụ `20,50` được lưu là `2050`. Giá trị không biểu diễn chính xác ở độ phân giải 0,01 m² sẽ bị từ chối. Chuỗi có thêm số 0 cuối như `20,500` vẫn có thể hợp lệ vì giá trị vẫn chính xác là 20,50 m².

### 8.5. FarmScreens.kt

Mở [FarmScreens.kt](G:/Documents/2026/my-project/Mobile/app/src/main/java/com/example/agriculture_management/ui/FarmScreens.kt:176).

- Loại form `AddPlantingScreen` cũ khỏi file.
- `LandDetailScreen` nhận thêm danh sách vụ. Với dữ liệu Room, mỗi vụ hiển thị riêng cây, diện tích và hai ngày.
- Ghi chú và vị trí vẫn có thể hiển thị khi thửa đất chưa có vụ.
- Giữ nhánh hiển thị cũ khi không truyền danh sách vụ để phục vụ preview mẫu.
- Bỏ kiểm tra diện tích theo `Land.cropAreas` ở biểu mẫu sửa đất. Kiểm tra chính được chuyển về Repository và dựa vào dữ liệu Room, tránh dùng tổng diện tích mẫu hoặc số đã làm tròn.

Tab nhật ký và chi phí vẫn đọc `FarmState`; đợt này chưa chuyển các tab đó sang bảng hoạt động/chi phí của Room.

### 8.6. ScreenGallery.kt

Mở [ScreenGallery.kt](G:/Documents/2026/my-project/Mobile/app/src/main/java/com/example/agriculture_management/ui/ScreenGallery.kt).

Cập nhật lời gọi preview theo chữ ký mới của `AddPlantingScreen`: truyền danh sách đất mẫu, danh sách `CropOption` mẫu và danh sách vụ rỗng. Dữ liệu preview không phải dữ liệu runtime và không được ghi vào Room.

### 8.7. Cấu hình Gradle

- [libs.versions.toml](G:/Documents/2026/my-project/Mobile/gradle/libs.versions.toml): thêm alias `androidx-lifecycle-viewmodel-compose`, dùng phiên bản lifecycle đã có là 2.9.4.
- [app/build.gradle.kts](G:/Documents/2026/my-project/Mobile/app/build.gradle.kts): thêm `implementation(libs.androidx.lifecycle.viewmodel.compose)` để dùng `viewModel()` trong Compose.

### 8.8. Tài liệu kỹ thuật

- [PROJECT-STRUCTURE.md](G:/Documents/2026/my-project/Mobile/docs/PROJECT-STRUCTURE.md): bổ sung file mới, luồng lưu vụ và giới hạn hiện tại.
- [SQLITE-ROOM-SCHEMA.md](G:/Documents/2026/my-project/Mobile/docs/design/SQLITE-ROOM-SCHEMA.md): cập nhật trạng thái cây/vụ đã nối Room; nhật ký, kho và backup vẫn là phần riêng.

## 9. LandPlotRepository: bảo vệ diện tích và lỗi lặp code

Mở [LandPlotRepository.kt](G:/Documents/2026/my-project/Mobile/app/src/main/java/com/example/agriculture_management/data/repository/LandPlotRepository.kt:82).

### Ý định của thay đổi

Khi sửa diện tích đất, Repository cần kiểm tra:

1. Thửa đất còn tồn tại và chưa ẩn.
2. Không đổi farmId của thửa đất.
3. Diện tích mới không nhỏ hơn tổng diện tích các vụ đang giữ.
4. Cập nhật bản ghi trong cùng transaction với kiểm tra.

Ví dụ đất có 60,50 m² đã dành cho vụ: sửa tổng diện tích xuống 60 m² phải bị từ chối; sửa thành 61 m² có thể hợp lệ.

### Phát hiện khi kiểm tra mã hiện tại

Code đang có hai `database.withTransaction` lồng nhau, bắt đầu khoảng dòng 85 và 91. Hai khối lặp lại cùng việc đọc thửa đất, kiểm tra farmId và tính diện tích. Chỉ có **một** lời gọi `dao.update()`.

Đây là đoạn lặp ngoài ý muốn trong lần sửa trước. Nó gây kiểm tra/truy vấn thừa và làm code khó đọc; không phải bằng chứng rằng ứng dụng cập nhật hai bản ghi hoặc ghi hai lần. Việc build trước đó thành công cũng không bảo đảm đoạn lặp này là thiết kế hợp lý.

Hướng sửa đề xuất: giữ một transaction, một lần kiểm tra và một lần update. **Chưa sửa trong lượt lập báo cáo này.**

Ngoài ra, hàm có kiểu trả về `Result<Unit>`, nhưng một số lỗi trong transaction vẫn có thể ném exception trực tiếp. ViewModel hiện đã bắt exception; tuy nhiên nên thống nhất cách trả lỗi ở Repository để dễ dùng và kiểm thử hơn.

## 10. Kiểm thử đã bổ sung và kết quả thực tế

### 10.1. PlantingRepositoryTest: 7 bài

Mở [PlantingRepositoryTest.kt](G:/Documents/2026/my-project/Mobile/app/src/androidTest/java/com/example/agriculture_management/PlantingRepositoryTest.kt:17).

Mỗi bài dùng database test có tên riêng và dọn database test khi kết thúc.

| Bài | Điều muốn xác minh |
|---|---|
| Lưu và mở lại database | Vụ gắn đúng thửa đất mới, đúng cây, đúng diện tích; đóng/mở kết nối vẫn đọc lại được |
| Ngày sai và diện tích không dương | Từ chối dữ liệu sai và không ghi vụ |
| Nhiều vụ cùng cây | Tạo các dòng độc lập; cộng đủ diện tích mọi vụ, không ghi đè vụ cũ |
| Hai yêu cầu đồng thời | Với đất 100 m² và hai yêu cầu 60 m², chỉ một yêu cầu được chấp nhận |
| ID thiếu, đất ẩn, khác nông trại | Từ chối tham chiếu không hợp lệ |
| Thu nhỏ diện tích đất | Không cho giảm xuống dưới diện tích đã dành cho vụ |
| Khởi tạo cây nhiều lần | Không nhân đôi danh mục mặc định |

### 10.2. PlantingFlowTest: 1 bài

Mở [PlantingFlowTest.kt](G:/Documents/2026/my-project/Mobile/app/src/androidTest/java/com/example/agriculture_management/PlantingFlowTest.kt:41).

Bài test tạo database trong RAM, tạo thửa đất qua Repository, mở form với ViewModel thật, nhập diện tích 20,50 m², chọn ngày, lưu rồi kiểm tra chi tiết nhận diện tích mới qua Flow.

Phạm vi test là form, ViewModel, Repository, DAO và phần chi tiết trong một bố cục test. Nó không kiểm tra toàn bộ điều hướng từ Splash của app hoặc khả năng khởi động lại cả tiến trình.

### 10.3. Kết quả đã có

| Kiểm tra | Trạng thái |
|---|---|
| Compile mã ứng dụng | Thành công trong lượt triển khai trước |
| Compile mã Android test | Thành công, gồm hai lớp test mới |
| Build APK debug và APK test | Thành công |
| Unit test hiện có | 3 bài qua: 2 bài FarmRepositoryTemplateTest và 1 ExampleUnitTest |
| 8 bài Android mới | Chưa có kết quả chạy thành công; máy ảo không ổn định, lần gọi connected test báo không có thiết bị kết nối |
| git diff --check | Không ghi nhận lỗi whitespace trong lần kiểm tra trước; có cảnh báo chuyển LF/CRLF |

Các unit test đang qua thuộc bộ test cũ, không thay thế các bài test Room mới. Không được diễn giải “unit test qua” thành “vụ trồng đã được kiểm chứng đầy đủ”. Lượt lập báo cáo không chạy lại build hoặc test.

## 11. Những giới hạn còn lại

1. Vụ mới luôn được gán `ACTIVE`, kể cả khi nhập ngày trồng ở tương lai. Chưa có quy trình chuyển trạng thái theo lịch.
2. Chưa có nút kết thúc vụ, nên các vụ chưa kết thúc tiếp tục giữ diện tích. Chưa có màn hình lịch sử các vụ đã kết thúc.
3. Vụ mẫu cũ trong SharedPreferences chưa được chuyển sang Room. Không tự ghép theo tên vì có thể gắn sai thửa đất.
4. Tạo vụ mới không còn thêm một dòng nhật ký mẫu. Bảng activities và luồng nhật ký Room chưa được nối trong đợt này.
5. Kho, thu hoạch, bán hàng, báo cáo và sao lưu JSON chưa được nối xuyên suốt với các vụ Room. Backup JSON chưa bao phủ dữ liệu Room.
6. Chưa bổ sung backend server, API, tài khoản hoặc đồng bộ nhiều thiết bị. farmId hiện vẫn là nông trại mặc định.
7. Schema vẫn ở version 2; các thiếu sót migration cũ và cấu hình xuất schema chưa được xử lý trong đợt này.
8. Cần sửa đoạn transaction lặp và chạy lại bộ test Android trước khi kết luận luồng mới ổn định trên thiết bị.

## 12. Thứ tự đọc code để hiểu thay đổi

Nên đọc theo thứ tự sau:

1. **Planting.kt:** hiểu yêu cầu cần những dữ liệu nào.
2. **CropDao.kt và PlantingDao.kt:** hiểu dữ liệu được đọc/ghi ra sao.
3. **PlantingRepository.create():** hiểu quy tắc nào quyết định có được lưu không.
4. **LandPlotViewModel:** hiểu nơi nhận yêu cầu, quản lý saving/error và collect dữ liệu.
5. **GreenFarmApp:** tìm chỗ nối ViewModel vào form và chi tiết.
6. **AddPlantingScreen:** hiểu chuyển ngày/diện tích và tạo CreatePlanting.
7. **Hai file test:** hiểu từng hành vi cần được chứng minh.

**Tóm tắt:** trước đây vụ trồng sửa dữ liệu mẫu trong FarmState. Hiện tại code đã tạo đường lưu vào bảng plantings, lấy cây từ crops, liên kết thửa đất bằng plotId và cập nhật dữ liệu qua Flow. Phần cần hoàn tất kiểm chứng là các test Android; phần lỗi đã thấy rõ trong mã là transaction bị lặp ở updatePlot().
