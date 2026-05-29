---
_organized: true
---
# UC-2 Đăng nhập bằng Email

### Use Case ID: UC-2

### Use Case Name: Đăng nhập bằng email

### Description:

- Use case này cho phép Guest hoặc Admin đăng nhập bằng email và mật khẩu.
- Sau khi đăng nhập thành công, hệ thống tạo phiên đăng nhập theo role và điều hướng actor đến khu vực phù hợp.

### Actor(s):

- Chính: Guest, Admin
- Phụ: Không có

### Priority: MUST

- Đây là phương thức đăng nhập cơ bản cho Customer và là phương thức đăng nhập bắt buộc của Admin.

### Trigger

1. Guest hoặc Admin chọn chức năng "Đăng nhập" trên giao diện hệ thống.
2. Guest hoặc Admin truy cập chức năng yêu cầu đăng nhập và chọn đăng nhập bằng email.

### Pre-Conditions

- Guest hoặc Admin chưa có phiên đăng nhập hợp lệ tương ứng với role của mình.

### Post-Conditions

1. Hệ thống tạo phiên đăng nhập hợp lệ theo role của tài khoản.
2. Nếu tài khoản Customer có email chưa xác thực, hệ thống hiển thị nhắc xác minh email.
3. Actor được điều hướng đến trang phù hợp sau đăng nhập.

### Normal Flow

**Guest/Admin đăng nhập bằng email và mật khẩu thành công.**

2.1.1 Guest/Admin — Chọn chức năng "Đăng nhập" trên giao diện hệ thống.
2.1.2 Hệ thống — Hiển thị form đăng nhập với các trường email và mật khẩu.
2.1.3 Guest/Admin — Nhập email, mật khẩu và gửi yêu cầu đăng nhập.
2.1.4 Hệ thống — Kiểm tra thông tin đăng nhập.
2.1.5 Hệ thống — Xác thực thông tin đăng nhập với tài khoản hợp lệ.
2.1.6 Hệ thống — Xác nhận tài khoản là Admin hoặc Customer có email đã xác thực.
2.1.7 Hệ thống — Tạo phiên đăng nhập theo role của tài khoản.
2.1.8 Hệ thống — Xác định trang điều hướng sau đăng nhập theo role và chức năng yêu cầu đăng nhập nếu có.
2.1.9 Hệ thống — Điều hướng Guest/Admin đến trang điều hướng sau đăng nhập.

### Alternative Flow

**AF1: Customer chưa xác thực email**

Tại bước 2.1.6 của Normal Flow, hệ thống phát hiện tài khoản Customer có email chưa xác thực.

2.2.1 Hệ thống — Hiển thị hộp thoại nhắc Customer xác thực email.
2.2.2 Customer — Chọn tiếp tục đăng nhập.
2.2.3 Hệ thống — Quay lại bước 2.1.7 của Normal Flow.

### Exception Flow

**EF1: Dữ liệu không hợp lệ**

Tại bước 2.1.4 của Normal Flow, hệ thống phát hiện email hoặc mật khẩu bị bỏ trống, hoặc email không đúng định dạng.

2.3.1 Hệ thống — Không tạo phiên đăng nhập mới.
2.3.2 Hệ thống — Hiển thị thông báo lỗi tương ứng với thông tin chưa hợp lệ.
2.3.3 Guest/Admin — Chỉnh sửa thông tin đăng nhập và gửi lại.
2.3.4 Hệ thống — Quay lại bước 2.1.4 của Normal Flow.

**EF2: Email hoặc mật khẩu không đúng**

Tại bước 2.1.5 của Normal Flow, hệ thống phát hiện không có tài khoản nào khớp với email và mật khẩu đã nhập.

2.4.1 Hệ thống — Không tạo phiên đăng nhập mới.
2.4.2 Hệ thống — Hiển thị thông báo lỗi chung: "Email hoặc mật khẩu không đúng."
2.4.3 Guest/Admin — Chỉnh sửa thông tin đăng nhập và gửi lại.
2.4.4 Hệ thống — Quay lại bước 2.1.4 của Normal Flow.

**EF3: Lỗi hệ thống**

Tại bước 2.1.4, 2.1.5, 2.1.6, 2.1.7 hoặc 2.1.8 của Normal Flow, hệ thống không thể hoàn tất đăng nhập do lỗi xử lý nội bộ.

2.5.1 Hệ thống — Không tạo phiên đăng nhập mới.
2.5.2 Hệ thống — Hiển thị thông báo lỗi: "Đã xảy ra lỗi. Vui lòng thử lại sau."
2.5.3 Hệ thống — Kết thúc thất bại.

### Business Rules

1. UC-2 áp dụng cho tài khoản Customer hoặc Admin có phương thức đăng nhập bằng email và mật khẩu.
2. Admin account được tạo sẵn, không được tự đăng ký và không đăng nhập bằng Google.
3. Email của Admin được xem là đã xác thực hoặc không yêu cầu nhắc xác minh email.
4. Email được trim khoảng trắng dư và chuyển về lowercase trước khi xác thực.
5. Thông báo lỗi đăng nhập không được tiết lộ email có tồn tại trong hệ thống hay không.
6. Trang điều hướng sau đăng nhập được xác định theo role và ngữ cảnh đăng nhập: nếu Guest/Admin bắt đầu từ chức năng yêu cầu đăng nhập phù hợp với role của tài khoản, hệ thống điều hướng về chức năng đó; nếu không, hệ thống điều hướng đến trang mặc định theo role.
7. Nếu Customer đăng nhập bằng email với email chưa xác thực, hệ thống hiển thị hộp thoại nhắc xác thực email nhưng vẫn cho phép Customer tiếp tục đăng nhập.
8. Hộp thoại nhắc xác thực email cho phép Customer chuyển sang UC-7 để xác thực email hoặc tiếp tục đăng nhập.
9. Đăng nhập bằng email ở UC-2 chỉ cấp quyền theo role của tài khoản đã xác thực.

### Non-Functional Requirements

1. Mật khẩu không được ghi log hoặc trả về phía client.
2. Dữ liệu đăng nhập phải được truyền qua kết nối mã hóa (HTTPS/TLS).
3. Hệ thống cần bảo vệ chức năng đăng nhập khỏi brute force bằng giới hạn tốc độ hoặc cơ chế tương đương.
