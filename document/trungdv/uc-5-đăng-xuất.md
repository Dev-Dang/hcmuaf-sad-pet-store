---
type: Note
_organized: true
---
# UC-5: Đăng xuất

### Use Case ID: UC-5

### Use Case Name: Đăng xuất

### Description:

- Customer hoặc Admin chủ động kết thúc phiên đăng nhập hiện tại khi không còn nhu cầu sử dụng hệ thống.
- Sau khi đăng xuất thành công, hệ thống đưa actor về trạng thái Guest và điều hướng đến trang phù hợp với loại tài khoản.

### Actor(s):

- Chính: Customer, Admin
- Phụ: Không có

### Priority: MUST

- Bắt buộc để Customer/Admin chủ động kết thúc phiên đăng nhập và giảm rủi ro truy cập trái phép trên thiết bị hiện tại.

### Trigger

Actor chọn chức năng "Đăng xuất" trong giao diện.

### Pre-Conditions

Actor đang có phiên đăng nhập hiện tại trên thiết bị/browser đang sử dụng.

### Post-Conditions

**Thành công**

1. Phiên đăng nhập hiện tại không còn hiệu lực với actor trên thiết bị/browser hiện tại.
2. Actor trở về trạng thái Guest hoặc được yêu cầu đăng nhập lại nếu phiên đã hết hạn.
3. Actor được điều hướng đến trang sau đăng xuất phù hợp với loại tài khoản hoặc trạng thái phiên.
4. Phiên đăng nhập trên thiết bị/browser khác của cùng tài khoản không bị ảnh hưởng.

**Thất bại**

1. Phiên đăng nhập hiện tại vẫn còn hiệu lực cho đến khi đăng xuất thành công hoặc phiên hết hạn.
2. Actor được thông báo rằng hệ thống chưa thể hoàn tất đăng xuất.

### Normal Flow

**Customer/Admin đăng xuất thành công khỏi phiên hiện tại.**

5.1.1 Customer/Admin — Chọn chức năng "Đăng xuất" trong giao diện.\
5.1.2 Hệ thống — Xác nhận phiên đăng nhập hiện tại còn hợp lệ.\
5.1.3 Hệ thống — Xác định trang điều hướng sau đăng xuất.\
5.1.4 Hệ thống — Vô hiệu hóa phiên đăng nhập hiện tại.\
5.1.5 Hệ thống — Xóa thông tin xác thực trên thiết bị/browser hiện tại. (cookie, token xác thực, ...)\
5.1.6 Hệ thống — Điều hướng actor đến trang sau đăng xuất.

### Alternative Flow

**AF1: Phiên đăng nhập đã hết hạn khi đăng xuất**

Tại bước 5.1.2 của Normal Flow, hệ thống phát hiện phiên đăng nhập hiện tại đã hết hạn.

5.2.1 Hệ thống — Xóa thông tin xác thực trên thiết bị/browser hiện tại. (cookie, token xác thực, ...)\
5.2.2 Hệ thống — Điều hướng actor đến trang đăng nhập.\
5.2.3 Hệ thống — Hiển thị thông báo: "Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại."\
5.2.4 Hệ thống — Kết thúc thành công.

### Exception Flow

**EF1: Lỗi hệ thống**

Tại bước 5.1.2 của Normal Flow, hệ thống phát hiện lỗi hệ thống và không thể hoàn tất yêu cầu đăng xuất.

5.3.1 Hệ thống — Hiển thị thông báo lỗi và yêu cầu actor thử lại.\
5.3.2 Hệ thống — Giữ nguyên phiên đăng nhập hiện tại cho đến khi đăng xuất thành công hoặc phiên hết hạn.\
5.3.3 Hệ thống — Kết thúc thất bại.

### Business Rules

1. Sau khi đăng xuất thành công, phiên đăng nhập hiện tại trên thiết bị/browser hiện tại phải bị vô hiệu hóa và không còn được dùng để truy cập chức năng yêu cầu đăng nhập.
2. Đăng xuất chỉ áp dụng cho phiên đăng nhập hiện tại trên thiết bị/browser đang sử dụng; các phiên khác của cùng tài khoản không bị ảnh hưởng.
3. Sau khi Admin đăng xuất, actor phải đăng nhập lại trước khi truy cập khu vực quản trị.
4. Phiên đăng nhập hết hạn sau 24 giờ kể từ lúc tạo, áp dụng cho cả Customer và Admin.
5. Khi đăng xuất khỏi tài khoản đăng nhập bằng Google, hệ thống chỉ kết thúc phiên đăng nhập nội bộ; hệ thống không đăng xuất actor khỏi tài khoản Google và không hủy liên kết Google account.
6. Trang điều hướng sau đăng xuất:
   - Customer được điều hướng về Trang chủ cửa hàng.
   - Admin được điều hướng về Trang đăng nhập.
   - Actor có phiên đã hết hạn được điều hướng về Trang đăng nhập.

### Non-Functional Requirements

1. Sau khi đăng xuất thành công, thông tin xác thực đã bị xóa hoặc vô hiệu hóa không được dùng lại để truy cập dữ liệu hoặc chức năng yêu cầu đăng nhập.
2. Hệ thống không hiển thị dữ liệu cá nhân từ phiên đã đăng xuất khi actor quay lại trang trước đó bằng chức năng Back của browser.
