# SQLite/Room schema

Schema này ánh xạ từ `erd.mmd` vào SQLite trên Android. Room tạo các bảng từ từng file `@Entity` trong `data/local/entity/`. Xem [cấu trúc mã nguồn](../PROJECT-STRUCTURE.md) để đối chiếu tên cũ và tên mới.

## Bảng

| Room entity | SQLite table | Khóa chính | Quan hệ chính |
|---|---|---|---|
| `UserEntity` | `app_users` | `id` | Người dùng sở hữu Farm |
| `FarmEntity` | `farms` | `id` | `ownerId → app_users.id` |
| `LandPlotEntity` | `plots` | `id` | `farmId → farms.id` |
| `CropEntity` | `crops` | `id` | `farmId → farms.id` |
| `PlantingEntity` | `plantings` | `id` | `plotId`, `cropId` |
| `ActivityEntity` | `activities` | `id` | `plantingId` |
| `HarvestEntity` | `harvests` | `id` | `plantingId` |
| `StockLotEntity` | `stock_lots` | `id` | `farmId`, `cropId`, `harvestId` |
| `StockMovementEntity` | `stock_movements` | `id` | `lotId`, tùy chọn `saleItemId` |
| `SaleEntity` | `sales` | `id` | `farmId` |
| `SaleItemEntity` | `sale_items` | `id` | `saleId`, `lotId` |
| `ExpenseEntity` | `expenses` | `id` | `farmId`, tùy chọn `plantingId` |
| `ReminderEntity` | `reminders` | `id` | `farmId`, tùy chọn `plantingId` |

## Quy ước lưu số

- Diện tích trong `PlantingEntity.areaM2x100`: số nguyên theo 1/100 m².
- Khối lượng trong `HarvestEntity`, `SaleItemEntity`, `StockMovementEntity`: gram.
- Tiền trong `SaleEntity`, `SaleItemEntity`, `ExpenseEntity`: đồng VND nguyên.
- Ngày lịch: chuỗi ISO `yyyy-MM-dd`.
- Thời điểm: epoch milliseconds UTC.
- Trạng thái và loại hoạt động: chuỗi để dễ đọc và tương thích API.

## Quan hệ nghiệp vụ cần kiểm tra trong Repository/UseCase

SQLite/Room giữ khóa ngoại, nhưng không tự biết mọi quy tắc nghiệp vụ:

1. Tổng diện tích Planting đang ACTIVE không vượt diện tích Plot.
2. Plot và Crop trong Planting phải thuộc cùng Farm.
3. Harvest chỉ được ghi cho Planting hợp lệ.
4. Mỗi Harvest tạo một StockLot.
5. Tổng StockMovement của một lot không được âm.
6. SaleItem chỉ dùng lot thuộc cùng Farm với Sale.
7. `lineTotalVnd` và `totalVnd` phải được tính lại trong transaction.

Database được khai báo trong `data/local/database/FarmDatabase.kt`; tạo singleton bằng `FarmDatabaseProvider.get(context)`. Phần thửa đất kết nối Room qua `LandPlotRepository` và `LandPlotViewModel`, được lắp ghép trong `ui/GreenFarmApp.kt`. Các phần cây trồng, nhật ký và kho vẫn dùng `FarmState`.
