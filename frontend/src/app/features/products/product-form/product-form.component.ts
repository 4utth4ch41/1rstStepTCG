import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './product-form.component.html',
})
export class ProductFormComponent {
  name = '';
  description = '';
  price = '';
  stock: number | null = 0;
}