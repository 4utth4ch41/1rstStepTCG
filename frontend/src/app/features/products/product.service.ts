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

  getAdminProducts(): Promise<Product[]> {
    return this.auth.request<Product[]>('/admin/products');
  }

  createProduct(input: CreateProductInput): Promise<Product> {
    return this.auth.post<Product>(
      '/admin/products',
      JSON.stringify(input),
      'application/json',
    );
  }
}