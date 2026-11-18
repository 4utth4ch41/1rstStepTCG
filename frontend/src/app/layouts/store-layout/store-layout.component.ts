import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { Auth } from '../../core/auth/auth.service';

@Component({
  selector: 'app-store-layout',
  standalone: true,
  imports: [RouterLink, RouterOutlet],
  templateUrl: './store-layout.component.html',
})
export class StoreLayoutComponent {
  auth = inject(Auth);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);
  error = '';
  busy = false;

  async logout() {
    if (this.busy) return;
    this.error = '';
    this.busy = true;
    try {
      await this.auth.post('/auth/logout');
      this.auth.user.set(null);
      await this.router.navigateByUrl('/login');
    } catch {
      this.error = 'ออกจากระบบไม่สำเร็จ กรุณาลองใหม่';
    } finally {
      this.busy = false;
      this.cdr.markForCheck();
    }
  }
}
