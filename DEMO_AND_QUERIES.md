# Dữ liệu mẫu và nút tra cứu

Khi chạy bản mới, ứng dụng tự thêm dữ liệu vào MySQL đang được cấu hình. Trên Railway, dữ liệu nằm trong MySQL Railway; khi chạy local, dữ liệu nằm trong MySQL local. DBeaver cần kết nối đúng máy chủ và database để xem cùng dữ liệu.

## Bộ dữ liệu bài tập lớn

| Bảng | Số bản ghi mẫu | Ví dụ mã |
|---|---:|---|
| rooms | 15 (10 đã thuê, 5 trống) | DEMO_P01 |
| tenants | 10 | DEMO_KT01 |
| employees | 10 | DEMO_NV01 |
| contracts | 11 (10 đang hạn, 1 hết hạn) | DEMO_HD01, DEMO_HD_OLD |
| invoices | 10 | DEMO_HDon01 |
| utility_readings | 10 | DEMO_CS01 |
| rules | 10 | DEMO_NQ01 |
| violations | 10 | DEMO_VP01 |
| incidents | 10 | DEMO_SC01 |
| incident_resolutions | 10 | DEMO_XL01 |
| cleaning_supports | 10 | DEMO_DD01 |

Tổng cộng 116 bản ghi giả lập, có liên kết khóa ngoại. Tên, địa chỉ và CCCD trong bộ mẫu là dữ liệu giả để trình diễn. Không tạo tài khoản hay mật khẩu dùng chung.

Ứng dụng chỉ nạp bộ mẫu một lần, đánh dấu trong `demo_seed_history`. Khởi động lại không ghi đè nội dung đã sửa và không khôi phục các bản ghi bạn đã xóa. Có thể tắt lần nạp tự động bằng biến môi trường `APP_DEMO_SEED_ENABLED=false`. Nguồn dữ liệu nằm trong `src/main/resources/demo-data.sql`.

## Sử dụng trên giao diện

Đăng nhập với vai trò chủ nhà để thấy các bảng nghiệp vụ và các chức năng quản lý. Mỗi màn hình có vùng **Tra cứu nhanh**; bấm nút, nhập điều kiện nếu cần, rồi bấm **Tra cứu**. Kết quả hiện ở bảng phía dưới.

| Màn hình | Các nút ngoài Xem tất cả và Tìm theo mã |
|---|---|
| Khách thuê | Tìm khách theo tên |
| Nhân viên | Tìm nhân viên theo tên |
| Phòng | Theo tình trạng, Theo loại phòng, Giá thuê tối đa |
| Hợp đồng | Hợp đồng hết hạn, Hợp đồng theo khách, Hợp đồng theo phòng |
| Hóa đơn | Hóa đơn theo hợp đồng, Tổng giá trị hóa đơn |
| Sự cố | Sự cố theo phòng, Theo trạng thái |
| Xử lý sự cố | Theo sự cố, Theo nhân viên, Chưa hoàn thành, Đánh dấu hoàn thành |
| Hỗ trợ dọn dẹp | Theo phòng, Theo nhân viên, Chưa hoàn thành, Đánh dấu hoàn thành |
| Chỉ số điện nước | Chỉ số theo phòng |
| Vi phạm | Vi phạm theo phòng |
| Nội quy | Xem tất cả, Tìm theo mã |
| Tài khoản (chủ nhà) | Xem danh sách tài khoản |

Các nút thêm, sửa, xóa trong bảng và các màn hình khách đăng ký hợp đồng, chủ nhà duyệt yêu cầu thuê, báo cáo sự cố, thanh toán hóa đơn vẫn sử dụng API tương ứng. Tổng giá trị hóa đơn là tổng số tiền trên hóa đơn, không phải số tiền đã thực thu. Thanh toán hiện là ghi nhận/báo thanh toán trong hệ thống, chưa tích hợp cổng ngân hàng.

Khách thuê chỉ thấy các chức năng được phân quyền. Hợp đồng và hóa đơn chỉ hiển thị hồ sơ của chính tài khoản đăng nhập. Trong **Đăng ký hợp đồng**, chọn **Thuê của chủ nhà** rồi chọn phòng còn trống của người đó. Yêu cầu thuê chỉ gửi đến chủ nhà sở hữu phòng; khi được duyệt sẽ dùng chính phòng có sẵn.

## Số phòng cố định theo chủ nhà

Khi đăng ký ADMIN, nhập thêm **Số phòng cho thuê cố định** (1–200). Hệ thống tạo đúng số phòng này, ban đầu là UNCONFIGURED. Vào **Phòng / mặt bằng**, sửa diện tích, loại phòng, giá thuê và trạng thái AVAILABLE để mở đăng ký thuê. Mã phòng được tạo riêng cho từng chủ nhà.

Chủ nhà đã có tài khoản trước bản cập nhật: vào **Phòng / mặt bằng** để thiết lập một lần. Nhập tổng số phòng, chọn các phòng cũ thực sự thuộc về mình. Hệ thống giữ nguyên các phòng được chọn, chỉ tạo phòng chờ cấu hình cho số lượng còn thiếu. Phòng đã được liên kết với chủ nhà khác không thể nhận lại. Không tự đoán ai sở hữu các phòng cũ.

Sau khi xác nhận, số phòng cố định. Giao diện cho sửa thông tin từng phòng; API thêm/xóa phòng trực tiếp bị chặn. Việc đăng ký thuê, tạo khách thuê hoặc duyệt hợp đồng không tạo thêm phòng. API cũ POST /api/demo/my-rental trả 410 và không thay đổi dữ liệu. Những phòng/hợp đồng mẫu cá nhân đã được tạo trước bản sửa vẫn được giữ nguyên để tránh mất dữ liệu; chủ nhà có thể xác định lại quyền sở hữu ở bước thiết lập phòng cũ.

Danh sách phòng, hợp đồng, hóa đơn và yêu cầu thuê của chủ nhà được lọc theo phòng thuộc về họ. Các phân hệ dùng chung khác như nhân viên và nội quy vẫn theo phân quyền của dự án hiện tại.

## Kiểm tra dành cho người phát triển

Các truy vấn lọc dùng `GET /api/queries/{module}/{action}?value=...`. Nút hoàn thành dùng `PUT /api/incident-resolutions/{id}/complete` hoặc `PUT /api/cleaning-supports/{id}/complete`. Danh sách chủ nhà/phòng dùng GET /api/landlords và GET /api/landlords/rooms. Thiết lập một lần dùng POST /api/landlords/property (ADMIN).

Chạy `scripts/test-query-workflow.mjs` với Node khi ứng dụng thử chạy tại `http://127.0.0.1:18082`. Script chỉ cho phép địa chỉ local, tạo tài khoản kiểm thử riêng và kiểm tra số phòng cố định, quyền sở hữu, lựa chọn chủ nhà và đăng ký thuê dùng phòng có sẵn. Dùng database thử riêng; script có tạo và cập nhật bản ghi.

Để thấy thay đổi trên trang công khai, Railway phải triển khai commit mới trên nhánh `main`. Việc cập nhật mã nguồn trên máy không tự thay thế bản đang chạy nếu Railway chưa nhận commit.

Kiểm tra logic tồn kho phòng bằng `./mvnw -Dtest=LandlordInventoryTests test` (Windows dùng mvnw.cmd). Bộ kiểm tra dùng H2 trong bộ nhớ cho giao dịch/khóa ngoại và mô phỏng repository JPA; không thay thế kiểm tra toàn bộ luồng trên MySQL.

Bản sửa quyền thuê: chủ nhà không còn được tạo hợp đồng thay khách, chỉ duyệt yêu cầu do khách gửi. Tổng quan khách lấy phòng đang thuê từ hợp đồng riêng của tài khoản; danh sách phòng có thanh cuộn. Phương thức báo thanh toán gồm BANK_TRANSFER và CASH; khách chọn tại Thanh toán hoặc bấm nút Thanh toán trong Hóa đơn của tôi.
