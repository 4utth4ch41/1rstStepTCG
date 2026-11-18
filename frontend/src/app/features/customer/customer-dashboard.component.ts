import { ChangeDetectorRef, Component, inject, type OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Auth } from '../../core/auth/auth.service';

@Component({
  selector: 'app-customer-dashboard',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './customer-dashboard.component.html',
})
export class CustomerDashboardComponent implements OnInit {
  auth = inject(Auth);
  private cdr = inject(ChangeDetectorRef);
  error = '';

  async ngOnInit() {
    try {
      await this.auth.request('/customer/dashboard');
    } catch {
      this.error = 'โหลดข้อมูลไม่สำเร็จ กรุณาลองใหม่';
    } finally {
      this.cdr.markForCheck();
    }
  }
}
