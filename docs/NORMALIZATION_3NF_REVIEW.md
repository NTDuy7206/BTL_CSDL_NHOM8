# Rà soát 3NF của schema dự án

Trạng thái: rà soát thiết kế; chưa chuyển toàn bộ bảng của DB đang chạy sang schema mới. Không được coi việc sửa khóa ngoại hay thêm UNIQUE là chứng minh 3NF.

Nghiệp vụ đã chốt: tên vi phạm, đơn giá và tiền thuê giữ lịch sử tại thời điểm lập. Vì vậy rule_id không quyết định tên vi phạm lịch sử và room_id không quyết định tiền thuê lịch sử.

| Bảng | Khóa chính hiện tại | Phụ thuộc cơ sở |
|---|---|---|
| employees | employee_id | Khóa → các thuộc tính của bản ghi |
| rooms | room_id | Khóa → các thuộc tính của bản ghi |
| rules | rule_id | Khóa → các thuộc tính của bản ghi |
| tenants | tenant_id | Khóa → các thuộc tính của bản ghi |
| user | id | Khóa → các thuộc tính của bản ghi |
| incidents | incident_id | Khóa → các thuộc tính của bản ghi |
| contracts | contract_id | Khóa → các thuộc tính của bản ghi |
| cleaning_supports | support_id | Khóa → các thuộc tính của bản ghi |
| incident_resolutions | resolution_id | Khóa → các thuộc tính của bản ghi |
| invoices | invoice_id | Khóa → các thuộc tính của bản ghi |
| user_profiles | user_id | Khóa → các thuộc tính của bản ghi |
| utility_readings | reading_id | Khóa → các thuộc tính của bản ghi |
| violations | violation_id | Khóa → các thuộc tính của bản ghi |
| user_profiles | user_id | Khóa → các thuộc tính của bản ghi |
| contract_requests | request_id | Khóa → các thuộc tính của bản ghi |
| payment_reports | payment_report_id | Khóa → các thuộc tính của bản ghi |
| demo_seed_history | seed_key | Khóa → các thuộc tính của bản ghi |
| tenant_accounts | user_id | Khóa → các thuộc tính của bản ghi |
| rental_request_details | request_id | Khóa → các thuộc tính của bản ghi |
| landlord_properties | user_id | Khóa → các thuộc tính của bản ghi |
| room_landlords | room_id | Khóa → các thuộc tính của bản ghi |
| contract_stays | contract_id | Khóa → các thuộc tính của bản ghi |
| billing_start | id | Khóa → các thuộc tính của bản ghi |
| automatic_bills | contract_id,year,month | Khóa → các thuộc tính của bản ghi |
| billing_alerts | contract_id,year,month | Khóa → các thuộc tính của bản ghi |
| invoice_notifications | invoice_id | Khóa → các thuộc tính của bản ghi |
| tenant_landlord_selection | user_id | Khóa → các thuộc tính của bản ghi |
| tenant_landlords | tenant_id,user_id | Khóa → các thuộc tính của bản ghi |
| finance_receipts | payment_report_id | Khóa → các thuộc tính của bản ghi |
| utility_disbursements | id | Khóa → các thuộc tính của bản ghi |
| landlord_expenses | id | Khóa → các thuộc tính của bản ghi |
| room_listings | room_id | Khóa → các thuộc tính của bản ghi |
| room_photos | id | Khóa → các thuộc tính của bản ghi |
| payment_report_routes | payment_report_id | Khóa → các thuộc tính của bản ghi |

## Các điểm phải xử lý trước khi xác nhận toàn bộ schema đạt 3NF

1. `invoices.total_amount` tính từ các thành phần tiền: cần bỏ khỏi dữ liệu cơ sở và tính qua biểu thức/view; đồng bộ JPA, truy vấn JDBC, dữ liệu mẫu và báo cáo thanh toán. Giữ nguyên các thành phần tiền lịch sử.
2. `automatic_bills` đang lặp hợp đồng/kỳ với hóa đơn. Nên lưu duy nhất invoice_id hoặc xác lập rõ invoice_id là khóa ứng viên, kiểm tra dữ liệu cũ trước khi thêm UNIQUE.
3. `finance_receipts`, `payment_reports`, `payment_report_routes`: kiểm tra khoản thực thu và chủ nhà nhận tiền là dữ kiện độc lập của chứng từ hay bản sao từ báo thanh toán. Không tự thay thế chứng từ lịch sử bằng thông tin hiện tại.
4. `room_landlords`, `tenant_landlords`, `tenant_accounts`: giữ các quan hệ riêng. Không đưa tên người thuê, số điện thoại hoặc tên chủ nhà vào bảng phòng/hợp đồng để làm bộ lọc.
5. CCCD nullable UNIQUE không phải khóa ứng viên bắt buộc cho toàn bộ quan hệ. Không lấy CCCD → các thuộc tính khác làm FD vô điều kiện khi còn NULL.
6. Phải sao lưu, chạy migration trên MySQL thử nghiệm, đối chiếu số dòng, tổng tiền và quyền truy cập trước khi áp dụng DB công khai. Docker/MySQL thử nghiệm hiện chưa chạy được; chưa thực hiện các thay đổi phá cấu trúc nêu trên.
