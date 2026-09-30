import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from '../services/auth.service';

/**
 * Guard para rutas de acceso publico (como el login).
 * Si el usuario ya tiene sesion activa, lo redirige al inicio para que
 * no vuelva a ver el formulario de login.
 */
export const guestGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.estaLogueado()) {
    router.navigate(['/']);
    return false;
  }

  return true;
};
