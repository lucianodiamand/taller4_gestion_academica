import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { Rol } from '../models/auth.model';

export interface Usuario {
  id: number;
  nombre: string;
  apellido: string;
  legajo: string;
  email: string;
  activo: boolean;
  rol: Rol;
}

export interface UsuarioRequest {
  nombre: string;
  apellido: string;
  legajo: string;
  email: string;
  dni: string;
  /** Requerido al crear. Al editar el backend lo ignora, asi que se omite. */
  password?: string;
  rol: Rol;
}

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private readonly apiUrl = '/api/usuarios';

  constructor(private readonly http: HttpClient) {}

  listar(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(this.apiUrl);
  }

  crear(usuario: UsuarioRequest): Observable<Usuario> {
    return this.http.post<Usuario>(this.apiUrl, usuario);
  }

  modificar(id: number, usuario: UsuarioRequest): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.apiUrl}/${id}`, usuario);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}