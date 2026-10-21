# Hướng dẫn cài đặt và chạy dự án

Dự án: Hệ thống quản lý tòa nhà và phòng cho thuê (Spring Boot + MySQL + Redis)
Mã nguồn: https://github.com/NTDuy7206/BTL_CSDL_NHOM8

## 1. Yêu cầu môi trường

- Git.
- JDK 26 (phiên bản khai báo trong `pom.xml`).
- MySQL đang chạy và truy cập được tại `localhost:3306`.
- Redis đang chạy tại `localhost:6379` nếu sử dụng các chức năng cache.
- Postman để gọi API.

Repo hiện không kèm `docker-compose.yml` hoặc script SQL khởi tạo CSDL. Docker là tùy chọn để chạy MySQL/Redis; nếu dùng Docker, cần publish các cổng 3306 và 6379 ra máy host tương ứng.

## 2. Clone mã nguồn

Mở PowerShell tại thư mục muốn lưu dự án:

```powershell
git clone https://github.com/NTDuy7206/BTL_CSDL_NHOM8.git
cd BTL_CSDL_NHOM8
```

## 3. Tạo và chuẩn bị database

Tạo database tên `btl_csdl` trong MySQL:

```sql
CREATE DATABASE btl_csdl CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Nạp schema tương ứng của nhóm vào database trước khi chạy ứng dụng. Cấu hình `spring.jpa.hibernate.ddl-auto: validate` chỉ xác nhận bảng/cột có khớp với Entity; ứng dụng không tự tạo schema. Cần có đủ các bảng nghiệp vụ và bảng `user` để đăng ký/đăng nhập. Repo hiện không chứa file SQL schema/seed, vì vậy hãy lấy file SQL từ nhóm hoặc xuất schema từ database đang dùng.

## 4. Cấu hình kết nối trên máy cá nhân

`application.yaml` mặc định kết nối tới database `btl_csdl` bằng `root` và mật khẩu rỗng. Nếu MySQL của bạn dùng thông tin khác, đặt biến môi trường trong cùng cửa sổ PowerShell trước khi chạy:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/btl_csdl"
$env:SPRING_DATASOURCE_USERNAME="root"
$env:SPRING_DATASOURCE_PASSWORD="<mật-khẩu-MySQL-của-bạn>"
$env:SPRING_DATA_REDIS_HOST="localhost"
$env:SPRING_DATA_REDIS_PORT="6379"
```

Thay `root` hoặc mật khẩu theo cấu hình MySQL của bạn. Không đưa mật khẩu MySQL thật lên GitHub.

## 5. Khởi động ứng dụng

Trong thư mục dự án, chạy:

```powershell
java -version
.\mvnw.cmd spring-boot:run
```

Lần chạy đầu Maven có thể cần Internet để tải dependencies. Khi log báo ứng dụng đã khởi động trên cổng `8080`, API có địa chỉ gốc `http://localhost:8080`. Để dừng, nhấn `Ctrl+C` trong cửa sổ đang chạy ứng dụng.

## 6. Tạo tài khoản đăng nhập mẫu

**Tài khoản bên dưới chưa được tạo sẵn trong database.** Sau khi schema đã được nạp và ứng dụng đang chạy, đăng ký một lần qua Postman:

- Method: `POST`
- URL: `http://localhost:8080/api/auth/register`
- Header: `Content-Type: application/json`
- Body → raw → JSON:

```json
{
  "username": "demo",
  "password": "Demo@1234"
}
```

Sau khi đăng ký thành công, dùng thông tin này để đăng nhập:

| Username | Password | Vai trò khi đăng ký |
|---|---|---|
| `demo` | `Demo@1234` | `USER` |

Nếu username `demo` đã tồn tại trong database, không đăng ký lại; hãy dùng mật khẩu đã đặt cho tài khoản đó hoặc chọn username khác và cập nhật cặp thông tin trong bảng này. Endpoint đăng ký hiện tự gán vai trò `USER`, không tạo tài khoản `ADMIN`.

### Đăng nhập và lấy JWT

- Method: `POST`
- URL: `http://localhost:8080/api/auth/login`
- Header: `Content-Type: application/json`
- Body:

```json
{
  "username": "demo",
  "password": "Demo@1234"
}
```

Response đăng nhập có JWT. Trong Postman, mở Authorization → chọn Bearer Token → dán giá trị token. Hoặc thêm header:

```text
Authorization: Bearer <JWT-nhận-được-từ-login>
```

Các API ngoài `/api/auth/**` yêu cầu xác thực theo cấu hình Spring Security. Để gọi API admin, tài khoản cần có authority `ADMIN`; tài khoản đăng ký theo hướng dẫn trên chỉ có vai trò `USER`.

## 7. Gọi API và truy vấn dữ liệu

Các controller chính cung cấp API cho tenant, employee, room, contract, invoice, incident, incident resolution, cleaning support, rule, utility reading và violation. Xem các lớp `*Controller` trong `src/main/java/com/ntd/csdl/controller` để biết chính xác URL, tham số và JSON của từng endpoint.

Quy trình thử API trong Postman:

1. Gọi `/api/auth/login` và lấy token.
2. Gửi token Bearer cho các API cần đăng nhập.
3. Gọi `GET` để đọc danh sách/chi tiết; dùng `POST`, `PUT` hoặc `DELETE` theo endpoint được khai báo trong controller.
4. Các câu SQL báo cáo có thể chạy trong MySQL Workbench/DBeaver trên database `btl_csdl`; cần import dữ liệu mẫu trước nếu muốn truy vấn có kết quả.

## 8. Một số lỗi thường gặp

- **Không kết nối được MySQL:** kiểm tra MySQL/container đang chạy, cổng 3306 đã mở, tên database và thông tin đăng nhập.
- **Schema-validation báo thiếu bảng/cột:** schema chưa được import hoặc không khớp các Entity trong phiên bản mã nguồn.
- **Không đăng ký/đăng nhập được:** kiểm tra bảng `user` đã tồn tại, username chưa trùng và body JSON đúng tên trường `username`, `password`.
- **API trả 401:** đăng nhập lại để lấy JWT mới; kiểm tra header `Authorization: Bearer ...` và token còn hạn.
- **Không kết nối được Redis:** bật Redis tại `localhost:6379` nếu chức năng đang gọi cache.
- **Port 8080 bị sử dụng:** tắt tiến trình khác hoặc đổi `server.port` trong cấu hình.

## 9. Lưu ý về tài khoản và dữ liệu mẫu

Tài khoản trong database MySQL của máy người phát triển không tự đi theo mã nguồn khi người khác clone repo. Mỗi thành viên cần nạp schema/dữ liệu riêng và đăng ký tài khoản của mình. Không lưu mật khẩu thật hoặc token JWT vào repo công khai.
