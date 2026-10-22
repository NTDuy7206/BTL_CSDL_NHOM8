# Hướng dẫn cài đặt và chạy dự án

Dự án: Hệ thống quản lý tòa nhà và phòng cho thuê (Spring Boot + MySQL + Redis + MinIO)
Mã nguồn: https://github.com/NTDuy7206/BTL_CSDL_NHOM8

## 1. Yêu cầu môi trường

- Git.
- JDK 26 (phiên bản khai báo trong `pom.xml`).
- MySQL đang chạy và truy cập được tại `localhost:3306`.
- Redis đang chạy tại `localhost:6379` nếu sử dụng các chức năng cache.
- Docker Desktop nếu muốn chạy MinIO bằng cấu hình có sẵn.
- Postman để gọi API.

`compose.minio.yml` cung cấp cấu hình chạy MinIO cục bộ cho ảnh đại diện. Dự án chưa cung cấp script schema nghiệp vụ cho MySQL; cần nạp schema của nhóm như mô tả bên dưới.

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

## 5. Khởi động MinIO

Trong thư mục dự án, chạy lệnh sau khi Docker Desktop đã khởi động:

```powershell
docker compose -f compose.minio.yml up -d --build
```

Lần đầu chạy, Docker dựng MinIO Community từ mã nguồn chính thức; thao tác này cần Internet và có thể mất vài phút. Cách này tránh phụ thuộc vào ảnh dựng sẵn từ registry đã trả lỗi `401 Unauthorized`. MinIO cung cấp API lưu trữ tại `http://localhost:9000` và trang quản trị tại `http://localhost:9001`. Cấu hình phát triển mặc định là `minioadmin` / `minioadmin`. Có thể đổi thông tin này bằng biến môi trường `MINIO_ROOT_USER` và `MINIO_ROOT_PASSWORD`; nếu đổi, đặt thêm `MINIO_ACCESS_KEY` và `MINIO_SECRET_KEY` tương ứng trước khi chạy Spring Boot.

Ứng dụng tạo bảng `user_profiles` từ `src/main/resources/schema.sql` khi khởi động. Ảnh hồ sơ hỗ trợ JPG, PNG và WEBP, tối đa 5 MB. Nếu không dùng MinIO thì phần hồ sơ văn bản vẫn dùng được; chức năng tải ảnh cần dịch vụ MinIO chạy ở cổng 9000.

## 6. Khởi động ứng dụng

Trong thư mục dự án, chạy:

```powershell
java -version
.\mvnw.cmd spring-boot:run
```

Lần chạy đầu Maven có thể cần Internet để tải dependencies. Khi log báo ứng dụng đã khởi động trên cổng `8080`, API có địa chỉ gốc `http://localhost:8080`. Để dừng, nhấn `Ctrl+C` trong cửa sổ đang chạy ứng dụng.

Để chạy frontend trong lúc phát triển, mở một cửa sổ PowerShell khác:

```powershell
cd frontend
npm install
npm run dev
```

Mở `http://localhost:5173`. Vite chuyển tiếp các request `/api` tới Spring Boot ở cổng 8080; frontend và backend cần cùng chạy. Để đóng gói giao diện vào Spring Boot, trong thư mục `frontend` chạy `npm run build`, sau đó mở `http://localhost:8080`.

## 7. Tạo tài khoản và sử dụng giao diện

Sau khi schema được nạp và backend đã chạy, mở giao diện rồi chọn **Đăng ký** để tạo tài khoản:
- Username: `demo`
- Password: `Demo@1234`

Đây là thông tin mẫu để đăng ký lần đầu, **không phải tài khoản seed có sẵn**. Sau khi đăng ký thành công, giao diện tự đăng nhập và chuyển tới dashboard. Tài khoản mới được cấp vai trò `USER`.

```json
{
  "username": "demo",
  "password": "Demo@1234"
}
```

Sau khi đăng ký, dùng thông tin mẫu ở trên để đăng nhập:

| Username | Password | Vai trò khi đăng ký |
|---|---|---|
| `demo` | `Demo@1234` | `USER` |

Chọn các phân hệ ở thanh bên để xem, tìm kiếm, thêm, sửa, xóa và xuất dữ liệu CSV. Mục **Thông tin cá nhân** cho phép cập nhật tên hiển thị, email, số điện thoại và ảnh đại diện. Giao diện gửi JWT tự động nên không cần Postman cho thao tác thông thường. Nếu username `demo` đã tồn tại, hãy dùng mật khẩu đã đặt hoặc chọn username khác. Endpoint đăng ký chỉ tự gán vai trò `USER`, không tạo tài khoản `ADMIN`.

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

## 8. Gọi API và truy vấn dữ liệu

Các controller chính cung cấp API cho tenant, employee, room, contract, invoice, incident, incident resolution, cleaning support, rule, utility reading và violation. Xem các lớp `*Controller` trong `src/main/java/com/ntd/csdl/controller` để biết chính xác URL, tham số và JSON của từng endpoint.

Quy trình thử API trong Postman:

1. Gọi `/api/auth/login` và lấy token.
2. Gửi token Bearer cho các API cần đăng nhập.
3. Gọi `GET` để đọc danh sách/chi tiết; dùng `POST`, `PUT` hoặc `DELETE` theo endpoint được khai báo trong controller.
4. Các câu SQL báo cáo có thể chạy trong MySQL Workbench/DBeaver trên database `btl_csdl`; cần import dữ liệu mẫu trước nếu muốn truy vấn có kết quả.

## 9. Một số lỗi thường gặp

- **Không kết nối được MySQL:** kiểm tra MySQL/container đang chạy, cổng 3306 đã mở, tên database và thông tin đăng nhập.
- **Schema-validation báo thiếu bảng/cột:** schema chưa được import hoặc không khớp các Entity trong phiên bản mã nguồn.
- **Không đăng ký/đăng nhập được:** kiểm tra bảng `user` đã tồn tại, username chưa trùng và body JSON đúng tên trường `username`, `password`.
- **API trả 401:** đăng nhập lại để lấy JWT mới; kiểm tra header `Authorization: Bearer ...` và token còn hạn.
- **Không kết nối được Redis:** bật Redis tại `localhost:6379` nếu chức năng đang gọi cache.
- **Không tải được ảnh đại diện:** kiểm tra container MinIO đang chạy, cổng 9000 được publish và thông tin `MINIO_ACCESS_KEY` / `MINIO_SECRET_KEY` khớp với MinIO.
- **Port 8080 bị sử dụng:** tắt tiến trình khác hoặc đổi `server.port` trong cấu hình.

## 10. Lưu ý về tài khoản và dữ liệu mẫu

Tài khoản trong database MySQL của máy người phát triển không tự đi theo mã nguồn khi người khác clone repo. Mỗi thành viên cần nạp schema/dữ liệu riêng và đăng ký tài khoản của mình. Không lưu mật khẩu thật hoặc token JWT vào repo công khai.

## 11. Đưa ứng dụng lên server công khai

Để người khác truy cập qua Internet, xem hướng dẫn triển khai VPS tại `DEPLOY.md`. Tệp này mô tả cách chạy Spring Boot, MySQL, Redis, MinIO và HTTPS bằng Docker Compose. Bản CSDL trên server được khởi tạo rỗng, không sao chép tài khoản hay dữ liệu cá nhân từ máy phát triển.

### Triển khai bằng Railway

Nếu bạn chọn Railway, xem hướng dẫn chi tiết trong DEPLOY_RAILWAY.md. Railway chạy app, MySQL, Redis và MinIO dưới dạng các dịch vụ riêng; file Docker Compose dành cho VPS không được Railway chạy trực tiếp.
