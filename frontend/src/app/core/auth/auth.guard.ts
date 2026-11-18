import { inject } from '@angular/core';
import { Router, type CanActivateFn } from '@angular/router';
import { Auth } from './auth.service';

export const authGuard: CanActivateFn = async (route) => {
  const auth = inject(Auth),
    router = inject(Router);
  try {
    const user = await auth.refresh();
    if (!user) return router.parseUrl("/login");
    return (
      user.role === route.data["role"] ||
      router.parseUrl(auth.destination(user))
    );
  } catch {
    return router.parseUrl("/login");
  }
};
