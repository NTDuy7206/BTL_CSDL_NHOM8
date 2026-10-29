# HomeBase — Bài tập lớn Cơ sở dữ liệu, Nhóm 8

Hệ thống quản lý tòa nhà và phòng cho thuê, xây dựng bằng **MySQL + Spring Boot + React/Vite**. Trọng tâm môn học là mô hình dữ liệu quan hệ, khóa/ràng buộc, truy vấn SQL và giao dịch; giao diện hỗ trợ thao tác trực tiếp với dữ liệu thay cho việc phải dùng Postman.

[Mã nguồn GitHub](https://github.com/NTDuy7206/BTL_CSDL_NHOM8) · [Địa chỉ web đã cấu hình](https://btlcsdlnhom8-production.up.railway.app)

Trang công khai có thể đang chạy commit khác với mã trên máy. Muốn dùng tính năng mới, cần triển khai đúng nhánh/commit trên Railway. README này mô tả mã nguồn hiện tại, không xác nhận trạng thái triển khai của server.

## Mục lục

- [Chức năng và vai trò](#chức-năng-và-vai-trò)
- [Chạy dự án trên máy](#chạy-dự-án-trên-máy)
- [Chạy bằng IntelliJ IDEA](#chạy-bằng-intellij-idea)
- [Phát triển frontend và đóng gói](#phát-triển-frontend-và-đóng-gói)
- [Tạo tài khoản và dùng thử](#tạo-tài-khoản-và-dùng-thử)
- [Quy trình thuê phòng và hóa đơn](#quy-trình-thuê-phòng-và-hóa-đơn)
- [Cơ sở dữ liệu và dữ liệu mẫu](#cơ-sở-dữ-liệu-và-dữ-liệu-mẫu)
- [API chính](#api-chính)
- [Cấu hình môi trường](#cấu-hình-môi-trường)
- [Kiểm tra và triển khai](#kiểm-tra-và-triển-khai)
- [Khắc phục lỗi](#khắc-phục-lỗi)
- [Cấu trúc và tài liệu](#cấu-trúc-và-tài-liệu)

## Chức năng và vai trò

| Chức năng | USER — Khách thuê | ADMIN — Chủ nhà |
|---|---|---|
| Tài khoản | Đăng ký, đăng nhập JWT, cập nhật hồ sơ/ảnh đại diện | Đăng ký với số phòng cố định, đăng nhập, cập nhật hồ sơ |
| Phòng | Xem danh sách, chọn chủ nhà rồi chọn phòng trống | Khai báo số phòng một lần, sửa thông tin phòng thuộc mình |
| Đăng ký thuê | Tự nhập hồ sơ và gửi yêu cầu bằng tài khoản đang đăng nhập | Xem yêu cầu thuộc phòng của mình, duyệt hoặc từ chối |
| Hợp đồng | Chỉ xem hợp đồng gắn với tài khoản của mình | Duyệt yêu cầu để tạo hợp đồng; không tạo hợp đồng thay khách |
| Ngày thuê thực tế | Xem ngày nhận/trả thực tế trên hợp đồng | Xác nhận Check-in/Check-out, công tơ và giá thuê thỏa thuận |
| Hóa đơn | Chỉ xem/thanh toán hóa đơn của mình | Quản lý hóa đơn theo phòng thuộc mình, đối soát thanh toán |
| Thanh toán | Chọn chuyển khoản hoặc tiền mặt, gửi thông tin đã thanh toán | Xác nhận đã thu hoặc yêu cầu kiểm tra lại |
| Sự cố | Báo sự cố tại phòng đang thuê, xem tình trạng | Xử lý, bấm Đã xử lý xong, xem lịch sử hoàn thành |
| Tra cứu | Dùng những nút được phân quyền | Tra cứu nhanh theo mã, tên, phòng, trạng thái, giá… |

Các bảng hiển thị trong **khung cuộn**, không chia trang; có đơn vị **m², kWh, m³ (khối nước), ₫**. Tổng quan của khách lấy **Phòng tôi đang thuê** từ hợp đồng của chính tài khoản đó. Danh sách phòng để tham khảo có thể bao gồm phòng của các chủ nhà khác.

Một số phân hệ như nhân viên, nội quy và các chức năng quản lý dùng chung vẫn theo phân quyền của bài tập lớn; dự án chưa tách toàn bộ dữ liệu thành các tòa nhà độc lập. Quyền ở API là căn cứ thực thi, không chỉ dựa vào việc ẩn nút trên giao diện.

## Chạy dự án trên máy

### 1. Chuẩn bị

| Thành phần | Yêu cầu / cách dùng |
|---|---|
| JDK | **26**, khớp java.version trong pom.xml |
| Maven | Dùng Maven Wrapper có sẵn; không bắt buộc cài Maven riêng |
| MySQL | Đang chạy, ví dụ localhost:3306; cấu hình Docker server dùng MySQL 8.4 |
| Node.js / npm | Dùng khi sửa/build React; Node 24 đã được dùng để build dự án |
| Git | Clone/cập nhật mã nguồn |
| Docker Desktop | Dùng nếu chạy Redis/MinIO hoặc các dịch vụ bằng container |
| MinIO | Cần cho chức năng tải ảnh đại diện; không bắt buộc cho hồ sơ văn bản |
| Redis | Có cấu hình Redis/cache; cần dịch vụ Redis nếu bật/sử dụng cache. Không phải nơi lưu dữ liệu nghiệp vụ chính |

Mở **PowerShell** trong Terminal của IntelliJ hoặc Windows Terminal. Các lệnh có ký hiệu $env bên dưới là PowerShell, không dán vào CMD.

### 2. Clone và tạo database

```powershell
git clone https://github.com/NTDuy7206/BTL_CSDL_NHOM8.git
cd BTL_CSDL_NHOM8
java -version
```

Trong MySQL/DBeaver, tạo database nếu chưa có:

```sql
CREATE DATABASE IF NOT EXISTS btl_csdl
  CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

### 3. Cấu hình trong PowerShell

Thay thông tin MySQL bằng cấu hình trên máy bạn. Các biến có hiệu lực trong cửa sổ đang mở.

```powershell
$env:DB_URL='jdbc:mysql://localhost:3306/btl_csdl?characterEncoding=UTF-8&serverTimezone=UTC'
$env:DB_USERNAME='root'
$env:DB_PASSWORD='<mat-khau-MySQL-cua-ban>'
$env:PORT='8080'
$env:REDIS_HOST='localhost'
$env:REDIS_PORT='6379'
$env:SPRING_SQL_INIT_SCHEMA_LOCATIONS='classpath:server-schema.sql,classpath:schema.sql'
```

Nếu tài khoản MySQL thực sự dùng mật khẩu rỗng, đặt DB_PASSWORD thành chuỗi rỗng. Không dùng mật khẩu ví dụ thay cho mật khẩu thật.

Sinh khóa JWT Base64 riêng cho môi trường chạy:

```powershell
$jwtBytes = New-Object byte[] 32
$jwtRng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$jwtRng.GetBytes($jwtBytes)
$env:JWT_SECRET = [Convert]::ToBase64String($jwtBytes)
$jwtRng.Dispose()
```

Giữ cùng JWT_SECRET giữa các lần chạy nếu muốn token cũ còn hợp lệ đến hạn. Đổi khóa sẽ làm token cũ không dùng được; đăng nhập lại để lấy token mới. Với IntelliJ Run Configuration hoặc server, lưu khóa trong biến môi trường của nơi chạy, không ghi khóa thật vào Git.

### 4. Chạy backend và mở web

```powershell
.\mvnw.cmd spring-boot:run
```

Mở **[http://localhost:8080/](http://localhost:8080/)**. Thư mục static đã có bản build React nên không bắt buộc chạy Node chỉ để xem giao diện. Nhấn Ctrl+C để dừng backend.

Override SPRING_SQL_INIT_SCHEMA_LOCATIONS ở bước trên cho ứng dụng chạy lần lượt **server-schema.sql → schema.sql**, sau đó Hibernate validate cấu trúc. Nếu không đặt override khi chạy profile local, cần tự nạp server-schema.sql trước; cấu hình local mặc định chỉ tìm schema.sql. Profile prod đã khai báo cả hai file.

Các script dùng CREATE TABLE IF NOT EXISTS; không tự chuyển đổi toàn bộ cấu trúc bảng cũ đã khác schema. Lỗi thiếu cột/ràng buộc cần được kiểm tra theo cấu trúc DB thực tế.

### 5. MinIO và Redis nếu cần

Chạy MinIO cục bộ:

```powershell
docker compose -f compose.minio.yml up -d --build
```

| Dịch vụ | Địa chỉ | Tài khoản |
|---|---|---|
| MinIO API | http://localhost:9000 | Ứng dụng dùng MINIO_ACCESS_KEY / MINIO_SECRET_KEY |
| MinIO Console | http://localhost:9001 | Mặc định cấu hình phát triển: minioadmin / minioadmin |

Thông tin MinIO là tài khoản lưu ảnh, **không phải tài khoản đăng nhập HomeBase**. Nếu đổi MINIO_ROOT_USER / MINIO_ROOT_PASSWORD cho container thì đặt MINIO_ACCESS_KEY / MINIO_SECRET_KEY tương ứng cho ứng dụng. Lần build MinIO đầu có thể mất thời gian vì Docker dựng từ mã nguồn. Ảnh đại diện hỗ trợ JPG/PNG/WEBP, tối đa 5 MB.

Nếu cần tạo Redis thử riêng và cổng 6379 chưa có dịch vụ:

```powershell
docker run -d --name homebase-redis -p 127.0.0.1:6379:6379 redis:7-alpine
```

Nếu đã có container/dịch vụ Redis thì dùng dịch vụ hiện có. Các annotation cache/cấu hình Redis trong mã không có nghĩa toàn bộ API đều đang cache; phòng và hóa đơn hiện đọc mới để phản ánh ngày thuê/tính tiền tự động.

## Chạy bằng IntelliJ IDEA

1. Open thư mục dự án chứa pom.xml, tải Maven project.
2. Chọn Project SDK và Maven Runner JRE là JDK 26.
3. Mở Run → Edit Configurations → cấu hình chạy CsdlApplication.
4. Đặt Environment variables tương ứng DB_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET, PORT và SPRING_SQL_INIT_SCHEMA_LOCATIONS như trên.
5. Bật MySQL và dịch vụ bạn cần; chạy com.ntd.csdl.CsdlApplication rồi mở localhost:8080.

Biến đã đặt ở một terminal PowerShell không tự trở thành biến của nút Run trong IntelliJ. Cần đặt riêng trong Run Configuration nếu chạy bằng nút Run.

## Phát triển frontend và đóng gói

Backend chạy ở cổng 8080. Mở terminal thứ hai:

```powershell
cd frontend
npm ci
npm run dev
```

Mở **[http://localhost:5173/](http://localhost:5173/)**; nếu cổng đang dùng, xem địa chỉ Vite in ra. Vite chuyển tiếp /api sang localhost:8080. Nếu đổi cổng backend, chỉnh cấu hình proxy trong frontend/vite.config.js.

Sau khi sửa React, build giao diện trước rồi đóng gói Java:

```powershell
cd frontend
npm ci
npm run build
cd ..
.\mvnw.cmd -DskipTests package
java -jar target/CSDL-0.0.1-SNAPSHOT.jar
```

Chạy các lệnh từ thư mục gốc dự án; không cd frontend thêm lần nữa nếu terminal đã ở đó. Bản build Vite ghi vào src/main/resources/static. Dockerfile hiện build Java và dùng static đã có, nên cần commit cả tài nguyên static sau khi build React.

## Tạo tài khoản và dùng thử

Ứng dụng **không tạo sẵn username/password dùng chung** khi nạp dữ liệu mẫu. Đăng ký tài khoản của bạn trên giao diện. Không thể suy ra mật khẩu gốc từ hash BCrypt trong bảng user.

Ví dụ để tự đăng ký qua API; đây là tài khoản ví dụ, không khẳng định đã tồn tại:

```json
{
  "username": "chu_nha_demo",
  "password": "TuDatMatKhauCuaBan!",
  "role": "ADMIN",
  "roomCount": 10
}
```

Khách thuê dùng role USER và không cần roomCount. Đăng ký chủ nhà cần số phòng từ **1 đến 200**. Username trùng bị từ chối.

| Bước | Chủ nhà | Khách thuê |
|---|---|---|
| Thiết lập | Khai báo số phòng; với tài khoản cũ, vào Phòng / mặt bằng và liên kết các phòng cũ thực sự thuộc mình | Đăng ký USER và đăng nhập |
| Mở cho thuê | Sửa diện tích, loại phòng, giá và chuyển trạng thái AVAILABLE | Vào Đăng ký thuê, chọn chủ nhà và phòng |
| Hợp đồng | Duyệt yêu cầu do đúng khách gửi | Nhập hồ sơ, thời hạn và gửi yêu cầu |
| Nhận/trả phòng | Xác nhận ngày thực tế và công tơ | Xem Hợp đồng của tôi |
| Hóa đơn | Nhập chỉ số cuối tháng; xem cảnh báo, đối soát thanh toán | Xem Hóa đơn của tôi, chọn phương thức thanh toán |

Phòng mới ban đầu UNCONFIGURED. Số phòng cố định sau thiết lập; tạo hồ sơ/yêu cầu/hợp đồng không sinh thêm phòng. API thêm/xóa phòng trực tiếp đã bị chặn. Không tự gán phòng cũ cho một chủ nhà chỉ dựa trên tên.

## Quy trình thuê phòng và hóa đơn

**Khách gửi yêu cầu → Chủ nhà duyệt → Tạo hợp đồng → Check-in → Chỉ số cuối tháng → Hóa đơn → Khách báo thanh toán → Chủ nhà xác nhận.**

| Quy tắc | Hành vi hiện tại |
|---|---|
| Tạo hợp đồng | POST trực tiếp /api/contracts và /api/rentals bị từ chối. Chủ nhà chỉ duyệt yêu cầu khách tự gửi |
| Sở hữu dữ liệu | Hợp đồng/hóa đơn của khách dựa trên liên kết tài khoản, không tìm theo họ tên giống nhau |
| Nhận/trả sớm | Lưu ngày thực tế riêng; không sửa mất ngày trong hợp đồng. Không cho chồng lấn người thuê khác |
| Tính tiền thuê | Giá đã chốt × ngày ở thực tế trong tháng ÷ số ngày của tháng; ngày trả phòng không tính tiền thuê |
| Điện/nước | Chênh lệch công tơ × đơn giá. Đổi khách giữa tháng cần chỉ số nhận/trả của từng người |
| Cuối tháng | Chốt từ 23:55 ngày cuối tháng, giờ Việt Nam; tác vụ kiểm tra mỗi phút khi ứng dụng đang chạy |
| Thiếu dữ liệu | Chưa tự lập hóa đơn; hiện cảnh báo cho chủ nhà và thử lại khi bổ sung |
| Chống trùng | Khóa giao dịch và đánh dấu theo hợp đồng/tháng/năm; hóa đơn đã thu/đang đối soát không tự bị ghi đè |
| Phòng hết hạn | Cập nhật về AVAILABLE khi không còn hợp đồng hiệu lực; không đánh trống khi ngày cần xác minh còn thiếu |
| Thanh toán | BANK_TRANSFER hoặc CASH; khách báo đã trả, chủ nhà xác nhận. Chưa kết nối cổng thu tiền tự động |
| Hoàn thành công việc | Admin bấm Đã xử lý xong; mục đó rời Chờ xử lý và nằm trong Đã xong |

Hợp đồng cũ cần xác nhận giá thuê và ngày/công tơ thực tế trước khi tính tự động. Tiền thuê được chốt theo hợp đồng; đổi giá niêm yết phòng sau này không làm đổi giá đã chốt. Tính năng hóa đơn bắt đầu từ tháng bật chức năng, không tự tạo hàng loạt hóa đơn cho lịch sử trước đó.

Chi tiết và các trường hợp điều chỉnh sau khi đã lập hóa đơn: [Hướng dẫn thuê và hóa đơn](HUONG_DAN_THUE_VA_HOA_DON.md).

## Cơ sở dữ liệu và dữ liệu mẫu

| Nhóm | Bảng / vai trò |
|---|---|
| 11 bảng nghiệp vụ gốc | tenants, employees, rooms, rules, contracts, invoices, utility_readings, violations, incidents, incident_resolutions, cleaning_supports |
| Tài khoản/hồ sơ | user, user_profiles; mật khẩu BCrypt, vai trò USER/ADMIN |
| Liên kết và yêu cầu thuê | tenant_accounts, contract_requests, rental_request_details |
| Chủ nhà/phòng | landlord_properties, room_landlords |
| Ngày ở thực tế | contract_stays; ngày/công tơ nhận-trả và giá thuê chốt |
| Thanh toán | payment_reports |
| Tự tính hóa đơn | billing_start, automatic_bills, billing_alerts, invoice_notifications |
| Đánh dấu mẫu | demo_seed_history |

SQL chuẩn cho cấu trúc gốc: [server-schema.sql](src/main/resources/server-schema.sql). Bảng bổ sung/quy trình: [schema.sql](src/main/resources/schema.sql). Dữ liệu mẫu: [demo-data.sql](src/main/resources/demo-data.sql).

Cột user.role hiện lưu ordinal: 0 = USER, 1 = ADMIN; API trả tên vai trò. Không đổi thứ tự enum khi còn dữ liệu cũ.

Các bảng dùng PK/FK để liên kết phòng–hợp đồng–khách thuê–hóa đơn. Schema có UNIQUE cho username, CCCD và liên kết tenant_accounts; hồ sơ/ngày ở dùng khóa theo hợp đồng. Chỉ số điện/nước gốc là INT, tiền dùng DECIMAL(18,2). Mỗi phòng/kỳ cần một bản chỉ số hợp lệ; nếu có nhiều bản thì tự tính hóa đơn dừng để chủ nhà kiểm tra, không tự chọn ngẫu nhiên.

Ứng dụng nạp một lần **116 bản ghi giả lập**: 15 phòng, 11 hợp đồng và 10 bản ghi ở mỗi bảng nghiệp vụ còn lại. Không ghi đè mẫu đã sửa và không tự tạo lại bản ghi đã xóa sau khi đã đánh dấu nạp. Phòng/hợp đồng mẫu chưa có chủ nhà hoặc tài khoản khách cần được liên kết/xác minh bằng nghiệp vụ phù hợp; không tự nhận dữ liệu của người khác.

| Thao tác | Cách kiểm tra |
|---|---|
| Xem tài khoản | SELECT id, username, role FROM user; — không cần xuất mật khẩu/hash |
| Xem hợp đồng/hóa đơn | Join invoices.contract_id → contracts.contract_id → tenants.tenant_id |
| Xem trên DBeaver | Kết nối đúng host/port/database đang dùng bởi backend |
| Local và Railway | Là hai DB riêng nếu DB_URL khác nhau; không tự đồng bộ |

Ví dụ truy vấn đọc dữ liệu:

```sql
USE btl_csdl;

SELECT r.room_id, r.area AS area_m2, r.status, u.username AS landlord
FROM rooms r
LEFT JOIN room_landlords l ON l.room_id = r.room_id
LEFT JOIN user u ON u.id = l.user_id
ORDER BY u.username, r.room_id;

SELECT i.invoice_id, i.month, i.year, t.full_name,
       i.rent_amount, i.electricity_amount, i.water_amount, i.total_amount
FROM invoices i
JOIN contracts c ON c.contract_id = i.contract_id
JOIN tenants t ON t.tenant_id = c.tenant_id
ORDER BY i.year DESC, i.month DESC;
```

## API chính

Đăng nhập ở POST /api/auth/login với username/password. Lấy accessToken từ response và gửi header **Authorization: Bearer &lt;accessToken&gt;**. Username/ID của khách được lấy từ xác thực, không tin vào trường username tự gửi trong body.

| Method và đường dẫn | Mục đích / quyền |
|---|---|
| POST /api/auth/register | Tạo USER hoặc ADMIN; ADMIN cần roomCount |
| POST /api/auth/login | Lấy accessToken và thông tin tài khoản |
| GET /api/landlords; GET /api/landlords/rooms | Danh sách chủ nhà/phòng để chọn thuê |
| GET/POST /api/landlords/property | Chủ nhà xem/thiết lập số phòng một lần |
| POST /api/contract-requests | USER gửi hồ sơ, phòng và thời hạn |
| GET /api/contract-requests | Yêu cầu của khách hoặc các yêu cầu gửi đúng chủ nhà |
| PATCH /api/contract-requests/{id}/status | Chủ nhà APPROVED / REJECTED |
| GET /api/contracts; GET /api/invoices | Danh sách theo quyền tài khoản |
| POST /api/contracts/{id}/check-in | Chủ nhà xác nhận date, electricityIndex, waterIndex |
| POST /api/contracts/{id}/check-out | Chủ nhà xác nhận trả phòng thực tế |
| POST /api/contracts/{id}/rent-rate | Xác nhận monthlyRent cho hợp đồng cũ chưa chốt giá |
| POST /api/payment-reports | USER gửi invoiceId, paymentMethod, transactionReference |
| PATCH /api/payment-reports/{id}/status | Chủ nhà xác nhận PAID hoặc REJECTED |
| POST /api/incidents | Khách báo sự cố ở phòng đang thuê; admin theo quyền quản lý |
| PUT /api/incidents/{id}/complete | Chủ nhà hoàn thành sự cố thuộc phòng của mình |
| PUT /api/incident-resolutions/{id}/complete | Hoàn thành một bản xử lý |
| PUT /api/cleaning-supports/{id}/complete | Hoàn thành hỗ trợ dọn dẹp |
| GET /api/queries/{module}/{action} | Tra cứu nhanh; điều kiện thường ở query value |
| GET /api/billing/alerts; POST /api/billing/retry | Chủ nhà xem thiếu dữ liệu / thử tính lại kỳ đến hạn |
| GET /api/billing/notifications | Thông báo hóa đơn của tài khoản |
| POST /api/billing/notifications/{id}/seen | Đánh dấu đã xem thông báo của mình |
| GET/PUT /api/profile; POST /api/profile/avatar | Thông tin cá nhân và ảnh đại diện |

Xem controller tại src/main/java/com/ntd/csdl/controller để biết JSON và URL đầy đủ. Các API quản lý còn lại không mặc định mở cho USER. Lỗi 401 là chưa xác thực/token không hợp lệ; 403 là không đủ quyền.

## Cấu hình môi trường

| Biến | Công dụng |
|---|---|
| DB_URL, DB_USERNAME, DB_PASSWORD | Kết nối MySQL |
| JWT_SECRET | Khóa Base64 ký JWT HS256; nên sinh ít nhất 32 byte ngẫu nhiên |
| PORT | Cổng Spring Boot, mặc định 8080 |
| SPRING_PROFILES_ACTIVE=prod | Dùng cấu hình server, tự chạy cả hai script schema |
| SPRING_SQL_INIT_SCHEMA_LOCATIONS | Override danh sách SQL khi chạy local như phần hướng dẫn |
| REDIS_HOST, REDIS_PORT, REDIS_PASSWORD | Dịch vụ Redis |
| MINIO_ENDPOINT | Địa chỉ backend kết nối MinIO |
| MINIO_PUBLIC_ENDPOINT | Địa chỉ trình duyệt truy cập ảnh/URL ký |
| MINIO_ACCESS_KEY, MINIO_SECRET_KEY, MINIO_BUCKET | Thông tin lưu ảnh |
| APP_DEMO_SEED_ENABLED=false | Tắt nạp mẫu tự động |
| APP_RENTAL_JOBS_ENABLED=false | Tắt tác vụ định kỳ ngày thuê/hóa đơn |

File .env không tự được Spring Boot local đọc. Docker Compose đọc .env cho cấu hình Compose; với IntelliJ/PowerShell cần truyền biến vào đúng tiến trình ứng dụng. Không commit mật khẩu hay khóa môi trường thật.

## Kiểm tra và triển khai

### Kiểm tra logic độc lập với MySQL thật

```powershell
.\mvnw.cmd "-Dtest=StayBillingTests,TenantPrivacyPaymentTests,LandlordInventoryTests,IncidentCompletionTests" test
node scripts/test-room-groups.mjs
node scripts/test-completion.mjs
```

Bộ kiểm tra trên dùng H2 trong bộ nhớ và repository mô phỏng ở một số luồng quyền; không thay thế thử nghiệm tích hợp trên MySQL/Railway. Toàn bộ Maven test còn có các bài SpringBootTest cần DB/schema và cấu hình phù hợp. Không hướng bài kiểm tra ghi dữ liệu vào DB thật đang dùng.

Các script HTTP test-rental-workflow.mjs và test-query-workflow.mjs cần backend thử riêng tại localhost:18082, với database thử riêng; chúng tạo/cập nhật dữ liệu. Sau đó chạy node scripts/test-rental-workflow.mjs hoặc node scripts/test-query-workflow.mjs.

### Railway và Docker

[DEPLOY_RAILWAY.md](DEPLOY_RAILWAY.md) có cấu hình các dịch vụ và biến tham chiếu Railway. Đối chiếu repo/nhánh và commit triển khai; không dùng README hay ngày sửa file trên máy để kết luận server đã nhận bản mới. Các thông tin gói/giá trong tài liệu cũ cần kiểm tra lại tại tài khoản Railway.

Dockerfile gốc đóng gói Java. Với máy chủ tự quản lý, tham khảo [compose.server.yml](compose.server.yml), [production.env.example](production.env.example) và [Caddyfile](Caddyfile). Điền biến thật ở môi trường triển khai. Dữ liệu MySQL/MinIO cần volume bền vững.

Khi web đã chạy trên Railway, người dùng mở link web mà không cần bật IntelliJ trên máy phát triển. DBeaver có thể xem DB Railway nếu đã cấu hình kết nối đến đúng endpoint được cung cấp; localhost:3306 thường là MySQL local, không phải Railway.

## Khắc phục lỗi

| Hiện tượng | Kiểm tra |
|---|---|
| Vẫn giao diện cũ | Đúng commit Railway chưa? Đã build React và commit static chưa? Tải lại Ctrl+F5 |
| 502 trên Railway | Đọc Deploy Logs, tìm Caused by sâu nhất; kiểm tra DB_URL, JWT_SECRET, profile và cổng public khớp PORT |
| Thiếu bảng/cột, Hibernate validate lỗi | Đã chạy đúng hai file SQL chưa? DB cũ có khác schema không? Không tự xóa DB để xử lý |
| Không kết nối DB | Dịch vụ MySQL, cổng, tên database, username/password trong tiến trình backend |
| Đăng ký trên web nhưng DBeaver không thấy | So sánh host/port/database với DB_URL của bản web đang chạy |
| 401 khi gọi API | Đăng nhập lại và gửi accessToken Bearer; URL chỉ chứa đường dẫn, JSON đặt trong Body |
| 403 | Kiểm tra vai trò, chủ nhà sở hữu phòng, hợp đồng/hóa đơn có thuộc khách không |
| Username đã tồn tại | Đăng nhập tài khoản đã có hoặc chọn tên khác; không cần xóa UNIQUE |
| Không thấy hợp đồng riêng | Khách phải gửi yêu cầu bằng tài khoản mình và được chủ nhà duyệt; không liên kết theo họ tên |
| Không thấy phòng để thuê | Chủ nhà đã cấu hình phòng và đặt AVAILABLE chưa? Có đang chọn đúng chủ nhà không? |
| Không có hóa đơn tự động | Đã đến kỳ chưa? Đã xác nhận giá/check-in và đủ chỉ số? Xem khung cảnh báo chủ nhà |
| Ảnh đại diện không tải | MinIO đang chạy? Endpoint công khai, khóa truy cập và bucket khớp chưa? |

## Cấu trúc và tài liệu

```text
BTL_CSDL_NHOM8/
├── src/main/java/com/ntd/csdl/
│   ├── config/             # Security, Redis, tác vụ định kỳ
│   ├── controller/         # REST API
│   ├── dto/                # Dữ liệu request/response
│   ├── entity/             # JPA Entity
│   ├── repo/               # Truy vấn JPA
│   ├── security/           # JWT, thông tin người dùng
│   └── service/            # Nghiệp vụ, ngày ở, hóa đơn
├── src/main/resources/     # SQL, cấu hình, static React đã build
├── src/test/java/          # Kiểm tra Java
├── frontend/src/           # React component, màn hình, dữ liệu giao diện
├── database/               # SQL server và migration username
├── scripts/                # Kiểm tra HTTP và bộ lọc giao diện
├── docker/minio/           # Build MinIO
└── docs/                   # Nội dung README báo cáo cũ
```

| Tài liệu | Nội dung |
|---|---|
| [HUONG_DAN_THUE_VA_HOA_DON.md](HUONG_DAN_THUE_VA_HOA_DON.md) | Check-in/out, công thức tiền, công tơ, tác vụ cuối tháng |
| [ROLE_WORKFLOWS.md](ROLE_WORKFLOWS.md) | Vai trò, yêu cầu thuê và quy trình hiện hành |
| [DEMO_AND_QUERIES.md](DEMO_AND_QUERIES.md) | Dữ liệu mẫu và nút tra cứu |
| [HUONG_DAN_CHAY.md](HUONG_DAN_CHAY.md) | Hướng dẫn chạy bổ sung; đối chiếu README này cho các bước hiện tại |
| [Frontend README](frontend/README.md) | Chạy/build React và Vite |
| [DEPLOY_RAILWAY.md](DEPLOY_RAILWAY.md) | Cấu hình triển khai Railway |
| [README báo cáo cũ](docs/README_BAO_CAO_CU.md) | Giữ lại toàn bộ nội dung đã viết trước đây để tham khảo; có phần đã lỗi thời |

README là hướng dẫn chạy và dùng mã nguồn. Báo cáo môn Cơ sở dữ liệu trình bày riêng phần phân tích yêu cầu, ER, lược đồ quan hệ, khóa/ràng buộc, SQL và chuẩn hóa theo schema đã xác minh.
