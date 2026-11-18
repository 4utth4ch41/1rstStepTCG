import { Component, input, signal } from '@angular/core';

@Component({
  selector: 'app-product-gallery',
  standalone: true,
  templateUrl: './product-gallery.component.html',
  styleUrl: './product-gallery.component.css',
})
export class ProductGalleryComponent {
  images = input<string[]>([]);
  productName = input('สินค้า');
  selectedIndex = signal(0);
}
