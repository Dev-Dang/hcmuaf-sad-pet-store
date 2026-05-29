---
_organized: true
---
# UC-1 Đăng ký tài khoản bằng Email

### Use Case ID: UC-1

### Use Case Name: Đăng ký tài khoản bằng email

### Description:

- Use case này cho phép Guest tạo tài khoản Customer bằng email và mật khẩu.
- Sau khi đăng ký thành công, hệ thống tạo phiên đăng nhập Customer và chuyển Customer sang yêu cầu xác thực email.

### Actor(s):

- Chính: Guest
- Phụ: Không có

### Priority: MUST

- Đây là entry point bắt buộc để Guest tạo tài khoản Customer bằng email và mật khẩu.

### Trigger

- Guest chủ động chọn chức năng "Đăng ký" trên giao diện hệ thống.

### Pre-Conditions

1. Guest chưa có phiên đăng nhập Customer hợp lệ trên trình duyệt hiện tại.
2. Hệ thống đang cho phép đăng ký tài khoản Customer bằng email và mật khẩu.

### Post-Conditions

1. Hệ thống tạo tài khoản Customer mới bằng email và mật khẩu.
2. Tài khoản Customer mới có trạng thái email chưa xác thực.
3. Hệ thống tạo phiên đăng nhập Customer cho tài khoản mới.
4. Customer được chuyển sang UC-7 để xác thực email.

### Normal Flow

**Guest đăng ký tài khoản bằng email và mật khẩu thành công.**

1.1.1 Guest — Chọn chức năng "Đăng ký" trên giao diện hệ thống.\
1.1.2 Hệ thống — Hiển thị form đăng ký với các trường bắt buộc: Họ tên, email và mật khẩu.\
1.1.3 Guest — Nhập đầy đủ họ tên, email, mật khẩu và gửi yêu cầu đăng ký.\
1.1.4 Hệ thống — Kiểm tra thông tin đăng ký hợp lệ.\
1.1.5 Hệ thống — Kiểm tra email chưa tồn tại trong hệ thống.\
1.1.6 Hệ thống — Tạo tài khoản Customer mới với mật khẩu được lưu dưới dạng đã mã hoá.\
1.1.7 Hệ thống — Tạo phiên đăng nhập Customer cho tài khoản mới.\
1.1.8 Hệ thống — Chuyển Customer sang UC-7 để xác thực email.

### Alternative Flow

**Không có Alternative Flow riêng biệt cho UC-1.**

### Exception Flow

**EF1: Email đã tồn tại trong hệ thống**

Tại bước 1.1.5 của Normal Flow, hệ thống phát hiện email đã được đăng ký bởi tài khoản khác.

1.2.1 Hệ thống — Không tạo tài khoản Customer mới.\
1.2.2 Hệ thống — Hiển thị thông báo lỗi: "Email này đã được sử dụng. Vui lòng dùng email khác hoặc đăng nhập."\
1.2.3 Guest — Nhập email khác và gửi lại yêu cầu đăng ký.\
1.2.4 Hệ thống — Quay lại bước 1.1.4 của Normal Flow.

**EF2: Thông tin đăng ký không hợp lệ**

Tại bước 1.1.4 của Normal Flow, hệ thống phát hiện thông tin đăng ký không hợp lệ.

1.3.1 Hệ thống — Không tạo tài khoản Customer mới.\
1.3.2 Hệ thống — Hiển thị thông báo lỗi tương ứng với thông tin chưa hợp lệ.\
1.3.3 Guest — Sửa thông tin đăng ký và gửi lại yêu cầu đăng ký.\
1.3.4 Hệ thống — Quay lại bước 1.1.4 của Normal Flow.

**EF3: Lỗi hệ thống**

Tại bước 1.1.6 hoặc 1.1.7 của Normal Flow, hệ thống không thể tạo tài khoản Customer hoặc phiên đăng nhập do lỗi xử lý nội bộ.

1.4.1 Hệ thống — Không tạo tài khoản Customer hoặc phiên đăng nhập mới.\
1.4.2 Hệ thống — Hiển thị thông báo lỗi: "Đã xảy ra lỗi. Vui lòng thử lại sau."\
1.4.3 Hệ thống — Kết thúc thất bại.

### Business Rules

1. Khi đăng ký bằng email, các thông tin bắt buộc gồm email, mật khẩu và họ tên.
2. Mật khẩu của Customer phải có tối thiểu 8 ký tự, gồm ít nhất 1 chữ cái và 1 chữ số.
3. Email là định danh duy nhất của tài khoản Customer trong phạm vi Customer account.
4. Trước khi lưu và so sánh, email phải được trim khoảng trắng và chuyển về lowercase.
5. Nếu email đã thuộc tài khoản Customer có phương thức đăng nhập Google, UC-1 không tạo tài khoản Customer mới.
6. Sau khi đăng ký bằng email thành công, hệ thống chuyển Customer sang UC-7 để yêu cầu xác thực email.
7. Email verified phục vụ mục đích đặt lại mật khẩu, không phải điều kiện bắt buộc để đăng nhập hoặc đặt hàng.
8. Nếu hệ thống không thể hoàn tất đăng ký, hệ thống không được ghi nhận tài khoản Customer hoặc phiên đăng nhập ở trạng thái tạo dở.

### Non-Functional Requirements

1. Mật khẩu được lưu dưới dạng đã mã hoá.
2. Dữ liệu form đăng ký phải được truyền qua kết nối mã hóa (HTTPS/TLS).
3. Customer chỉ được truy cập dữ liệu tài khoản của chính mình sau khi tạo tài khoản thành công.
