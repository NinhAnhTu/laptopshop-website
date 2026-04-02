# 💻 LaptopShop - Hệ thống bán laptop trực tuyến

![Java](https://img.shields.io/badge/Java-17-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%2520Boot-3.x-brightgreen.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0-orange.svg)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.x-yellow.svg)
![WebSocket](https://img.shields.io/badge/WebSocket-STOMP-blueviolet.svg)
![OAuth2](https://img.shields.io/badge/OAuth2-Google-red.svg)

## 📑 Tổng quan
**LaptopShop** là một nền tảng thương mại điện tử hoàn chỉnh chuyên bán laptop và linh kiện máy tính. Hệ thống được xây dựng với mục tiêu mang lại trải nghiệm mua sắm liền mạch cho khách hàng và cung cấp công cụ quản trị mạnh mẽ cho chủ cửa hàng. Dự án tích hợp nhiều tính năng hiện đại như:
- Đăng nhập bằng Google OAuth2
- Chat hỗ trợ trực tuyến realtime (WebSocket)
- Thanh toán trực tuyến qua VNPay (sandbox)
- Gợi ý sản phẩm thông minh dựa trên lịch sử tìm kiếm
- Quản lý kho bằng serial number
- Hệ thống voucher theo cấp độ tích lũy chi tiêu
- Bảo hành điện tử và gửi email nhắc nhở tự động
- AI tự động trả lời đánh giá 5 sao (Gemini)

---

## 🔡 Tính năng chính

### 👤 Dành cho khách hàng
- **Đăng ký / Đăng nhập:** Tài khoản local hoặc Google OAuth2.
- **Quên mật khẩu:** Gửi link reset qua email (hết hạn 15 phút).
- **Xem sản phẩm:** Danh sách có phân trang, lọc theo danh mục, thương hiệu, giá, đánh giá.
- **Chi tiết sản phẩm:** Thông số kỹ thuật, ảnh, đánh giá (phân trang 3 review/trang).
- **Đánh giá sản phẩm:** Chỉ được đánh giá sau khi đã nhận hàng (xác thực mua hàng).
- **Tìm kiếm thông minh:** Gợi ý realtime, lưu lịch sử tìm kiếm để gợi ý sản phẩm.
- **Giỏ hàng:** Thêm/xóa/sửa số lượng, kiểm tra tồn kho realtime.
- **Voucher:** Xem danh sách mã giảm giá khả dụng, áp dụng tự động, kiểm tra điều kiện tích lũy (20tr/30tr/40tr).
- **Thanh toán:** Chọn sản phẩm cụ thể, nhập địa chỉ, phí ship theo vùng, chọn COD hoặc VNPay.
- **Quản lý đơn hàng:** Xem lịch sử, hủy đơn (khi trạng thái "Chờ xác nhận" hoặc "Chờ thanh toán").
- **Hồ sơ cá nhân:** Cập nhật thông tin, đổi mật khẩu (gửi mail xác nhận mật khẩu mới).
- **Chat hỗ trợ:** Nhắn tin realtime với admin (WebSocket), nhận trả lời từ AI nếu là câu hỏi phổ biến.

### 👨‍💼 Dành cho quản trị viên (Admin)
- **Dashboard:** Biểu đồ doanh thu theo tháng, biểu đồ tròn trạng thái đơn hàng, top 5 sản phẩm bán chạy.
- **Quản lý sản phẩm:** CRUD, upload ảnh thumbnail và ảnh chi tiết, lọc theo danh mục/thương hiệu/giá.
- **Quản lý danh mục, thương hiệu, v.v:** CRUD cơ bản nhà cung cấp, chính sách bảo hành.
- **Quản lý kho (Inventory):** Nhập serial number (hàng loạt cách nhau dấu phẩy), xóa serial, kiểm tra tồn kho thực tế (status: AVAILABLE / SOLD).
- **Quản lý đơn hàng:** Xem danh sách, lọc theo từ khóa/trạng thái/ngày, cập nhật trạng thái (tự động gửi mail thông báo, tự động kích hoạt bảo hành khi "Đã giao").
- **Quản lý người dùng:** CRUD, phân quyền (Customer/Admin), khi đổi mật khẩu sẽ gửi mail thông báo cho người dùng.
- **Quản lý voucher:** Tạo mã giảm giá %, giới hạn số lượng, giá trị đơn tối thiểu, mức giảm tối đa, thời gian hiệu lực.
- **Quản lý đánh giá:** Xem danh sách, lọc theo rating/trạng thái. Trả lời hoặc xóa kèm lý do (gửi mail thông báo). 
- **Quản lý bảo hành:** Xem danh sách phiếu, tìm kiếm, cập nhật hạn và trạng thái, gửi mail nhắc nhở khi sắp hết hạn (7 ngày).
- **Chat hỗ trợ:** Giao diện chat realtime với khách hàng, xem lịch sử, trả lời trực tiếp.

### 🤖 Tự động hóa & AI
- **Chatbot AI:** Sử dụng Gemini để trả lời các câu hỏi thường gặp (chính sách bảo hành, vận chuyển, đổi trả).
- **Auto-reply đánh giá:** Đánh giá 5 sao được Gemini tự động trả lời cảm ơn.
- **Cảnh báo đánh giá thấp:** Đánh giá dưới 5 sao sẽ tự động gửi email cảnh báo cho admin.
- **Nhắc nhở bảo hành:** Mỗi ngày quét và gửi mail nhắc cho khách hàng có bảo hành sắp hết hạn (7 ngày).
- **Tặng voucher tích lũy:** Khách hàng đạt mốc chi tiêu 20tr/30tr/40tr, hệ thống tự động gửi email tặng voucher.

---

## 🛠️ Công nghệ sử dụng

| Thành phần | Công nghệ sử dụng |
| :--- | :--- |
| **Backend** | Spring Boot 3, Spring MVC, Spring Data JPA, Spring Security, Spring WebSocket |
| **Frontend** | Thymeleaf, Bootstrap 5, FontAwesome, JavaScript (AJAX, SockJS, STOMP) |
| **Realtime** | WebSocket (STOMP) với SockJS |
| **Database** | MySQL 8.0 |
| **Authentication** | Form login + OAuth2 Client (Google), BCryptPasswordEncoder |
| **Payment** | VNPay (sandbox) |
| **Email** | JavaMailSender (SMTP) – gửi mail tự động |
| **AI** | Google Gemini API (tự động trả lời chat và đánh giá) |
| **File Upload** | Local storage (thư mục `uploads/`) |
| **Mapping** | ModelMapper |
| **Build Tool** | Maven |

---

## 📁 Cấu trúc thư mục chính

```text
src/
└── main/
    ├── java/com/example/laptopshop/
    │   ├── config/               # Cấu hình (Security, WebSocket, VNPay, MVC)
    │   ├── controller/           # Điều khiển (admin, client, auth, rest)
    │   ├── dto/                  # Data Transfer Objects (request/response)
    │   ├── entity/               # JPA entities
    │   ├── repository/           # JPA repositories
    │   ├── service/              # Business logic + implementation
    │   └── util/                 # UploadService, ...
    └── resources/
        ├── application.properties
        ├── templates/            # Thymeleaf HTML (admin, client, auth, mail)
        ├── static/               # CSS, JS, images
        └── mail/                 # Template email (Thymeleaf)
uploads/                          # Thư mục chứa ảnh sản phẩm, logo (tự động tạo)
```
⚙️ Hướng dẫn cài đặt và chạy
Yêu cầu hệ thống
JDK 17+

MySQL 8.0+

Maven 3.6+

Tài khoản Google Cloud, VNPay (sandbox), Google Gemini API.

Các bước cài đặt
1. Clone repository
   ```bash
   git clone [https://github.com/your-username/laptopshop.git](https://github.com/your-username/laptopshop.git)
   cd laptopshop
   ```
2. Tạo Database MySQL
   ```sql
   CREATE DATABASE laptopshop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
3. Cấu hình kết nối database
   Sửa file src/main/resources/application.properties:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/laptopshop?useSSL=false&serverTimezone=UTC
   spring.datasource.username=root
   spring.datasource.password=your_password
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=true
   ```
4. Cấu hình email (SMTP)
   Thêm vào application.properties:
   ```properties
   spring.mail.host=smtp.gmail.com
   spring.mail.port=587
   spring.mail.username=your_email@gmail.com
   spring.mail.password=your_app_password
   spring.mail.properties.mail.smtp.auth=true
   spring.mail.properties.mail.smtp.starttls.enable=true
   app.email.from=your_email@gmail.com
   app.email.admin-email=admin@laptopshop.com
   ```
5. Cấu hình OAuth2 Google
   Thêm Client ID và Secret vào application.properties:
   ```peoperties
   spring.security.oauth2.client.registration.google.client-id=xxx
   spring.security.oauth2.client.registration.google.client-secret=xxx
   spring.security.oauth2.client.registration.google.scope=email,profile
   ```
6. Cấu hình VNPay & Gemini API
   + Sửa file config/VnPayConfig.java với thông tin test VNPay của bạn.
   + Thêm Gemini API vào application.properties:
   ```properties
   gemini.api.key=your_gemini_api_key
   ```
7. Build và chạy
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
🌐 Truy cập ứng dụng

Trang chủ khách hàng: http://localhost:8080

Đăng nhập admin: http://localhost:8080/login (Tài khoản mặc định: admin@laptopshop.com / admin123)

Quản trị Dashboard: http://localhost:8080/admin/dashboard

Lưu ý: Dữ liệu mẫu cần được khởi tạo lần đầu bằng cách chạy script data.sql hoặc tạo thủ công qua giao diện admin.

## 📊 Entity & Chức năng hệ thống
| Tên Entity        | Vai trò & Chức năng |
|------------------|--------------------|
| **User**         | Người dùng (email, password, fullname, phone, address, userType, resetToken...) |
| **UserType**     | Phân quyền hệ thống (Customer, Admin) |
| **Product**      | Sản phẩm (tên, giá, thông số kỹ thuật, brand, category, supplier, stock...) |
| **ProductImage** | Ảnh sản phẩm (url, isThumbnail) |
| **ProductSerial**| Serial number quản lý kho (status: AVAILABLE / SOLD) |
| **Brand / Category / Supplier / WarrantyPolicy** | Danh mục, thương hiệu, nhà cung cấp, chính sách bảo hành |
| **Cart / CartDetail** | Giỏ hàng và chi tiết sản phẩm trong giỏ |
| **Order / OrderDetail** | Đơn hàng và chi tiết đơn (số lượng, giá, tổng tiền) |
| **Voucher**      | Mã giảm giá (code, discountPercent, maxDiscount, minOrder, quantity, dates) |
| **PaymentMethod**| Phương thức thanh toán (COD, VNPay) |
| **Review / ReviewReply** | Đánh giá sản phẩm và phản hồi của admin |
| **Warranty**     | Phiếu bảo hành (warrantyCode, purchaseDate, expirationDate, status) |
| **ChatMessage**  | Tin nhắn chat giữa user và admin (sender, receiver, content, isRead) |
| **Transaction**  | Giao dịch thanh toán (orderId, amount, status, vnpayResponse) |

🙋‍♂️ Người phát triển
Ninh Anh Tú

⭐ Nếu bạn thấy dự án hữu ích, hãy để lại một star trên GitHub nhé!
📧 Mọi thắc mắc xin liên hệ: ninhanhtu1704@gmail.com
