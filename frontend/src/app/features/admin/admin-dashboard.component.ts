import { ChangeDetectorRef, Component, inject, type OnInit } from '@angular/core';
import { Auth } from '../../core/auth/auth.service';
import { DecimalPipe } from '@angular/common';
import { ProductService } from '../products/product.service';
import type { Product } from '../products/product.model';
import { ProductFormComponent } from '../products/product-form/product-form.component';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [DecimalPipe, ProductFormComponent],
  templateUrl: './admin-dashboard.component.html',
})
export class AdminDashboardComponent implements OnInit {
  auth = inject(Auth);
  private cdr = inject(ChangeDetectorRef);
  customers: number | string = '…';
  error = '';

  private productService = inject(ProductService);
  products: Product[] = [];
  loading = true;

  async ngOnInit() {
    try {
      const [dashboard, products] = await Promise.all([
        this.auth.request<{ customers: number }>('/admin/dashboard'),
        this.productService.getAdminProducts(),
      ]);

      this.customers = dashboard.customers;
      this.products = products;
    } catch {
      this.error = 'โหลดข้อมูลไม่สำเร็จ กรุณาลองใหม่';
    } finally {
      this.loading = false;
      this.cdr.markForCheck();
    }
  }
}
