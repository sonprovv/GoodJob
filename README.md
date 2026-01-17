# 🏠 Xây dựng ứng dụng **HELPO** tối ưu hóa kết nối khách hàng và người cung cấp dịch vụ chăm sóc tại gia (Xây dựng app phía khách hàng)

---

## I. Giới thiệu tổng quan

**HELPO** là nền tảng bao gồm **ứng dụng di động** và **website**, được xây dựng nhằm tạo ra một **cầu nối đáng tin cậy** giữa **Khách hàng** và **Người cung cấp dịch vụ (Người làm)** trong lĩnh vực **chăm sóc tại gia**.

Hệ thống cho phép người dùng **dễ dàng tìm kiếm, đặt lịch và quản lý dịch vụ**, đồng thời hỗ trợ người làm **chủ động nhận việc**, **quản lý thời gian** và **tạo nguồn thu nhập ổn định**.

---

## II. Các dịch vụ hệ thống cung cấp

HELPO hỗ trợ các nhóm dịch vụ chính sau:

- 🧹 **Dọn dẹp nhà cửa**  
<p align="center">
  <img src="demo/cleaning.gif" width="300"/>
</p>

- 🩺 **Chăm sóc sức khỏe tại gia**
<p align="center">
  <img src="demo/healthcare.gif" width="300"/>
</p>

- 🧼 **Vệ sinh thiết bị gia đình**
<p align="center">
  <img src="demo/maintenance.gif" width="300"/>
</p>


---

## III. Kiến trúc hệ thống & các vai trò người dùng

Hệ thống được thiết kế với **ba thành phần chính**, tương ứng với **ba nhóm người dùng**:

### 1. 📱 Ứng dụng Khách hàng (Kotlin, XML)
- Tìm kiếm và lựa chọn dịch vụ phù hợp
- Đặt lịch và theo dõi tiến trình thực hiện
- Thanh toán dịch vụ
- Đánh giá chất lượng dịch vụ sau khi hoàn thành

---

### 2. 👨‍🔧 Ứng dụng Người làm (Kotlin, Jetpack Compose)
- Nhận và quản lý công việc
- Chủ động sắp xếp lịch làm việc
- Theo dõi thu nhập và lịch sử công việc

<a href="https://github.com/noname1288/WorkerApp">Worker App Repository</a>

---

### 3. 🖥️ Website Quản trị (Admin)
- Quản lý người dùng và dịch vụ
- Theo dõi, kiểm soát đánh giá giữa hai bên
- Tiếp nhận và xử lý khiếu nại
- Hỗ trợ và giám sát hoạt động toàn hệ thống

---

## IV. Công nghệ sử dụng

### 🔹 Front-end
- **Ứng dụng di động**:  
  - Kotlin  
  - Jetpack Compose  
  - XML

- **Website**:  
  - Angular

---

### 🔹 Back-end
- **Node.js**

---

### 🔹 Cơ sở dữ liệu
- **Firebase**

---

### 🔹 Trí tuệ nhân tạo (AI)
- Model API
- **RAG (Retrieval-Augmented Generation)**
- *(Tùy chọn mở rộng)*: **ARAG**
- **Recommendation System** (hệ thống gợi ý dịch vụ)

<a href="https://huggingface.co/spaces/sonpt2304/AI-DoAnTotNghiep">AI Repository</a>

---

## V. Mục tiêu của hệ thống

- Tối ưu hóa quy trình kết nối giữa khách hàng và người cung cấp dịch vụ
- Nâng cao trải nghiệm người dùng thông qua cá nhân hóa và gợi ý thông minh
- Tạo nền tảng minh bạch, an toàn và dễ mở rộng
- Hỗ trợ người lao động có thêm cơ hội việc làm bền vững

---

## VI. 📄 Báo cáo

- [Báo cáo đồ án (PDF)](report/Bao_cao_do_an_2025.pdf)
