# README

## Cách chạy ứng dụng Android

Ứng dụng này được phát triển bằng **Android Studio**, sử dụng **Java**, **Firebase**, và **Room Database**. Dưới đây là hai cách để chạy ứng dụng:
Link video demo: [https://drive.google.com/file/d/1Qogo8FTmqUZ0ppklgCdWrCGVphIoVNm_/view?usp=sharing]
Link UI: [https://s.net.vn/bWGv]
---

### **Cách 1: Giải nén source và chạy bằng Android Studio**

1. **Giải nén source code**:
   - Tải tệp source code của ứng dụng (soucecode.zip).
   - Giải nén tệp vào một thư mục trên máy tính.
   - file .csv là file import topic mẫu (ví dụ cho trường dữ liêu cần có)
2. **Mở dự án trong Android Studio**:
   - Mở **Android Studio**.
   - Chọn **File > Open** và điều hướng đến thư mục chứa tệp source code vừa giải nén.
   - Chọn thư mục gốc của dự án và nhấn **OK**.

3. **Cài đặt Firebase**:
   - Vào Email xác nhận lời mời vào Firebase
   - Vào Firebase Console: [https://console.firebase.google.com/](https://console.firebase.google.com/project/vopet0028).
   - Đăng nhập bằng **tài khoản: vudinhhong@tdtu.edu.vn**
   - Thêm tệp `google-services.json` vào thư mục `app/` trong dự án.
   - Truy cập vào Firebase database để xem dữ liệu

4. **Kiểm tra và chạy dự án**:
   - Kiểm tra không có lỗi trong file `build.gradle`.
   - Kết nối một thiết bị Android hoặc sử dụng trình giả lập.
   - Nhấn **Run** (hoặc phím tắt `Shift + F10`) để chạy ứng dụng.

5. **Đăng nhập bằng tài khoản admin**:
   - Sử dụng tài khoản admin đã tạo sẵn trên Firebase để đăng nhập và sử dụng toàn bộ chức năng của ứng dụng.
      + Tài khoản 1:
         admin@gmail.com
         admin123
      + Tài khoản 2:
         nguyenhamyjenna2007@gmail.com
         1234567
      + Tài khoản 3:
         traang180204@gmail.com
         123456
   - Tạo tài khoản trên ứng dụng và đăng nhập để sử dụng toàn bộ chức năng của ứng dụng

---

### **Cách 2: Tải file APK và cài đặt trên thiết bị Android**

1. **Tải file APK**:
   - Tải file APK đã được build sẵn từ nguồn cung cấp.
   - File APK thường có tên như `app.apk`.

2. **Cài đặt file APK trên thiết bị Android**:
   - Kết nối thiết bị Android với máy tính qua cáp USB hoặc chia sẻ file qua email, Google Drive, v.v.
   - Mở file APK trên thiết bị Android và cài đặt.
   - Nếu thiết bị yêu cầu, bật **Cài đặt từ nguồn không xác định**:
     - Vào **Cài đặt > Bảo mật > Cài đặt ứng dụng từ nguồn không xác định**.

3. **Sử dụng tài khoản admin Firebase**:
   - Mở ứng dụng đã cài đặt.
   - Đăng nhập bằng tài khoản admin Firebase để sử dụng toàn bộ chức năng.
      + Tài khoản 1:
         `admin@gmail.com`
         `admin123`
      + Tài khoản 2:
         `nguyenhamyjenna2007@gmail.com`
         `123456`
   - Tạo tài khoản trên ứng dụng và đăng nhập để sử dụng toàn bộ chức năng của ứng dụng

---

## Lưu ý quan trọng
- **Firebase**:
  - Ứng dụng sử dụng Firebase để quản lý cơ sở dữ liệu, xác thực người dùng và các dịch vụ khác.
  - Đảm bảo rằng tài khoản google đã xác nhận lời mời admin có quyền truy cập đầy đủ vào Firebase Console.

- **Yêu cầu hệ thống**:
  - Thiết bị Android với **Android 6.0 (API 23)** hoặc cao hơn.
  - Máy tính có môi trường **JDK 17** hoặc cao hơn.
  - Firebase Console được cấu hình đúng với thông tin dự án.
