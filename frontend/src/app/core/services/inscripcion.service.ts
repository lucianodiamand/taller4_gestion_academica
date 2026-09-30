import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export enum EstadoInscripcion {
  INSCRIPTO = 'INSCRIPTO',
  REGULAR = 'REGULAR',
  APROBADO = 'APROBADO',
  DESAPROBADO = 'DESAPROBADO',
  CANCELADO = 'CANCELADO',
}

export interface InscripcionAlumno {
  id: number;
  nombre: string;
  apellido: string;
  legajo: string;
  email: string;
}

export interface InscripcionCurso {
  id: number;
  anio: number;
  cuatrimestre: number;
  comision: string;
  materiaNombre?: string;
}

export interface Inscripcion {
  id: number;
  fechaInscripcion: string;
  estado: EstadoInscripcion;
  activo: boolean;
  alumno: InscripcionAlumno;
  curso: InscripcionCurso;
}

export interface InscripcionRequest {
  alumnoId?: number;
  cursoId: number;
}

@Injectable({ providedIn: 'root' })
export class InscripcionService {
  private readonly apiUrl = '/api/inscripciones';

  constructor(private readonly http: HttpClient) {}

  listar(): Observable<Inscripcion[]> {
    return this.http.get<Inscripcion[]>(this.apiUrl);
  }

  listarMias(): Observable<Inscripcion[]> {
    return this.http.get<Inscripcion[]>(`${this.apiUrl}/mias`);
  }

  crear(inscripcion: InscripcionRequest): Observable<Inscripcion> {
    return this.http.post<Inscripcion>(this.apiUrl, inscripcion);
  }

  modificarEstado(id: number, estado: EstadoInscripcion): Observable<Inscripcion> {
    return this.http.put<Inscripcion>(`${this.apiUrl}/${id}/estado`, { estado });
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
