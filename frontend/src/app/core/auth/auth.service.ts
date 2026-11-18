import { Injectable, signal } from '@angular/core';
import type { Profile } from './profile.model';

@Injectable({ providedIn: 'root' })
export class Auth {
  user = signal<Profile | null>(null);
  async request<T = unknown>(path: string, options: RequestInit = {}): Promise<T> {
    const response = await fetch('/api' + path, { ...options, credentials: 'same-origin' });
    if (!response.ok) {
      let message = response.status === 403 ? 'สิทธิ์หรือเซสชันไม่ถูกต้อง กรุณาลองใหม่' : 'ไม่สามารถดำเนินการได้ กรุณาลองใหม่';
      try { message = (await response.json()).message || message; } catch { }
      throw new Error(message);
    }
    return response.status === 204 ? (undefined as T) : response.json();
  }
  async post(path: string, body?: BodyInit, contentType?: string) {
    const csrf = await this.request<{ token: string }>('/auth/csrf');
    return this.request(path, { method: 'POST', body, headers: { 'X-CSRF-TOKEN': csrf.token, ...(contentType ? { 'Content-Type': contentType } : {}) } });
  }
  async refresh() {
    const response = await fetch('/api/auth/me', { credentials: 'same-origin' });
    if (response.status === 401) { this.user.set(null); return null; }
    if (!response.ok) throw new Error('เชื่อมต่อระบบสมาชิกไม่ได้ กรุณาลองใหม่');
    const user: Profile = await response.json(); this.user.set(user); return user;
  }
  destination(user: Profile) { return user.role === 'ADMIN' ? '/admin' : '/dashboard'; }
}
