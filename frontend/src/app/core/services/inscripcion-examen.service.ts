import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface ExamenCurso {
  id: number;
  anio: number;
  cuatrimestre: number;
  comision: string;
}

export interface ExamenInfo {
  id: number;
  fecha: string;
  tipo: string;
  descripcion: string;
  activo: boolean;
  curso: ExamenCurso;
  materiaNombre: string;
}

export interface InscripcionExamenAlumno {
  id: number;
  nombre: string;
  apellido: string;
  legajo: string;
  email: string;
}

export interface InscripcionExamen {
  id: number;
  fechaInscripcion: string;
  nota: number | null;
  activo: boolean;
  alumno: InscripcionExamenAlumno;
  examen: ExamenInfo;
}

export interface InscripcionExamenRequest {
  alumnoId?: number;
  examenId: number;
}

@Injectable({ providedIn: 'root' })
export class InscripcionExamenService {
  private readonly apiUrl = '/api/inscripciones-examen';

  constructor(private readonly http: HttpClient) {}

  listar(): Observable<InscripcionExamen[]> {
    return this.http.get<InscripcionExamen[]>(this.apiUrl);
  }

  crear(request: InscripcionExamenRequest): Observable<InscripcionExamen> {
    return this.http.post<InscripcionExamen>(this.apiUrl, request);
  }

  cargarNota(id: number, nota: number): Observable<InscripcionExamen> {
    return this.http.put<InscripcionExamen>(`${this.apiUrl}/${id}/nota`, { nota });
  }

  cancelar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
