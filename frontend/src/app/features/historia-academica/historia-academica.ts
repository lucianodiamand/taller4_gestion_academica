import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import { AuthService } from '../../core/services/auth.service';
import { Curso, CursoService } from '../../core/services/curso.service';
import { EstadoInscripcion, InscripcionService } from '../../core/services/inscripcion.service';
import { InscripcionExamenService } from '../../core/services/inscripcion-examen.service';

interface NotaRendida {
  tipo: string;
  fecha: string;
  nota: number | null;
}

interface MateriaCursada {
  cursoId: number;
  materiaCodigo: string;
  materiaNombre: string;
  anio: number;
  cuatrimestre: number;
  comision: string;
  estado: EstadoInscripcion;
  notas: NotaRendida[];
}

@Component({
  selector: 'app-historia-academica',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './historia-academica.html',
  styleUrl: './historia-academica.css',
})
export class HistoriaAcademica implements OnInit {
  protected readonly authService = inject(AuthService);
  private readonly inscripcionService = inject(InscripcionService);
  private readonly inscripcionExamenService = inject(InscripcionExamenService);
  private readonly cursoService = inject(CursoService);

  protected readonly EstadoInscripcion = EstadoInscripcion;

  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly materias = signal<MateriaCursada[]>([]);

  protected readonly materiasAprobadas = computed(
    () => this.materias().filter((m) => m.estado === EstadoInscripcion.APROBADO).length,
  );

  protected readonly promedioFinales = computed(() => {
    const notasFinales = this.materias()
      .flatMap((m) => m.notas)
      .filter((n) => n.tipo === 'FINAL' && n.nota !== null)
      .map((n) => n.nota as number);

    if (notasFinales.length === 0) {
      return null;
    }

    const suma = notasFinales.reduce((acc, n) => acc + n, 0);
    return Math.round((suma / notasFinales.length) * 10) / 10;
  });

  ngOnInit(): void {
    forkJoin({
      inscripciones: this.inscripcionService.listarMias(),
      examenes: this.inscripcionExamenService.listar(),
      cursos: this.cursoService.listar(),
    }).subscribe({
      next: ({ inscripciones, examenes, cursos }) => {
        const cursosPorId = new Map<number, Curso>(cursos.map((c) => [c.id, c]));

        const materias: MateriaCursada[] = inscripciones.map((inscripcion) => {
          const curso = cursosPorId.get(inscripcion.curso.id);

          const notas: NotaRendida[] = examenes
            .filter((e) => e.examen.curso.id === inscripcion.curso.id)
            .map((e) => ({
              tipo: e.examen.tipo,
              fecha: e.examen.fecha,
              nota: e.nota,
            }))
            .sort((a, b) => a.fecha.localeCompare(b.fecha));

          return {
            cursoId: inscripcion.curso.id,
            materiaCodigo: curso?.materia.codigo ?? '—',
            materiaNombre: curso?.materia.nombre ?? `Curso #${inscripcion.curso.id}`,
            anio: inscripcion.curso.anio,
            cuatrimestre: inscripcion.curso.cuatrimestre,
            comision: inscripcion.curso.comision,
            estado: inscripcion.estado,
            notas,
          };
        });

        materias.sort((a, b) => b.anio - a.anio || b.cuatrimestre - a.cuatrimestre);

        this.materias.set(materias);
        this.cargando.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.cargando.set(false);
        this.error.set(
          err.status === 403
            ? 'No tenés permiso para ver esta información.'
            : 'No se pudo cargar tu historia académica. Probá de nuevo en unos segundos.',
        );
      },
    });
  }
}