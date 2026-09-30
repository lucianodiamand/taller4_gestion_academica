import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Pagination } from '../../core/components/pagination/pagination';
import { SearchBar } from '../../core/components/search-bar/search-bar';
import { Rol } from '../../core/models/auth.model';
import { AuthService } from '../../core/services/auth.service';
import { ConfirmService } from '../../core/services/confirm.service';
import { Examen, ExamenService } from '../../core/services/examen.service';
import {
  InscripcionExamen,
  InscripcionExamenRequest,
  InscripcionExamenService,
} from '../../core/services/inscripcion-examen.service';
import { InscripcionService } from '../../core/services/inscripcion.service';
import { Usuario, UsuarioService } from '../../core/services/usuario.service';
import { Paginator, TableSorter, filtrar } from '../../core/utils/tabla';

type ModoFormulario = 'crear' | 'nota' | null;

@Component({
  selector: 'app-inscripciones-examen',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, SearchBar, Pagination],
  templateUrl: './inscripciones-examen.html',
  styleUrl: './inscripciones-examen.css',
})
export class InscripcionesExamen implements OnInit {
  private readonly inscripcionExamenService = inject(InscripcionExamenService);
  private readonly inscripcionService = inject(InscripcionService);
  private readonly examenService = inject(ExamenService);
  private readonly usuarioService = inject(UsuarioService);
  private readonly confirmService = inject(ConfirmService);
  protected readonly authService = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  protected get esAdmin(): boolean {
    return this.authService.tienePermiso([Rol.ADMIN]);
  }
  protected get esAlumno(): boolean {
    return this.authService.tienePermiso([Rol.ALUMNO]);
  }
  protected get puedeCrear(): boolean {
    return this.authService.tienePermiso([Rol.ADMIN, Rol.ALUMNO]);
  }
  protected get puedeCargarNota(): boolean {
    return this.authService.tienePermiso([Rol.ADMIN, Rol.PROFESOR]);
  }
  protected get puedeCancelar(): boolean {
    return this.authService.tienePermiso([Rol.ADMIN, Rol.ALUMNO]);
  }

  protected readonly inscripciones = signal<InscripcionExamen[]>([]);
  protected readonly examenes = signal<Examen[]>([]);
  protected readonly alumnos = signal<Usuario[]>([]);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly tab = signal<'general' | 'inscritos' | 'finalizados'>(
    this.authService.tienePermiso([Rol.ALUMNO]) ? 'general' : 'inscritos',
  );
  protected readonly termino = signal('');
  protected readonly sorter = new TableSorter();
  protected readonly paginator = new Paginator();

  protected readonly inscripcionesPorTab = computed(() =>
    this.tab() === 'finalizados'
      ? this.inscripciones().filter((i) => this.esFechaPasada(i.examen.fecha))
      : this.inscripciones().filter((i) => !this.esFechaPasada(i.examen.fecha)),
  );

  protected readonly filas = computed(() =>
    this.sorter.ordenar(filtrar(this.inscripcionesPorTab(), this.termino())),
  );
  protected readonly totalPaginas = computed(() =>
    this.paginator.totalPaginas(this.filas().length),
  );
  protected readonly paginaActual = computed(() =>
    this.paginator.paginaActual(this.filas().length),
  );
  protected readonly filasPaginadas = computed(() => {
    const inicio = this.paginaActual() * this.paginator.tamanoPagina();
    return this.filas().slice(inicio, inicio + this.paginator.tamanoPagina());
  });

  protected readonly examenesDisponibles = computed(() => {
    let base = this.examenes();
    if (this.esAlumno) {
      const inscritos = new Set(
        this.inscripciones()
          .filter((i) => !this.esFechaPasada(i.examen.fecha))
          .map((i) => i.examen.id),
      );
      base = base.filter((e) => !inscritos.has(e.id));
    }
    return filtrar(base, this.termino());
  });

  protected readonly mostrarAcciones = computed(() =>
    this.inscripcionesPorTab().some((i) => this.tieneAcciones(i)),
  );

  protected readonly modo = signal<ModoFormulario>(null);
  protected readonly guardando = signal(false);
  protected readonly errorFormulario = signal<string | null>(null);

  private examenSeleccionado: Examen | null = null;
  private inscripcionEnEdicion: InscripcionExamen | null = null;

  protected readonly form = this.fb.nonNullable.group({
    alumnoId: [null as number | null, Validators.required],
  });

  protected readonly notaForm = this.fb.nonNullable.group({
    nota: [
      null as number | null,
      [Validators.required, Validators.min(1), Validators.max(10)],
    ],
  });

  ngOnInit(): void {
    this.cargarInscripciones();
    this.cargarExamenes();
    if (this.esAdmin) {
      this.cargarAlumnos();
    }
  }

  cargarInscripciones(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.inscripcionExamenService.listar().subscribe({
      next: (data) => {
        this.inscripciones.set(data);
        this.cargando.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.cargando.set(false);
        this.error.set(
          err.status === 403
            ? 'No tenés permiso para ver las inscripciones a examen.'
            : 'No se pudo cargar la lista de inscripciones a examen.',
        );
      },
    });
  }

  anotarse(examen: Examen): void {
    if (this.esAdmin) {
      this.examenSeleccionado = examen;
      this.errorFormulario.set(null);
      this.form.reset({ alumnoId: null });
      this.modo.set('crear');
      return;
    }

    this.confirmService
      .confirmar({
        titulo: 'Anotarse a examen',
        mensaje: `¿Anotarte al examen ${examen.tipo} del ${examen.fecha}?`,
        textoConfirmar: 'Anotarse',
        textoCancelar: 'Volver',
      })
      .subscribe((confirmado) => {
        if (!confirmado) {
          return;
        }

        this.guardando.set(true);
        this.error.set(null);
        this.inscripcionExamenService.crear({ examenId: examen.id }).subscribe({
          next: () => {
            this.guardando.set(false);
            this.cargarInscripciones();
            this.cargarExamenes();
          },
          error: (err: HttpErrorResponse) => {
            this.guardando.set(false);
            this.error.set(
              typeof err.error === 'string'
                ? err.error
                : 'No se pudo anotarte al examen.',
            );
          },
        });
      });
  }

  confirmarAnotarse(): void {
    if (!this.examenSeleccionado || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const payload: InscripcionExamenRequest = {
      alumnoId: Number(this.form.getRawValue().alumnoId),
      examenId: this.examenSeleccionado.id,
    };

    this.guardando.set(true);
    this.errorFormulario.set(null);

    this.inscripcionExamenService.crear(payload).subscribe({
      next: () => this.onGuardadoExitoso(),
      error: (err: HttpErrorResponse) => this.onErrorGuardando(err),
    });
  }

  abrirFormularioNota(inscripcion: InscripcionExamen): void {
    this.inscripcionEnEdicion = inscripcion;
    this.errorFormulario.set(null);
    this.notaForm.reset({ nota: inscripcion.nota });
    this.modo.set('nota');
  }

  cerrarFormulario(): void {
    this.modo.set(null);
    this.examenSeleccionado = null;
    this.inscripcionEnEdicion = null;
    this.errorFormulario.set(null);
  }

  guardarNota(): void {
    if (!this.inscripcionEnEdicion || this.notaForm.invalid) {
      this.notaForm.markAllAsTouched();
      return;
    }

    this.guardando.set(true);
    this.errorFormulario.set(null);

    this.inscripcionExamenService
      .cargarNota(this.inscripcionEnEdicion.id, Number(this.notaForm.getRawValue().nota))
      .subscribe({
        next: () => this.onGuardadoExitoso(),
        error: (err: HttpErrorResponse) => this.onErrorGuardando(err),
      });
  }

  cancelar(inscripcion: InscripcionExamen): void {
    this.confirmService
      .confirmar({
        titulo: 'Cancelar inscripción',
        mensaje: `¿Cancelar la inscripción de ${inscripcion.alumno.nombre} ${inscripcion.alumno.apellido} al examen ${inscripcion.examen.tipo} del ${inscripcion.examen.fecha}?`,
        textoConfirmar: 'Cancelar inscripción',
        textoCancelar: 'Volver',
      })
      .subscribe((confirmado) => {
        if (!confirmado) {
          return;
        }

        this.error.set(null);
        this.inscripcionExamenService.cancelar(inscripcion.id).subscribe({
          next: () => {
            this.cargarInscripciones();
            this.cargarExamenes();
          },
          error: (err: HttpErrorResponse) => {
            this.error.set(
              typeof err.error === 'string'
                ? err.error
                : 'No se pudo cancelar la inscripción.',
            );
          },
        });
      });
  }

  protected irPagina(pagina: number): void {
    this.paginator.pagina.set(pagina);
  }

  protected esFechaPasada(fecha: string): boolean {
    return fecha < this.hoy();
  }

  protected esFechaFutura(fecha: string): boolean {
    return fecha > this.hoy();
  }

  private tieneAcciones(inscripcion: InscripcionExamen): boolean {
    const muestraCargarNota = this.puedeCargarNota && !this.esFechaFutura(inscripcion.examen.fecha);
    const muestraCancelar = this.puedeCancelar && inscripcion.nota === null && !this.esFechaPasada(inscripcion.examen.fecha);
    return muestraCargarNota || muestraCancelar;
  }

  private hoy(): string {
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  }

  private cargarExamenes(): void {
    this.examenService.listar().subscribe({
      next: (examenes) => {
        const disponibles = examenes.filter((e) => e.activo && !this.esFechaPasada(e.fecha));
        if (this.esAlumno) {
          this.inscripcionService.listarMias().subscribe({
            next: (mias) => {
              const cursos = new Set(mias.map((i) => i.curso.id));
              this.examenes.set(disponibles.filter((e) => cursos.has(e.curso.id)));
            },
            error: () => this.examenes.set([]),
          });
        } else {
          this.examenes.set(disponibles);
        }
      },
      error: () => console.error('no se pudieron cargar los examenes'),
    });
  }

  private cargarAlumnos(): void {
    this.usuarioService.listar().subscribe({
      next: (data) =>
        this.alumnos.set(data.filter((u) => u.rol === Rol.ALUMNO && u.activo)),
      error: () => console.error('no se pudieron cargar los alumnos'),
    });
  }

  private onGuardadoExitoso(): void {
    this.guardando.set(false);
    this.cerrarFormulario();
    this.cargarInscripciones();
    this.cargarExamenes();
  }

  private onErrorGuardando(err: HttpErrorResponse): void {
    this.guardando.set(false);
    this.errorFormulario.set(
      typeof err.error === 'string' && err.error.length > 0
        ? err.error
        : 'No se pudo guardar. Revisá los datos e intentá de nuevo.',
    );
  }
}
