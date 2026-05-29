---
type: Note
_organized: true
---
# UC-4 Đặt lại mật khẩu

### Use Case ID: UC-4

### Use Case Name: Đặt lại mật khẩu

### Description:

- Use case này cho phép Customer đặt lại mật khẩu cho tài khoản email đã đăng ký khi quên mật khẩu.
- Sau khi xác thực OTP thành công và mật khẩu mới hợp lệ, hệ thống cập nhật mật khẩu của tài khoản Customer và yêu cầu Customer đăng nhập lại bằng mật khẩu mới.

### Actor(s):

- Chính: Customer
- Phụ: Không có

### Priority: MUST

- Đây là chức năng bắt buộc để Customer khôi phục quyền truy cập tài khoản khi quên mật khẩu.

### Trigger

- Customer chọn chức năng "Quên mật khẩu" trên màn hình đăng nhập.

### Pre-Conditions

1. Customer chưa có phiên đăng nhập hợp lệ.
2. Hệ thống cho phép đặt lại mật khẩu bằng email.

### Post-Conditions

#### Thành công

1. Hệ thống ghi nhận kết quả xác thực OTP thành công từ UC-6.
2. Mật khẩu của tài khoản Customer được cập nhật.
3. Nếu email của Customer chưa được xác thực trước đó, hệ thống đánh dấu email là đã xác thực.
4. Không có phiên đăng nhập mới được tạo; Customer được yêu cầu đăng nhập bằng mật khẩu mới.

#### Thất bại

1. Mật khẩu của tài khoản Customer không được cập nhật.
2. Trạng thái xác thực email của Customer không thay đổi.

### Normal Flow

**Customer đặt lại mật khẩu thành công bằng email đã xác thực.**

4.1.1 Customer — Chọn chức năng "Quên mật khẩu" trên màn hình đăng nhập.
4.1.2 Hệ thống — Hiển thị form nhập email đặt lại mật khẩu.
4.1.3 Customer — Nhập email và gửi yêu cầu đặt lại mật khẩu.
4.1.4 Hệ thống — Kiểm tra định dạng email.
4.1.5 Hệ thống — Hiển thị thông báo: "Nếu email đã được đăng ký, OTP đặt lại mật khẩu sẽ được gửi đến email này."
4.1.6 Hệ thống — Tìm tài khoản Customer theo email.
4.1.7 Hệ thống — Xác nhận tài khoản có phương thức đăng nhập bằng email và mật khẩu.
4.1.8 Hệ thống — Xác nhận email của tài khoản Customer đã được xác thực.
4.1.9 Hệ thống — Thực hiện UC-6 để xác thực OTP cho yêu cầu đặt lại mật khẩu.
4.1.10 UC-6 — Trả kết quả xác thực OTP thành công.
4.1.11 Hệ thống — Hiển thị form nhập mật khẩu mới.
4.1.12 Customer — Nhập mật khẩu mới và gửi yêu cầu cập nhật mật khẩu.
4.1.13 Hệ thống — Kiểm tra mật khẩu mới.
4.1.14 Hệ thống — Cập nhật mật khẩu của tài khoản Customer.
4.1.15 Hệ thống — Thông báo đặt lại mật khẩu thành công.
4.1.16 Hệ thống — Yêu cầu Customer đăng nhập bằng mật khẩu mới.

### Alternative Flow

**AF1: Email chưa xác thực**

Tại bước 4.1.8 của Normal Flow, hệ thống phát hiện email của tài khoản Customer chưa được xác thực.

4.2.1 Hệ thống — Thực hiện UC-6 để xác thực OTP cho yêu cầu đặt lại mật khẩu.
4.2.2 UC-6 — Trả kết quả xác thực OTP thành công.
4.2.3 Hệ thống — Đánh dấu email của tài khoản Customer là đã xác thực.
4.2.4 Hệ thống — Quay lại bước 4.1.11 của Normal Flow.

### Exception Flow

**EF1: Email không hợp lệ**

Tại bước 4.1.4 của Normal Flow, hệ thống phát hiện email bị bỏ trống hoặc không đúng định dạng.

4.3.1 Hệ thống — Hiển thị thông báo lỗi tương ứng.
4.3.2 Customer — Chỉnh sửa email và gửi lại yêu cầu đặt lại mật khẩu.
4.3.3 Hệ thống — Quay lại bước 4.1.4 của Normal Flow.

**EF2: Không thể đặt lại mật khẩu với email đã nhập**

Tại bước 4.1.6 hoặc 4.1.7 của Normal Flow, hệ thống không tìm thấy tài khoản Customer nào khớp với email đã nhập hoặc phát hiện tài khoản Customer không có phương thức đăng nhập bằng email và mật khẩu.

4.4.1 Hệ thống — Không gửi OTP đặt lại mật khẩu.
4.4.2 Hệ thống — Kết thúc thất bại.

**EF3: Xác thực OTP thất bại**

Tại bước 4.1.9 của Normal Flow hoặc bước 4.2.1 của AF1, UC-6 trả kết quả xác thực OTP thất bại.

4.5.1 Hệ thống — Không cập nhật mật khẩu của tài khoản Customer.
4.5.2 Hệ thống — Không cập nhật trạng thái xác thực email.
4.5.3 Hệ thống — Kết thúc thất bại.

**EF4: Mật khẩu mới không hợp lệ**

Tại bước 4.1.13 của Normal Flow, hệ thống phát hiện mật khẩu mới bị bỏ trống hoặc không đáp ứng chính sách mật khẩu.

4.6.1 Hệ thống — Hiển thị thông báo lỗi tương ứng.
4.6.2 Customer — Chỉnh sửa mật khẩu mới và gửi lại yêu cầu cập nhật mật khẩu.
4.6.3 Hệ thống — Quay lại bước 4.1.13 của Normal Flow.

**EF5: Lỗi hệ thống**

Tại bước 4.1.6, 4.1.7, 4.1.8, 4.1.9, 4.1.13 hoặc 4.1.14 của Normal Flow, hoặc bước 4.2.1, 4.2.3 của AF1, hệ thống không thể hoàn tất đặt lại mật khẩu do lỗi hệ thống.

4.7.1 Hệ thống — Không cập nhật mật khẩu của tài khoản Customer.
4.7.2 Hệ thống — Không cập nhật trạng thái xác thực email.
4.7.3 Hệ thống — Hiển thị thông báo lỗi: "Đã xảy ra lỗi. Vui lòng thử lại sau."
4.7.4 Hệ thống — Kết thúc thất bại.

### Business Rules

1. UC-4 chỉ áp dụng cho tài khoản Customer có phương thức đăng nhập bằng email và mật khẩu.
2. UC-4 include UC-6 để xác thực OTP cho yêu cầu đặt lại mật khẩu.
3. UC-4 không gọi UC-7; nếu email của Customer chưa được xác thực, OTP đặt lại mật khẩu thành công đồng thời xác thực email.
4. Hệ thống chỉ cập nhật mật khẩu sau khi UC-6 trả kết quả xác thực OTP thành công và mật khẩu mới đáp ứng chính sách mật khẩu.
5. Sau khi đặt lại mật khẩu thành công, hệ thống không tạo phiên đăng nhập mới; Customer phải đăng nhập bằng mật khẩu mới.
6. Email được trim khoảng trắng dư và chuyển về lowercase trước khi kiểm tra tài khoản.
7. Nếu email không tồn tại hoặc tài khoản không có phương thức đăng nhập bằng email và mật khẩu, hệ thống không gửi OTP đặt lại mật khẩu.
8. Thông báo sau khi Customer gửi email đặt lại mật khẩu không được tiết lộ email có tồn tại trong hệ thống hay không.
9. Mật khẩu được lưu dưới dạng đã mã hoá.

### Non-Functional Requirements

1. Mật khẩu và OTP không được ghi log hoặc trả về phía client.
2. Dữ liệu đặt lại mật khẩu phải được truyền qua kết nối mã hóa (HTTPS/TLS).
3. Hệ thống cần bảo vệ chức năng đặt lại mật khẩu khỏi brute force bằng giới hạn tốc độ hoặc cơ chế tương đương.
4. Hệ thống phải đảm bảo việc cập nhật mật khẩu và trạng thái xác thực email của cùng một lần đặt lại mật khẩu được xử lý nhất quán, không để phát sinh trạng thái cập nhật một phần.