# 1srtStepTCG

ร้าน Cardfight!! Vanguard ร้านเดียว ภาษาไทยและเงินบาท พัฒนาด้วย Angular และ Spring Boot / Java 21

## Step 1: ระบบสมาชิกและสิทธิ์

- `/login`: เข้าสู่ระบบ / สมัครบัญชีลูกค้า
- `/shop`: หน้าร้านสำหรับทุกคน ยังไม่มีระบบสินค้าและตะกร้า
- `/admin`: แดชบอร์ดแอดมินเบื้องต้น ยังไม่เปิดจัดการสินค้า
- `/dashboard`: แดชบอร์ดลูกค้าเบื้องต้น
- Backend ตรวจสิทธิ์แยกจาก Angular route guard เสมอ
- Session cookie แบบ HttpOnly, SameSite=Lax, อายุเมื่อไม่ใช้งาน 30 นาที และ CSRF token
- BCrypt cost 12; รหัสผ่าน 8–20 ตัวอักษร สูงสุด 72 UTF-8 bytes
- จำกัด login/register รวม 15 ครั้งต่อนาทีต่อ IP ภายใน process (รีเซ็ตเมื่อ restart)
- สมัครผ่าน API ได้ CUSTOMER เท่านั้น ไม่รับ role จากผู้ใช้
- บัญชีเก็บใน H2 ที่ `backend/data/` และยังอยู่หลัง restart

## เปิดใช้งานในเครื่อง

ต้องมี Java 21, Maven, Node.js >=22.22.3 ในสาย 22 และ npm

Terminal แรก จากโฟลเดอร์โปรเจกต์:

```bash
./scripts/start-backend.sh --create-admin
```

กรอกอีเมลและรหัสผ่านแอดมินของคุณ รหัสผ่านจะไม่แสดงบนจอและไม่ถูกบันทึกใน source code
สคริปต์สร้างแอดมินเฉพาะเมื่ออีเมลนั้นยังไม่มี หากเป็นแอดมินอยู่แล้วจะคงรหัสผ่านเดิมไว้ หากเป็นลูกค้าจะไม่ยกระดับสิทธิ์ให้
หลังสร้างครั้งแรก ใช้ `./scripts/start-backend.sh` ได้เลย

Terminal ที่สอง:

```bash
cd frontend
npm ci
npm start
```

เปิด http://localhost:4200 และใช้ hostname เดิมตลอดเพื่อให้ session cookie ต่อเนื่อง
Angular proxy ส่ง `/api` ไป Spring Boot บน 127.0.0.1:8080 ไม่ต้องเปิด CORS

Maven script ใช้ cache `/tmp/firststep-m2` ที่เตรียมระหว่างพัฒนา หากเครื่องล้าง `/tmp` Maven จะดาวน์โหลดใหม่
รันแบบมาตรฐานด้วย `cd backend` แล้ว `mvn spring-boot:run` ก็ได้ โดยจะใช้ Maven cache ของผู้ใช้แทน

## สมัครบัญชีลูกค้าในเครื่อง

1. เปิด Backend และ Frontend ตามขั้นตอนข้างต้น สำหรับการทดสอบบัญชีลูกค้าอย่างเดียว เปิด Backend ด้วย `./scripts/start-backend.sh` ได้เลย
2. เปิด `http://127.0.0.1:4200/login` แล้วเลือก **สมัครสมาชิก**
3. กรอกชื่อที่แสดง เช่น `ลูกค้าทดสอบ` และอีเมลที่ยังไม่มีบัญชี เช่น `customer01@example.test`
4. ตั้งรหัสผ่าน 8–20 ตัวอักษร เช่น `Try12345` สำหรับการทดสอบในเครื่อง แล้วกรอกรหัสเดียวกันในช่อง **ยืนยันรหัสผ่าน**
5. กด **สร้างบัญชีลูกค้า** เมื่อสมัครสำเร็จ หน้าจะสลับไปแท็บ **เข้าสู่ระบบ** พร้อมข้อความยืนยัน
6. กรอกอีเมลและรหัสผ่านที่เพิ่งสมัคร แล้วกด **เข้าสู่ระบบ** ระบบจะพาไปที่ `/dashboard`

ถ้าหน้าเว็บแสดง 8–20 ตัวอักษร แต่ข้อความแจ้งเตือนยังระบุ 12–64 ให้หยุด Backend ตัวเดิมด้วย `Ctrl+C` แล้วรัน `./scripts/start-backend.sh` จากโฟลเดอร์โปรเจกต์ จากนั้นรีเฟรชหน้าเว็บแล้วลองสมัครอีกครั้ง

ถ้าขึ้นว่าใช้อีเมลนี้สมัครไม่ได้ ให้ลองเข้าสู่ระบบด้วยบัญชีเดิมหรือเลือกอีเมลทดสอบใหม่ ฐานข้อมูลจะเก็บบัญชีเดิมไว้หลังรีสตาร์ต

## ตรวจสอบ

```bash
cd backend
mvn -Dmaven.repo.local=/tmp/firststep-m2 test
```

```bash
cd frontend
npm run build
```

ลองสมัครลูกค้าแล้วเข้าสู่ระบบ ต้องไป `/dashboard`; แอดมินต้องไป `/admin`
ลูกค้าเปิด `/admin` จะกลับ `/dashboard` และเรียก API แอดมินจะได้ 403
ผู้เยี่ยมชมเปิดแดชบอร์ดจะไป `/login`; กดเข้าชมร้านไป `/shop` ได้
เมื่อออกจากระบบแล้วต้องเข้าหน้าส่วนตัวไม่ได้

## ขอบเขตปัจจุบัน

เป็นเวอร์ชันพัฒนาในเครื่อง ยังไม่เชื่อมชำระเงิน ไม่ส่งอีเมลยืนยัน ไม่รองรับลืมรหัสผ่าน และยังไม่มีข้อมูลสินค้า
ก่อนเปิดร้านจริงต้องเพิ่มยืนยันอีเมล/กู้รหัสผ่าน ใช้ฐานข้อมูล production และ migration ตั้ง HTTPS พร้อม `COOKIE_SECURE=true` และระบบจำกัดคำขอที่รองรับ proxy/หลาย instance
การเปลี่ยนอีเมลหรือรหัสผ่านแอดมินใน environment ไม่ได้แก้บัญชีที่สร้างไปแล้ว

เอกสารอ้างอิง: https://angular.dev/reference/versions และ https://docs.spring.io/spring-boot/3.5/reference/web/spring-security.html

## โครงสร้าง Frontend

```text
frontend/src/
├── main.ts                         # เริ่ม Angular
├── styles.css                      # styles ร่วม
└── app/
    ├── app.component.ts / .html     # root และ router-outlet
    ├── app.config.ts               # providers
    ├── app.routes.ts               # lazy routes
    ├── core/auth/                  # service, guard และ profile model
    ├── layouts/store-layout/       # header, footer และ logout ร่วม
    └── features/
        ├── auth/login/             # login และสมัครสมาชิก
        ├── shop/                   # หน้าร้านสาธารณะ
        ├── admin/                  # dashboard แอดมิน
        └── customer/               # dashboard ลูกค้า
```

แต่ละหน้ามี `.component.ts` และ `.component.html` แยกกัน โดย styles ยังใช้จาก `styles.css`
Routes โหลด component เมื่อใช้งาน ส่วน guard ตรวจสิทธิ์ก่อนเปิดหน้าส่วนตัว และ Backend ยังคงตรวจสิทธิ์ API เสมอ
