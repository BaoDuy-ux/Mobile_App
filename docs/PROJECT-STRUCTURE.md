# Cấu trúc mã nguồn

Các đường dẫn dưới đây tính từ `app/src/main/java/com/example/agriculture_management/`.

## Phần dữ liệu trên điện thoại

```text
data/
  local/
    database/
      FarmDatabase.kt           # Khai báo database Room và danh sách bảng
      FarmDatabaseProvider.kt   # Tạo và dùng chung database SQLite
    entity/
      LandPlotEntity.kt         # Bảng thửa đất: plots
      FarmEntity.kt             # Bảng nông trại: farms
      UserEntity.kt             # Bảng người dùng: app_users
      CropEntity.kt             # Bảng cây trồng: crops
      PlantingEntity.kt         # Bảng vụ trồng: plantings
      ActivityEntity.kt         # Bảng hoạt động: activities
      HarvestEntity.kt          # Bảng thu hoạch: harvests
      StockLotEntity.kt         # Bảng lô hàng: stock_lots
      StockMovementEntity.kt    # Bảng biến động kho: stock_movements
      SaleEntity.kt             # Bảng bán hàng: sales
      SaleItemEntity.kt         # Bảng chi tiết bán hàng: sale_items
      ExpenseEntity.kt          # Bảng chi phí: expenses
      ReminderEntity.kt         # Bảng nhắc việc: reminders
    dao/
      LandPlotDao.kt            # Các truy vấn thửa đất
      FarmDao.kt                # Các truy vấn nông trại bằng Room
      UserDao.kt                # Các truy vấn người dùng
      CropDao.kt                # Đọc danh mục cây theo nông trại
      PlantingDao.kt            # Lưu vụ, quan sát vụ và tính diện tích đã dùng
    demo/
      DemoFarmDao.kt            # Interface mẫu và InMemoryFarmDao lưu trong RAM
      DemoFarmRecord.kt         # Bản ghi dùng cho mã mẫu
  repository/
    LandPlotRepository.kt       # Kiểm tra và lưu/đọc thửa đất
    PlantingRepository.kt       # Kiểm tra ngày/diện tích/quan hệ và lưu vụ trong transaction
    FarmRepository.kt           # Repository mẫu dùng DemoFarmDao
domain/
  model/
    LandPlot.kt                 # Đối tượng thửa đất dùng trong nghiệp vụ
    Planting.kt                 # CreatePlanting, CropOption và PlantingSummary
feature/
  landplot/
    LandPlotViewModel.kt        # Nhận yêu cầu, gọi repository, cập nhật trạng thái
```

`local` là dữ liệu trên thiết bị. `entity` mô tả bảng. `dao` chứa thao tác với bảng. `repository` xử lý dữ liệu và kiểm tra đầu vào. `demo` chứa mã mẫu lưu tạm.

## Điểm nối với giao diện

`ui/GreenFarmApp.kt` tạo `LandPlotViewModel` với `LandPlotRepository` và `FarmDatabaseProvider`. Khi lưu thửa đất:

```text
GreenFarmApp → LandPlotViewModel → LandPlotRepository → LandPlotDao → SQLite
```

Khi đọc, DAO trả `Flow`; ViewModel cập nhật `LandPlotUiState` để Compose nhận dữ liệu mới.

## Vụ trồng đã nối Room

`ui/AddPlantingScreen.kt` nhận thửa đất, danh mục cây và vụ trồng từ Room. Khi lưu:

```text
AddPlantingScreen → LandPlotViewModel → PlantingRepository → PlantingDao → SQLite
```

- Tạo danh mục 5 cây mặc định theo nông trại, không tạo trùng khi mở lại app.
- Chọn đúng ID thửa đất mới; không còn sửa `FarmState.lands` khi tạo vụ trồng.
- Diện tích nhập hỗ trợ 2 số thập phân, lưu bằng 1/100 m². Ngày lưu ISO `yyyy-MM-dd`.
- Repository kiểm tra cây/thửa đất tồn tại và cùng nông trại; ngày thu hoạch không trước ngày trồng; tổng diện tích chưa kết thúc không vượt diện tích đất.
- Vụ mới có trạng thái ACTIVE. Các vụ ACTIVE/PLANNED chưa kết thúc đều giữ diện tích; chưa có thao tác kết thúc vụ trong giao diện.
- Kiểm tra diện tích và insert chạy cùng transaction; sửa diện tích đất cũng kiểm tra phần đã dành cho vụ trồng.
- Flow cập nhật danh sách và chi tiết thửa đất; chi tiết hiển thị riêng ngày và diện tích của từng vụ.
- ViewModel được lấy bằng `viewModel()` và Factory, có chặn lưu lặp, trạng thái đang lưu và bắt lỗi.

Schema vẫn ở version 2 vì các bảng đã tồn tại; lần này chỉ bổ sung DAO và luồng xử lý. Không xóa dữ liệu cũ. Chưa tự chuyển vụ mẫu từ SharedPreferences sang Room vì ID và mô hình dữ liệu khác nhau. Nhật ký, kho, báo cáo và sao lưu JSON vẫn là các phần riêng; tạo vụ không còn thêm nhật ký mẫu để tránh ghi hai nguồn dữ liệu khác nhau.

Kiểm thử mới: `app/src/androidTest/.../PlantingRepositoryTest.kt` dùng database test riêng để kiểm tra lưu/mở lại, ngày sai, diện tích không hợp lệ, cùng cây nhiều vụ, lưu đồng thời, tham chiếu khác nông trại, thu nhỏ đất và tạo danh mục lặp.

## Tên cũ và tên mới

| Tên cũ | Tên mới |
|---|---|
| Plot.kt | LandPlot.kt |
| PlotEntity.kt | LandPlotEntity.kt |
| PlotDao.kt | LandPlotDao.kt |
| PlotRepository.kt | LandPlotRepository.kt |
| PlotViewModel.kt | LandPlotViewModel.kt |
| AppDatabase.kt | FarmDatabase.kt |
| DatabaseProvider.kt | FarmDatabaseProvider.kt |
| FarmEntityDao.kt | FarmDao.kt trong local/dao |
| FarmDao.kt (mã mẫu) | DemoFarmDao.kt trong local/demo |
| FarmRecord.kt | DemoFarmRecord.kt |
| AgricultureEntities.kt | Tách thành từng file Entity |
| data/room | data/local/database và data/local/entity |
| data/dao (các DAO Room) | data/local/dao |
| feature/farm/PlotViewModel.kt | feature/landplot/LandPlotViewModel.kt |

Tên bảng, cột, phiên bản schema và tên file SQLite `nong_san_xanh.db` được giữ nguyên. Việc đổi tên này không chuyển các module còn dùng dữ liệu mẫu sang Room. Tài liệu thiết kế cũ có thể còn dùng thuật ngữ `Plot`; thuật ngữ đó tương ứng với `LandPlot` trong mã mới.
