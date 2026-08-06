import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Usuario {
  id: number;
  nombre: string;
  apellido: string;
  legajo: string;
  email: string;
  activo: boolean;
  rol: string;
}

export interface UsuarioRequest {
  nombre: string;
  apellido: string;
  legajo: string;
  email: string;
  dni: string;
  password: string;
  rol: string;
}

@Injectable({
  providedIn: 'root'
})
export class UsuarioService {

  private readonly apiUrl = '/api/usuarios';

  constructor(private http: HttpClient) {}

  listar(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(this.apiUrl);
  }

  crear(usuario: UsuarioRequest): Observable<Usuario> {
    return this.http.post<Usuario>(
      this.apiUrl,
      usuario
    );
  }

  modificar(id:number, usuario:UsuarioRequest):Observable<Usuario>{
    return this.http.put<Usuario>(
      `${this.apiUrl}/${id}`,
      usuario
    );
  }

  eliminar(id:number):Observable<void>{
    return this.http.delete<void>(
      `${this.apiUrl}/${id}`
    );
  }
}