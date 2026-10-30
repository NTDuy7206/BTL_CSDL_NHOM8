# Ảnh minh họa phòng

Ảnh mẫu do công cụ image_gen tích hợp tạo, được lưu tại
`src/main/resources/sample-rooms/studio.png`.

Ảnh không phải ảnh chụp căn phòng thực tế. Giao diện hiển thị nhãn
“Ảnh minh họa”. Ảnh thật do chủ nhà tải lên được ưu tiên trước ảnh mẫu.

Khi bản này khởi động lần đầu, các phòng đã gắn với chủ nhà nhưng chưa
có ảnh được thêm một ảnh JPEG và thumbnail vào bảng `room_photos`.
Phòng đã có ảnh được giữ nguyên. Quá trình được đánh dấu trong
`demo_seed_history`; ảnh đã bị chủ nhà xóa sẽ không tự xuất hiện lại khi khởi động.

Prompt tạo ảnh:

> Create a photorealistic landscape interior photograph for a Vietnamese rental apartment demo website, aspect ratio 3:2. A modest attractive 25 square meter studio, white walls, light oak floor, neatly made double bed in cream linen, small bedside table, compact kitchenette with white cabinets, small desk and chair, one large window with soft natural daylight and sheer curtains, one indoor plant. Realistic and affordable, not luxurious, clean uncluttered room, natural camera perspective, no people, no words, no logos, no watermark. This is illustrative sample property imagery, not a real listing.
