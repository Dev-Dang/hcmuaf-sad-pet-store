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
- Khi thêm hoặc chỉnh sửa địa chỉ, Customer nhập địa chỉ vào ô tìm kiếm; hệ thống sử dụng Google Places Autocomplete để gợi ý địa chỉ thời gian thực. Ngoài ra Customer có thể sử dụng vị trí hiện tại của thiết bị để tự động điền địa chỉ.
- Hệ thống chỉ lưu địa chỉ khi đã xác định được tọa độ (vĩ độ, kinh độ) hợp lệ từ Google Places — tọa độ này phục vụ tính phí giao hàng khi Customer đặt đơn.

### Actor(s):

- Chính: Customer
- Phụ: Google Places API

### Priority: MUST

- Địa chỉ giao hàng có tọa độ hợp lệ là điều kiện để tính phí ship trong quy trình đặt hàng.

### Trigger

1. Customer mở chức năng quản lý địa chỉ giao hàng trong hồ sơ cá nhân.
2. Customer yêu cầu thêm, chỉnh sửa, xóa hoặc đặt mặc định một địa chỉ giao hàng.

### Pre-Conditions

1. Customer đã đăng nhập và phiên đăng nhập còn hiệu lực tại thời điểm bắt đầu thao tác.
2. Hệ thống có dữ liệu hành chính về tỉnh/thành phố và phường/xã để hỗ trợ Customer nhập địa chỉ giao hàng.

### Post-Conditions

**Thành công**

1. Danh sách địa chỉ giao hàng của Customer được cập nhật theo thao tác đã xác nhận.
2. Mỗi địa chỉ được lưu đều có đầy đủ thông tin bắt buộc, tọa độ (vĩ độ, kinh độ) hợp lệ từ Google Places.
3. Nếu Customer đặt địa chỉ mặc định, hệ thống ghi nhận đúng một địa chỉ mặc định trong danh sách.

**Không thành công**

1. Danh sách địa chỉ giao hàng của Customer không thay đổi nếu thao tác không hợp lệ, bị hủy, không hoàn tất hoặc không xác định được tọa độ hợp lệ.

### Normal Flow

**Customer xem danh sách địa chỉ giao hàng và chọn thao tác quản lý.**

8.1.1 Customer — Chọn mục quản lý địa chỉ giao hàng trong hồ sơ cá nhân.\
8.1.2 Hệ thống — Truy xuất danh sách địa chỉ giao hàng của Customer.\
8.1.3 Hệ thống — Hiển thị danh sách địa chỉ giao hàng với thông tin tóm tắt (tên, số điện thoại và địa chỉ ngắn gọn 1 dòng) và gán nhãn rõ ràng cho địa chỉ mặc định (nếu có).\
8.1.4 Hệ thống — Hiển thị tùy chọn thêm địa chỉ giao hàng mới.\
8.1.5 Hệ thống — Hệ thống — Hiển thị các tùy chọn chỉnh sửa, xóa và đặt mặc định phù hợp với từng địa chỉ giao hàng.\
8.1.6 Customer — Chọn một thao tác quản lý địa chỉ giao hàng.

- Nếu Customer chọn thêm địa chỉ giao hàng, thực hiện SF1: Customer thêm địa chỉ giao hàng.
- Nếu Customer chọn chỉnh sửa địa chỉ giao hàng, thực hiện SF2: Customer chỉnh sửa địa chỉ giao hàng.
- Nếu Customer chọn xóa địa chỉ giao hàng, thực hiện SF3: Customer xóa địa chỉ giao hàng.
- Nếu Customer chọn đặt địa chỉ mặc định, thực hiện SF4: Customer đặt địa chỉ giao hàng mặc định.

### Subflow

**SF1: Customer thêm địa chỉ giao hàng**

Tại bước 8.1.4 của Normal Flow, Customer chọn thêm địa chỉ giao hàng.

8.2.1 Hệ thống — Thực hiện SF5 để Customer nhập thông tin nhận hàng và xác định vị trí giao hàng.\
8.2.2 Customer — Xác nhận lưu địa chỉ giao hàng.\
8.2.3 Hệ thống — Kiểm tra tính hợp lệ của địa chỉ giao hàng mới.\
8.2.4 Hệ thống — Lưu địa chỉ giao hàng mới cho Customer.\
8.2.5 Hệ thống — Thông báo thêm địa chỉ giao hàng thành công và hiển thị danh sách địa chỉ giao hàng đã cập nhật.

**SF2: Customer chỉnh sửa địa chỉ giao hàng**

Tại bước 8.1.5 của Normal Flow, Customer chọn chỉnh sửa một địa chỉ giao hàng.

8.3.1 Hệ thống — Truy xuất thông tin chi tiết của địa chỉ giao hàng được chọn.\
8.3.2 Hệ thống — Thực hiện SF5 để Customer cập nhật thông tin nhận hàng và xác định lại vị trí giao hàng.\
8.3.3 Customer — Xác nhận lưu thay đổi địa chỉ giao hàng.\
8.3.4 Hệ thống — Kiểm tra tính hợp lệ của địa chỉ giao hàng gửi lên.\
8.3.5 Hệ thống — Cập nhật địa chỉ giao hàng đã chọn của Customer.\
8.3.6 Hệ thống — Thông báo cập nhật địa chỉ giao hàng thành công và hiển thị danh sách địa chỉ giao hàng đã cập nhật.

**SF3: Customer xóa địa chỉ giao hàng**

Tại bước 8.1.5 của Normal Flow, Customer chọn xóa một địa chỉ giao hàng.

8.4.1 Hệ thống — Hiển thị hộp thoại xác nhận xóa địa chỉ giao hàng.\
8.4.2 Customer — Xác nhận xóa địa chỉ giao hàng.\
8.4.3 Hệ thống — Xác nhận địa chỉ giao hàng được chọn không phải là địa chỉ mặc định.\
8.4.4 Hệ thống — Xóa địa chỉ giao hàng được chọn của Customer.\
8.4.5 Hệ thống — Thông báo xóa địa chỉ giao hàng thành công và hiển thị danh sách địa chỉ giao hàng đã cập nhật.

**SF4: Customer đặt địa chỉ giao hàng mặc định**

Tại bước 8.1.5 của Normal Flow, Customer chọn đặt một địa chỉ giao hàng làm mặc định.

8.5.1 Hệ thống — Hiển thị hộp thoại xác nhận đặt địa chỉ giao hàng mặc định.\
8.5.2 Customer — Xác nhận đặt địa chỉ giao hàng mặc định.\
8.5.3 Hệ thống — Cập nhật địa chỉ giao hàng được chọn thành địa chỉ mặc định; bỏ nhãn mặc định khỏi địa chỉ cũ.\
8.5.4 Hệ thống — Thông báo đặt địa chỉ giao hàng mặc định thành công và hiển thị danh sách địa chỉ giao hàng đã cập nhật.

**SF5: Customer nhập/cập nhật thông tin nhận hàng và xác định vị trí giao hàng**

Tại bước 8.2.1 của SF1 hoặc bước 8.3.2 của SF2, hệ thống thực hiện SF5 để thu thập / cập nhật thông tin nhận hàng và xác định vị trí giao hàng qua Google Places Autocomplete.

8.6.1 Hệ thống — Hiển thị form với các trường: họ tên người nhận, số điện thoại nhận hàng, ô tìm kiếm địa chỉ (Google Places Autocomplete), địa chỉ chi tiết bổ sung; trong đó vĩ độ và kinh độ là trường ẩn, được điền tự động khi Customer chọn địa chỉ từ gợi ý.

- Khi chỉnh sửa (SF2): form hiển thị sẵn dữ liệu địa chỉ hiện tại.
- Khi thêm mới (SF1): form hiển thị trống.

8.6.2 Customer — Nhập họ tên người nhận và số điện thoại nhận hàng.

8.6.3 Customer — Nhập từ khóa địa chỉ vào ô tìm kiếm.

8.6.4 Hệ thống — Gửi từ khóa đến Google Places Autocomplete và hiển thị danh sách gợi ý địa chỉ phù hợp theo thời gian thực.

8.6.5 Customer — Chọn một địa chỉ từ danh sách gợi ý.

8.6.6 Hệ thống — Nhận địa chỉ đã chọn từ Google Places, bao gồm: địa chỉ đầy đủ, vĩ độ và kinh độ.

8.6.7 Hệ thống — Tự động điền địa chỉ đầy đủ vào ô tìm kiếm; điền vĩ độ và kinh độ vào các trường ẩn tương ứng.

8.6.8 Customer — Nhập thêm địa chỉ chi tiết bổ sung nếu cần (số nhà, tầng, ghi chú giao hàng).

8.6.9 Hệ thống — Quay lại bước 8.2.2 của SF1 hoặc bước 8.3.3 của SF2.

### Alternative Flow

**AF1: Customer sử dụng vị trí hiện tại**

Tại bước 8.6.3 của SF5, Customer chọn sử dụng vị trí hiện tại thay vì nhập từ khóa.

8.7.1 Hệ thống — Yêu cầu trình duyệt/thiết bị cung cấp tọa độ vị trí hiện tại của Customer.\
8.7.2 Customer — Cấp quyền truy cập vị trí cho trình duyệt/ứng dụng.\
8.7.3 Hệ thống — Nhận vĩ độ và kinh độ hiện tại từ thiết bị.\
8.7.4 Hệ thống — Gửi tọa độ đến Google Places (Reverse Geocoding) để lấy địa chỉ tương ứng.\
8.7.5 Google Places — Trả về địa chỉ phù hợp với tọa độ đã gửi.\
8.7.6 Hệ thống — Tự động điền địa chỉ vào ô tìm kiếm; điền vĩ độ và kinh độ vào các trường ẩn tương ứng.\
8.7.7 Hệ thống — Quay lại bước 8.6.8 của SF5 để Customer bổ sung địa chỉ chi tiết nếu cần.

### Exception Flow

**EF1: Thông tin địa chỉ giao hàng không hợp lệ**

Tại bước 8.2.3 của SF1 hoặc bước 8.3.4 của SF2, hệ thống phát hiện địa chỉ giao hàng không hợp lệ.

8.8.1 Hệ thống — Thông báo lỗi tương ứng với thông tin không hợp lệ (thiếu họ tên, sai định dạng số điện thoại, chưa chọn địa chỉ từ gợi ý, thiếu tọa độ).\
8.8.2 Hệ thống — Hiển thị lại form cùng dữ liệu Customer đã nhập.\
8.8.3 Hệ thống — Quay lại bước 8.2.2 của SF1 hoặc bước 8.3.3 của SF2.

**EF2: Google Places không trả về gợi ý địa chỉ**

Tại bước 8.6.4 của SF5, Google Places không trả về kết quả gợi ý (lỗi API, timeout, hoặc không có địa chỉ phù hợp với từ khóa).

8.9.1 Hệ thống — Hiển thị thông báo: "Không tìm thấy địa chỉ phù hợp. Vui lòng thử lại với từ khóa khác."\
8.9.2 Hệ thống — Giữ nguyên từ khóa Customer đã nhập trong ô tìm kiếm.\
8.9.3 Customer — Chỉnh sửa từ khóa địa chỉ.\
8.9.4 Hệ thống — Quay lại bước 8.6.4 của SF5.

**EF3: Không thể xác định vị trí hiện tại của Customer**

Tại bước 8.7.1 hoặc 8.7.3 của AF1, Customer từ chối cấp quyền vị trí hoặc thiết bị không trả về tọa độ.

8.10.1 Hệ thống — Hiển thị thông báo: "Không thể xác định vị trí hiện tại. Vui lòng nhập địa chỉ thủ công."\
8.10.2 Hệ thống — Hiển thị lại form với ô tìm kiếm địa chỉ để Customer nhập thủ công.\
8.10.3 Hệ thống — Quay lại bước 8.6.3 của SF5.

**EF4: Xóa địa chỉ giao hàng mặc định**

Tại bước 8.4.3 của SF3, hệ thống phát hiện địa chỉ giao hàng được chọn là địa chỉ mặc định.

8.11.1 Hệ thống — Hiển thị thông báo: "Không thể xóa địa chỉ mặc định. Vui lòng đặt địa chỉ khác làm mặc định trước."\
8.11.2 Hệ thống — Hiển thị danh sách địa chỉ giao hàng hiện tại.\
8.11.3 Hệ thống — Quay lại bước 8.1.6 của Normal Flow.

**EF5: Phiên đăng nhập hết hạn**

Tại bước 8.1.2 của Normal Flow, bước 8.2.4 của SF1, bước 8.3.1 hoặc 8.3.5 của SF2, bước 8.4.4 của SF3, hoặc bước 8.5.3 của SF4, hệ thống phát hiện phiên đăng nhập của Customer đã hết hạn.

8.12.1 Hệ thống — Thông báo phiên đăng nhập đã hết hạn và yêu cầu Customer đăng nhập lại.\
8.12.2 Hệ thống — Điều hướng Customer đến trang đăng nhập.\
8.12.3 Hệ thống — Kết thúc thất bại.

**EF6: Lỗi hệ thống**

Tại bước 8.1.2 của Normal Flow, bước 8.2.4 của SF1, bước 8.3.1 hoặc 8.3.5 của SF2, bước 8.4.4 của SF3, hoặc bước 8.5.3 của SF4, hệ thống không thể hoàn tất thao tác.

8.13.1 Hệ thống — Hiển thị thông báo: "Đã xảy ra lỗi. Vui lòng thử lại sau."\
8.13.2 Hệ thống — Không cập nhật dữ liệu địa chỉ giao hàng của Customer.\
8.13.3 Hệ thống — Kết thúc thất bại.

### Business Rules

1. Mỗi địa chỉ giao hàng phải thuộc về đúng một Customer.
2. Customer chỉ được quản lý các địa chỉ giao hàng thuộc tài khoản của mình.
3. Địa chỉ giao hàng phải có: họ tên người nhận, số điện thoại nhận hàng, địa chỉ đã chọn từ gợi ý Google Places, vĩ độ và kinh độ tương ứng.
4. Số điện thoại nhận hàng phải đúng định dạng số điện thoại Việt Nam: 10 chữ số và bắt đầu bằng 0.
5. Hệ thống chỉ kiểm tra định dạng số điện thoại nhận hàng, không yêu cầu xác thực OTP khi thêm hoặc chỉnh sửa địa chỉ.
6. Customer có thể không có địa chỉ giao hàng nào trong danh sách.
7. Khi danh sách có địa chỉ giao hàng, hệ thống luôn ghi nhận đúng một địa chỉ mặc định.
8. Khi Customer thêm địa chỉ giao hàng đầu tiên, hệ thống tự đặt địa chỉ đó làm địa chỉ mặc định.
9. Customer không được xóa địa chỉ giao hàng mặc định.
10. Customer muốn xóa địa chỉ mặc định phải đặt một địa chỉ giao hàng khác làm mặc định trước.
11. Khi Customer đặt một địa chỉ giao hàng làm mặc định, hệ thống cập nhật để chỉ có đúng một địa chỉ mặc định duy nhất.
12. Khi Customer xóa địa chỉ giao hàng, các đơn hàng đã tạo trước đó không bị thay đổi địa chỉ giao hàng đã lưu snapshot.
13. Hệ thống không yêu cầu email của Customer đã xác thực để Customer quản lý địa chỉ giao hàng.
14. Hệ thống không lưu thay đổi một phần nếu không thể hoàn tất thao tác thêm, chỉnh sửa, xóa hoặc đặt mặc định địa chỉ giao hàng.
15. Customer có thể hủy thao tác thêm, chỉnh sửa, xóa hoặc đặt mặc định bất kỳ lúc nào; khi đó hệ thống hiển thị lại danh sách địa chỉ giao hàng.
16. Hệ thống chỉ cho phép lưu địa chỉ giao hàng khi đã xác định được vĩ độ và kinh độ hợp lệ từ Google Places — tọa độ phải được cung cấp bởi Google Places, không cho phép Customer tự nhập thủ công.
17. Khi Google Places không trả về gợi ý địa chỉ, hệ thống không cập nhật địa chỉ cho đến khi Customer nhập lại từ khóa và chọn từ danh sách gợi ý mới.
18. Vĩ độ và kinh độ được lưu vào DB dưới dạng `DECIMAL(10,8)` và `DECIMAL(11,8)` tương ứng; các giá trị này phục vụ tính phí giao hàng khi Customer đặt đơn hàng.

### Non-Functional Requirements

1. Hệ thống phải bảo vệ dữ liệu địa chỉ giao hàng để Customer không thể xem hoặc chỉnh sửa địa chỉ của Customer khác.
2. Hệ thống phải thông báo rõ khi: thông tin địa chỉ không hợp lệ, Google Places không trả về gợi ý, không thể xác định vị trí hiện tại, phiên đăng nhập hết hạn, địa chỉ mặc định không thể bị xóa, hoặc thao tác quản lý địa chỉ không thể hoàn tất.
3. Danh sách địa chỉ giao hàng phải hiển thị rõ địa chỉ mặc định để Customer dễ nhận biết.
4. Google Places Autocomplete phải hiển thị gợi ý trong vòng 300ms sau khi Customer dừng gõ, để không làm gián đoạn thao tác nhập liệu (debounce phía client).
5. Hệ thống không được gửi request đến Google Places API cho mỗi ký tự Customer gõ — phải áp dụng debounce tối thiểu 300ms phía client.
