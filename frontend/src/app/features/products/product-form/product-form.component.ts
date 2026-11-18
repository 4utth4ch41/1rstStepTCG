import { ChangeDetectorRef, Component, ElementRef, ViewChild, inject, output, type OnDestroy } from '@angular/core';
import { FormsModule, type NgForm } from '@angular/forms';
import { ProductService } from '../product.service';
import type { Product } from '../product.model';

type SelectedImage = { file: File; preview: string };

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './product-form.component.html',
  styleUrl: './product-form.component.css',
})
export class ProductFormComponent implements OnDestroy {
  private productService = inject(ProductService);
  private cdr = inject(ChangeDetectorRef);
  @ViewChild('modal', { static: true }) private modal!: ElementRef<HTMLDialogElement>;
  @ViewChild('productForm') private form?: NgForm;
  @ViewChild('imageInput') private imageInput?: ElementRef<HTMLInputElement>;
  created = output<Product>();

  name = '';
  description = '';
  price = '';
  stock: number | null = 0;
  images: SelectedImage[] = [];
  saving = false;
  error = '';
  imageError = '';
  success = '';
  private previousOverflow = '';
  private opened = false;

  open() {
    if (this.opened) return;
    this.error = '';
    this.success = '';
    this.previousOverflow = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    this.modal.nativeElement.showModal();
    this.opened = true;
  }

  requestClose() {
    if (this.saving) return;
    if (this.hasChanges() && !window.confirm('ยังไม่ได้บันทึกสินค้า ต้องการยกเลิกข้อมูลที่กรอกหรือไม่?')) return;
    this.reset();
    this.close();
  }

  onCancel(event: Event) {
    event.preventDefault();
    this.requestClose();
  }

  onBackdropClick(event: MouseEvent) {
    if (event.target !== this.modal.nativeElement) return;
    const rect = this.modal.nativeElement.getBoundingClientRect();
    if (event.clientX < rect.left || event.clientX > rect.right || event.clientY < rect.top || event.clientY > rect.bottom) {
      this.requestClose();
    }
  }

  selectImages(event: Event) {
    const input = event.target as HTMLInputElement;
    const files = Array.from(input.files ?? []);
    this.imageError = '';
    this.error = '';
    if (this.images.length + files.length > 5) {
      this.imageError = 'เพิ่มได้สูงสุด 5 รูป กรุณาเลือกใหม่น้อยลง';
    } else if (files.some(file => !['image/jpeg', 'image/png'].includes(file.type))) {
      this.imageError = 'รองรับรูป JPEG และ PNG เท่านั้น';
    } else if (files.some(file => file.size === 0 || file.size > 5 * 1024 * 1024)) {
      this.imageError = 'แต่ละรูปต้องมีขนาดมากกว่า 0 และไม่เกิน 5 MB';
    } else {
      this.images = [...this.images, ...files.map(file => ({ file, preview: URL.createObjectURL(file) }))];
    }
    input.value = '';
  }

  removeImage(index: number) {
    URL.revokeObjectURL(this.images[index].preview);
    this.images = this.images.filter((_, i) => i !== index);
    this.imageError = '';
  }

  async submit(form: NgForm) {
    if (this.saving) return;
    this.error = '';
    this.success = '';
    if (form.invalid || !this.name.trim()) {
      form.control.markAllAsTouched();
      this.error = 'กรุณากรอกชื่อ ราคา และสต็อกให้ถูกต้อง';
      return;
    }
    if (this.stock === null || !Number.isInteger(this.stock) || this.stock < 0 || this.stock > 2147483647) {
      this.error = 'สต็อกต้องเป็นจำนวนเต็มตั้งแต่ 0 ถึง 2,147,483,647';
      return;
    }
    this.saving = true;
    try {
      const product = await this.productService.createProduct({
        name: this.name.trim(), description: this.description, price: this.price, stock: this.stock,
      }, this.images.map(image => image.file));
      this.reset();
      this.close();
      this.success = 'เพิ่มสินค้าเรียบร้อยแล้ว';
      this.created.emit(product);
    } catch (error) {
      this.error = error instanceof Error ? error.message : 'เพิ่มสินค้าไม่สำเร็จ กรุณาลองใหม่';
    } finally {
      this.saving = false;
      this.cdr.markForCheck();
    }
  }

  private hasChanges() {
    return !!(this.form?.dirty || this.images.length || this.name || this.description || this.price || (this.stock !== null && this.stock !== 0));
  }

  private reset() {
    this.form?.resetForm({ productName: '', productDescription: '', productPrice: '', productStock: 0 });
    this.name = ''; this.description = ''; this.price = ''; this.stock = 0;
    this.images.forEach(image => URL.revokeObjectURL(image.preview));
    this.images = [];
    this.imageError = ''; this.error = '';
    if (this.imageInput) this.imageInput.nativeElement.value = '';
  }

  private close() {
    this.modal.nativeElement.close();
    document.body.style.overflow = this.previousOverflow;
    this.opened = false;
  }

  ngOnDestroy() {
    this.images.forEach(image => URL.revokeObjectURL(image.preview));
    if (this.opened) {
      this.modal.nativeElement.close();
      document.body.style.overflow = this.previousOverflow;
    }
  }
}
