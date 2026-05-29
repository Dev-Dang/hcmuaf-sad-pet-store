---
_organized: true
---
# UC-6 Xác thực OTP

### Use Case ID: UC-6

### Use Case Name: Xác thực OTP

### Description:

- Use case này mô tả cơ chế dùng chung để Customer xác thực OTP cho một nghiệp vụ cụ thể.
- Sau khi OTP được xác thực thành công, hệ thống trả kết quả xác thực OTP cho use case gọi UC-6.

### Actor(s):

- Chính: Customer
- Phụ: Email Service

### Priority: MUST

- Đây là cơ chế bắt buộc cho các nghiệp vụ cần xác thực quyền kiểm soát email bằng OTP, gồm xác thực email và đặt lại mật khẩu.

### Trigger

- UC-7 hoặc UC-4 yêu cầu xác thực OTP cho email của Customer.

### Pre-Conditions

1. Use case gọi UC-6 đã xác định email cần xác thực OTP.
2. Hệ thống đang cho phép gửi và xác thực OTP cho nghiệp vụ tương ứng.

### Post-Conditions

#### Thành công

1. OTP được xác thực thành công.
2. Hệ thống trả kết quả xác thực OTP thành công cho use case gọi UC-6.
3. OTP đã xác thực được vô hiệu hóa và không thể dùng lại.

#### Thất bại

1. OTP không được xác thực thành công.
2. Hệ thống trả kết quả xác thực OTP thất bại cho use case gọi UC-6 và không cập nhật trạng thái nghiệp vụ của use case đó.

### Normal Flow

**Customer xác thực OTP thành công.**

6.1.1 UC-7/UC-4 — Yêu cầu xác thực OTP cho email của Customer.\
6.1.2 Hệ thống — Sinh OTP cho nghiệp vụ xác thực hiện tại.\
6.1.3 Hệ thống — Yêu cầu Email Service gửi OTP cho Customer.\
6.1.4 Email Service — Gửi email chứa OTP đến hộp thư của Customer.\
6.1.5 Hệ thống — Ghi nhận thời điểm gửi OTP thành công.\
6.1.6 Hệ thống — Hiển thị form nhập OTP kèm thời gian chờ còn lại trước khi Customer có thể yêu cầu gửi lại OTP.\
6.1.7 Customer — Nhập OTP và gửi yêu cầu xác thực.\
6.1.8 Hệ thống — Xác thực OTP là hợp lệ.\
6.1.9 Hệ thống — Vô hiệu hóa OTP đã xác thực.\
6.1.10 Hệ thống — Trả kết quả xác thực OTP thành công cho UC-7/UC-4.

### Alternative Flow

**AF1: Gửi lại OTP**

Tại bước 6.1.7 của Normal Flow, Customer yêu cầu gửi lại OTP.

6.2.1 Customer — Chọn gửi lại OTP.\
6.2.2 Hệ thống — Kiểm tra yêu cầu gửi lại OTP là hợp lệ.\
6.2.3 Hệ thống — Vô hiệu hóa OTP trước đó.\
6.2.4 Hệ thống — Sinh OTP mới cho nghiệp vụ xác thực hiện tại.\
6.2.5 Hệ thống — Yêu cầu Email Service gửi OTP mới cho Customer.\
6.2.6 Email Service — Gửi email chứa OTP mới đến hộp thư của Customer.\
6.2.7 Hệ thống — Ghi nhận thời điểm gửi OTP mới thành công.\
6.2.8 Hệ thống — Hiển thị form nhập OTP kèm thời gian chờ còn lại trước khi Customer có thể yêu cầu gửi lại OTP.\
6.2.9 Hệ thống — Quay lại bước 6.1.7 của Normal Flow.

### Exception Flow

**EF1: Quá số lần thử OTP**

Tại bước 6.1.8 của Normal Flow, hệ thống phát hiện yêu cầu xác thực OTP hiện tại đã vượt quá số lần thử cho phép.

6.3.1 Hệ thống — Từ chối xác thực OTP cho yêu cầu hiện tại.\
6.3.2 Hệ thống — Hiển thị thông báo: "Bạn đã thử quá số lần cho phép. Vui lòng lấy OTP mới."\
6.3.3 Hệ thống — Trả kết quả xác thực OTP thất bại cho UC-7/UC-4.\
6.3.4 Hệ thống — Kết thúc thất bại.

**EF2: OTP không hợp lệ**

Tại bước 6.1.8 của Normal Flow, hệ thống không tìm thấy bản ghi OTP tương ứng với OTP Customer nhập và mục đích xác thực hiện tại.

6.4.1 Hệ thống — Hiển thị thông báo OTP không khớp, vui lòng thử lại.\
6.4.2 Customer — Nhập lại OTP và gửi yêu cầu xác thực.\
6.4.3 Hệ thống — Quay lại bước 6.1.8 của Normal Flow.

**EF3: OTP không còn hiệu lực**

Tại bước 6.1.8 của Normal Flow, hệ thống tìm thấy bản ghi OTP tương ứng với OTP Customer nhập nhưng bản ghi này không còn hiệu lực.

6.5.1 Hệ thống — Hiển thị thông báo: "OTP không còn hiệu lực. Vui lòng yêu cầu gửi OTP mới."\
6.5.2 Customer — Chọn gửi lại OTP.\
6.5.3 Hệ thống — Chuyển sang bước 6.2.2 của AF1.

**EF4: Yêu cầu gửi lại OTP khi chưa hết thời gian chờ**

Tại bước 6.2.2 của AF1, hệ thống phát hiện yêu cầu gửi lại OTP được gửi lên khi chưa đủ 60 giây kể từ lần gửi OTP gần nhất.

6.6.1 Hệ thống — Từ chối yêu cầu gửi lại OTP.\
6.6.2 Hệ thống — Xác định thời gian chờ còn lại trước khi Customer có thể yêu cầu gửi lại OTP.\
6.6.3 Hệ thống — Hiển thị thông báo yêu cầu Customer vui lòng chờ kèm thời gian chờ còn lại trước khi yêu cầu OTP mới.\
6.6.4 Hệ thống — Quay lại bước 6.1.7 của Normal Flow.

**EF5: Vượt quá số lần gửi lại OTP**

Tại bước 6.2.2 của AF1, hệ thống phát hiện yêu cầu gửi lại OTP đã vượt quá số lần cho phép.

6.7.1 Hệ thống — Không gửi OTP mới.\
6.7.2 Hệ thống — Hiển thị thông báo: "Bạn đã gửi lại OTP quá số lần cho phép. Vui lòng thử lại sau 24 giờ."\
6.7.3 Hệ thống — Trả kết quả xác thực OTP thất bại cho UC-7/UC-4.\
6.7.4 Hệ thống — Kết thúc thất bại.

**EF6: Lỗi hệ thống**

Tại bước 6.1.2, 6.1.3, 6.1.8, 6.1.9 của Normal Flow hoặc bước 6.2.2, 6.2.3, 6.2.4, 6.2.5, 6.2.7 của AF1, hệ thống không thể hoàn tất xử lý OTP do lỗi hệ thống hoặc lỗi từ Email Service.

6.8.1 Hệ thống — Dừng xử lý xác thực OTP.\
6.8.2 Hệ thống — Hiển thị thông báo: "Đã xảy ra lỗi. Vui lòng thử lại sau."\
6.8.3 Hệ thống — Trả kết quả xác thực OTP thất bại cho UC-7/UC-4.\
6.8.4 Hệ thống — Kết thúc thất bại.

### Business Rules

1. OTP chỉ hợp lệ cho đúng email Customer và mục đích xác thực đã yêu cầu.
2. OTP có hiệu lực 10 phút tính từ thời điểm tạo thành công.
3. Customer chỉ được gửi lại OTP sau 60 giây kể từ lần gửi OTP gần nhất, tối đa 5 lần cho mỗi yêu cầu xác thực; vượt giới hạn phải chờ 24 giờ.
4. Mỗi OTP cho phép tối đa 5 lần thử.
5. Khi gửi lại OTP, hệ thống vô hiệu hóa OTP trước đó và sinh OTP mới cho cùng email Customer và mục đích xác thực.
6. OTP đã xác thực thành công hoặc đã bị vô hiệu hóa không thể dùng lại.
7. UC-6 chỉ trả kết quả xác thực OTP thành công hoặc thất bại cho UC-7/UC-4; kết quả nghiệp vụ cuối cùng do UC gọi quyết định.

### Non-Functional Requirements

1. OTP không được trả về trong response phía client.
2. OTP phải được sinh bằng cơ chế đủ an toàn để không thể đoán được.
3. Số lần thử OTP, số lần gửi lại OTP và thời gian chờ giữa hai lần gửi OTP được duy trì phía server; client không thể bypass các giới hạn bảo mật.
4. Dữ liệu OTP phải được truyền qua kết nối mã hóa (HTTPS/TLS).
