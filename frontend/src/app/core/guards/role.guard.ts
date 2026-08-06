import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { Rol } from '../models/auth.model';
import { AuthService } from '../services/auth.service';

/**
 * Uso en app.routes.ts:
 *   { path: 'usuarios', canActivate: [roleGuard([Rol.ADMIN])], ... }
 */
export function roleGuard(rolesPermitidos: Rol[]): CanActivateFn {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.estaLogueado()) {
      router.navigate(['/login']);
      return false;
    }

    if (!authService.tienePermiso(rolesPermitidos)) {
      router.navigate(['/']);
      return false;
    }

    return true;
  };
}
