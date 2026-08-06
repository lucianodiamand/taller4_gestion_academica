import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { Rol } from './core/models/auth.model';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/login/login').then((m) => m.Login),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./features/home/home').then((m) => m.Home),
  },
  {
    path: 'usuarios',
    canActivate: [roleGuard([Rol.ADMIN])],
    loadComponent: () =>
      import('./features/usuarios/usuarios').then((m) => m.Usuarios),
  },
  {
    path: '**',
    redirectTo: '',
  },
];