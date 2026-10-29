# Khách thuê và chủ nhà

Trang đăng ký có hai lựa chọn: **Khách thuê (USER)** và **Chủ nhà (ADMIN)**. Vai trò được lưu trong MySQL và kiểm tra lại ở API; ẩn nút trên giao diện không thay thế kiểm tra quyền.

| Chức năng | USER – khách thuê | ADMIN – chủ nhà |
|---|---|---|
| Nhân viên, nội quy, vi phạm, danh sách sự cố | Xem | Thêm, sửa, xóa |
| Phòng | Xem chủ sở hữu ghi trên mỗi phòng; chọn trực tiếp phòng muốn thuê | Khai báo danh sách phòng một lần, chỉ quản lý phòng và khách thuê thuộc mình |
| Báo cáo sự cố | Báo sự cố tại phòng có hợp đồng đang hiệu lực của mình | Quản lý, cập nhật và xử lý |
| Hợp đồng | Gửi đăng ký; chỉ xem hợp đồng được liên kết với tài khoản mình | Tạo, duyệt, quản lý theo phòng thuộc mình |
| Hóa đơn | Chỉ xem hóa đơn thuộc hợp đồng của mình | Quản lý theo phòng thuộc mình |
| Thanh toán | Gửi mã chuyển khoản cho hóa đơn của mình | Đối soát, xác nhận đã thu hoặc từ chối |
| Khách thuê, chỉ số điện nước, xử lý sự cố, dọn dẹp | Không truy cập API quản lý | Quản lý |
| Thông tin cá nhân | Xem và cập nhật hồ sơ của mình | Xem và cập nhật hồ sơ của mình |

Chủ nhà mới khai báo từng phòng (mã, tầng, diện tích, loại và giá) khi đăng ký. Chủ nhà đã có tài khoản vào Phòng / mặt bằng để thiết lập số phòng một lần và liên kết các phòng cũ thực sự thuộc về mình. Xem DEMO_AND_QUERIES.md để biết cách cấu hình. Khách thuê chọn phòng có ghi rõ tên chủ nhà; gửi yêu cầu và duyệt hợp đồng không tạo phòng mới.

## Khách gửi yêu cầu, chủ nhà duyệt

Khách đăng nhập bằng tài khoản của mình, vào Đăng ký thuê, nhập hồ sơ, chọn phòng có ghi rõ chủ nhà. Yêu cầu được gắn với tài khoản đang đăng nhập. Chủ nhà chỉ xem và duyệt yêu cầu thuộc phòng của mình. Duyệt yêu cầu tự tạo hợp đồng cho đúng khách gửi yêu cầu.

Đã bỏ menu Thêm khách & hợp đồng và API tạo hợp đồng trực tiếp. POST /api/rentals và POST /api/contracts trả 403. Không cho đổi khách thuê hoặc phòng bằng cách sửa hợp đồng; cần gửi yêu cầu thuê mới.


## Khách đăng ký và thanh toán

Khách vào **Đăng ký thuê**, nhập hồ sơ, chọn phòng và thời hạn. Chủ nhà nhận yêu cầu trong **Yêu cầu thuê**. Khi chọn **Duyệt & tạo hợp đồng**, hệ thống tạo hợp đồng và chuyển phòng sang đã thuê. Hai yêu cầu không thể cùng được duyệt để thuê một phòng. Hợp đồng từ yêu cầu thuê có tiền cọc ban đầu là 0; chủ nhà cập nhật theo thỏa thuận thực tế.

Khách xem **Hợp đồng của tôi**, **Hóa đơn của tôi** và **Thanh toán**. Thanh toán hiện là chuyển khoản bên ngoài rồi gửi mã giao dịch để chủ nhà xác nhận, chưa tích hợp cổng ngân hàng tự động. Không đánh dấu đã thanh toán chỉ vì khách gửi báo cáo.

## Dữ liệu và triển khai

`tenant_accounts` liên kết một tài khoản USER với một hồ sơ khách thuê. `rental_request_details` lưu khách thuê, thời hạn và hợp đồng được tạo từ yêu cầu. Không tự suy đoán liên kết cho dữ liệu cũ chỉ dựa vào tên giống nhau. Yêu cầu cũ thiếu hồ sơ cần được gửi lại.

`src/main/resources/schema.sql` tạo các bảng bổ sung nếu chưa có, đồng thời chuyển vai trò cũ OWNER thành ADMIN và TENANT/OWNER_PENDING thành USER. Giữ nguyên USER=0, ADMIN=1 trong dữ liệu hiện có. Sau nâng cấp cần đăng xuất rồi đăng nhập lại để làm mới giao diện theo quyền.

Chạy kiểm thử trên **database riêng**, không chạy trên dữ liệu thật: khởi động ứng dụng tại cổng 18082 với profile prod và MySQL thử nghiệm, sau đó chạy `node scripts/test-rental-workflow.mjs`. Script chỉ nhận địa chỉ localhost, tạo tài khoản và bản ghi thử để kiểm tra phân quyền, duyệt hợp đồng, quyền sở hữu hóa đơn, báo sự cố, thanh toán và hoàn tác giao dịch.

## Phòng và thanh toán của khách

Tổng quan của USER hiển thị Phòng tôi đang thuê từ hợp đồng đang hiệu lực của tài khoản đó. Danh sách phòng trống là danh sách tham khảo chung; các danh sách phòng có vùng cuộn và hiển thị toàn bộ kết quả.

Tại Hóa đơn của tôi, bấm Thanh toán để chọn hóa đơn tương ứng. Khách chọn BANK_TRANSFER (chuyển khoản) hoặc CASH (tiền mặt), gửi mã giao dịch/thông tin giao tiền. Chủ nhà xác nhận đã nhận tiền thì báo cáo chuyển PAID. Đây là đối soát thủ công, chưa tích hợp cổng thu tiền trực tuyến.

## Hoàn thành công việc

Trong Sự cố, Xử lý sự cố và Hỗ trợ dọn dẹp, danh sách mặc định là Chờ xử lý. Chủ nhà bấm Đã xử lý xong trên từng dòng để hoàn thành; chỉ được hoàn thành việc thuộc phòng của mình. Dòng đã xong rời danh sách chờ và có thể xem tại Đã xong. Dữ liệu lịch sử vẫn được lưu trong MySQL. Hoàn thành sự cố cũng đánh dấu các bản ghi xử lý liên quan là đã hoàn thành.

## Nhận/trả phòng và tự tính hóa đơn

Xem [HUONG_DAN_THUE_VA_HOA_DON.md](HUONG_DAN_THUE_VA_HOA_DON.md) để dùng check-in/check-out, chốt giá thuê và nhập chỉ số điện nước. Hóa đơn đến hạn được tính tự động khi đủ dữ liệu và thông báo trong tài khoản khách thuê.

## Điện/nước và lợi nhuận của chủ nhà
ADMIN vào Thanh toán để tổng hợp điện/nước của khách theo tháng và ghi nhận số thực trả cho nhà cung cấp sau khi đã trả bên ngoài ứng dụng. Thu tiền khách vẫn được xác nhận ở phần dưới, có ngày thực thu. Vào Lợi nhuận để xem thu/chi và lợi nhuận tạm tính theo ngày, tháng, năm; bổ sung chi phí khác và ngày thực thu còn thiếu của dữ liệu cũ. USER không được truy cập sổ chi trả hay lợi nhuận của chủ nhà.
