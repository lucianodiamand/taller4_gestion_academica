import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface CursoMateria {
  id: number;
  codigo: string;
  nombre: string;
  descripcion?: string;
  anioCursada: number;
  activo: boolean;
}

export interface CursoProfesor {
  id: number;
  nombre: string;
  apellido: string;
  legajo: string;
  email: string;
}

export interface Curso {
  id: number;
  anio: number;
  cuatrimestre: number;
  comision: string;
  activo: boolean;
  profesor: CursoProfesor;
  materia: CursoMateria;
}

export interface CursoRequest {
  anio: number;
  cuatrimestre: number;
  comision: string;
  profesorId: number;
  materiaId: number;
}

@Injectable({ providedIn: 'root' })
export class CursoService {
  private readonly apiUrl = '/api/cursos';
  private readonly materiasUrl = '/api/materias';

  constructor(private readonly http: HttpClient) {}

  listar(): Observable<Curso[]> {
    return this.http.get<Curso[]>(this.apiUrl);
  }

  buscarPorId(id: number): Observable<Curso> {
    return this.http.get<Curso>(`${this.apiUrl}/${id}`);
  }

  crear(curso: CursoRequest): Observable<Curso> {
    return this.http.post<Curso>(this.apiUrl, curso);
  }

  modificar(id: number, curso: CursoRequest): Observable<Curso> {
    return this.http.put<Curso>(`${this.apiUrl}/${id}`, curso);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Obtiene la lista de materias activas para poblar el selector en el formulario de cursos.
   */
  listarMaterias(): Observable<CursoMateria[]> {
    return this.http.get<CursoMateria[]>(this.materiasUrl);
  }
}
