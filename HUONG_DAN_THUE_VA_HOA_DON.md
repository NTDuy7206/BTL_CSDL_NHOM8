# Nhận/trả phòng thực tế và hóa đơn cuối tháng

## Chủ nhà sử dụng

1. Mở Hợp đồng. Với hợp đồng cũ chưa chốt giá, bấm **Xác nhận giá thuê** và nhập giá đã thỏa thuận theo tháng. Giá chốt này dùng để tính hóa đơn, không lấy giá phòng có thể thay đổi sau này. Hợp đồng mới được duyệt đã lưu giá tại lúc duyệt.
2. Khi nhận phòng, bấm **Check-in**, nhập ngày nhận và chỉ số công tơ điện (kWh), nước (m³). Cho phép ngày sớm hơn ngày bắt đầu hợp đồng nếu không trùng người thuê khác. Không nhập ngày trong tương lai.
3. Khi trả phòng, bấm **Check-out**, nhập ngày trả và chỉ số cuối. Có thể trả sớm hoặc ghi trả muộn nếu không chồng lấn hợp đồng khác. Phòng được cập nhật ngay khi xác nhận. Chỉ số trả không được nhỏ hơn chỉ số nhận.
4. Nhập một bản **Chỉ số điện nước** cho mỗi phòng, mỗi tháng/năm: chỉ số đầu/cuối điện và nước, đơn giá điện (₫/kWh), đơn giá nước (₫/m³). Không tạo hai bản cùng kỳ. Lược đồ gốc đang dùng chỉ số nguyên (INT), nên giao diện hiện nhập công tơ theo số nguyên.
5. Xem khung **Tự động tính hóa đơn cuối tháng**. Nếu thiếu ngày/check-in, chỉ số, đơn giá hoặc giá thuê, hệ thống báo hợp đồng và tháng cần bổ sung. Có nút kiểm tra lại các kỳ đến hạn.

Ngày nhận/trả thực tế được lưu riêng; ngày trong hợp đồng vẫn giữ để đối chiếu. Ngày trả phòng không tính tiền thuê: nhận ngày 10, trả ngày 20 là 10 ngày. Nếu chưa trả, ngày kết thúc hợp đồng được tính hết ngày đó.

Phòng có hợp đồng hết hạn được trả về AVAILABLE sau ngày kết thúc; kiểm tra mỗi phút. Hệ thống không đánh trống nếu còn hợp đồng đang hiệu lực khác hoặc thiếu ngày cần xác minh. Trạng thái bảo trì/chưa cấu hình được giữ nguyên. Phòng đã đặt cho tương lai vẫn được kiểm tra thời hạn khi gửi yêu cầu/duyệt, nên không tạo hai hợp đồng trùng khoảng thời gian.

## Tính tiền

| Khoản | Cách tính |
|---|---|
| Tiền thuê | Giá thuê thỏa thuận × số ngày ở thực tế trong tháng ÷ số ngày của tháng |
| Tiền điện | (Chỉ số cuối − chỉ số đầu) kWh × đơn giá ₫/kWh |
| Tiền nước | (Chỉ số cuối − chỉ số đầu) m³ × đơn giá ₫/m³ |
| Tổng | Tiền thuê + tiền điện + tiền nước + tiền phạt đã ghi nhận trên hóa đơn |

Tiền được làm tròn 2 chữ số thập phân theo HALF_UP, phù hợp cột DECIMAL(18,2) hiện tại. 1 m³ nước = 1 khối. Tự động tạo mới chưa cộng tiền phạt: chủ nhà ghi nhận khoản phạt theo nghiệp vụ riêng. Tiền phạt có sẵn trên hóa đơn được giữ khi bổ sung điện/nước.

Khi đổi khách giữa tháng, chỉ số nhận/trả riêng của mỗi hợp đồng xác định mức dùng của từng người; không chia đều hoặc tự ước lượng. Thiếu chỉ số ở ranh giới thì chưa lập hóa đơn tự động cho hợp đồng đó.

Hóa đơn đã lập thủ công có khoản điện/nước hoặc đang/đã đối soát thanh toán được giữ nguyên, không tạo thêm hóa đơn cùng kỳ. Nếu hóa đơn thủ công chỉ có tiền thuê và chưa thanh toán, hệ thống có thể bổ sung điện/nước từ chỉ số hợp lệ, giữ tiền thuê đã nhập. Ngày thực tế sửa sau khi đã có hóa đơn sẽ sinh cảnh báo để chủ nhà đối soát; không tự sửa hóa đơn đã thu tiền.

## Lịch chạy và người thuê nhận hóa đơn

Từ 23:55 ngày cuối tháng, giờ Asia/Ho_Chi_Minh, hệ thống tính các kỳ đến hạn. Kiểm tra mỗi phút; thiếu chỉ số sẽ tự thử lại khi bổ sung. Nếu máy chủ tạm ngừng, khi chạy lại hệ thống tiếp tục các kỳ chưa xử lý kể từ tháng bắt đầu bật tính năng. Không tự tính hàng loạt hóa đơn lịch sử trước tháng bật tính năng.

Hóa đơn được lưu trong MySQL và gắn đúng hợp đồng/khách thuê. Khách nhận thông báo trong ứng dụng và xem tại **Hóa đơn của tôi**. Giao diện kiểm tra thông báo mỗi 60 giây. Đây không phải gửi email/SMS hoặc tự trừ tiền ngân hàng. Thanh toán vẫn theo chuyển khoản/tiền mặt và chủ nhà xác nhận.

Tác vụ cần ứng dụng đang chạy. Có thể tắt bằng APP_RENTAL_JOBS_ENABLED=false. Dữ liệu đánh dấu automatic_bills giúp chạy lại không tạo trùng; các tác vụ đồng thời khóa hợp đồng/hóa đơn trước khi ghi.

## Giao diện và kiểm tra

Các bảng đã bỏ chia trang, hiển thị toàn bộ kết quả trong khung cuộn, có tiêu đề cột cố định. Cỡ chữ tăng nhẹ và có đơn vị tại trường nhập, cột công tơ và giá.

Kiểm tra logic thuê/phí: mvnw.cmd -Dtest=StayBillingTests,TenantPrivacyPaymentTests,LandlordInventoryTests,IncidentCompletionTests test. Các kiểm tra dùng H2 trong bộ nhớ và repository mô phỏng ở các luồng quyền; cần kiểm chứng thêm trên MySQL/Railway sau triển khai. Kiểm tra giao diện dữ liệu: node scripts/test-room-groups.mjs và node scripts/test-completion.mjs.
