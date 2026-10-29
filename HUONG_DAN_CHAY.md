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

`compose.minio.yml` cung cấp cấu hình chạy MinIO cục bộ cho ảnh đại diện. Schema nghiệp vụ có tại `src/main/resources/server-schema.sql`; quy trình phân quyền và hợp đồng mới xem [ROLE_WORKFLOWS.md](ROLE_WORKFLOWS.md).

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

Khi chạy local, nạp `src/main/resources/server-schema.sql` vào database trước. `schema.sql` bổ sung các bảng hồ sơ, yêu cầu thuê, thanh toán và liên kết tài khoản khi ứng dụng khởi động. Profile `prod` tự chạy cả hai file SQL; Hibernate dùng `validate` để kiểm tra cấu trúc sau khi SQL được thực thi. Các script chỉ tạo bảng nếu chưa có, không nhập dữ liệu cá nhân từ máy phát triển.

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

Ứng dụng tạo các bảng phụ `user_profiles`, `contract_requests`, `payment_reports`, `tenant_accounts` và `rental_request_details` từ `src/main/resources/schema.sql` khi khởi động. Schema nghiệp vụ chính (phòng, hợp đồng, hóa đơn, khách thuê…) vẫn phải được nạp trước. Ảnh hồ sơ hỗ trợ JPG, PNG và WEBP, tối đa 5 MB. Nếu không dùng MinIO thì phần hồ sơ văn bản vẫn dùng được; chức năng tải ảnh cần dịch vụ MinIO chạy ở cổng 9000.

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

Sau khi schema được nạp và backend đã chạy, mở giao diện, chọn **Tạo tài khoản**, nhập username/password và chọn loại tài khoản:
- **Khách thuê:** được xem dữ liệu, gửi yêu cầu thuê phòng và báo thông tin thanh toán; không được sửa dữ liệu quản lý.
- **Chủ nhà (ADMIN):** quản lý toàn bộ dữ liệu, duyệt hợp đồng và xử lý sự cố.

Đăng ký thành công sẽ tự đăng nhập. Chủ nhà đang chờ duyệt được đưa tới màn hình thông báo trạng thái.

Ví dụ body đăng ký khách thuê qua API:

```json
{
  "username": "demo",
  "password": "Demo@1234",
  "role": "USER"
}
```

Đăng ký chủ nhà dùng `"role": "ADMIN"` và `"roomCount": 10` (thay 10 bằng số phòng thực tế, từ 1 đến 200). Số phòng được khai báo một lần; sửa thông tin từng phòng tại Phòng / mặt bằng để mở cho thuê. Chỉ có hai lựa chọn USER và ADMIN. Ví dụ trên là dữ liệu mẫu, không phải tài khoản có sẵn.

Sau khi đăng ký, dùng username và mật khẩu vừa chọn để đăng nhập:

| Username | Password | Vai trò khi đăng ký |
|---|---|---|
| Username đã đăng ký | Mật khẩu đã đặt | `USER` (khách thuê) hoặc `ADMIN` (chủ nhà) |

ADMIN quản lý dữ liệu; USER chỉ xem các phân hệ được cấp, báo sự cố tại phòng mình đang thuê, gửi đăng ký hợp đồng và báo thanh toán hóa đơn của mình. Chủ nhà duyệt yêu cầu sẽ tự tạo hợp đồng. Biểu mẫu **Thêm khách & hợp đồng** nhập hồ sơ, chọn tài khoản và phòng trong một lần lưu. Thanh toán hiện là đối soát chuyển khoản thủ công, chưa kết nối cổng ngân hàng. Sau khi nâng cấp vai trò, đăng xuất và đăng nhập lại. Xem hướng dẫn chi tiết tại [ROLE_WORKFLOWS.md](ROLE_WORKFLOWS.md).

Các tài khoản ADMIN dùng chung dữ liệu của dự án; chưa phân tách thành nhiều tòa nhà độc lập. USER chỉ đọc hợp đồng/hóa đơn qua liên kết `tenant_accounts`, không tự gán dữ liệu cũ chỉ theo tên giống nhau.

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

Các API ngoài `/api/auth/**` yêu cầu xác thực. USER được xem nhân viên, phòng, sự cố, nội quy, vi phạm; xem hợp đồng/hóa đơn của mình; POST báo sự cố, đăng ký hợp đồng và báo thanh toán. Các thao tác quản lý còn lại dành cho ADMIN. API trả 403 khi không đủ quyền và 401 khi chưa xác thực.

## 8. Gọi API và truy vấn dữ liệu

Các controller chính cung cấp API cho tenant, employee, room, contract, invoice, incident, incident resolution, cleaning support, rule, utility reading và violation. Xem các lớp `*Controller` trong `src/main/java/com/ntd/csdl/controller` để biết chính xác URL, tham số và JSON của từng endpoint.

Quy trình thử API trong Postman:

1. Gọi `/api/auth/login` và lấy token.
2. Gửi token Bearer cho các API cần đăng nhập.
3. Gọi `GET` để đọc danh sách/chi tiết; quyền `POST`, `PUT`, `PATCH`, `DELETE` được giới hạn theo vai trò. Khách thuê gửi đề nghị tại `POST /api/contract-requests` và báo thanh toán tại `POST /api/payment-reports`.
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

### Thuê phòng và thanh toán

Khách dùng Đăng ký thuê để gửi yêu cầu của chính tài khoản mình; chủ nhà chỉ duyệt yêu cầu. Đã bỏ tạo hợp đồng thay khách. Trong Hóa đơn của tôi, bấm Thanh toán và chọn chuyển khoản hoặc tiền mặt, nhập thông tin giao dịch rồi gửi để chủ nhà xác nhận. Chưa tích hợp cổng thu tiền tự động.
