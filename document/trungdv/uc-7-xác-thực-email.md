---
type: Note
_organized: true
---
# UC-7 Xác thực Email

### Use Case ID: UC-7

### Use Case Name: Xác thực Email

### Description:

- Use case này mô tả cách Customer xác thực email gắn với tài khoản của mình; sau khi xác thực thành công, hệ thống cập nhật trạng thái tài khoản là đã xác thực email.

### Actor(s):

- Chính: Customer

### Priority: MUST

- Xác thực email là chức năng bắt buộc để hệ thống ghi nhận email thuộc về Customer sở hữu tài khoản.

### Trigger

1. UC-1 chuyển Customer sang UC-7 sau khi đăng ký tài khoản bằng email thành công.
2. Customer chọn xác thực email từ hộp thoại nhắc xác thực email sau khi đăng nhập bằng email ở UC-2.

### Pre-Conditions

1. Customer có phiên đăng nhập hợp lệ.
2. Tài khoản Customer có email chưa xác thực.

### Post-Conditions

1. Hệ thống ghi nhận kết quả xác thực OTP thành công từ UC-6.
2. Email của tài khoản Customer được đánh dấu đã xác thực.

### Normal Flow

**Customer xác thực email thành công.**

7.1.1 Hệ thống — Hiển thị màn hình xác thực email.
7.1.2 Hệ thống — Thực hiện UC-6 để xác thực OTP.
7.1.3 UC-6 — Trả kết quả xác thực OTP thành công.
7.1.4 Hệ thống — Đánh dấu email của tài khoản Customer là đã xác thực.
7.1.5 Hệ thống — Thông báo xác thực email thành công.

### Alternative Flow

**Không có Alternative Flow riêng biệt cho UC-7.**

### Exception Flow

**EF1: Xác thực OTP thất bại**

Tại bước 7.1.2 của Normal Flow, UC-6 trả kết quả xác thực OTP thất bại.

7.2.1 Hệ thống — Không cập nhật trạng thái xác thực email.
7.2.2 Hệ thống — Kết thúc thất bại.

**EF2: Lỗi hệ thống**

Tại bước 7.1.4 của Normal Flow, hệ thống không thể cập nhật trạng thái xác thực email do lỗi hệ thống.

7.3.1 Hệ thống — Giữ email của tài khoản Customer ở trạng thái chưa xác thực.
7.3.2 Hệ thống — Hiển thị thông báo lỗi: "Đã xảy ra lỗi. Vui lòng thử lại sau."
7.3.3 Hệ thống — Kết thúc thất bại.

### Business Rules

1. UC-7 include UC-6 để xác thực OTP.
2. UC-7 chỉ cập nhật trạng thái email sau khi UC-6 trả kết quả xác thực OTP thành công.
3. Nếu Customer bỏ qua xác thực email, hệ thống vẫn giữ email ở trạng thái chưa xác thực.

### Non-Functional Requirements

1. Hệ thống không được cập nhật trạng thái email đã xác thực nếu UC-6 trả kết quả xác thực OTP thất bại.
2. Trạng thái xác thực email phải được lưu nhất quán với tài khoản Customer.

