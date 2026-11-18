import { ChangeDetectorRef, Component, inject, type OnInit } from '@angular/core';
import { Auth } from '../../core/auth/auth.service';

@Component({
  selector: 'app-shop',
  standalone: true,
  imports: [],
  templateUrl: './shop.component.html',
})
export class ShopComponent implements OnInit {
  auth = inject(Auth);
  private cdr = inject(ChangeDetectorRef);
  error = '';

  async ngOnInit() {
    try {
      await this.auth.refresh();
    } catch {
      this.error = 'โหลดข้อมูลไม่สำเร็จ กรุณาลองใหม่';
    } finally {
      this.cdr.markForCheck();
    }
  }
}
