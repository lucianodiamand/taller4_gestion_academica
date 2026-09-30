import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { SearchBar } from '../../core/components/search-bar/search-bar';
import { Pagination } from '../../core/components/pagination/pagination';
import { Rol } from '../../core/models/auth.model';
import { AuthService } from '../../core/services/auth.service';
import { ConfirmService } from '../../core/services/confirm.service';
import { Curso, CursoService } from '../../core/services/curso.service';
import {
  EstadoInscripcion,
  Inscripcion,
  InscripcionRequest,
  InscripcionService,
} from '../../core/services/inscripcion.service';
import { Usuario, UsuarioService } from '../../core/services/usuario.service';
import { Paginator, TableSorter, filtrar } from '../../core/utils/tabla';

type ModoFormulario = 'crear' | 'estado' | null;

@Component({
  selector: 'app-inscripciones',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, SearchBar, Pagination],
  templateUrl: './inscripciones.html',
  styleUrl: './inscripciones.css',
})
export class Inscripciones implements OnInit {
  private readonly inscripcionService = inject(InscripcionService);
  private readonly cursoService = inject(CursoService);
  private readonly usuarioService = inject(UsuarioService);
  private readonly confirmService = inject(ConfirmService);
  protected readonly authService = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  // permisos segun lo que permite el backend
  protected get esAdmin(): boolean {
    return this.authService.tienePermiso([Rol.ADMIN]);
  }
  protected get esAlumno(): boolean {
    return this.authService.tienePermiso([Rol.ALUMNO]);
  }
  protected get puedeVer(): boolean {
    return true;
  }
  protected get puedeCrear(): boolean {
    return this.authService.tienePermiso([Rol.ADMIN, Rol.ALUMNO]);
  }
  protected get puedeGestionarEstado(): boolean {
    return this.authService.tienePermiso([Rol.ADMIN, Rol.PROFESOR]);
  }
  protected get puedeDarBaja(): boolean {
    return this.authService.tienePermiso([Rol.ADMIN]);
  }

  protected readonly inscripciones = signal<Inscripcion[]>([]);
  protected readonly cursos = signal<Curso[]>([]);
  protected readonly alumnos = signal<Usuario[]>([]);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly termino = signal('');
  protected readonly sorter = new TableSorter();
  protected readonly paginator = new Paginator();
  protected readonly filas = computed(() =>
    this.sorter.ordenar(filtrar(this.inscripciones(), this.termino())),
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

  protected readonly cursosDisponibles = computed(() => {
    if (!this.esAlumno) {
      return this.cursos();
    }
    const yaInscripto = new Set(
      this.inscripciones()
        .filter((i) => i.activo)
        .map((i) => i.curso.id),
    );
    return this.cursos().filter((c) => !yaInscripto.has(c.id));
  });

  protected irPagina(pagina: number): void {
    this.paginator.pagina.set(pagina);
  }

  protected readonly modo = signal<ModoFormulario>(null);
  protected readonly guardando = signal(false);
  protected readonly errorFormulario = signal<string | null>(null);

  private inscripcionEnEdicion: Inscripcion | null = null;

  protected readonly estadosPermitidos = [
    EstadoInscripcion.INSCRIPTO,
    EstadoInscripcion.REGULAR,
    EstadoInscripcion.LIBRE,
    EstadoInscripcion.APROBADO,
    EstadoInscripcion.DESAPROBADO,
  ];

  protected readonly form = this.fb.nonNullable.group({
    alumnoId: [null as number | null, Validators.required],
    cursoId: [null as number | null, Validators.required],
  });

  protected readonly estadoForm = this.fb.nonNullable.group({
    estado: [EstadoInscripcion.INSCRIPTO, Validators.required],
  });

  ngOnInit(): void {
    if (this.puedeVer) {
      this.cargarInscripciones();
    }
  }

  cargarInscripciones(): void {
    this.cargando.set(true);
    this.error.set(null);

    const peticion = this.esAlumno
      ? this.inscripcionService.listarMias()
      : this.inscripcionService.listar();

    peticion.subscribe({
      next: (data) => {
        this.inscripciones.set(data);
        this.cargando.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.cargando.set(false);
        this.error.set(
          err.status === 403
            ? 'No tenés permiso para ver las inscripciones.'
            : 'No se pudo cargar la lista de inscripciones.',
        );
      },
    });
  }

  abrirFormularioCrear(): void {
    this.inscripcionEnEdicion = null;
    this.errorFormulario.set(null);
    this.form.reset({ alumnoId: null, cursoId: null });

    if (this.esAdmin) {
      this.form.controls.alumnoId.setValidators(Validators.required);
      this.cargarAlumnos();
    } else {
      this.form.controls.alumnoId.clearValidators();
    }
    this.form.controls.alumnoId.updateValueAndValidity();

    this.cargarCursos();
    this.modo.set('crear');
  }

  abrirFormularioEstado(inscripcion: Inscripcion): void {
    this.inscripcionEnEdicion = inscripcion;
    this.errorFormulario.set(null);
    this.estadoForm.reset({ estado: inscripcion.estado });
    this.modo.set('estado');
  }

  cerrarFormulario(): void {
    this.modo.set(null);
    this.inscripcionEnEdicion = null;
    this.errorFormulario.set(null);
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const valores = this.form.getRawValue();

    if (!valores.cursoId) {
      this.errorFormulario.set('Debés seleccionar un curso.');
      return;
    }

    if (this.esAdmin && !valores.alumnoId) {
      this.errorFormulario.set('Debés seleccionar un alumno.');
      return;
    }

    const payload: InscripcionRequest = {
      cursoId: Number(valores.cursoId),
    };

    if (this.esAdmin && valores.alumnoId) {
      payload.alumnoId = Number(valores.alumnoId);
    }

    this.guardando.set(true);
    this.errorFormulario.set(null);

    this.inscripcionService.crear(payload).subscribe({
      next: () => this.onGuardadoExitoso(),
      error: (err: HttpErrorResponse) => this.onErrorGuardando(err),
    });
  }

  guardarEstado(): void {
    if (!this.inscripcionEnEdicion) {
      return;
    }

    this.guardando.set(true);
    this.errorFormulario.set(null);

    this.inscripcionService
      .modificarEstado(this.inscripcionEnEdicion.id, this.estadoForm.getRawValue().estado)
      .subscribe({
        next: () => this.onGuardadoExitoso(),
        error: (err: HttpErrorResponse) => this.onErrorGuardando(err),
      });
  }

  darDeBaja(inscripcion: Inscripcion): void {
    this.confirmService
      .confirmar({
        titulo: 'Cancelar inscripción',
        mensaje: `¿Cancelar la inscripción de ${inscripcion.alumno.nombre} ${inscripcion.alumno.apellido} al curso ${inscripcion.curso.anio} - ${inscripcion.curso.cuatrimestre}º cuat. (comisión ${inscripcion.curso.comision})?`,
        textoConfirmar: 'Cancelar inscripción',
        textoCancelar: 'Volver',
      })
      .subscribe((confirmado) => {
        if (!confirmado) {
          return;
        }

        this.error.set(null);
        this.inscripcionService.eliminar(inscripcion.id).subscribe({
          next: () => this.cargarInscripciones(),
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

  private cargarCursos(): void {
    this.cursoService.listar().subscribe({
      next: (data) => this.cursos.set(data.filter((c) => c.activo)),
      error: () => console.error('no se pudieron cargar los cursos'),
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
