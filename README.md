# Spring Dev Job
Spring Dev Job là một dịch vụ Restful API được phát triển bởi Spring Boot, cung cấp các chức năng quản lý tuyển dụng IT.
## Tính Năng
- Quản lý người dùng: Đăng ký (thành công thông báo qua gmail), đăng nhập, quên mật khẩu(xác thực bằng mã xác nhận qua gmail)
- Phân quyền người dùng với vai trì ADMIN/HR/PRO/USER
- Quản lý Company: Tạo (kèm upload avatar), sửa, xóa company
- Quản lý Job: Tạo, sửa, xóa, lấy thông tin tất cả job
- Quản lý Resume: Tạo , sửa, xóa, lấy thông tin các CV
- Quản lý Skill: Tạo , sửa, xóa, lấy thông tin các skill
- Tìm kiếm và phân trang: sử dụng Query Method, Criteria API, Specification
- Upload / Download file lên AWS
- Kiểm tra ứng dụng bằng UnitTest
## Công Nghệ Sử Dụng
- **Công cụ build**: Maven >= 3.4.1
- **Java**: 17
- **Framework**: Spring Boot 3.2.x
- **Database**: MySQL
## Hướng dẫn sử dụng 
**1. Clone Repository :**
```java
git clone https://github.com/lean2708/Spring_DevJob.git  
cd Spring_DevJob
```
**2. Cấu hình file application.properties :**
- Cấu hình cơ sở dữ liệu MySQL:
```java
spring.datasource.url=jdbc:mysql://localhost:3306/devjob  
spring.datasource.username=<tên người dùng>  
spring.datasource.password=<mật khẩu>
```
- Cấu hình thông tin email khi bạn cần sử dụng email để thông báo :
```java
spring.mail.username=<email>
spring.mail.password=<mật khẩu ứng dụng của email>
```
- Cấu hình thông tin AWS :
```java
aws.bucket.name= <your-bucket>
aws.accessKey= <your-access-key>
aws.secretKey= <your-secret-key>
spring.profiles.active=${PROFILE:local}
spring.servlet.multipart.max-file-size= <kích thước max 1 file>
spring.servlet.multipart.max-request-size= <kích thước max 1 request>
```
**3. Hướng Dẫn Sử Dụng Swagger (API Document):**
- **Sau khi chạy ứng dụng, bạn có thể truy cập Swagger UI tại:**
```java
http://localhost:8080/devjob/swagger-ui/index.html
```
- Với URL mặc định để lấy tài liệu API ở dạng JSON của Swagger **(phần Explore)**:
```java
/devjob/v3/api-docs
```
Swagger UI sẽ hiển thị các API và cho phép thử nghiệm các chức năng của ứng dụng
- **Cấu Hình JWT Authentication :**
- Để sử dụng các API yêu cầu xác thực, bạn cần thêm JWT token :
- Mở Swagger UI và nhấp vào **Authorize** ở góc trên bên phải
- Nhập token có được sau khi login
- Nhấn **Authorize** để thực hiện được API yêu cầu xác thực

**4. Mẫu thông tin thanh toán VNPAY (Test) :**
| Ngân hàng             | NCB                      |
|-----------------------|--------------------------|
| Số thẻ                | 9704198526191432198      |
| Tên chủ thẻ           | NGUYEN VAN A             |
| Ngày phát hành        | 07/15                    |
| Mật khẩu OTP          | 123456                   |
