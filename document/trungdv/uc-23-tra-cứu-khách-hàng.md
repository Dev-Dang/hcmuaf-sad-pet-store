---
type: Note
_organized: true
---
# UC-23 Tra cứu Khách hàng

### Use Case ID: UC-23

### Use Case Name: Tra cứu khách hàng

### Description:

- Admin tra cứu khách hàng trong hệ thống để hỗ trợ vận hành và xử lý đơn hàng.
- Use case cho phép Admin xem danh sách tài khoản Customer, thông tin chi tiết của Customer và lịch sử đơn hàng liên quan.
- Kết quả tra cứu giúp Admin có đủ thông tin cần thiết mà không làm thay đổi dữ liệu khách hàng hoặc đơn hàng.

### Actor(s):

- Chính: Admin
- Phụ: Không có

### Priority: SHOULD

Chức năng quan trọng cho vận hành và hỗ trợ đơn hàng, nhưng không trực tiếp chặn Customer thực hiện các luồng mua hàng chính.

### Trigger

Admin truy cập chức năng Tra cứu khách hàng trong giao diện quản trị để tra cứu thông tin khách hàng phục vụ vận hành.

### Pre-Conditions

1. Admin đã đăng nhập thành công vào hệ thống Admin.
2. Admin có quyền truy cập chức năng Quản lý khách hàng.

### Post-Conditions

**Thành công**

1. Admin xem được danh sách tài khoản Customer theo điều kiện tìm kiếm hiện tại, hoặc trạng thái không có kết quả phù hợp.
2. Admin xem được thông tin chi tiết của Customer được chọn.
3. Admin xem được lịch sử đơn hàng của Customer nếu chọn xem lịch sử đơn hàng.

**Bất biến dữ liệu**

4. Không có dữ liệu khách hàng hoặc dữ liệu đơn hàng nào bị thay đổi trong UC-23.

### Normal Flow

**Admin tra cứu tài khoản Customer và xem thông tin chi tiết.**

23.1.1 Admin — Chọn chức năng Tra cứu khách hàng trong giao diện quản trị.
23.1.2 Hệ thống — Truy xuất danh sách tài khoản Customer.
23.1.3 Hệ thống — Hiển thị danh sách tài khoản Customer gồm tên hiển thị, email và ngày tạo tài khoản, có phân trang.
23.1.4 Admin — Nhập từ khóa theo tên hoặc email và thực hiện tìm kiếm.
23.1.5 Hệ thống — Truy xuất danh sách tài khoản Customer khớp với từ khóa.
23.1.6 Hệ thống — Hiển thị danh sách tài khoản Customer khớp với từ khóa, có phân trang.
23.1.7 Admin — Chọn một tài khoản Customer để xem chi tiết.
23.1.8 Hệ thống — Truy xuất thông tin chi tiết của tài khoản Customer được chọn.
23.1.9 Hệ thống — Hiển thị thông tin chi tiết của Customer.

### Alternative Flow

**AF1: Admin xem lịch sử đơn hàng của Customer**

Tại bước 23.1.9 của Normal Flow, Admin chọn xem lịch sử đơn hàng của Customer.

23.2.1 Admin — Mở tab Lịch sử đơn hàng.
23.2.2 Hệ thống — Truy xuất danh sách đơn hàng của Customer được chọn.
23.2.3 Hệ thống — Hiển thị lịch sử đơn hàng gồm mã đơn, ngày tạo, trạng thái đơn hàng, trạng thái thanh toán và tổng giá trị, có phân trang.
23.2.4 Hệ thống — Kết thúc thành công.

### Exception Flow

**EF1: Không có tài khoản Customer khớp với từ khóa**

Tại bước 23.1.5 của Normal Flow, hệ thống không tìm thấy tài khoản Customer nào khớp với từ khóa.

23.3.1 Hệ thống — Hiển thị thông báo không có tài khoản Customer khớp với từ khóa tìm kiếm.
23.3.2 Hệ thống — Giữ nguyên từ khóa tìm kiếm để Admin có thể điều chỉnh.
23.3.3 Hệ thống — Quay lại bước 23.1.4 của Normal Flow.

**EF2: Customer chưa có đơn hàng**

Tại bước 23.2.2 của AF1, hệ thống không tìm thấy đơn hàng nào của Customer được chọn.

23.4.1 Hệ thống — Hiển thị thông báo Customer chưa có đơn hàng.
23.4.2 Hệ thống — Kết thúc thành công.

**EF3: Phiên đăng nhập Admin hết hạn**

Tại bước 23.1.2, 23.1.5, 23.1.8 của Normal Flow hoặc bước 23.2.2 của AF1, hệ thống phát hiện phiên đăng nhập Admin đã hết hạn.

23.5.1 Hệ thống — Thông báo phiên đăng nhập đã hết hạn và yêu cầu Admin đăng nhập lại.
23.5.2 Hệ thống — Điều hướng Admin đến trang đăng nhập.
23.5.3 Hệ thống — Kết thúc thất bại.

**EF4: Lỗi hệ thống**

Tại bước 23.1.2, 23.1.5, 23.1.8 của Normal Flow hoặc bước 23.2.2 của AF1, hệ thống không thể hoàn tất truy xuất dữ liệu.

23.6.1 Hệ thống — Hiển thị thông báo "Lỗi hệ thống tạm thời. Vui lòng thử lại sau."
23.6.2 Hệ thống — Kết thúc thất bại.

### Business Rules

1. Chỉ Admin có quyền Quản lý khách hàng mới được sử dụng chức năng Tra cứu khách hàng.
2. UC-23 là chức năng read-only; Admin không thể chỉnh sửa thông tin tài khoản Customer, địa chỉ hoặc đơn hàng từ chức năng này.
3. Danh sách tài khoản Customer hiển thị tên hiển thị, email và ngày tạo tài khoản.
4. Tìm kiếm tài khoản Customer hỗ trợ theo tên hiển thị và email, không phân biệt chữ hoa/thường.
5. Thông tin chi tiết Customer bao gồm email, số điện thoại, địa chỉ, ngày tạo tài khoản, tổng số đơn hàng và tổng giá trị đơn hàng.
6. Số điện thoại và địa chỉ trong thông tin chi tiết Customer được lấy từ địa chỉ giao hàng của Customer nếu có.
7. Lịch sử đơn hàng của Customer hiển thị mã đơn, ngày tạo, trạng thái đơn hàng, trạng thái thanh toán và tổng giá trị.
8. Trạng thái đơn hàng và trạng thái thanh toán phải được hiển thị riêng, không hợp nhất thành một trường.
9. Nếu Admin chọn một đơn hàng trong lịch sử đơn hàng để xem chi tiết, hệ thống điều hướng sang UC-18 và UC-23 kết thúc.

### Non-Functional Requirements

**Bảo mật**

1. Tất cả màn hình thuộc UC-23 phải yêu cầu phiên đăng nhập Admin hợp lệ.
2. Truy cập trực tiếp URL tra cứu hoặc chi tiết Customer khi chưa có phiên đăng nhập Admin hợp lệ phải bị chặn bằng cách chuyển hướng đến trang đăng nhập hoặc trả về lỗi phân quyền.
3. Hệ thống không hiển thị thông tin Customer nếu người truy cập chưa xác thực hoặc không đủ quyền.
4. Dữ liệu PII như email, số điện thoại và địa chỉ không được cache phía client-side.

**Hiệu năng**

5. Danh sách tài khoản Customer phải tải trong ≤ 2 giây với tối đa 10,000 bản ghi.
6. Kết quả tìm kiếm tài khoản Customer phải trả về trong ≤ 1 giây.
7. Lịch sử đơn hàng của Customer phải tải trong ≤ 2 giây.

**Phân trang và khả năng sử dụng**

8. Danh sách tài khoản Customer, kết quả tìm kiếm và lịch sử đơn hàng đều phải có phân trang.
9. Mỗi trang hiển thị tối thiểu 10 bản ghi.
10. Tìm kiếm tài khoản Customer hỗ trợ tối thiểu theo tên hiển thị và email.
