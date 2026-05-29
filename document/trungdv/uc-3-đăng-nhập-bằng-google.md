---
_organized: true
---
# UC-3: Đăng nhập bằng Google

### Use Case ID: UC-3

### Use Case Name: Đăng nhập bằng Google

### Description:

- Use case này mô tả cách Guest hoặc Customer đăng nhập vào tài khoản Customer bằng Google.
- Sau khi Google xác thực thành công, hệ thống tạo phiên đăng nhập Customer hợp lệ và liên kết hoặc tạo tài khoản Customer theo email Google.

### Actor(s):

- Chính: Guest/Customer
- Phụ: Google OAuth

### Priority: MUST

- Đăng nhập bằng Google là phương thức đăng nhập Customer quan trọng trong MVP và là cơ sở cho account linking theo email.

### Trigger

1. Guest/Customer chọn đăng nhập bằng Google từ trang đăng nhập hoặc từ chức năng Customer yêu cầu đăng nhập.
2. Customer muốn dùng Google để đăng nhập vào tài khoản Customer đã có cùng email.

### Pre-Conditions

1. Guest/Customer chưa có phiên Customer hợp lệ hoặc muốn tạo phiên Customer mới bằng Google.
2. Hệ thống đang cho phép đăng nhập bằng Google cho Customer.

### Post-Conditions

1. Customer có phiên đăng nhập Customer hợp lệ.
2. Google login được liên kết với tài khoản Customer hiện có nếu email đã tồn tại.
3. Hệ thống tạo tài khoản Customer mới từ thông tin Google nếu email chưa tồn tại.

### Normal Flow

**Customer đăng nhập bằng Google với tài khoản đã liên kết Google thành công.**

3.1.1 Guest/Customer — Truy cập trang đăng nhập hoặc chức năng yêu cầu đăng nhập.
3.1.2 Hệ thống — Hiển thị form đăng nhập với tùy chọn đăng nhập bằng Google.
3.1.3 Guest/Customer — Chọn chức năng "Đăng nhập bằng Google".
3.1.4 Hệ thống — Chuyển yêu cầu xác thực sang Google OAuth.
3.1.5 Google OAuth — Xác thực Google account của người dùng.
3.1.6 Google OAuth — Trả về kết quả định danh Google hợp lệ cho hệ thống.
3.1.7 Hệ thống — Xác nhận Google là phương thức đăng nhập hợp lệ của tài khoản Customer.
3.1.8 Hệ thống — Tạo phiên đăng nhập với quyền Customer.
3.1.9 Hệ thống — Xác định trang điều hướng sau đăng nhập theo chức năng yêu cầu nếu có, mặc định là trang chủ.
3.1.10 Hệ thống — Điều hướng Customer đến trang sau đăng nhập.

### Alternative Flow

**AF1: Liên kết Google với tài khoản Customer cùng email**

Tại bước 3.1.7 của Normal Flow, hệ thống phát hiện email Google trùng với tài khoản Customer đăng ký bằng email và mật khẩu nhưng chưa liên kết Google.

3.2.1 Hệ thống — Liên kết Google login với tài khoản Customer cùng email.
3.2.2 Hệ thống — Quay lại bước 3.1.8 của Normal Flow.

**AF2: Tạo tài khoản Customer mới từ Google**

Tại bước 3.1.7 của Normal Flow, hệ thống phát hiện email Google chưa thuộc tài khoản Customer nào.

3.3.1 Hệ thống — Tạo tài khoản Customer mới từ thông tin Google.
3.3.2 Hệ thống — Ghi nhận Google là phương thức đăng nhập của tài khoản Customer mới.
3.3.3 Hệ thống — Quay lại bước 3.1.8 của Normal Flow.

**AF3: Trang được điều hướng yêu cầu quyền Admin**

Tại bước 3.1.9 của Normal Flow, hệ thống phát hiện trang điều hướng sau đăng nhập yêu cầu quyền Admin.

3.4.1 Hệ thống — Không điều hướng Customer đến trang yêu cầu quyền Admin.
3.4.2 Hệ thống — Hiển thị thông báo Customer không có quyền truy cập trang này.
3.4.3 Hệ thống — Điều hướng Customer đến trang chủ.

### Exception Flow

**EF1: Google OAuth lỗi hoặc người dùng hủy xác thực**

Tại bước 3.1.6 của Normal Flow, hệ thống nhận kết quả lỗi từ Google OAuth, người dùng hủy xác thực hoặc kết quả định danh Google không hợp lệ.

3.5.1 Hệ thống — Không tạo phiên đăng nhập Customer.
3.5.2 Hệ thống — Hiển thị thông báo đăng nhập Google không thành công.
3.5.3 Guest/Customer — Thử lại hoặc chọn phương thức đăng nhập khác.
3.5.4 Hệ thống — Kết thúc thất bại.

**EF2: Lỗi hệ thống**

Tại bước 3.1.7, 3.1.8 hoặc 3.1.9 của Normal Flow, hoặc tại AF1/AF2, hệ thống không thể hoàn tất xử lý đăng nhập Google.

3.6.1 Hệ thống — Dừng xử lý đăng nhập Google.
3.6.2 Hệ thống — Rollback toàn bộ thay đổi của lần đăng nhập Google này.
3.6.3 Hệ thống — Không tạo phiên đăng nhập Customer mới.
3.6.4 Hệ thống — Hiển thị thông báo hệ thống tạm thời không thể xử lý đăng nhập Google.
3.6.5 Hệ thống — Kết thúc thất bại.

### Business Rules

1. Google login và email login nếu có cùng email được liên kết chung vào cùng một tài khoản Customer.
2. Nếu Google cung cấp trạng thái email verified hợp lệ, hệ thống có thể ghi nhận email Customer là đã xác thực để phục vụ tính năng đặt lại mật khẩu.
3. Khi Customer đăng xuất sau khi đăng nhập bằng Google, hệ thống chỉ kết thúc phiên đăng nhập nội bộ; không đăng xuất khỏi Google và không hủy liên kết Google login.
4. Google login chỉ áp dụng cho Customer và không cấp quyền Admin.
5. Trang điều hướng sau đăng nhập được xác định theo chức năng yêu cầu nếu có; nếu không có, hệ thống điều hướng Customer đến trang chủ.
6. Nếu trang điều hướng sau đăng nhập yêu cầu quyền Admin, hệ thống không điều hướng Customer đến trang đó và điều hướng Customer về trang chủ.
7. Nếu hệ thống không thể hoàn tất xử lý đăng nhập Google, hệ thống rollback toàn bộ thay đổi của lần đăng nhập Google đó.

### Non-Functional Requirements

1. Hệ thống chỉ tạo hoặc liên kết tài khoản Customer khi kết quả định danh Google hợp lệ.
2. Hệ thống cần thông báo rõ khi Google login không thành công.
