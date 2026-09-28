import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export enum TipoExamen {
  PARCIAL = 'PARCIAL',
  RECUPERATORIO = 'RECUPERATORIO',
  FINAL = 'FINAL',
}

export interface CursoResumen {
  id: number;
  anio: number;
  cuatrimestre: number;
  comision: string;
}

export interface Examen {
  id: number;
  fecha: string;
  tipo: TipoExamen;
  descripcion: string;
  activo: boolean;
  curso: CursoResumen;
}

export interface ExamenRequest {
  fecha: string;
  tipo: TipoExamen;
  descripcion?: string;
  cursoId: number;
}

@Injectable({ providedIn: 'root' })
export class ExamenService {
  private readonly apiUrl = '/api/examenes';

  constructor(private readonly http: HttpClient) {}

  listar(): Observable<Examen[]> {
    return this.http.get<Examen[]>(this.apiUrl);
  }

  buscarPorId(id: number): Observable<Examen> {
    return this.http.get<Examen>(`${this.apiUrl}/${id}`);
  }

  crear(examen: ExamenRequest): Observable<Examen> {
    return this.http.post<Examen>(this.apiUrl, examen);
  }

  modificar(id: number, examen: ExamenRequest): Observable<Examen> {
    return this.http.put<Examen>(`${this.apiUrl}/${id}`, examen);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
