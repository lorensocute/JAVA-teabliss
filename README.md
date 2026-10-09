# 🍃 TeaBliss — Nền Tảng Đặt Đồ Uống Trực Tuyến

> **Hệ thống đặt trà sữa và đồ uống trực tuyến TeaBliss**, được xây dựng với kiến trúc hướng dịch vụ kết hợp giữa **Spring Boot (Backend REST API)** và **React + Vite (Frontend SPA)**, sử dụng cơ sở dữ liệu **MySQL**.

---

## 📌 Mục Lục

- [Giới Thiệu Dự Án](#-giới-thiệu-dự-án)
- [Công Nghệ Sử Dụng](#-công-nghệ-sử-dụng)
- [Cấu Trúc Thư Mục Repository](#-cấu-trúc-thư-mục-repository)
- [Chức Năng Hệ Thống](#-chức-năng-hệ-thống)
  - [Dành cho Khách hàng (CUSTOMER)](#1-dành-cho-khách-hàng-customer)
  - [Dành cho Quản trị viên (ADMIN)](#2-dành-cho-quản-trị-viên-admin)
- [Cơ Chế Dữ Liệu Mẫu (DataInitializer)](#-cơ-chế-dữ-liệu-mẫu-datainitializer)
- [Yêu Cầu Môi Trường](#-yêu-cầu-môi-trường)
- [Hướng Dẫn Cài Đặt & Khởi Chạy](#-hướng-dẫn-cài-đặt--khởi-chạy)
  - [Bước 1: Clone Repository](#bước-1-clone-repository)
  - [Bước 2: Cấu hình Cơ sở dữ liệu MySQL](#bước-2-cấu-hình-cơ-sở-dữ-liệu-mysql)
  - [Bước 3: Khởi chạy Backend (Spring Boot)](#bước-3-khởi-chạy-backend-spring-boot)
  - [Bước 4: Khởi chạy Frontend (React + Vite)](#bước-4-khởi-chạy-frontend-react--vite)
- [Lưu Ý Về Tài Khoản ADMIN & Phân Quyền](#-lưu-ý-về-tài-khoản-admin--phân-quyền)
- [Các Giới Hạn Chức Năng Hiện Tại](#-các-giới-hạn-chức-năng-hiện-tại)
- [Nhóm Phát Triển](#-nhóm-phát-triển)

---

## 📖 Giới Thiệu Dự Án

**TeaBliss** là giải pháp thương mại điện tử chuyên biệt cho chuỗi cửa hàng đồ uống (trà sữa, trà trái cây, cà phê, matcha). Dự án giải quyết trọn vẹn quy trình từ việc khám phá thực đơn, tùy biến cấu hình đồ uống (size, độ ngọt, mức đá, danh sách topping), đặt hàng thanh toán COD, theo dõi đơn hàng đến việc quản trị tập trung toàn bộ danh mục, sản phẩm, đơn hàng, khách hàng, khuyến mãi và báo cáo doanh thu thực tế.

- **Backend API**: Chạy tại `http://localhost:8080`
- **Frontend Web**: Chạy tại `http://localhost:5173`
- **Authentication**: Phiên làm việc phân tán dựa trên `HttpSession` (Cookie `JSESSIONID`).
- **Thanh toán**: Tiền mặt khi nhận hàng (`COD` — Cash On Delivery).

---

## 🛠 Công Nghệ Sử Dụng

### Backend (Spring Boot)
- **Ngôn ngữ & Nền tảng**: Java 17, Spring Boot 4.x.
- **Dịch vụ Web & REST**: Spring Web MVC, Jakarta Validation (`spring-boot-starter-validation`).
- **Bảo mật & Phiên**: Spring Security (BCrypt password encoding, CORS cấu hình cookie `allowCredentials: true`, quản lý phiên `HttpSession`).
- **Tương tác Cơ sở dữ liệu**: Spring Data JPA / Hibernate ORM.
- **Trình điều khiển cơ sở dữ liệu**: MySQL Connector/J.
- **Công cụ hỗ trợ**: Project Lombok, Maven Wrapper (`mvnw`).

### Frontend (React SPA)
- **Thư viện nền tảng**: React 19, React DOM 19.
- **Định tuyến (Routing)**: React Router DOM v7.
- **Công cụ đóng gói & Máy chủ phát triển**: Vite 8.
- **Phong cách giao diện (Styling)**: Vanilla CSS thuần (Hệ thống thiết kế Dark Navy + Gold sang trọng, hiệu ứng kính mờ Glassmorphism, chuẩn responsive cho cả Desktop và Mobile).
- **Giao tiếp API**: Fetch API bản địa với chế độ gửi cookie `credentials: 'include'`.

### Cơ sở dữ liệu
- **Hệ quản trị CSDL**: MySQL 8.0+
- **Database Schema**: `teabliss_db` (bảng mã `utf8mb4_unicode_ci`).

---

## 📂 Cấu Trúc Thư Mục Repository

Dự án được tổ chức theo mô hình thống nhất (Monorepo), giữ nguyên backend Spring Boot ở thư mục gốc và frontend React đặt trong thư mục `store-fe/`:

```text
JAVA-teabliss/
├── src/                               # Mã nguồn Backend (Spring Boot)
│   ├── main/
│   │   ├── java/Web_Drink_Store/webstore/
│   │   │   ├── config/                # Cấu hình Security, CORS, DataInitializer
│   │   │   ├── controller/            # REST Controllers (Auth, Product, Cart, Order, Admin,...)
│   │   │   ├── dto/                   # Data Transfer Objects (Request / Response)
│   │   │   ├── entity/                # JPA Entities (User, Product, Order, OrderItem,...)
│   │   │   ├── enums/                 # Enums trạng thái, Size, Ice, Role,...
│   │   │   ├── exception/             # Xử lý ngoại lệ toàn cục (@RestControllerAdvice)
│   │   │   ├── repository/            # Spring Data JPA Repositories
│   │   │   └── service/               # Interface & Service Implementations
│   │   └── resources/
│   │       └── application.properties # Cấu hình kết nối MySQL và Hibernate
│   └── test/                          # Unit & Integration tests
├── pom.xml                            # File quản lý phụ thuộc Maven
├── mvnw / mvnw.cmd                    # Maven Wrapper cho Linux/macOS và Windows
├── .gitignore                         # Quy tắc loại trừ target, node_modules, secret
│
└── store-fe/                          # Mã nguồn Frontend (React + Vite)
    ├── public/                        # Tài nguyên tĩnh (favicon, icons, ảnh sản phẩm webp)
    │   └── images/products/           # 20 ảnh sản phẩm thực tế theo danh mục
    ├── src/
    │   ├── assets/                    # Hình ảnh, banner tĩnh
    │   ├── components/                # Component tái sử dụng (Navbar, Footer, ProductCard, Toast,...)
    │   ├── context/                   # Quản lý trạng thái toàn cục (CartContext)
    │   ├── pages/                     # Các trang ứng dụng:
    │   │   ├── HomePage               # Trang chủ giới thiệu & sản phẩm tiêu biểu
    │   │   ├── ProductsPage           # Danh sách thực đơn, lọc danh mục, tìm kiếm, sắp xếp
    │   │   ├── ProductDetailPage     # Chi tiết món, cấu hình Size, Đường, Đá, Topping
    │   │   ├── CartPage               # Giỏ hàng, cập nhật số lượng, xóa món
    │   │   ├── CheckoutPage           # Thanh toán COD, địa chỉ, nhập mã voucher
    │   │   ├── OrdersPage             # Lịch sử và chi tiết các đơn hàng cá nhân
    │   │   ├── ProfilePage            # Hồ sơ, đổi mật khẩu, sổ địa chỉ nhận hàng
    │   │   ├── AuthPage               # Đăng nhập & Đăng ký tài khoản
    │   │   └── AdminPage              # Trang Quản trị Dashboard 6 Tab chức năng
    │   ├── services/api.js            # Module gọi REST API tập trung
    │   ├── utils/                     # Helper xử lý ảnh, chuẩn hóa tên tiếng Việt
    │   ├── App.jsx                    # Cấu hình Route chính
    │   ├── index.css                  # Design Tokens & Global CSS
    │   └── main.jsx                   # Điểm khởi chạy React
    ├── package.json                   # Quản lý dependencies npm
    └── vite.config.js                 # Cấu hình Vite Dev Server
```

---

## 🚀 Chức Năng Hệ Thống

### 1. Dành cho Khách hàng (CUSTOMER)

- **Xác thực & Tài khoản**:
  - Đăng ký tài khoản mới và đăng nhập an toàn (mật khẩu mã hóa BCrypt).
  - Quản lý hồ sơ cá nhân (Họ tên, Số điện thoại) và đổi mật khẩu.
  - Sổ địa chỉ: Quản lý CRUD danh sách địa chỉ nhận hàng, chọn địa chỉ mặc định.
- **Khám phá Thực đơn**:
  - Trang chủ trình diễn thông điệp thương hiệu, các dòng sản phẩm đặc trưng.
  - Bộ lọc sản phẩm theo nhóm danh mục: Trà Sữa, Trà Trái Cây, Cà Phê, Matcha.
  - Tìm kiếm món theo từ khóa và sắp xếp linh hoạt theo giá bán.
- **Tùy biến ly đồ uống (Product Customization)**:
  - Chọn Size: Size M (chuẩn), Size L (+6.000 ₫), Size XL (+10.000 ₫).
  - Chọn độ ngọt: 0%, 30%, 50%, 70%, 100%.
  - Chọn mức đá: Nóng, Ít đá (30%), Đá vừa (70%), Đầy đá (100%).
  - Chọn Toppings: Trân châu đen (+5.000 ₫), Trân châu trắng (+5.000 ₫), Thạch nha đam (+6.000 ₫), Kem cheese (+10.000 ₫), Pudding trứng (+8.000 ₫),...
- **Giỏ hàng & Đặt hàng (Cart & Checkout)**:
  - Xem giỏ hàng, điều chỉnh số lượng hoặc xóa món.
  - Lưu giữ toàn vẹn snapshot đơn giá, cấu hình ly và toppings tại thời điểm đặt.
  - Nhập mã voucher khuyến mãi (hỗ trợ giảm theo % hoặc số tiền cố định, tự động kiểm tra điều kiện đơn tối thiểu, mức giảm tối đa và ngày hiệu lực).
  - Phương thức thanh toán khi nhận hàng (`COD`).
  - Tự động trừ tồn kho và làm trống giỏ hàng sau khi đặt thành công.
- **Lịch sử & Chi tiết đơn hàng**:
  - Theo dõi trạng thái đơn hàng: `PENDING` (Chờ duyệt) ➔ `CONFIRMED` (Đã xác nhận) ➔ `SHIPPING` (Đang giao) ➔ `COMPLETED` (Hoàn thành) hoặc `CANCELLED` (Đã hủy).
  - Xem chi tiết từng ly đồ uống đã đặt kèm danh sách topping, số tiền tạm tính, số tiền giảm giá và tổng thanh toán.

### 2. Dành cho Quản trị viên (ADMIN)

Trang quản trị tập trung tại đường dẫn `/admin` gồm 6 phân hệ:

1. **Báo cáo Thống kê (Dashboard)**:
   - Thống kê tổng số người dùng, đơn hàng, tổng sản phẩm (hiển thị rõ số lượng món đang mở bán).
   - **Doanh thu thực thu (COMPLETED)**: Doanh thu thực tế của các đơn hoàn thành sau khi đã trừ chiết khấu voucher.
   - Thống kê phân bố đơn hàng theo 5 trạng thái.
   - **Top sản phẩm bán chạy**: Sắp xếp theo số lượng ly bán ra giảm dần, tính doanh thu tạm tính theo món dựa trên dữ liệu `OrderItem` thực tế.
2. **Quản lý Sản phẩm**:
   - Danh sách sản phẩm kèm hình ảnh, danh mục, giá bán, tồn kho.
   - Thêm sản phẩm mới hoặc cập nhật thông tin sản phẩm.
   - Bật/tắt trạng thái mở bán (`ACTIVE` / `INACTIVE` — cơ chế xóa mềm soft delete).
3. **Quản lý Danh mục**:
   - Thêm mới, chỉnh sửa thông tin danh mục đồ uống.
   - Bật/tắt trạng thái hoạt động của danh mục.
4. **Quản lý Đơn hàng**:
   - Danh sách toàn bộ đơn hàng khách đặt, hỗ trợ xem thông tin giao hàng snapshot.
   - Xem chi tiết cấu hình từng ly đồ uống trong đơn hàng.
   - Chuyển trạng thái đơn hàng theo đúng chu trình nghiệp vụ:
     - `PENDING` ➔ `CONFIRMED` hoặc `CANCELLED`
     - `CONFIRMED` ➔ `SHIPPING` hoặc `CANCELLED`
     - `SHIPPING` ➔ `COMPLETED`
5. **Quản lý Người dùng**:
   - Danh sách tài khoản kèm vai trò (`CUSTOMER` / `ADMIN`) và trạng thái.
   - Tìm kiếm nhanh người dùng theo họ tên, email hoặc số điện thoại.
   - Khóa hoặc mở khóa tài khoản (bảo vệ an toàn dữ liệu, không xóa vật lý).
6. **Quản lý Khuyến mãi**:
   - Tạo mã voucher giảm theo tỷ lệ phần trăm (%) hoặc số tiền cố định (VNĐ).
   - Cài đặt thời hạn hiệu lực (`startAt`, `endAt`), giá trị đơn tối thiểu và mức giảm tối đa.
   - Hiển thị trực quan 4 trạng thái thời gian: `● Đang hiệu lực`, `⏳ Chưa bắt đầu`, `⏰ Hết hạn`, `○ Đã tắt`.

---

## 📦 Cơ Chế Dữ Liệu Mẫu (DataInitializer)

Khi ứng dụng Backend Spring Boot khởi chạy, class [`DataInitializer`](src/main/java/Web_Drink_Store/webstore/config/DataInitializer.java) sẽ kiểm tra cơ sở dữ liệu và tự động tạo dữ liệu mẫu khởi đầu nếu chưa tồn tại:

- **4 Nhóm Danh mục**: Trà sữa, Trà trái cây, Cà phê, Matcha.
- **20 Món đồ uống thực tế** (5 món cho mỗi danh mục) kèm giá tiền, mô tả chi tiết và số lượng tồn kho khởi điểm 100 ly cho mỗi món.

> [!IMPORTANT]
> **Lưu ý quan trọng về tài khoản:** Mã nguồn `DataInitializer` **KHÔNG** tự động tạo sẵn bất kỳ tài khoản người dùng hoặc tài khoản `ADMIN` mặc định nào. Tất cả người dùng khi tự đăng ký qua trang Web sẽ mặc định có quyền `CUSTOMER`. Để tạo tài khoản `ADMIN`, vui lòng xem mục [Hướng dẫn phân quyền Admin](#-lưu-ý-về-tài-khoản-admin--phân-quyền) bên dưới.

---

## 💻 Yêu Cầu Môi Trường

Trước khi cài đặt, hãy đảm bảo máy tính của bạn đã cài đặt các phần mềm sau:

- **Java Development Kit (JDK)**: Phiên bản **17** trở lên.
- **Node.js**: Phiên bản **18.x** trở lên (kèm `npm` 9.x+).
- **MySQL Server**: Phiên bản **8.0** trở lên.
- **Git**: Đã cài đặt trên hệ điều hành.
- *(Không bắt buộc cài đặt Maven riêng vì dự án đã tích hợp sẵn Maven Wrapper `mvnw`).*

---

## 🛠 Hướng Dẫn Cài Đặt & Khởi Chạy

### Bước 1: Clone Repository

Mở terminal và tải mã nguồn dự án về máy:

```bash
git clone https://github.com/lorensocute/JAVA-teabliss.git
cd JAVA-teabliss
```

---

### Bước 2: Cấu hình Cơ sở dữ liệu MySQL

1. Mở MySQL Client hoặc MySQL Workbench và tạo database mới:

```sql
CREATE DATABASE teabliss_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. Cấu hình biến môi trường `DB_PASSWORD` tương ứng với mật khẩu MySQL của bạn:

- **Trên Windows (PowerShell):**
  ```powershell
  $env:DB_PASSWORD="your_mysql_password"
  ```
- **Trên Windows (CMD):**
  ```cmd
  set DB_PASSWORD=your_mysql_password
  ```
- **Trên Linux / macOS (Bash/Zsh):**
  ```bash
  export DB_PASSWORD="your_mysql_password"
  ```

*(Lưu ý: Thay `your_mysql_password` bằng mật khẩu người dùng `root` MySQL trên máy của bạn).*

---

### Bước 3: Khởi chạy Backend (Spring Boot)

Tại thư mục gốc của repository (`JAVA-teabliss/`), chạy lệnh Maven Wrapper:

- **Trên Windows (PowerShell / CMD):**
  ```powershell
  .\mvnw spring-boot:run
  ```
- **Trên Linux / macOS:**
  ```bash
  ./mvnw spring-boot:run
  ```

- Khi thấy log hiển thị `Started WebstoreApplication in ... seconds`, Backend đã sẵn sàng tại cổng **`8080`**.
- Hibernate sẽ tự động khởi tạo bảng (`ddl-auto=update`) và nạp dữ liệu mẫu ban đầu qua `DataInitializer`.

---

### Bước 4: Khởi chạy Frontend (React + Vite)

Mở một cửa sổ terminal mới, di chuyển vào thư mục `store-fe/`:

```bash
cd store-fe

# Cài đặt các gói phụ thuộc
npm install

# Khởi chạy máy chủ phát triển
npm run dev
```

- Trình duyệt sẽ mở ứng dụng tại: **`http://localhost:5173`**.

---

## 🔐 Lưu Ý Về Tài Khoản ADMIN & Phân Quyền

Do ứng dụng không sinh sẵn tài khoản Admin để bảo đảm an toàn, bạn có thể thiết lập tài khoản quản trị theo các bước sau:

1. Mở giao diện website tại `http://localhost:5173/auth`, chuyển sang tab **Đăng Ký** và tạo tài khoản (ví dụ: `admin@teabliss.vn`).
2. Mở MySQL client và cấp quyền `ADMIN` cho tài khoản này bằng câu lệnh SQL:
   ```sql
   USE teabliss_db;
   UPDATE users SET role = 'ADMIN' WHERE email = 'admin@teabliss.vn';
   ```
3. Quay lại website, đăng nhập bằng tài khoản trên. Biểu tượng **👑 Quản trị** sẽ xuất hiện trên thanh điều hướng hoặc truy cập trực tiếp đường dẫn `http://localhost:5173/admin` để vào Dashboard.

---

## ⚠️ Các Giới Hạn Chức Năng Hiện Tại

- **Phương thức thanh toán:** Hiện chỉ hỗ trợ **COD (Thanh toán tiền mặt khi nhận hàng)**. Chưa tích hợp cổng thanh toán trực tuyến (VNPAY, MoMo, ZaloPay).
- **Cơ chế xác thực:** Sử dụng cơ chế session-based thông qua cookie `JSESSIONID` của Spring Boot. Khi chạy cục bộ, yêu cầu trình duyệt chấp nhận cookie giữa hai origin `localhost:5173` và `localhost:8080`.
- **Cơ chế xóa an toàn:** Các thao tác xóa Danh mục, Sản phẩm, Khách hàng hoặc Mã khuyến mãi đều áp dụng **Xóa mềm (Soft Delete / Inactive)** nhằm duy trì toàn vẹn dữ liệu cho lịch sử đơn hàng và thống kê tài chính.

---

## 👥 Nhóm phát triển

Dự án TeaBliss được thực hiện bởi các thành viên:
- Thư
- Diệp Anh
- Huệ
- Thơ

---

<p align="center">
  <sub>Phát triển bởi đội ngũ TeaBliss • 2026</sub>
</p>