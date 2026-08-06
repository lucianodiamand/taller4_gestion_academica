import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';

import { AuthService } from '../services/auth.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // 401: no logueado o token vencido -> lo mandamos a login.
      // (403 lo dejamos pasar: el usuario esta logueado pero sin permiso
      // para esa accion puntual, así que cada pantalla decide como avisarle).
      if (error.status === 401) {
        authService.logout();
      }
      return throwError(() => error);
    })
  );
};
