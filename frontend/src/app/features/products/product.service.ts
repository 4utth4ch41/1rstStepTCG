import { Injectable, inject } from '@angular/core';

import { Auth } from '../../core/auth/auth.service';
import type {
  Product,
  CreateProductInput,
} from './product.model';

@Injectable({
  providedIn: 'root',
})
export class ProductService {
  private auth = inject(Auth);

  getPublicProducts(): Promise<Product[]> {
    return this.auth.request<Product[]>('/products');
  }

  getAdminProducts(): Promise<Product[]> {
    return this.auth.request<Product[]>('/admin/products');
  }

  createProduct(input: CreateProductInput, images: File[] = []): Promise<Product> {
    const body = new FormData();
    body.append('product', new Blob([JSON.stringify(input)], { type: 'application/json' }));
    images.forEach(image => body.append('images', image));
    // The browser sets the multipart boundary; do not supply Content-Type manually.
    return this.auth.post<Product>('/admin/products', body);
  }
}
