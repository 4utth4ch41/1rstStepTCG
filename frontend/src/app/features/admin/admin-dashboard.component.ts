import { ChangeDetectorRef, Component, inject, type OnInit } from '@angular/core';
import { Auth } from '../../core/auth/auth.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [],
  templateUrl: './admin-dashboard.component.html',
})
export class AdminDashboardComponent implements OnInit {
  auth = inject(Auth);
  private cdr = inject(ChangeDetectorRef);
  customers: number | string = '…';
  error = '';

  async ngOnInit() {
    try {
      const data = await this.auth.request<{ customers: number }>('/admin/dashboard');
      this.customers = data.customers;
    } catch {
      this.error = 'โหลดข้อมูลไม่สำเร็จ กรุณาลองใหม่';
    } finally {
      this.cdr.markForCheck();
    }
  }
}
