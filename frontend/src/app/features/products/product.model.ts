export interface Product {
  id: number;
  name: string;
  description: string | null;
  price: number;
  stock: number;
  active: boolean;
}

export interface CreateProductInput {
  name: string;
  description: string;
  price: string;
  stock: number;
}