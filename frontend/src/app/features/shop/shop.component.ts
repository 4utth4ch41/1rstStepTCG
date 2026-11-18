import { ChangeDetectorRef, Component, inject, type OnInit } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { Auth } from '../../core/auth/auth.service';
import { ProductService } from '../products/product.service';
import type { Product } from '../products/product.model';
import { ProductGalleryComponent } from '../products/product-gallery/product-gallery.component';

@Component({
  selector: 'app-shop',
  standalone: true,
  imports: [DecimalPipe, ProductGalleryComponent],
  templateUrl: './shop.component.html',
})
export class ShopComponent implements OnInit {
  private auth = inject(Auth);
  private productsService = inject(ProductService);
  private cdr = inject(ChangeDetectorRef);
  products: Product[] = [];
  loading = true;
  error = '';

  async ngOnInit() {
    // Session state and the public catalog load independently.
    void this.auth.refresh().catch(() => null);
    try {
      this.products = await this.productsService.getPublicProducts();
    } catch {
      this.error = 'โหลดสินค้าไม่สำเร็จ กรุณาลองใหม่';
    } finally {
      this.loading = false;
      this.cdr.markForCheck();
    }
  }
}
