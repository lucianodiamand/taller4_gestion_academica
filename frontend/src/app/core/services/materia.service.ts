import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface Materia {
  id: number;
  codigo: string;
  nombre: string;
  descripcion: string;
  contenido: string;
  anioCursada: number;
  activo: boolean;
}

export interface MateriaRequest {
  nombre: string;
  descripcion?: string;
  contenido?: string;
  anioCursada: number;
}

@Injectable({ providedIn: 'root' })
export class MateriaService {
  private readonly apiUrl = '/api/materias';

  constructor(private readonly http: HttpClient) {}

  listar(): Observable<Materia[]> {
    return this.http.get<Materia[]>(this.apiUrl);
  }

  obtenerProximoCodigo(): Observable<{ codigo: string }> {
    return this.http.get<{ codigo: string }>(`${this.apiUrl}/proximo-codigo`);
  }

  crear(materia: MateriaRequest): Observable<Materia> {
    return this.http.post<Materia>(this.apiUrl, materia);
  }

  modificar(id: number, materia: MateriaRequest): Observable<Materia> {
    return this.http.put<Materia>(`${this.apiUrl}/${id}`, materia);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
