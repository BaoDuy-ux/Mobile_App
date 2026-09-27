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
    demo/
      DemoFarmDao.kt            # Interface mẫu và InMemoryFarmDao lưu trong RAM
      DemoFarmRecord.kt         # Bản ghi dùng cho mã mẫu
  repository/
    LandPlotRepository.kt       # Kiểm tra và lưu/đọc thửa đất
    FarmRepository.kt           # Repository mẫu dùng DemoFarmDao
domain/
  model/
    LandPlot.kt                 # Đối tượng thửa đất dùng trong nghiệp vụ
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
