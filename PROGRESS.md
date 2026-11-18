# จุดพักโปรเจกต์ 1srtStepTCG

อัปเดต: 18 พฤศจิกายน 2026

## เป้าหมายและรูปแบบทำงาน
ร้าน Cardfight!! Vanguard ร้านเดียว ภาษาไทย/เงินบาท ใช้ Angular + Spring Boot Java 21
ผู้สมัครได้ CUSTOMER เท่านั้น ผู้เยี่ยมชมดูร้านได้ ระบบสินค้า ตะกร้า จัดส่ง และชำระเงินจริงเป็นขั้นถัดไป
ผู้ใช้ฝึกเขียนแบบจับมือทีละ Step ให้คำใบ้และเฉลยเมื่อขอ มีพื้นฐาน HTML/CSS/PHP และ JS เล็กน้อย ใช้ Ubuntu
ล่าสุดผู้ใช้อนุญาตให้ agent จัดโครงสร้างและย้ายโค้ดทั้งหมด พร้อมลบสิ่งซ้ำที่ไม่ได้ใช้ ซึ่งทำเสร็จแล้ว

## สถานะล่าสุด
- Root component, Profile model, Auth service และ Login component แยกสำเร็จ
- main.ts เหลือ bootstrap เท่านั้น
- app.config.ts ลงทะเบียน router; app.routes.ts กำหนด routes แบบ lazy loading
- core/auth/auth.guard.ts ตรวจ session และ role
- features/auth/login มี TypeScript และ HTML ครบ พร้อมสมัคร/ล็อกอิน
- features/shop, features/admin, features/customer แยกหน้าจาก class Space เดิม
- layouts/store-layout ใช้ header/footer และ logout ร่วมกันผ่าน router-outlet
- styles.css ยังคงเป็น global styles เดิม
- ลบ class Login/Space ที่ฝังใน main.ts และโฟลเดอร์ซ้อนว่างที่ตรวจแล้ว ไม่มีการลบแบบฝึกหัดหรือไฟล์ Backend
- ยืนยัน npm run build ผ่านหลัง refactor Angular แยก lazy chunks ของทุกหน้า
- initial bundle ก่อน refactor 285.36 kB หลัง refactor 234.75 kB (raw size; หน้า Login ยังโหลด chunk เพิ่มเมื่อเข้าใช้งาน)
- ยังไม่ได้ตรวจ UI หรือ navigation หลัง refactor ผ่าน browser automation

## Backend และการรัน
Backend มี session authentication, role authorization, CSRF, BCrypt และจำกัด login/register
H2 แบบเก็บไฟล์ สร้างแอดมินผ่าน scripts/start-backend.sh --create-admin วิธีรันใน README.md
การทดสอบ Backend เดิม 5 รายการผ่านก่อน refactor Frontend; งานรอบนี้ไม่เปลี่ยน Backend
ยังไม่มีจัดการสินค้า ตะกร้า สั่งซื้อ ชำระเงิน ยืนยันอีเมล และกู้รหัสผ่าน

## ครั้งถัดไป
อธิบายโครงสร้างใหม่และเส้นทาง main → config → routes → component ให้ผู้ใช้เข้าใจตามจังหวะ
ให้ลอง login/register, guest shop, admin/customer redirects และ logout บนเว็บหลัง refactor
อย่าเริ่มฟีเจอร์ถัดไปข้ามการเรียนรู้ของผู้ใช้โดยไม่รับ steering

## จุดพักล่าสุด — พักเที่ยง 18 พฤศจิกายน 2026 (ข้อมูลนี้แทนแผนครั้งถัดไปข้างบน)
- ผู้ใช้ติดตั้ง PostgreSQL 16 แล้ว เชื่อม DBeaver สำเร็จที่ 127.0.0.1:5432 ฐานข้อมูล firststep_tcg ผู้ใช้ฐานข้อมูล firststep_app
- เพิ่ม PostgreSQL runtime dependency และ application-postgres.properties แล้ว ใช้ SPRING_PROFILES_ACTIVE=postgres กับ DB_PASSWORD ผ่าน environment ใน Terminal เดิม
- แก้รหัสผ่านจนเชื่อมต่อสำเร็จแล้ว ห้ามขอหรือแสดงรหัสผ่าน
- ผู้ใช้ยืนยันสมัครลูกค้าผ่านเว็บแล้วเห็น CUSTOMER ใน PostgreSQL สร้าง ADMIN ผ่าน start-backend.sh --create-admin และทดสอบ login/refresh แอดมินผ่านแล้ว
- ข้อมูลเดิมใน H2 ยังไม่ได้ย้าย ไม่ลบไฟล์ H2
- เริ่มฟีเจอร์สินค้าแบบสอนทีละ Step ผู้ใช้ลงมือเขียนเอง
- Product.java มี entity ตาราง products: id/name/description/price BigDecimal(12,2)/stock/active พร้อม constructor ว่างสำหรับ JPA, constructor รับข้อมูลสินค้า และ getter ครบ ตรวจโค้ดแล้วถูกต้อง
- ProductRepository.java extends JpaRepository<Product, Long> มี findByActiveTrue()
- ผู้ใช้ยืนยันตาราง products ปรากฏใน PostgreSQL แล้ว
- ผู้ใช้เขียน ProductService มี @Service, @Transactional(readOnly=true), constructor injection, getAllProducts(), getAvailableProducts() และบอกว่า compile ผ่าน
- **ตรวจพบตอนบันทึกว่าชื่อไฟล์จริงเป็น ProductService.jav (ขาด a) จึงยังไม่ถือว่า Maven compile service นี้แล้ว ต้องเริ่มครั้งหน้าด้วยเปลี่ยนชื่อเป็น ProductService.java แล้ว compile อีกครั้ง** ผู้ช่วยยังไม่ได้เปลี่ยนไฟล์โค้ดให้ระหว่างพัก
- ขั้นถัดไปหลังแก้ชื่อ: สอนเพิ่ม method สร้างสินค้า พร้อมตรวจชื่อ ราคา และสต็อก แล้วต่อ Controller/DTO และหน้าแอดมินเพิ่มสินค้า ยังไม่มี API สินค้าและฟอร์มสินค้าที่ใช้งานจริง
- ผู้ใช้มีการปรับเงื่อนไขรหัสผ่านระหว่างเรียนเป็น 8–20 ตัวอักษรในบางส่วน ให้ตรวจโค้ดจริงก่อนอ้างเงื่อนไข อย่าเปลี่ยนกลับโดยอัตโนมัติ
