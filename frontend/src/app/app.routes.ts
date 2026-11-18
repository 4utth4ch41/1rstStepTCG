import type { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent),
  },
  {
    path: '',
    loadComponent: () => import('./layouts/store-layout/store-layout.component').then(m => m.StoreLayoutComponent),
    children: [
      { path: '', redirectTo: 'login', pathMatch: 'full' },
      {
        path: 'shop',
        loadComponent: () => import('./features/shop/shop.component').then(m => m.ShopComponent),
      },
      {
        path: 'admin',
        canActivate: [authGuard],
        data: { role: 'ADMIN' },
        loadComponent: () => import('./features/admin/admin-dashboard.component').then(m => m.AdminDashboardComponent),
      },
      {
        path: 'dashboard',
        canActivate: [authGuard],
        data: { role: 'CUSTOMER' },
        loadComponent: () => import('./features/customer/customer-dashboard.component').then(m => m.CustomerDashboardComponent),
      },
    ],
  },
  { path: '**', redirectTo: 'login' },
];
