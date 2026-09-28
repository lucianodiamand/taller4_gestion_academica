import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface CursoMateria {
  id: number;
  codigo: string;
  nombre: string;
}

export interface CursoProfesor {
  id: number;
  nombre: string;
  apellido: string;
  legajo: string;
}

export interface Curso {
  id: number;
  anio: number;
  cuatrimestre: number;
  comision: string;
  activo: boolean;
  materia?: CursoMateria;
  profesor?: CursoProfesor;
}

@Injectable({ providedIn: 'root' })
export class CursoService {
  private readonly apiUrl = '/api/cursos';

  constructor(private readonly http: HttpClient) {}

  listar(): Observable<Curso[]> {
    return this.http.get<Curso[]>(this.apiUrl);
  }
}
