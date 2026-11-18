import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { Auth } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  auth = inject(Auth);
  router = inject(Router);
  cdr = inject(ChangeDetectorRef);
  registering = false;
  visible = false;
  busy = false;
  email = "";
  password = "";
  confirm = "";
  name = "";
  error = "";
  success = "";
  switchMode(value: boolean) {
    this.registering = value;
    this.error = "";
    this.success = "";
    this.password = "";
    this.confirm = "";
  }
  async submit() {
    if (this.busy) return;
    this.error = "";
    this.success = "";
    if (
      this.registering &&
      (this.password !== this.confirm ||
        this.password.length < 12 ||
        this.password.length > 64 ||
        new TextEncoder().encode(this.password).length > 72)
    ) {
      this.error = "รหัสผ่านต้องตรงกัน ยาว 12–64 ตัวอักษร และไม่เกิน 72 ไบต์";
      return;
    }
    this.busy = true;
    try {
      if (this.registering) {
        await this.auth.post(
          "/auth/register",
          JSON.stringify({
            email: this.email,
            displayName: this.name,
            password: this.password,
          }),
          "application/json",
        );
        this.switchMode(false);
        this.success = "สมัครสมาชิกสำเร็จ เข้าสู่ระบบด้วยบัญชีใหม่ได้เลย";
      } else {
        await this.auth.post(
          "/auth/login",
          new URLSearchParams({
            email: this.email.trim(),
            password: this.password,
          }),
          "application/x-www-form-urlencoded",
        );
        const user = await this.auth.refresh();
        if (user) await this.router.navigateByUrl(this.auth.destination(user));
      }
    } catch (e) {
      this.error =
        e instanceof TypeError
          ? "เชื่อมต่อเซิร์ฟเวอร์ไม่ได้ กรุณาตรวจสอบว่า Backend ทำงานอยู่"
          : (e as Error).message;
    } finally {
      this.busy = false;
      this.cdr.markForCheck();
    }
  }
}
