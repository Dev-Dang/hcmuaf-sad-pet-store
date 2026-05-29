---
type: Note
_organized: true
---
# UC-8 Quản lý địa chỉ giao hàng

### Use Case ID: UC-8

### Use Case Name: Quản lý địa chỉ giao hàng

### Description:

- Use case này mô tả cách Customer quản lý danh sách địa chỉ giao hàng trong hồ sơ cá nhân.
- Customer có thể xem danh sách, thêm, chỉnh sửa, xóa và đặt một địa chỉ giao hàng làm địa chỉ mặc định.
- Hệ thống chỉ cập nhật danh sách địa chỉ giao hàng khi thao tác đáp ứng đầy đủ thông tin bắt buộc và quy tắc hợp lệ.

### Actor(s):

- Chính: Customer
- Phụ: Không có

### Priority: MUST

- Địa chỉ giao hàng hợp lệ giúp Customer sử dụng nhanh trong quy trình đặt hàng và giảm lỗi khi tạo đơn hàng.

### Trigger

1. Customer mở chức năng quản lý địa chỉ giao hàng trong hồ sơ cá nhân.
2. Customer yêu cầu thêm, chỉnh sửa, xóa hoặc đặt mặc định một địa chỉ giao hàng.

### Pre-Conditions

1. Customer đã đăng nhập và phiên đăng nhập còn hiệu lực.

### Post-Conditions

**Thành công**

1. Danh sách địa chỉ giao hàng của Customer được cập nhật theo thao tác đã xác nhận.
2. Mỗi địa chỉ được lưu đều đáp ứng đầy đủ thông tin bắt buộc và quy tắc hợp lệ.
3. Nếu Customer đặt địa chỉ mặc định, hệ thống ghi nhận đúng một địa chỉ mặc định trong danh sách.

**Không thành công**

1. Danh sách địa chỉ giao hàng của Customer không thay đổi nếu thao tác không hợp lệ, bị hủy hoặc không hoàn tất.
2. Dữ liệu địa chỉ giao hàng của Customer được giữ ở trạng thái nhất quán, không phát sinh bản ghi hoặc cập nhật dang dở.

### Normal Flow

**Customer xem danh sách địa chỉ giao hàng.**

8.1.1 Customer — Chọn mục quản lý địa chỉ giao hàng trong hồ sơ cá nhân.
8.1.2 Hệ thống — Truy xuất danh sách địa chỉ giao hàng của Customer.
8.1.3 Hệ thống — Hiển thị danh sách địa chỉ giao hàng với thông tin tóm tắt (tên, số điện thoại và địa chỉ ngắn gọn 1 dòng) và gán nhãn rõ ràng cho địa chỉ mặc định (nếu có).
8.1.4 Hệ thống — Hiển thị tùy chọn thêm địa chỉ giao hàng mới.
8.1.5 Hệ thống — Hiển thị các tùy chọn chỉnh sửa, xóa và đặt mặc định phù hợp với từng địa chỉ giao hàng.

### Alternative Flow

**AF1: Customer thêm địa chỉ giao hàng**

Tại bước 8.1.4 của Normal Flow, Customer chọn thêm địa chỉ giao hàng.

8.2.1 Hệ thống — Hiển thị form thêm địa chỉ gồm họ tên người nhận, số điện thoại nhận hàng, tỉnh/thành phố, phường/xã và địa chỉ chi tiết.
8.2.2 Customer — Nhập thông tin địa chỉ giao hàng và chọn "Lưu địa chỉ".
8.2.3 Hệ thống — Kiểm tra tính hợp lệ của địa chỉ giao hàng mới.
8.2.4 Hệ thống — Lưu địa chỉ giao hàng mới cho Customer.
8.2.5 Hệ thống — Thông báo thêm địa chỉ thành công và hiển thị danh sách địa chỉ giao hàng đã được cập nhật.

**AF2: Customer chỉnh sửa địa chỉ giao hàng**

Tại bước 8.1.5 của Normal Flow, Customer chọn chỉnh sửa một địa chỉ giao hàng.

8.3.1 Hệ thống — Truy xuất thông tin chi tiết của địa chỉ giao hàng được chọn.
8.3.2 Hệ thống — Hiển thị form chỉnh sửa địa chỉ với thông tin hiện tại gồm họ tên người nhận, số điện thoại nhận hàng, tỉnh/thành phố, phường/xã và địa chỉ chi tiết.
8.3.3 Customer — Cập nhật thông tin địa chỉ giao hàng và chọn "Lưu thay đổi".
8.3.4 Hệ thống — Kiểm tra tính hợp lệ của địa chỉ giao hàng đã cập nhật.
8.3.5 Hệ thống — Cập nhật địa chỉ giao hàng được chọn của Customer.
8.3.6 Hệ thống — Thông báo cập nhật địa chỉ thành công và hiển thị danh sách địa chỉ giao hàng đã được cập nhật.

**AF3: Customer xóa địa chỉ giao hàng**

Tại bước 8.1.5 của Normal Flow, Customer chọn xóa một địa chỉ giao hàng trong danh sách.

8.4.1 Hệ thống — Hiển thị hộp thoại xác nhận xóa địa chỉ giao hàng.
8.4.2 Customer — Xác nhận xóa địa chỉ giao hàng.
8.4.3 Hệ thống — Xác nhận địa chỉ giao hàng được chọn không phải là địa chỉ mặc định.
8.4.4 Hệ thống — Xóa địa chỉ giao hàng được chọn của Customer.
8.4.5 Hệ thống — Thông báo xóa địa chỉ thành công và hiển thị danh sách địa chỉ giao hàng đã được cập nhật.

**AF4: Customer đặt địa chỉ giao hàng mặc định**

Tại bước 8.1.5 của Normal Flow, Customer chọn đặt địa chỉ mặc định.

8.5.1 Hệ thống — Hiển thị hộp thoại xác nhận đặt địa chỉ giao hàng mặc định.
8.5.2 Customer — Xác nhận đặt địa chỉ giao hàng mặc định.
8.5.3 Hệ thống — Cập nhật địa chỉ giao hàng được chọn thành địa chỉ mặc định duy nhất của Customer.
8.5.4 Hệ thống — Thông báo đặt địa chỉ mặc định thành công và hiển thị danh sách địa chỉ giao hàng đã được cập nhật.

### Exception Flow

**EF1: Địa chỉ giao hàng không hợp lệ**

Tại bước 8.2.3 của AF1 hoặc bước 8.3.4 của AF2, hệ thống phát hiện địa chỉ giao hàng không hợp lệ.

8.6.1 Hệ thống — Thông báo lỗi tương ứng với thông tin chưa hợp lệ.
8.6.2 Hệ thống — Hiển thị lại form địa chỉ với thông tin Customer đã nhập.
8.6.3 Hệ thống — Quay lại bước nhập địa chỉ của luồng hiện tại.

**EF2: Xóa địa chỉ giao hàng mặc định**

Tại bước 8.4.3 của AF3, hệ thống phát hiện địa chỉ giao hàng được chọn là địa chỉ mặc định.

8.7.1 Hệ thống — Thông báo không thể xóa địa chỉ mặc định; hãy đặt địa chỉ khác làm mặc định trước.
8.7.2 Hệ thống — Hiển thị lại danh sách địa chỉ giao hàng.

**EF3: Phiên đăng nhập hết hạn**

Tại bước 8.1.2 của Normal Flow, bước 8.2.4 của AF1, bước 8.3.5 của AF2, bước 8.4.4 của AF3 hoặc bước 8.5.3 của AF4, hệ thống phát hiện phiên đăng nhập của Customer đã hết hạn.

8.8.1 Hệ thống — Thông báo phiên đăng nhập đã hết hạn và yêu cầu Customer đăng nhập lại.
8.8.2 Hệ thống — Điều hướng Customer đến trang đăng nhập.

**EF4: Lỗi hệ thống**

Tại bước 8.1.2 của Normal Flow, bước 8.2.4 của AF1, bước 8.3.5 của AF2, bước 8.4.4 của AF3 hoặc bước 8.5.3 của AF4, hệ thống không thể hoàn tất thao tác.

8.9.1 Hệ thống — Thông báo không thể hoàn tất thao tác quản lý địa chỉ giao hàng.
8.9.2 Hệ thống — Không lưu thay đổi một phần đối với dữ liệu địa chỉ giao hàng của Customer.

### Business Rules

1. Mỗi địa chỉ giao hàng phải thuộc về đúng một Customer.
2. Customer chỉ được quản lý các địa chỉ giao hàng thuộc tài khoản của mình.
3. Địa chỉ giao hàng phải có họ tên người nhận, số điện thoại nhận hàng, tỉnh/thành phố, phường/xã và địa chỉ chi tiết.
4. Số điện thoại nhận hàng phải đúng định dạng số điện thoại Việt Nam: 10 chữ số và bắt đầu bằng 0.
5. Hệ thống chỉ kiểm tra định dạng số điện thoại nhận hàng, không yêu cầu xác minh số điện thoại bằng OTP khi thêm hoặc chỉnh sửa địa chỉ.
6. Customer có thể không có địa chỉ giao hàng nào trong danh sách.
7. Khi danh sách có địa chỉ giao hàng, hệ thống luôn ghi nhận đúng một địa chỉ mặc định.
8. Khi Customer thêm địa chỉ giao hàng đầu tiên, hệ thống tự đặt địa chỉ đó làm địa chỉ mặc định.
9. Customer không được xóa địa chỉ giao hàng mặc định.
10. Customer muốn xóa địa chỉ mặc định phải đặt một địa chỉ giao hàng khác làm mặc định trước.
11. Khi Customer đặt một địa chỉ giao hàng làm mặc định, hệ thống cập nhật để địa chỉ được chọn là địa chỉ mặc định duy nhất.
12. Khi Customer xóa địa chỉ giao hàng, các đơn hàng đã tạo trước đó không bị thay đổi địa chỉ giao hàng đã lưu snapshot.
13. Hệ thống không yêu cầu email của Customer đã xác thực để Customer quản lý địa chỉ giao hàng.
14. Hệ thống không lưu thay đổi một phần nếu không thể hoàn tất thao tác thêm, chỉnh sửa, xóa hoặc đặt mặc định địa chỉ giao hàng.
15. Customer có thể hủy thao tác thêm, chỉnh sửa, xóa hoặc đặt mặc định trước khi hệ thống lưu thay đổi; khi đó hệ thống hiển thị lại danh sách địa chỉ giao hàng.

### Non-Functional Requirements

1. Hệ thống phải bảo vệ dữ liệu địa chỉ giao hàng để Customer không thể xem hoặc thay đổi địa chỉ của Customer khác.
2. Hệ thống phải thông báo rõ khi thông tin địa chỉ không hợp lệ, phiên đăng nhập hết hạn, địa chỉ mặc định không thể bị xóa hoặc thao tác quản lý địa chỉ không thể hoàn tất.
3. Danh sách địa chỉ giao hàng phải hiển thị rõ địa chỉ mặc định để Customer dễ nhận biết.
