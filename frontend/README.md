# Frontend HomeBase

Giao diện quản lý xây dựng bằng React và Vite. Các component giao diện nằm trong `src/components`, các trang trong `src/pages`, định nghĩa phân hệ/API trong `src/data/modules.js`.

## Chạy khi phát triển

Cần Node.js 22.12 trở lên (hoặc bản 20.19 trở lên). Mở hai cửa sổ terminal:

1. Ở thư mục dự án Spring Boot, bật backend và các dịch vụ MySQL/Redis theo `HUONG_DAN_CHAY.md`.
2. Trong cửa sổ terminal khác:

```powershell
cd frontend
npm install
npm run dev
```

Mở địa chỉ Vite in ra (mặc định `http://localhost:5173`). Vite tự chuyển tiếp các request `/api` đến Spring Boot tại `http://localhost:8080`.

## Đóng gói để Spring Boot phục vụ giao diện

```powershell
cd frontend
npm install
npm run build
```

Vite đặt trang và các tệp đã tối ưu vào `src/main/resources/static`. Sau đó chạy Spring Boot và mở `http://localhost:8080`.
