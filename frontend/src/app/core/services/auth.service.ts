import { Injectable, computed, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

import { LoginRequest, LoginResponse, Rol, UsuarioAutenticado } from '../models/auth.model';

const TOKEN_KEY = 'gestion_academica_token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  /** El backend corre bajo /api (ver proxy.conf.json en dev). */
  private readonly apiUrl = '/api/auth';

  private readonly usuarioSignal = signal<UsuarioAutenticado | null>(
    this.leerUsuarioDesdeStorage()
  );

  /** Usuario logueado actual (null si no hay sesión o el token venció). */
  readonly usuario = this.usuarioSignal.asReadonly();
  readonly estaLogueado = computed(() => this.usuarioSignal() !== null);
  readonly rol = computed<Rol | null>(() => this.usuarioSignal()?.rol ?? null);

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router
  ) {}

  login(credenciales: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credenciales).pipe(
      tap((response) => {
        localStorage.setItem(TOKEN_KEY, response.token);
        this.usuarioSignal.set(this.decodificarToken(response.token));
      })
    );
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    this.usuarioSignal.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  /** true si el usuario logueado tiene alguno de los roles pedidos. */
  tienePermiso(rolesPermitidos: Rol[]): boolean {
    const rolActual = this.rol();
    return rolActual !== null && rolesPermitidos.includes(rolActual);
  }

  private leerUsuarioDesdeStorage(): UsuarioAutenticado | null {
    const token = this.getToken();
    if (!token || this.tokenExpirado(token)) {
      localStorage.removeItem(TOKEN_KEY);
      return null;
    }
    return this.decodificarToken(token);
  }

  private decodificarToken(token: string): UsuarioAutenticado | null {
    const payload = this.leerPayload(token);
    if (!payload || typeof payload['sub'] !== 'string' || typeof payload['rol'] !== 'string') {
      return null;
    }
    return { legajo: payload['sub'], rol: payload['rol'] as Rol };
  }

  private tokenExpirado(token: string): boolean {
    const payload = this.leerPayload(token);
    if (!payload || typeof payload['exp'] !== 'number') {
      return true;
    }
    return payload['exp'] * 1000 < Date.now();
  }

  private leerPayload(token: string): Record<string, unknown> | null {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=');
      return JSON.parse(atob(padded));
    } catch {
      return null;
    }
  }
}
