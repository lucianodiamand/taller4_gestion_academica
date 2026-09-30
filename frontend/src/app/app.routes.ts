import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';
import { roleGuard } from './core/guards/role.guard';
import { Rol } from './core/models/auth.model';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
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
    path: 'materias',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/materias/materias').then((m) => m.Materias),
  },
  {
    path: 'cursos',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/cursos/cursos').then((m) => m.Cursos),
  },
  {
    path: 'examenes',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/examenes/examenes').then((m) => m.Examenes),
  },
  {
    path: 'perfil',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/perfil/perfil').then((m) => m.Perfil),
  },
  {
    path: 'historia-academica',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/historia-academica/historia-academica').then(
        (m) => m.HistoriaAcademica,
      ),
  },
  {
    path: 'inscripciones',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/inscripciones/inscripciones').then((m) => m.Inscripciones),
  },
  {
    path: 'inscripciones-examen',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/inscripciones-examen/inscripciones-examen').then(
        (m) => m.InscripcionesExamen,
      ),
  },
  {
    path: '**',
    redirectTo: '',
  },
];