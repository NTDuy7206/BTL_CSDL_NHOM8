# BÀI TẬP LỚN CƠ SỞ DỮ LIỆU
# XÂY DỰNG HỆ THỐNG QUẢN LÝ TÒA NHÀ VÀ PHÒNG CHO THUÊ

---

# MỤC LỤC

## CHƯƠNG 1. GIỚI THIỆU ĐỀ TÀI
- 1.1. Lý do chọn đề tài
- 1.2. Mục tiêu của đề tài
- 1.3. Đối tượng và phạm vi của đề tài
- 1.4. Phương pháp thực hiện
- 1.5. Công nghệ sử dụng
- 1.6. Kết quả dự kiến

## CHƯƠNG 2. KHẢO SÁT VÀ PHÂN TÍCH BÀI TOÁN
- 2.1. Khảo sát bài toán thực tế
- 2.2. Mô tả bài toán
- 2.3. Xác định các đối tượng quản lý
- 2.4. Phân tích các nghiệp vụ
- 2.5. Yêu cầu chức năng
- 2.6. Yêu cầu phi chức năng
- 2.7. Quy trình hoạt động của hệ thống

## CHƯƠNG 3. PHÂN TÍCH VÀ THIẾT KẾ CƠ SỞ DỮ LIỆU
- 3.1. Xác định thực thể
- 3.2. Xác định thuộc tính
- 3.3. Xác định khóa chính và khóa ngoại
- 3.4. Thiết kế mối quan hệ giữa các bảng
- 3.5. Cài đặt cơ sở dữ liệu bằng SQL
- 3.6. Kiểm tra và xác nhận CSDL

## CHƯƠNG 4. XÂY DỰNG ỨNG DỤNG SPRING BOOT
- 4.1. Giới thiệu công nghệ sử dụng
- 4.2. Kiến trúc hệ thống
- 4.3. Khởi tạo và cấu hình dự án Spring Boot
- 4.4. Xây dựng Entity và ánh xạ CSDL
- 4.5. Xây dựng Repository và truy vấn CSDL
- 4.6. Xây dựng Service xử lý nghiệp vụ
- 4.7. Xây dựng REST API với Controller
- 4.8. Xây dựng các chức năng quản lý chính
- 4.9. Xây dựng chức năng tìm kiếm và thống kê
- 4.10. Spring Security và JWT
- 4.11. Tích hợp Redis
- 4.12. Kiểm thử REST API bằng Postman
- 4.13. Kết quả xây dựng hệ thống

## CHƯƠNG 5. KIỂM THỬ VÀ ĐÁNH GIÁ HỆ THỐNG
- 5.1. Môi trường kiểm thử
- 5.2. Kiểm thử cơ sở dữ liệu
- 5.3. Kiểm thử CRUD
- 5.4. Kiểm thử truy vấn
- 5.5. Kiểm thử Security và JWT
- 5.6. Kiểm thử Redis
- 5.7. Đánh giá kết quả
- 5.8. Hạn chế của hệ thống
- 5.9. Hướng phát triển

## KẾT LUẬN

---

# CHƯƠNG 1. GIỚI THIỆU ĐỀ TÀI

## 1.1. Lý do chọn đề tài

Trong thực tế, việc quản lý một tòa nhà có nhiều phòng cho thuê phát sinh nhiều loại dữ liệu như thông tin người thuê, nhân viên, phòng, hợp đồng, hóa đơn, điện nước, vi phạm và sự cố.

Nếu quản lý bằng phương pháp thủ công hoặc bằng nhiều file riêng lẻ, việc tìm kiếm, cập nhật và thống kê dữ liệu sẽ gặp nhiều khó khăn. Dữ liệu có thể bị trùng lặp, thiếu đồng nhất và khó kiểm soát mối quan hệ giữa các đối tượng.

Vì vậy, đề tài **Xây dựng hệ thống quản lý tòa nhà và phòng cho thuê** được lựa chọn nhằm áp dụng kiến thức về cơ sở dữ liệu vào một bài toán thực tế.

Hệ thống sử dụng MySQL để lưu trữ dữ liệu và Spring Boot để xây dựng backend. Ngoài ra, project tích hợp Spring Data JPA, Spring Security, JWT và Redis nhằm tăng khả năng mở rộng, bảo mật và tối ưu truy vấn.

## 1.2. Mục tiêu của đề tài

Mục tiêu tổng quát là xây dựng một hệ thống quản lý tòa nhà có cơ sở dữ liệu rõ ràng và ứng dụng backend cho phép thao tác với dữ liệu thông qua REST API.

Các mục tiêu cụ thể:

- Phân tích bài toán quản lý tòa nhà.
- Xác định thực thể và thuộc tính.
- Xác định khóa chính và khóa ngoại.
- Thiết kế các mối quan hệ.
- Xây dựng cơ sở dữ liệu MySQL.
- Cài đặt CSDL bằng SQL.
- Kiểm tra dữ liệu và ràng buộc.
- Xây dựng Entity bằng Java.
- Kết nối Spring Boot với MySQL.
- Xây dựng Repository, Service và Controller.
- Thực hiện CRUD.
- Xây dựng truy vấn tìm kiếm và thống kê.
- Tích hợp Spring Security và JWT.
- Tích hợp Redis.
- Kiểm thử API bằng Postman.

## 1.3. Đối tượng và phạm vi của đề tài

### Đối tượng quản lý

Hệ thống quản lý:

- Người thuê.
- Nhân viên.
- Phòng.
- Quy định.
- Hợp đồng.
- Vi phạm.
- Sự cố.
- Xử lý sự cố.
- Hỗ trợ dọn dẹp.
- Chỉ số điện nước.
- Hóa đơn.

### Phạm vi

Đề tài tập trung vào:

1. Thiết kế cơ sở dữ liệu.
2. Cài đặt MySQL.
3. Xây dựng backend Spring Boot.
4. Xây dựng REST API.
5. CRUD.
6. Truy vấn dữ liệu.
7. Tìm kiếm và thống kê.
8. Authentication và Authorization.
9. JWT.
10. Redis Cache.
11. Kiểm thử bằng Postman.

## 1.4. Phương pháp thực hiện

Quy trình thực hiện:

```text
Khảo sát bài toán
       ↓
Phân tích yêu cầu
       ↓
Xác định thực thể
       ↓
Xác định thuộc tính
       ↓
Xác định PK / FK
       ↓
Thiết kế quan hệ
       ↓
Cài đặt MySQL
       ↓
Kiểm tra CSDL
       ↓
Xây dựng Spring Boot
       ↓
Entity → Repository → Service → Controller
       ↓
Security / JWT / Redis
       ↓
Kiểm thử Postman
       ↓
Đánh giá
```

## 1.5. Công nghệ sử dụng

| Công nghệ | Mục đích |
|---|---|
| Java | Ngôn ngữ lập trình |
| Spring Boot | Xây dựng backend |
| Spring Data JPA | Truy cập cơ sở dữ liệu |
| MySQL | Lưu trữ dữ liệu |
| Maven | Quản lý dependency và build |
| Lombok | Giảm code Java |
| Spring Security | Bảo mật |
| JWT | Xác thực |
| Redis | Cache |
| Postman | Kiểm thử API |
| IntelliJ IDEA | Môi trường phát triển |

## 1.6. Kết quả dự kiến

Sau khi hoàn thành, hệ thống có khả năng:

- Quản lý dữ liệu tập trung.
- Thực hiện CRUD.
- Tìm kiếm dữ liệu.
- Truy vấn dữ liệu từ nhiều bảng.
- Thống kê dữ liệu.
- Xác thực người dùng bằng JWT.
- Phân quyền API.
- Cache dữ liệu bằng Redis.
- Cung cấp REST API.

---

# CHƯƠNG 2. KHẢO SÁT VÀ PHÂN TÍCH BÀI TOÁN

## 2.1. Khảo sát bài toán thực tế

Một tòa nhà có nhiều phòng cho thuê cần quản lý đồng thời nhiều đối tượng và nghiệp vụ.

Người quản lý cần biết:

- Có bao nhiêu phòng.
- Phòng nào đang được thuê.
- Phòng nào còn trống.
- Ai đang thuê phòng.
- Hợp đồng bắt đầu và kết thúc khi nào.
- Hóa đơn của từng tháng.
- Chỉ số điện nước.
- Các trường hợp vi phạm.
- Các sự cố xảy ra.
- Nhân viên nào xử lý sự cố.
- Nhân viên nào thực hiện hỗ trợ.

Do đó cần một cơ sở dữ liệu tập trung để liên kết các thông tin trên.

## 2.2. Mô tả bài toán

Hệ thống quản lý tòa nhà được xây dựng xoay quanh phòng cho thuê.

Người thuê ký hợp đồng với phòng. Hợp đồng lưu thời gian thuê và tiền đặt cọc. Trong quá trình thuê, phòng có thể phát sinh vi phạm, sự cố, yêu cầu hỗ trợ hoặc chỉ số điện nước.

Từ hợp đồng và các khoản phát sinh, hệ thống quản lý hóa đơn theo tháng.

Nhân viên có thể tham gia xử lý sự cố hoặc thực hiện các công việc hỗ trợ.

## 2.3. Xác định các đối tượng quản lý

Các đối tượng chính:

1. `Tenant`
2. `Employee`
3. `Room`
4. `Rule`
5. `Contract`
6. `Violation`
7. `Incident`
8. `IncidentResolution`
9. `CleaningSupport`
10. `UtilityReading`
11. `Invoice`

## 2.4. Phân tích các nghiệp vụ

### 2.4.1. Quản lý người thuê

- Thêm người thuê.
- Cập nhật thông tin.
- Xóa người thuê.
- Tìm kiếm.
- Xem chi tiết.

### 2.4.2. Quản lý nhân viên

- Thêm nhân viên.
- Cập nhật nhân viên.
- Xóa nhân viên.
- Xem danh sách.
- Tìm kiếm.

### 2.4.3. Quản lý phòng

- Thêm phòng.
- Cập nhật phòng.
- Xóa phòng.
- Xem phòng.
- Tìm phòng theo trạng thái.

### 2.4.4. Quản lý hợp đồng

- Tạo hợp đồng.
- Cập nhật hợp đồng.
- Xem hợp đồng.
- Liên kết người thuê với phòng.

### 2.4.5. Quản lý hóa đơn

- Tạo hóa đơn.
- Tra cứu hóa đơn.
- Tra cứu theo tháng/năm.
- Quản lý tiền thuê, điện, nước và tiền phạt.

### 2.4.6. Quản lý điện nước

- Ghi nhận chỉ số điện.
- Ghi nhận chỉ số nước.
- Lưu giá điện.
- Lưu giá nước.
- Theo dõi theo tháng/năm.

### 2.4.7. Quản lý vi phạm

- Ghi nhận vi phạm.
- Liên kết với quy định.
- Ghi nhận mức phạt.

### 2.4.8. Quản lý sự cố

- Ghi nhận sự cố.
- Theo dõi trạng thái.
- Phân công nhân viên.
- Ghi nhận kết quả xử lý.

### 2.4.9. Quản lý hỗ trợ dọn dẹp

- Tạo yêu cầu hỗ trợ.
- Phân công nhân viên.
- Cập nhật trạng thái hoàn thành.

## 2.5. Yêu cầu chức năng

Hệ thống cần đáp ứng:

- Đăng nhập.
- Xác thực người dùng.
- Phân quyền.
- Quản lý người thuê.
- Quản lý nhân viên.
- Quản lý phòng.
- Quản lý quy định.
- Quản lý hợp đồng.
- Quản lý vi phạm.
- Quản lý sự cố.
- Quản lý xử lý sự cố.
- Quản lý hỗ trợ dọn dẹp.
- Quản lý điện nước.
- Quản lý hóa đơn.
- Tìm kiếm.
- Thống kê.
- REST API.
- Cache.

## 2.6. Yêu cầu phi chức năng

Hệ thống cần:

- Dễ sử dụng.
- Dễ bảo trì.
- Có cấu trúc rõ ràng.
- Bảo mật API.
- Đảm bảo tính nhất quán dữ liệu.
- Có khả năng mở rộng.
- Có khả năng cache.
- Có khả năng xử lý lỗi.
- Có thể kiểm thử từng API.

## 2.7. Quy trình hoạt động của hệ thống

```text
Đăng nhập
   ↓
Xác thực JWT
   ↓
Gọi API
   ↓
Controller
   ↓
Service
   ↓
Redis / Repository
   ↓
MySQL
   ↓
JSON Response
```

---

# CHƯƠNG 3. PHÂN TÍCH VÀ THIẾT KẾ CƠ SỞ DỮ LIỆU

## 3.1. Xác định thực thể

CSDL gồm 11 bảng:

```text
tenants
employees
rooms
rules
contracts
violations
incidents
incident_resolutions
cleaning_supports
utility_readings
invoices
```

## 3.2. Xác định thuộc tính

### 3.2.1. tenants

| Thuộc tính | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|
| tenant_id | VARCHAR(20) | PK | Mã người thuê |
| cccd | VARCHAR(20) | UNIQUE | Số CCCD |
| full_name | VARCHAR(50) | NULL | Họ tên |
| date_of_birth | DATE | NULL | Ngày sinh |
| permanent_address | VARCHAR(200) | NULL | Địa chỉ thường trú |
| phone_number | VARCHAR(10) | NULL | Số điện thoại |

### 3.2.2. employees

| Thuộc tính | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|
| employee_id | VARCHAR(20) | PK | Mã nhân viên |
| cccd | VARCHAR(20) | UNIQUE | Số CCCD |
| full_name | VARCHAR(50) | NULL | Họ tên |
| position | VARCHAR(50) | NULL | Chức vụ |
| base_salary | DECIMAL(18,2) | NULL | Lương cơ bản |
| seniority | INT | NULL | Thâm niên |
| date_of_birth | DATE | NULL | Ngày sinh |
| phone_number | VARCHAR(10) | NULL | Số điện thoại |

### 3.2.3. rooms

| Thuộc tính | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|
| room_id | VARCHAR(20) | PK | Mã phòng |
| floor | INT | NULL | Tầng |
| area | FLOAT | NULL | Diện tích |
| room_type | VARCHAR(50) | NULL | Loại phòng |
| status | VARCHAR(50) | NULL | Trạng thái |
| base_price | DECIMAL(18,2) | NULL | Giá cơ bản |

### 3.2.4. rules

| Thuộc tính | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|
| rule_id | VARCHAR(20) | PK | Mã quy định |
| violation_name | VARCHAR(100) | NULL | Tên vi phạm |
| description | TEXT | NULL | Mô tả |
| prescribed_fine | DECIMAL(18,2) | NULL | Mức phạt quy định |

### 3.2.5. contracts

| Thuộc tính | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|
| contract_id | VARCHAR(20) | PK | Mã hợp đồng |
| start_date | DATE | NULL | Ngày bắt đầu |
| end_date | DATE | NULL | Ngày kết thúc |
| deposit | DECIMAL(18,2) | NULL | Tiền đặt cọc |
| room_id | VARCHAR(20) | FK | Mã phòng |
| tenant_id | VARCHAR(20) | FK | Mã người thuê |

### 3.2.6. violations

| Thuộc tính | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|
| violation_id | VARCHAR(20) | PK | Mã vi phạm |
| violation_name | VARCHAR(100) | NULL | Tên vi phạm |
| violation_date | DATE | NULL | Ngày vi phạm |
| room_id | VARCHAR(20) | FK | Mã phòng |
| rule_id | VARCHAR(20) | FK | Mã quy định |
| detailed_description | TEXT | NULL | Mô tả chi tiết |
| actual_fine | DECIMAL(18,2) | NULL | Mức phạt thực tế |

### 3.2.7. incidents

| Thuộc tính | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|
| incident_id | VARCHAR(20) | PK | Mã sự cố |
| incident_content | TEXT | NULL | Nội dung |
| report_date | DATE | NULL | Ngày báo cáo |
| status | VARCHAR(50) | NULL | Trạng thái |
| room_id | VARCHAR(20) | FK | Mã phòng |

### 3.2.8. incident_resolutions

| Thuộc tính | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|
| resolution_id | VARCHAR(20) | PK | Mã xử lý |
| resolution_date | DATE | NULL | Ngày xử lý |
| result | TEXT | NULL | Kết quả |
| is_completed | VARCHAR(10) | NULL | Hoàn thành |
| incident_id | VARCHAR(20) | FK | Mã sự cố |
| employee_id | VARCHAR(20) | FK | Mã nhân viên |

### 3.2.9. cleaning_supports

| Thuộc tính | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|
| support_id | VARCHAR(20) | PK | Mã hỗ trợ |
| support_date | DATE | NULL | Ngày hỗ trợ |
| task_content | TEXT | NULL | Nội dung |
| is_completed | VARCHAR(10) | NULL | Hoàn thành |
| room_id | VARCHAR(20) | FK | Mã phòng |
| employee_id | VARCHAR(20) | FK | Mã nhân viên |

### 3.2.10. utility_readings

| Thuộc tính | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|
| reading_id | VARCHAR(20) | PK | Mã chỉ số |
| month | INT | NULL | Tháng |
| year | INT | NULL | Năm |
| electricity_price | DECIMAL(18,2) | NULL | Giá điện |
| water_price | DECIMAL(18,2) | NULL | Giá nước |
| electricity_start_index | INT | NULL | Điện đầu kỳ |
| electricity_end_index | INT | NULL | Điện cuối kỳ |
| water_start_index | INT | NULL | Nước đầu kỳ |
| water_end_index | INT | NULL | Nước cuối kỳ |
| room_id | VARCHAR(20) | FK | Mã phòng |

### 3.2.11. invoices

| Thuộc tính | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|
| invoice_id | VARCHAR(20) | PK | Mã hóa đơn |
| month | INT | NULL | Tháng |
| year | INT | NULL | Năm |
| rent_amount | DECIMAL(18,2) | NULL | Tiền thuê |
| electricity_amount | DECIMAL(18,2) | NULL | Tiền điện |
| water_amount | DECIMAL(18,2) | NULL | Tiền nước |
| fine_amount | DECIMAL(18,2) | NULL | Tiền phạt |
| total_amount | DECIMAL(18,2) | NULL | Tổng tiền |
| contract_id | VARCHAR(20) | FK | Mã hợp đồng |

> Mô tả trên bám theo script SQL hiện tại: chỉ khóa chính được khai báo bắt buộc; các thuộc tính khác không có `NOT NULL` nên có thể nhận `NULL`. Hiện script cũng không khai báo `DEFAULT`.

## 3.3. Xác định khóa chính và khóa ngoại

### Khóa chính

```text
tenants              → tenant_id
employees            → employee_id
rooms                → room_id
rules                → rule_id
contracts            → contract_id
violations           → violation_id
incidents             → incident_id
incident_resolutions → resolution_id
cleaning_supports    → support_id
utility_readings     → reading_id
invoices             → invoice_id
```

### Khóa ngoại

```text
contracts.room_id → rooms.room_id
contracts.tenant_id → tenants.tenant_id

violations.room_id → rooms.room_id
violations.rule_id → rules.rule_id

incidents.room_id → rooms.room_id

incident_resolutions.incident_id → incidents.incident_id
incident_resolutions.employee_id → employees.employee_id

cleaning_supports.room_id → rooms.room_id
cleaning_supports.employee_id → employees.employee_id

utility_readings.room_id → rooms.room_id

invoices.contract_id → contracts.contract_id
```

## 3.4. Thiết kế mối quan hệ giữa các bảng

```text
tenants 1 -------- N contracts N -------- 1 rooms

rooms 1 -------- N violations N -------- 1 rules

rooms 1 -------- N incidents
                  |
                  N
                  |
           incident_resolutions
                  |
                  N
                  |
              employees

rooms 1 -------- N cleaning_supports N -------- 1 employees

rooms 1 -------- N utility_readings

contracts 1 -------- N invoices
```

## 3.5. Cài đặt cơ sở dữ liệu bằng SQL

Tên database:

```text
btl_csdl
```

Khởi tạo:

```sql
CREATE DATABASE btl_csdl;
USE btl_csdl;
```

Các bảng được tạo theo thứ tự phù hợp với quan hệ khóa ngoại.

Ví dụ:

```sql
CREATE TABLE tenants (
    tenant_id VARCHAR(20) PRIMARY KEY,
    cccd VARCHAR(20) UNIQUE,
    full_name VARCHAR(50),
    date_of_birth DATE,
    permanent_address VARCHAR(200),
    phone_number VARCHAR(10)
);
```

Các bảng còn lại được cài đặt theo thiết kế ở trên.

## 3.6. Kiểm tra và xác nhận CSDL

Kiểm tra database:

```sql
SHOW DATABASES;
```

Kiểm tra bảng:

```sql
SHOW TABLES;
```

Kiểm tra cấu trúc:

```sql
DESC tenants;
DESC employees;
DESC rooms;
DESC contracts;
```

Kiểm tra khóa ngoại:

```sql
SHOW CREATE TABLE contracts;
```

Kiểm tra dữ liệu:

```sql
SELECT * FROM tenants;
SELECT * FROM rooms;
SELECT * FROM contracts;
```

Mục tiêu là xác nhận:

- Database tồn tại.
- Các bảng được tạo.
- Kiểu dữ liệu đúng.
- PK đúng.
- FK đúng.
- Quan hệ đúng.
- Dữ liệu có thể truy vấn.

---

# CHƯƠNG 4. XÂY DỰNG ỨNG DỤNG SPRING BOOT

## 4.1. Giới thiệu công nghệ sử dụng

Project backend sử dụng Java Spring Boot kết hợp:

- Spring Data JPA.
- MySQL.
- Maven.
- Lombok.
- Spring Security.
- JWT.
- Redis.
- Postman.

## 4.2. Kiến trúc hệ thống

```text
Client
   ↓
Controller
   ↓
Service
   ↓
Repository
   ↓
MySQL
```

Redis được sử dụng như lớp cache:

```text
Client
   ↓
Controller
   ↓
Service
   ├── Redis
   └── Repository → MySQL
```

## 4.3. Khởi tạo và cấu hình dự án Spring Boot

Cấu trúc cơ bản:

```text
src
└── main
    ├── java
    │   └── com.ntd.csdl
    └── resources
        └── application.yaml
```

Cấu hình MySQL:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/btl_csdl
    username: root
    password: your_password

  jpa:
    hibernate:
      ddl-auto: none
    show-sql: true
```

`ddl-auto: none` được dùng vì database đã được xây dựng bằng SQL.

## 4.4. Xây dựng Entity và ánh xạ CSDL

Mapping:

```text
tenants              → Tenant
employees            → Employee
rooms                → Room
rules                → Rule
contracts            → Contract
violations           → Violation
incidents             → Incident
incident_resolutions → IncidentResolution
cleaning_supports    → CleaningSupport
utility_readings     → UtilityReading
invoices             → Invoice
```

Ví dụ:

```java
@Entity
@Table(name = "tenants")
public class Tenant {

    @Id
    @Column(name = "tenant_id")
    private String tenantId;

    @Column(name = "cccd")
    private String cccd;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "permanent_address")
    private String permanentAddress;

    @Column(name = "phone_number")
    private String phoneNumber;
}
```

Do ID trong database là `VARCHAR(20)`, ID trong Java sử dụng `String`.

## 4.5. Xây dựng Repository và truy vấn CSDL

Repository sử dụng `JpaRepository`.

Ví dụ:

```java
public interface RoomRepository
        extends JpaRepository<Room, String> {

    List<Room> findByStatus(String status);
}
```

### Derived Query

```java
List<Tenant> findByFullNameContainingIgnoreCase(String name);
```

### Tìm hóa đơn theo tháng và năm

```java
List<Invoice> findByMonthAndYear(
    Integer month,
    Integer year
);
```

### JPQL thống kê

```java
@Query("""
    SELECT r.status, COUNT(r)
    FROM Room r
    GROUP BY r.status
""")
List<Object[]> countRoomsByStatus();
```

### Truy vấn nhiều bảng

Có thể sử dụng JPQL để kết hợp Tenant, Contract và Room phục vụ tra cứu.

Ví dụ:

```java
@Query("""
    SELECT t, c, r
    FROM Tenant t
    JOIN Contract c ON t.tenantId = c.tenantId
    JOIN Room r ON c.roomId = r.roomId
""")
List<Object[]> findTenantRooms();
```

## 4.6. Xây dựng Service xử lý nghiệp vụ

Service nằm giữa Controller và Repository.

Ví dụ:

```java
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public List<Room> getAvailableRooms() {
        return roomRepository.findByStatus("AVAILABLE");
    }

    public Room save(Room room) {
        return roomRepository.save(room);
    }
}
```

## 4.7. Xây dựng REST API với Controller

Ví dụ:

```java
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    @GetMapping("/available")
    public List<Room> getAvailableRooms() {
        return roomService.getAvailableRooms();
    }
}
```

## 4.8. Xây dựng các chức năng quản lý chính

### Tenant

```text
POST   /api/tenants
GET    /api/tenants
GET    /api/tenants/{id}
PUT    /api/tenants/{id}
DELETE /api/tenants/{id}
```

### Employee

```text
POST   /api/employees
GET    /api/employees
GET    /api/employees/{id}
PUT    /api/employees/{id}
DELETE /api/employees/{id}
```

### Room

```text
POST   /api/rooms
GET    /api/rooms
GET    /api/rooms/{id}
PUT    /api/rooms/{id}
DELETE /api/rooms/{id}
GET    /api/rooms/available
```

### Contract

```text
POST   /api/contracts
GET    /api/contracts
GET    /api/contracts/{id}
PUT    /api/contracts/{id}
DELETE /api/contracts/{id}
```

### Invoice

```text
POST   /api/invoices
GET    /api/invoices
GET    /api/invoices/{id}
PUT    /api/invoices/{id}
DELETE /api/invoices/{id}
```

### Violation

```text
POST /api/violations
GET  /api/violations
GET  /api/violations/{id}
PUT  /api/violations/{id}
```

### Incident

```text
POST /api/incidents
GET  /api/incidents
GET  /api/incidents/{id}
PUT  /api/incidents/{id}
```

### Cleaning Support

```text
POST /api/cleaning-supports
GET  /api/cleaning-supports
GET  /api/cleaning-supports/{id}
PUT  /api/cleaning-supports/{id}
```

## 4.9. Xây dựng chức năng tìm kiếm và thống kê

### Tìm phòng theo trạng thái

```java
List<Room> findByStatus(String status);
```

### Tìm người thuê theo tên

```java
List<Tenant> findByFullNameContainingIgnoreCase(String name);
```

### Tìm hóa đơn theo tháng/năm

```java
List<Invoice> findByMonthAndYear(
    Integer month,
    Integer year
);
```

### Thống kê phòng

```java
@Query("""
    SELECT r.status, COUNT(r)
    FROM Room r
    GROUP BY r.status
""")
List<Object[]> countRoomsByStatus();
```

## 4.10. Spring Security và JWT

Security chịu trách nhiệm:

- Xác thực người dùng.
- Kiểm tra tài khoản.
- Xác thực JWT.
- Phân quyền.
- Bảo vệ API.

Cấu trúc:

```text
security
├── CustomUserDetailsService.java
├── JwtAuthenticationFilter.java
└── JwtService.java
```

Luồng:

```text
Login
  ↓
Kiểm tra username/password
  ↓
Tạo JWT
  ↓
Client nhận token
  ↓
Gửi Bearer Token
  ↓
JwtAuthenticationFilter
  ↓
Xác thực token
  ↓
Truy cập API
```

## 4.11. Tích hợp Redis

Redis được sử dụng để cache dữ liệu.

Luồng:

```text
GET API
   ↓
Kiểm tra Redis
   ├── Có dữ liệu → Response
   │
   └── Không có
          ↓
        MySQL
          ↓
        Redis
          ↓
       Response
```

Các dữ liệu có thể cache:

- Danh sách phòng.
- Phòng còn trống.
- Dữ liệu thống kê.
- Dữ liệu được truy vấn thường xuyên.

## 4.12. Kiểm thử REST API bằng Postman

Ví dụ thêm người thuê:

```http
POST /api/tenants
```

Body:

```json
{
    "tenantId": "T001",
    "cccd": "001234567890",
    "fullName": "Nguyen Van A",
    "dateOfBirth": "2000-05-10",
    "permanentAddress": "Ha Noi",
    "phoneNumber": "0912345678"
}
```

Lấy danh sách:

```http
GET /api/tenants
```

Tìm phòng trống:

```http
GET /api/rooms/available
```

Tra cứu hóa đơn:

```http
GET /api/invoices?month=9&year=2026
```

## 4.13. Kết quả xây dựng hệ thống

Sau Chương 4, hệ thống có:

- Spring Boot backend.
- MySQL.
- Entity.
- Repository.
- Service.
- Controller.
- REST API.
- CRUD.
- Query.
- Search.
- Statistics.
- Security.
- JWT.
- Redis.
- Exception handling.

---

# CHƯƠNG 5. KIỂM THỬ VÀ ĐÁNH GIÁ HỆ THỐNG

## 5.1. Môi trường kiểm thử

| Thành phần | Công cụ |
|---|---|
| Hệ điều hành | Windows |
| IDE | IntelliJ IDEA |
| Ngôn ngữ | Java |
| Backend | Spring Boot |
| Database | MySQL |
| Cache | Redis |
| API Test | Postman |

## 5.2. Kiểm thử cơ sở dữ liệu

Kiểm tra:

- Database.
- Bảng.
- PK.
- FK.
- Unique.
- Thêm dữ liệu.
- Cập nhật dữ liệu.
- Xóa dữ liệu.
- Truy vấn dữ liệu.

## 5.3. Kiểm thử CRUD

Với mỗi module:

```text
CREATE
READ
UPDATE
DELETE
```

Ví dụ Room:

```text
POST   /api/rooms
GET    /api/rooms
GET    /api/rooms/{id}
PUT    /api/rooms/{id}
DELETE /api/rooms/{id}
```

## 5.4. Kiểm thử truy vấn

Kiểm tra:

- Tìm kiếm người thuê.
- Tìm phòng theo trạng thái.
- Tìm hóa đơn theo tháng/năm.
- Thống kê phòng.
- Truy vấn nhiều bảng.

## 5.5. Kiểm thử Security và JWT

Các trường hợp:

1. Đăng nhập đúng.
2. Đăng nhập sai.
3. Không gửi JWT.
4. Gửi JWT hợp lệ.
5. Gửi JWT không hợp lệ.
6. Người dùng không đủ quyền.

## 5.6. Kiểm thử Redis

Kiểm tra:

1. Request lần đầu.
2. Redis chưa có cache.
3. Dữ liệu được lấy từ MySQL.
4. Dữ liệu được lưu vào Redis.
5. Request tiếp theo đọc từ cache.
6. Cache được xử lý khi dữ liệu thay đổi.

## 5.7. Đánh giá kết quả

Hệ thống đáp ứng các yêu cầu chính:

- CSDL được thiết kế theo bài toán.
- Các bảng có quan hệ rõ ràng.
- Spring Boot kết nối MySQL.
- REST API hoạt động.
- CRUD được triển khai.
- Truy vấn được thực hiện thông qua Repository.
- Security bảo vệ API.
- JWT hỗ trợ xác thực.
- Redis hỗ trợ cache.
- Postman kiểm thử được API.

## 5.8. Hạn chế của hệ thống

Một số hạn chế:

- Chưa có frontend hoàn chỉnh.
- Chưa triển khai production.
- Phân quyền có thể tiếp tục mở rộng.
- Redis cần xử lý kỹ việc đồng bộ cache.
- Chưa tích hợp thanh toán trực tuyến.
- Chưa có hệ thống thông báo hoàn chỉnh.
- Chưa có dashboard trực quan đầy đủ.

## 5.9. Hướng phát triển

Có thể mở rộng:

- Frontend bằng React/Vue/Angular.
- Docker và Docker Compose.
- Triển khai Cloud.
- Thanh toán trực tuyến.
- Email/SMS.
- Dashboard thống kê.
- Phân quyền chi tiết.
- Audit log.
- Notification.
- Báo cáo doanh thu.
- Quản lý lịch bảo trì.

---

# KẾT LUẬN

Đề tài **Xây dựng hệ thống quản lý tòa nhà và phòng cho thuê** được thực hiện nhằm áp dụng kiến thức cơ sở dữ liệu vào một bài toán thực tế.

Đề tài bắt đầu từ việc khảo sát và phân tích nghiệp vụ, xác định các thực thể, thuộc tính, khóa chính, khóa ngoại và mối quan hệ. Từ đó xây dựng cơ sở dữ liệu MySQL gồm 11 bảng chính.

Sau khi hoàn thiện CSDL, hệ thống được phát triển bằng Spring Boot theo kiến trúc Controller - Service - Repository. Spring Data JPA được sử dụng để ánh xạ và truy vấn dữ liệu. Các chức năng CRUD, tìm kiếm và thống kê được cung cấp thông qua REST API.

Bên cạnh phần cơ sở dữ liệu, project tích hợp Spring Security và JWT để xác thực, phân quyền, đồng thời sử dụng Redis để cache các dữ liệu phù hợp. Postman được sử dụng để kiểm thử API.

Toàn bộ đề tài kết hợp:

```text
Cơ sở dữ liệu
      ↓
SQL / MySQL
      ↓
Java
      ↓
Spring Boot
      ↓
JPA / Repository
      ↓
Service
      ↓
REST API
      ↓
Spring Security / JWT
      ↓
Redis
      ↓
Postman
```

Đề tài có thể tiếp tục được phát triển thành một hệ thống hoàn chỉnh hơn thông qua việc bổ sung frontend, triển khai thực tế, thanh toán trực tuyến, thông báo tự động và dashboard thống kê.

---

# PHỤ LỤC: DANH SÁCH HÌNH ẢNH NÊN ĐƯA VÀO BÁO CÁO

## Chương 1
- Hình giới thiệu đề tài nếu cần.

## Chương 2
- Sơ đồ quy trình nghiệp vụ.
- Sơ đồ các đối tượng quản lý.

## Chương 3
- Sơ đồ ERD.
- Danh sách database.
- Danh sách bảng.
- Cấu trúc từng bảng.
- SQL tạo bảng.
- Kết quả `SHOW TABLES`.
- Kết quả `DESC`.
- Dữ liệu mẫu.

## Chương 4
- Cây thư mục Spring Boot.
- `pom.xml`.
- `application.yaml`.
- Entity.
- Repository.
- Service.
- Controller.
- Security.
- JWT.
- Redis.
- API Postman.
- Response JSON.

## Chương 5
- Kết quả POST.
- Kết quả GET.
- Kết quả PUT.
- Kết quả DELETE.
- Kết quả tìm kiếm.
- Kết quả thống kê.
- Kết quả xác thực JWT.
- Kết quả kiểm tra Redis.

---

# TÓM TẮT CẤU TRÚC BÀI TẬP LỚN

```text
CHƯƠNG 1 - GIỚI THIỆU ĐỀ TÀI
│
├── 1.1 Lý do chọn đề tài
├── 1.2 Mục tiêu
├── 1.3 Đối tượng và phạm vi
├── 1.4 Phương pháp
├── 1.5 Công nghệ
└── 1.6 Kết quả dự kiến
        │
        ↓
CHƯƠNG 2 - KHẢO SÁT VÀ PHÂN TÍCH
│
├── 2.1 Khảo sát
├── 2.2 Mô tả bài toán
├── 2.3 Đối tượng quản lý
├── 2.4 Nghiệp vụ
├── 2.5 Yêu cầu chức năng
├── 2.6 Yêu cầu phi chức năng
└── 2.7 Quy trình
        │
        ↓
CHƯƠNG 3 - PHÂN TÍCH VÀ THIẾT KẾ CSDL
│
├── 3.1 Thực thể
├── 3.2 Thuộc tính
├── 3.3 PK / FK
├── 3.4 Quan hệ
├── 3.5 SQL
└── 3.6 Kiểm tra CSDL
        │
        ↓
CHƯƠNG 4 - XÂY DỰNG SPRING BOOT
│
├── 4.1 Công nghệ
├── 4.2 Kiến trúc
├── 4.3 Cấu hình
├── 4.4 Entity
├── 4.5 Repository / Query
├── 4.6 Service
├── 4.7 Controller / REST API
├── 4.8 Chức năng quản lý
├── 4.9 Search / Statistics
├── 4.10 Security / JWT
├── 4.11 Redis
├── 4.12 Postman
└── 4.13 Kết quả
        │
        ↓
CHƯƠNG 5 - KIỂM THỬ VÀ ĐÁNH GIÁ
│
├── 5.1 Môi trường
├── 5.2 Kiểm thử CSDL
├── 5.3 CRUD
├── 5.4 Query
├── 5.5 Security / JWT
├── 5.6 Redis
├── 5.7 Đánh giá
├── 5.8 Hạn chế
└── 5.9 Hướng phát triển
        │
        ↓
KẾT LUẬN
```
