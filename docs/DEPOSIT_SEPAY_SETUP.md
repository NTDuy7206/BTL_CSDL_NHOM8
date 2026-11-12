# Đặt cọc qua chuyển khoản ngân hàng

Tài liệu lưu lịch sử của luồng cọc trước đây. Bản hiện tại duyệt yêu cầu thuê
trực tiếp, lập hóa đơn đến kỳ và xác nhận thanh toán giả lập. Phần cấu hình
SePay bên dưới áp dụng cho bản cũ.

Phần tích hợp đã chuẩn bị cho SePay Webhooks. Chưa có tài khoản SePay kết nối
ngân hàng thì hệ thống chưa thể tự xác nhận giao dịch tiền thật. Người thuê có
thể báo mã giao dịch trên hóa đơn cọc để chủ nhà đối chiếu và xác nhận đã nhận.

## Kết nối cho từng chủ nhà

1. Chủ nhà đăng ký SePay tại https://my.sepay.vn và tự kết nối tài khoản ngân hàng của mình.
2. Trong HomeBase, mở Thông tin cá nhân → Ngân hàng nhận tiền cọc.
   Nhập tên ngân hàng đúng như trường gateway của SePay (ví dụ MBBank), số tài khoản,
   tên chủ tài khoản. Có thể tải thêm ảnh QR ngân hàng.
3. Tạo khóa webhook, sao chép khóa trước khi lưu. Hệ thống chỉ lưu SHA-256 của khóa.
   Không đưa khóa vào Git, ảnh chụp hoặc tài khoản người thuê.
4. Trong SePay tạo webhook cho sự kiện tiền vào của đúng tài khoản ngân hàng.
   Dán URL hiển thị trong hồ sơ, chọn xác thực API Key và nhập khóa vừa sao chép.
   Header dịch vụ gửi phải có dạng `Authorization: Apikey <khóa>`.
5. Kiểm chứng cấu hình bằng giao dịch thực tế có sự chủ động của chủ tài khoản.
   Mã người nhận và số tiền phải được đối chiếu trong ứng dụng ngân hàng trước khi chuyển.

Tài liệu nguồn:
https://docs.sepay.vn/tich-hop-webhooks.html
https://docs.sepay.vn/tao-qr-code-vietqr-dong.html

## Luồng thuê mới

Yêu cầu mới ghi số cọc cố định bằng giá thuê một tháng tại thời điểm gửi.
Chủ nhà xem chi tiết, chấp thuận yêu cầu cọc. Hệ thống chốt ngân hàng nhận tiền;
người thuê nhận thông báo trong ứng dụng và thấy QR động trong mục Hóa đơn đặt cọc.
Sau khi chuyển khoản, người thuê nhập mã trên biên lai và bấm Tôi đã chuyển khoản.
Chủ nhà đối chiếu tiền vào tài khoản rồi bấm Đã nhận cọc. Báo chuyển khoản chưa
chuyển hóa đơn sang trạng thái đã nhận tiền.

Webhook hợp lệ phải đồng thời đúng khóa của chủ nhà, tên ngân hàng, số tài khoản,
giao dịch tiền vào, mã cọc và số tiền chính xác. Mỗi ID giao dịch chỉ xử lý một lần.
Giao dịch thiếu tiền, thừa tiền, sai mã hoặc sai người nhận được ghi nhận để đối soát,
không được tự coi là đã thanh toán.

Sau khi trạng thái cọc là PAID, chủ nhà hoàn tất duyệt để tạo hợp đồng, gắn đúng
khách và phòng thuộc chủ nhà đó. Số cọc được đưa vào contracts.deposit.
Các yêu cầu cũ trước khi có tính năng này không bị áp thêm nghĩa vụ cọc hồi tố.

Không thu hai khoản cọc cho hai yêu cầu chồng lịch đã được chấp thuận tại cùng phòng.
Chủ nhà có thể từ chối yêu cầu chưa nhận cọc để giải phóng lịch.
Yêu cầu đã nhận cọc không được hủy qua nút từ chối; phải đối soát/hoàn tiền thực tế.
Phần mềm chưa thực hiện hoàn tiền tự động và chưa có cổng tự động thu tiền hóa đơn.

Tiền cọc được lưu tách khỏi doanh thu tiền thuê, không tự cộng vào lợi nhuận.
Lịch sử thu/chi tháng trước tính theo ngày thực nhận/thực trả; bộ lọc tháng lấy đủ
đầu và cuối tháng, kể cả tháng 2 năm nhuận.
