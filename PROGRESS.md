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
